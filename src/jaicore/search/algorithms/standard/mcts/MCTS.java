package jaicore.search.algorithms.standard.mcts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graph.LabeledGraph;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.interfaces.solutionannotations.SolutionAnnotation;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * Best first algorithm implementation.
 *
 * @author Felix Mohr
 */
public class MCTS<T,A,V extends Comparable<V>> implements IObservableORGraphSearch<T,A,Double> {
	
	private static final Logger logger = LoggerFactory.getLogger(ORGraphSearch.class);

	/* communication */
	protected final GraphEventBus<Node<T, V>> graphEventBus = new GraphEventBus<>();
	protected final Map<T, Node<T, V>> ext2int = new HashMap<>();	

	protected final RootGenerator<T> rootGenerator;
	protected final SuccessorGenerator<T, A> successorGenerator;
	protected final boolean checkGoalPropertyOnEntirePath;
	protected final PathGoalTester<T> pathGoalTester;
	protected final NodeGoalTester<T> nodeGoalTester;

	protected final IPolicy<T,A,V> treePolicy;
	protected final IPolicy<T,A,V> defaultPolicy;
	protected final INodeEvaluator<T, V> playoutSimulator;

	private final T root;
	protected final LabeledGraph<T, A> exploredGraph;
	
	public MCTS(GraphGenerator<T, A> graphGenerator, IPolicy<T,A,V> treePolicy, IPolicy<T,A,V> defaultPolicy, INodeEvaluator<T, V> playoutSimulator) {
		super();
		this.rootGenerator = graphGenerator.getRootGenerator();
		this.successorGenerator = graphGenerator.getSuccessorGenerator();
		checkGoalPropertyOnEntirePath = !(graphGenerator.getGoalTester() instanceof NodeGoalTester);
		if (checkGoalPropertyOnEntirePath) {
			this.nodeGoalTester = null;
			this.pathGoalTester = (PathGoalTester<T>) graphGenerator.getGoalTester();
			;
		} else {
			this.nodeGoalTester = (NodeGoalTester<T>) graphGenerator.getGoalTester();
			this.pathGoalTester = null;
		}
		
		this.treePolicy = treePolicy;
		this.defaultPolicy = defaultPolicy;
		this.playoutSimulator = playoutSimulator;
		this.exploredGraph = new LabeledGraph<>();
		this.root = ((SingleRootGenerator<T>)rootGenerator).getRoot();
		this.exploredGraph.addItem(root);
	}
	
	@Override
	public void bootstrap(Collection<Node<T, Double>> nodes) {
		// TODO Auto-generated method stub
	}

	@Override
	public List<T> nextSolution() {
		
		/* walk tree until first unexpanded node */
		try {
			for (int i = 0; i < 10; i++) {
				System.out.println("-------------\nRun " + i + "\n--------------");
				List<T> path = getPlayout();
				V playout = playoutSimulator.f(new Node<>(null, path.get(path.size() - 1)));
				System.out.println(path.get(path.size() - 1));
				System.out.println(playout);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private List<T> getPlayout() throws Exception {
		T current = root;
		Collection<T> childrenOfCurrent;
		List<T> path = new ArrayList<>();
		path.add(current);
		
		while (!(childrenOfCurrent = exploredGraph.getSuccessors(current)).isEmpty()) {
			List<A> availableActions = new ArrayList<>();
			Map<A,T> successorStates = new HashMap<>();
			for (T child : childrenOfCurrent) {
				A action = exploredGraph.getEdgeLabel(current, child);
				availableActions.add(action);
				successorStates.put(action, child);
			}
//			System.out.println("Available actions of expanded node: " + availableActions);
			A chosenAction = treePolicy.getAction(current, availableActions);
			current = successorStates.get(chosenAction);
			path.add(current);
//			System.out.println("Chosen action: " + chosenAction + ". Successor: " + current);
		}
		
//		System.out.println("Reached child node of traveral tree. Continuing with default-policy.");
		
		/* use default policy to proceed to a goal node */
		while (!isGoal(current)) {
			Collection<NodeExpansionDescription<T, A>> availableActions = successorGenerator.generateSuccessors(current);
			Map<A,T> successorStates = new HashMap<>();
			if (availableActions.isEmpty()) {
				return path;
			}
			List<A> actions = new ArrayList<>();
			for (NodeExpansionDescription<T, A> d : availableActions) {
				successorStates.put(d.getAction(), d.getTo());
				exploredGraph.addItem(d.getTo());
				exploredGraph.addEdge(d.getFrom(), d.getTo(), d.getAction());
				actions.add(d.getAction());
			}
			current = successorStates.get(treePolicy.getAction(current, actions));
			path.add(current);
		}
		return path;
	}
	
	private boolean isGoal(T node) {
		return nodeGoalTester.isGoal(node);
	}

	@Override
	public Double getFValue(T node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getFValue(Node<T, Double> node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SolutionAnnotation<T, Double> getAnnotationOfReturnedSolution(List<T> solution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getFOfReturnedSolution(List<T> solution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancel() {
		
	}

	@Override
	public Node<T, Double> getInternalRepresentationOf(T node) {
		return null;
	}

	@Override
	public List<Node<T, Double>> getOpenSnapshot() {
		return null;
	}

	@Override
	public INodeEvaluator<T, Double> getNodeEvaluator() {
		return null;
	}

	@Override
	public GraphEventBus<Node<T, Double>> getEventBus() {
		return null;
	}
}