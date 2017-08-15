package jaicore.ml.classification.multiclass.reduction.reducer;

import java.util.ArrayList;
import java.util.Set;

@SuppressWarnings("serial")
class RestProblem extends ArrayList<Set<String>> {
	
	private static int counter = 0;
	private final int id = (counter ++);
	private final Decision edgeToParent;

	public RestProblem(Decision edgeToParent) {
		super();
		this.edgeToParent = edgeToParent;
	}

	public Decision getEdgeToParent() {
		return edgeToParent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((edgeToParent == null) ? 0 : edgeToParent.hashCode());
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestProblem other = (RestProblem) obj;
		if (edgeToParent == null) {
			if (other.edgeToParent != null)
				return false;
		} else if (!edgeToParent.equals(other.edgeToParent))
			return false;
		if (id != other.id)
			return false;
		return true;
	}
}
