package org.bds.lang.nativeFunctions;

import org.bds.lang.Parameters;
import org.bds.lang.Type;
import org.bds.run.BdsThread;

/**
 * Native function "rand". Return a random number [0,1] interval
 * 
 * @author pcingola
 */
public class FunctionNativeRand extends FunctionNative {

	public FunctionNativeRand() {
		super();
	}

	@Override
	protected void initFunction() {
		functionName = "rand";
		returnType = Type.REAL;

		String argNames[] = {};
		Type argTypes[] = {};
		parameters = Parameters.get(argTypes, argNames);
		addNativeFunctionToScope();
	}

	@Override
	protected Object runFunctionNative(BdsThread csThread) {
		return csThread.getRandom().nextDouble();
	}
}
