package myforkjoin;

import java.util.concurrent.RecursiveTask;

public class Fib extends RecursiveTask<Integer> {
	private static int threshold = 11;
	private int number;
	
	public Fib(int number) {
		this.number = number;
	}
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		int n = 30;
//		Fib fib = new Fib(n);
//		fib.fork();
//		int num = fib.join();
//		System.out.println("Fib of " + n + ": " + num);
//
//	}

	@Override
	protected Integer compute() {
		// TODO Auto-generated method stub
		int num = 0;
		
		if(number > threshold) {
			try {
				
				Fib fibA = new Fib(number - 1);
				Fib fibB = new Fib(number - 2);
				fibA.fork();
				fibB.fork();
				num = fibA.join() + fibB.join();
				
			} catch(Exception e) {
				System.out.println("Error: " + e);
			}
		} else {
			return fib(number);
		}
		
		return num;
	}
	
	private int fib(int n) {
		if(n <= 1) return n;
		return fib(n-1) + fib(n-2);
	}

}


/*

class CustomRecursiveAction extends RecursiveAction {

    private String workload = "";
    private static final int THRESHOLD = 4;

    public CustomRecursiveAction(String workload) {
        this.workload = workload;
    }

    @Override
    protected void compute() {
        if (workload.length() > THRESHOLD) {
            invokeAll(createSubtasks());
        } else {
            processing(workload);
        }
    }

    private List<CustomRecursiveAction> createSubtasks() {
        List<CustomRecursiveAction> subTasks = new ArrayList<>();

        String partOne = workload.substring(0, workload.length() / 2);
        String partTwo = workload.substring(workload.length() / 2, workload.length());

        subTasks.add(new CustomRecursiveAction(partOne));
        subTasks.add(new CustomRecursiveAction(partTwo));

        return subTasks;
    }

    private void processing(String work) {
        String result = work.toUpperCase();
        System.out.println("This result - (" + result + ") - was processed by "
                + Thread.currentThread().getName());
    }
}

class CustomRecursiveTask extends RecursiveTask<Integer> {
    private int[] arr;
    private static final int THRESHOLD = 20;

    public CustomRecursiveTask(int[] arr) {
        this.arr = arr;
    }

    @Override
    protected Integer compute() {
        int sum = 0;
        if (arr.length > THRESHOLD) {
            CustomRecursiveTask customRecursiveTaskA = new CustomRecursiveTask(
                    Arrays.copyOfRange(arr, 0, arr.length / 2));
            CustomRecursiveTask customRecursiveTaskB = new CustomRecursiveTask(
                    Arrays.copyOfRange(arr, arr.length / 2, arr.length));
            customRecursiveTaskA.fork();
            customRecursiveTaskB.fork();
            sum = customRecursiveTaskA.join() + customRecursiveTaskB.join();
            return sum;
        } else {
            return processing(arr);
        }
    }

    private Integer processing(int[] arr) {
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        System.out.println("This result - (" + sum + ") - was processed by "
                + Thread.currentThread().getName());
        return sum;
    }
}

*/
