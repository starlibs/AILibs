package hasco.metamining.factories;

import hasco.metamining.MetaMinerBasedSorter;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearchFactory;
import jaicore.search.algorithms.standard.lds.ImprovedLimitedDiscrepancySearch;
import jaicore.search.structure.core.GraphGenerator;

public class ImprovedLimitedDiscrepancySearchFactory extends ORGraphSearchFactory<TFDNode, String, Double> {

	private MetaMinerBasedSorter sorter;
	
	public ImprovedLimitedDiscrepancySearchFactory(MetaMinerBasedSorter sorter) {
		this.sorter = sorter;
	}

	@Override
	public IObservableORGraphSearch<TFDNode, String, Double> createSearch(final GraphGenerator<TFDNode, String> graphGenerator,
			final INodeEvaluator<TFDNode, Double> nodeEvaluator, final int numberOfCPUs) {
		IObservableORGraphSearch<TFDNode, String, Double> search = new ImprovedLimitedDiscrepancySearch<TFDNode>(graphGenerator, sorter);
		return search;
	}
}
