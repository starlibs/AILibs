package de.upb.crc901.mlplan.classifiers;

import java.util.List;
import java.util.Random;

import de.upb.crc901.mlplan.core.MLPipeline;
import de.upb.crc901.mlplan.core.MLUtil;
import de.upb.crc901.mlplan.search.algorithms.GraphBasedPipelineSearcher;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.ceocstn.CEOCSTNUtil;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MCTSPipelineSearcher extends GraphBasedPipelineSearcher<TFDNode, String, Double> {

	public MCTSPipelineSearcher(Random random, int timeout, boolean showGraph) {
		super(random, timeout, showGraph);
	}

	@Override
	protected ORGraphSearch<TFDNode, String, Double> getSearch(Instances data) throws Exception {
		
//		/* setup search */
//		GraphGenerator<TFDNode, String> graphGenerator = MLUtil.getGraphGenerator(new File("testrsc/automl3.testset"), null, null, null);
//		Random r = new Random(0);
//		RandomCompletionEvaluator<Double> solutionEvaluator = new DoubleRandomCompletionEvaluator(r, 1, new SimpleSolutionEvaluator());
//		solutionEvaluator.setData(data);
//		MCTS<TFDNode,String,Double> search = new MCTS<>(graphGenerator, new UniformRandomPolicy<>(r), new UniformRandomPolicy<>(r), solutionEvaluator);
//		return search;
		return null;
	}

	@Override
	protected Classifier convertPathToPipeline(List<TFDNode> path) {
		try {
			return MLUtil.extractGeneratedClassifierFromPlan(CEOCSTNUtil.extractPlanFromSolutionPath(path));
		}
		catch (Throwable e) {
			throw new IllegalArgumentException("Converting path to pipeline caused " + e.getClass().getName() + " with message " + e.getMessage() + " for path " + path);
		}
	}

	@Override
	protected MLPipeline selectModel() {
		// TODO Auto-generated method stub
		return null;
	}

}
