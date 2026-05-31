"""Download orchestration with live progress callbacks."""

from __future__ import annotations

import os
import re
from typing import Any, Callable, Dict, Optional

import instaloader
from instaloader import Hashtag, Post

from instaloader_service.options import build_instaloader, profile_download_kwargs
from instaloader_service.profile_fetch import fetch_profile, fetch_profile_or_raise_private, normalize_username
from instaloader_service.progress import merge_progress
from instaloader_service.session import apply_session

_CANCELLED = False


def cancel_download() -> None:
    global _CANCELLED
    _CANCELLED = True


def is_cancelled() -> bool:
    return _CANCELLED


def _reset_cancel() -> None:
    global _CANCELLED
    _CANCELLED = False


def _check_cancelled(callback: Optional[Callable]) -> None:
    if _CANCELLED:
        merge_progress(callback, phase="cancelled", message="Download cancelled")
        raise InterruptedError("Download cancelled")


def get_post_count(username: str, session_username: Optional[str], session_dir: Optional[str]) -> int:
    from instaloader_service.options import DEFAULT_USER_AGENT

    loader = instaloader.Instaloader(quiet=True, user_agent=DEFAULT_USER_AGENT)
    apply_session(loader, session_username, session_dir)
    profile = fetch_profile(loader, username)
    return profile.mediacount


def download_with_options(
    target_type: str,
    target_value: str,
    options: Dict[str, Any],
    progress_callback: Optional[Callable] = None,
) -> None:
    _reset_cancel()
    loader = build_instaloader(options)
    apply_session(loader, options.get("session_username"), options.get("session_dir"))

    base_dir = options.get("base_dir") or os.path.join(
        os.environ.get("EXTERNAL_STORAGE", "/storage/emulated/0"),
        "Download",
        "InstaLoaderApp",
    )
    os.makedirs(base_dir, exist_ok=True)

    target_type = (target_type or "profile").strip().lower()
    target_value = normalize_username(target_value) if target_type == "profile" else target_value.strip()
    if not target_value:
        raise ValueError("Target value is required")

    if target_type == "profile":
        _download_profile(loader, target_value, options, base_dir, progress_callback)
    elif target_type in {"post", "reel", "reels"}:
        shortcode = _normalize_shortcode(target_value)
        _download_single_post(loader, shortcode, base_dir, progress_callback)
    elif target_type == "hashtag":
        _download_hashtag(loader, target_value, options, base_dir, progress_callback)
    else:
        raise ValueError(f"Unsupported target type: {target_type}")

    merge_progress(progress_callback, phase="finished", message="Download finished")


def _download_single_post(
    loader: instaloader.Instaloader,
    shortcode: str,
    base_dir: str,
    callback: Optional[Callable],
) -> None:
    merge_progress(callback, phase="starting", total=1, message="Downloading post")
    _check_cancelled(callback)
    loader.dirname_pattern = os.path.join(base_dir, "posts")
    os.makedirs(loader.dirname_pattern, exist_ok=True)
    post = Post.from_shortcode(loader.context, shortcode)
    try:
        loader.download_post(post, target="")
        merge_progress(
            callback,
            phase="downloading",
            total=1,
            completed=1,
            current=shortcode,
            message="Post downloaded",
        )
    except Exception as exc:
        merge_progress(
            callback,
            phase="downloading",
            total=1,
            failed=1,
            current=shortcode,
            message=str(exc),
        )
        raise


def _download_hashtag(
    loader: instaloader.Instaloader,
    hashtag: str,
    options: Dict[str, Any],
    base_dir: str,
    callback: Optional[Callable],
) -> None:
    tag = hashtag.lstrip("#")
    loader.dirname_pattern = os.path.join(base_dir, f"hashtag_{tag}")
    os.makedirs(loader.dirname_pattern, exist_ok=True)
    posts = Hashtag.from_name(loader.context, tag).get_posts_resumable()
    max_count = options.get("max_count")
    total = int(max_count) if max_count else None
    completed = failed = skipped = 0

    merge_progress(callback, phase="starting", total=total, message=f"Downloading #{tag}")
    for index, post in enumerate(posts):
        _check_cancelled(callback)
        if total is not None and completed + failed + skipped >= total:
            break
        try:
            downloaded = loader.download_post(post, target=f"#{tag}")
            if downloaded:
                completed += 1
            else:
                skipped += 1
        except Exception:
            failed += 1
        merge_progress(
            callback,
            phase="downloading",
            total=total,
            completed=completed,
            failed=failed,
            skipped=skipped,
            current=post.shortcode,
            message=f"Post {index + 1}",
        )


def _download_profile_posts(
    loader: instaloader.Instaloader,
    profile,
    username: str,
    kwargs: Dict[str, Any],
    callback: Optional[Callable],
) -> None:
    posts = profile.get_posts()
    max_count = kwargs.get("max_count")
    total = min(profile.mediacount, max_count) if max_count else profile.mediacount
    completed = failed = skipped = 0
    post_filter = kwargs.get("post_filter")

    merge_progress(callback, phase="starting", total=total, message=f"Found {total} posts")
    for index, post in enumerate(posts):
        _check_cancelled(callback)
        if max_count is not None and completed + failed + skipped >= max_count:
            break
        if post_filter is not None and not post_filter(post):
            skipped += 1
            continue
        try:
            downloaded = loader.download_post(post, target=username)
            if downloaded:
                completed += 1
            else:
                skipped += 1
                if kwargs.get("fast_update"):
                    break
        except Exception:
            failed += 1
        merge_progress(
            callback,
            phase="downloading",
            total=total,
            completed=completed,
            failed=failed,
            skipped=skipped,
            current=post.shortcode,
            message=f"Post {index + 1} of {total}",
        )


def _download_profile(
    loader: instaloader.Instaloader,
    username: str,
    options: Dict[str, Any],
    base_dir: str,
    callback: Optional[Callable],
) -> None:
    profile = fetch_profile_or_raise_private(loader, username)
    kwargs = profile_download_kwargs(options)
    loader.dirname_pattern = options.get("dirname_pattern") or os.path.join(base_dir, username)
    os.makedirs(loader.dirname_pattern, exist_ok=True)

    if kwargs["profile_pic"]:
        _check_cancelled(callback)
        merge_progress(callback, phase="profile_pic", message="Downloading profile picture")
        try:
            loader.download_profilepic(profile)
        except Exception as exc:
            merge_progress(callback, phase="profile_pic", failed=1, message=str(exc))

    if kwargs["posts"]:
        _download_profile_posts(loader, profile, username, kwargs, callback)

    post_filter = kwargs.get("post_filter")
    storyitem_filter = kwargs.get("storyitem_filter")
    fast_update = kwargs.get("fast_update", False)

    extra_phases = [
        (
            "stories",
            kwargs["stories"],
            lambda: loader.download_stories(
                userids=[profile.userid],
                fast_update=fast_update,
                storyitem_filter=storyitem_filter,
            ),
        ),
        (
            "highlights",
            kwargs["highlights"],
            lambda: loader.download_highlights(
                profile.userid,
                fast_update=fast_update,
                storyitem_filter=storyitem_filter,
            ),
        ),
        (
            "reels",
            kwargs["reels"],
            lambda: loader.download_reels(profile, fast_update=fast_update, post_filter=post_filter),
        ),
        (
            "igtv",
            kwargs["igtv"],
            lambda: loader.download_igtv(profile, fast_update=fast_update, post_filter=post_filter),
        ),
        (
            "tagged",
            kwargs["tagged"],
            lambda: loader.download_tagged(profile, fast_update=fast_update, post_filter=post_filter),
        ),
    ]
    for phase, enabled, action in extra_phases:
        if not enabled:
            continue
        _check_cancelled(callback)
        merge_progress(callback, phase=phase, message=f"Downloading {phase}")
        try:
            action()
        except Exception as exc:
            merge_progress(callback, phase=phase, failed=1, message=str(exc))


def _normalize_shortcode(value: str) -> str:
    text = value.strip()
    if "instagram.com" in text:
        text = extract_shortcode(text)
    return text.split("?")[0].split("/")[0].strip()


def extract_shortcode(url_or_code: str) -> str:
    value = url_or_code.strip()
    patterns = [
        r"instagram\.com/p/([^/?#]+)",
        r"instagram\.com/reel/([^/?#]+)",
        r"instagram\.com/reels/([^/?#]+)",
        r"instagram\.com/tv/([^/?#]+)",
    ]
    for pattern in patterns:
        match = re.search(pattern, value)
        if match:
            return match.group(1)
    return value
