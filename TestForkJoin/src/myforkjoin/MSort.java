package myforkjoin;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MSort {
	private static int threshold = 10;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int procs = 2;
		int num = 100000;
		
		try {
			if(args.length > 0) procs = Integer.parseInt(args[0]);
			if(args.length > 1) num = Integer.parseInt(args[1]);
			
		} catch(Exception e) {
			System.out.println("Error: " + e);
		}
		System.out.println("Processor:  " + Runtime.getRuntime().availableProcessors());
		
		Random random = new Random();
		int[] arr = new int[num];
		for(int i = 0; i < num; i++) {
			arr[i] = random.nextInt();
		}
		
		System.out.println(arr.length);
		
		ForkJoinPool pool = new ForkJoinPool(procs);
		MergeSort sort = new MergeSort(arr, 0, arr.length-1);
		pool.execute(sort);
		sort.join();
		for(int i = 0; i < 10; i++) {
			System.out.println(arr[i]);
		}
		
	}

}

class  MergeSort extends RecursiveAction {
	public static int threshold = 10; 
	private int[] arr;
	private int hi;
	private int lo;
	
	public MergeSort(int[] arr, int lo, int hi) {
		this.arr = arr;
		this.lo = lo;
		this.hi = hi;
	}

	@Override
	protected void compute() {
		// TODO Auto-generated method stub
		if(this.hi == this.lo) {
			return;
		} else {
//			System.out.println(lo + " " + hi);
			int mid = lo + (hi - lo) / 2;
			
			MergeSort sortA = new MergeSort(arr, lo, mid);
			MergeSort sortB = new MergeSort(arr, mid+1, hi);
			sortA.invoke();
			sortB.invoke();
			
			MergeAction merge = new MergeAction(arr, lo, hi, mid);
			merge.invoke();
			
		}
		
	}
	
}

class MergeAction extends RecursiveAction {
	public static int threshold = 10; 
	private int[] arr;
	private int hi;
	private int lo;
	private int mid;
	
	public MergeAction(int[] arr, int lo, int hi, int mid) {
		this.arr = arr;
		this.hi = hi;
		this.lo = lo;
		this.mid = mid;
		
	}
	
	@Override
	protected void compute() {
		// TODO Auto-generated method stub
		
		int len = hi - lo;
		int[] temp = new int[len+1];
		
		int i = lo, j = mid+1;
		for(int k = lo; k <= hi; k++) {
			if(i == mid+1) {
				temp[k - lo] = arr[j++];
				k++;
			} else if(j == hi+1) {
				temp[k - lo] = arr[i++];
			} else {
				if(arr[i] < arr[j]) {
					temp[k - lo] = arr[i++];
				} else {
					temp[k - lo] = arr[j++];
				}
			}
		}
		
		for(int k = lo; k <= hi; k++) {
			arr[k] = temp[k - lo];
		}
		
	}
	
}


