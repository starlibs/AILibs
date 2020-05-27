package ai.libs.jaicore.search.exampleproblems.taxi;

import ai.libs.jaicore.basic.sets.IntCoordinates;

public class TaxiState {
	private final IntCoordinates position;
	private final boolean passengerOnBoard;
	private final boolean passengerDelivered;

	public TaxiState(final IntCoordinates position, final boolean passengerOnBoard, final boolean passengerDelivered) {
		super();
		this.position = position;
		this.passengerOnBoard = passengerOnBoard;
		this.passengerDelivered = passengerDelivered;
	}

	public IntCoordinates getPosition() {
		return this.position;
	}

	public boolean isPassengerOnBoard() {
		return this.passengerOnBoard;
	}

	public boolean isPassengerDelivered() {
		return this.passengerDelivered;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.passengerDelivered ? 1231 : 1237);
		result = prime * result + (this.passengerOnBoard ? 1231 : 1237);
		result = prime * result + ((this.position == null) ? 0 : this.position.hashCode());
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
		TaxiState other = (TaxiState) obj;
		if (this.passengerDelivered != other.passengerDelivered) {
			return false;
		}
		if (this.passengerOnBoard != other.passengerOnBoard) {
			return false;
		}
		if (this.position == null) {
			if (other.position != null) {
				return false;
			}
		} else if (!this.position.equals(other.position)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.position.getX() + "/" + this.position.getY() + "[" + this.passengerOnBoard + ", " + this.passengerDelivered + "]";
	}


}
