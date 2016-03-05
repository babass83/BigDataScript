package org.bds.lang;

import org.antlr.v4.runtime.tree.ParseTree;
import org.bds.run.BdsThread;
import org.bds.scope.Scope;
import org.bds.util.Gpr;

/**
 * A boolean literal
 *
 * @author pcingola
 */
public class LiteralBool extends Literal {

	boolean value;

	public LiteralBool(BdsNode parent, ParseTree tree) {
		super(parent, tree);
	}

	/**
	 * Evaluate an expression
	 */
	@Override
	public void runStep(BdsThread bdsThread) {
		bdsThread.push(value);
	}

	public boolean isValue() {
		return value;
	}

	@Override
	protected void parse(ParseTree tree) {
		value = Gpr.parseBoolSafe(tree.getChild(0).getText());
	}

	@Override
	public Type returnType(Scope scope) {
		if (returnType != null) return returnType;

		returnType = Type.BOOL;
		return returnType;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "" + value;
	}
}
