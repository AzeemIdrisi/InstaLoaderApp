"""Unit tests for safe filter parsing."""

from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path

_FILTER_PARSER_PATH = (
    Path(__file__).resolve().parents[2]
    / "main"
    / "python"
    / "instaloader_service"
    / "filter_parser.py"
)
_spec = importlib.util.spec_from_file_location("filter_parser", _FILTER_PARSER_PATH)
assert _spec and _spec.loader
_filter_parser = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_filter_parser)
FilterParseError = _filter_parser.FilterParseError
filterstr_to_filterfunc = _filter_parser.filterstr_to_filterfunc


class PostStub:
    is_video = False
    likes = 150
    date_utc = None
    caption_hashtags = ["cute", "kitten"]
    viewer_has_liked = True


class FilterParserTest(unittest.TestCase):
    def test_simple_attribute(self) -> None:
        fn = filterstr_to_filterfunc("not is_video", PostStub)
        self.assertTrue(fn(PostStub()))

    def test_comparison(self) -> None:
        fn = filterstr_to_filterfunc("likes > 100", PostStub)
        self.assertTrue(fn(PostStub()))

    def test_membership(self) -> None:
        fn = filterstr_to_filterfunc("'cute' in caption_hashtags", PostStub)
        self.assertTrue(fn(PostStub()))

    def test_rejects_attribute_traversal(self) -> None:
        with self.assertRaises(FilterParseError):
            filterstr_to_filterfunc("(1).__class__", PostStub)

    def test_rejects_arbitrary_call(self) -> None:
        with self.assertRaises(FilterParseError):
            filterstr_to_filterfunc("open('/etc/passwd')", PostStub)

    def test_rejects_unknown_name(self) -> None:
        with self.assertRaises(FilterParseError):
            filterstr_to_filterfunc("__import__('os')", PostStub)


if __name__ == "__main__":
    unittest.main()
