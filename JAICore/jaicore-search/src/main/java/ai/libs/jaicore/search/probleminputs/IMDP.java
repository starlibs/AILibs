package ai.libs.jaicore.search.probleminputs;

import java.util.Collection;
import java.util.Map;

import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

public interface IMDP<N, A, V extends Comparable<V>> {

	public N getInitState();

	public boolean isMaximizing();

	public Collection<A> getApplicableActions(N state) throws InterruptedException;

	public boolean isTerminalState(N state) throws InterruptedException;

	public Map<N, Double> getProb(N state, A action) throws InterruptedException;

	public double getProb(N state, A action, N successor) throws InterruptedException;

	public V getScore(N state, A action, N successor) throws InterruptedException, ObjectEvaluationFailedException;
}
