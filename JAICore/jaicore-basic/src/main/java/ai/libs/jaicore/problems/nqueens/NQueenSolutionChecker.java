package ai.libs.jaicore.problems.nqueens;

import java.util.List;

public class NQueenSolutionChecker {

	private NQueenSolutionChecker() {
		/* avoids instantiation */
	}

	public static boolean isSolution(final List<Integer> solution) {
		int n = solution.size();
		for (int i = 0; i < n; i++) {
			int j = solution.get(i);
			if (attack(solution, i, j)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a cell is attacked by the queens on the board
	 * @param i
	 * 		The row of the cell to be checked.
	 * @param j
	 * 		The collumn of the cell to be checked.
	 * @return
	 * 		<code>true</code> if the cell is attacked, <code>false</code> otherwise.
	 */
	public static boolean attack(final List<Integer> positions, final int i, final int j) {
		int n = positions.size();
		for(int x = 0; x < n; x++) {
			int y = positions.get(x);

			/* ignore the field that is queried as a source of attack */
			if (x == i && y == j) {
				continue;
			}

			/* if the queried field is in the same column as a positioned queen */
			if(j == y) {
				return true;
			}

			int z = Math.abs(i - positions.indexOf(y));
			if(j == y+z || y-z == j) {
				return true;
			}
		}
		return false;
	}
}
