// Copyright (c) 2011, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.google.dart.compiler.backend.js;

import com.google.dart.compiler.backend.js.ast.*;
import com.google.dart.compiler.util.TextOutput;

import java.util.List;
import java.util.Map;

import static com.google.dart.compiler.backend.js.ast.JsNumberLiteral.JsDoubleLiteral;
import static com.google.dart.compiler.backend.js.ast.JsNumberLiteral.JsIntLiteral;

/**
 * Produces text output from a JavaScript AST.
 */
public class JsToStringGenerationVisitor extends JsVisitor {
    private static final char[] CHARS_BREAK = "break".toCharArray();
    private static final char[] CHARS_CASE = "case".toCharArray();
    private static final char[] CHARS_CATCH = "catch".toCharArray();
    private static final char[] CHARS_CONTINUE = "continue".toCharArray();
    private static final char[] CHARS_DEBUGGER = "debugger".toCharArray();
    private static final char[] CHARS_DEFAULT = "default".toCharArray();
    private static final char[] CHARS_DO = "do".toCharArray();
    private static final char[] CHARS_ELSE = "else".toCharArray();
    private static final char[] CHARS_FALSE = "false".toCharArray();
    private static final char[] CHARS_FINALLY = "finally".toCharArray();
    private static final char[] CHARS_FOR = "for".toCharArray();
    private static final char[] CHARS_FUNCTION = "function".toCharArray();
    private static final char[] CHARS_IF = "if".toCharArray();
    private static final char[] CHARS_IN = "in".toCharArray();
    private static final char[] CHARS_NEW = "new".toCharArray();
    private static final char[] CHARS_NULL = "null".toCharArray();
    private static final char[] CHARS_RETURN = "return".toCharArray();
    private static final char[] CHARS_SWITCH = "switch".toCharArray();
    private static final char[] CHARS_THIS = "this".toCharArray();
    private static final char[] CHARS_THROW = "throw".toCharArray();
    private static final char[] CHARS_TRUE = "true".toCharArray();
    private static final char[] CHARS_TRY = "try".toCharArray();
    private static final char[] CHARS_VAR = "var".toCharArray();
    private static final char[] CHARS_WHILE = "while".toCharArray();

    private static final char[] HEX_DIGITS = {
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * How many lines of code to print inside of a JsBlock when printing terse.
     */
    private static final int JS_BLOCK_LINES_TO_PRINT = 3;

    protected boolean needSemi = true;
    private boolean lineBreakAfterBlock = true;

    private boolean lineBreakAfterElse;

    protected final TextOutput p;

    public JsToStringGenerationVisitor(TextOutput out) {
        p = out;
    }

    @Override
    public void visitArrayAccess(JsArrayAccess x) {
        printPair(x, x.getArrayExpression());
        leftSquare();
        accept(x.getIndexExpression());
        rightSquare();
    }

    @Override
    public void visitArray(JsArrayLiteral x) {
        leftSquare();
        printExpressions(x.getExpressions());
        rightSquare();
    }

    private void printExpressions(List<JsExpression> expressions) {
        boolean notFirst = false;
        for (JsExpression expression : expressions) {
            notFirst = sepCommaOptSpace(notFirst) && !(expression instanceof JsDocComment);
            boolean isEnclosed = parenPushIfCommaExpression(expression);
            accept(expression);
            if (isEnclosed) {
                rightParen();
            }
        }
    }

    @Override
    public void visitBinaryExpression(JsBinaryOperation binaryOperation) {
        JsBinaryOperator operator = binaryOperation.getOperator();
        JsExpression arg1 = binaryOperation.getArg1();
        boolean isExpressionEnclosed = parenPush(binaryOperation, arg1, !operator.isLeftAssociative());

        accept(arg1);
        if (operator.isKeyword()) {
            _parenPopOrSpace(binaryOperation, arg1, !operator.isLeftAssociative());
        }
        else if (operator != JsBinaryOperator.COMMA) {
            if (isExpressionEnclosed) {
                rightParen();
            }
            spaceOpt();
        }

        p.print(operator.getSymbol());

        JsExpression arg2 = binaryOperation.getArg2();
        boolean isParenOpened;
        if (operator == JsBinaryOperator.COMMA) {
            isParenOpened = false;
            spaceOpt();
        }
        else if (arg2 instanceof JsBinaryOperation && ((JsBinaryOperation) arg2).getOperator() == JsBinaryOperator.AND) {
            spaceOpt();
            leftParen();
            isParenOpened = true;
        }
        else {
            if (spaceCalc(operator, arg2)) {
                isParenOpened = _parenPushOrSpace(binaryOperation, arg2, operator.isLeftAssociative());
            }
            else {
                spaceOpt();
                isParenOpened = parenPush(binaryOperation, arg2, operator.isLeftAssociative());
            }
        }
        accept(arg2);
        if (isParenOpened) {
            rightParen();
        }
    }

    @Override
    public void visitBlock(JsBlock block) {
        printBlock(block, true, true);
    }

    @Override
    public void visitBoolean(JsLiteral.JsBooleanLiteral x) {
        if (x.getValue()) {
            p.print(CHARS_TRUE);
        }
        else {
            p.print(CHARS_FALSE);
        }
    }

    @Override
    public void visitBreak(JsBreak x) {
        p.print(CHARS_BREAK);
        continueOrBreakLabel(x);
    }

    @Override
    public void visitContinue(JsContinue x) {
        p.print(CHARS_CONTINUE);
        continueOrBreakLabel(x);
    }

    private void continueOrBreakLabel(JsContinue x) {
        String label = x.getLabel();
        if (label != null) {
            space();
            p.print(label);
        }
    }

    @Override
    public void visitCase(JsCase x) {
        p.print(CHARS_CASE);
        space();
        accept(x.getCaseExpression());
        _colon();
        newlineOpt();

        printSwitchMemberStatements(x);
    }

    private void printSwitchMemberStatements(JsSwitchMember x) {
        p.indentIn();
        for (JsStatement stmt : x.getStatements()) {
            needSemi = true;
            accept(stmt);
            if (needSemi) {
                semi();
            }
            newlineOpt();
        }
        p.indentOut();
        needSemi = false;
    }

    @Override
    public void visitCatch(JsCatch x) {
        p.print(CHARS_CATCH);
        spaceOpt();
        leftParen();
        p.print(x.getParameter().getName());

        // Optional catch condition.
        JsExpression catchCond = x.getCondition();
        if (catchCond != null) {
            space();
            p.print(CHARS_IF);
            space();
            accept(catchCond);
        }

        rightParen();
        spaceOpt();
        accept(x.getBody());
    }

    @Override
    public void visitConditional(JsConditional x) {
        // Associativity: for the then and else branches, it is safe to insert
        // another
        // ternary expression, but if the test expression is a ternary, it should
        // get parentheses around it.
        printPair(x, x.getTestExpression());
        spaceOpt();
        p.print('?');
        spaceOpt();
        printPair(x, x.getThenExpression());
        spaceOpt();
        _colon();
        spaceOpt();
        printPair(x, x.getElseExpression());
    }

    private void printPair(JsExpression parent, JsExpression expression) {
        boolean isNeedParen = parenCalc(parent, expression, false);
        if (isNeedParen) {
            leftParen();
        }
        accept(expression);
        if (isNeedParen) {
            rightParen();
        }
    }

    @Override
    public void visitDebugger(JsDebugger x) {
        p.print(CHARS_DEBUGGER);
    }

    @Override
    public void visitDefault(JsDefault x) {
        p.print(CHARS_DEFAULT);
        _colon();

        printSwitchMemberStatements(x);
    }

    @Override
    public void visitWhile(JsWhile x) {
        _while();
        spaceOpt();
        leftParen();
        accept(x.getCondition());
        rightParen();
        indentIfNotBlock(x.getBody());
        accept(x.getBody());
        nestedPop(x.getBody());
    }

    @Override
    public void visitDoWhile(JsDoWhile x) {
        p.print(CHARS_DO);
        indentIfNotBlock(x.getBody());
        accept(x.getBody());
        nestedPop(x.getBody());
        if (needSemi) {
            semi();
            newlineOpt();
        }
        else {
            spaceOpt();
            needSemi = true;
        }
        _while();
        spaceOpt();
        leftParen();
        accept(x.getCondition());
        rightParen();
    }

    @Override
    public void visitEmpty(JsEmpty x) {
    }

    @Override
    public void visitExpressionStatement(JsExpressionStatement x) {
        boolean surroundWithParentheses = JsFirstExpressionVisitor.exec(x);
        if (surroundWithParentheses) {
            leftParen();
        }
        accept(x.getExpression());
        if (surroundWithParentheses) {
            rightParen();
        }
    }

    @Override
    public void visitFor(JsFor x) {
        _for();
        spaceOpt();
        leftParen();

        if (x.getInitExpression() != null) {
            accept(x.getInitExpression());
        }
        else if (x.getInitVars() != null) {
            accept(x.getInitVars());
        }

        semi();

        if (x.getCondition() != null) {
            spaceOpt();
            accept(x.getCondition());
        }

        semi();

        if (x.getIncrementExpression() != null) {
            spaceOpt();
            accept(x.getIncrementExpression());
        }

        rightParen();
        indentIfNotBlock(x.getBody());
        accept(x.getBody());
        nestedPop(x.getBody());
    }

    @Override
    public void visitForIn(JsForIn x) {
        _for();
        spaceOpt();
        leftParen();

        if (x.getIterVarName() != null) {
            var();
            space();
            p.print(x.getIterVarName());

            if (x.getIterExpression() != null) {
                spaceOpt();
                assignment();
                spaceOpt();
                accept(x.getIterExpression());
            }
        }
        else {
            accept(x.getIterExpression());
        }

        space();
        p.print(CHARS_IN);
        space();
        accept(x.getObjectExpression());

        rightParen();
        indentIfNotBlock(x.getBody());
        accept(x.getBody());
        nestedPop(x.getBody());
    }

    @Override
    public void visitFunction(JsFunction function) {
        p.print(CHARS_FUNCTION);
        space();
        if (function.getName() != null) {
            nameOf(function);
        }

        leftParen();
        boolean notFirst = false;
        List<JsParameter> parameters = function.getParameters();
        if (!parameters.isEmpty()) {
            for (JsParameter element : parameters) {
                notFirst = sepCommaOptSpace(notFirst);
                accept(element);
            }
        }
        rightParen();
        space();

        lineBreakAfterBlock = false;
        accept(function.getBody());
        needSemi = false;
    }

    @Override
    public void visitIf(JsIf x) {
        boolean doLineBreakAfterElse = lineBreakAfterElse;
        lineBreakAfterElse = false;

        p.print(CHARS_IF);
        spaceOpt();
        leftParen();
        accept(x.getIf());
        rightParen();

        JsNode then = x.getThen();
        printThenOrElseNode(then);

        JsNode elseStatement = x.getElse();
        if (elseStatement != null) {
            if (then instanceof JsBlock || doLineBreakAfterElse) {
                newlineOpt();
            }
            else if (elseStatement instanceof JsIf) {
                if (p.isCompact()) {
                    space();
                }
                else {
                    p.newline();
                }
            }
            else {
                spaceOpt();
            }
            p.print(CHARS_ELSE);
            if (elseStatement instanceof JsIf) {
                space();
                lineBreakAfterElse = true;
                accept(elseStatement);
            }
            else {
                printThenOrElseNode(elseStatement);
            }
        }
    }

    private void printThenOrElseNode(JsNode node) {
        boolean isBlock = node instanceof JsBlock;
        if (isBlock) {
            if (!p.isCompact()) {
                p.newline();
                p.indentIn();
            }
        }
        else {
            spaceOpt();
        }
        accept(node);
        if (isBlock) {
            p.indentOut();
        }
        else {
           semi();
        }
    }

    @Override
    public void visitInvocation(JsInvocation invocation) {
        if (invocation.getQualifier() != null) {
            printPair(invocation, invocation.getQualifier());
        }

        leftParen();
        printExpressions(invocation.getArguments());
        rightParen();
    }

    @Override
    public void visitLabel(JsLabel x) {
        nameOf(x);
        _colon();
        spaceOpt();
        accept(x.getStatement());
    }

    @Override
    public void visitNameRef(JsNameRef nameRef) {
        JsExpression qualifier = nameRef.getQualifier();
        if (qualifier != null) {
            boolean enclose = qualifier instanceof JsNumberLiteral || parenCalc(nameRef, qualifier, false);
            if (enclose) {
                leftParen();
            }
            accept(qualifier);
            if (enclose) {
                rightParen();
            }
            p.print('.');
        }

        p.maybeIndent();
        beforeNodePrinted(nameRef);
        p.print(nameRef.getName());
    }

    protected void beforeNodePrinted(@SuppressWarnings("UnusedParameters") JsNode node) {
    }

    @Override
    public void visitNew(JsNew x) {
        p.print(CHARS_NEW);
        space();

        JsExpression constructorExpression = x.getConstructorExpression();
        boolean needsParens = JsConstructExpressionVisitor.exec(constructorExpression);
        if (needsParens) {
            leftParen();
        }
        accept(constructorExpression);
        if (needsParens) {
            rightParen();
        }

        leftParen();
        printExpressions(x.getArguments());
        rightParen();
    }

    @Override
    public void visitNull(JsNullLiteral x) {
        p.print(CHARS_NULL);
    }

    @Override
    public void visitInt(JsIntLiteral x) {
        p.print(x.value);
    }

    @Override
    public void visitDouble(JsDoubleLiteral x) {
        p.print(x.value);
    }

    @Override
    public void visitObjectLiteral(JsObjectLiteral objectLiteral) {
        p.print('{');
        if (objectLiteral.isMultiline()) {
            p.indentIn();
        }

        boolean notFirst = false;
        for (JsPropertyInitializer item : objectLiteral.getPropertyInitializers()) {
            if (notFirst) {
                p.print(',');
            }

            if (objectLiteral.isMultiline()) {
                newlineOpt();
            }
            else if (notFirst) {
                spaceOpt();
            }

            notFirst = true;

            JsExpression labelExpr = item.getLabelExpr();
            // labels can be either string, integral, or decimal literals
            if (item.getLabel() != null) {
                p.print(item.getLabel());
            }
            else if (labelExpr instanceof JsNameRef) {
                p.print(((JsNameRef) labelExpr).getName());
            }
            else if (labelExpr instanceof JsStringLiteral) {
                p.print(((JsStringLiteral) labelExpr).getValue());
            }
            else {
                accept(labelExpr);
            }

            _colon();
            space();
            JsExpression valueExpr = item.getValueExpr();
            boolean wasEnclosed = parenPushIfCommaExpression(valueExpr);
            accept(valueExpr);
            if (wasEnclosed) {
                rightParen();
            }
        }

        if (objectLiteral.isMultiline()) {
            p.indentOut();
            newlineOpt();
        }

        p.print('}');
    }

    @Override
    public void visitParameter(JsParameter x) {
        nameOf(x);
    }

    @Override
    public void visitPostfixOperation(JsPostfixOperation x) {
        JsUnaryOperator op = x.getOperator();
        JsExpression arg = x.getArg();
        // unary operators always associate correctly (I think)
        printPair(x, arg);
        p.print(op.getSymbol());
    }

    @Override
    public void visitPrefixOperation(JsPrefixOperation x) {
        JsUnaryOperator op = x.getOperator();
        p.print(op.getSymbol());
        JsExpression arg = x.getArg();
        if (spaceCalc(op, arg)) {
            space();
        }
        // unary operators always associate correctly (I think)
        printPair(x, arg);
    }

    @Override
    public void visitProgram(JsProgram x) {
        p.print("<JsProgram>");
    }

    @Override
    public void visitRegExp(JsRegExp x) {
        slash();
        p.print(x.getPattern());
        slash();
        String flags = x.getFlags();
        if (flags != null) {
            p.print(flags);
        }
    }

    @Override
    public void visitReturn(JsReturn x) {
        p.print(CHARS_RETURN);
        JsExpression expr = x.getExpression();
        if (expr != null) {
            space();
            accept(expr);
        }
    }

    /**
     * Generate JavaScript code that evaluates to the supplied string. Adapted
     * from {@link org.mozilla.javascript.ScriptRuntime#escapeString(String)}
     * . The difference is that we quote with either &quot; or &apos; depending on
     * which one is used less inside the string.
     */
    @SuppressWarnings("JavadocReference")
    private static CharSequence javaScriptString(CharSequence chars, StringBuilder result, boolean forceDoubleQuote) {
        final int n = chars.length();
        int quoteCount = 0;
        int aposCount = 0;

        for (int i = 0; i < n; i++) {
            switch (chars.charAt(i)) {
                case '"':
                    ++quoteCount;
                    break;
                case '\'':
                    ++aposCount;
                    break;
            }
        }

        char quoteChar = (quoteCount < aposCount || forceDoubleQuote) ? '"' : '\'';
        result.ensureCapacity(result.length() + chars.length() + 2);
        result.append(quoteChar);
        for (int i = 0; i < n; i++) {
            char c = chars.charAt(i);

            if (' ' <= c && c <= '~' && c != quoteChar && c != '\\') {
                // an ordinary print character (like C isprint())
                result.append(c);
                continue;
            }

            int escape = -1;
            switch (c) {
                case '\b':
                    escape = 'b';
                    break;
                case '\f':
                    escape = 'f';
                    break;
                case '\n':
                    escape = 'n';
                    break;
                case '\r':
                    escape = 'r';
                    break;
                case '\t':
                    escape = 't';
                    break;
                case '"':
                    escape = '"';
                    break; // only reach here if == quoteChar
                case '\'':
                    escape = '\'';
                    break; // only reach here if == quoteChar
                case '\\':
                    escape = '\\';
                    break;
            }

            if (escape >= 0) {
                // an \escaped sort of character
                result.append('\\');
                result.append((char) escape);
            }
            else {
                        /*
                        * Emit characters from 0 to 31 that don't have a single character
                        * escape sequence in octal where possible. This saves one or two
                        * characters compared to the hexadecimal format '\xXX'.
                        *
                        * These short octal sequences may only be used at the end of the string
                        * or where the following character is a non-digit. Otherwise, the
                        * following character would be incorrectly interpreted as belonging to
                        * the sequence.
                        */
                if (c < ' ' && (i == n - 1 || chars.charAt(i + 1) < '0' || chars.charAt(i + 1) > '9')) {
                    result.append('\\');
                    if (c > 0x7) {
                        result.append((char) ('0' + (0x7 & (c >> 3))));
                    }
                    result.append((char) ('0' + (0x7 & c)));
                }
                else {
                    int hexSize;
                    if (c < 256) {
                        // 2-digit hex
                        result.append("\\x");
                        hexSize = 2;
                    }
                    else {
                        // Unicode.
                        result.append("\\u");
                        hexSize = 4;
                    }
                    // append hexadecimal form of ch left-padded with 0
                    for (int shift = (hexSize - 1) * 4; shift >= 0; shift -= 4) {
                        int digit = 0xf & (c >> shift);
                        result.append(HEX_DIGITS[digit]);
                    }
                }
            }
        }
        result.append(quoteChar);
        return result;
    }

    @Override
    public void visitString(JsStringLiteral stringLiteral) {
        if (stringLiteral.isUnescaped()) {
            p.maybeIndent();
            StringBuilder builder = p.getBuilder();
            int size = builder.length();
            javaScriptString(stringLiteral.getValue(), builder, false);
            p.builderProduced(builder.length() - size);
            return;
        }

        p.print(stringLiteral.getQuote());
        p.print(stringLiteral.getValue());
        p.print(stringLiteral.getQuote());
    }

    @Override
    public void visit(JsSwitch x) {
        p.print(CHARS_SWITCH);
        spaceOpt();
        leftParen();
        accept(x.getExpression());
        rightParen();
        spaceOpt();
        blockOpen();
        acceptList(x.getCases());
        blockClose();
    }

    @Override
    public void visitThis(JsLiteral.JsThisRef x) {
        p.print(CHARS_THIS);
    }

    @Override
    public void visitThrow(JsThrow x) {
        p.print(CHARS_THROW);
        space();
        accept(x.getExpression());
    }

    @Override
    public void visitTry(JsTry x) {
        p.print(CHARS_TRY);
        spaceOpt();
        accept(x.getTryBlock());

        acceptList(x.getCatches());

        JsBlock finallyBlock = x.getFinallyBlock();
        if (finallyBlock != null) {
            p.print(CHARS_FINALLY);
            spaceOpt();
            accept(finallyBlock);
        }
    }

    @Override
    public void visit(JsVar var) {
        nameOf(var);
        JsExpression initExpr = var.getInitExpression();
        if (initExpr != null) {
            spaceOpt();
            assignment();
            spaceOpt();
            boolean isEnclosed = parenPushIfCommaExpression(initExpr);
            accept(initExpr);
            if (isEnclosed) {
                rightParen();
            }
        }
    }

    @Override
    public void visitVars(JsVars vars) {
        var();
        space();
        boolean sep = false;
        for (JsVar var : vars.getVars()) {
            if (sep) {
                if (vars.isMultiline()) {
                    newlineOpt();
                }
                p.print(',');
                spaceOpt();
            }
            else {
                sep = true;
            }

            accept(var);
        }
    }

    @Override
    public void visitDocComment(JsDocComment comment) {
        boolean asSingleLine = comment.getTags().size() == 1;
        if (!asSingleLine) {
            newlineOpt();
        }
        p.print("/**");
        if (asSingleLine) {
            space();
        }
        else {
            p.newline();
        }

        boolean notFirst = false;
        for (Map.Entry<String, Object> entry : comment.getTags().entrySet()) {
            if (notFirst) {
                p.newline();
                p.print(' ');
                p.print('*');
            }
            else {
                notFirst = true;
            }

            p.print('@');
            p.print(entry.getKey());
            Object value = entry.getValue();
            if (value != null) {
                space();
                if (value instanceof CharSequence) {
                    p.print((CharSequence) value);
                }
                else {
                    accept((JsExpression) value);
                }
            }

            if (!asSingleLine) {
                p.newline();
            }
        }

        if (asSingleLine) {
            space();
        }
        else {
            newlineOpt();
        }

        p.print('*');
        p.print('/');
        if (asSingleLine) {
            spaceOpt();
        }
    }

    protected final void newlineOpt() {
        if (!p.isCompact()) {
            p.newline();
        }
    }

    protected void printBlock(JsBlock block, boolean truncate, boolean finalNewline) {
        if (!lineBreakAfterBlock) {
            finalNewline = false;
            lineBreakAfterBlock = true;
        }

        boolean needBraces = !block.isGlobalBlock();
        if (needBraces) {
            blockOpen();
        }

        int count = 0;
        List<JsNode> nodes = block.getStatements();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = nodes.size(); i < n; i++) {
            if (truncate && count > JS_BLOCK_LINES_TO_PRINT) {
                p.print("[...]");
                newlineOpt();
                break;
            }
            JsNode node = nodes.get(i);
            if (node instanceof JsEmpty) {
                continue;
            }

            accept(node);
            if (p.isCompact()) {
                semi();
            }
            else {
                if (!(node instanceof JsFunction) &&
                    (node instanceof JsLiteral ||
                     node instanceof JsInvocation ||
                     node instanceof JsVars ||
                     node instanceof JsArrayAccess ||
                     node instanceof JsBinaryOperation ||
                     node instanceof JsUnaryOperation ||
                     node instanceof JsOperator ||
                     node instanceof JsNameRef ||
                     node instanceof JsDebugger ||
                     node instanceof JsExpressionStatement ||
                     node instanceof JsContinue)) {
                    semi();
                }
                p.newline();
            }
            ++count;
        }

        if (needBraces) {
            p.indentOut();
            p.print('}');
            if (finalNewline) {
                newlineOpt();
            }
        }
        needSemi = false;
    }

    private void assignment() {
        p.print('=');
    }

    private void blockClose() {
        p.indentOut();
        p.print('}');
        newlineOpt();
    }

    private void blockOpen() {
        p.print('{');
        p.indentIn();
        newlineOpt();
    }

    private void _colon() {
        p.print(':');
    }

    private void _for() {
        p.print(CHARS_FOR);
    }

    private void leftParen() {
        p.print('(');
    }

    private void leftSquare() {
        p.print('[');
    }

    private void nameOf(HasName hasName) {
        p.print(hasName.getName());
    }

    private boolean nestedPop(JsStatement statement) {
        boolean pop = !(statement instanceof JsBlock);
        if (pop) {
            p.indentOut();
        }
        return pop;
    }

    private boolean indentIfNotBlock(JsStatement statement) {
        boolean indented = !(statement instanceof JsBlock);
        if (indented) {
            newlineOpt();
            p.indentIn();
        }
        else {
            spaceOpt();
        }
        return indented;
    }

    private static boolean parenCalc(JsExpression parent, JsExpression child, boolean wrongAssoc) {
        int parentPrec = JsPrecedenceVisitor.exec(parent);
        int childPrec = JsPrecedenceVisitor.exec(child);
        return parentPrec > childPrec || parentPrec == childPrec && wrongAssoc;
    }

    private boolean _parenPopOrSpace(JsExpression parent, JsExpression child, boolean wrongAssoc) {
        boolean doPop = parenCalc(parent, child, wrongAssoc);
        if (doPop) {
            rightParen();
        }
        else {
            space();
        }
        return doPop;
    }

    private boolean parenPush(JsExpression parent, JsExpression child, boolean wrongAssoc) {
        boolean doPush = parenCalc(parent, child, wrongAssoc);
        if (doPush) {
            leftParen();
        }
        return doPush;
    }

    private boolean parenPushIfCommaExpression(JsExpression x) {
        boolean doPush = x instanceof JsBinaryOperation && ((JsBinaryOperation) x).getOperator() == JsBinaryOperator.COMMA;
        if (doPush) {
            leftParen();
        }
        return doPush;
    }

    private boolean _parenPushOrSpace(JsExpression parent, JsExpression child, boolean wrongAssoc) {
        boolean doPush = parenCalc(parent, child, wrongAssoc);
        if (doPush) {
            leftParen();
        }
        else {
            space();
        }
        return doPush;
    }

    private void rightParen() {
        p.print(')');
    }

    private void rightSquare() {
        p.print(']');
    }

    private void semi() {
        p.print(';');
    }

    private boolean sepCommaOptSpace(boolean sep) {
        if (sep) {
            p.print(',');
            spaceOpt();
        }
        return true;
    }

    private void slash() {
        p.print('/');
    }

    private void space() {
        p.print(' ');
    }

    /**
     * Decide whether, if <code>op</code> is printed followed by <code>arg</code>,
     * there needs to be a space between the operator and expression.
     *
     * @return <code>true</code> if a space needs to be printed
     */
    private static boolean spaceCalc(JsOperator op, JsExpression arg) {
        if (op.isKeyword()) {
            return true;
        }
        if (arg instanceof JsBinaryOperation) {
            JsBinaryOperation binary = (JsBinaryOperation) arg;
            /*
            * If the binary operation has a higher precedence than op, then it won't
            * be parenthesized, so check the first argument of the binary operation.
            */
            return binary.getOperator().getPrecedence() > op.getPrecedence() && spaceCalc(op, binary.getArg1());
        }
        if (arg instanceof JsPrefixOperation) {
            JsOperator op2 = ((JsPrefixOperation) arg).getOperator();
            return (op == JsBinaryOperator.SUB || op == JsUnaryOperator.NEG)
                   && (op2 == JsUnaryOperator.DEC || op2 == JsUnaryOperator.NEG)
                   || (op == JsBinaryOperator.ADD && op2 == JsUnaryOperator.INC);
        }
        if (arg instanceof JsNumberLiteral && (op == JsBinaryOperator.SUB || op == JsUnaryOperator.NEG)) {
            if (arg instanceof JsIntLiteral) {
                return ((JsIntLiteral) arg).value < 0;
            }
            else {
                assert arg instanceof JsDoubleLiteral;
                //noinspection CastConflictsWithInstanceof
                return ((JsDoubleLiteral) arg).value < 0;
            }
        }
        return false;
    }

    private void spaceOpt() {
        p.printOpt(' ');
    }

    private void var() {
        p.print(CHARS_VAR);
    }

    private void _while() {
        p.print(CHARS_WHILE);
    }
}
