package jaicore.search.gui;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.IsLiveEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;

/**
 * A test-implementation of the controllable search.
 * @author jkoepe
 *
 * @param <T>
 * @param <A>
 */
public class ControllableBestFirst<T,A> extends StandardBestFirst<T, A, Double> implements ControllableSearch{

	public boolean live;

	/**
	 * Create a new Search which contains a state for live play and replay.
	 * @param graphGenerator
	 * @param pNodeEvaluator
	 */
	public ControllableBestFirst(GeneralEvaluatedTraversalTree<T, A, Double> problem) {
		super(problem);
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
