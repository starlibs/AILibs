package ai.libs.jaicore.search.exampleproblems.taxi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ai.libs.jaicore.basic.sets.IntCoordinates;
import ai.libs.jaicore.search.probleminputs.AMDP;

public class TaxiMDP extends AMDP<TaxiState, ETaxiAction, Double> {
	private final boolean[][][] possibleTransitions; // this is the street map: width x height. Third dimension is 0=N,1=E,2=S,3=W
	private final int width;
	private final int height;
	private final double successRate;
	private final List<IntCoordinates> pickupLocations;
	private final IntCoordinates pickupLocation;
	private final IntCoordinates targetLocation;

	private static TaxiState drawInitState(final int width, final int height, final List<IntCoordinates> pickupLocations, final Random random) {
		int x;
		int y;
		IntCoordinates coords;
		do
		{
			x = random.nextInt(width);
			y = random.nextInt(height);
			coords = new IntCoordinates(x, y);
		}
		while (pickupLocations.contains(coords));
		return new TaxiState(coords, false, false);
	}

	public TaxiMDP(final boolean[][][] possibleTransitions, final double successRate, final List<IntCoordinates> pickupLocations, final Random random) {
		super (drawInitState(possibleTransitions.length, possibleTransitions[0].length, pickupLocations, random));
		this.width = possibleTransitions.length;
		this.height = possibleTransitions[0].length;
		this.possibleTransitions = possibleTransitions;
		this.successRate = successRate;
		this.pickupLocations = pickupLocations;
		this.pickupLocation = pickupLocations.get(0);
		this.targetLocation = pickupLocations.get(1);
	}

	public boolean[][][] getPossibleTransitions() {
		return this.possibleTransitions;
	}

	public double getSuccessRate() {
		return this.successRate;
	}

	public List<IntCoordinates> getPickupLocations() {
		return this.pickupLocations;
	}

	@Override
	public boolean isMaximizing() {
		return true;
	}

	@Override
	public Collection<ETaxiAction> getApplicableActions(final TaxiState state) {
		Collection<ETaxiAction> possibleActions = new ArrayList<>();
		if (state.isPassengerDelivered()) {
			return possibleActions;
		}
		if (state.getPosition().equals(this.pickupLocation)  && !state.isPassengerOnBoard()) {
			possibleActions.add(ETaxiAction.PICKUP);
			return possibleActions;
		}
		if (state.getPosition().equals(this.targetLocation)  && state.isPassengerOnBoard()) {
			possibleActions.add(ETaxiAction.PUTDOWN);
			return possibleActions;
		}

		for (ETaxiAction a : ETaxiAction.values()) {
			if (a != ETaxiAction.PICKUP && a != ETaxiAction.PUTDOWN && this.isDirectionPossible(state, a)) {
				possibleActions.add(a);
			}
		}
		return possibleActions;
	}

	private boolean isDirectionPossible(final TaxiState state, final ETaxiAction action) {
		int x = state.getPosition().getX();
		int y = state.getPosition().getY();
		switch (action) {
		case W:
			return x > 0 && this.possibleTransitions[x][y][3];
		case E:
			return x < this.width - 1 && this.possibleTransitions[x][y][1];
		case S:
			return y > 0 && this.possibleTransitions[x][y][2];
		case N:
			return y < this.height - 1 && this.possibleTransitions[x][y][0];
		default: throw new IllegalStateException("Invalid direction " + action);
		}
	}

	@Override
	public Map<TaxiState, Double> getProb(final TaxiState state, final ETaxiAction action) {
		Map<TaxiState, Double> dist = new HashMap<>();
		if (action == ETaxiAction.PICKUP) {
			TaxiState succ = new TaxiState(state.getPosition(), true, false);
			dist.put(succ, 1.0);
			return dist;
		}
		if (action == ETaxiAction.PUTDOWN) {
			TaxiState succ = new TaxiState(state.getPosition(), false, true);
			dist.put(succ, 1.0);
			return dist;
		}

		IntCoordinates pos = state.getPosition();
		boolean lPossible = this.isDirectionPossible(state, ETaxiAction.W);
		boolean rPossible = this.isDirectionPossible(state, ETaxiAction.E);
		boolean tPossible = this.isDirectionPossible(state, ETaxiAction.N);
		boolean bPossible = this.isDirectionPossible(state, ETaxiAction.S);
		boolean isOnBoard = state.isPassengerOnBoard();
		boolean isDelivered = state.isPassengerDelivered();

		if (action == ETaxiAction.N) {
			dist.put(new TaxiState(pos.getUp(), isOnBoard, isDelivered), this.successRate);
			double prob = (lPossible && rPossible) ? (1 - this.successRate) / 2 : (1 - this.successRate);
			if (lPossible) {
				dist.put(new TaxiState(pos.getLeft(), isOnBoard, isDelivered), prob);
			}
			if (rPossible) {
				dist.put(new TaxiState(pos.getRight(), isOnBoard, isDelivered), prob);
			}
		}
		else if (action == ETaxiAction.E) {
			dist.put(new TaxiState(pos.getRight(), isOnBoard, isDelivered), this.successRate);
			double prob = (tPossible && bPossible) ? (1 - this.successRate) / 2 : (1 - this.successRate);
			if (tPossible) {
				dist.put(new TaxiState(pos.getUp(), isOnBoard, isDelivered), prob);
			}
			if (bPossible) {
				dist.put(new TaxiState(pos.getDown(), isOnBoard, isDelivered), prob);
			}
		}
		else if (action == ETaxiAction.S) {
			dist.put(new TaxiState(pos.getDown(), isOnBoard, isDelivered), this.successRate);
			double prob = (lPossible && rPossible) ? (1 - this.successRate) / 2 : (1 - this.successRate);
			if (lPossible) {
				dist.put(new TaxiState(pos.getLeft(), isOnBoard, isDelivered), prob);
			}
			if (rPossible) {
				dist.put(new TaxiState(pos.getRight(), isOnBoard, isDelivered), prob);
			}
		}
		else if (action == ETaxiAction.W) {
			dist.put(new TaxiState(pos.getLeft(), isOnBoard, isDelivered), this.successRate);
			double prob = (tPossible && bPossible) ? (1 - this.successRate) / 2 : (1 - this.successRate);
			if (tPossible) {
				dist.put(new TaxiState(pos.getUp(), isOnBoard, isDelivered), prob);
			}
			if (bPossible) {
				dist.put(new TaxiState(pos.getDown(), isOnBoard, isDelivered), prob);
			}
		}
		else {
			throw new IllegalArgumentException("Do not know how to process action " + action + " in state " + state);
		}
		return dist;
	}

	@Override
	public Double getScore(final TaxiState state, final ETaxiAction action, final TaxiState successor) {
		if (successor.isPassengerDelivered() && action == ETaxiAction.PUTDOWN) {
			return (20.0 - 1) / 100;
		}
		return -1.0 / 100;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int cols = this.width;
		int rows = this.height;
		int startCol = this.getInitState().getPosition().getX();
		int startRow = this.getInitState().getPosition().getY();
		int pickupCol = this.pickupLocation.getX();
		int pickupRow = this.pickupLocation.getY();
		int targetCol = this.targetLocation.getX();
		int targetRow = this.targetLocation.getY();
		for (int r = rows - 1; r >= 0; r --) {

			/* print grid above row */
			for (int c = 0; c < cols; c ++) {
				sb.append("+-");
			}
			sb.append("+\n");

			/* print content of row */
			for (int c = 0; c < cols; c ++) {
				sb.append("|");
				if (c == startCol && r == startRow) {
					sb.append("x");
				}
				else if (targetCol == c && targetRow == r) {
					sb.append("*");
				}
				else if (pickupCol == c && pickupRow == r) {
					sb.append("o");
				}
				else {
					sb.append(" ");
				}
			}
			sb.append("+\n");
		}
		return sb.toString();
	}
}
