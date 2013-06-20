// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import com.intellij.util.SmartList;

import java.util.List;

/**
 * A member/case in a JavaScript switch object.
 */
public abstract class JsSwitchMember extends SourceInfoAwareJsNode {
    protected final List<JsNode> statements = new SmartList<JsNode>();

    protected JsSwitchMember() {
        super();
    }

    public List<JsNode> getStatements() {
        return statements;
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        visitor.acceptList(statements);
    }
}