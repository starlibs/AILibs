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
import de.upb.crc901.automl.pipeline.service.MLServicePipeline;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import hasco.serialization.ComponentLoader;
import jaicore.ml.classification.multiclass.reduction.PipelineOptimizer;
import scala.annotation.elidable;
import scala.util.parsing.combinator.testing.Str;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class PipelineEvaluator {
	
	
	
	public static void main(String[] args) {
		// load Components that can be used to create the pipeline
		ComponentLoader cl = new ComponentLoader();
		try {
			Util.loadClassifierComponents(cl);
			Util.loadPreprocessorComponents(cl);	
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		for (Component c : cl.getComponents()) {
			System.out.println(c.getName());
			
			for (Parameter p : c.getParameters()) {
				System.out.println("\t" + p.getName());
				
			}
			
		}
		
		// read args
			// Data path
			// Preprocessor (or null)
			// ... Parmas
			// Classifier
			// ... Params
			// result Path
		int index = 0;
		
		// read Data
		DataSource ds;
		Instances instances = null;
		try {
			ds = new DataSource(args[index++]);
			instances = new Instances(ds.getDataSet());
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
		
		// create Preprocessor
		Component prePorcessor = null;
		Map<String, String> prePorcessorParameter = new HashMap<>();
		
		String preProcessorName = args[index++];
		if(!preProcessorName.equals("null")) {
			for (Component c : cl.getComponents()) {
				if(c.getName().equals(preProcessorName)) {
					prePorcessor = c; 
				}
			}
			// add parameter
			for (Parameter p : prePorcessor.getParameters()) {
				prePorcessorParameter.put(p.getName(), args[index++]);
			}
		}
		
		// create Classifier
		Component classifier = null;
		String classifierName = args[index++];
		Map<String, String> classifierParameter = new HashMap<>();
		
		for (Component c : cl.getComponents()) {
			if(c.getName().equals(classifierName)) {
				classifier = c; 
			}
			// add parameter
			for (Parameter p : classifier.getParameters()) {
				classifierParameter.put(p.getName(), args[index++]);
			}
		}
		
		// build Pipeline
		MLServicePipeline mlPipeline = Util.createPipeline(prePorcessor, prePorcessorParameter, classifier, classifierParameter);
		
		// evaluate Pipeline
		double pctIncorrect = 100;
		try {
			Evaluation eval = new Evaluation(instances);
			
			eval.crossValidateModel(mlPipeline, instances, 10, new Random());
			
			pctIncorrect = eval.pctIncorrect();
			
		} catch (Exception e) {
			//TODO 
			e.printStackTrace();
		}
		
		// return result
		File resultFile = new File(args[index++]);
		try {
			resultFile.createNewFile();
			PrintStream out = new PrintStream(new FileOutputStream(resultFile));
			out.println(pctIncorrect);
			out.close();
		} catch (IOException e) {
			// TODO 
			e.printStackTrace();
		}
		
	}
	
}
