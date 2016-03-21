package org.bds.lang;

import org.antlr.v4.runtime.tree.ParseTree;
import org.bds.run.BdsThread;

/**
 * A bitwise XOR
 *
 * @author pcingola
 */
public class ExpressionBitXor extends ExpressionBit {

	public ExpressionBitXor(BdsNode parent, ParseTree tree) {
		super(parent, tree);
	}

	@Override
	protected String op() {
		return "^";
	}

	/**
	 * Evaluate an expression
	 */
	@Override
	public void runStep(BdsThread bdsThread) {
		bdsThread.run(left);
		bdsThread.run(right);

		if (bdsThread.isCheckpointRecover()) return;

		bdsThread.push(left.popInt(bdsThread) ^ right.popInt(bdsThread));
	}

}
