package ai.libs.jaicore.testproblems.samegame;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import ai.libs.jaicore.problems.samegame.SameGameState;

public class SameGameTest {

	@Test
	public void test() {
		String board = "\n" +
				"250000500000000\n" +
				"520003504000000\n" +
				"130405505000000\n" +
				"321255555000000\n" +
				"145355523510000";
		SameGameState state = new SameGameState(board);
		assertEquals(22, state.getBlocksOfPieces().size());
	}

}
