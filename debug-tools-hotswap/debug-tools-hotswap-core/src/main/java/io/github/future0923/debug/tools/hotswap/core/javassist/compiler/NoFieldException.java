/*
 * Copyright (C) 2024-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.future0923.debug.tools.hotswap.core.javassist.compiler;

import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.ast.ASTree;

public class NoFieldException extends CompileError {
    /** default serialVersionUID */
    private static final long serialVersionUID = 1L;
    private String fieldName;
    private ASTree expr;

    /* NAME must be JVM-internal representation.
     */
    public NoFieldException(String name, ASTree e) {
        super("no such field: " + name);
        fieldName = name;
        expr = e;
    }

    /* The returned name should be JVM-internal representation.
     */
    public String getField() { return fieldName; }

    /* Returns the expression where this exception is thrown.
     */
    public ASTree getExpr() { return expr; }
}
