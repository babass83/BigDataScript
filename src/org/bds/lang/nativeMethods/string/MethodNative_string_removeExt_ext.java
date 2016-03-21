package org.bds.lang.nativeMethods.string;

import org.bds.lang.Parameters;
import org.bds.lang.Type;
import org.bds.lang.nativeMethods.MethodNative;
import org.bds.run.BdsThread;

public class MethodNative_string_removeExt_ext extends MethodNative {
	public MethodNative_string_removeExt_ext() {
		super();
	}

	@Override
	protected void initMethod() {
		functionName = "removeExt";
		classType = Type.STRING;
		returnType = Type.STRING;

		String argNames[] = { "this", "ext" };
		Type argTypes[] = { Type.STRING, Type.STRING };
		parameters = Parameters.get(argTypes, argNames);
		addNativeMethodToClassScope();
	}

	@Override
	protected Object runMethodNative(BdsThread bdsThread, Object objThis) {
		String ext = bdsThread.getString("ext");
		String b = objThis.toString();
		if (b.endsWith(ext)) return b.substring(0, b.length() - ext.length());
		return b;
	}
}
