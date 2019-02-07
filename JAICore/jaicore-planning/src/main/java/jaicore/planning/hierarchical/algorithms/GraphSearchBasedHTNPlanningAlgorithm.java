package jaicore.planning.hierarchical.algorithms;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AOptimizer;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.planning.classical.problems.strips.Operation;
import jaicore.planning.core.Action;
import jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import jaicore.planning.core.Plan;
import jaicore.planning.core.events.PlanFoundEvent;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningGraphGeneratorDeriver;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearch;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.builders.SearchProblemInputBuilder;

/**
 * 
 * @author fmohr
 *
 * @param <PA> class of actions in the planning problem
 * @param <P> class of the HTN planning problem
 * @param <ISearch> class of the graph search problem input to which the HTN problem is reduced
 * @param <N> class of the nodes in the search problem
 * @param <A> class of the edges in the search problem
 * @param <V> evaluation of solutions
 */
public class GraphSearchBasedHTNPlanningAlgorithm<IP extends IHTNPlanningProblem, ISearch extends GraphSearchInput<N, A>, N, A, V extends Comparable<V>>
		extends AOptimizer<IP, EvaluatedSearchGraphBasedPlan<V, N>, V> {

	private Logger logger = LoggerFactory.getLogger(GraphSearchBasedHTNPlanningAlgorithm.class);
	private String loggerName;

	/* algorithm inputs */
	private final IHierarchicalPlanningGraphGeneratorDeriver<IP, N, A> problemTransformer;
	private final IOptimalPathInORGraphSearch<ISearch, N, A, V> search;

	public GraphSearchBasedHTNPlanningAlgorithm(final IP problem, final IHierarchicalPlanningGraphGeneratorDeriver<IP, N, A> problemTransformer,
			final IOptimalPathInORGraphSearchFactory<ISearch, N, A, V> searchFactory, final SearchProblemInputBuilder<N, A, ISearch> searchProblemBuilder) {
		super(problem);

		this.problemTransformer = problemTransformer;

		/* set the problem in the search factory */
		searchProblemBuilder.setGraphGenerator(problemTransformer.transform(problem));
		searchFactory.setProblemInput(searchProblemBuilder.build());
		this.search = searchFactory.getAlgorithm();
	}
	
	public List<Action> getPlan(final List<TFDNode> path) {
		return path.stream().filter(n -> n.getAppliedAction() != null).map(n -> n.getAppliedAction()).collect(Collectors.toList());
	}

	@Override
	public void cancel() {
		super.cancel();
		this.getSearch().cancel();
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmExecutionCanceledException, InterruptedException, TimeoutException, AlgorithmException {

		this.logger.debug("I'm being asked whether there is a next solution.");

		switch (this.getState()) {
		case created: {
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
				this.logger.debug("The HTN problem is defined as follows:\n\tOperations:{}\n\tMethods:{}", opSB.toString(), methodSB.toString());
			}

//			if (this.getLoggerName() != null && this.getLoggerName().length() > 0 && this.search instanceof ILoggingCustomizable) {
//				this.logger.info("Setting logger of search to {}", this.getLoggerName() + ".search");
//				((ILoggingCustomizable) this.search).setLoggerName(this.getLoggerName() + ".search");
//			}
			
			/* set timeout on search */
			TimeOut to = getTimeout();
			logger.debug("Setting timeout of search to {}", to);
			this.search.setTimeout(to);
			return activate();
		}
		case active: {
			if (this.isCanceled()) {
				throw new IllegalStateException("The planner has already been canceled. Cannot compute more plans.");
			}
			this.logger.info("Starting/continuing search for next plan.");
			try {
				EvaluatedSearchGraphPath<N, A, V> solution = this.search.nextSolutionCandidate();
				if (solution == null) {
					this.logger.info("No more solutions will be found. Terminating algorithm.");
					return terminate();
				}
				this.logger.info("Next solution found.");
				List<N> solutionPath = solution.getNodes();
				Plan plan = this.problemTransformer.getPlan(solutionPath);
				PlanFoundEvent<?, V> event = new PlanFoundEvent<>(new EvaluatedSearchGraphBasedPlan<>(plan.getActions(), solution.getScore(), solution));
				this.post(event);
				return event;
			}
			catch (NoSuchElementException e) { // if no more solution exists, terminate
				return terminate();
			}
		}
		default:
			throw new IllegalStateException("Don't know what to do in state " + this.getState());
		}
	}

	public IOptimalPathInORGraphSearch<ISearch, N, A, V> getSearch() {
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
			logger.info("Setting logger of problem transformer to {}", name + ".problemtransformer");
			((ILoggingCustomizable) this.problemTransformer).setLoggerName(name + ".problemtransformer");
		}
		if (this.search instanceof ILoggingCustomizable) {
			logger.info("Setting logger of search to {}", name + ".search");
			((ILoggingCustomizable) this.search).setLoggerName(name + ".search");
		}
		super.setLoggerName(this.loggerName + "._algorithm");
	}
}
