package myforkjoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class NQueen extends RecursiveAction {

	static int boardSize = 15; // TEST

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();

		try {
			int procs = 2; // TEST
			try {
				procs = Integer.parseInt(args[0]);
				boardSize = Integer.parseInt(args[1]);
			} catch (Exception e) {
				
				System.out.println("Error: " + e);;
				System.out.println("Usage: java NQueen <threads> <boardSize>");
				System.out.println("Remove return for program execution from IDE");
				return; // REMOVE
			}

			if (boardSize <= 3) {
				System.out.println("There is no solution for board size <= 3");
				return;
			}

			ForkJoinPool pool = new ForkJoinPool(procs);
			NQueen boardGame = new NQueen(new int[0]);
			pool.execute(boardGame);
			boardGame.join();

			int[] board = result.await();

			System.out.print("Result:");

			for (int i = 0; i < board.length; ++i) {
				System.out.print(" " + board[i]);
			}
			System.out.println();

		} catch (InterruptedException ex) {
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

	/**
	 * Global variable holding the result of search. FJTasks check this to see if it
	 * is nonnull, if so, returning early because a result has been found. In a more
	 * serious program, we might use a fancier scheme to reduce read/write pressure
	 * on this variable.
	 **/

	static final class Result {
		private int[] board = null;

		synchronized int[] get() {
			return board;
		}

		synchronized void set(int[] b) {
			if (board == null) {
				board = b;
				notifyAll();
			}
		}

		synchronized int[] await() throws InterruptedException {
			while (board == null) {
				wait();
			}
			return board;
		}
	}

	static final Result result = new Result();

	// Boards are represented as arrays where each cell
	// holds the column number of the queen in that row

	final int[] sofar;

	NQueen(int[] a) {
		this.sofar = a;
	}

	@Override
	protected void compute() {
		if (result.get() == null) { // check if already solved
			int row = sofar.length;

			if (row >= boardSize) // done
				result.set(sofar);
			else {
				for (int q = 0; q < boardSize; ++q) {

					// Check if can place queen in column q of next row
					boolean attacked = false;
					for (int i = 0; i < row; i++) {
						int p = sofar[i];
						if (q == p || q == p - (row - i) || q == p + (row - i)) {
							attacked = true;
							break;
						}
					}

					// Fork to explore moves from new configuration
					if (!attacked) {
						int[] next = new int[row + 1];
						for (int k = 0; k < row; ++k)
							next[k] = sofar[k];
						next[row] = q;
						new NQueen(next).fork();
					}
				}
			}
		}
	}

}