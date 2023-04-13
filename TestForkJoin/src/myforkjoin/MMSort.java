package myforkjoin;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MMSort {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		try {
			int n = 500000; // TEST
			int p = 2; // TEST

			try {
				p = Integer.parseInt(args[0]);
				n = Integer.parseInt(args[1]);
			} catch (Exception e) {
				System.out.println("Error: " + e);;
				System.out.println("Usage: java MMSort <threads> <array size>");
				System.out.println("Remove return for program execution from IDE");
				return; // REMOVE
			}
			
			System.out.println("<threads>: "+ p + " <array size>: " + n);

			int[] A = new int[n];

			// Fill in array A with random values.
			Random rng = new Random();
			for (int i = 0; i < n; i++)
				A[i] = rng.nextInt();

			int[] workSpace = new int[n];

			ForkJoinPool pool = new ForkJoinPool(p);
			Sorter t = new Sorter(A, 0, workSpace, 0, n);
			pool.execute(t);
			t.join();

//			// Check solution correctness
//			checkSorted(A, n);
//			for(int i = 0; i < Math.min(10, n); i++) {
//				System.out.println(A[i]);
//			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Performance analysis
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out
				.println("startTime " + startTime + "  endTime: " + endTime + " elapsedTime: " + elapsedTime + " (ms)");

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

	static void checkSorted(int[] A, int n) {
		for (int i = 0; i < n - 1; i++) {
			if (A[i] > A[i + 1]) {
				throw new Error("Unsorted at " + i + ": " + A[i] + " / " + A[i + 1]);
			}
		}
	}

	/* Threshold values */

	// Cutoff for when to do sequential versus parallel merges
	static final int MERGE_SIZE = 2048;

	// Cutoff for when to do sequential quicksort versus parallel mergesort
	static final int QUICK_SIZE = 2048;

	// Cutoff for when to use insertion-sort instead of quicksort
	static final int INSERTION_SIZE = 20;

	static class Sorter extends RecursiveAction {
		final int[] A; // Input array.
		final int aLo; // offset into the part of array we deal with
		final int[] W; // workspace for merge
		final int wLo;
		final int n; // Number of elements in (sub)arrays.

		Sorter(int[] A, int aLo, int[] W, int wLo, int n) {
			this.A = A;
			this.aLo = aLo;
			this.W = W;
			this.wLo = wLo;
			this.n = n;
		}

		/** Relay to quicksort within sync method to ensure memory barriers **/
		synchronized void qs() {
			quickSort(aLo, aLo + n - 1);
		}

		/** A standard sequential quicksort **/
		void quickSort(int lo, int hi) {

			// If under threshold, use insertion sort
			if (hi - lo + 1l <= INSERTION_SIZE) {
				for (int i = lo + 1; i <= hi; i++) {
					int t = A[i];
					int j = i - 1;
					while (j >= lo && A[j] > t) {
						A[j + 1] = A[j];
						--j;
					}
					A[j + 1] = t;
				}
				return;
			}

			// Use median-of-three(lo, mid, hi) to pick a partition.
			// Also swap them into relative order while we are at it.

			int mid = (lo + hi) >>> 1;

			if (A[lo] > A[mid]) {
				int t = A[lo];
				A[lo] = A[mid];
				A[mid] = t;
			}
			if (A[mid] > A[hi]) {
				int t = A[mid];
				A[mid] = A[hi];
				A[hi] = t;

				if (A[lo] > A[mid]) {
					t = A[lo];
					A[lo] = A[mid];
					A[mid] = t;
				}

			}

			int left = lo + 1; // start one past lo since already handled lo
			int right = hi - 1; // similarly

			int partition = A[mid];

			for (;;) {

				while (A[right] > partition)
					--right;

				while (left < right && A[left] <= partition)
					++left;

				if (left < right) {
					int t = A[left];
					A[left] = A[right];
					A[right] = t;
					--right;
				} else
					break;

			}

			quickSort(lo, left);
			quickSort(left + 1, hi);

		}

		@Override
		protected void compute() {

			/*
			 * Algorithm:
			 * 
			 * IF array size is small, just use a sequential quicksort
			 * 
			 * Otherwise: Break array in half. For each half, break the half in half (i.e.,
			 * quarters), sort the quarters merge them together Finally, merge together the
			 * two halves.
			 */

			if (n <= QUICK_SIZE) {
				qs();
			} else {

				int q = n / 4;

				invokeAll(new Sorter(A, aLo, W, wLo, q), new Sorter(A, aLo + q, W, wLo + q, q));
				invokeAll(new Merger(A, aLo, q, A, aLo + q, q, W, wLo));
				
				invokeAll(new Sorter(A, aLo + q * 2, W, wLo + q * 2, q),
						new Sorter(A, aLo + q * 3, W, wLo + q * 3, n - q * 3));
				invokeAll(new Merger(A, aLo + q * 2, q, A, aLo + q * 3, n - q * 3, W, wLo + q * 2));

				invokeAll(new Merger(W, wLo, q * 2, W, wLo + q * 2, n - q * 2, A, aLo));

			}
		}

	}

	static class Merger extends RecursiveAction {

		final int[] A; // First sorted array.
		final int aLo; // first index of A
		final int aSize; // number of elements

		final int[] B; // Second sorted array.
		final int bLo;
		final int bSize;

		final int[] out; // Output array.
		final int outLo;

		Merger(int[] A, int aLo, int aSize, int[] B, int bLo, int bSize, int[] out, int outLo) {

			this.out = out;
			this.outLo = outLo;

			// A must be largest of the two for split. Might as well swap now.
			if (aSize >= bSize) {
				this.A = A;
				this.aLo = aLo;
				this.aSize = aSize;
				this.B = B;
				this.bLo = bLo;
				this.bSize = bSize;
			} else {
				this.A = B;
				this.aLo = bLo;
				this.aSize = bSize;
				this.B = A;
				this.bLo = aLo;
				this.bSize = aSize;
			}
		}

		/** find greatest point in B less than value. return 0-based offset **/
		synchronized int findSplit(int value) {
			int low = 0;
			int high = bSize;
			while (low < high) {
				int middle = low + ((high - low) >>> 1);
				if (value <= B[bLo + middle])
					high = middle;
				else
					low = middle + 1;
			}
			return high;
		}

		/** A standard sequential merge **/
		synchronized void merge() {
			int a = aLo;
			int aFence = aLo + aSize;
			int b = bLo;
			int bFence = bLo + bSize;
			int k = outLo;

			while (a < aFence && b < bFence) {
				if (A[a] < B[b])
					out[k++] = A[a++];
				else
					out[k++] = B[b++];
			}

			while (a < aFence)
				out[k++] = A[a++];
			while (b < bFence)
				out[k++] = B[b++];
		}

		@Override
		protected void compute() {

			/*
			 * Algorithm: If the arrays are small, then just sequentially merge.
			 * 
			 * Otherwise: Split A in half. Find the greatest point in B less than the
			 * beginning of the second half of A. In parallel: merge the left half of A with
			 * elements of B up to split point merge the right half of A with elements of B
			 * past split point
			 */

			if (aSize <= MERGE_SIZE) {
				merge();
			} else {

				int aHalf = aSize >>> 1;
				int bSplit = findSplit(A[aLo + aHalf]);

				invokeAll(new Merger(A, aLo, aHalf, B, bLo, bSplit, out, outLo), new Merger(A, aLo + aHalf,
						aSize - aHalf, B, bLo + bSplit, bSize - bSplit, out, outLo + aHalf + bSplit));
			}
		}
	}

}
