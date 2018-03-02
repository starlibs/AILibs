package de.upb.crc901.mlplan.classifiers;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.upb.crc901.mlplan.core.SolutionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MonteCarloCrossValidationEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MulticlassEvaluator;
import jaicore.basic.MySQLAdapter;
import jaicore.ml.WekaUtil;
import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.UnsupportedAttributeTypeException;

public class BRGreedyParallelized implements MultiLabelClassifier {

//	private final MySQLExperimentLogger mysql;
	private final Map<Integer, Map<String, Double>> scores = new HashMap<>();
	Classifier[] baseClassifiers;
	
	

	@Override
	public String debugTipText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getDebug() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDebug(boolean arg0) {
		// TODO Auto-generated method stub

	}

	private Instances turnToSingleClassProblem(Instances data, int labelIndex) {
		int numberOfLabels = data.classIndex();
		Instances copy = new Instances(data);
		for (int j = 0; j < numberOfLabels; j++) {
			if (j < labelIndex)
				copy.deleteAttributeAt(0);
			else if (j > labelIndex)
				copy.deleteAttributeAt(1);
		}
		copy.setClassIndex(0);
		return copy;
	}

	private class IndividualClassifierRunner implements Runnable {
		Classifier c;
		Instances data;
		int labelIndex;

		public IndividualClassifierRunner(Classifier c, Instances data, int labelIndex) {
			super();
			this.c = c;
			this.data = data;
			this.labelIndex = labelIndex;
		}

		@Override
		public void run() {
			double score = -1;
			try {
				/* prepare dataset for single class classification */
				Instances copy = turnToSingleClassProblem(data, labelIndex);

				/* run classifier on reduced set */
				System.out.print("Build classifier");

				SolutionEvaluator eval = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(0)), 3, .7f);
				eval.setData(copy);
				score = eval.getSolutionScore(c);
				System.out.println(" done");
				System.out.println(scores);
			} catch (UnsupportedAttributeTypeException e) {
				
			} catch (Throwable e) {
				e.printStackTrace();
			}
			finally {
				synchronized (scores) {
					if (!scores.containsKey(labelIndex))
						scores.put(labelIndex, new HashMap<>());
					scores.get(labelIndex).put(c.getClass().getName(), score);
				}
			}
		}

	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		int numberOfLabels = data.classIndex();
		ExecutorService pool = Executors.newFixedThreadPool(8);
		Collection<String> classifiers = WekaUtil.getBasicLearners();
		for (int labelIndex = 0; labelIndex < numberOfLabels; labelIndex++) {
			for (String classifier : classifiers) {
				pool.submit(new IndividualClassifierRunner(AbstractClassifier.forName(classifier, null), data, labelIndex));
			}
		}
		pool.shutdown();
		System.out.println("Submitted " + numberOfLabels * classifiers.size() + " jobs. Awaiting shutdown ...");
		pool.awaitTermination(10, TimeUnit.MINUTES);
		System.out.println("Shutdown observed. Finished training");
	}

	@Override
	public double classifyInstance(Instance instance) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Capabilities getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Option> listOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public Classifier[] getBaseClassifiers() {
		return baseClassifiers;
	}
}
