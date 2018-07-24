package defaultEval.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
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
import jaicore.ml.classification.multiclass.reduction.PipelineOptimizer;
import scala.annotation.elidable;
import scala.util.parsing.combinator.testing.Str;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class PipelineEvaluator {
	
	//TODO remove
	static String pat = "F:\\Data\\Uni\\PG\\DefaultEvalEnvironment";
	
	public static void main(String[] args) {
		// TODO write result file even if no result
		
		try {
			// load Components that can be used to create the pipeline
			ComponentLoader cl_p = new ComponentLoader();
			ComponentLoader cl_c = new ComponentLoader();
			
			try {
				Util.loadClassifierComponents(cl_c);
				Util.loadPreprocessorComponents(cl_p);	
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			// read args
				// Data path
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
				ds = new DataSource(pat + "/datasets/" + args[index++] + ".arff");	// TODO keep path
				instances = new Instances(ds.getDataSet());
				instances.setClassIndex(instances.numAttributes()-1); // last one as class
			} catch (Exception e) {
				// TODO
				e.printStackTrace();
			}
			
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
							searcherParameter.put(p.getName(), args[index++]);
						}
					}
				}
				
				evaluatorName = args[index++];
				for (Component c : cl_p.getComponents()) {
					if(c.getName().equals(evaluatorName)) {
						evaluator = c; 
						
						// add parameter
						for (Parameter p : evaluator.getParameters()) {
							evaluatorParameter.put(p.getName(), args[index++]);
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
						classifierParameter.put(p.getName(), args[index++]);
					}
				}
			}
		
			System.out.println("Build Pipeline...");
			
			WEKAPipelineFactory factory = new WEKAPipelineFactory();
			ComponentInstance pipeline = Util.createPipeline(searcher, searcherParameter, evaluator, evaluatorParameter, classifier, classifierParameter);
			MLPipeline mlPipeline = factory.getComponentInstantiation(pipeline);
			
			System.out.println("Evaluate Pipeline...");
			
			// evaluate Pipeline
			double pctIncorrect = 100;
			double pctCorrect = 0;
			
			try {
				Evaluation eval = new Evaluation(instances);
				
				eval.crossValidateModel(mlPipeline, instances, 2, new Random());
				
				pctIncorrect = eval.pctIncorrect();
				pctCorrect = eval.pctCorrect();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// print result
			System.out.println("RESULT-INCORRECT: " + pctIncorrect);
			System.out.println("RESULT-CORRECT: " + pctCorrect);
			
			// return result
			File resultFile = new File(args[index++]);
			try {
				resultFile.createNewFile();
				PrintStream out = new PrintStream(new FileOutputStream(resultFile));
				out.println(pctIncorrect);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
