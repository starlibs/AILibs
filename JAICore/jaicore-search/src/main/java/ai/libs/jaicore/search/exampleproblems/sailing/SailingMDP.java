package ai.libs.jaicore.search.exampleproblems.sailing;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ai.libs.jaicore.search.probleminputs.AMDP;

public class SailingMDP extends AMDP<SailingState, SailingMove, Double> {

	private final int rows;
	private final int cols;
	private final int goalRow;
	private final int goalCol;

	public SailingMDP(final int rows, final int cols, final int initRow, final int initCol, final int goalRow, final int goalCol, final SailingMove initWind) {
		super(new SailingState(initRow, initCol, initWind));
		this.rows = rows;
		this.cols = cols;
		this.goalRow = goalRow;
		this.goalCol = goalCol;
	}

	@Override
	public Collection<SailingMove> getApplicableActions(final SailingState state) {
		if (state.getRow() == this.goalRow && state.getCol() == this.goalCol) {
			return Arrays.asList();
		}
		Set<SailingMove> possibleMoves = Arrays.stream(SailingMove.values()).collect(Collectors.toSet());
		possibleMoves.remove(state.getWind());
		if (state.getRow() == 0) {
			possibleMoves.remove(SailingMove.N);
			possibleMoves.remove(SailingMove.NE);
			possibleMoves.remove(SailingMove.NW);
		}
		if (state.getCol() == 0) {
			possibleMoves.remove(SailingMove.W);
			possibleMoves.remove(SailingMove.NW);
			possibleMoves.remove(SailingMove.SW);
		}
		if (state.getRow() == this.rows - 1) {
			possibleMoves.remove(SailingMove.S);
			possibleMoves.remove(SailingMove.SE);
			possibleMoves.remove(SailingMove.SW);
		}
		if (state.getCol() == this.cols - 1) {
			possibleMoves.remove(SailingMove.E);
			possibleMoves.remove(SailingMove.NE);
			possibleMoves.remove(SailingMove.SE);
		}
		return possibleMoves;
	}

	@Override
	public Map<SailingState, Double> getProb(final SailingState state, final SailingMove action) {
		if (!this.getApplicableActions(state).contains(action)) {
			throw new IllegalArgumentException("Action " + action + " is not applicable in state " + state);
		}
		int newRow = state.getRow();
		int newCol = state.getCol();
		switch (action) {
		case NW:
		case NE:
		case N:
			newRow -= 1;
			break;
		case SW:
		case SE:
		case S:
			newRow += 1;
			break;

		}
		switch (action) {
		case NW:
		case W:
		case SW:
			newCol -= 1;
			break;
		case NE:
		case E:
		case SE:
			newCol += 1;
			break;
		}

		List<SailingMove> windDirections = null;
		switch (state.getWind()) {
		case N:
			windDirections = Arrays.asList(SailingMove.NW, SailingMove.N, SailingMove.NE);
			break;
		case NE:
			windDirections = Arrays.asList(SailingMove.N, SailingMove.NE, SailingMove.E);
			break;
		case E:
			windDirections = Arrays.asList(SailingMove.NE, SailingMove.E, SailingMove.SE);
			break;
		case SE:
			windDirections = Arrays.asList(SailingMove.E, SailingMove.SE, SailingMove.S);
			break;
		case S:
			windDirections = Arrays.asList(SailingMove.SE, SailingMove.S, SailingMove.SW);
			break;
		case SW:
			windDirections = Arrays.asList(SailingMove.S, SailingMove.SW, SailingMove.W);
			break;
		case W:
			windDirections = Arrays.asList(SailingMove.SW, SailingMove.W, SailingMove.NW);
			break;
		case NW:
			windDirections = Arrays.asList(SailingMove.W, SailingMove.NW, SailingMove.N);
			break;
		}

		Map<SailingState, Double> map = new HashMap<>();

		for (SailingMove wind : windDirections) {
			map.put(new SailingState(newRow, newCol, wind), 1.0 / windDirections.size());
		}
		return map;
	}

	@Override
	public Double getScore(final SailingState state, final SailingMove action, final SailingState successor) {
		SailingMove wind = state.getWind();
		switch (wind) {
		case N:
			switch (action) {
			case NW:
			case NE:
				return 4.0;
			case W:
			case E:
				return 3.0;
			case SW:
			case SE:
				return 2.0;
			case S:
				return 1.0;
			default:
				throw new IllegalArgumentException();
			}
		case NE:
			switch (action) {
			case N:
			case E:
				return 4.0;
			case NW:
			case SE:
				return 3.0;
			case W:
			case S:
				return 2.0;
			case SW:
				return 1.0;
			default:
				throw new IllegalArgumentException();
			}
		case E:
			switch (action) {
			case NE:
			case SE:
				return 4.0;
			case N:
			case S:
				return 3.0;
			case NW:
			case SW:
				return 2.0;
			case W:
				return 1.0;
			default:
				throw new IllegalArgumentException();
			}
		case SE:
			switch (action) {
			case E:
			case S:
				return 4.0;
			case NE:
			case SW:
				return 3.0;
			case N:
			case W:
				return 2.0;
			case NW:
				return 1.0;
			default:
				throw new IllegalArgumentException();
			}
		case S:
			switch (action) {
			case SE:
			case SW:
				return 4.0;
			case E:
			case W:
				return 3.0;
			case NE:
			case NW:
				return 2.0;
			case N:
				return 1.0;
			default:
				throw new IllegalArgumentException();
			}
		case SW:
			switch (action) {
			case S:
			case W:
				return 4.0;
			case SE:
			case NW:
				return 3.0;
			case E:
			case N:
				return 2.0;
			case NE:
				return 1.0;
			default:
				throw new IllegalArgumentException();
			}
		case W:
			switch (action) {
			case SW:
			case NW:
				return 4.0;
			case S:
			case N:
				return 3.0;
			case SE:
			case NE:
				return 2.0;
			case E:
				return 1.0;
			default:
				throw new IllegalArgumentException();
			}
		case NW:
			switch (action) {
			case W:
			case N:
				return 4.0;
			case SW:
			case NE:
				return 3.0;
			case S:
			case E:
				return 2.0;
			case SE:
				return 1.0;
			default:
				throw new IllegalArgumentException();
			}
		default:
			throw new IllegalStateException();
		}
	}

	public int getRows() {
		return this.rows;
	}

	public int getCols() {
		return this.cols;
	}

	public int getGoalRow() {
		return this.goalRow;
	}

	public int getGoalCol() {
		return this.goalCol;
	}


}
