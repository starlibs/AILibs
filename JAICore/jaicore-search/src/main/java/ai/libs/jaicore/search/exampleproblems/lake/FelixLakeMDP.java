package ai.libs.jaicore.search.exampleproblems.lake;

public class FelixLakeMDP extends LakeMDP {
	public static boolean[][] getPits(final int height) {
		boolean[][] pits = new boolean[height][5];
		for (int i = 2; i < height - 1; i++) {
			pits[i][2] = true;
		}
		return pits;
	}

	public FelixLakeMDP(final int height) {
		super(new LakeLayout(height, 5, getPits(height)), height - 1, 0, height - 1, 4);
	}
}
