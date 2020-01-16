package ai.libs.jaicore.search.algorithms.standard.lds;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.algorithm.ASolutionCandidateFoundEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmFinishedEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.basic.algorithm.EAlgorithmState;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.SuccessorComputationCompletedEvent;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 * This class conducts a limited discrepancy search by running a best first algorithm with list-based node evaluations.
 * Since the f-values are lists too, we do not simply extend BestFirst but rather forward all commands to it.
 *
 * @author fmohr
 *
 * @param <T>
 * @param <A>
 * @param <V>
 */
public class BestFirstLimitedDiscrepancySearch<I extends GraphSearchWithNodeRecommenderInput<T, A>, T, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, T, A, V> {

	private Logger logger = LoggerFactory.getLogger(BestFirstLimitedDiscrepancySearch.class);
	private String loggerName;

	private final StandardBestFirst<T, A, NodeOrderList> bestFirst;

	private class OrderListNumberComputer implements IPathEvaluator<T, A, NodeOrderList> {
		private final Comparator<T> heuristic;
		private final Map<BackPointerPath<T, A, ?>, List<T>> childOrdering = new HashMap<>();

		public OrderListNumberComputer(final Comparator<T> heuristic) {
			super();
			this.heuristic = heuristic;
		}

		@Override
		public NodeOrderList evaluate(final ILabeledPath<T, A> node) {
			NodeOrderList list = new NodeOrderList();
			BackPointerPath<T, A, ?> parent = ((BackPointerPath<T, A, ?>)node).getParent();
			if (parent == null) {
				return list;
			}

			/* add the label sequence of the parent to this node*/
			list.addAll((NodeOrderList) parent.getScore());
			list.add(this.childOrdering.get(parent).indexOf(node.getHead()));
			return list;
		}

		@Subscribe
		public void receiveSuccessorsCreatedEvent(final SuccessorComputationCompletedEvent<T, A, ?> successorDescriptions) {
			List<T> successors = successorDescriptions.getSuccessorDescriptions().stream().map(INewNodeDescription::getTo).sorted(this.heuristic).collect(Collectors.toList());
			this.childOrdering.put(successorDescriptions.getNode(), successors);
		}
	}

	public BestFirstLimitedDiscrepancySearch(final I problem) {
		super(problem);
		OrderListNumberComputer nodeEvaluator = new OrderListNumberComputer(problem.getRecommender());
		this.bestFirst = new StandardBestFirst<>(new GraphSearchWithSubpathEvaluationsInput<>(problem, nodeEvaluator));
		this.bestFirst.registerListener(nodeEvaluator);
	}

	@Override
	public void cancel() {
		super.cancel();
		this.bestFirst.cancel();
	}

	@Override
	public void registerListener(final Object listener) {
		this.bestFirst.registerListener(listener);
	}

	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		super.setNumCPUs(numberOfCPUs);
		this.bestFirst.setNumCPUs(numberOfCPUs);
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		this.checkAndConductTermination();
		if (this.getState().equals(EAlgorithmState.CREATED)) {
			this.bestFirst.setTimeout(this.getTimeout());
			return this.activate();
		}
		IAlgorithmEvent e = this.bestFirst.nextWithException();
		if (e instanceof AlgorithmInitializedEvent) {
			return this.nextWithException();
		} else if (e instanceof AlgorithmFinishedEvent) {
			return this.terminate();
		} else if (e instanceof ISolutionCandidateFoundEvent) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			EvaluatedSearchGraphPath<T, A, NodeOrderList> solution = (EvaluatedSearchGraphPath<T, A, NodeOrderList>) ((ISolutionCandidateFoundEvent) e).getSolutionCandidate();
			EvaluatedSearchGraphPath<T, A, V> modifiedSolution = new EvaluatedSearchGraphPath<>(solution.getNodes(), solution.getArcs(), null);
			return new ASolutionCandidateFoundEvent<>(this, modifiedSolution);
		} else {
			return e;
		}
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		if (this.bestFirst instanceof ILoggingCustomizable) {
			this.bestFirst.setLoggerName(name + ".bestfirst");
		}
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}
}
