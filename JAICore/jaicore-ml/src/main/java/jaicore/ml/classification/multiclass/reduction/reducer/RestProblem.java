package jaicore.ml.classification.multiclass.reduction.reducer;

import java.util.ArrayList;
import java.util.Set;

@SuppressWarnings("serial")
class RestProblem extends ArrayList<Set<String>> {

	private static int counter = 0;
	private final int id = (counter ++);
	private final Decision edgeToParent;

	public RestProblem(final Decision edgeToParent) {
		super();
		this.edgeToParent = edgeToParent;
	}

	public Decision getEdgeToParent() {
		return this.edgeToParent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.edgeToParent == null) ? 0 : this.edgeToParent.hashCode());
		result = prime * result + this.id;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		RestProblem other = (RestProblem) obj;
		if (this.edgeToParent == null) {
			if (other.edgeToParent != null) {
				return false;
			}
		} else if (!this.edgeToParent.equals(other.edgeToParent)) {
			return false;
		}
		return this.id == other.id;
	}
}
