package jaicore.search.gui;

import com.google.common.eventbus.Subscribe;

import jaicore.graph.IControllableGraphAlgorithm;
import jaicore.graphvisualizer.events.controlEvents.AlgorithmEvent;
import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.IsLiveEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

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
	public void receiveControlEvent(ControlEvent event) {
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
}
