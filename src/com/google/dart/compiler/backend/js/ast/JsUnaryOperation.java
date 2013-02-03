// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

public abstract class JsUnaryOperation extends JsExpressionImpl {

    private JsExpression arg;

    private final JsUnaryOperator op;

    public JsUnaryOperation(JsUnaryOperator op) {
        this(op, null);
    }

    public JsUnaryOperation(JsUnaryOperator op, JsExpression arg) {
        super();
        this.op = op;
        this.arg = arg;
    }

    public JsExpression getArg() {
        return arg;
    }

    public JsUnaryOperator getOperator() {
        return op;
    }

    public void setArg(JsExpression arg) {
        this.arg = arg;
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        if (op.isModifying()) {
            // The delete operator is practically like an assignment of undefined, so
            // for practical purposes we're treating it as an lvalue.
            visitor.accept(arg);
        }
        else {
            visitor.accept(arg);
        }
    }
}
