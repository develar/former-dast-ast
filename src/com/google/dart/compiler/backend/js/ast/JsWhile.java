// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

public class JsWhile extends JsStatement {
    protected JsNode body;
    protected JsExpression condition;

    public JsWhile() {
    }

    public JsWhile(JsExpression condition, JsStatement body) {
        this.condition = condition;
        this.body = body;
    }

    public JsNode getBody() {
        return body;
    }

    public JsExpression getCondition() {
        return condition;
    }

    public void setBody(JsNode body) {
        this.body = body;
    }

    public void setCondition(JsExpression condition) {
        this.condition = condition;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitWhile(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        visitor.accept(condition);
        visitor.accept(body);
    }
}