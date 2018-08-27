package jaicore.search.gui;

import com.google.common.eventbus.Subscribe;

import jaicore.graph.IControllableGraphAlgorithm;
import jaicore.graphvisualizer.events.controlEvents.AlgorithmEvent;
import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.IsLiveEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.ScoreNode;

import java.util.stream.Collectors;

/**
 * A test-implementation of the controllable search.
 * @author jkoepe
 *
 * @param <T>
 * @param <A>
 */
public class ControllableBestFirst<T,A> extends BestFirst<T, A> implements IControllableGraphAlgorithm<T,A> {

	public boolean live;

	/**
	 * Create a new Search which contains a state for live play and replay.
	 * @param graphGenerator
	 * @param pNodeEvaluator
	 */
	public ControllableBestFirst(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Double> pNodeEvaluator) {
		super(graphGenerator, pNodeEvaluator);
		// TODO Auto-generated constructor stub
		this.live = false;

	}



	@Subscribe
	public void receiveControlEvent(ControlEvent event) throws Exception {
		// TODO Auto-generated method stub
		
		if(event instanceof AlgorithmEvent) {
			if(((AlgorithmEvent) event).getNode()==null) 
				this.next();
			else
				this.step((Node<T, Double>) ((AlgorithmEvent) event).getNode());
			
		}
	}


	@Override
	public void step() {
		super.step();
	}

	@Override
	public void initGraph() throws Throwable {
		super.initGraph();
	}

	@Override
	public void step(Object node){
		super.step((Node<T, Double>) node);
	}


	@Override
	protected synchronized Node<T, Double> newNode(Node<T, Double> parent, T t2, Double evaluation) {
		assert parent == null || expanded.contains(parent.getPoint()) : "Generating successors of an unexpanded node " + parent + ". List of expanded nodes:\n" + expanded.stream().map(n -> "\n\t" + n.toString()).collect(Collectors.joining());
		assert !open.contains(parent) : "Parent node " + parent + " is still on OPEN, which must not be the case!";

		/* create new node and check whether it is a goal */
//		Node<T, V> newNode = new Node<>(parent, t2);
		ScoreNode<T> newNode = new ScoreNode<>(parent, t2);
		if (evaluation != null)
			newNode.setInternalLabel(evaluation);

		/* check loop */
		assert parent == null || !parent.externalPath().contains(t2) : "There is a loop in the underlying graph. The following path contains the last node twice: " + newNode.externalPath().stream().map(n -> n.toString()).reduce("", (s,t) -> s + "\n\t\t" + t);

		/* currently, we only support tree search */
		assert !ext2int.containsKey(t2) : "Reached node " + t2 + " for the second time.\nt\tFirst path:" + ext2int.get(t2).externalPath().stream().map(n -> n.toString()).reduce("", (s,t) -> s + "\n\t\t" + t)
				+ "\n\tSecond Path:" + newNode.externalPath().stream().map(n -> n.toString()).reduce("", (s,t) -> s + "\n\t\t" + t);

		/* register node in map and create annotation object */
		ext2int.put(t2, newNode);

		/* detect whether node is solution */
		if (checkGoalPropertyOnEntirePath ? pathGoalTester.isGoal(newNode.externalPath()) : nodeGoalTester.isGoal(newNode.getPoint()))
			newNode.setGoal(true);

		/* send events for this new node */
		if (parent == null) {
			this.graphEventBus.post(new GraphInitializedEvent<Node<T, Double>>(newNode));
		} else {
			this.graphEventBus.post(new NodeReachedEvent<Node<T, Double>>(parent, newNode, "or_" + (newNode.isGoal() ? "solution" : "created")));
			logger.debug("Sent message for creation of node {} as a successor of {}", newNode, parent);
		}

		return newNode;
	}
}
