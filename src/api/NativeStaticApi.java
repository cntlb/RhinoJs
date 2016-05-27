package api;

import org.mozilla.javascript.Scriptable;
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
	
	/**
	 * Exception in thread "main" org.mozilla.javascript.EvaluatorException: Cannot load class "api.NativeStaticApi$StaticInner" which has no zero-parameter constructor.
		at org.mozilla.javascript.DefaultErrorReporter.runtimeError(DefaultErrorReporter.java:77)
		at org.mozilla.javascript.Context.reportRuntimeError(Context.java:954)
		at org.mozilla.javascript.Context.reportRuntimeError(Context.java:1010)
		at org.mozilla.javascript.Context.reportRuntimeError1(Context.java:973)
		at org.mozilla.javascript.ScriptableObject.buildClassCtor(ScriptableObject.java:1335)
		at org.mozilla.javascript.ScriptableObject.defineClass(ScriptableObject.java:1279)
		at org.mozilla.javascript.ScriptableObject.defineClass(ScriptableObject.java:1212)
		at api.TestStatic.main(TestStatic.java:20)
	 */
	public static class StaticInner extends ScriptableObject {

		private static final long serialVersionUID = 1L;
		
/*
		public StaticInner(Scriptable scope, Scriptable prototype) {
			super(scope, prototype);
			// TODO Auto-generated constructor stub
		}
*/
		@Override
		public String getClassName() {
			return "StaticInner";
		}
		
		@JSStaticFunction
		public static void call(){
			System.out.println("StaticInner.call() in api.NativeUserApi$StaticInner");
		}
	}
}
