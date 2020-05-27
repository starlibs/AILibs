package ai.libs.jaicore.search.probleminputs;

import java.util.Collection;
import java.util.Map;

public interface IMDP<N, A, V extends Comparable<V>> {

	public N getInitState();

	public boolean isMaximizing();

	public Collection<A> getApplicableActions(N state);

	public Map<N, Double> getProb(N state, A action);

	public double getProb(N state, A action, N successor);

	public V getScore(N state, A action, N successor);
}
