package org.bds.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.bds.compile.CompilerMessages;
import org.bds.compile.CompilerMessage.MessageType;
import org.bds.run.BdsThread;
import org.bds.scope.Scope;

/**
 * A 'goal' expression
 *
 * @author pcingola
 */
public class ExpressionGoal extends ExpressionUnary {

	public ExpressionGoal(BdsNode parent, ParseTree tree) {
		super(parent, tree);
		op = "goal";
	}

	@Override
	public Type returnType(Scope scope) {
		if (returnType != null) return returnType;
		returnType = TypeList.get(Type.STRING);
		return returnType;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void runStep(BdsThread bdsThread) {
		bdsThread.run(expr);
		if (bdsThread.isCheckpointRecover()) return;

		// Get expression value
		Object value = bdsThread.pop();

		// Goal returns a list of taskIds to be run
		List<String> taskIds = null;
		if (expr.isList()) {
			// Is is a list? Run goal for each element
			taskIds = new ArrayList<String>();

			// Process each goal
			Collection goals = (Collection) value;
			for (Object goal : goals)
				taskIds.addAll(bdsThread.goal(goal.toString()));
		} else {
			// Single valued
			taskIds = bdsThread.goal(value.toString());
		}

		// Add results to stack
		bdsThread.push(taskIds);
	}

	@Override
	protected void typeCheckNotNull(Scope scope, CompilerMessages compilerMessages) {
		super.typeCheckNotNull(scope, compilerMessages);

		if (!expr.getReturnType().isString()) compilerMessages.add(this, "Expression does not return 'string' (" + expr.getReturnType() + ")", MessageType.ERROR);
	}
}
