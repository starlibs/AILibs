package jaicore.search.gui;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.IsLiveEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

public class ControllableBestFirst<T,A> extends BestFirst<T, A> implements ControllableSearch{

	public boolean live;

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
