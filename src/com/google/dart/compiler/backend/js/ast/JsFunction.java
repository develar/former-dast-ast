// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class JsFunction extends JsLiteral implements HasName {
    private JsBlock body;
    private List<JsParameter> parameters = Collections.emptyList();
    private final JsScope scope;
    private String name;

    public JsFunction(JsScope parentScope) {
        this(parentScope, (String) null);
    }

    public JsFunction(@Nullable JsScope parentScope, JsBlock body) {
        this(parentScope, (String) null);
        this.body = body;
    }

    private JsFunction(@Nullable JsScope parentScope, @Nullable String name) {
        this.name = name;
        scope = new JsScope(parentScope);
    }

    public JsBlock getBody() {
        return body;
    }

    @Override
    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public List<JsParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<JsParameter> parameters) {
        this.parameters = parameters;
    }

    public JsScope getScope() {
        return scope;
    }

    public void setBody(JsBlock body) {
        this.body = body;
    }

    public void add(JsNode statement) {
        body.getStatements().add(statement);
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitFunction(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        if (parameters != null) {
            visitor.acceptList(parameters);
        }
        visitor.accept(body);
    }
}