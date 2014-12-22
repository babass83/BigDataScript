package ca.mcgill.mcb.pcingola.bigDataScript.lang;

import java.util.ArrayList;
import java.util.Collection;

import org.antlr.v4.runtime.tree.ParseTree;

import ca.mcgill.mcb.pcingola.bigDataScript.compile.CompilerMessage.MessageType;
import ca.mcgill.mcb.pcingola.bigDataScript.compile.CompilerMessages;
import ca.mcgill.mcb.pcingola.bigDataScript.run.BigDataScriptThread;
import ca.mcgill.mcb.pcingola.bigDataScript.scope.Scope;

/**
 * A sum of two expressions
 *
 * @author pcingola
 */
public class ExpressionPlus extends ExpressionMath {

	public ExpressionPlus(BigDataScriptNode parent, ParseTree tree) {
		super(parent, tree);
	}

	/**
	 * Evaluate an expression
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void eval(BigDataScriptThread csThread) {
		if (isInt()) {
			csThread.push(left.evalInt(csThread) + right.evalInt(csThread));
			return;
		}

		if (isReal()) {
			csThread.push(left.evalReal(csThread) + right.evalReal(csThread));
			return;
		}

		if (isString()) {
			csThread.push(left.evalString(csThread) + right.evalString(csThread));
			return;
		}

		if (isList()) {
			ArrayList list = new ArrayList();
			left.eval(csThread);
			if (left.isList()) list.addAll((Collection) csThread.pop());
			else list.add(csThread.pop());

			right.eval(csThread);
			if (right.isList()) list.addAll((Collection) csThread.pop());
			else list.add(csThread.pop());

			csThread.push(list);
			return;
		}

		throw new RuntimeException("Unknown return type " + returnType + " for expression " + getClass().getSimpleName());
	}

	@Override
	protected String op() {
		return "+";
	}

	@Override
	public Type returnType(Scope scope) {
		if (returnType != null) return returnType;

		super.returnType(scope);

		if (left.canCastInt() && right.canCastInt()) returnType = Type.INT;
		else if (left.canCastReal() && right.canCastReal()) returnType = Type.REAL;
		else if (left.isList() && right.isList()) {
			if (left.getReturnType() == null || right.getReturnType() == null) return null;
			if (left.getReturnType().compareTo(right.getReturnType()) == 0) returnType = left.getReturnType(); // List plus List
		} else if (left.isList() && !right.isList()) {
			TypeList tlist = (TypeList) left.getReturnType();
			if (left.getReturnType() == null || right.getReturnType() == null) return null;
			if (right.getReturnType().compareTo(tlist.getBaseType()) == 0) returnType = left.getReturnType(); // List plus Item
		} else if (!left.isList() && right.isList()) {
			TypeList tlist = (TypeList) right.getReturnType();
			if (left.getReturnType() == null || right.getReturnType() == null) return null;
			if (left.getReturnType().compareTo(tlist.getBaseType()) == 0) returnType = right.getReturnType(); // Item plus List
		} else if (right.isList() && left.getReturnType().canCast(right.getReturnType())) returnType = right.getReturnType(); // Item plus List
		else if (left.isString() || right.isString()) returnType = Type.STRING;

		return returnType;
	}

	@Override
	public void typeCheckNotNull(Scope scope, CompilerMessages compilerMessages) {
		if (left.isList() && right.isList()) {
			if (left.getReturnType().compareTo(right.getReturnType()) != 0) {
				compilerMessages.add(this, "Cannot append " + right.getReturnType() + " to " + left.getReturnType(), MessageType.ERROR);
			}
		} else if (left.isList() && !right.isList()) {
			TypeList tlist = (TypeList) left.getReturnType();
			if (right.getReturnType().compareTo(tlist.getBaseType()) != 0) compilerMessages.add(this, "Cannot append " + right.getReturnType() + " to " + left.getReturnType(), MessageType.ERROR);
		} else if (right.isList() && !left.isList()) {
			TypeList tlist = (TypeList) right.getReturnType();
			if (left.getReturnType().compareTo(tlist.getBaseType()) != 0) compilerMessages.add(this, "Cannot append " + left.getReturnType() + " to " + right.getReturnType(), MessageType.ERROR);
		} else if (left.isString() || right.isString()) {
			// Either side is a string? => String plus String
		} else {
			// Normal 'math'
			left.checkCanCastIntOrReal(compilerMessages);
			right.checkCanCastIntOrReal(compilerMessages);
		}
	}

}
