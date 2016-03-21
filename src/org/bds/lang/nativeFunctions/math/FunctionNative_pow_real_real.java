package org.bds.lang.nativeFunctions.math;

import org.bds.lang.Parameters;
import org.bds.lang.Type;
import org.bds.lang.nativeFunctions.FunctionNative;
import org.bds.run.BdsThread;

public class FunctionNative_pow_real_real extends FunctionNative {
	public FunctionNative_pow_real_real() {
		super();
	}

	@Override
	protected void initFunction() {
		functionName = "pow";
		returnType = Type.REAL;

		String argNames[] = { "a", "b" };
		Type argTypes[] = { Type.REAL, Type.REAL };
		parameters = Parameters.get(argTypes, argNames);
		addNativeFunctionToScope();
	}

	@Override
	protected Object runFunctionNative(BdsThread bdsThread) {
		return (Double) Math.pow(bdsThread.getReal("a"), bdsThread.getReal("b"));
	}
}
