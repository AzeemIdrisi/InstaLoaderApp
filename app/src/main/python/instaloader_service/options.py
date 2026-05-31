"""Map option dictionaries from Kotlin into Instaloader configuration."""

from __future__ import annotations

from typing import Any, Callable, Dict, Optional

import instaloader


DEFAULT_USER_AGENT = (
    "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/AP2A.240805.005) "
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
)


def _bool(value: Any, default: bool) -> bool:
    if value is None:
        return default
    return bool(value)


def _optional_str(value: Any) -> Optional[str]:
    if value is None:
        return None
    text = str(value).strip()
    return text or None


def _optional_int(value: Any) -> Optional[int]:
    if value is None or value == "":
        return None
    return int(value)


def build_instaloader(options: Dict[str, Any]) -> instaloader.Instaloader:
    kwargs: Dict[str, Any] = {
        "quiet": _bool(options.get("quiet"), True),
        "download_pictures": _bool(options.get("download_pictures"), True),
        "download_videos": _bool(options.get("download_videos"), True),
        "download_video_thumbnails": _bool(options.get("download_video_thumbnails"), True),
        "download_geotags": _bool(options.get("download_geotags"), False),
        "download_comments": _bool(options.get("download_comments"), False),
        "save_metadata": _bool(options.get("save_metadata"), False),
        "compress_json": _bool(options.get("compress_json"), True),
        "max_connection_attempts": int(options.get("max_connection_attempts", 3)),
        "request_timeout": float(options.get("request_timeout", 300.0)),
        "iphone_support": _bool(options.get("iphone_support"), True),
        "sanitize_paths": _bool(options.get("sanitize_paths"), False),
        "check_resume_bbd": _bool(options.get("check_resume_bbd"), True),
    }

    user_agent = _optional_str(options.get("user_agent"))
    kwargs["user_agent"] = user_agent or DEFAULT_USER_AGENT

    dirname_pattern = _optional_str(options.get("dirname_pattern"))
    if dirname_pattern:
        kwargs["dirname_pattern"] = dirname_pattern

    filename_pattern = _optional_str(options.get("filename_pattern"))
    if filename_pattern:
        kwargs["filename_pattern"] = filename_pattern

    title_pattern = _optional_str(options.get("title_pattern"))
    if title_pattern:
        kwargs["title_pattern"] = title_pattern

    slide = _optional_str(options.get("slide"))
    if slide:
        kwargs["slide"] = slide

    if _bool(options.get("save_captions"), False):
        caption_pattern = _optional_str(options.get("post_metadata_txt_pattern")) or "{caption}"
        kwargs["post_metadata_txt_pattern"] = caption_pattern
    else:
        kwargs["post_metadata_txt_pattern"] = ""

    story_pattern = _optional_str(options.get("storyitem_metadata_txt_pattern"))
    if story_pattern is not None:
        kwargs["storyitem_metadata_txt_pattern"] = story_pattern

    if _bool(options.get("enable_resume"), True):
        resume_prefix = _optional_str(options.get("resume_prefix")) or "iterator"
        kwargs["resume_prefix"] = resume_prefix
    else:
        kwargs["resume_prefix"] = None

    return instaloader.Instaloader(**kwargs)


def parse_filter(expression: Optional[str]) -> Optional[Callable]:
    if not expression or not str(expression).strip():
        return None
    expr = str(expression).strip()
    return eval(f"lambda p: ({expr})", {"__builtins__": {}}, {})


def profile_download_kwargs(options: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "profile_pic": _bool(options.get("profile_pic"), True),
        "posts": _bool(options.get("posts"), True),
        "tagged": _bool(options.get("tagged"), False),
        "igtv": _bool(options.get("igtv"), False),
        "highlights": _bool(options.get("highlights"), False),
        "stories": _bool(options.get("stories"), False),
        "reels": _bool(options.get("reels"), False),
        "fast_update": _bool(options.get("fast_update"), False),
        "max_count": _optional_int(options.get("max_count")),
        "post_filter": parse_filter(options.get("post_filter")),
        "storyitem_filter": parse_filter(options.get("storyitem_filter")),
    }
