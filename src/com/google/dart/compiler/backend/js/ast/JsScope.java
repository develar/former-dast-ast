// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js.ast;

import com.google.dart.compiler.util.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * A scope is a factory for creating and allocating
 * {@link String}s. A JavaScript AST is
 * built in terms of abstract name objects without worrying about obfuscation,
 * keyword/identifier blacklisting, and so on.
 * <p/>
 * <p/>
 * <p/>
 * Scopes are associated with
 * {@link JsFunction}s, but the two are
 * not equivalent. Functions <i>have</i> scopes, but a scope does not
 * necessarily have an associated Function.
 * <p/>
 * <p/>
 * <p/>
 * Scopes can have parents to provide constraints when allocating actual
 * identifiers for names. Specifically, names in child scopes are chosen such
 * that they do not conflict with names in their parent scopes. The ultimate
 * parent is usually the global scope,
 * but parentless scopes are useful for managing names that are always accessed
 * with a qualifier and could therefore never be confused with the global scope
 * hierarchy.
 */
public class JsScope {
    private Map<String, String> names = Collections.emptyMap();
    private final JsScope parent;
    protected int tempIndex = 0;

    public JsScope(@Nullable JsScope parent) {
        this.parent = parent;
    }

    protected JsScope() {
        parent = null;
    }

    @NotNull
    public JsScope innerScope() {
        return new JsScope(this);
    }

    /**
     * Gets a name object associated with the specified identifier in this scope,
     * creating it if necessary.<br/>
     * If the JsName does not exist yet, a new JsName is created. The identifier,
     * short name, and original name of the newly created JsName are equal to
     * the given identifier.
     *
     * @param identifier An identifier that is unique within this scope.
     */
    public String declareName(String identifier) {
        String name = names.get(identifier);
        return name != null ? name : doCreateName(identifier);
    }

    /**
     * Creates a new variable with an unique ident in this scope.
     * The generated JsName is guaranteed to have an identifier that does not clash with any existing variables in the scope.
     * Future declarations of variables might however clash with the temporary
     * (unless they use this function).
     */
    @NotNull
    public String declareFreshName(String suggestedName) {
        String name = suggestedName;
        int counter = 0;
        while (names.containsKey(name)) {
            name = suggestedName + '_' + counter++;
        }
        return doCreateName(name);
    }

    /**
     * Creates a temporary variable with an unique name in this scope.
     * The generated temporary is guaranteed to have an identifier (but not short
     * name) that does not clash with any existing variables in the scope.
     * Future declarations of variables might however clash with the temporary.
     */
    public String declareTemporary() {
        return declareFreshName("tmp$" + tempIndex++);
    }

    /**
     * Attempts to find the name object for the specified ident, searching in this
     * scope, and if not found, in the parent scopes.
     *
     * @return <code>null</code> if the identifier has no associated name
     */
    @Nullable
    public final String findName(String ident) {
        String name = names.get(ident);
        if (name == null && parent != null) {
            return parent.findName(ident);
        }
        return name;
    }

    protected String doCreateName(String name) {
        names = Maps.put(names, name, name);
        return name;
    }
}