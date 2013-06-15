// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import com.intellij.util.SmartList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JsVars extends JsStatement {
    private final List<JsVar> vars;

    private final boolean multiline;

    public JsVars() {
        this(new SmartList<JsVar>(), false);
    }

    public JsVars(boolean multiline) {
        this(new SmartList<JsVar>(), multiline);
    }

    public JsVars(List<JsVar> vars, boolean multiline) {
        this.vars = vars;
        this.multiline = multiline;
    }

    public JsVars(JsVar var) {
        this(Collections.singletonList(var), false);
    }

    public JsVars(JsVar... vars) {
        this(Arrays.asList(vars), false);
    }

    public List<JsVar> getVars() {
        return vars;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void add(JsVar var) {
        vars.add(var);
    }

    public void addIfHasInitializer(JsVar var) {
        if (var.getInitExpression() != null) {
            add(var);
        }
    }

    public boolean isEmpty() {
        return vars.isEmpty();
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitVars(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        visitor.acceptList(vars);
    }
}