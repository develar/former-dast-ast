// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

public class JsStringLiteral extends JsLiteral {
    private final CharSequence value;
    private final char quote;

    public JsStringLiteral(CharSequence value) {
        this.value = value;
        quote = '\'';
    }

    public JsStringLiteral(CharSequence value, char quote) {
        this.value = value;
        this.quote = quote;
    }

    public CharSequence getValue() {
        return value;
    }

    public char getQuote() {
        return quote;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitString(this);
    }
}
