package jaicore.ml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.SimpleRandomSampling;
import jaicore.ml.core.dataset.sampling.WekaInstancesUtil;
import jaicore.ml.core.dataset.standard.SimpleDataset;
import jaicore.ml.core.dataset.standard.SimpleInstance;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import jaicore.ml.openml.OpenMLHelper;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * 
 * @author jnowack
 *
 */
public class LearningCurveTestDataGenerator {

	public static int[] anchors = new int[] {1,2,4,8,16, 32, 64, 128};
	
	
	public static void main(String[] args) throws UnsupportedAttributeTypeException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, Exception {
		
//		anchors = new int[10];
//		for (int i = 0; i < anchors.length; i++) {
//			anchors[i] = (i+1)*2;
//		}
//		
		Random rand = new Random(42);
		
		StringBuilder xString = new StringBuilder("x <- c(");
		StringBuilder yString = new StringBuilder("y <- c(");
		int i = 0;		
		for (int anchor : anchors) {
			if(i != 0) {
				xString.append(",");
				yString.append(",");
			}
			xString.append(anchor);
				
			yString.append(""+(Math.log(2* Math.log(anchor) + 1) + rand.nextGaussian()*0.01));
			i++;
		}
		xString.append(")");
		yString.append(")");
		
		
		System.out.println("N <- " + anchors.length);
		System.out.println(xString.toString());
		System.out.println(yString.toString());
	}	
}
