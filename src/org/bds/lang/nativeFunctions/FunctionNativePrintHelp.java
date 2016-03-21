package org.bds.lang.nativeFunctions;

import org.bds.lang.Parameters;
import org.bds.lang.Type;
import org.bds.lang.TypeList;
import org.bds.run.BdsThread;
import org.bds.run.HelpCreator;

/**
 * Native function "sleep"
 *
 * @author pcingola
 */
public class FunctionNativePrintHelp extends FunctionNative {

	public FunctionNativePrintHelp() {
		super();
	}

	@Override
	protected void initFunction() {
		functionName = "printHelp";
		returnType = TypeList.get(Type.BOOL);

		String argNames[] = {};
		Type argTypes[] = {};
		parameters = Parameters.get(argTypes, argNames);
		addNativeFunctionToScope();
	}

	@Override
	protected Object runFunctionNative(BdsThread bdsThread) {
		HelpCreator hc = new HelpCreator(bdsThread.getRoot().getProgramUnit());
		System.out.println(hc);
		return true;
	}

}
