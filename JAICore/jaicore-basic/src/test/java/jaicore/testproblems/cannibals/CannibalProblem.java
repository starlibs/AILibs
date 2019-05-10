package jaicore.testproblems.cannibals;

/**
 * Problem describing the missionary cannibal game.
 * All persons must be moved to the right and the missionaries must not be a minority on either side at any time.
 *
 * @author fmohr
 *
 */
public class CannibalProblem {
	private final boolean boatOnLeft;
	private final int missionariesOnLeft;
	private final int cannibalsOnLeft;
	private final int missionariesOnRight;
	private final int cannibalsOnRight;

	public CannibalProblem(final boolean boatOnLeft, final int missionariesOnLeft, final int cannibalsOnLeft, final int missionariesOnRight, final int cannibalsOnRight) {
		super();
		this.boatOnLeft = boatOnLeft;
		this.missionariesOnLeft = missionariesOnLeft;
		this.cannibalsOnLeft = cannibalsOnLeft;
		this.missionariesOnRight = missionariesOnRight;
		this.cannibalsOnRight = cannibalsOnRight;
		if (missionariesOnLeft < 0) {
			throw new IllegalArgumentException("Number of missionaries on left must be non-negative!");
		}
		if (missionariesOnRight < 0) {
			throw new IllegalArgumentException("Number of missionaries on right must be non-negative!");
		}
		if (cannibalsOnLeft < 0) {
			throw new IllegalArgumentException("Number of cannibales on left must be non-negative!");
		}
		if (cannibalsOnRight < 0) {
			throw new IllegalArgumentException("Number of cannibales on right must be non-negative!");
		}
	}

	public boolean isLost() {
		return missionariesOnLeft > 0 && missionariesOnLeft < cannibalsOnLeft || missionariesOnRight > 0 && missionariesOnRight < cannibalsOnRight;
	}

	public boolean isWon() {
		return missionariesOnLeft == 0 && cannibalsOnLeft == 0;
	}

	public int getMissionariesOnLeft() {
		return missionariesOnLeft;
	}

	public int getCannibalsOnLeft() {
		return cannibalsOnLeft;
	}

	public int getMissionariesOnRight() {
		return missionariesOnRight;
	}

	public int getCannibalsOnRight() {
		return cannibalsOnRight;
	}

	public boolean isBoatOnLeft() {
		return boatOnLeft;
	}

	public int getTotalNumberOfPeople() {
		return cannibalsOnLeft + cannibalsOnRight + missionariesOnLeft + missionariesOnRight;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (missionariesOnLeft > 0) {
			sb.append(missionariesOnLeft + "M");
		}
		if (cannibalsOnLeft > 0) {
			sb.append(cannibalsOnLeft + "C");
		}
		sb.append(" ");
		if (missionariesOnRight > 0) {
			sb.append(missionariesOnRight + "M");
		}
		if (cannibalsOnRight > 0) {
			sb.append(cannibalsOnRight + "C");
		}
		return sb.toString();
	}
}
