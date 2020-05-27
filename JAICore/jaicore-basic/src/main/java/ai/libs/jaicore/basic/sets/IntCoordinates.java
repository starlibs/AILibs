package ai.libs.jaicore.basic.sets;

public class IntCoordinates {
	private final int x;
	private final int y;

	public IntCoordinates(final int x, final int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public IntCoordinates getUp() {
		return new IntCoordinates(this.x, this.y + 1);
	}

	public IntCoordinates getDown() {
		return new IntCoordinates(this.x, this.y - 1);
	}

	public IntCoordinates getLeft() {
		return new IntCoordinates(this.x - 1, this.y);
	}

	public IntCoordinates getRight() {
		return new IntCoordinates(this.x + 1, this.y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.x;
		result = prime * result + this.y;
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
		IntCoordinates other = (IntCoordinates) obj;
		if (this.x != other.x) {
			return false;
		}
		if (this.y != other.y) {
			return false;
		}
		return true;
	}
}
