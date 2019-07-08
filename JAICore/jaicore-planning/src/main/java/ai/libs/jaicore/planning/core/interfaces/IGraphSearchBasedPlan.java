package ai.libs.jaicore.planning.core.interfaces;

import ai.libs.jaicore.search.model.other.SearchGraphPath;

public interface IGraphSearchBasedPlan<N, A> extends IPlan {
	public SearchGraphPath<N, A> getSearchGraphPath();
}
