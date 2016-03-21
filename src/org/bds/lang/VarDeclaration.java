package org.bds.lang;

import org.antlr.v4.runtime.tree.ParseTree;
import org.bds.compile.CompilerMessages;
import org.bds.compile.CompilerMessage.MessageType;
import org.bds.run.BdsThread;
import org.bds.scope.Scope;
import org.bds.scope.ScopeSymbol;

/**
 * Variable declaration
 *
 * @author pcingola
 */
public class VarDeclaration extends Statement {

	boolean implicit;
	Type type;
	VariableInit varInit[];

	public static VarDeclaration get(BdsNode parent, Type type, String varName, Expression expression) {
		VarDeclaration vd = new VarDeclaration(parent, null);
		vd.type = type;
		vd.varInit = new VariableInit[1];
		vd.varInit[0] = VariableInit.get(varName, expression);
		return vd;
	}

	public static VarDeclaration get(Type type, String varName) {
		return get(null, type, varName, null);
	}

	public VarDeclaration(BdsNode parent, ParseTree tree) {
		super(parent, tree);
	}

	public Type getType() {
		return type;
	}

	public VariableInit[] getVarInit() {
		return varInit;
	}

	@Override
	public boolean isStopDebug() {
		return false;
	}

	@Override
	protected void parse(ParseTree tree) {
		int idx = 0;

		String classname = tree.getChild(0).getClass().getSimpleName();
		if (classname.equals("VariableInitImplicitContext")) {
			// Variable 'short' declaration
			// Format : varMame := initValue
			// E.g.   : i := 2
			implicit = true;
			varInit = new VariableInit[1];
			varInit[0] = (VariableInit) factory(tree, idx);
		} else {
			// Variable 'classic' declaration
			// Format : type varMame = initValue
			// E.g.   : int i = 2
			implicit = false;
			type = (Type) factory(tree, idx++);

			// Create VarInit nodes
			int num = tree.getChildCount() / 2;
			varInit = new VariableInit[num];

			// Parse all VarInit nodes
			for (int i = idx, j = 0; i < tree.getChildCount(); i++) {
				varInit[j++] = (VariableInit) factory(tree, i);
				i++; // ','
			}
		}
	}

	/**
	 * Run
	 */
	@Override
	public void runStep(BdsThread bdsThread) {
		for (VariableInit vi : varInit) {
			if (!bdsThread.isCheckpointRecover()) {
				bdsThread.getScope().add(new ScopeSymbol(vi.varName, type)); // Add variable to scope
			}

			bdsThread.run(vi);

			// Act based on run state
			switch (bdsThread.getRunState()) {
			case OK: // OK do nothing
			case CHECKPOINT_RECOVER:
				break;

			case BREAK: // Break form this block immediately
			case CONTINUE:
			case RETURN:
			case EXIT:
			case FATAL_ERROR:
				return;

			default:
				throw new RuntimeException("Unhandled RunState: " + bdsThread.getRunState());
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (type != null) sb.append(type + " ");
		for (int i = 0; i < varInit.length; i++) {
			sb.append(varInit[i]);
			if (i < varInit.length - 1) sb.append(",");
		}
		return sb.toString();
	}

	@Override
	protected void typeCheck(Scope scope, CompilerMessages compilerMessages) {
		// Add all symbols
		for (VariableInit vi : varInit) {
			String varName = vi.varName;

			// Already declared?
			if (scope.hasSymbolLocal(varName)) {
				String other = "";
				if (scope.getFunctionsLocal(varName) != null) {
					ScopeSymbol ssf = scope.getFunctionsLocal(varName).get(0);
					FunctionDeclaration fdecl = (FunctionDeclaration) ssf.getValue();

					other = " (function '" + varName + "' declared in " + fdecl.getFileName() + ", line " + fdecl.getLineNum() + ")";
				}

				compilerMessages.add(this, "Duplicate local name '" + varName + "'" + other, MessageType.ERROR);
			} else {
				// Calculate implicit data type
				if (implicit && type == null) type = vi.getExpression().returnType(scope);

				if (type != null && type.isVoid()) {
					compilerMessages.add(this, "Cannot declare variable '" + varName + "' type 'void'", MessageType.ERROR);
					type = null;
				}

				// Add variable to scope
				if ((varName != null) && (type != null)) scope.add(new ScopeSymbol(varName, type));
			}
		}
	}
}
