package ai.libs.jaicore.search.exampleproblems.lake;

public class JasminLakeMDP extends LakeMDP {
	private static boolean[][] pits = new boolean[][] {
		{false, false, false, false},
		{false, true, false, true},
		{false, false, false, true},
		{true, false, false, false}
	};

	public JasminLakeMDP() {
		super(new LakeLayout(4, 4, pits), 0, 0, 3, 3);
	}
}
