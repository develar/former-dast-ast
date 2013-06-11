// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import gnu.trove.TDoubleObjectHashMap;
import gnu.trove.TIntObjectHashMap;

import static com.google.dart.compiler.backend.js.ast.JsNumberLiteral.JsDoubleLiteral;
import static com.google.dart.compiler.backend.js.ast.JsNumberLiteral.JsIntLiteral;

public final class JsProgram extends JsGlobalBlock {
    private final TDoubleObjectHashMap<JsDoubleLiteral> doubleLiteralMap = new TDoubleObjectHashMap<JsDoubleLiteral>();
    private final TIntObjectHashMap<JsIntLiteral> intLiteralMap = new TIntObjectHashMap<JsIntLiteral>();

    public JsNumberLiteral getNumberLiteral(double value) {
        JsDoubleLiteral literal = doubleLiteralMap.get(value);
        if (literal == null) {
            literal = new JsDoubleLiteral(value);
            doubleLiteralMap.put(value, literal);
        }

        return literal;
    }

    public JsNumberLiteral getNumberLiteral(int value) {
        JsIntLiteral literal = intLiteralMap.get(value);
        if (literal == null) {
            literal = new JsIntLiteral(value);
            intLiteralMap.put(value, literal);
        }

        return literal;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitProgram(this);
    }
}