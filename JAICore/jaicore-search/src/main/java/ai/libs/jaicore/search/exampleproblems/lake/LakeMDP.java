package ai.libs.jaicore.search.exampleproblems.lake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.libs.jaicore.search.probleminputs.AMDP;

public class LakeMDP extends AMDP<LakeState, ELakeActions, Double> {

	private static final Collection<ELakeActions> POSSIBLE_ACTIONS = Arrays.asList(ELakeActions.values());

	private final LakeLayout layout;
	private final int goalRow;
	private final int goalCol;

	public LakeMDP(final LakeLayout layout, final int startRow, final int startCol, final int goalRow, final int goalCol) {
		super(new LakeState(layout, startRow, startCol));
		this.layout = layout;
		this.goalRow = goalRow;
		this.goalCol = goalCol;
	}

	@Override
	public Collection<ELakeActions> getApplicableActions(final LakeState state) {
		if (state.isInPit() || (state.getRow() == this.goalRow && state.getCol() == this.goalCol)) {
			return Arrays.asList();
		}
		return POSSIBLE_ACTIONS;
	}

	@Override
	public Map<LakeState, Double> getProb(final LakeState state, final ELakeActions action) {
		Map<LakeState, Double> dist = new HashMap<>();
		List<LakeState> possibleOutcomes = new ArrayList<>(3);
		switch (action) {
		case UP:
			possibleOutcomes.add(this.left(state));
			possibleOutcomes.add(this.up(state));
			possibleOutcomes.add(this.right(state));
			break;
		case RIGHT:
			possibleOutcomes.add(this.up(state));
			possibleOutcomes.add(this.right(state));
			possibleOutcomes.add(this.down(state));
			break;
		case DOWN:
			possibleOutcomes.add(this.left(state));
			possibleOutcomes.add(this.down(state));
			possibleOutcomes.add(this.right(state));
			break;
		case LEFT:
			possibleOutcomes.add(this.left(state));
			possibleOutcomes.add(this.up(state));
			possibleOutcomes.add(this.down(state));
			break;
		default:
			throw new IllegalArgumentException("Unknown action " + action);
		}

		/* compute frequencies for the different outcomes */
		for (LakeState succ : possibleOutcomes) {
			dist.put(succ, dist.computeIfAbsent(succ, s -> 0.0) + 1.0 / 3);
		}
		return dist;
	}

	public LakeState up(final LakeState s) {
		return new LakeState(s.getLayout(), Math.max(0, s.getRow() - 1), s.getCol());
	}

	public LakeState down(final LakeState s) {
		return new LakeState(s.getLayout(), Math.min(this.layout.getRows() - 1, s.getRow() + 1), s.getCol());
	}

	public LakeState left(final LakeState s) {
		return new LakeState(s.getLayout(), s.getRow(), Math.max(0, s.getCol() - 1));
	}

	public LakeState right(final LakeState s) {
		return new LakeState(s.getLayout(), s.getRow(), Math.min(this.layout.getCols() - 1, s.getCol() + 1));
	}

	@Override
	public Double getScore(final LakeState state, final ELakeActions action, final LakeState successor) {
		return successor.isInPit() ? 100000.0 : 1.0;
	}
}
