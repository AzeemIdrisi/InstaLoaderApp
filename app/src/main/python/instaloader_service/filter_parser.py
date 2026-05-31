"""Safe parsing of instaloader-style post/story filter expressions."""

from __future__ import annotations

import ast
import datetime
from typing import Any, Callable, Optional, Set, Type, Union

_ALLOWED_COMPARE_OPS = (
    ast.Eq,
    ast.NotEq,
    ast.Lt,
    ast.LtE,
    ast.Gt,
    ast.GtE,
    ast.In,
    ast.NotIn,
)
_ALLOWED_BOOL_OPS = (ast.And, ast.Or)
_ALLOWED_UNARY_OPS = (ast.Not,)


class FilterParseError(ValueError):
    """Raised when a filter expression uses unsupported or unsafe syntax."""


def _validate_node(node: ast.AST, allowed_names: Set[str]) -> None:
    if isinstance(node, ast.Expression):
        _validate_node(node.body, allowed_names)
        return

    if isinstance(node, ast.BoolOp):
        if type(node.op) not in _ALLOWED_BOOL_OPS:
            raise FilterParseError(f"Unsupported operator: {type(node.op).__name__}")
        for value in node.values:
            _validate_node(value, allowed_names)
        return

    if isinstance(node, ast.UnaryOp):
        if type(node.op) not in _ALLOWED_UNARY_OPS:
            raise FilterParseError(f"Unsupported unary operator: {type(node.op).__name__}")
        _validate_node(node.operand, allowed_names)
        return

    if isinstance(node, ast.Compare):
        _validate_node(node.left, allowed_names)
        for op in node.ops:
            if type(op) not in _ALLOWED_COMPARE_OPS:
                raise FilterParseError(f"Unsupported comparison: {type(op).__name__}")
        for comparator in node.comparators:
            _validate_node(comparator, allowed_names)
        return

    if isinstance(node, ast.Call):
        if not isinstance(node.func, ast.Name) or node.func.id != "datetime":
            raise FilterParseError("Only datetime(...) calls are allowed.")
        if node.keywords:
            raise FilterParseError("datetime() keyword arguments are not allowed.")
        for arg in node.args:
            _validate_node(arg, allowed_names)
        return

    if isinstance(node, ast.Name):
        if not isinstance(node.ctx, ast.Load):
            raise FilterParseError(f"Modifying variables ({node.id}) is not allowed.")
        if node.id != "datetime" and node.id not in allowed_names:
            raise FilterParseError(f"Unknown attribute: {node.id}")
        return

    if isinstance(node, ast.Constant):
        if isinstance(node.value, (bool, int, float, str)) or node.value is None:
            return
        raise FilterParseError(f"Unsupported constant: {type(node.value).__name__}")

    if isinstance(node, (ast.List, ast.Tuple)):
        for elt in node.elts:
            _validate_node(elt, allowed_names)
        return

    raise FilterParseError(f"Unsupported syntax: {type(node).__name__}")


class _TransformFilterAst(ast.NodeTransformer):
    def visit_Name(self, node: ast.Name) -> ast.AST:
        if node.id == "datetime":
            return node
        if not isinstance(node.ctx, ast.Load):
            raise FilterParseError(f"Modifying variables ({node.id}) is not allowed.")
        return ast.copy_location(
            ast.Attribute(
                ast.Name(id="item", ctx=ast.Load()),
                attr=node.id,
                ctx=ast.Load(),
            ),
            node,
        )


def _allowed_names_for(item_type: Type[Any]) -> Set[str]:
    names: Set[str] = set()
    for name in dir(item_type):
        if name.startswith("_"):
            continue
        if hasattr(item_type, name):
            names.add(name)
    return names


def filterstr_to_filterfunc(filter_str: str, item_type: Type[Any]) -> Callable[[Any], bool]:
    """Parse an instaloader CLI-style filter into a callable(item) -> bool."""
    allowed_names = _allowed_names_for(item_type)
    tree = ast.parse(filter_str, mode="eval")
    _validate_node(tree, allowed_names)
    transformed = _TransformFilterAst().visit(tree)
    ast.fix_missing_locations(transformed)
    compiled = compile(transformed, "<filter>", mode="eval")

    def filterfunc(item: Any) -> bool:
        # Evaluated code only references `item` attributes and datetime(...).
        return bool(
            eval(
                compiled,
                {"__builtins__": {}},
                {"item": item, "datetime": datetime.datetime},
            )
        )

    return filterfunc


def parse_filter(
    expression: Optional[Union[str, Any]],
    item_type: Type[Any],
) -> Optional[Callable[[Any], bool]]:
    if not expression or not str(expression).strip():
        return None
    return filterstr_to_filterfunc(str(expression).strip(), item_type)
