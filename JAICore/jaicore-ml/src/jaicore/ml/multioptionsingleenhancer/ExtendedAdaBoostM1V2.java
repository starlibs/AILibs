package jaicore.ml.multioptionsingleenhancer;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.RandomizableMultipleClassifiersCombiner;
import weka.classifiers.Sourcable;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

/**
 * 
 * @author nino
 * 
 * This is an extended version of AdaBoostM1 that does not only allow one base classifier with one configuration, but
 * multiple different configuration for one given classifier
 * 
 * As one very easy notices, most of this code is copied from weka.classifiers.meta.AdaBoostM1
 * In fact all routines are very similar as in weka.classifiers.meta.AdaBoostM1. The only difference is, that
 * the array that is used in the iterations of buildingClassifer now is allowed to contain multiple different classifers.
 * 
 * Note that unfortunately we had to copy this much code, because ExtendedAdaBoostM1V2 has to be a MultiClassifiersCombiner
 * to ensure that ML-Plan is able to work properly with this class
 * 
 * Furthermore note that this is the second version, the first one inherited from AdaBoostM1, but the inheritance strategy 
 * of version two fits better to ML-Plan
 *
 */

public class ExtendedAdaBoostM1V2 extends SingleEnhancerDerivedRandomizableMultipleClassifiersCombiner {
	
	/** for serialization */
	private static final long serialVersionUID = 82826486654682762L;

	/**
	 * Constructor
	 */
	public ExtendedAdaBoostM1V2() {
		super();
	}

	/** Array for storing the weights for the votes. */
  protected double[] m_Betas;

  /** The number of successfully generated base classifiers. */
  protected int m_NumIterationsPerformed;

  /** Weight Threshold. The percentage of weight mass used in training */
  protected int m_WeightThreshold = 100;

  /** Use boosting with reweighting? */
  protected boolean m_UseResampling;

  /** The number of classes */
  protected int m_NumClasses;

  /** a ZeroR model in case no model can be built from the data */
  protected Classifier m_ZeroR;

  /** The (weighted) training data */
  protected Instances m_TrainingData;

  /** Random number generator to be used for resampling */
  protected Random m_RandomInstance;

  /**
   * Returns a string describing classifier
   *
   * @return a description suitable for displaying in the explorer/experimenter gui
   */
  public String globalInfo() {

    return "Class for boosting a nominal class classifier using the Adaboost " + "M1 method, that is expanded to the use of multiple differenc classifiers. Only nominal class problems can be tackled. Often "
        + "dramatically improves performance, but sometimes overfits.\n\n" + "For more information, see\n\n" + this.getTechnicalInformation().toString();
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing detailed information about the
   * technical background of this class, e.g., paper reference or book this class is based on.
   *
   * @return the technical information about this class
   */
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation result;

    result = new TechnicalInformation(Type.INPROCEEDINGS);
    result.setValue(Field.AUTHOR, "Yoav Freund and Robert E. Schapire");
    result.setValue(Field.TITLE, "Experiments with a new boosting algorithm");
    result.setValue(Field.BOOKTITLE, "Thirteenth International Conference on Machine Learning");
    result.setValue(Field.YEAR, "1996");
    result.setValue(Field.PAGES, "148-156");
    result.setValue(Field.PUBLISHER, "Morgan Kaufmann");
    result.setValue(Field.ADDRESS, "San Francisco");

    return result;
  }

  /**
   * Select only instances with weights that contribute to the specified quantile of the weight
   * distribution
   *
   * @param data
   *          the input instances
   * @param quantile
   *          the specified quantile eg 0.9 to select 90% of the weight mass
   * @return the selected instances
   */
  protected Instances selectWeightQuantile(final Instances data, final double quantile) {

    int numInstances = data.numInstances();
    Instances trainData = new Instances(data, numInstances);
    double[] weights = new double[numInstances];

    double sumOfWeights = 0;
    for (int i = 0; i < numInstances; i++) {
      weights[i] = data.instance(i).weight();
      sumOfWeights += weights[i];
    }
    double weightMassToSelect = sumOfWeights * quantile;
    int[] sortedIndices = Utils.sort(weights);

    // Select the instances
    sumOfWeights = 0;
    for (int i = numInstances - 1; i >= 0; i--) {
      Instance instance = (Instance) data.instance(sortedIndices[i]).copy();
      trainData.add(instance);
      sumOfWeights += weights[sortedIndices[i]];
      if ((sumOfWeights > weightMassToSelect) && (i > 0) && (weights[sortedIndices[i]] != weights[sortedIndices[i - 1]])) {
        break;
      }
    }
    if (this.m_Debug) {
      System.err.println("Selected " + trainData.numInstances() + " out of " + numInstances);
    }
    return trainData;
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration<Option> listOptions() {

    Vector<Option> newVector = new Vector<>();

    newVector.addElement(new Option("\tPercentage of weight mass to base training on.\n" + "\t(default 100, reduce to around 90 speed up)", "P", 1, "-P <num>"));

    newVector.addElement(new Option("\tUse resampling for boosting.", "Q", 0, "-Q"));

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
   * -P &lt;num&gt;
   *  Percentage of weight mass to base training on.
   *  (default 100, reduce to around 90 speed up)
   * </pre>
   *
   * <pre>
   * -Q
   *  Use resampling for boosting.
   * </pre>
   *
   * <pre>
   * -S &lt;num&gt;
   *  Random number seed.
   *  (default 1)
   * </pre>
   *
   * <pre>
   * -D
   *  If set, classifier is run in debug mode and
   *  may output additional info to the console
   *
   * <pre>
   * -B classifierstring <br>
   * Classifierstring should contain the full class name of a scheme
   * included for selection followed by options to the classifier
   * (required, option should be used once for each classifier).
   * </pre>
   *
   * @param options
   *          the list of options as an array of strings
   * @throws Exception
   *           if an option is not supported
   */
  @Override
  public void setOptions(final String[] options) throws Exception {

    String thresholdString = Utils.getOption('P', options);
    if (thresholdString.length() != 0) {
      this.setWeightThreshold(Integer.parseInt(thresholdString));
    } else {
      this.setWeightThreshold(100);
    }

    this.setUseResampling(Utils.getFlag('Q', options));

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
    Vector<String> result = new Vector<>();

    if (this.getUseResampling()) {
      result.add("-Q");
    }

    result.add("-P");
    result.add("" + this.getWeightThreshold());

    Collections.addAll(result, super.getOptions());

    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the explorer/experimenter gui
   */
  public String weightThresholdTipText() {
    return "Weight threshold for weight pruning.";
  }

  /**
   * Set weight threshold
   *
   * @param threshold
   *          the percentage of weight mass used for training
   */
  public void setWeightThreshold(final int threshold) {

    this.m_WeightThreshold = threshold;
  }

  /**
   * Get the degree of weight thresholding
   *
   * @return the percentage of weight mass used for training
   */
  public int getWeightThreshold() {

    return this.m_WeightThreshold;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the explorer/experimenter gui
   */
  public String useResamplingTipText() {
    return "Whether resampling is used instead of reweighting.";
  }

  /**
   * Set resampling mode
   *
   * @param r
   *          true if resampling should be done
   */
  public void setUseResampling(final boolean r) {

    this.m_UseResampling = r;
  }

  /**
   * Get whether resampling is turned on
   *
   * @return true if resampling output is on
   */
  public boolean getUseResampling() {

    return this.m_UseResampling;
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
    if (super.getCapabilities().handles(Capability.NOMINAL_CLASS)) {
      result.enable(Capability.NOMINAL_CLASS);
    }
    if (super.getCapabilities().handles(Capability.BINARY_CLASS)) {
      result.enable(Capability.BINARY_CLASS);
    }

    return result;
  }

  /**
   * Method used to build the classifier.
   */
  @Override
  public void buildClassifier(final Instances data) throws Exception {

    // Initialize classifier
    this.initializeClassifier(data);
    if (Thread.interrupted()) {
      throw new InterruptedException("Killed WEKA!");
    }

    // Perform boosting iterations
    while (this.next()) {
      if (Thread.interrupted()) {
        throw new InterruptedException("Killed WEKA!");
      }
    }

    // Clean up
    this.done();
  }

  /**
   * Initialize the classifier.
   *
   * @param data
   *          the training data to be used for generating the boosted classifier.
   * @throws Exception
   *           if the classifier could not be built successfully
   */
  public void initializeClassifier(Instances data) throws Exception {

    if(m_Classifiers.length == 0) {
    	throw new Exception("No Base Classifiers are specified");
    }
    

    // can classifier handle the data?
    this.getCapabilities().testWithFail(data);

    // remove instances with missing class
    data = new Instances(data);
    data.deleteWithMissingClass();

    this.m_ZeroR = new weka.classifiers.rules.ZeroR();
    this.m_ZeroR.buildClassifier(data);

    this.m_NumClasses = data.numClasses();
    this.m_Betas = new double[this.m_Classifiers.length];
    this.m_NumIterationsPerformed = 0;
    this.m_TrainingData = new Instances(data);

    this.m_RandomInstance = new Random(this.m_Seed);

    if ((this.m_UseResampling) || (!checkIfAnyClassifierIsInstanceOfWeightedInstancesHandler(m_Classifiers))) {

      // Normalize weights so that they sum to one and can be used as sampling probabilities
      double sumProbs = this.m_TrainingData.sumOfWeights();
      for (int i = 0; i < this.m_TrainingData.numInstances(); i++) {
        if (Thread.interrupted()) {
          throw new InterruptedException("Killed WEKA!");
        }
        this.m_TrainingData.instance(i).setWeight(this.m_TrainingData.instance(i).weight() / sumProbs);
      }
    }
  }

  /**
   * Perform the next boosting iteration.
   *
   * @throws Exception
   *           if an unforeseen problem occurs
   */
  public boolean next() throws Exception {
    if (Thread.interrupted()) {
      throw new InterruptedException("Killed WEKA!");
    }

    // Have we reached the maximum?
    if (this.m_NumIterationsPerformed >= this.m_Classifiers.length) {
      return false;
    }

    // only class? -> just use ZeroR model
    if (this.m_TrainingData.numAttributes() == 1) {
      return false;
    }

    if (this.m_Debug) {
      System.err.println("Training classifier " + (this.m_NumIterationsPerformed + 1));
    }

    // Select instances to train the classifier on
    Instances trainData = null;
    if (this.m_WeightThreshold < 100) {
      trainData = this.selectWeightQuantile(this.m_TrainingData, (double) this.m_WeightThreshold / 100);
    } else {
      trainData = new Instances(this.m_TrainingData);
    }

    double epsilon = 0;
    if ((this.m_UseResampling) || (!checkIfAnyClassifierIsInstanceOfWeightedInstancesHandler(m_Classifiers))) {

      // Resample
      int resamplingIterations = 0;
      double[] weights = new double[trainData.numInstances()];
      for (int i = 0; i < weights.length; i++) {
        weights[i] = trainData.instance(i).weight();
      }
      do {
        if (Thread.interrupted()) {
          throw new InterruptedException("Killed WEKA!");
        }
        Instances sample = trainData.resampleWithWeights(this.m_RandomInstance, weights);

        // Build and evaluate classifier
        Evaluation evaluation = new Evaluation(this.m_TrainingData);
        evaluation.evaluateModel(this.m_Classifiers[this.m_NumIterationsPerformed], this.m_TrainingData);
        epsilon = evaluation.errorRate();
        resamplingIterations++;
      } while (Utils.eq(epsilon, 0) && (resamplingIterations < m_Classifiers.length));
    } else {

      // Build the classifier
      if (this.m_Classifiers[this.m_NumIterationsPerformed] instanceof Randomizable) {
        ((Randomizable) this.m_Classifiers[this.m_NumIterationsPerformed]).setSeed(this.m_RandomInstance.nextInt());
      }
      this.m_Classifiers[this.m_NumIterationsPerformed].buildClassifier(trainData);

      // Evaluate the classifier
      Evaluation evaluation = new Evaluation(this.m_TrainingData); // Does this need to be a copy
      evaluation.evaluateModel(this.m_Classifiers[this.m_NumIterationsPerformed], this.m_TrainingData);
      epsilon = evaluation.errorRate();
    }

    // Stop if error too big or 0
    if (Utils.grOrEq(epsilon, 0.5) || Utils.eq(epsilon, 0)) {
      if (this.m_NumIterationsPerformed == 0) {
        this.m_NumIterationsPerformed = 1; // If we're the first we have to use it
      }
      return false;
    }

    // Determine the weight to assign to this model
    double reweight = (1 - epsilon) / epsilon;
    this.m_Betas[this.m_NumIterationsPerformed] = Math.log(reweight);
    if (this.m_Debug) {
      System.err.println("\terror rate = " + epsilon + "  beta = " + this.m_Betas[this.m_NumIterationsPerformed]);
    }

    // Update instance weights
    this.setWeights(this.m_TrainingData, reweight);

    // Model has been built successfully
    this.m_NumIterationsPerformed++;
    return true;
  }

  /**
   * Clean up after boosting.
   */
  public void done() {

    this.m_TrainingData = null;

    // Can discard ZeroR model if we don't need it anymore
    if (this.m_NumIterationsPerformed > 0) {
      this.m_ZeroR = null;
    }
  }

  /**
   * Sets the weights for the next iteration.
   *
   * @param training
   *          the training instances
   * @param reweight
   *          the reweighting factor
   * @throws Exception
   *           if something goes wrong
   */
  protected void setWeights(final Instances training, final double reweight) throws Exception {

    double oldSumOfWeights, newSumOfWeights;

    oldSumOfWeights = training.sumOfWeights();
    Enumeration<Instance> enu = training.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance instance = enu.nextElement();
      if (!Utils.eq(this.m_Classifiers[this.m_NumIterationsPerformed].classifyInstance(instance), instance.classValue())) {
        instance.setWeight(instance.weight() * reweight);
      }
    }

    // Renormalize weights
    newSumOfWeights = training.sumOfWeights();
    enu = training.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance instance = enu.nextElement();
      instance.setWeight(instance.weight() * oldSumOfWeights / newSumOfWeights);
    }
  }

  /**
   * Calculates the class membership probabilities for the given test instance.
   *
   * @param instance
   *          the instance to be classified
   * @return predicted class probability distribution
   * @throws Exception
   *           if instance could not be classified successfully
   */
  @Override
  public double[] distributionForInstance(final Instance instance) throws Exception {
	System.out.println("distForInstance");
	System.out.println(this.m_NumIterationsPerformed);
    // default model?
    if (this.m_NumIterationsPerformed == 0) {
      return this.m_ZeroR.distributionForInstance(instance);
    }

    if (this.m_NumIterationsPerformed == 0) {
      throw new Exception("No model built");
    }
    double[] sums = new double[instance.numClasses()];

    if (this.m_NumIterationsPerformed == 1) {
      System.out.println(this.m_Classifiers[0].getClass().getName());
      return this.m_Classifiers[0].distributionForInstance(instance);
    } else {
      for (int i = 0; i < this.m_NumIterationsPerformed; i++) {
        sums[(int) this.m_Classifiers[i].classifyInstance(instance)] += this.m_Betas[i];
      }
      return Utils.logs2probs(sums);
    }
  }

  /**
   * Returns the boosted model as Java source code.
   *
   * @param className
   *          the classname of the generated class
   * @return the tree as Java source code
   * @throws Exception
   *           if something goes wrong
   */
  public String toSource(final String className) throws Exception {

    if (this.m_NumIterationsPerformed == 0) {
      throw new Exception("No model built yet");
    }
    for(Classifier classifier: m_Classifiers) {
	    if (!(classifier instanceof Sourcable)) {
	      throw new Exception("Base learner " + classifier.getClass().getName() + " is not Sourcable");
	    }
    }

    StringBuffer text = new StringBuffer("class ");
    text.append(className).append(" {\n\n");

    text.append("  public static double classify(Object[] i) {\n");

    if (this.m_NumIterationsPerformed == 1) {
      text.append("    return " + className + "_0.classify(i);\n");
    } else {
      text.append("    double [] sums = new double [" + this.m_NumClasses + "];\n");
      for (int i = 0; i < this.m_NumIterationsPerformed; i++) {
        text.append("    sums[(int) " + className + '_' + i + ".classify(i)] += " + this.m_Betas[i] + ";\n");
      }
      text.append("    double maxV = sums[0];\n" + "    int maxI = 0;\n" + "    for (int j = 1; j < " + this.m_NumClasses + "; j++) {\n"
          + "      if (sums[j] > maxV) { maxV = sums[j]; maxI = j; }\n" + "    }\n    return (double) maxI;\n");
    }
    text.append("  }\n}\n");

    for (int i = 0; i < this.m_Classifiers.length; i++) {
      text.append(((Sourcable) this.m_Classifiers[i]).toSource(className + '_' + i));
    }
    return text.toString();
  }

  /**
   * Returns description of the boosted classifier.
   *
   * @return description of the boosted classifier as a string
   */
  @Override
  public String toString() {

    // only ZeroR model?
    if (this.m_NumIterationsPerformed == 0) {
      StringBuffer buf = new StringBuffer();
      if (this.m_ZeroR == null) {
        buf.append("ExtendedAdaBoostM1V2: No model built yet.\n");
      } else {
        buf.append(this.getClass().getName().replaceAll(".*\\.", "") + "\n");
        buf.append(this.getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
        buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
        buf.append(this.m_ZeroR.toString());
      }
      return buf.toString();
    }

    StringBuffer text = new StringBuffer();
    if (this.m_NumIterationsPerformed == 1) {
      text.append("ExtendedAdaBoostM1V2: No boosting possible, one classifier used!\n");
      text.append(this.m_Classifiers[0].toString() + "\n");
    } else {
      text.append("ExtendedAdaBoostM1V2: Base classifiers and their weights: \n\n");
      for (int i = 0; i < this.m_NumIterationsPerformed; i++) {
        text.append(this.m_Classifiers[i].toString() + "\n\n");
        text.append("Weight: " + Utils.roundDouble(this.m_Betas[i], 2) + "\n\n");
      }
      text.append("Number of performed Iterations: " + this.m_NumIterationsPerformed + "\n");
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
    return RevisionUtils.extract("$Revision: 10969 $");
  }


  /**
   * Main method for testing this class.
   *
   * @param argv
   *          the options
   */
  public static void main(final String[] argv) {
    runClassifier(new ExtendedAdaBoostM1V2(), argv);
  }
}
