package api;

import java.io.*;

import org.mozilla.javascript.*;

public class TestStatic {

	public static void main(String[] args) throws Exception {
		if(args.length < 1){
			System.err.println("not enough argument(s) for main!");
			System.err.println("args: jsSourceName [jsFunction [args]]");
			System.exit(1);
		}
		Context cx = Context.enter();
		Reader in = new FileReader(args[0]);
		Script script = cx.compileReader(in, args[0], 0, null);
		Scriptable scope = cx.initStandardObjects();
		ScriptableObject.defineClass(scope, NativeStaticApi.class);
		ScriptableObject.defineClass(scope, NativeStaticApi.StaticInner.class);
		script.exec(cx,scope);
		
		if(args.length < 2){
			System.exit(1);
		}
		
		//call method
		Object obj = scope.get(args[1], scope);
		if (obj instanceof Function) {
			Object[] as = new Object[args.length - 2];
			System.arraycopy(args, 2, as, 0, as.length);
			Object result = ((Function) obj).call(cx, scope, (Function) obj, as);
			System.out.println("return " + result);
		} else {
			System.out.println(obj);
		}
	}
}
