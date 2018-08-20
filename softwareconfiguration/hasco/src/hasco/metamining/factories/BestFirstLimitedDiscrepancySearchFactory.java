package hasco.metamining.factories;

import hasco.metamining.MetaMinerBasedSorter;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.algorithms.standard.core.ORGraphSearchFactory;
import jaicore.search.algorithms.standard.lds.BestFirstLimitedDiscrepancySearch;
import jaicore.search.algorithms.standard.lds.NodeOrderList;
import jaicore.search.structure.core.GraphGenerator;

public class BestFirstLimitedDiscrepancySearchFactory<A> extends ORGraphSearchFactory<TFDNode, A, NodeOrderList> {
	
	private MetaMinerBasedSorter sorter;
	
	public BestFirstLimitedDiscrepancySearchFactory(MetaMinerBasedSorter sorter) {
		this.sorter = sorter;
	}

	@Override
	public IObservableORGraphSearch<TFDNode, A, NodeOrderList> createSearch(final GraphGenerator<TFDNode, A> graphGenerator,
			final INodeEvaluator<TFDNode, NodeOrderList> nodeEvaluator, final int numberOfCPUs) {
		ORGraphSearch<TFDNode, A, NodeOrderList> search = new BestFirstLimitedDiscrepancySearch<TFDNode, A>(graphGenerator, sorter);
		search.parallelizeNodeExpansion(numberOfCPUs);
		search.setTimeoutForComputationOfF(this.timeoutForFInMS, this.timeoutEvaluator);
		if (loggerName != null && loggerName.length() > 0)
			search.setLoggerName(loggerName);
		return search;
	}
}
