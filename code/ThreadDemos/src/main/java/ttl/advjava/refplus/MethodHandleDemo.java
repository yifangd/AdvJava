package ttl.advjava.refplus;

import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class MethodHandleDemo {

	public static void main(String[] args) throws Throwable {
		String result = doMethodHandle();
		System.out.println("result is " + result);
		result = doReflection();
		System.out.println("result is " + result);

		int rI = doMethodHandleInt();
		System.out.println("rI is " + rI);

		result = doMethodHandleToReflection();
		System.out.println("MH To Reflection is " + result);
		
		outMH();

	}

	/**
	 * Things to note - difference between invokeExact and invoke. invokeExact
	 * will demand an exact match to the MethodType specified, which means even
	 * the return type better have something to receive it if it is not void.
	 * 
	 * invoke is more forgiving. Args and return types are castable.
	 * 
	 * @return
	 * @throws Throwable
	 */
	public static String doMethodHandle() throws Throwable {
		System.out.println("Entering doMethodHandle");

		MethodType mt = MethodType.methodType(String.class, int.class);
		MethodHandle goHandle = MethodHandles.lookup()
				.findVirtual(MethodHandleDemo.class, "go", mt);
		// MethodHandle goHandle =
		// MethodHandles.lookup().findStatic(MethodHandleDemo.class, "count",
		// mt);

		MethodHandleDemo mhd = new MethodHandleDemo();
		// InvokeExact wants things to be exact. Have to catch the return value
		// also.
		String result1 = (String)goHandle.invokeExact(mhd, 10);

		// For invoke, you can have compiler do conversions etc. Or ignore
		// return type
		String result2 = (String) goHandle.invoke(mhd, Integer.valueOf(10));

		return result2;
	}

	public static int doMethodHandleInt() throws Throwable {
		System.out.println("Entering doMethodHandleInt");

		MethodType mt = MethodType.methodType(int.class, int.class);
		MethodHandle goHandle = MethodHandles.lookup()
				.findVirtual(MethodHandleDemo.class, "count", mt);

		MethodHandleDemo mhd = new MethodHandleDemo();
		// int c = (int)goHandle.invokeExact(mhd, 10);
		Integer c = (Integer) goHandle.invoke(mhd, 10);

		return c;
	}

	public static String doReflection() throws Throwable {
		System.out.println("Entering doReflection");

		Class<MethodHandleDemo> cl = MethodHandleDemo.class;
		MethodHandleDemo obj = cl.getDeclaredConstructor()
				.newInstance();

		Method goMethod = cl.getMethod("go", int.class);

		String result = (String) goMethod.invoke(obj, 10);
		return result;
	}

	/**
	 * Here we start with a MethodHandle but then need to check for an annotation
	 * on the method before invoking it.  So we reflectAs from the MethodHandle to
	 * get the corresponding Method.
	 * @return
	 * @throws Exception
	 */
	public static String doMethodHandleToReflection() throws Exception {
		MethodType mt = MethodType.methodType(String.class, int.class);
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		
		MethodHandle goHandle = lookup.findVirtual(MethodHandleDemo.class, "go", mt);

		Method goMethod = lookup.revealDirect(goHandle)
				.reflectAs(Method.class, lookup);

		String result = null;
		if (!goMethod.isAnnotationPresent(Ignore.class)) {
			MethodHandleDemo obj = MethodHandleDemo.class.getDeclaredConstructor()
					.newInstance();
			result = (String) goMethod.invoke(obj, 10);
		}
		System.out.println(result);
		return result;
	}
	
	private static int count;

	public static void outMH() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		
		MethodHandle out = lookup.findStaticGetter(System.class, "out", PrintStream.class);
		
		PrintStream o = (PrintStream)out.invoke();
		o.println("Boo");
		
		MethodHandle countSetter = lookup.findStaticSetter(MethodHandleDemo.class, 
				"count", int.class);
		
		countSetter.invoke(1000);
		
		System.out.println("Count after setting is " + count);
	}

	public String go(int i) {
		return "i is " + i;
	}

	public int count(int s) {
		return s * s;
	}
}
