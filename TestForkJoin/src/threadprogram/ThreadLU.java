package threadprogram;


public class ThreadLU {

	// granularity is hard-wired as compile-time constant here
	static final int BLOCK_SIZE = 16;

	static final boolean CHECK = true; // set true to check answer

	public static void main(String[] args) {

		final String usage = "Usage: java ThreadLU <threads> <matrix size (must be a power of two)> [runs] \n For example, try java LU 2 512";
		long startTime = System.currentTimeMillis();

		try {
			int procs = 4; // TEST
			int n = 512; // TEST
			int runs = 1;
			try {
				procs = Integer.parseInt(args[0]);
				n = Integer.parseInt(args[1]);
				if (args.length > 2)
					runs = Integer.parseInt(args[2]);

			} catch (Exception e) {
				System.out.println("Error: " + e);
				System.out.println(usage);
				System.out.println("Remove return for program execution from IDE");
				return;
			}
			
			System.out.println("Input: " + procs + " " + n);

			if (((n & (n - 1)) != 0)) {
				System.out.println(usage);
				return;
			}

			for (int run = 0; run < runs; ++run) {

				double[][] m = new double[n][n];
				randomInit(m, n);

				double[][] copy = null;
				if (CHECK) {
					copy = new double[n][n];
					for (int i = 0; i < n; ++i) {
						for (int j = 0; j < n; ++j) {
							copy[i][j] = m[i][j];
						}
					}
				}
				
				System.out.println(procs + " " + n);

				Block M = new Block(m, 0, 0);

				LowerUpper lu = new LowerUpper(n, M);
				lu.start();
				lu.join();

				if (CHECK)
					check(m, copy, n);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("End");
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

	static void randomInit(double[][] M, int n) {

		java.util.Random rng = new java.util.Random();

		for (int i = 0; i < n; ++i)
			for (int j = 0; j < n; ++j)
				M[i][j] = rng.nextDouble();

		// for compatibility with hood demo, force larger diagonals
		for (int k = 0; k < n; ++k)
			M[k][k] *= 10.0;
	}

	static void check(double[][] LU, double[][] M, int n) {

		double maxDiff = 0.0; // track max difference

		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < n; ++j) {
				double v = 0.0;
				int k;
				for (k = 0; k < i && k <= j; k++)
					v += LU[i][k] * LU[k][j];
				if (k == i && k <= j)
					v += LU[k][j];
				double diff = M[i][j] - v;
				if (diff < 0)
					diff = -diff;
				if (diff > 0.001) {
					System.out.println("large diff at[" + i + "," + j + "]: " + M[i][j] + " vs " + v);
				}
				if (diff > maxDiff)
					maxDiff = diff;
			}
		}

		System.out.println("Max difference = " + maxDiff);
	}

	// Blocks record underlying matrix, and offsets into current block
	static class Block {
		final double[][] m;
		final int loRow;
		final int loCol;

		Block(double[][] mat, int lr, int lc) {
			m = mat;
			loRow = lr;
			loCol = lc;
		}
	}

	static class Schur extends Thread {
		final int size;
		final Block V;
		final Block W;
		final Block M;

		Schur(int size, Block V, Block W, Block M) {
			this.size = size;
			this.V = V;
			this.W = W;
			this.M = M;
		}

		void schur() { // base case
			for (int j = 0; j < BLOCK_SIZE; ++j) {
				for (int i = 0; i < BLOCK_SIZE; ++i) {
					double s = M.m[i + M.loRow][j + M.loCol];
					for (int k = 0; k < BLOCK_SIZE; ++k) {
						s -= V.m[i + V.loRow][k + V.loCol] * W.m[k + W.loRow][j + W.loCol];
					}
					M.m[i + M.loRow][j + M.loCol] = s;
				}
			}
		}

		public void run() {
			if (size == BLOCK_SIZE) {
				schur();
			} else {
				int h = size / 2;

				Block M00 = new Block(M.m, M.loRow, M.loCol);
				Block M01 = new Block(M.m, M.loRow, M.loCol + h);
				Block M10 = new Block(M.m, M.loRow + h, M.loCol);
				Block M11 = new Block(M.m, M.loRow + h, M.loCol + h);

				Block V00 = new Block(V.m, V.loRow, V.loCol);
				Block V01 = new Block(V.m, V.loRow, V.loCol + h);
				Block V10 = new Block(V.m, V.loRow + h, V.loCol);
				Block V11 = new Block(V.m, V.loRow + h, V.loCol + h);

				Block W00 = new Block(W.m, W.loRow, W.loCol);
				Block W01 = new Block(W.m, W.loRow, W.loCol + h);
				Block W10 = new Block(W.m, W.loRow + h, W.loCol);
				Block W11 = new Block(W.m, W.loRow + h, W.loCol + h);
				
				
				Thread[] seq1 = { new Schur(h, V00, W00, M00), new Schur(h, V01, W10, M00) };
				Thread[] seq2 = { new Schur(h, V00, W01, M01), new Schur(h, V01, W11, M01) };
				Thread[] seq3 = { new Schur(h, V10, W00, M10), new Schur(h, V11, W10, M10) };
				Thread[] seq4 = { new Schur(h, V10, W01, M11), new Schur(h, V11, W11, M11) };

				for (int i = 0; i < 2; i++) {
					seq1[i].start();
					seq2[i].start();
					seq3[i].start();
					seq4[i].start();

					try {
						seq4[i].join();
						seq3[i].join();
						seq2[i].join();
						seq1[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		}
	}

	static class Lower extends Thread {

		final int size;
		final Block L;
		final Block M;

		Lower(int size, Block L, Block M) {
			this.size = size;
			this.L = L;
			this.M = M;
		}

		void lower() { // base case
			for (int i = 1; i < BLOCK_SIZE; ++i) {
				for (int k = 0; k < i; ++k) {
					double a = L.m[i + L.loRow][k + L.loCol];
					double[] x = M.m[k + M.loRow];
					double[] y = M.m[i + M.loRow];
					int n = BLOCK_SIZE;
					for (int p = n - 1; p >= 0; --p) {
						y[p + M.loCol] -= a * x[p + M.loCol];
					}
				}
			}
		}

		public void run() {
			if (size == BLOCK_SIZE) {
				lower();
			} else {
				int h = size / 2;

				Block M00 = new Block(M.m, M.loRow, M.loCol);
				Block M01 = new Block(M.m, M.loRow, M.loCol + h);
				Block M10 = new Block(M.m, M.loRow + h, M.loCol);
				Block M11 = new Block(M.m, M.loRow + h, M.loCol + h);

				Block L00 = new Block(L.m, L.loRow, L.loCol);
				Block L01 = new Block(L.m, L.loRow, L.loCol + h);
				Block L10 = new Block(L.m, L.loRow + h, L.loCol);
				Block L11 = new Block(L.m, L.loRow + h, L.loCol + h);
				
				Thread[] seq1 = { new Lower(h, L00, M00), new Schur(h, L10, M00, M10), new Lower(h, L11, M10) };
				Thread[] seq2 = { new Lower(h, L00, M01), new Schur(h, L10, M01, M11), new Lower(h, L11, M11) };

				for (int i = 0; i < 3; i++) {
					seq1[i].start();
					seq2[i].start();

					try {
						seq2[i].join();
						seq1[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	static class Upper extends Thread {

		final int size;
		final Block U;
		final Block M;

		Upper(int size, Block U, Block M) {
			this.size = size;
			this.U = U;
			this.M = M;
		}

		void upper() { // base case
			for (int i = 0; i < BLOCK_SIZE; ++i) {
				for (int k = 0; k < BLOCK_SIZE; ++k) {
					double a = M.m[i + M.loRow][k + M.loCol] / U.m[k + U.loRow][k + U.loCol];
					M.m[i + M.loRow][k + M.loCol] = a;
					double[] x = U.m[k + U.loRow];
					double[] y = M.m[i + M.loRow];
					int n = BLOCK_SIZE - k - 1;
					for (int p = n - 1; p >= 0; --p) {
						y[p + k + 1 + M.loCol] -= a * x[p + k + 1 + U.loCol];
					}
				}
			}
		}

		public void run() {
			if (size == BLOCK_SIZE) {
				upper();
			} else {
				int h = size / 2;

				Block M00 = new Block(M.m, M.loRow, M.loCol);
				Block M01 = new Block(M.m, M.loRow, M.loCol + h);
				Block M10 = new Block(M.m, M.loRow + h, M.loCol);
				Block M11 = new Block(M.m, M.loRow + h, M.loCol + h);

				Block U00 = new Block(U.m, U.loRow, U.loCol);
				Block U01 = new Block(U.m, U.loRow, U.loCol + h);
				Block U10 = new Block(U.m, U.loRow + h, U.loCol);
				Block U11 = new Block(U.m, U.loRow + h, U.loCol + h);
				
				Thread[] seq1 = { new Upper(h, U00, M00), new Schur(h, M00, U01, M01), new Upper(h, U11, M01) };
				Thread[] seq2 = { new Upper(h, U00, M10), new Schur(h, M10, U01, M11), new Upper(h, U11, M11) };

				for (int i = 0; i < 3; i++) {
					seq1[i].start();
					seq2[i].start();

					try {
						seq2[i].join();
						seq1[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	static class LowerUpper extends Thread {

		final int size;
		final Block M;

		LowerUpper(int size, Block M) {
			this.size = size;
			this.M = M;
		}

		void lu() { // base case
			for (int k = 0; k < BLOCK_SIZE; ++k) {
				for (int i = k + 1; i < BLOCK_SIZE; ++i) {
					double b = M.m[k + M.loRow][k + M.loCol];
					double a = M.m[i + M.loRow][k + M.loCol] / b;
					M.m[i + M.loRow][k + M.loCol] = a;
					double[] x = M.m[k + M.loRow];
					double[] y = M.m[i + M.loRow];
					int n = BLOCK_SIZE - k - 1;
					for (int p = n - 1; p >= 0; --p) {
						y[k + 1 + p + M.loCol] -= a * x[k + 1 + p + M.loCol];
					}
				}
			}
		}

		public void run() {
			if (size == BLOCK_SIZE) {
				lu();
			} else {
				int h = size / 2;

				Block M00 = new Block(M.m, M.loRow, M.loCol);
				Block M01 = new Block(M.m, M.loRow, M.loCol + h);
				Block M10 = new Block(M.m, M.loRow + h, M.loCol);
				Block M11 = new Block(M.m, M.loRow + h, M.loCol + h);
				
				Thread lu = new LowerUpper(h, M00);
				lu.start();
				try {
					lu.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				Thread low01 = new Lower(h, M00, M01);
				Thread up01 = new Upper(h, M00, M10);
				
				low01.start();
				up01.start();
				
				try {
					low01.join();
					up01.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				Thread sc = new Schur(h, M10, M01, M11);
				sc.start();
				try {
					sc.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Thread luEnd = new LowerUpper(h, M11);
				luEnd.start();
				try {
					luEnd.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
}
