package jaicore.basic;

public abstract class Combinatorics {

	public static boolean[][] getTruthTable(int n) {
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
