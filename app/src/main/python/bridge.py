"""Bridge module invoked from Kotlin via Chaquopy."""

from __future__ import annotations

from typing import Any, Callable, Dict, Optional

from instaloader_service import (
    cancel_download,
    download_with_options as _download_with_options,
    get_post_count,
    login,
    test_session,
    two_factor_login,
)


def _to_python_dict(value: Any) -> Dict[str, Any]:
    """Convert a Chaquopy Java Map or Python dict into a plain Python dict."""
    if value is None:
        return {}
    if isinstance(value, dict):
        return dict(value)

    # Native Python dict built via builtins.dict() from Kotlin — use as-is after copy.
    if hasattr(value, "items"):
        try:
            return {str(key): val for key, val in value.items()}
        except TypeError:
            pass

    # Java Map: iterate entrySet (keySet iteration breaks on Chaquopy HashMap).
    if hasattr(value, "entrySet"):
        result: Dict[str, Any] = {}
        iterator = value.entrySet().iterator()
        while iterator.hasNext():
            entry = iterator.next()
            result[str(entry.getKey())] = entry.getValue()
        return result

    return {}


def _stringify(value: Any) -> str:
    if value is None:
        return ""
    return str(value)


def download_with_options(
    target_type: str,
    target_value: str,
    options: Any,
    progress_callback: Optional[Callable] = None,
) -> None:
    """Kotlin entry point — options must be a native Python dict, not a Java HashMap."""
    py_options = _to_python_dict(options)

    def wrapped_callback(payload: Dict[str, Any]) -> None:
        if progress_callback is None:
            return
        # Pass strings only — Chaquopy throws ClassCastException on Int/Long mismatches.
        progress_callback(
            str(payload.get("phase") or ""),
            _stringify(payload.get("total")),
            _stringify(payload.get("completed")),
            _stringify(payload.get("failed")),
            _stringify(payload.get("skipped")),
            _stringify(payload.get("remaining")),
            str(payload.get("current") or ""),
            str(payload.get("message") or ""),
        )

    _download_with_options(
        target_type,
        target_value,
        py_options,
        wrapped_callback if progress_callback is not None else None,
    )


__all__ = [
    "cancel_download",
    "download_with_options",
    "get_post_count",
    "login",
    "test_session",
    "two_factor_login",
]
