package jaicore.ml.multioptionsingleenhancer;

/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    Bagging.java
 *    Copyright (C) 1999-2012 University of Waikato, Hamilton, New Zealand
 *
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.evaluation.Evaluation;
import weka.core.AdditionalMeasureProducer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.PartitionGenerator;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;

/**
 *
 * @author nino
 *
 * This is an extended version of Bagging that does not only allow one base classifier with one configuration, but
 * multiple different configuration for one given classifier
 *
 * As one very easy notices, most of this code is copied from weka.classifiers.meta.Bagging
 * In fact all routines are very similar as in weka.classifiers.meta.Bagging. The only difference is, that
 * the array that is used in the iterations of buildingClassifer now is allowed to contain multiple different classifers.
 *
 * Note that unfortunately we had to copy this much code, because ExtendedBagging has to be a MultiClassifiersCombiner
 * to ensure that ML-Plan is able to work properly with this class
 *
 */
public class ExtendedBagging extends SingleEnhancerDerivedRandomizableMultipleClassifiersCombiner
implements WeightedInstancesHandler, AdditionalMeasureProducer, TechnicalInformationHandler, PartitionGenerator {

	//does not work in parallel anymore
	//not aggregatable anymore

	/** for serialization */
	private static final long serialVersionUID = -115879962237199703L;

	/** The size of each bag sample, as a percentage of the training size */
	protected int m_BagSizePercent = 100;

	/** Whether to calculate the out of bag error */
	protected boolean m_CalcOutOfBag = false;

	/** Whether to represent copies of instances using weights rather than explicitly */
	protected boolean m_RepresentUsingWeights = false;

	/** The evaluation object holding the out of bag error, etc. */
	protected Evaluation m_OutOfBagEvaluationObject = null;

	/** Whether to store the out of bag predictions in the evaluation object. */
	private boolean m_StoreOutOfBagPredictions = false;

	/** Whether to output complexity-based statistics when OOB-evaluation is performed. */
	private boolean m_OutputOutOfBagComplexityStatistics;

	/** Whether class is numeric. */
	private boolean m_Numeric = false;

	/** Whether to print individual ensemble members in output. */
	private boolean m_printClassifiers;

	/** Random number generator */
	protected Random m_random;

	/** Used to indicate whether an instance is in a bag or not */
	protected boolean[][] m_inBag;

	/** Reference to the training data */
	protected Instances m_data;

	/**
	 * Constructor.
	 */
	public ExtendedBagging() {

		super();
	}

	/**
	 * Returns a string describing classifier
	 *
	 * @return a description suitable for displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {

		return "Class for bagging a classifier to reduce variance. Can do classification " + "and regression depending on the base learner. \n\n" + "For more information, see\n\n"
				+ this.getTechnicalInformation().toString();
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed information about the
	 * technical background of this class, e.g., paper reference or book this class is based on.
	 *
	 * @return the technical information about this class
	 */
	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;

		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "Leo Breiman");
		result.setValue(Field.YEAR, "1996");
		result.setValue(Field.TITLE, "Bagging predictors");
		result.setValue(Field.JOURNAL, "Machine Learning");
		result.setValue(Field.VOLUME, "24");
		result.setValue(Field.NUMBER, "2");
		result.setValue(Field.PAGES, "123-140");

		return result;
	}

	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {

		Vector<Option> newVector = new Vector<>(4);

		newVector.addElement(new Option("\tSize of each bag, as a percentage of the\n" + "\ttraining set size. (default 100)", "P", 1, "-P"));
		newVector.addElement(new Option("\tCalculate the out of bag error.", "O", 0, "-O"));
		newVector
		.addElement(new Option("\tWhether to store out of bag predictions in internal evaluation object.", "store-out-of-bag-predictions", 0, "-store-out-of-bag-predictions"));
		newVector.addElement(new Option("\tWhether to output complexity-based statistics when out-of-bag evaluation is performed.", "output-out-of-bag-complexity-statistics", 0,
				"-output-out-of-bag-complexity-statistics"));
		newVector
		.addElement(new Option("\tRepresent copies of instances using weights rather than explicitly.", "represent-copies-using-weights", 0, "-represent-copies-using-weights"));
		newVector.addElement(new Option("\tPrint the individual classifiers in the output", "print", 0, "-print"));

		newVector.addAll(Collections.list(super.listOptions()));

		return newVector.elements();
	}

	/**
	 * Parses a given list of options.
	 * <p/>
	 *
	 * <!-- options-start --> Valid options are:
	 * <p/>
	 *
	 * <pre>
	 *  -P
	 *  Size of each bag, as a percentage of the
	 *  training set size. (default 100)
	 * </pre>
	 *
	 * <pre>
	 *  -O
	 *  Calculate the out of bag error.
	 * </pre>
	 *
	 * <pre>
	 *  -print
	 *  Print the individual classifiers in the output
	 * </pre>
	 *
	 * <pre>
	 *  -store-out-of-bag-predictions
	 *  Whether to store out of bag predictions in internal evaluation object.
	 * </pre>
	 *
	 * <pre>
	 *  -output-out-of-bag-complexity-statistics
	 *  Whether to output complexity-based statistics when out-of-bag evaluation is performed.
	 * </pre>
	 *
	 * <pre>
	 *  -represent-copies-using-weights
	 *  Represent copies of instances using weights rather than explicitly.
	 * </pre>
	 *
	 * <pre>
	 *  -S &lt;num&gt;
	 *  Random number seed.
	 *  (default 1)
	 * </pre>
	 *
	 * <pre>
	 *  -num-slots &lt;num&gt;
	 *  Number of execution slots.
	 *  (default 1 - i.e. no parallelism)
	 * </pre>
	 *
	 * <pre>
	 *  -I &lt;num&gt;
	 *  Number of iterations.
	 *  (default 10)
	 * </pre>
	 *
	 * <pre>
	 *  -D
	 *  If set, classifier is run in debug mode and
	 *  may output additional info to the console
	 * </pre>
	 *
	 * <pre>
	 *  -W
	 *  Full name of base classifier.
	 *  (default: weka.classifiers.trees.REPTree)
	 * </pre>
	 *
	 *
	 * <!-- options-end -->
	 *
	 * Options after -- are passed to the designated classifier.
	 * <p>
	 *
	 * @param options
	 *          the list of options as an array of strings
	 * @throws Exception
	 *           if an option is not supported
	 */
	@Override
	public void setOptions(final String[] options) throws Exception {

		String bagSize = Utils.getOption('P', options);
		if (bagSize.length() != 0) {
			this.setBagSizePercent(Integer.parseInt(bagSize));
		} else {
			this.setBagSizePercent(100);
		}

		this.setCalcOutOfBag(Utils.getFlag('O', options));

		this.setStoreOutOfBagPredictions(Utils.getFlag("store-out-of-bag-predictions", options));

		this.setOutputOutOfBagComplexityStatistics(Utils.getFlag("output-out-of-bag-complexity-statistics", options));

		this.setRepresentCopiesUsingWeights(Utils.getFlag("represent-copies-using-weights", options));

		this.setPrintClassifiers(Utils.getFlag("print", options));

		super.setOptions(options);

		Utils.checkForRemainingOptions(options);
	}

	/**
	 * Gets the current settings of the Classifier.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	@Override
	public String[] getOptions() {

		Vector<String> options = new Vector<>();

		options.add("-P");
		options.add("" + this.getBagSizePercent());

		if (this.getCalcOutOfBag()) {
			options.add("-O");
		}

		if (this.getStoreOutOfBagPredictions()) {
			options.add("-store-out-of-bag-predictions");
		}

		if (this.getOutputOutOfBagComplexityStatistics()) {
			options.add("-output-out-of-bag-complexity-statistics");
		}

		if (this.getRepresentCopiesUsingWeights()) {
			options.add("-represent-copies-using-weights");
		}

		if (this.getPrintClassifiers()) {
			options.add("-print");
		}

		Collections.addAll(options, super.getOptions());

		return options.toArray(new String[0]);
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the explorer/experimenter gui
	 */
	public String bagSizePercentTipText() {
		return "Size of each bag, as a percentage of the training set size.";
	}

	/**
	 * Gets the size of each bag, as a percentage of the training set size.
	 *
	 * @return the bag size, as a percentage.
	 */
	public int getBagSizePercent() {

		return this.m_BagSizePercent;
	}

	/**
	 * Sets the size of each bag, as a percentage of the training set size.
	 *
	 * @param newBagSizePercent
	 *          the bag size, as a percentage.
	 */
	public void setBagSizePercent(final int newBagSizePercent) {

		this.m_BagSizePercent = newBagSizePercent;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the explorer/experimenter gui
	 */
	public String representCopiesUsingWeightsTipText() {
		return "Whether to represent copies of instances using weights rather than explicitly.";
	}

	/**
	 * Set whether copies of instances are represented using weights rather than explicitly.
	 *
	 * @param representUsingWeights
	 *          whether to represent copies using weights
	 */
	public void setRepresentCopiesUsingWeights(final boolean representUsingWeights) {

		this.m_RepresentUsingWeights = representUsingWeights;
	}

	/**
	 * Get whether copies of instances are represented using weights rather than explicitly.
	 *
	 * @return whether copies of instances are represented using weights rather than explicitly
	 */
	public boolean getRepresentCopiesUsingWeights() {

		return this.m_RepresentUsingWeights;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the explorer/experimenter gui
	 */
	public String storeOutOfBagPredictionsTipText() {
		return "Whether to store the out-of-bag predictions.";
	}

	/**
	 * Set whether the out of bag predictions are stored.
	 *
	 * @param storeOutOfBag
	 *          whether the out of bag predictions are stored
	 */
	public void setStoreOutOfBagPredictions(final boolean storeOutOfBag) {

		this.m_StoreOutOfBagPredictions = storeOutOfBag;
	}

	/**
	 * Get whether the out of bag predictions are stored.
	 *
	 * @return whether the out of bag predictions are stored
	 */
	public boolean getStoreOutOfBagPredictions() {

		return this.m_StoreOutOfBagPredictions;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the explorer/experimenter gui
	 */
	public String calcOutOfBagTipText() {
		return "Whether the out-of-bag error is calculated.";
	}

	/**
	 * Set whether the out of bag error is calculated.
	 *
	 * @param calcOutOfBag
	 *          whether to calculate the out of bag error
	 */
	public void setCalcOutOfBag(final boolean calcOutOfBag) {

		this.m_CalcOutOfBag = calcOutOfBag;
	}

	/**
	 * Get whether the out of bag error is calculated.
	 *
	 * @return whether the out of bag error is calculated
	 */
	public boolean getCalcOutOfBag() {

		return this.m_CalcOutOfBag;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the explorer/experimenter gui
	 */
	public String outputOutOfBagComplexityStatisticsTipText() {

		return "Whether to output complexity-based statistics when out-of-bag evaluation is performed.";
	}

	/**
	 * Gets whether complexity statistics are output when OOB estimation is performed.
	 *
	 * @return whether statistics are calculated
	 */
	public boolean getOutputOutOfBagComplexityStatistics() {

		return this.m_OutputOutOfBagComplexityStatistics;
	}

	/**
	 * Sets whether complexity statistics are output when OOB estimation is performed.
	 *
	 * @param b
	 *          whether statistics are calculated
	 */
	public void setOutputOutOfBagComplexityStatistics(final boolean b) {

		this.m_OutputOutOfBagComplexityStatistics = b;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for displaying in the explorer/experimenter gui
	 */
	public String printClassifiersTipText() {
		return "Print the individual classifiers in the output";
	}

	/**
	 * Set whether to print the individual ensemble classifiers in the output
	 *
	 * @param print
	 *          true if the individual classifiers are to be printed
	 */
	public void setPrintClassifiers(final boolean print) {
		this.m_printClassifiers = print;
	}

	/**
	 * Get whether to print the individual ensemble classifiers in the output
	 *
	 * @return true if the individual classifiers are to be printed
	 */
	public boolean getPrintClassifiers() {
		return this.m_printClassifiers;
	}

	/**
	 * Gets the out of bag error that was calculated as the classifier was built. Returns error rate in
	 * classification case and mean absolute error in regression case.
	 *
	 * @return the out of bag error; -1 if out-of-bag-error has not be estimated
	 */
	public double measureOutOfBagError() {

		if (this.m_OutOfBagEvaluationObject == null) {
			return -1;
		}
		if (this.m_Numeric) {
			return this.m_OutOfBagEvaluationObject.meanAbsoluteError();
		} else {
			return this.m_OutOfBagEvaluationObject.errorRate();
		}
	}

	/**
	 * Returns an enumeration of the additional measure names.
	 *
	 * @return an enumeration of the measure names
	 */
	@Override
	public Enumeration<String> enumerateMeasures() {

		Vector<String> newVector = new Vector<>(1);
		newVector.addElement("measureOutOfBagError");
		return newVector.elements();
	}

	/**
	 * Returns the value of the named measure.
	 *
	 * @param additionalMeasureName
	 *          the name of the measure to query for its value
	 * @return the value of the named measure
	 * @throws IllegalArgumentException
	 *           if the named measure is not supported
	 */
	@Override
	public double getMeasure(final String additionalMeasureName) {

		if (additionalMeasureName.equalsIgnoreCase("measureOutOfBagError")) {
			return this.measureOutOfBagError();
		} else {
			throw new IllegalArgumentException(additionalMeasureName + " not supported (Bagging)");
		}
	}

	/**
	 * Returns a training set for a particular iteration.
	 *
	 * @param iteration
	 *          the number of the iteration for the requested training set.
	 * @return the training set for the supplied iteration number
	 * @throws Exception
	 *           if something goes wrong when generating a training set.
	 */
	protected synchronized Instances getTrainingSet(final int iteration) throws Exception {
		int bagSize = (int) (this.m_data.numInstances() * (this.m_BagSizePercent / 100.0));
		Instances bagData = null;
		Random r = new Random(this.m_Seed + iteration);

		// create the in-bag dataset
		if (this.m_CalcOutOfBag) {
			this.m_inBag[iteration] = new boolean[this.m_data.numInstances()];
			bagData = this.m_data.resampleWithWeights(r, this.m_inBag[iteration], this.getRepresentCopiesUsingWeights());
		} else {
			if (bagSize < this.m_data.numInstances()) {
				bagData = this.m_data.resampleWithWeights(r, false); // Need to turn off representation using weights in this case.
				bagData.randomize(r);
				Instances newBagData = new Instances(bagData, 0, bagSize);
				bagData = newBagData;
			} else {
				bagData = this.m_data.resampleWithWeights(r, this.getRepresentCopiesUsingWeights());
			}
		}

		return bagData;
	}

	/**
	 * Returns the out-of-bag evaluation object.
	 *
	 * @return the out-of-bag evaluation object; null if out-of-bag error hasn't been calculated
	 */
	public Evaluation getOutOfBagEvaluationObject() {

		return this.m_OutOfBagEvaluationObject;
	}

	/**
	 * Bagging method.
	 *
	 * @param data
	 *          the training data to be used for generating the bagged classifier.
	 * @throws Exception
	 *           if the classifier could not be built successfully
	 */
	@Override
	public void buildClassifier(final Instances data) throws Exception {

		// can classifier handle the data?
		this.getCapabilities().testWithFail(data);

		// Has user asked to represent copies using weights?
		if (this.getRepresentCopiesUsingWeights() && !(this.checkIfAnyClassifierIsInstanceOfWeightedInstancesHandler(this.m_Classifiers))) {
			throw new IllegalArgumentException("Cannot represent copies using weights when " + "base learner in bagging does not implement " + "WeightedInstancesHandler.");
		}

		// get fresh Instances object
		this.m_data = new Instances(data);

		super.buildClassifier(this.m_data);

		for(int i = 0; i < this.m_Classifiers.length; i++) {
			this.m_Classifiers[i].buildClassifier(this.m_data);
		}

		if (this.m_CalcOutOfBag && (this.m_BagSizePercent != 100)) {
			throw new IllegalArgumentException("Bag size needs to be 100% if " + "out-of-bag error is to be calculated!");
		}

		this.m_random = new Random(this.m_Seed);

		this.m_inBag = null;
		if (this.m_CalcOutOfBag) {
			this.m_inBag = new boolean[this.m_Classifiers.length][];
		}

		for (int j = 0; j < this.m_Classifiers.length; j++) {
			if (Thread.interrupted()) {
				throw new InterruptedException("Killed WEKA!");
			}
			if (this.m_Classifiers[j] instanceof Randomizable) {
				((Randomizable) this.m_Classifiers[j]).setSeed(this.m_random.nextInt());
			}
		}

		this.m_Numeric = this.m_data.classAttribute().isNumeric();

		// calc OOB error?
		if (this.getCalcOutOfBag()) {
			this.m_OutOfBagEvaluationObject = new Evaluation(this.m_data);

			for (int i = 0; i < this.m_data.numInstances(); i++) {
				if (Thread.interrupted()) {
					throw new InterruptedException("Killed WEKA!");
				}
				double[] votes;
				if (this.m_Numeric) {
					votes = new double[1];
				} else {
					votes = new double[this.m_data.numClasses()];
				}

				// determine predictions for instance
				int voteCount = 0;
				for (int j = 0; j < this.m_Classifiers.length; j++) {
					if (this.m_inBag[j][i]) {
						continue;
					}

					if (this.m_Numeric) {
						double pred = this.m_Classifiers[j].classifyInstance(this.m_data.instance(i));
						if (!Utils.isMissingValue(pred)) {
							votes[0] += pred;
							voteCount++;
						}
					} else {
						voteCount++;
						double[] newProbs = this.m_Classifiers[j].distributionForInstance(this.m_data.instance(i));
						// sum the probability estimates
						for (int k = 0; k < newProbs.length; k++) {
							votes[k] += newProbs[k];
						}
					}
				}

				// "vote"
				if (this.m_Numeric) {
					if (voteCount > 0) {
						votes[0] /= voteCount;
						this.m_OutOfBagEvaluationObject.evaluationForSingleInstance(votes, this.m_data.instance(i), this.getStoreOutOfBagPredictions());
					}
				} else {
					double sum = Utils.sum(votes);
					if (sum > 0) {
						Utils.normalize(votes, sum);
						this.m_OutOfBagEvaluationObject.evaluationForSingleInstance(votes, this.m_data.instance(i), this.getStoreOutOfBagPredictions());
					}
				}
			}
		} else {
			this.m_OutOfBagEvaluationObject = null;
		}

		// save memory
		this.m_inBag = null;
		this.m_data = new Instances(this.m_data, 0);
	}

	/**
	 * Calculates the class membership probabilities for the given test instance.
	 *
	 * @param instance
	 *          the instance to be classified
	 * @return preedicted class probability distribution
	 * @throws Exception
	 *           if distribution can't be computed successfully
	 */
	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {

		double[] sums = new double[instance.numClasses()], newProbs;

		double numPreds = 0;
		for (int i = 0; i < this.m_Classifiers.length; i++) {
			if (this.m_Numeric) {
				double pred = this.m_Classifiers[i].classifyInstance(instance);
				if (!Utils.isMissingValue(pred)) {
					sums[0] += pred;
					numPreds++;
				}
			} else {
				newProbs = this.m_Classifiers[i].distributionForInstance(instance);
				for (int j = 0; j < newProbs.length; j++) {
					sums[j] += newProbs[j];
				}
			}
		}
		if (this.m_Numeric) {
			if (numPreds == 0) {
				sums[0] = Utils.missingValue();
			} else {
				sums[0] /= numPreds;
			}
			return sums;
		} else if (Utils.eq(Utils.sum(sums), 0)) {
			return sums;
		} else {
			Utils.normalize(sums);
			return sums;
		}
	}

	/**
	 * Returns description of the bagged classifier.
	 *
	 * @return description of the bagged classifier as a string
	 */
	@Override
	public String toString() {

		if (this.m_Classifiers == null) {
			return "Bagging: No model built yet.";
		}
		StringBuffer text = new StringBuffer();
		text.append("Bagging with " + this.m_Classifiers.length + " iterations and base learners\n\n");
		for(int i = 0; i < this.m_Classifiers.length; i++) {
			text.append(this.getClassifierSpec(i) + "\n");
		}
		if (this.getPrintClassifiers()) {
			text.append("All the base classifiers: \n\n");
			for (int i = 0; i < this.m_Classifiers.length; i++) {
				text.append(this.m_Classifiers[i].toString() + "\n\n");
			}
		}
		if (this.m_CalcOutOfBag) {
			text.append(this.m_OutOfBagEvaluationObject.toSummaryString("\n\n*** Out-of-bag estimates ***\n", this.getOutputOutOfBagComplexityStatistics()));
		}

		return text.toString();
	}

	/**
	 * Builds the classifier to generate a partition.
	 */
	@Override
	public void generatePartition(final Instances data) throws Exception {

		if (this.checkIfAllClassifierIsInstanceOfPartitionGenerator(this.m_Classifiers)) {
			this.buildClassifier(data);
		} else {
			throw new Exception("At least one of the BaseClassifiers cannot generate a partition");
		}
	}

	/**
	 * Computes an array that indicates leaf membership
	 */
	@Override
	public double[] getMembershipValues(final Instance inst) throws Exception {

		if (this.checkIfAllClassifierIsInstanceOfPartitionGenerator(this.m_Classifiers)) {
			ArrayList<double[]> al = new ArrayList<>();
			int size = 0;
			for (int i = 0; i < this.m_Classifiers.length; i++) {
				double[] r = ((PartitionGenerator) this.m_Classifiers[i]).getMembershipValues(inst);
				size += r.length;
				al.add(r);
			}
			double[] values = new double[size];
			int pos = 0;
			for (double[] v : al) {
				System.arraycopy(v, 0, values, pos, v.length);
				pos += v.length;
			}
			return values;
		} else {
			throw new Exception("At least one of the BaseClassifiers cannot generate a partition");
		}
	}

	/**
	 * Returns the number of elements in the partition.
	 */
	@Override
	public int numElements() throws Exception {

		if (this.checkIfAllClassifierIsInstanceOfPartitionGenerator(this.m_Classifiers)) {
			int size = 0;
			for (int i = 0; i < this.m_Classifiers.length; i++) {
				size += ((PartitionGenerator) this.m_Classifiers[i]).numElements();
			}
			return size;
		} else {
			throw new Exception("At least one of the BaseClassifiers cannot generate a partition");
		}
	}

	/**
	 * Returns the revision string.
	 *
	 * @return the revision
	 */
	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 13186 $");
	}

	/**
	 * Main method for testing this class.
	 *
	 * @param argv
	 *          the options
	 */
	public static void main(final String[] argv) {
		runClassifier(new ExtendedBagging(), argv);
	}
}
