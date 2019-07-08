package ai.libs.jaicore.planning.core;

import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class SearchGraphBasedPlan<N, A> extends Plan {

	private final SearchGraphPath<N, A> searchGraphPath;

	public SearchGraphBasedPlan(final Plan plan, final SearchGraphPath<N, A> searchGraphPath) {
		super(plan.getActions());
		this.searchGraphPath = searchGraphPath;
	}

	public SearchGraphPath<N, A> getSearchGraphPath() {
		return this.searchGraphPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.searchGraphPath == null) ? 0 : this.searchGraphPath.hashCode());
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
		SearchGraphBasedPlan other = (SearchGraphBasedPlan) obj;
		if (this.searchGraphPath == null) {
			if (other.searchGraphPath != null) {
				return false;
			}
		} else if (!this.searchGraphPath.equals(other.searchGraphPath)) {
			return false;
		}
		return true;
	}
}
