package ai.libs.jaicore.search.exampleproblems.racetrack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ai.libs.jaicore.search.probleminputs.AMDP;

public class RacetrackMDP extends AMDP<RacetrackState, RacetrackAction, Double> {

	private final boolean[][] track; // true for accessible fields and false for others
	private final boolean[][] goal; // true for goal fields and false for others
	private final double successRate;
	private static final List<RacetrackAction> POSSIBLE_ACTIONS;
	private final boolean stopOnCrash;

	static {
		POSSIBLE_ACTIONS = new ArrayList<>();
		for (int vAcc = -1; vAcc < 2; vAcc ++) {
			for (int hAcc = -1; hAcc < 2; hAcc ++) {
				POSSIBLE_ACTIONS.add(new RacetrackAction(hAcc, vAcc));
			}
		}
	}

	private static List<RacetrackState> getPossibleInitStates(final boolean[][] start) {
		List<RacetrackState> validStartStates= new ArrayList<>();
		for (int i = 0; i < start.length; i++) {
			for (int j = 0; j < start[i].length; j++) {
				if (start[i][j]) {
					validStartStates.add(new RacetrackState(i, j, 0, 0, false, false, false));
				}
			}
		}
		return validStartStates;
	}

	private static RacetrackState drawInitState(final boolean[][] start, final Random random) {
		List<RacetrackState> validStartStates = getPossibleInitStates(start);
		Collections.shuffle(validStartStates);
		return validStartStates.get(random.nextInt(validStartStates.size()));
	}

	private final List<RacetrackState> possibleInitStates;

	public RacetrackMDP(final boolean[][] track, final boolean[][] start, final boolean[][] goal, final double successRate, final Random random, final boolean stopOnCrash) {
		super(drawInitState(start, random));
		this.track = track;
		this.goal = goal;
		this.successRate = successRate;
		this.possibleInitStates = getPossibleInitStates(start);
		this.stopOnCrash = stopOnCrash;
	}

	@Override
	public boolean isMaximizing() {
		return false; // get to the goal as fast as possible
	}

	@Override
	public Collection<RacetrackAction> getApplicableActions(final RacetrackState state) {
		if (this.stopOnCrash && state.isCrashed()) {
			return new ArrayList<>();
		}
		return POSSIBLE_ACTIONS;
	}

	private boolean hasMatchOnLine(final boolean[][] boolmap, final int xStart, final int yStart, final int xEnd, final int yEnd) {
		if (xEnd == xStart) {
			for (int i = Math.min(yStart, yEnd); i <= Math.max(yStart, yEnd); i++) {
				if (boolmap[xStart][i]) {
					return true;
				}
			}
			return false;
		}

		if (yEnd == yStart) {
			for (int i = Math.min(xStart, xEnd); i <= Math.max(xStart, xEnd); i++) {
				if (boolmap[i][yStart]) {
					return true;
				}
			}
			return false;
		}

		double slope = (yEnd - yStart) * 1.0 / (xEnd - xStart);
		int xCur = xStart;
		int yCur = yStart;
		int xMoves = 0;
		int yMoves = 0;
		int xFactor = xEnd > xStart ? 1 : -1;
		int yFactor = yEnd > yStart ? 1 : -1;
		while (xCur != xEnd) {
			xMoves ++;
			xCur += xFactor;
			int expectedYMoves = Math.abs((int)Math.round(xMoves * slope));
			if (expectedYMoves > yMoves) {
				while (expectedYMoves > yMoves) {
					yMoves ++;
					yCur += yFactor;
					if (boolmap[xCur][yCur]) {
						return true;
					}
				}
			}
			else {
				if (boolmap[xCur][yCur]) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	/**
	 *  in fact we just teleport (no check of fields in between)
	 *  */
	public Map<RacetrackState, Double> getProb(final RacetrackState state, final RacetrackAction action) {
		Map<RacetrackState, Double> out = new HashMap<>();

		/* first determine the possible new coordinates */
		int xCur = state.getX();
		int yCur = state.getY();
		int xLazy = xCur + state.gethSpeed();
		int yLazy = yCur + state.getvSpeed();
		int xAcc = xLazy += action.gethAcceleration();
		int yAcc = yLazy += action.getvAcceleration();

		/* first consider the case that speed change failed */
		if (xLazy >= 0 && xLazy < this.track.length && yLazy >= 0 && yLazy < this.track[xLazy].length && this.track[xLazy][yLazy]) {
			boolean finished = this.hasMatchOnLine(this.goal, xCur, yCur, xLazy, yLazy);
			RacetrackState succ = new RacetrackState(xLazy, yLazy, state.gethSpeed(), state.getvSpeed(), false, finished, false);
			out.put(succ, 1 - this.successRate);
		}
		else {
			double prob = (1 - this.successRate) * 1.0 / this.possibleInitStates.size();
			for (RacetrackState initState : this.possibleInitStates) {
				RacetrackState crashState = new RacetrackState(initState.getX(), initState.getY(), 0, 0, false, false, true);
				out.put(crashState, prob);
			}
		}

		/* now consider the case that the speed change succeeded */
		if (xAcc >= 0 && xAcc < this.track.length && yAcc >= 0 && yAcc < this.track[xAcc].length && this.track[xAcc][yAcc]) {
			boolean finished = this.hasMatchOnLine(this.goal, xCur, yCur, xAcc, yAcc);
			RacetrackState succ = new RacetrackState(xAcc, yAcc, state.gethSpeed() + action.gethAcceleration(), state.getvSpeed() + action.getvAcceleration(), true, finished, false);
			if (out.containsKey(succ)) {
				out.put(succ, out.get(succ) + this.successRate);
			}
			else {
				out.put(succ, this.successRate);
			}
		}
		else {
			double prob = this.successRate / this.possibleInitStates.size();
			for (RacetrackState initState : this.possibleInitStates) {
				RacetrackState crashState = new RacetrackState(initState.getX(), initState.getY(), 0, 0, true, false, true);
				out.put(crashState, out.get(crashState) + prob);
			}
		}
		return out;
	}

	@Override
	public Double getScore(final RacetrackState state, final RacetrackAction action, final RacetrackState successor) {
		double rawScore = successor.isCrashed() ? 100.0 : 1.0; // every move costs one time unit if it is admissible and 10 if the car is set back to an init state
		return rawScore / 100; // make it inside [0,1]
	}
}
