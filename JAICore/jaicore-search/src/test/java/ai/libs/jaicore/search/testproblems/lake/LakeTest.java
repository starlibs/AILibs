package ai.libs.jaicore.search.testproblems.lake;

import org.junit.Test;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSPolicySearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.exampleproblems.lake.ELakeActions;
import ai.libs.jaicore.search.exampleproblems.lake.LakeLayout;
import ai.libs.jaicore.search.exampleproblems.lake.LakeMDP;
import ai.libs.jaicore.search.exampleproblems.lake.LakeState;

public class LakeTest {

	@Test
	public void test() throws Exception {
		boolean[][] pits = new boolean[][] {
			{false, false, false, false},
			{false, true, false, true},
			{false, false, false, true},
			{true, false, false, false}
		};
		LakeLayout layout = new LakeLayout(4, 4, pits);
		LakeMDP mdp = new LakeMDP(layout, 0, 0, 3, 3);
		//		LakeState succ = mdp.getInitState();
		//		System.out.println(succ.getStringVisualization());
		//		for (int i = 0; i < 10; i++) {
		//			succ = mdp.drawSuccessorState(succ, ELakeActions.DOWN);
		//			System.out.println(succ.getStringVisualization());
		//		}

		MCTSPolicySearch<LakeState, ELakeActions> mcts = new MCTSPolicySearch<>(mdp, null, new UniformRandomPolicy<>(), 2);
		mcts.call();
	}
}
