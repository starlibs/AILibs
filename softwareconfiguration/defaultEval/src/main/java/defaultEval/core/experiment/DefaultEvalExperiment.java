package defaultEval.core.experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.aeonbits.owner.ConfigCache;

import de.upb.crc901.automl.hascowekaml.WEKAPipelineFactory;
import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import defaultEval.core.DefaultOptimizer;
import defaultEval.core.Optimizer;
import defaultEval.core.SMACOptimizer;
import defaultEval.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import jaicore.basic.SQLAdapter;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetConfig;
import jaicore.experiments.IExperimentSetEvaluator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class DefaultEvalExperiment {
	
	
	
	// usable Components
	private static ComponentLoader classifierComponents = new ComponentLoader();
	private static ComponentLoader preProcessorComponents = new ComponentLoader();
		
		
	public static void main(String[] args) {
		try {
			Util.loadClassifierComponents(classifierComponents);
			Util.loadPreprocessorComponents(preProcessorComponents);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		DefaultEvalMCCConfig m = ConfigCache.getOrCreate(DefaultEvalMCCConfig.class);
		if (m.getDatasetFolder() == null || !m.getDatasetFolder().exists())
			throw new IllegalArgumentException("config specifies invalid dataset folder " + m.getDatasetFolder());

		ExperimentRunner runner = new ExperimentRunner(new IExperimentSetEvaluator() {

			@Override
			public IExperimentSetConfig getConfig() {
				return m;
			}

			@Override
			public void evaluate(ExperimentDBEntry experimentEntry, SQLAdapter adapter,
					IExperimentIntermediateResultProcessor processor) throws Exception {

				
				/* get experiment setup */
				Map<String, String> description = experimentEntry.getExperiment().getValuesOfKeyFields();
				String classifierName = description.get("classifier");
				String searcherName = description.get("searcher");
				String evaluatorName = description.get("evaluator");
				String optimizerName = description.get("optimizer");
				String datasetName = description.get("dataset");
				int seed = Integer.valueOf(description.get("seed"));
				
				
				Map<String, Object> results = new HashMap<>();
				Optimizer optimizer = null;
				
				// TODO fix if wrong input
				Component classifier = classifierComponents.getComponents().stream().filter(e -> e.getName().equals(classifierName)).findAny().get();
				Component searcher = preProcessorComponents.getComponents().stream().filter(e -> e.getName().equals(searcherName)).findAny().get();
				Component evaluator = preProcessorComponents.getComponents().stream().filter(e -> e.getName().equals(evaluatorName)).findAny().get();
				
				switch (optimizerName) {
				case "SMAC":
					optimizer = new SMACOptimizer(searcher, evaluator, classifier, datasetName, m.getEnvironment(), seed);
					break;

				case "default":
					optimizer = new DefaultOptimizer(searcher, evaluator, classifier, datasetName, m.getEnvironment(), seed);
					break;
					
				default:
					break;
				}
				
				optimizer.optimize();
				
				WEKAPipelineFactory factory = new WEKAPipelineFactory();
				double pctCorrect = Util.evaluate(Util.loadInstances(m.getDatasetFolder().getAbsolutePath(), datasetName), factory.getComponentInstantiation(Util.createPipeline(optimizer.getFinalSearcher(), optimizer.getFinalEvaluator(), optimizer.getFinalClassifier())));
				
				/* report results */
				results.put("pctCorrect", pctCorrect);
				processor.processResults(results);
			}
		});
		runner.randomlyConductExperiments(true);
	}

}
