package org.bds.lang;

import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Expression
 * 
 * @author pcingola
 */
public class ExpressionAssignmentBitOr extends ExpressionAssignmentBinary {

	public ExpressionAssignmentBitOr(BdsNode parent, ParseTree tree) {
		super(parent, tree);
	}

	@Override
	protected ExpressionBinary createSubExpression() {
		return new ExpressionBitOr(this, null);
	}

	@Override
	protected String op() {
		return "|=";
	}

}
