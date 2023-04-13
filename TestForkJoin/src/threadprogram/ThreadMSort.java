package threadprogram;

import java.util.Random;

public class ThreadMSort {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int procs = 2;
		int num = 10;
		
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
//			System.out.println(arr[i]);
		}
		
		System.out.println(arr.length);
		MergeSort sort = new MergeSort(arr, 0, arr.length-1);
		sort.start();
		
		try {
			sort.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		for(int i = 0; i < 10; i++) {
			System.out.println(arr[i]);
		}

	}

}

class MergeSort extends Thread {
	private int[] arr;
	private int lo;
	private int hi;
	
	public MergeSort(int[] arr, int lo, int hi) {
		this.arr = arr;
		this.lo = lo;
		this.hi = hi;
	}
	
	public void run() {
		if(this.lo == this.hi) {
			return;
		}
		
		int mid = this.lo + (this.hi - this.lo) / 2;
		
		MergeSort sortLeft = new MergeSort(arr, lo, mid);
		MergeSort sortRight = new MergeSort(arr, mid+1, hi);
		sortLeft.start();
		sortRight.start();
		
		try {
			sortLeft.join();
			sortRight.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Merge merge = new Merge(arr, lo, mid, hi);
		merge.start();
		try {
			merge.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

class Merge extends Thread{
	private int[] arr;
	private int lo;
	private int hi;
	private int mid;
	
	public Merge(int[] arr, int lo, int mid, int hi) {
		this.arr = arr;
		this.lo = lo;
		this.hi = hi;
		this.mid = mid;
	}
	
	public void run() {
		int len = hi - lo + 1;
		int[] temp = new int[len];
		
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
