package de.upb.crc901.taskconfigurator.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import de.upb.crc901.taskconfigurator.core.MLPipeline;
import de.upb.crc901.taskconfigurator.core.MLUtil;
import de.upb.crc901.taskconfigurator.search.algorithms.GraphBasedPipelineSearcher;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.ceocstn.CEOCSTNUtil;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.standard.rdfs.RandomizedDepthFirstSearch;
import weka.core.Instances;

/**
 * This class generates k random pipelines and picks the one that is best against a validation set
 * 
 * @author Felix
 *
 */
public class RandomPipelinePicker extends GraphBasedPipelineSearcher<TFDNode, String, Integer>{
	private final SerializableGraphGenerator<TFDNode, String> graphGenerator;
	public RandomPipelinePicker(File testsetFile, boolean showGraph, int numberOfSolutions, int selectionDepth, int timeout, Random random) throws IOException {
		super(random, timeout, showGraph);
		this.graphGenerator = MLUtil.getGraphGenerator(testsetFile, null);
	}

	@Override
	protected IObservableORGraphSearch<TFDNode, String, Integer> getSearch(Instances data) {		return new RandomizedDepthFirstSearch<>(graphGenerator, getRandom());
	}

	@Override
	protected MLPipeline convertPathToPipeline(List<TFDNode> path) {
		return MLUtil.extractPipelineFromPlan(CEOCSTNUtil.extractPlanFromSolutionPath(path));
	}

	@Override
	protected MLPipeline selectModel() {
		return solutions.peek();
	}
}