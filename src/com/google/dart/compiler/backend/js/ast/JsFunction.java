// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import com.intellij.util.SmartList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class JsFunction extends JsLiteral implements HasName {
    private JsBlock body;
    private List<JsParameter> parameters;
    private final JsScope scope;
    private JsName name;

    public JsFunction(JsScope parentScope) {
        this(parentScope, (JsName) null);
    }

    public JsFunction(JsScope parentScope, JsBlock body) {
        this(parentScope, (JsName) null);
        this.body = body;
    }

    private JsFunction(JsScope parentScope, @Nullable JsName name) {
        this.name = name;
        scope = new JsScope(parentScope, name == null ? null : name.getIdent());
    }

    public JsBlock getBody() {
        return body;
    }

    @Override
    public JsName getName() {
        return name;
    }

    public List<JsParameter> getParameters() {
        if (parameters == null) {
            parameters = new SmartList<JsParameter>();
        }
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

    public void setName(@Nullable JsName name) {
        this.name = name;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitFunction(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        visitor.acceptWithInsertRemove(parameters);
        visitor.accept(body);
    }
}
