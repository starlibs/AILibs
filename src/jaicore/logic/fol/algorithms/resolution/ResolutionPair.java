package jaicore.logic.fol.algorithms.resolution;

import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.Literal;

public class ResolutionPair {
	private Clause c1, c2;
	private Literal l1, l2;

	public ResolutionPair(Clause c1, Clause c2, Literal l1, Literal l2) {
		super();
		this.c1 = c1;
		this.c2 = c2;
		this.l1 = l1;
		this.l2 = l2;
	}

	public Clause getC1() {
		return c1;
	}

	public Clause getC2() {
		return c2;
	}

	public Literal getL1() {
		return l1;
	}

	public Literal getL2() {
		return l2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((c1 == null) ? 0 : c1.hashCode());
		result = prime * result + ((c2 == null) ? 0 : c2.hashCode());
		result = prime * result + ((l1 == null) ? 0 : l1.hashCode());
		result = prime * result + ((l2 == null) ? 0 : l2.hashCode());
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
		ResolutionPair other = (ResolutionPair) obj;
		if (c1 == null) {
			if (other.c1 != null)
				return false;
		} else if (!c1.equals(other.c1))
			return false;
		if (c2 == null) {
			if (other.c2 != null)
				return false;
		} else if (!c2.equals(other.c2))
			return false;
		if (l1 == null) {
			if (other.l1 != null)
				return false;
		} else if (!l1.equals(other.l1))
			return false;
		if (l2 == null) {
			if (other.l2 != null)
				return false;
		} else if (!l2.equals(other.l2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PL1ResolutionPair [c1=" + c1 + ", c2=" + c2 + ", l1=" + l1 + ", l2=" + l2 + "]";
	}
}
