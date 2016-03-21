package org.bds.lang.nativeFunctions;

import org.bds.lang.Parameters;
import org.bds.lang.Type;
import org.bds.run.BdsThread;

/**
 * Native function "rand". Return a random int number
 * 
 * @author pcingola
 */
public class FunctionNativeToIntFromReal extends FunctionNative {

	public FunctionNativeToIntFromReal() {
		super();
	}

	@Override
	protected void initFunction() {
		functionName = "toInt";
		returnType = Type.INT;

		String argNames[] = { "num" };
		Type argTypes[] = { Type.REAL };
		parameters = Parameters.get(argTypes, argNames);
		addNativeFunctionToScope();
	}

	@Override
	protected Object runFunctionNative(BdsThread csThread) {
		double num = csThread.getReal("num");
		return ((long) num);
	}
}
