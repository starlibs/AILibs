package ai.libs.jaicore.search.probleminputs;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.algorithms.mdp.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class MDPUtils implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(MDPUtils.class);

	public <N, A> Collection<N> getStates(final IMDP<N, A, ?> mdp) throws InterruptedException {
		Collection<N> states = new HashSet<>();
		Deque<N> open = new ArrayDeque<>();
		open.add(mdp.getInitState());
		while (!open.isEmpty()) {
			N next = open.pop();
			if (states.contains(next)) {
				continue;
			}
			states.add(next);
			for (A a : mdp.getApplicableActions(next)) {
				open.addAll(mdp.getProb(next, a).keySet());
			}
		}
		return states;
	}

	public <N, A> N drawSuccessorState(final IMDP<N, A, ?> mdp, final N state, final A action) throws InterruptedException {
		return this.drawSuccessorState(mdp, state, action, new Random());
	}

	public <N, A> N drawSuccessorState(final IMDP<N, A, ?> mdp, final N state, final A action, final Random rand) throws InterruptedException {
		if (!mdp.isActionApplicableInState(state, action)) {
			throw new IllegalArgumentException("Action " + action + " is not applicable in " + state);
		}
		Map<N, Double> dist = mdp.getProb(state, action);
		double p = rand.nextDouble();
		double s = 0;
		for (Entry<N, Double> neighborWithProb : dist.entrySet()) {
			s += neighborWithProb.getValue();
			if (s >= p) {
				return neighborWithProb.getKey();
			}
		}
		throw new IllegalStateException("The accumulated probability of all the " + dist.size() + " successors is only " + s + " instead of 1.\n\tState: " + state + "\n\tAction: " + action + "\nConsidered successor states: " + dist.entrySet().stream().map(e -> "\n\t" + e.toString()).collect(Collectors.joining()));
	}

	public <N, A> IEvaluatedPath<N, A, Double> getRun(final IMDP<N, A, Double> mdp, final double gamma, final IPolicy<N, A> policy, final Random random, final Predicate<ILabeledPath<N, A>> stopCriterion) throws InterruptedException, ActionPredictionFailedException, ObjectEvaluationFailedException {
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

	public static int getTimeHorizon(final double gamma, final double epsilon) {
		return gamma < 1 ? (int) Math.ceil(Math.log(epsilon) / Math.log(gamma)) : Integer.MAX_VALUE;
	}
}
