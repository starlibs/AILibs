package jaicore.logic.fol.algorithms.resolution;

import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.Literal;

public class ResolutionPair {
	private Clause c1;
	private Clause c2;
	private Literal l1;
	private Literal l2;

	public ResolutionPair(final Clause c1, final Clause c2, final Literal l1, final Literal l2) {
		super();
		this.c1 = c1;
		this.c2 = c2;
		this.l1 = l1;
		this.l2 = l2;
	}

	public Clause getC1() {
		return this.c1;
	}

	public Clause getC2() {
		return this.c2;
	}

	public Literal getL1() {
		return this.l1;
	}

	public Literal getL2() {
		return this.l2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.c1 == null) ? 0 : this.c1.hashCode());
		result = prime * result + ((this.c2 == null) ? 0 : this.c2.hashCode());
		result = prime * result + ((this.l1 == null) ? 0 : this.l1.hashCode());
		result = prime * result + ((this.l2 == null) ? 0 : this.l2.hashCode());
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
		ResolutionPair other = (ResolutionPair) obj;
		if (this.c1 == null) {
			if (other.c1 != null) {
				return false;
			}
		} else if (!this.c1.equals(other.c1)) {
			return false;
		}
		if (this.c2 == null) {
			if (other.c2 != null) {
				return false;
			}
		} else if (!this.c2.equals(other.c2)) {
			return false;
		}
		if (this.l1 == null) {
			if (other.l1 != null) {
				return false;
			}
		} else if (!this.l1.equals(other.l1)) {
			return false;
		}
		if (this.l2 == null) {
			if (other.l2 != null) {
				return false;
			}
		} else if (!this.l2.equals(other.l2)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PL1ResolutionPair [c1=" + this.c1 + ", c2=" + this.c2 + ", l1=" + this.l1 + ", l2=" + this.l2 + "]";
	}
}
