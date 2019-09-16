package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.Collection;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class ObservationsUpdatedEvent<N> extends AAlgorithmEvent {

	private final N node;
	private final Collection<Double> scoresLeft;
	private final Collection<Double> scoresRight;
	private final int winsLeft;
	private final int winsRight;
	private final double pLeft;
	private final double pRight;
	private final double pLeftScaled;
	private final double pRightScaled;
	private final int visits;

	public ObservationsUpdatedEvent(final String algorithmId, final N node, final int visits, final Collection<Double> scoresLeft, final Collection<Double> scoresRight, final int winsLeft, final int winsRights, final double pLeft, final double pRight, final double pLeftScaled, final double pRightScaled) {
		super(algorithmId);
		this.visits = visits;
		this.node = node;
		this.winsLeft = winsLeft;
		this.winsRight = winsRights;
		this.scoresLeft = scoresLeft;
		this.scoresRight = scoresRight;
		this.pLeft = pLeft;
		this.pRight = pRight;
		this.pLeftScaled = pLeftScaled;
		this.pRightScaled = pRightScaled;
	}

	public N getNode() {
		return this.node;
	}

	public int getVisits() {
		return this.visits;
	}

	public Collection<Double> getScoresLeft() {
		return this.scoresLeft;
	}

	public Collection<Double> getScoresRight() {
		return this.scoresRight;
	}

	public double getpLeft() {
		return this.pLeft;
	}

	public double getpRight() {
		return this.pRight;
	}

	public double getpLeftScaled() {
		return this.pLeftScaled;
	}

	public double getpRightScaled() {
		return this.pRightScaled;
	}

	public int getWinsLeft() {
		return this.winsLeft;
	}

	public int getWinsRight() {
		return this.winsRight;
	}
}
