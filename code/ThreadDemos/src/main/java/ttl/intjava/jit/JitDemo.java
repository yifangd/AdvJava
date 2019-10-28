package ttl.intjava.jit;

import java.util.concurrent.ThreadLocalRandom;

public class JitDemo {

	static int blackHole;
	
	public static void main(String[] args) {
		int [] nums = createInts(5000);
		for	(int i = 0; i < 100; i++) {
			long start = System.nanoTime();
			
			blackHole = sum(nums);
			
			long end = System.nanoTime();
			System.out.printf("%d\t%d\n", i, end - start);
		}
				
	}
	
	public static int sum(int [] arr) {
		int sum = 0;
		for(int i : arr) {
			sum += i;
		}
		return sum;
	}
	public static int [] createInts(int count) {
		int [] arr = new int[count];
		for(int i = 0; i < arr.length; i++) {
			arr[i] = ThreadLocalRandom.current().nextInt(1000);
		}
		
		return arr;
	}
}
