package com.example;

import org.mozilla.javascript.*;
import java.io.*;

public class Test1 {
	int age = 100;
	public static String flag = "[user]";

	public static void test1() {
		System.out.println("public static void test1");
	}

	public static void main(String... args) throws Exception {
		if (args.length < 1) {
			System.err.println("not enough argument(s) for main!");
			System.err.println("args: jsSourceName [jsFunction [args]]");
			System.exit(1);
		}

		Context ctx = Context.enter();
		Scriptable scope = ctx.initStandardObjects();
		File patchf = new File(args[0]);
		byte[] patch_array = new byte[(int) patchf.length()];
		InputStream is = new FileInputStream(patchf);
		is.read(patch_array);
		is.close();

		ctx.evaluateString(scope, new String(patch_array), "MySource", 1, null);
		// scope中添加一个函数, 不会覆盖原有的函数
		ctx.evaluateString(scope, "function calc(st){return st;}", "MySource2", 
				1, null);

		Thread.sleep(2000);
		callJsFunction(ctx, scope, args);
	}

	private static void callJsFunction(Context context, Scriptable scope,
			String... args) {
		Object obj = scope.get(args[1], scope);
		if (obj instanceof Function) {
			Object[] as = new Object[args.length - 2];
			System.arraycopy(args, 2, as, 0, as.length);
			Object result = ((Function) obj).call(context, scope, (Function) obj, as);
			System.out.println("return " + result);
		} else {
			System.out.println(obj);
		}
	}
}