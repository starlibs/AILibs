package jaicore.search.testproblems.knapsack;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class KnapsackNode {

	private final List<String> packedObjects;
	private final Set<String> remainingObjects;
	private final double usedCapacity;

	public KnapsackNode(List<String> packedObjects, Set<String> remainingObjects, double usedCapacity) {
		super();
		this.packedObjects = packedObjects;
		this.remainingObjects = remainingObjects;
		this.usedCapacity = usedCapacity;
	}

	public List<String> getPackedObjects() {
		return this.packedObjects;
	}

	public double getUsedCapacity() {
		return this.usedCapacity;
	}

	public Set<String> getRemainingObjects() {
		return remainingObjects;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((packedObjects == null) ? 0 : packedObjects.hashCode());
		result = prime * result + ((remainingObjects == null) ? 0 : remainingObjects.hashCode());
		long temp;
		temp = Double.doubleToLongBits(usedCapacity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KnapsackNode other = (KnapsackNode) obj;
		if (packedObjects == null) {
			if (other.packedObjects != null)
				return false;
		} else if (!packedObjects.equals(other.packedObjects))
			return false;
		if (remainingObjects == null) {
			if (other.remainingObjects != null)
				return false;
		} else if (!remainingObjects.equals(other.remainingObjects))
			return false;
		if (Double.doubleToLongBits(usedCapacity) != Double.doubleToLongBits(other.usedCapacity))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String s = "[";
		Iterator<String> it = packedObjects.iterator();
		while (it.hasNext()) {
			s += it.next();
			if (it.hasNext()) {
				s += ", ";
			}
		}
		s += "]-<" + usedCapacity + ">";
		return s;
	}
}