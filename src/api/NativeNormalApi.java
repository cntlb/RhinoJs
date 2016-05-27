package api;

import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.annotations.JSFunction;

public class NativeNormalApi extends ImporterTopLevel {

	private static final long serialVersionUID = 3947292768495435201L;
	
	@Override
	public String getClassName() {
		return "Normal";
	}
	
	@JSFunction
	public String mToString() {
		return "NativeNormalApi";
	}
	
	@JSFunction
	public void mPrint(Object obj){
		System.out.println(obj);
	}
}
