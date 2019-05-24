package jaicore.ml.multioptionsingleenhancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.IterativeClassifier;
import weka.classifiers.MultipleClassifiersCombiner;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.UnassignedClassException;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;

public class ExtendedAdditiveRegression extends MultipleClassifiersCombiner implements OptionHandler, AdditionalMeasureProducer, WeightedInstancesHandler, TechnicalInformationHandler, IterativeClassifier {

	/** for serialization */
	private static final long serialVersionUID = -2368937577670527151L;

	/**
	 * ArrayList for storing the generated base classifiers.
	 * Note: we are hiding the variable from IteratedSingleClassifierEnhancer
	 */
	protected ArrayList<Classifier> m_ClassifiersArrayList;

	/** Shrinkage (Learning rate). Default = no shrinkage. */
	protected double m_shrinkage = 1.0;

	/** The mean or median */
	protected double m_InitialPrediction;

	/** whether we have suitable data or nor (if only mean/mode is used) */
	protected boolean m_SuitableData = true;

	/** The working data */
	protected Instances m_Data;

	/** The sum of (absolute or squared) residuals. */
	protected double m_Error;

	/** The improvement in the sum of (absolute or squared) residuals. */
	protected double m_Diff;

	/** Whether to minimise absolute error instead of squared error. */
	protected boolean m_MinimizeAbsoluteError = false;

	/** Iterator used for next() function, because in this case m_NumIterations does not exists but one has to know how many iterations have to be performed */
	private int next_iterator;

	/**
	 * Returns a string describing this attribute evaluator
	 *
	 * @return a description of the evaluator suitable for
	 *         displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {
		return " Meta classifier that enhances the performance of a regression " + "base classifier. Each iteration fits a model to the residuals left " + "by the classifier on the previous iteration. Prediction is "
				+ "accomplished by adding the predictions of each classifier. " + "Reducing the shrinkage (learning rate) parameter helps prevent " + "overfitting and has a smoothing effect but increases the learning " + "time.\n\n"
				+ "For more information see:\n\n" + this.getTechnicalInformation().toString();
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing
	 * detailed information about the technical background of this class,
	 * e.g., paper reference or book this class is based on.
	 *
	 * @return the technical information about this class
	 */
	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;

		result = new TechnicalInformation(Type.TECHREPORT);
		result.setValue(Field.AUTHOR, "J.H. Friedman");
		result.setValue(Field.YEAR, "1999");
		result.setValue(Field.TITLE, "Stochastic Gradient Boosting");
		result.setValue(Field.INSTITUTION, "Stanford University");
		result.setValue(Field.PS, "http://www-stat.stanford.edu/~jhf/ftp/stobst.ps");

		return result;
	}

	/**
	 * Default constructor specifying DecisionStump as the classifier
	 */
	public ExtendedAdditiveRegression() {

		super();
	}

	/**
	 * String describing default classifier.
	 *
	 * @return the default classifier classname
	 */
	protected String defaultClassifierString() {

		return "weka.classifiers.trees.DecisionStump";
	}

	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {

		Vector<Option> newVector = new Vector<Option>(2);

		newVector.addElement(new Option("\tSpecify shrinkage rate. (default = 1.0, i.e., no shrinkage)", "S", 1, "-S"));

		newVector.addElement(new Option("\tMinimize absolute error instead of squared error (assumes that base learner minimizes absolute error).", "A", 0, "-A"));

		newVector.addAll(Collections.list(super.listOptions()));

		return newVector.elements();
	}

	/**
	 * Parses a given list of options.
	 * <p/>
	 *
	 * <!-- options-start -->
	 * Valid options are:
	 * <p/>
	 *
	 * <pre>
	 *  -S
	 *  Specify shrinkage rate. (default = 1.0, ie. no shrinkage)
	 * </pre>
	 *
	 * <pre>
	 *  -A
	 *  Minimize absolute error instead of squared error (assumes that base learner minimizes absolute error).
	 *
	 * <pre> -D
	 *  If set, classifier is run in debug mode and
	 *  may output additional info to the console
	 * </pre>
	 *
	 * <pre>
	 * -B classifierstring <br>
	 * Classifierstring should contain the full class name of a scheme
	 * included for selection followed by options to the classifier
	 * (required, option should be used once for each classifier).
	 * </pre>
	 *
	 * <!-- options-end -->
	 *
	 * @param options the list of options as an array of strings
	 * @throws Exception if an option is not supported
	 */
	@Override
	public void setOptions(final String[] options) throws Exception {

		String optionString = Utils.getOption('S', options);
		if (optionString.length() != 0) {
			Double temp = Double.valueOf(optionString);
			this.setShrinkage(temp.doubleValue());
		}
		this.setMinimizeAbsoluteError(Utils.getFlag('A', options));

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

		Vector<String> options = new Vector<String>();

		options.add("-S");
		options.add("" + this.getShrinkage());

		if (this.getMinimizeAbsoluteError()) {
			options.add("-A");
		}

		Collections.addAll(options, super.getOptions());

		return options.toArray(new String[0]);
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for
	 *         displaying in the explorer/experimenter gui
	 */
	public String shrinkageTipText() {
		return "Shrinkage rate. Smaller values help prevent overfitting and " + "have a smoothing effect (but increase learning time). " + "Default = 1.0, ie. no shrinkage.";
	}

	/**
	 * Set the shrinkage parameter
	 *
	 * @param l the shrinkage rate.
	 */
	public void setShrinkage(final double l) {
		this.m_shrinkage = l;
	}

	/**
	 * Get the shrinkage rate.
	 *
	 * @return the value of the learning rate
	 */
	public double getShrinkage() {
		return this.m_shrinkage;
	}

	/**
	 * Returns the tip text for this property
	 *
	 * @return tip text for this property suitable for
	 *         displaying in the explorer/experimenter gui
	 */
	public String minimizeAbsoluteErrorTipText() {
		return "Minimize absolute error instead of squared error (assume base learner minimizes absolute error)";
	}

	/**
	 * Sets whether absolute error is to be minimized.
	 *
	 * @param f true if absolute error is to be minimized.
	 */
	public void setMinimizeAbsoluteError(final boolean f) {
		this.m_MinimizeAbsoluteError = f;
	}

	/**
	 * Gets whether absolute error is to be minimized.
	 *
	 * @return true if absolute error is to be minimized
	 */
	public boolean getMinimizeAbsoluteError() {
		return this.m_MinimizeAbsoluteError;
	}

	/**
	 * Returns default capabilities of the classifier.
	 *
	 * @return the capabilities of this classifier
	 */
	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();

		// class
		result.disableAllClasses();
		result.disableAllClassDependencies();
		result.enable(Capability.NUMERIC_CLASS);
		result.enable(Capability.DATE_CLASS);

		return result;
	}

	/**
	 * Method used to build the classifier.
	 */
	@Override
	public void buildClassifier(final Instances data) throws Exception {

		// Initialize classifier
		this.initializeClassifier(data);

		this.next_iterator = 0;

		// For the given number of iterations
		while (this.next()) {
		}
		;

		// Clean up
		this.done();
	}

	/**
	 * Initialize classifier.
	 *
	 * @param data the training data
	 * @throws Exception if the classifier could not be initialized successfully
	 */
	@Override
	public void initializeClassifier(final Instances data) throws Exception {

		// can classifier handle the data?
		this.getCapabilities().testWithFail(data);

		// remove instances with missing class
		this.m_Data = new Instances(data);
		this.m_Data.deleteWithMissingClass();

		// Add the model for the mean first
		if (this.getMinimizeAbsoluteError()) {
			this.m_InitialPrediction = this.m_Data.kthSmallestValue(this.m_Data.classIndex(), this.m_Data.numInstances() / 2);
		} else {
			this.m_InitialPrediction = this.m_Data.meanOrMode(this.m_Data.classIndex());
		}

		// only class? -> use only ZeroR model
		if (this.m_Data.numAttributes() == 1) {
			System.err.println("Cannot build non-trivial model (only class attribute present in data!).");
			this.m_SuitableData = false;
			return;
		} else {
			this.m_SuitableData = true;
		}

		// Initialize list of classifiers and data
		this.m_ClassifiersArrayList = new ArrayList<Classifier>(this.m_Classifiers.length);
		this.m_Data = this.residualReplace(this.m_Data, this.m_InitialPrediction);

		// Calculate error
		this.m_Error = 0;
		this.m_Diff = Double.MAX_VALUE;
		for (int i = 0; i < this.m_Data.numInstances(); i++) {
			if (this.getMinimizeAbsoluteError()) {
				this.m_Error += this.m_Data.instance(i).weight() * Math.abs(this.m_Data.instance(i).classValue());
			} else {
				this.m_Error += this.m_Data.instance(i).weight() * this.m_Data.instance(i).classValue() * this.m_Data.instance(i).classValue();
			}
		}
		if (this.m_Debug) {
			if (this.getMinimizeAbsoluteError()) {
				System.err.println("Sum of absolute residuals (predicting the median) : " + this.m_Error);
			} else {
				System.err.println("Sum of squared residuals (predicting the mean) : " + this.m_Error);
			}
		}
	}

	/**
	 * Perform another iteration.
	 */
	@Override
	public boolean next() throws Exception {

		if ((!this.m_SuitableData) || (this.m_ClassifiersArrayList.size() <= this.next_iterator) || (this.m_Diff <= Utils.SMALL)) {
			return false;
		}

		// Build the classifier
		this.m_ClassifiersArrayList.add(AbstractClassifier.makeCopy(this.m_Classifiers[this.next_iterator]));
		this.m_ClassifiersArrayList.get(this.m_ClassifiersArrayList.size() - 1).buildClassifier(this.m_Data);

		this.m_Data = this.residualReplace(this.m_Data, this.m_ClassifiersArrayList.get(this.m_ClassifiersArrayList.size() - 1));
		double sum = 0;
		for (int i = 0; i < this.m_Data.numInstances(); i++) {
			if (this.getMinimizeAbsoluteError()) {
				sum += this.m_Data.instance(i).weight() * Math.abs(this.m_Data.instance(i).classValue());
			} else {
				sum += this.m_Data.instance(i).weight() * this.m_Data.instance(i).classValue() * this.m_Data.instance(i).classValue();
			}
		}
		if (this.m_Debug) {
			if (this.getMinimizeAbsoluteError()) {
				System.err.println("Sum of absolute residuals: " + sum);
			} else {
				System.err.println("Sum of squared residuals: " + sum);
			}
		}

		this.m_Diff = this.m_Error - sum;
		this.m_Error = sum;

		return true;
	}

	/**
	 * Clean up.
	 */
	@Override
	public void done() {

		this.m_Data = null;
	}

	/**
	 * Classify an instance.
	 *
	 * @param inst the instance to predict
	 * @return a prediction for the instance
	 * @throws Exception if an error occurs
	 */
	@Override
	public double classifyInstance(final Instance inst) throws Exception {

		double prediction = this.m_InitialPrediction;

		// default model?
		if (!this.m_SuitableData) {
			return prediction;
		}

		for (Classifier classifier : this.m_ClassifiersArrayList) {
			double toAdd = classifier.classifyInstance(inst);
			if (Utils.isMissingValue(toAdd)) {
				throw new UnassignedClassException("AdditiveRegression: base learner predicted missing value.");
			}
			prediction += (toAdd * this.getShrinkage());
		}

		return prediction;
	}

	/**
	 * Replace the class values of the instances from the current iteration
	 * with residuals after predicting with the supplied classifier.
	 *
	 * @param data the instances to predict
	 * @param c the classifier to use
	 * @return a new set of instances with class values replaced by residuals
	 * @throws Exception if something goes wrong
	 */
	private Instances residualReplace(final Instances data, final Classifier c) throws Exception {

		Instances newInst = new Instances(data);
		for (int i = 0; i < newInst.numInstances(); i++) {
			double pred = c.classifyInstance(newInst.instance(i));
			if (Utils.isMissingValue(pred)) {
				throw new UnassignedClassException("AdditiveRegression: base learner predicted missing value.");
			}
			newInst.instance(i).setClassValue(newInst.instance(i).classValue() - (pred * this.getShrinkage()));
		}
		return newInst;
	}

	/**
	 * Replace the class values of the instances from the current iteration
	 * with residuals after predicting the given constant.
	 *
	 * @param data the instances to predict
	 * @param c the constant to use
	 * @return a new set of instances with class values replaced by residuals
	 * @throws Exception if something goes wrong
	 */
	private Instances residualReplace(final Instances data, final double c) throws Exception {

		Instances newInst = new Instances(data);
		for (int i = 0; i < newInst.numInstances(); i++) {
			newInst.instance(i).setClassValue(newInst.instance(i).classValue() - c);
		}
		return newInst;
	}

	/**
	 * Returns an enumeration of the additional measure names
	 *
	 * @return an enumeration of the measure names
	 */
	@Override
	public Enumeration<String> enumerateMeasures() {
		Vector<String> newVector = new Vector<String>(1);
		newVector.addElement("measureNumIterations");
		return newVector.elements();
	}

	/**
	 * Returns the value of the named measure
	 *
	 * @param additionalMeasureName the name of the measure to query for its value
	 * @return the value of the named measure
	 * @throws IllegalArgumentException if the named measure is not supported
	 */
	@Override
	public double getMeasure(final String additionalMeasureName) {
		if (additionalMeasureName.compareToIgnoreCase("measureNumIterations") == 0) {
			return this.measureNumIterations();
		} else {
			throw new IllegalArgumentException(additionalMeasureName + " not supported (AdditiveRegression)");
		}
	}

	/**
	 * return the number of iterations (base classifiers) completed
	 *
	 * @return the number of iterations (same as number of base classifier
	 *         models)
	 */
	public double measureNumIterations() {
		return this.m_ClassifiersArrayList.size();
	}

	/**
	 * Returns textual description of the classifier.
	 *
	 * @return a description of the classifier as a string
	 */
	@Override
	public String toString() {

		if (this.m_SuitableData && this.m_ClassifiersArrayList == null) {
			return "Classifier hasn't been built yet!";
		}

		// only ZeroR model?
		if (!this.m_SuitableData) {
			StringBuilder buf = new StringBuilder();
			buf.append(this.getClass().getName().replaceAll(".*\\.", "") + "\n");
			buf.append(this.getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
			buf.append("Warning: Non-trivial model could not be built, initial prediction is: ");
			buf.append(this.m_InitialPrediction);
			return buf.toString();
		}

		StringBuilder text = new StringBuilder();
		text.append("Additive Regression\n\n");

		text.append("Initial prediction: " + this.m_InitialPrediction + "\n\n");

		for (int i = 0; i < this.m_ClassifiersArrayList.size(); i++) {
			text.append("Base classifier " + this.getClassifier(i).getClass().getName() + "\n\n");
		}
		text.append("" + this.m_ClassifiersArrayList.size() + " models generated.\n");

		for (int i = 0; i < this.m_ClassifiersArrayList.size(); i++) {
			text.append("\nModel number " + i + "\n\n" + this.m_ClassifiersArrayList.get(i) + "\n");
		}

		return text.toString();
	}

	/**
	 * Returns the revision string.
	 *
	 * @return the revision
	 */
	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 12091 $");
	}

	/**
	 * Main method for testing this class.
	 *
	 * @param argv should contain the following arguments:
	 *            -t training file [-T test file] [-c class index]
	 */
	public static void main(final String[] argv) {
		runClassifier(new ExtendedAdditiveRegression(), argv);
	}
}
