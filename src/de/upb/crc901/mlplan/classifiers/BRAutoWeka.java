package de.upb.crc901.mlplan.classifiers;

import meka.classifiers.multilabel.BR;
import weka.classifiers.meta.AutoWEKAClassifier;
import weka.core.Instances;

public class BRAutoWeka extends BR {
	
	private final int seed;
	private final int timeout;
	private final int memoryInMB;
	
	public BRAutoWeka(int seed, int timeout, int memoryInMB) {
		this.seed = seed;
		this.timeout = timeout;
		this.memoryInMB = memoryInMB;
	}
	
	@Override
	public void buildClassifier(Instances data) throws Exception {
		AutoWEKAClassifier c = new AutoWEKAClassifier();
		c.setSeed(seed);
		int numberOfLabels = data.classIndex();
		int timeoutInMinutes = (int)Math.floor(timeout * 1f / numberOfLabels / 60);
		int memoryInMB = (int)Math.floor(this.memoryInMB * 1f / numberOfLabels);
		System.out.println("Timeout per AW: " + timeoutInMinutes);
		System.out.println("Memory per AW: " + memoryInMB);
		c.setTimeLimit(timeoutInMinutes);
		c.setParallelRuns(1);
		c.setMemLimit(memoryInMB);
		this.setClassifier(c);
		super.buildClassifier(data);
	}

//	private Classifier[] autoWEKA;
//
//	private Instances turnToSingleClassProblem(Instances data, int labelIndex) {
//		int numberOfLabels = data.classIndex();
//		Instances copy = new Instances(data);
//		for (int j = 0; j < numberOfLabels; j++) {
//			if (j < labelIndex)
//				copy.deleteAttributeAt(0);
//			else if (j > labelIndex)
//				copy.deleteAttributeAt(1);
//		}
//		copy.setClassIndex(0);
//		return copy;
//	}
//	
//	private class AutoWEKARunner implements Runnable {
//		AutoWEKAClassifier c;
//		Instances data;
//		int labelIndex;
//
//		public AutoWEKARunner(AutoWEKAClassifier c, Instances data, int labelIndex) {
//			super();
//			this.c = c;
//			this.data = data;
//			this.labelIndex = labelIndex;
//		}
//
//		@Override
//		public void run() {
//			try {
//				/* prepare dataset for single class classification */
//				Instances copy = turnToSingleClassProblem(data, labelIndex);
//
//				/* run classifier on reduced set */
//
//				System.out.print("Build AutoWEKA");
//				c.setSeed(labelIndex);
//				c.setTimeLimit(1);
//				c.setParallelRuns(1);
//				c.buildClassifier(copy);
//				System.out.println(" done");
//			} catch (Throwable e) {
//				e.printStackTrace();
//			}
//		}
//		
//	}
//
//	@Override
//	public void buildClassifier(Instances data) throws Exception {
//		int numberOfLabels = data.classIndex();
//		autoWEKA = new AutoWEKAClassifier[numberOfLabels];
//		ExecutorService pool = Executors.newFixedThreadPool(4);
//		for (int labelIndex = 0; labelIndex < numberOfLabels; labelIndex++) {
//			autoWEKA[labelIndex] = new AutoWEKAClassifier();
//			pool.submit(new AutoWEKARunner(autoWEKA[labelIndex], data, labelIndex));
//		}
//		pool.shutdown();
//		System.out.println("Awaiting shutdown ...");
//		pool.awaitTermination(10, TimeUnit.MINUTES);
//		System.out.println("Shutdown observed. Finished training");
//	}
//
//	@Override
//	public double classifyInstance(Instance instance) throws Exception {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	public double[] distributionForInstance(Instance instance) throws Exception {
//		double[] predictions = new double[autoWEKA.length];
//		for (int labelIndex = 0; labelIndex < autoWEKA.length; labelIndex ++) {
//			predictions[labelIndex] = autoWEKA[labelIndex].distributionForInstance(instance)[0];
//		}
//		System.out.println(Arrays.toString(predictions));
//		return predictions;
//	}
//
//	@Override
//	public Capabilities getCapabilities() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Enumeration<Option> listOptions() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setOptions(String[] options) throws Exception {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public String[] getOptions() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setDebug(boolean debug) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public boolean getDebug() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public String debugTipText() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getModel() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
