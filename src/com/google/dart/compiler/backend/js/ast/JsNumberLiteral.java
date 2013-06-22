package com.google.dart.compiler.backend.js.ast;

public class JsNumberLiteral extends JsLiteral {
    public static final JsNumberLiteral V_0 = new JsNumberLiteral(0);
    public static final JsNumberLiteral V_M1 = new JsNumberLiteral(-1);

    private final Number value;

    public JsNumberLiteral(Number value) {
        this.value = value;
    }

    public Number getValue() {
        return value;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitNumber(this);
    }

    public String toString() {
        return String.valueOf(value);
    }
}