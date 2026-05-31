"""Reliable profile lookup with web_profile_info fallback.

Instaloader 4.15.x often raises ProfileNotExistsException for valid profiles when
Instagram blocks anonymous GraphQL/doc_id requests (403/401). The web_profile_info
REST endpoint still works with browser-like headers, or when logged in.
"""

from __future__ import annotations

import re
from typing import Optional

import instaloader
from instaloader import Profile
from instaloader.exceptions import (
    ConnectionException,
    LoginRequiredException,
    ProfileNotExistsException,
    PrivateProfileNotFollowedException,
)

# Instagram web app id (public, used by instagram.com frontend).
_IG_APP_ID = "936619743392459"

_DEFAULT_USER_AGENT = (
    "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/AP2A.240805.005) "
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
)


def normalize_username(username: str) -> str:
    value = username.strip().lstrip("@")
    value = value.split("/")[0].split("?")[0]
    return value.lower()


def _web_headers(loader: instaloader.Instaloader, username: str) -> dict:
    session = loader.context._session
    user_agent = (
        session.headers.get("User-Agent")
        or getattr(loader.context, "user_agent", None)
        or _DEFAULT_USER_AGENT
    )
    return {
        "User-Agent": user_agent,
        "Accept": "*/*",
        "Accept-Language": "en-US,en;q=0.9",
        "X-IG-App-ID": _IG_APP_ID,
        "X-Requested-With": "XMLHttpRequest",
        "Referer": f"https://www.instagram.com/{username}/",
        "Origin": "https://www.instagram.com",
    }


def _fetch_via_web_profile_info(
    loader: instaloader.Instaloader,
    username: str,
) -> Profile:
    url = f"https://www.instagram.com/api/v1/users/web_profile_info/?username={username}"
    response = loader.context._session.get(
        url,
        headers=_web_headers(loader, username),
        timeout=loader.context.request_timeout,
    )

    if response.status_code == 404:
        raise ProfileNotExistsException(f"Profile {username} does not exist.")

    if response.status_code in {401, 403}:
        raise LoginRequiredException(
            "Instagram blocked this request. Log in from the Login screen and try again."
        )

    response.raise_for_status()
    payload = response.json()
    user = (payload.get("data") or {}).get("user")
    if not user:
        raise ProfileNotExistsException(f"Profile {username} does not exist.")

    return Profile(loader.context, user)


def fetch_profile(loader: instaloader.Instaloader, username: str) -> Profile:
    """Resolve a profile by username with GraphQL + web_profile_info fallback."""
    username = normalize_username(username)
    if not username or not re.fullmatch(r"[A-Za-z0-9._]+", username):
        raise ProfileNotExistsException(f"Invalid username: {username}")

    try:
        return Profile.from_username(loader.context, username)
    except ProfileNotExistsException:
        pass
    except (ConnectionException, LoginRequiredException):
        raise
    except Exception:
        # GraphQL failures are common on mobile/anonymous — try REST fallback.
        pass

    try:
        return _fetch_via_web_profile_info(loader, username)
    except ProfileNotExistsException as exc:
        logged_in = bool(getattr(loader.context, "username", None))
        if logged_in:
            raise exc
        raise ProfileNotExistsException(
            f"Could not load profile '{username}'. The account may exist but Instagram "
            f"blocked anonymous access — open Login and sign in, then retry."
        ) from exc


def fetch_profile_or_raise_private(
    loader: instaloader.Instaloader,
    username: str,
) -> Profile:
    profile = fetch_profile(loader, username)
    if profile.is_private and not profile.followed_by_viewer and not getattr(loader.context, "username", None):
        raise PrivateProfileNotFollowedException(
            f"Profile {username} is private. Log in with an account that follows it."
        )
    return profile
