package org.bds.lang;

import org.antlr.v4.runtime.tree.ParseTree;
import org.bds.compile.CompilerMessages;
import org.bds.compile.CompilerMessage.MessageType;
import org.bds.run.BdsThread;
import org.bds.scope.Scope;

/**
 * Conditional expression
 *
 * 		expr ? exprTrue : exprFalse
 *
 * @author pcingola
 */
public class ExpressionCond extends Expression {

	Expression expr;
	Expression exprTrue;
	Expression exprFalse;

	public ExpressionCond(BdsNode parent, ParseTree tree) {
		super(parent, tree);
	}

	@Override
	protected boolean isReturnTypesNotNull() {
		if (expr == null || expr.getReturnType() == null) return false;
		if (exprTrue == null || exprTrue.getReturnType() == null) return false;
		if (exprFalse == null || exprFalse.getReturnType() == null) return false;
		return true;
	}

	@Override
	public boolean isStopDebug() {
		return true;
	}

	@Override
	protected void parse(ParseTree tree) {
		expr = (Expression) factory(tree, 0);
		exprTrue = (Expression) factory(tree, 2); // Child 1 is '?'
		exprFalse = (Expression) factory(tree, 4); // Child 3 is ':'
	}

	@Override
	public Type returnType(Scope scope) {
		if (returnType != null) return returnType;

		expr.returnType(scope);
		returnType = exprTrue.returnType(scope);
		exprFalse.returnType(scope);

		return returnType;
	}

	/**
	 * Evaluate an expression
	 */
	@Override
	public void runStep(BdsThread bdsThread) {
		bdsThread.run(expr);

		if (bdsThread.isCheckpointRecover()) {
			bdsThread.run(exprTrue);
			if (bdsThread.isCheckpointRecover()) bdsThread.run(exprFalse);
		} else {
			if (popBool(bdsThread)) bdsThread.run(exprTrue);
			else bdsThread.run(exprFalse);
		}
	}

	@Override
	public String toString() {
		return expr.toString() + " ? " + exprTrue + " : " + exprFalse;
	}

	@Override
	public void typeCheckNotNull(Scope scope, CompilerMessages compilerMessages) {
		if (expr != null) expr.checkCanCastBool(compilerMessages);

		if (exprTrue != null //
				&& exprFalse != null //
				&& !exprTrue.getReturnType().canCast(exprFalse.getReturnType()) //
				) compilerMessages.add(this, "Both expressions must be the same type. Expression for 'true': " + exprTrue.getReturnType() + ", expression for 'false' " + exprFalse.getReturnType(), MessageType.ERROR);
	}
}
