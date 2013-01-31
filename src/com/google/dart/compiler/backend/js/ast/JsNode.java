// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

public interface JsNode {
    /**
     * Causes this object to have the visitor visit itself and its children.
     *
     * @param visitor the visitor that should traverse this node
     */
    void accept(JsVisitor visitor);

    void acceptChildren(JsVisitor visitor);

    /**
     * Return the source info associated with this object.
     */
    Object getSource();

    /**
     * Set the source info associated with this object.
     *
     * @param info
     */
    void setSource(Object info);

    JsNode source(Object info);

    JsStatement asStatement();
}