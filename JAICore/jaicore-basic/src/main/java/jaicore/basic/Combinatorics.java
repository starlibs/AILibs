package jaicore.basic;

public class Combinatorics {

	private Combinatorics() {
		// prevent instantiation of this util class
	}

	/**
	 * Returns a complete truth table for n variables. Meaning, a matrix of all possible true false combinations of n variables is generated.
	 *
	 * @param n The number of variables for the truth table.
	 * @return The truth table for the specified n variables.
	 */
	public static boolean[][] getTruthTable(final int n) {
		int rowCount = (int) Math.pow(2, n);
		boolean[][] table = new boolean[rowCount][n];

		for (int i = 0; i < rowCount; i++) {
			for (int j = n - 1; j >= 0; j--) {
				table[i][j] = (i / (int) Math.pow(2, j)) % 2 == 1;
			}
		}
		return table;
	}
}
