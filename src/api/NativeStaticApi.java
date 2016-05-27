package api;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

public class NativeStaticApi extends ScriptableObject {
	private static final long serialVersionUID = -5695271009600534723L;

	@Override
	public String getClassName() {
		return "Static";
	}
	
	@JSStaticFunction
	public static void sMethod(){
		System.out.println("Static.sMethod() in api.NativeUserApi");
	}
}
