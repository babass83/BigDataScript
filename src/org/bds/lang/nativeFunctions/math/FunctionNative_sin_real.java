package org.bds.lang.nativeFunctions.math;

import org.bds.lang.Parameters;
import org.bds.lang.Type;
import org.bds.lang.nativeFunctions.FunctionNative;
import org.bds.run.BdsThread;

public class FunctionNative_sin_real extends FunctionNative {
	public FunctionNative_sin_real() {
		super();
	}

	@Override
	protected void initFunction() {
		functionName = "sin";
		returnType = Type.REAL;

		String argNames[] = { "a" };
		Type argTypes[] = { Type.REAL };
		parameters = Parameters.get(argTypes, argNames);
		addNativeFunctionToScope();
	}

	@Override
	protected Object runFunctionNative(BdsThread bdsThread) {
		return (Double) Math.sin(bdsThread.getReal("a"));
	}
}
