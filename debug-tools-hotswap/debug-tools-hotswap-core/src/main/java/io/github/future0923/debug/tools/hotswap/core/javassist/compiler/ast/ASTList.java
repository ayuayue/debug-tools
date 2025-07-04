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
package io.github.future0923.debug.tools.hotswap.core.javassist.compiler.ast;

import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.CompileError;

/**
 * A linked list.
 * The right subtree must be an ASTList object or null.
 */
public class ASTList extends ASTree {
    /** default serialVersionUID */
    private static final long serialVersionUID = 1L;
    private ASTree left;
    private ASTList right;

    public ASTList(ASTree _head, ASTList _tail) {
        left = _head;
        right = _tail;
    }

    public ASTList(ASTree _head) {
        left = _head;
        right = null;
    }

    public static ASTList make(ASTree e1, ASTree e2, ASTree e3) {
        return new ASTList(e1, new ASTList(e2, new ASTList(e3)));
    }

    @Override
    public ASTree getLeft() { return left; }

    @Override
    public ASTree getRight() { return right; }

    @Override
    public void setLeft(ASTree _left) { left = _left; }

    @Override
    public void setRight(ASTree _right) {
        right = (ASTList)_right;
    }

    /**
     * Returns the car part of the list.
     */
    public ASTree head() { return left; }

    public void setHead(ASTree _head) {
        left = _head;
    }

    /**
     * Returns the cdr part of the list.
     */
    public ASTList tail() { return right; }

    public void setTail(ASTList _tail) {
        right = _tail;
    }

    @Override
    public void accept(Visitor v) throws CompileError { v.atASTList(this); }

    @Override
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("(<");
        sbuf.append(getTag());
        sbuf.append('>');
        ASTList list = this;
        while (list != null) {
            sbuf.append(' ');
            ASTree a = list.left;
            sbuf.append(a == null ? "<null>" : a.toString());
            list = list.right;
        }

        sbuf.append(')');
        return sbuf.toString();
    }

    /**
     * Returns the number of the elements in this list.
     */
    public int length() {
        return length(this);
    }

    public static int length(ASTList list) {
        if (list == null)
            return 0;

        int n = 0;
        while (list != null) {
            list = list.right;
            ++n;
        }

        return n;
    }

    /**
     * Returns a sub list of the list.  The sub list begins with the
     * n-th element of the list.
     *
     * @param nth       zero or more than zero.
     */
    public ASTList sublist(int nth) {
        ASTList list = this;
        while (nth-- > 0)
            list = list.right;

        return list;
    }

    /**
     * Substitutes <code>newObj</code> for <code>oldObj</code> in the
     * list.
     */
    public boolean subst(ASTree newObj, ASTree oldObj) {
        for (ASTList list = this; list != null; list = list.right)
            if (list.left == oldObj) {
                list.left = newObj;
                return true;
            }

        return false;
    }

    /**
     * Appends an object to a list.
     */
    public static ASTList append(ASTList a, ASTree b) {
        return concat(a, new ASTList(b));
    }

    /**
     * Concatenates two lists.
     */
    public static ASTList concat(ASTList a, ASTList b) {
        if (a == null)
            return b;
        ASTList list = a;
        while (list.right != null)
            list = list.right;

        list.right = b;
        return a;
    }
}
