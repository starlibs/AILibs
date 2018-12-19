package jaicore.planning.algorithms;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.IAlgorithmListener;
import jaicore.planning.EvaluatedSearchGraphBasedPlan;
import jaicore.planning.algorithms.events.PlanFoundEvent;
import jaicore.planning.graphgenerators.IHierarchicalPlanningGraphGeneratorDeriver;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.builders.SearchProblemInputBuilder;

public class GraphSearchBasedHTNPlanningAlgorithm<PA extends Action, P extends IHTNPlanningProblem<?, ?, PA>, ISearch, OSearch, NSrc, ASrc, V extends Comparable<V>, NSearch, ASearch, L extends IAlgorithmListener>
		extends AAlgorithm<P, EvaluatedSearchGraphBasedPlan<PA, V, NSrc>> {

	private Logger logger = LoggerFactory.getLogger(GraphSearchBasedHTNPlanningAlgorithm.class);
	private String loggerName;

	/* algorithm inputs */
	private final IHierarchicalPlanningGraphGeneratorDeriver<?, ?, PA, P, NSrc, ASrc> problemTransformer;
	private final IGraphSearch<ISearch, OSearch, NSrc, ASrc, V, NSearch, ASearch> search;

	public GraphSearchBasedHTNPlanningAlgorithm(final P problem, final IHierarchicalPlanningGraphGeneratorDeriver<?, ?, PA, P, NSrc, ASrc> problemTransformer,
			final IGraphSearchFactory<ISearch, OSearch, NSrc, ASrc, V, NSearch, ASearch> searchFactory, final SearchProblemInputBuilder<NSrc, ASrc, ISearch> searchProblemBuilder) {
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
	public AlgorithmEvent nextWithException() throws AlgorithmExecutionCanceledException, InterruptedException {

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

			if (this.getLoggerName() != null && this.getLoggerName().length() > 0 && this.search instanceof ILoggingCustomizable) {
				this.logger.info("Customizing logger of search with {}", this.getLoggerName());
				((ILoggingCustomizable) this.search).setLoggerName(this.getLoggerName() + ".search");
			}
			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		}
		case active: {
			if (this.isCanceled()) {
				throw new IllegalStateException("The planner has already been canceled. Cannot compute more plans.");
			}
			this.logger.info("Starting/continuing search for next plan.");
			EvaluatedSearchGraphPath<NSrc, ASrc, V> solution = this.search.nextSolution();
			if (solution == null) {
				this.logger.info("No more solutions will be found. Terminating algorithm.");
				this.setState(AlgorithmState.inactive);
				return new AlgorithmFinishedEvent();
			}
			this.logger.info("Next solution found.");
			List<NSrc> solutionPath = solution.getNodes();
			Plan<PA> plan = this.problemTransformer.getPlan(solutionPath);
			PlanFoundEvent<PA, V> event = new PlanFoundEvent<>(new EvaluatedSearchGraphBasedPlan<>(plan.getActions(), solution.getScore(), solution));
			this.post(event);
			return event;
		}
		default:
			throw new IllegalStateException("Don't know what to do in state " + this.getState());
		}
	}

	public IGraphSearch<ISearch, OSearch, NSrc, ASrc, V, NSearch, ASearch> getSearch() {
		return this.search;
	}

	@Override
	public EvaluatedSearchGraphBasedPlan<PA, V, NSrc> call() throws Exception {
		return null;
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		if (this.search instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.problemTransformer).setLoggerName(name + ".problemtransformer");
		}
		if (this.search instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.search).setLoggerName(name + ".search");
		}
		super.setLoggerName(this.loggerName + "._algorithm");
	}
}
