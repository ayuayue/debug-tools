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
package io.github.future0923.debug.tools.hotswap.core.javassist.expr;

import io.github.future0923.debug.tools.hotswap.core.javassist.CannotCompileException;
import io.github.future0923.debug.tools.hotswap.core.javassist.ClassPool;
import io.github.future0923.debug.tools.hotswap.core.javassist.CtBehavior;
import io.github.future0923.debug.tools.hotswap.core.javassist.CtClass;
import io.github.future0923.debug.tools.hotswap.core.javassist.CtConstructor;
import io.github.future0923.debug.tools.hotswap.core.javassist.NotFoundException;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.BadBytecode;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.Bytecode;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.CodeAttribute;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.CodeIterator;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.ConstPool;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.Descriptor;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.MethodInfo;
import io.github.future0923.debug.tools.hotswap.core.javassist.bytecode.Opcode;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.CompileError;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.Javac;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.JvstCodeGen;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.JvstTypeChecker;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.ProceedHandler;
import io.github.future0923.debug.tools.hotswap.core.javassist.compiler.ast.ASTList;

/**
 * Object creation (<code>new</code> expression).
 */
public class NewExpr extends Expr {
    String newTypeName;
    int newPos;

    /**
     * Undocumented constructor.  Do not use; internal-use only.
     */
    protected NewExpr(int pos, CodeIterator i, CtClass declaring,
                      MethodInfo m, String type, int np) {
        super(pos, i, declaring, m);
        newTypeName = type;
        newPos = np;
    }

    /*
     * Not used
     * 
    private int getNameAndType(ConstPool cp) {
        int pos = currentPos;
        int c = iterator.byteAt(pos);
        int index = iterator.u16bitAt(pos + 1);

        if (c == INVOKEINTERFACE)
            return cp.getInterfaceMethodrefNameAndType(index);
        else
            return cp.getMethodrefNameAndType(index);
    } */

    /**
     * Returns the method or constructor containing the <code>new</code>
     * expression represented by this object.
     */
    @Override
    public CtBehavior where() { return super.where(); }

    /**
     * Returns the line number of the source line containing the
     * <code>new</code> expression.
     *
     * @return -1       if this information is not available.
     */
    @Override
    public int getLineNumber() {
        return super.getLineNumber();
    }

    /**
     * Returns the source file containing the <code>new</code> expression.
     *
     * @return null     if this information is not available.
     */
    @Override
    public String getFileName() {
        return super.getFileName();
    }

    /**
     * Returns the class of the created object.
     */
    private CtClass getCtClass() throws NotFoundException {
        return thisClass.getClassPool().get(newTypeName);
    }

    /**
     * Returns the class name of the created object.
     */
    public String getClassName() {
        return newTypeName;
    }

    /**
     * Get the signature of the constructor
     *
     * The signature is represented by a character string
     * called method descriptor, which is defined in the JVM specification.
     *
     * @see javassist.CtBehavior#getSignature()
     * @see javassist.bytecode.Descriptor
     * @return the signature
     */
    public String getSignature() {
        ConstPool constPool = getConstPool();
        int methodIndex = iterator.u16bitAt(currentPos + 1);   // constructor
        return constPool.getMethodrefType(methodIndex);
    }

    /**
     * Returns the constructor called for creating the object.
     */
    public CtConstructor getConstructor() throws NotFoundException {
        ConstPool cp = getConstPool();
        int index = iterator.u16bitAt(currentPos + 1);
        String desc = cp.getMethodrefType(index);
        return getCtClass().getConstructor(desc);
    }

    /**
     * Returns the list of exceptions that the expression may throw.
     * This list includes both the exceptions that the try-catch statements
     * including the expression can catch and the exceptions that
     * the throws declaration allows the method to throw.
     */
    @Override
    public CtClass[] mayThrow() {
        return super.mayThrow();
    }

    /*
     * Returns the parameter types of the constructor.

    public CtClass[] getParameterTypes() throws NotFoundException {
        ConstPool cp = getConstPool();
        int index = iterator.u16bitAt(currentPos + 1);
        String desc = cp.getMethodrefType(index);
        return Descriptor.getParameterTypes(desc, thisClass.getClassPool());
    }
    */

    private int canReplace() throws CannotCompileException {
        int op = iterator.byteAt(newPos + 3);
        if (op == Opcode.DUP)     // Typical single DUP or Javaflow DUP DUP2_X2 POP2
            return ((iterator.byteAt(newPos + 4) == Opcode.DUP2_X2
                 && iterator.byteAt(newPos + 5) == Opcode.POP2)) ? 6 : 4;
        else if (op == Opcode.DUP_X1
                 && iterator.byteAt(newPos + 4) == Opcode.SWAP)
            return 5;
        else
            return 3;   // for Eclipse.  The generated code may include no DUP.
            // throw new CannotCompileException(
            //            "sorry, cannot edit NEW followed by no DUP");
    }

    /**
     * Replaces the <code>new</code> expression with the bytecode derived from
     * the given source text.
     *
     * <p>$0 is available but the value is null.
     *
     * @param statement         a Java statement except try-catch.
     */
    @Override
    public void replace(String statement) throws CannotCompileException {
        thisClass.getClassFile();   // to call checkModify().

        final int bytecodeSize = 3;
        int pos = newPos;

        int newIndex = iterator.u16bitAt(pos + 1);

        /* delete the preceding NEW and DUP (or DUP_X1, SWAP) instructions.
         */
        int codeSize = canReplace();
        int end = pos + codeSize;
        for (int i = pos; i < end; ++i)
            iterator.writeByte(NOP, i);

        ConstPool constPool = getConstPool();
        pos = currentPos;
        int methodIndex = iterator.u16bitAt(pos + 1);   // constructor

        String signature = constPool.getMethodrefType(methodIndex);

        Javac jc = new Javac(thisClass);
        ClassPool cp = thisClass.getClassPool();
        CodeAttribute ca = iterator.get();
        try {
            CtClass[] params = Descriptor.getParameterTypes(signature, cp);
            CtClass newType = cp.get(newTypeName);
            int paramVar = ca.getMaxLocals();
            jc.recordParams(newTypeName, params,
                            true, paramVar, withinStatic());
            int retVar = jc.recordReturnType(newType, true);
            jc.recordProceed(new ProceedForNew(newType, newIndex,
                                               methodIndex));

            /* Is $_ included in the source code?
             */
            checkResultValue(newType, statement);

            Bytecode bytecode = jc.getBytecode();
            storeStack(params, true, paramVar, bytecode);
            jc.recordLocalVariables(ca, pos);

            bytecode.addConstZero(newType);
            bytecode.addStore(retVar, newType);     // initialize $_

            jc.compileStmnt(statement);
            if (codeSize > 3)   // if the original code includes DUP.
                bytecode.addAload(retVar);

            replace0(pos, bytecode, bytecodeSize);
        }
        catch (CompileError e) { throw new CannotCompileException(e); }
        catch (NotFoundException e) { throw new CannotCompileException(e); }
        catch (BadBytecode e) {
            throw new CannotCompileException("broken method");
        }
    }

    static class ProceedForNew implements ProceedHandler {
        CtClass newType;
        int newIndex, methodIndex;

        ProceedForNew(CtClass nt, int ni, int mi) {
            newType = nt;
            newIndex = ni;
            methodIndex = mi;
        }

        @Override
        public void doit(JvstCodeGen gen, Bytecode bytecode, ASTList args)
            throws CompileError
        {
            bytecode.addOpcode(NEW);
            bytecode.addIndex(newIndex);
            bytecode.addOpcode(DUP);
            gen.atMethodCallCore(newType, MethodInfo.nameInit, args,
                                 false, true, -1, null);
            gen.setType(newType);
        }

        @Override
        public void setReturnType(JvstTypeChecker c, ASTList args)
            throws CompileError
        {
            c.atMethodCallCore(newType, MethodInfo.nameInit, args);
            c.setType(newType);
        }
    }
}
