package org.bds.lang;

import org.antlr.v4.runtime.tree.ParseTree;
import org.bds.compile.CompilerMessages;
import org.bds.compile.CompilerMessage.MessageType;
import org.bds.scope.Scope;

/**
 * Arguments
 *
 * @author pcingola
 */
public class Args extends BdsNode {

	protected Expression arguments[];

	/**
	 * Create 'method' arguments by prepending 'this' argument expression
	 */
	public static Args getArgsThis(Args args, Expression exprThis) {
		Args argsThis = new Args(null, null);
		argsThis.parent = args.parent;

		// Create new arguments
		int len = (args.arguments == null ? 0 : args.arguments.length) + 1;
		argsThis.arguments = new Expression[len];

		// Copy arguments
		argsThis.arguments[0] = exprThis;
		if (args.arguments != null) {
			for (int i = 0; i < args.arguments.length; i++) {
				Expression expr = args.arguments[i];
				argsThis.arguments[i + 1] = expr; // Assign to new arguments
				expr.parent = argsThis; // Update parent
			}
		}

		return argsThis;
	}

	public Args(BdsNode parent, ParseTree tree) {
		super(parent, tree);
	}

	public Expression[] getArguments() {
		return arguments;
	}

	@Override
	protected void parse(ParseTree tree) {
		parse(tree, 0, tree.getChildCount());
	}

	protected void parse(ParseTree tree, int offset, int max) {
		int num = (max - offset + 1) / 2;
		arguments = new Expression[num];

		for (int i = offset, j = 0; i < max; i += 2, j++) { // Note: Increment by 2 to skip separating commas
			arguments[j] = (Expression) factory(tree, i);
		}
	}

	/**
	 * Calculate return type for every expression
	 */
	@Override
	public Type returnType(Scope scope) {
		if (returnType != null) return returnType;

		if (arguments != null) {
			for (Expression e : arguments)
				if (e.getReturnType() == null) // Only assign this to show that calculation was already performed
					returnType = e.returnType(scope);

		} else returnType = Type.VOID;

		return returnType;
	}

	@Override
	protected void sanityCheck(CompilerMessages compilerMessages) {
		// Check that all arguments are expressions
		int argNum = 1;
		for (BdsNode node : arguments) {
			if (!(node instanceof Expression)) compilerMessages.add(node, "Expression expected as argument number " + argNum + " (instead of '" + node.getClass().getSimpleName() + "')", MessageType.ERROR);
			argNum++;
		}
	}

	public int size() {
		return (arguments != null ? arguments.length : 0);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arguments.length; i++) {
			sb.append(arguments[i]);
			if (i < arguments.length - 1) sb.append(",");
		}
		return sb.toString();
	}

}
