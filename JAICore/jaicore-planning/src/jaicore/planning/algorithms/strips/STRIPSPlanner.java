package jaicore.planning.algorithms.strips;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.graphvisualizer.gui.VisualizationWindow;
import jaicore.planning.EvaluatedPlan;
import jaicore.planning.algorithms.IPlanningAlgorithm;
import jaicore.planning.algorithms.events.PlanFoundEvent;
import jaicore.planning.graphgenerators.strips.forward.StripsForwardPlanningNode;
import jaicore.planning.graphgenerators.strips.forward.StripsTooltipGenerator;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.strips.StripsAction;
import jaicore.planning.model.strips.StripsOperation;
import jaicore.planning.model.strips.StripsPlanningProblem;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;

public class STRIPSPlanner<V extends Comparable<V>> implements IPlanningAlgorithm<StripsPlanningProblem, EvaluatedPlan<StripsAction, V>>, ILoggingCustomizable {
	private final StripsPlanningProblem problem;
	private IGraphSearch<GeneralEvaluatedTraversalTree<StripsForwardPlanningNode, String, V>, EvaluatedSearchGraphPath<StripsForwardPlanningNode, String, V>, StripsForwardPlanningNode, String, V, Node<StripsForwardPlanningNode,V>, String> search;
	private final INodeEvaluator<StripsForwardPlanningNode, V> nodeEvaluator;
	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(STRIPSPlanner.class);
	private final STRIPSForwardSearchReducer reducer = new STRIPSForwardSearchReducer();
	
	/* state of the algorithm */
	private boolean visualize = false;
	private AlgorithmState state = AlgorithmState.created;
	private boolean canceled = false;
	
	public STRIPSPlanner(StripsPlanningProblem problem, INodeEvaluator<StripsForwardPlanningNode, V> nodeEvaluator) {
		super();
		this.problem = problem;
		this.nodeEvaluator = nodeEvaluator;
		
		if (!problem.getInitState().getVariableParams().isEmpty())
			throw new IllegalArgumentException("The initial state contains variable parameters but must only contain constants!\nList of found variables: " + problem.getInitState().getVariableParams().stream().map(n -> "\n\t" + n.getName()).collect(Collectors.joining()));
		if (!problem.getGoalState().getVariableParams().isEmpty())
			throw new IllegalArgumentException("The goal state contains variable parameters but must only contain constants!\nList of found variables: " + problem.getGoalState().getVariableParams().stream().map(n -> "\n\t" + n.getName()).collect(Collectors.joining()));
	}

	@Override
	public StripsPlanningProblem getInput() {
		return problem;
	}

	@Override
	public void registerListener(Object listener) {
		
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		
	}

	@Override
	public int getNumCPUs() {
		return 0;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		
	}

	@Override
	public int getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TimeUnit getTimeoutUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		return state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		switch (state) {
		case created:
			state = AlgorithmState.active;
			
			/* logging problem */
			logger.info("Initializing planner for the following problem:\n\tOperations:{}\n\tInitial State: {}\n\tGoal State: {}", problem.getDomain().getOperations().stream().map(o -> "\n\t - " + o.getName() + "\n\t\tParams: " + o.getParams() + "\n\t\tPre: " + o.getPrecondition() + "\n\t\tAdd: " + ((StripsOperation)o).getAddList() + "\n\t\tDel: " + ((StripsOperation)o).getDeleteList()).collect(Collectors.joining()), problem.getInitState(), problem.getGoalState());
			
			/* create search algorithm */
			GraphGenerator<StripsForwardPlanningNode,String> gg = reducer.transform(problem);
			GeneralEvaluatedTraversalTree<StripsForwardPlanningNode, String, V> searchProblem = new GeneralEvaluatedTraversalTree<>(gg, nodeEvaluator);
			search = new BestFirst<GeneralEvaluatedTraversalTree<StripsForwardPlanningNode,String,V>, StripsForwardPlanningNode, String, V>(searchProblem);
			setLoggerOfSearch();
			if (visualize) {
				VisualizationWindow<Node<StripsForwardPlanningNode,V>,String> w = new VisualizationWindow<>(search);
				TooltipGenerator<StripsForwardPlanningNode> tt = new StripsTooltipGenerator<>();
				w.setTooltipGenerator(n -> tt.getTooltip(((Node<StripsForwardPlanningNode,V>)n).getPoint()));
			}
			return new AlgorithmInitializedEvent();
		case active:
			
			/* invoke next step in search algorithm*/
			if (!search.hasNext()) {
				state = AlgorithmState.inactive;
				return new AlgorithmFinishedEvent();
			}
			AlgorithmEvent e = search.next();
			if (e instanceof EvaluatedSearchSolutionCandidateFoundEvent) {
				Plan<StripsAction> plan = reducer.getPlan(((EvaluatedSearchSolutionCandidateFoundEvent<StripsForwardPlanningNode,String,V>) e).getSolutionCandidate().getNodes());
				return new PlanFoundEvent<>(new EvaluatedPlan<StripsAction, V>(plan, null));
			}
			else /* otherwise just return an empty event that indicates that "something" has been done */
				return new AlgorithmEvent() {
				};
			
		default:
			throw new IllegalStateException("Cannot handle algorithm state " + state);
		}
	}
	
	public void enableVisualization() {
		visualize = true;
	}

	@Override
	public EvaluatedPlan<StripsAction, V> call() throws Exception {
//		while (hasNext())
//			next();
		return null;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> getAnnotationsOfSolution(EvaluatedPlan<StripsAction, V> solution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoggerName(String name) {
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		setLoggerOfSearch();
	}
	
	private void setLoggerOfSearch() {
		if (this.search instanceof ILoggingCustomizable) {
			logger.info("Switching logger of search to {}.search", loggerName);
			((ILoggingCustomizable) this.search).setLoggerName(loggerName + ".search");
		}
		else if (search != null)
			logger.info("The search is of class {}, which is not logging customizable.", this.search.getClass());
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}
	
}
