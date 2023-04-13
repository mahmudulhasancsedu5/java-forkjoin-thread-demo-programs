package threadprogram;

public class ThreadFib extends Thread {
	private static int threshold = 10;
	private int number;
	private int answer;
	
	public ThreadFib(int num) {
//		System.out.println("N: " + num);
//		System.out.println(" " + Thread.currentThread().getName());
		this.number = num;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		long startTime = System.currentTimeMillis();
		
		int procs = 2;
		int num = 15;
		
		try {
			procs = Integer.parseInt(args[0]);
			num = Integer.parseInt(args[1]);
			if(args.length > 2) threshold = Integer.parseInt(args[2]);
			
		} catch(Exception e) {
			System.out.println("Error: " + e);
			System.out.println("Remove return for program execution from IDE");
			System.out.println("Usage: java Fib <threads> <number> [<sequntialThreshold>]");
			
			// REMOVE RETURN FOR PROGRAM EXECUTION FROM IDE
			return; 
		}
		System.out.println("Processor:  " + Runtime.getRuntime().availableProcessors());
		
		ThreadFib fib = new ThreadFib(num);
		fib.start();
		try {
			fib.join();
			
		} catch(Exception e) {
			System.out.println("Error: " + e);
		}
		
		System.out.println("Fib  " + fib.number + "is: " + fib.answer);
		
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
	
	public void run() {
		if(this.number > threshold) {
			ThreadFib fibA = new ThreadFib(this.number - 1);
			ThreadFib fibB = new ThreadFib(this.number - 2);
			
			fibA.start();
			fibB.start();
			
			try {
				fibA.join();
				fibB.join();
				
			} catch(Exception e) {
				System.out.println("Error: " + e);
			}
			
			this.answer = fibA.answer + fibB.answer;
			
		} else {
			
			this.answer = this.fib(number);
			
		}
		
	}
	
	private int fib(int n) {
		if(n <= 1) return n;
		return fib(n-1) + fib(n-2);
	}
	
	
	public int getAnswer() {
		return this.answer;
	}

}

