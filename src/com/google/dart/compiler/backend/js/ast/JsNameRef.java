// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import org.jetbrains.annotations.Nullable;

public final class JsNameRef extends JsExpressionImpl implements HasName {
    private String name;
    private JsExpression qualifier;

    public JsNameRef(String name) {
        this.name = name;
    }

    public JsNameRef(String name, JsExpression qualifier) {
        this.name = name;
        this.qualifier = qualifier;
    }

    public JsNameRef(String name, String qualifier) {
        this(name, new JsNameRef(qualifier));
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public JsExpression getQualifier() {
        return qualifier;
    }

    public boolean isLeaf() {
        return qualifier == null;
    }

    public void setQualifier(JsExpression qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitNameRef(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        if (qualifier != null) {
           visitor.accept(qualifier);
        }
    }
}
