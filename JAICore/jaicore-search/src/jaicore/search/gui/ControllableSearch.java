package jaicore.search.gui;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.ResetEvent;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;

public class ControllableSearch<T,A> extends BestFirst<T, A> {

	public ControllableSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, Double> pNodeEvaluator) {
		super(graphGenerator, pNodeEvaluator);
		// TODO Auto-generated constructor stub
	}
	
	@Subscribe
	public void receiveControlEvent(ControlEvent event) {
		System.out.println(event);
		if(event instanceof ResetEvent) {
			this.next();
		}
	}

}
