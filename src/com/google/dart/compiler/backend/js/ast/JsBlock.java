// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JsBlock extends JsStatement {
    private final List<JsNode> nodes;

    public JsBlock() {
        this(new ArrayList<JsNode>());
    }

    public JsBlock(JsNode statement) {
        this(Collections.singletonList(statement));
    }

    public JsBlock(JsNode... nodes) {
        this(Arrays.asList(nodes));
    }

    public JsBlock(List<JsNode> nodes) {
        this.nodes = nodes;
    }

    public List<JsNode> getStatements() {
        return nodes;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public boolean isGlobalBlock() {
        return false;
    }

    @Override
    public void accept(JsVisitor v) {
        v.visitBlock(this);
    }

    @Override
    public void acceptChildren(JsVisitor visitor) {
        visitor.acceptList(nodes);
    }
}