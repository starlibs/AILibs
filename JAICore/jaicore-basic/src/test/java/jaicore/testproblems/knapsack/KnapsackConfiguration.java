package jaicore.testproblems.knapsack;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class KnapsackConfiguration {

	private final Set<String> packedObjects;
	private final Set<String> remainingObjects;
	private final double usedCapacity;

	public KnapsackConfiguration(final Set<String> packedObjects, final Set<String> remainingObjects, final double usedCapacity) {
		super();
		this.packedObjects = packedObjects;
		this.remainingObjects = remainingObjects;
		this.usedCapacity = usedCapacity;
	}

	public Set<String> getPackedObjects() {
		return this.packedObjects;
	}

	public double getUsedCapacity() {
		return this.usedCapacity;
	}

	public Set<String> getRemainingObjects() {
		return this.remainingObjects;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.packedObjects == null) ? 0 : this.packedObjects.hashCode());
		result = prime * result + ((this.remainingObjects == null) ? 0 : this.remainingObjects.hashCode());
		long temp;
		temp = Double.doubleToLongBits(this.usedCapacity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		KnapsackConfiguration other = (KnapsackConfiguration) obj;
		if (this.packedObjects == null) {
			if (other.packedObjects != null) {
				return false;
			}
		} else if (!this.packedObjects.equals(other.packedObjects)) {
			return false;
		}
		if (this.remainingObjects == null) {
			if (other.remainingObjects != null) {
				return false;
			}
		} else if (!this.remainingObjects.equals(other.remainingObjects)) {
			return false;
		}
		return Double.doubleToLongBits(this.usedCapacity) == Double.doubleToLongBits(other.usedCapacity);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(packedObjects);
		sb.append("-<" + this.usedCapacity + ">");
		return sb.toString();
	}
}