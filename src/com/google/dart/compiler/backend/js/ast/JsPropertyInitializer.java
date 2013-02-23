// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used in object literals to specify property values by name.
 */
public class JsPropertyInitializer extends SourceInfoAwareJsNode {
    private final JsExpression labelExpr;
    private final String label;
    private JsExpression valueExpr;

    public JsPropertyInitializer(@NotNull JsExpression labelExpr) {
        this.labelExpr = labelExpr;
        label = null;
    }

    @Nullable
    public String getLabel() {
        return label;
    }

    public JsPropertyInitializer(@NotNull JsExpression labelExpr, @NotNull JsExpression value) {
        this(labelExpr);
        valueExpr = value;
    }

    public JsPropertyInitializer(@NotNull String label, @NotNull JsExpression value) {
        labelExpr = null;
        this.label = label;
        valueExpr = value;
    }

    public JsExpression getLabelExpr() {
        return labelExpr;
    }

    public JsExpression getValueExpr() {
        return valueExpr;
    }

    public void setValueExpr(@NotNull JsExpression valueExpr) {
        this.valueExpr = valueExpr;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitPropertyInitializer(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        if (labelExpr != null) {
            visitor.accept(labelExpr);
        }
        visitor.accept(valueExpr);
    }
}
