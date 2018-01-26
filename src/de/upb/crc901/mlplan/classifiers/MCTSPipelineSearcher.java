package de.upb.crc901.mlplan.classifiers;

import java.io.File;
import java.util.List;
import java.util.Random;

import de.upb.crc901.mlplan.core.MLPipeline;
import de.upb.crc901.mlplan.core.MLUtil;
import de.upb.crc901.mlplan.search.algorithms.GraphBasedPipelineSearcher;
import de.upb.crc901.mlplan.search.evaluators.DoubleRandomCompletionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.RandomCompletionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.SimpleSolutionEvaluator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.ceocstn.CEOCSTNUtil;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.standard.mcts.MCTS;
import jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import jaicore.search.structure.core.GraphGenerator;
import weka.core.Instances;

public class MCTSPipelineSearcher extends GraphBasedPipelineSearcher<TFDNode, String, Double> {

	public MCTSPipelineSearcher(Random random, int timeout, boolean showGraph) {
		super(random, timeout, showGraph);
	}

	@Override
	protected IObservableORGraphSearch<TFDNode, String, Double> getSearch(Instances data) throws Exception {
		
		/* setup search */
		GraphGenerator<TFDNode, String> graphGenerator = MLUtil.getGraphGenerator(new File("testrsc/automl3.testset"), null, null);
		Random r = new Random(0);
		RandomCompletionEvaluator<Double> solutionEvaluator = new DoubleRandomCompletionEvaluator(r, 1, new SimpleSolutionEvaluator());
		solutionEvaluator.setData(data);
		MCTS<TFDNode,String,Double> search = new MCTS<>(graphGenerator, new UniformRandomPolicy<>(r), new UniformRandomPolicy<>(r), solutionEvaluator);
		return search;
	}

	@Override
	protected MLPipeline convertPathToPipeline(List<TFDNode> path) {
		return MLUtil.extractPipelineFromPlan(CEOCSTNUtil.extractPlanFromSolutionPath(path));
	}

	@Override
	protected MLPipeline selectModel() {
		// TODO Auto-generated method stub
		return null;
	}

}
