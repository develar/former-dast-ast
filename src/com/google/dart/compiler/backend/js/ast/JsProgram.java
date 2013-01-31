// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import gnu.trove.TDoubleObjectHashMap;
import gnu.trove.THashMap;
import gnu.trove.TIntObjectHashMap;

import java.util.Map;

import static com.google.dart.compiler.backend.js.ast.JsNumberLiteral.JsDoubleLiteral;
import static com.google.dart.compiler.backend.js.ast.JsNumberLiteral.JsIntLiteral;

public final class JsProgram extends JsGlobalBlock {
    private final TDoubleObjectHashMap<JsDoubleLiteral> doubleLiteralMap = new TDoubleObjectHashMap<JsDoubleLiteral>();
    private final TIntObjectHashMap<JsIntLiteral> intLiteralMap = new TIntObjectHashMap<JsIntLiteral>();

    private final JsRootScope rootScope;
    private final Map<String, JsStringLiteral> stringLiteralMap = new THashMap<String, JsStringLiteral>();
    private final JsScope topScope;

    public JsProgram(String unitId) {
        rootScope = new JsRootScope(this);
        topScope = new JsScope(rootScope, "Global", unitId);
    }

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

    /**
     * Gets the quasi-mythical root scope. This is not the same as the top scope;
     * all unresolvable identifiers wind up here, because they are considered
     * external to the program.
     */
    public JsRootScope getRootScope() {
        return rootScope;
    }

    /**
     * Gets the top level scope. This is the scope of all the statements in the
     * main program.
     */
    public JsScope getScope() {
        return topScope;
    }

    /**
     * Creates or retrieves a JsStringLiteral from an interned object pool.
     */
    public JsStringLiteral getStringLiteral(String value) {
        JsStringLiteral literal = stringLiteralMap.get(value);
        if (literal == null) {
            literal = new JsStringLiteral(value);
            stringLiteralMap.put(value, literal);
        }
        return literal;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitProgram(this);
    }
}
