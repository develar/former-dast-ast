package com.google.dart.compiler.backend.js.ast;

public interface JsExpression extends JsNode {
    @Override
    JsExpression source(Object info);
}
