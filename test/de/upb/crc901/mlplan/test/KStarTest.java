package de.upb.crc901.mlplan.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import jaicore.ml.WekaUtil;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.OneRAttributeEval;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.attributeSelection.SymmetricalUncertAttributeEval;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.KStar;
import weka.core.Instances;

public class KStarTest {

	@Test
	public void test() throws Exception {
		System.out.print("Reading in data ...");
		Instances data = new Instances(new BufferedReader(new FileReader("testrsc/autowekasets/glass.arff")));
		System.out.println("Done");
		data.setClassIndex(data.numAttributes() - 1);
		
		ASSearch[] searchers = { new Ranker(), new BestFirst(), new GreedyStepwise()};
		ASEvaluation[] evaluators = { new CfsSubsetEval(), new InfoGainAttributeEval(), new PrincipalComponents(), new CorrelationAttributeEval(), new GainRatioAttributeEval(), new OneRAttributeEval(), new ReliefFAttributeEval(), new SymmetricalUncertAttributeEval()  };
		
		
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);
		for (ASSearch searcher : searchers) {
			for (ASEvaluation evaluator : evaluators) {
				String searcherName = searcher.getClass().getSimpleName();
				String evaluatorName = evaluator.getClass().getSimpleName();
				System.out.print(searcherName + "&" + evaluatorName + " ... ");
				if (searcherName.equals("Ranker")) {
					if (evaluatorName.equals("CfsSubsetEval")) {
						System.out.println("ignored");
						continue;
					}
				}
				if ((evaluatorName.equals("InfoGainAttributeEval") || evaluatorName.equals("PrincipalComponents") || evaluatorName.equals("CorrelationAttributeEval") || evaluatorName.equals("GainRatioAttributeEval") || evaluatorName.equals("OneRAttributeEval") || evaluatorName.equals("ReliefFAttributeEval") || evaluatorName.equals("SymmetricalUncertAttributeEval")) && !searcherName.equals("Ranker")) {
					System.out.println("ignored");
					continue;
				}
				KStar kstar = new KStar();
				MLPipeline pl = new MLPipeline(searcher, evaluator, kstar);
				pl.buildClassifier(split.get(0));
				Evaluation eval = new Evaluation(split.get(0));
				eval.evaluateModel(pl, split.get(1));
				System.out.println("checked");
			}
		}

	}

}
