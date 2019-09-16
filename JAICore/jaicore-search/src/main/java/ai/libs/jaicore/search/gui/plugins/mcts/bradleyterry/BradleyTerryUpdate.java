package ai.libs.jaicore.search.gui.plugins.mcts.bradleyterry;

import java.util.Collection;

public class BradleyTerryUpdate {
	private final String node;
	private final int visits;
	private final int winsLeft;
	private final int winsRight;
	private final Collection<Double> scoresLeft;
	private final Collection<Double> scoresRight;
	private final double pLeft;
	private final double pRight;
	private final double pLeftScaled;
	private final double pRightScaled;

	public BradleyTerryUpdate(final String node, final int visits, final int winsLeft, final int winsRight, final Collection<Double> scoresLeft, final Collection<Double> scoresRight, final double pLeft, final double pRight, final double pLeftScaled, final double pRightScaled) {
		super();
		this.node = node;
		this.visits = visits;
		this.winsLeft = winsLeft;
		this.winsRight = winsRight;
		this.scoresLeft = scoresLeft;
		this.scoresRight = scoresRight;
		this.pLeft = pLeft;
		this.pRight = pRight;
		this.pLeftScaled = pLeftScaled;
		this.pRightScaled = pRightScaled;
	}

	public String getNode() {
		return this.node;
	}

	public int getVisits() {
		return this.visits;
	}

	public int getWinsLeft() {
		return this.winsLeft;
	}

	public int getWinsRight() {
		return this.winsRight;
	}

	public Collection<Double> getScoresLeft() {
		return this.scoresLeft;
	}

	public double getpLeft() {
		return this.pLeft;
	}

	public double getpRight() {
		return this.pRight;
	}

	public Collection<Double> getScoresRight() {
		return this.scoresRight;
	}

	public double getpLeftScaled() {
		return this.pLeftScaled;
	}

	public double getpRightScaled() {
		return this.pRightScaled;
	}
}
