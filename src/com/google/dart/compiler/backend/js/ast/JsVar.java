package com.google.dart.compiler.backend.js.ast;

import org.jetbrains.annotations.Nullable;

public class JsVar extends JsStatement implements HasName {
    private final String name;
    private JsExpression initExpression;

    public JsVar(String name) {
        this.name = name;
    }

    public JsVar(String name, @Nullable JsExpression initExpression) {
        this.name = name;
        this.initExpression = initExpression;
    }

    public JsExpression getInitExpression() {
        return initExpression;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setInitExpression(JsExpression initExpression) {
        this.initExpression = initExpression;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visit(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        if (initExpression != null) {
            visitor.accept(initExpression);
        }
    }
}
