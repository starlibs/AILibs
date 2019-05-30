package jaicore.testproblems.enhancedttsp;

public class Location {
	private final short id;
	private final double x;
	private final double y;

	public Location(final short id, final double x, final double y) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
	}

	public short getId() {
		return this.id;
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.id;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Location other = (Location) obj;
		if (this.id != other.id) {
			return false;
		}
		return true;
	}
}
