package api;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
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
		
		addJs("src/api/m1.js");
		addJs("src/api/m2.js");
		
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
		((ScriptableObject)scope).defineFunctionProperties(new String[]{"mToString","mPrint"}, NativeNormalApi.class, 0);
		Class<?> class1 = Const.class;
		ScriptableObject.putProperty(scope, class1.getSimpleName(), classConstantsToJSObject(class1));
		//load js
		Reader in = new FileReader(sourceName);
//		Script script = sContext.compileReader(in, sourceName, 0, null);
//		
//		//execute script
//		script.exec(sContext, scope);
		sContext.evaluateReader(scope, in, sourceName, 1, null);
		states.add(new ScriptState(null, scope, sourceName));
	}

	private static void checkArgs(String[] args) {
		if(args.length < 1){
			System.err.println("not enough argument(s) for main!");
			System.err.println("args: jsSourceName [jsFunction [args]]");
			System.exit(1);
		}
	}
	
	public static ScriptableObject classConstantsToJSObject(Class<?> clazz) {
        ScriptableObject obj = new NativeObject();
        Field[] arr$ = clazz.getFields();
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$; i$ += 1) {
            Field field = arr$[i$];
            int fieldModifiers = field.getModifiers();
            if (Modifier.isStatic(fieldModifiers) && Modifier.isPublic(fieldModifiers)
            		&& Modifier.isFinal(fieldModifiers)) {
                try {
                    obj.putConst(field.getName(), obj, field.get(null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }

}
