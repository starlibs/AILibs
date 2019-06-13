package ai.libs.jaicore.planning.hierarchical.algorithms;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.ILoggingCustomizable;
import ai.libs.jaicore.basic.TimeOut;
import ai.libs.jaicore.basic.algorithm.AOptimizer;
import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import ai.libs.jaicore.planning.classical.problems.strips.Operation;
import ai.libs.jaicore.planning.core.Action;
import ai.libs.jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import ai.libs.jaicore.planning.core.Plan;
import ai.libs.jaicore.planning.core.events.PlanFoundEvent;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningGraphGeneratorDeriver;
import ai.libs.jaicore.planning.hierarchical.problems.stn.Method;
import ai.libs.jaicore.search.core.interfaces.IOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.builders.SearchProblemInputBuilder;

/**
 *
 * @author fmohr
 *
 * @param <P> class of the HTN planning problem
 * @param <S> class of the graph search problem input to which the HTN problem is reduced
 * @param <N> class of the nodes in the search problem
 * @param <A> class of the edges in the search problem
 * @param <V> evaluation of solutions
 */
public class GraphSearchBasedHTNPlanningAlgorithm<P extends IHTNPlanningProblem, S extends GraphSearchInput<N, A>, N, A, V extends Comparable<V>> extends AOptimizer<P, EvaluatedSearchGraphBasedPlan<V, N>, V> {

	private Logger logger = LoggerFactory.getLogger(GraphSearchBasedHTNPlanningAlgorithm.class);
	private String loggerName;

	/* algorithm inputs */
	private final IHierarchicalPlanningGraphGeneratorDeriver<P, N, A> problemTransformer;
	private final IOptimalPathInORGraphSearch<S, N, A, V> search;

	public GraphSearchBasedHTNPlanningAlgorithm(final P problem, final IHierarchicalPlanningGraphGeneratorDeriver<P, N, A> problemTransformer, final IOptimalPathInORGraphSearchFactory<S, N, A, V> searchFactory,
			final SearchProblemInputBuilder<N, A, S> searchProblemBuilder) {
		super(problem);

		this.problemTransformer = problemTransformer;

		/* set the problem in the search factory */
		searchProblemBuilder.setGraphGenerator(problemTransformer.encodeProblem(problem).getGraphGenerator());
		this.search = searchFactory.getAlgorithm(searchProblemBuilder.build());
	}

	public List<Action> getPlan(final List<TFDNode> path) {
		return path.stream().filter(n -> n.getAppliedAction() != null).map(TFDNode::getAppliedAction).collect(Collectors.toList());
	}

	@Override
	public void cancel() {
		super.cancel();
		this.getSearch().cancel();
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmExecutionCanceledException, InterruptedException, AlgorithmTimeoutedException, AlgorithmException {

		this.logger.debug("I'm being asked whether there is a next solution.");

		switch (this.getState()) {
		case CREATED:
			this.logger.info("Starting HTN planning process.");
			if (this.logger.isDebugEnabled()) {
				StringBuilder opSB = new StringBuilder();
				for (Operation op : this.getInput().getDomain().getOperations()) {
					opSB.append("\n\t\t");
					opSB.append(op);
				}
				StringBuilder methodSB = new StringBuilder();
				for (Method method : this.getInput().getDomain().getMethods()) {
					methodSB.append("\n\t\t");
					methodSB.append(method);
				}
				this.logger.debug("The HTN problem is defined as follows:\n\tOperations:{}\n\tMethods:{}", opSB, methodSB);
			}

			/* set timeout on search */
			TimeOut to = this.getTimeout();
			this.logger.debug("Setting timeout of search to {}", to);
			this.search.setTimeout(to);
			return this.activate();

		case ACTIVE:
			if (this.isCanceled()) {
				throw new IllegalStateException("The planner has already been canceled. Cannot compute more plans.");
			}
			this.logger.info("Starting/continuing search for next plan.");
			try {
				EvaluatedSearchGraphPath<N, A, V> solution = this.search.nextSolutionCandidate();
				if (solution == null) {
					this.logger.info("No more solutions will be found. Terminating algorithm.");
					return this.terminate();
				}
				this.logger.info("Next solution found.");
				Plan plan = this.problemTransformer.decodeSolution(solution);
				PlanFoundEvent<?, V> event = new PlanFoundEvent<>(this.getId(), new EvaluatedSearchGraphBasedPlan<>(plan.getActions(), solution.getScore(), solution));
				this.post(event);
				return event;
			} catch (NoSuchElementException e) { // if no more solution exists, terminate
				return this.terminate();
			}

		default:
			throw new IllegalStateException("Don't know what to do in state " + this.getState());
		}
	}

	public IOptimalPathInORGraphSearch<S, N, A, V> getSearch() {
		return this.search;
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
		if (this.problemTransformer instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of problem transformer to {}.problemtransformer", name);
			((ILoggingCustomizable) this.problemTransformer).setLoggerName(name + ".problemtransformer");
		}
		if (this.search instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of search to {}.search", name);
			((ILoggingCustomizable) this.search).setLoggerName(name + ".search");
		}
		super.setLoggerName(this.loggerName + "._algorithm");
	}
}
