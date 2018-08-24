package jaicore.search.gui;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.IsLiveEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

/**
 * A test-implementation of the controllable search.
 * @author jkoepe
 *
 * @param <T>
 * @param <A>
 */
public class ControllableBestFirst<T,A> extends BestFirst<T, A, Double> implements ControllableSearch{

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


	@Override
	@Subscribe
	public void receiveControlEvent(ControlEvent event) {
		// TODO Auto-generated method stub

		if(event instanceof StepEvent && live) {
			int steps = ((StepEvent) event).getSteps();
			if(((StepEvent) event).forward()) {
				while(steps != 0) {
					this.next();
					steps --;
				}
			}
		}
		
		if(event instanceof NodePushed && live)
			this.step((Node<T, Double>) ((NodePushed) event).getNode());

		if(event instanceof IsLiveEvent)
			live = ((IsLiveEvent) event).isLive();
	}
	
	
}
