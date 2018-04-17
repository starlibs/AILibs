package de.upb.crc901.mlplan.multilabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import de.upb.crc901.automl.pipeline.multilabel.MultilabelMLPipeline;
import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.classifiers.TwoPhaseHTNBasedPipelineSearcher;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.multilabel.evaluators.ExactMatchMultilabelEvaluator;
import jaicore.ml.multilabel.evaluators.MultilabelEvaluator;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import meka.classifiers.multilabel.BR;
import meka.core.MLUtils;
import weka.core.Instances;

public class MultiLabelTest {

	public static void main(String[] args) throws Throwable {
		// GraphGenerator<TFDNode,String> gg = MLUtil.getGraphGenerator(new File(""), null, null, null);
		// ORGraphSearch<TFDNode, String, Double> bf = new BestFirst<>(gg, n -> 1.0);
		// new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
		Instances data = new Instances(new BufferedReader(new FileReader("testrsc/multilabel/flags/flags.arff")));
		data.setRelationName(data.relationName() + ":-C -12");
		MLUtils.prepareData(data);
		int timeout = 60;

		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int seed = 0; seed < 1; seed++) {
			try {
				Random random = new Random(seed);
				List<Instances> split = WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, random, .7f));

				MultilabelEvaluator evaluator = new ExactMatchMultilabelEvaluator(random);
				
				// TwoPhasePipelineSearcher<Double> bs = new BalancedSearcher(random, 1000 * timeout);
				TwoPhaseHTNBasedPipelineSearcher<Double> bs = new TwoPhaseHTNBasedPipelineSearcher<>();
				
				bs.setHtnSearchSpaceFile(new File("testrsc/multilabel/mlplan-multilabel.searchspace"));
				// bs.setEvaluablePredicateFile(new File("testrsc/automl-reduction.evaluablepredicates"));
				bs.setRandom(random);
				bs.setTimeout(1000 * timeout);
				bs.setNumberOfCPUs(4);
				bs.setSolutionEvaluatorFactory4Search(() -> new MonteCarloCrossValidationEvaluator(evaluator, 3, .7f));
				bs.setSolutionEvaluatorFactory4Selection(() -> new MonteCarloCrossValidationEvaluator(evaluator, 10, .7f));
				bs.setRce(new DefaultPreorder(random, 3, new MonteCarloCrossValidationEvaluator(evaluator, 3, .7f)));
				bs.setTimeoutPerNodeFComputation(1000 * (timeout == 60 ? 15 : 300));
				bs.setTooltipGenerator(new TFDTooltipGenerator<>());
				bs.setPortionOfDataForPhase2(.7f);
				bs.buildClassifier(split.get(0));

				stats.addValue(evaluator.loss((MultilabelMLPipeline) bs.getSelectedModel(), split.get(1)));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(stats);
	}
}
