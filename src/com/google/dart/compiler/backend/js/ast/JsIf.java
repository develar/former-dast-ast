// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

public final class JsIf extends JsStatement {
    private JsExpression _if;
    private JsNode then;
    private JsNode _else;

    public JsIf() {
    }

    public JsIf(JsExpression ifExpression, JsStatement then, JsStatement elseStatement) {
        _if = ifExpression;
        this.then = then;
        _else = elseStatement;
    }

    public JsIf(JsExpression ifExpression, JsStatement then) {
        _if = ifExpression;
        this.then = then;
    }

    public JsNode getElse() {
        return _else;
    }

    public JsExpression getIf() {
        return _if;
    }

    public JsNode getThen() {
        return then;
    }

    public void setElse(JsNode elseStatement) {
        _else = elseStatement;
    }

    public void setIf(JsExpression ifExpression) {
        _if = ifExpression;
    }

    public void setThen(JsNode then) {
        this.then = then;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitIf(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        visitor.accept(_if);
        visitor.accept(then);
        if (_else != null) {
            visitor.accept(_else);
        }
    }
}
