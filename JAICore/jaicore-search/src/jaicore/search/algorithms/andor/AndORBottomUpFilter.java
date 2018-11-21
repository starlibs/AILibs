package jaicore.search.algorithms.andor;

import java.util.LinkedList;
import java.util.Queue;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.graph.IGraphAlgorithm;
import jaicore.graph.LabeledGraph;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;

public class AndORBottomUpFilter<N, A, V extends Comparable<V>> extends AAlgorithm<GraphGenerator<N, A>, Integer>
		implements IGraphAlgorithm<GraphGenerator<N, A>, Integer, N, A> {
	private final LabeledGraph<N, A> graph = new LabeledGraph<>();

	public AndORBottomUpFilter(GraphGenerator<N, A> gg) {
		super(gg);
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (getState()) {
		case created: {
			/* step 1: construct the whole graph */
			Queue<N> open = new LinkedList<>();
			N root = ((SingleRootGenerator<N>) getInput().getRootGenerator()).getRoot();
			open.add(root);
			post(new GraphInitializedEvent<N>(root));
			graph.addItem(root);
			while (!open.isEmpty()) {
				N n = open.poll();
				for (NodeExpansionDescription<N, A> descr : getInput().getSuccessorGenerator().generateSuccessors(n)) {
					graph.addItem(descr.getTo());
					graph.addEdge(n, descr.getTo(), descr.getAction());
					open.add(descr.getTo());
				}
			}

			System.out.println(graph.getItems().size());
			setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		}
		default:
			throw new IllegalStateException("No handler defined for state " + getState());
		}

	}

	@Override
	public Integer call() throws Exception {
		while (hasNext())
			nextWithException();
		return 1;
	}

}
