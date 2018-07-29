package defaultEval.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.upb.crc901.automl.hascocombinedml.MLServicePipelineFactory;
import de.upb.crc901.automl.hascowekaml.WEKAPipelineFactory;
import de.upb.crc901.automl.pipeline.basic.MLPipeline;
import de.upb.crc901.automl.pipeline.basic.SupervisedFilterSelector;
import de.upb.crc901.automl.pipeline.service.MLServicePipeline;
import de.upb.crc901.services.wrappers.WekaClassifierWrapper;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.serialization.ComponentLoader;
import jaicore.ml.WekaUtil;
import jaicore.ml.classification.multiclass.reduction.PipelineOptimizer;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import scala.annotation.elidable;
import scala.util.parsing.combinator.testing.Str;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class PipelineEvaluator {
	
	public static void main(String[] args) {
		// TODO write result file even if no result
		
		try {
			// load Components that can be used to create the pipeline
			ComponentLoader cl_p = new ComponentLoader();
			ComponentLoader cl_c = new ComponentLoader();
			
			try {
				cl_c.loadComponents(new File("models/weka-classifiers.json"));
				cl_p.loadComponents(new File("models/weka-preprocessors.json"));
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
			
			// read args
				// Data path
				// seed
				// Searcher (or null)
				// ... Parmas
				// Evaluator 
				// ... Parmas
				// Classifier
				// ... Params
				// result File
			int index = 0;
			
			// read Data
			DataSource ds;
			Instances instances = null;
			try {
				ds = new DataSource(args[index++]);	
				instances = new Instances(ds.getDataSet());
				instances.setClassIndex(instances.numAttributes()-1); // last one as class
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
			
			// get Seed
			int seed = Integer.valueOf(args[index++]);
			
			// create Searcher
			Component searcher = null;
			Map<String, String> searcherParameter = new HashMap<>();
			
			// create Evaluator
			Component evaluator = null;
			Map<String, String> evaluatorParameter = new HashMap<>();
			String evaluatorName = null;
						
			
			String searcherName = args[index++];
			if(!searcherName.equals("null")) {
				for (Component c : cl_p.getComponents()) {
					if(c.getName().equals(searcherName)) {
						searcher = c; 
						
						// add parameter
						for (Parameter p : searcher.getParameters()) {
							searcherParameter.put(args[index++], args[index++]);
						}
					}
				}
				
				evaluatorName = args[index++];
				for (Component c : cl_p.getComponents()) {
					if(c.getName().equals(evaluatorName)) {
						evaluator = c; 
						
						// add parameter
						for (Parameter p : evaluator.getParameters()) {
							evaluatorParameter.put(args[index++], args[index++]);
						}
					}
				}	
			}
			
			// create Classifier
			Component classifier = null;
			String classifierName = args[index++];
			Map<String, String> classifierParameter = new HashMap<>();
			
			for (Component c : cl_c.getComponents()) {
				if(c.getName().equals(classifierName)) {
					classifier = c; 
					// add parameter
					for (Parameter p : classifier.getParameters()) {
						classifierParameter.put(args[index++], args[index++]);
					}
				}
			}
		
			System.out.println("Build Pipeline...");
			
			WEKAPipelineFactory factory = new WEKAPipelineFactory();
			ComponentInstance pipeline = Util.createPipeline(searcher, searcherParameter, evaluator, evaluatorParameter, classifier, classifierParameter);
			MLPipeline mlPipeline = factory.getComponentInstantiation(pipeline);
			
			System.out.println("Evaluate Pipeline...");
			
			// evaluate Pipeline
			double loss = 1;
			
			List<Instances> instancesList =  WekaUtil.getStratifiedSplit(instances, new Random(seed), 0.7, 0.3);
			
			try {
				Evaluation eval = new Evaluation(instances);
				MulticlassEvaluator  multiclassEvaluator = new MulticlassEvaluator(new Random(seed));
				MonteCarloCrossValidationEvaluator crossValidationEvaluator = new MonteCarloCrossValidationEvaluator(multiclassEvaluator, 10, instancesList.get(0), 0.7f);
				
				
				loss = crossValidationEvaluator.evaluate(mlPipeline);
				
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
			
			// print result
			System.out.println("LOSS: " + loss);
			
			// return result
			File resultFile = new File(args[index++]);
			try {
				resultFile.createNewFile();
				PrintStream out = new PrintStream(new FileOutputStream(resultFile));
				out.println(loss);
				out.close();
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
			
			// print result for gga TODO use with others to
			System.out.println(loss);
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		
	}
	
}
