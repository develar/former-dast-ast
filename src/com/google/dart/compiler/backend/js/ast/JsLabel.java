// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

public class JsLabel extends JsStatement implements HasName {
    private final String label;

    private JsNode statement;

    public JsLabel(String label) {
        this.label = label;
    }

    public JsLabel(String label, JsNode statement) {
        this.label = label;
        this.statement = statement;
    }

    @Override
    public String getName() {
        return label;
    }

    public JsNode getStatement() {
        return statement;
    }

    public void setStatement(JsNode statement) {
        this.statement = statement;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitLabel(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        visitor.accept(statement);
    }
}
