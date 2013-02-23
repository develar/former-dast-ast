// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

public final class JsStringLiteral extends JsLiteral {
  private final String value;

    public JsStringLiteral(String value) {
      this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public void accept(JsVisitor v) {
    v.visitString(this);
  }
}
