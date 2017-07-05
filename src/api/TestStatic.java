package api;

import java.io.*;
import java.util.Scanner;

import org.mozilla.javascript.*;

public class TestStatic {

	public static void main(String[] args) throws Exception {
		if(args.length < 1){
			System.err.println("not enough argument(s) for main!");
			System.err.println("args: jsSourceName [jsFunction [args]]");
			System.exit(1);
		}
		Context cx = Context.enter();
		Scriptable scope = cx.initStandardObjects();
		ScriptableObject.defineClass(scope, NativeStaticApi.class);
		ScriptableObject.defineClass(scope, NativeStaticApi.StaticInner.class);
		Reader in = new FileReader(args[0]);
		cx.evaluateReader(scope, in, args[0], 1, null);
//		Script script = cx.compileReader(in, args[0], 0, null);
//
//		script.exec(cx,scope);
		
		if(args.length < 2){
			System.exit(1);
		}
		
		//call method
		Scanner scanner = new Scanner(System.in);
		while(true){
			String nextLine = scanner.nextLine();
			Object obj = scope.get(nextLine, scope);
			if (obj instanceof Function) {
				Object[] as = new Object[args.length - 2];
				System.arraycopy(args, 2, as, 0, as.length);
				for (int i = 0; i < as.length; i++) {
					if(as[i] instanceof String){
						String s = (String) as[i];
						try{
							as[i] = Integer.parseInt(s);
						}catch(NumberFormatException e){
							try{
								as[i] = Double.parseDouble(s);
							}catch(NumberFormatException e1){
								System.out.printf("%s", s);
							}
						}
					}
				}
				Object result = ((Function) obj).call(cx, scope, (Function) obj, as);
				System.out.println("return " + result);
			} else {
				System.out.println(obj);
			}
		}
	}
}
