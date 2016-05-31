
#Js插件
1. 文件位置 `'com\sj4399\mcpetool\mcpesdk\mcpelauncher\ScriptManager.java'`

1. `CallbackName`注解 

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface CallbackName {
    String[] args() default {};
    String name();
}
```

3. 注解反射(ScriptManager)
```java
private static void appendCallbacks(StringBuilder builder) {
    Method[] arr$ = ScriptManager.class.getMethods();
    int len$ = arr$.length;
    for (int i$ = 0; i$ < len$; i$ += 1) {
        CallbackName name = (CallbackName) arr$[i$].getAnnotation(CallbackName.class);
        if (name != null) {
            builder.append("function ").append(name.name()).append("(")
                    .append(Utils.joinArray(name.args(), ", ")).append(")\n");
        }
    }
    builder.append("\n");
}
```

#Mozilla Rhino

##概要
Rhino 是一种使用 Java 语言编写的 JavaScript 的开源实现，原先由Mozilla开发，现在被集成进入JDK 6.0。与其他很多语言一样，Rhino 是一种动态类型的、基于对象的脚本语言，它可以简单地访问各种 Java 类库。Rhino 从 JavaScript 中借用了很多语法，让程序员可以快速编写功能强大的程序。

##Rhino基本用法
下载: https://developer.mozilla.org/zh-CN/docs/Mozilla/Projects/Rhino

推荐官方的例子https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino/Scripting_Java

**注意事项:**  
1. 下载解压后配置js.jar路径到`CLASSPATH`中  
2. 照着官方提供的例子练习下来基本懂得了js和java的交互  

##Rhino继续探究
这部分例子参考自`ScriptManager.java`,最终目的就是应用于mcpe的js  
Rhino目录下的javadoc要经常参考,src也可以放到ide中(Eclipse等)方便查看源码

###加载js文件内容与java交互I
把js的内容全部读取,通过`Context.evaluateString()`加载执行:`public final Object evaluateString(Scriptable scope, String source, String sourceName, int lineno, Object securityDomain)`  
代码:
```java
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
```

1.js(js文件与java源文件同目录,下同)
```JavaScript
com.example.Test1.test1()

function js_m1(){
	java.lang.System.out.println("Hello World from 1.js js_m1.")
}

function js_add(a,b){
	return a+b;
}
```

编译运行
<div id="" class="" style="background: black;color:white;font-family: Fixedsys">
C:\Users\user>javac -d . Test1.java  <br>
<br>
C:\Users\user>java com.example.Test1 1.js js_m1  <br>
public static void test1  <br>
Hello World from 1.js js_m1.  <br>
return org.mozilla.javascript.Undefined@5e265ba4  <br>
<br>
C:\Users\user>java com.example.Test1 1.js js_add 10 20  <br>
public static void test1  <br>
return 1020 <br>
<br>
</div> 

###加载js文件内容与java交互II
每次加载都需要先读取字符串,效率太低了,改进

TestJs.java
```java
package com.example;
import org.mozilla.javascript.*;

import java.io.*;
public class TestJs{
	
	public static void main(String[]args) throws Exception{
		
		if(args.length < 1){
			System.err.println("not enough argument(s) for main!");
			System.err.println("args: jsSourceName [jsFunction [args]]");
			System.exit(1);
		}
		Context cx = Context.enter();
		Reader in = new FileReader(args[0]);
		Script script = cx.compileReader(in, args[0], 0, null);
		Scriptable scope = cx.initStandardObjects();
		script.exec(cx,scope);
		
		if(args.length < 2){
			System.exit(1);
		}
		
		//call method
		callJsFunction(cx, scope, args);
	}
}
```
callJsFunction()和上面一样

2.js
```JavaScript
function test1(){
	com.example.Test1.test1()
}

function js_add(a,b){
	return a+b;
}
```

编译运行
<div id="" class="" style="background: black;color:white;font-family: Fixedsys">
C:\Users\user>javac -d . TestJs.java  <br>
<br>
C:\Users\user>java com.example.TestJs 2.js test1  <br>
public static void test1  <br>
return org.mozilla.javascript.Undefined@ee7d9f1  <br>
<br>
C:\Users\user>java com.example.TestJs 2.js js_add 10 20  <br>
return 1020  <br>
<br>
</div>

##对JS提供API
###静态API
静态函数相对容易调用(无需对象),关键是将java方法注册给JavaScript  
NativeStaticApi.java
```java
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

static.js
Static.sMethod()

TestStatic.java
package api;

import java.io.*;

import org.mozilla.javascript.*;

public class TestStatic {
	public static void main(String[] args) throws Exception {
		Context cx = Context.enter();
		Reader in = new FileReader(args[0]);
		Script script = cx.compileReader(in, args[0], 0, null);
		Scriptable scope = cx.initStandardObjects();
		ScriptableObject.defineClass(scope, NativeStaticApi.class);
		script.exec(cx,scope);
	}
}
```

编译运行
<div id="" class="" style="background: black;color:white;font-family: Fixedsys">
C:\Users\user>javac -d . NativeStaticApi.java<br>
<br>
C:\Users\user>javac -d . TestStatic.java<br>
<br>
C:\Users\user>java api.TestStatic static.js<br>
Static.sMethod() in api.NativeUserApi<br>
<br>
</div>

###普通API
NativeNormalApi.java
```java
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
```

normal.js
```JavaScript
var str = mToString()
mPrint(str)
```

TestNormal.java
```java
package api;

import java.io.*;

import org.mozilla.javascript.*;

public class TestNormal {
	public static void main(String[] args) throws Exception {
		Context cx = Context.enter();
		Reader in = new FileReader(args[0]);
		Script script = cx.compileReader(in, args[0], 0, null);
		Scriptable scope = cx.initStandardObjects(new NativeNormalApi(),false);
		((ScriptableObject)scope).defineFunctionProperties(new String[]{"mToString","mPrint"},
                NativeNormalApi.class, 0);
		script.exec(cx,scope);
	}
}
```

编译运行
<div id="" class="" style="background: black;color:white;font-family: Fixedsys">
C:\Users\user>javac -d . NativeNormalApi.java<br>
<br>
C:\Users\user>javac -d . TestNormal.java<br>
<br>
C:\Users\user>java api.TestNormal normal.js<br>
NativeNormalApi<br>
<br>
</div>

###多文件操作
加载多个可能含有相同函数的js并不冲突的执行其中的函数

用到NativeNormalApi作为api

m1.js
```JavaScript
function show(){
	mPrint("invoke function show() in m1.js")
}
```
m2.js
```JavaScript
function show(){
	mPrint("invoke function show() in m2.js")
}
```
m1.js、m2.js和TestMultiJs.java在同一个目录下,都有一个函数show(),函数体不同,但都使用了mPrint函数(这个不是重点). 

现在要加载这两个js,在控制台输入 “show”,调试调用两个函数

TestMultiJs.java
```java
package api;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class TestMultiJs {
	private static Context sContext = Context.enter();
	private static ArrayList<ScriptState> states = new ArrayList<ScriptState>();
	
	public static class ScriptState {
		public String sourceName;
		public Scriptable scope;
		public Script script;

		protected ScriptState(Script script, Scriptable scope, String name) {
			this.script = script;
			this.scope = scope;
			this.sourceName = name;
		}
	}
	
	public static void main(String[] args) throws Exception {

		checkArgs(args);
		
		addJs("m1.js");
		addJs("m2.js");
		
		callJsMethod(args);
	}

	private static void callJsMethod(String[] args) {
		Object[] params = new Object[args.length - 1];
		System.arraycopy(args, 1, params, 0, params.length);
		for (ScriptState state : states) {
			Object object = state.scope.get(args[0], state.scope);
			if (object instanceof Function) {
				Function func = (Function) object;
				Object result = func.call(sContext, state.scope, func, params);
				System.out.println(result);
			}
		}
	}

	private static void addJs(String sourceName) throws IOException {
		//create scope
		Scriptable scope = sContext.initStandardObjects(new NativeNormalApi(),false);
		//register api
		((ScriptableObject)scope).defineFunctionProperties(new String[]{"mToString","mPrint"},
                NativeNormalApi.class, 0);
		//load js
		Reader in = new FileReader(sourceName);
		Script script = sContext.compileReader(in, sourceName, 0, null);
		
		//execute script
		script.exec(sContext, scope);
		states.add(new ScriptState(script, scope, sourceName));
	}

	private static void checkArgs(String[] args) {
		if(args.length < 1){
			System.err.println("not enough argument(s) for main!");
			System.err.println("args: jsSourceName [jsFunction [args]]");
			System.exit(1);
		}
	}
}
```

编译运行:  
<div id="" class="" style="background: black;color:white;font-family: Fixedsys">
C:\Users\user>javac -d . TestMultiJs.java<br>
<br>
C:\Users\user>java api.TestMultiJs show<br>
invoke function show() in m1.js<br>
org.mozilla.javascript.Undefined@1edf1c96<br>
invoke function show() in m2.js<br>
org.mozilla.javascript.Undefined@1edf1c96<br>
<br>
</div>

带参数的函数类似,略.
###补充
遇到的一个异常:  
org.mozilla.javascript.EvaluatorException:Cannot load class "***.ScriptManager$NativePlayerApi" which has no zero-parameter constructor.  
原因可能和反射之类的有关,具体不详,按照异常信息添加无参构造方法即可,只是奇怪不是有默认的构造方法,难道识别不了?