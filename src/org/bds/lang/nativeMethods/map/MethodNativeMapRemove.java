package org.bds.lang.nativeMethods.map;

import java.util.HashMap;

import org.bds.lang.Parameters;
import org.bds.lang.Type;
import org.bds.lang.TypeMap;
import org.bds.run.BdsThread;

/**
 * Return a list of keys
 * 
 * @author pcingola
 */
public class MethodNativeMapRemove extends MethodNativeMap {

	public MethodNativeMapRemove(Type baseType) {
		super(baseType);
	}

	@Override
	protected void initMethod(Type baseType) {
		functionName = "remove";
		classType = TypeMap.get(baseType);
		returnType = Type.BOOL;

		String argNames[] = { "this", "key" };
		Type argTypes[] = { classType, Type.STRING }; // null: don't check argument (anything can be converted to 'string')
		parameters = Parameters.get(argTypes, argNames);

		addNativeMethodToClassScope();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	protected Object runMethodNative(BdsThread csThread, Object objThis) {
		HashMap map = (HashMap) objThis;
		String key = csThread.getObject("key").toString();
		return map.remove(key);
	}
}
