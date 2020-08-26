package ai.libs.jaicore.search.probleminputs;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

public interface IMDP<N, A, V extends Comparable<V>> {

	public N getInitState();

	public boolean isMaximizing();

	public Collection<A> getApplicableActions(N state) throws InterruptedException;

	public boolean isActionApplicableInState(N state, A action) throws InterruptedException; // important short cut to avoid computation of all action

	public A getUniformlyRandomApplicableAction(N state, Random random) throws InterruptedException; // this is to enable quick queries for applicable actions if we are gonna take a random one anyway

	public boolean isTerminalState(N state) throws InterruptedException;

	public Map<N, Double> getProb(N state, A action) throws InterruptedException;

	public double getProb(N state, A action, N successor) throws InterruptedException;

	public V getScore(N state, A action, N successor) throws InterruptedException, ObjectEvaluationFailedException;
}
