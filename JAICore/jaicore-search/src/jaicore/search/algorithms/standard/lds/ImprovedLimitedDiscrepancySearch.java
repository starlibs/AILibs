package jaicore.search.algorithms.standard.lds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graph.TreeNode;
import jaicore.graphvisualizer.events.GraphInitializedEvent;
import jaicore.graphvisualizer.events.NodeReachedEvent;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * Implementation of the algorithm presented in
 * 
 * @inproceedings{harvey1995,
 *  title={Limited discrepancy search},
 *  author={Harvey, William D and Ginsberg, Matthew L},
 *  booktitle={IJCAI (1)},
 *  pages={607--615},
 *  year={1995}
 * }
 * 
 * @author fmohr
 *
 */
public class ImprovedLimitedDiscrepancySearch<T> implements IObservableORGraphSearch<T, String, Double> {

	/* logging */
	private final Logger logger = LoggerFactory.getLogger(ImprovedLimitedDiscrepancySearch.class);
	
	/* communication */
	protected final GraphEventBus<TreeNode<T>> graphEventBus = new GraphEventBus<>();
	protected final Map<T, TreeNode<T>> ext2int = new ConcurrentHashMap<>();
	protected TreeNode<T> traversalTree;
	protected Collection<TreeNode<T>> open = new HashSet<>();
	protected Collection<TreeNode<T>> expanded = new HashSet<>();
	protected final Collection<T> solutions = new HashSet<>();
	
	/* graph construction helpers */
	protected final SingleRootGenerator<T> rootGenerator;
	protected final SuccessorGenerator<T, String> successorGenerator;
	protected final boolean checkGoalPropertyOnEntirePath;
	protected final PathGoalTester<T> pathGoalTester;
	protected final NodeGoalTester<T> nodeGoalTester;
	
	/* graph travesal helpers */
	protected final Comparator<T> heuristic;
	
	public ImprovedLimitedDiscrepancySearch(GraphGenerator<T, String> graphGenerator, Comparator<T> heuristic) {
		super();
		this.rootGenerator = (SingleRootGenerator<T>)graphGenerator.getRootGenerator();
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
		this.heuristic = heuristic;
	}
	
	@Override
	public void bootstrap(Collection<Node<T, Double>> nodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<T> nextSolution() {
		logger.info("Starting Limited Discrepancy Search");
		this.traversalTree = newNode(null, rootGenerator.getRoot());
		for (int x = 0; ; x++) {
			ldsProbe(traversalTree, x, n -> System.out.println("Found solution: " + n.getValuesOnPathFromRoot()));
		}
	}
	
	private interface ISolutionCallBack<T> {
		void processSolution(TreeNode<T> solution);
	}
	
	/**
	 * Computes a solution path that deviates k times from the heuristic (if possible)
	 * 
	 * @param node
	 * @param k
	 * @return
	 */
	private void ldsProbe(TreeNode<T> node, int k, ISolutionCallBack<T> callback) {
		logger.info("Probing under node {} with k = {}", node.getValue(), k);
		
		/* if we have a goal, return it */
		if (solutions.contains(node.getValue())) {
			callback.processSolution(node);
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/* if this node has not been expanded, compute successors and the priorities among them and attach them to search graph */
		if (!expanded.contains(node)) {
			expanded.add(node);
			Collection<NodeExpansionDescription<T, String>> succ = successorGenerator.generateSuccessors(node.getValue());
			if (succ == null || succ.isEmpty())
				return;
			List<NodeExpansionDescription<T, String>> prioSucc = succ.stream().sorted((d1,d2) -> heuristic.compare(d1.getTo(), d2.getTo())).collect(Collectors.toList());
			List<TreeNode<T>> generatedNodes = new ArrayList<>();
			for (NodeExpansionDescription<T, String> successorDescription : prioSucc) {
				TreeNode<T> newNode = newNode(node, successorDescription.getTo());
				generatedNodes.add(newNode);
			}
		}
		else
			logger.info("Not expanding node {} again.", node.getValue());
		List<TreeNode<T>> children = node.getChildren();
		if (children.isEmpty())
			return;
		
		/* otherwise, deviate from the heuristic if this brings a solution */
		/* if no more discrepancies are allowed, keep searching under the first child */
		if (k > 0 && children.size() > 1)
			ldsProbe(children.get(1), k - 1, callback);
		ldsProbe(children.get(0), 0, callback);
	}


	@Override
	public void registerListener(Object listener) {
		this.graphEventBus.register(listener);
	}
	
	protected synchronized TreeNode<T> newNode(TreeNode<T> parent, T newNode) {
		
		/* attach new node to traversal tree */
		TreeNode<T> newTree = parent != null ? parent.addChild(newNode) : new TreeNode<>(newNode);

		/* register node in map and create annotation object */
		ext2int.put(newNode, newTree);
		
		/* check solution property */
		boolean isGoal = nodeGoalTester.isGoal(newNode);
		if (isGoal)
			solutions.add(newNode);
		else
			open.add(newTree);

		/* send events for this new node */
		if (parent == null) {
			this.graphEventBus.post(new GraphInitializedEvent<TreeNode<T>>(newTree));
		} else {
			this.graphEventBus.post(new NodeReachedEvent<TreeNode<T>>(parent, newTree, "or_" + (isGoal ? "solution" : "created")));
		}
		return newTree;
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
	public Map<String, Object> getAnnotationsOfReturnedSolution(List<T> solution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAnnotationOfReturnedSolution(List<T> solution, String annotation) {
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public Node<T, Double> getInternalRepresentationOf(T node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node<T, Double>> getOpenSnapshot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INodeEvaluator<T, Double> getNodeEvaluator() {
		// TODO Auto-generated method stub
		return null;
	}
}
