package ai.libs.jaicore.search.probleminputs;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Predicate;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPolicy;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class MDPUtils implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(MDPUtils.class);

	public <N, A> N drawSuccessorState(final IMDP<N, A, ?> mdp, final N state, final A action) {
		return this.drawSuccessorState(mdp, state, action, new Random());
	}

	public <N, A> N drawSuccessorState(final IMDP<N, A, ?> mdp, final N state, final A action, final Random rand) {

		Map<N, Double> dist = mdp.getProb(state, action);
		if (!mdp.getApplicableActions(state).contains(action)) {
			throw new IllegalArgumentException("Action " + action + " is not applicable in " + state);
		}
		double p = rand.nextDouble();
		double s = 0;
		for (Entry<N, Double> neighborWithProb : dist.entrySet()) {
			s += neighborWithProb.getValue();
			if (s >= p) {
				return neighborWithProb.getKey();
			}
		}
		throw new IllegalStateException("Up to here, a state mut have been returned!");
	}

	public <N, A> IEvaluatedPath<N, A, Double> getRun(final IMDP<N, A, Double> mdp, final double gamma, final IPolicy<N, A> policy, final Random random, final Predicate<ILabeledPath<N, A>> stopCriterion) throws ActionPredictionFailedException {
		double score = 0;
		ILabeledPath<N, A> path = new SearchGraphPath<>(mdp.getInitState());
		N current = path.getRoot();
		N nextState;
		Collection<A> possibleActions = mdp.getApplicableActions(current);
		double discount = 1;
		while (!possibleActions.isEmpty() && !stopCriterion.test(path)) {
			A action = policy.getAction(current, possibleActions);
			assert possibleActions.contains(action);
			nextState = this.drawSuccessorState(mdp, current, action, random);
			this.logger.debug("Choosing action {}. Next state is {} (probability is {})", action, nextState, mdp.getProb(current, action, nextState));
			score += discount * mdp.getScore(current, action, nextState);
			discount *= gamma;
			current = nextState;
			path.extend(current, action);
			possibleActions = mdp.getApplicableActions(current);
		}
		return new EvaluatedSearchGraphPath<>(path, score);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
