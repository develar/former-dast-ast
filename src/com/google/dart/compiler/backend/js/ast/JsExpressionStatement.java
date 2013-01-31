// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

public final class JsExpressionStatement extends JsStatement {
    private JsExpression expression;

    public JsExpressionStatement(JsExpression expression) {
        this.expression = expression;
    }

    public JsExpression getExpression() {
        return expression;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitExpressionStatement(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        visitor.accept(expression);
    }

    @Override
    public Object getSource() {
        return null;
    }

    @Override
    public void setSource(Object info) {
        throw new IllegalStateException("You must not set source info for JsExpressionStatement, set for expression");
    }

    @Override
    public JsNode source(Object info) {
        throw new IllegalStateException("You must not set source info for JsExpressionStatement, set for expression");
    }
}
