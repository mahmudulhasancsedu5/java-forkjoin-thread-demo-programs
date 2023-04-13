package myforkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinPoolFib extends RecursiveTask<Integer> {

	private static int threshold = 10;
	private int number;

	public ForkJoinPoolFib(int num) {
//		System.out.println("N: " + num);
//		System.out.println(" " + Thread.currentThread().getName());
		this.number = num;
	}

	public static int findFibNumber(int n, int nprocess) {
		ForkJoinPool pool = new ForkJoinPool(nprocess);
		ForkJoinPoolFib fib = new ForkJoinPoolFib(n);
		pool.execute(fib);
		int num = fib.join();
		System.out.println("Fib of " + n + ": " + num);
		return num;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final String usage = "Usage: java Fib <threads> <numb> \n For example, try java Fib 2 15";
		

		int procs = 4; // for test
		int num = 20; // for test

		try {
			procs = Integer.parseInt(args[0]);
			num = Integer.parseInt(args[1]);
			if (args.length > 2)
				threshold = Integer.parseInt(args[2]);

		} catch (Exception e) {
			System.out.println("Error: " + e);
			System.out.println("Remove return for program execution from IDE");
			System.out.println("Usage: java Fib <threads> <number> [<sequntialThreshold>]");
			
			// REMOVE RETURN FOR PROGRAM EXECUTION FROM IDE
			return; 
		}
		
		long startTime = System.currentTimeMillis();
		System.out.println("Processor:  " + Runtime.getRuntime().availableProcessors());
		findFibNumber(num, procs);
		
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("startTime " + startTime + "  endTime: " + endTime + " elapsedTime: " + elapsedTime + " (ms)");

		// Get the Java runtime
		Runtime runtime = Runtime.getRuntime();
		// Run the garbage collector
		runtime.gc();
		// Calculate the used memory
		long total_memory = runtime.totalMemory();
		long free_memory = runtime.freeMemory();
		long used_memory = total_memory - free_memory;
		System.out.println("total: " + total_memory + " free: " + free_memory + " used: " + used_memory + " (B)");
		System.out.println("total: " + total_memory / 1024 + " free: " + free_memory / 1024 + " used: "
				+ used_memory / 1024 + " (KB)");

	}

	@Override
	protected Integer compute() {
		// TODO Auto-generated method stub
		int num = 0;

		if (number > threshold) {
			try {
				ForkJoinPoolFib fibA = new ForkJoinPoolFib(number - 1);
				ForkJoinPoolFib fibB = new ForkJoinPoolFib(number - 2);
				fibA.fork();
				fibB.fork();
				num = fibA.join() + fibB.join();

			} catch (Exception e) {
				System.out.println("Error: " + e);
			}
		} else {
			return fib(number);
		}

		return num;
	}

	private int fib(int n) {
		if (n <= 1)
			return n;
		return fib(n - 1) + fib(n - 2);
	}

}
