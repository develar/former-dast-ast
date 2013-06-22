// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.util;

public interface TextOutput {
    int getPosition();

    int getLine();

    int getColumn();

    void indentIn();

    void indentOut();

    void newline();

    void print(char c);

    void printNumber(Number v);

    void print(char[] s);

    void print(CharSequence s);

    void printOpt(char c);

    void printOpt(char[] s);

    void printOpt(String s);

    boolean isCompact();

    boolean isJustNewlined();

    void setOutListener(OutListener outListener);

    void maybeIndent();

    StringBuilder getBuilder();
    void builderProduced(int increment);

    public interface OutListener {
        void newLined();

        void indentedAfterNewLine();
    }
}
