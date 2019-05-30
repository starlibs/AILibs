package jaicore.ml.multioptionsingleenhancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;

/**
 *
 * @author nino
 *
 * This is an extended version of AdaBoostM1 that does not only allow one base classifier with one configuration, but
 * multiple different configuration for one given classifier
 *
 */

public class ExtendedAdaBoostM1 extends AdaBoostM1 {

	/** for serialization */
	private static final long serialVersionUID = 1L;

	/** ArrayList of classifiers that also determines the number of iterations */
	private ArrayList<Classifier> m_ClassifiersArrayList;

	/** Array that will be build by m_ClassifiersArrayList, to avoid conflicts by the way AdaBostM1 is implemented */
	protected Classifier [] m_Classifiers;

	/**
	 * Constructor
	 */
	public ExtendedAdaBoostM1() {
		super();
	}

	/**
	 * Initialize the classifier
	 * If m_Classifiers is empty ExtendedAdaBoostM1 will be build like a regular AdaBoostM1
	 *
	 * @param data			the data to generate the boosted classifier
	 * @throws Exception	the classifier could not be build successfully
	 */
	@Override
	public void initializeClassifier(Instances data) throws Exception {

		if(!this.m_ClassifiersArrayList.isEmpty()) {

			// can classifier handle the data?
			this.getCapabilities().testWithFail(data);

			// remove instances with missing class
			data = new Instances(data);
			data.deleteWithMissingClass();

			this.m_ZeroR = new weka.classifiers.rules.ZeroR();
			this.m_ZeroR.buildClassifier(data);

			this.m_NumClasses = data.numClasses();
			this.m_Betas = new double[this.m_ClassifiersArrayList.size()];
			this.m_NumIterationsPerformed = 0;

			this.m_NumIterations = this.m_ClassifiersArrayList.size();

			this.m_TrainingData = new Instances(data);

			this.m_Classifiers = this.m_ClassifiersArrayList.toArray(this.m_Classifiers);

			this.m_RandomInstance = new Random(this.m_Seed);

			if ((this.m_UseResampling) || (!(this.m_Classifier instanceof WeightedInstancesHandler))) {

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
		else {
			super.initializeClassifier(data);
		}
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
	 * -I &lt;num&gt;
	 *  Number of iterations.
	 *  (default 10)
	 * </pre>
	 *
	 * <pre>
	 * -D
	 *  If set, classifier is run in debug mode and
	 *  may output additional info to the console
	 * </pre>
	 *
	 * <pre>
	 * -W
	 *  Full name of base classifier.
	 *  (default: weka.classifiers.trees.DecisionStump)
	 * </pre>
	 *
	 * <pre>
	 * -A
	 * Adds the current base classifier to the ArrayList
	 * of classifiers that will be used to build ExtendedAdaBoostM1
	 * <pre>
	 *
	 * <pre>
	 * Options specific to classifier weka.classifiers.trees.DecisionStump:
	 * </pre>
	 *
	 * <pre>
	 * -D
	 *  If set, classifier is run in debug mode and
	 *  may output additional info to the console
	 * </pre>
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

		super.setOptions(options);

		if(Utils.getFlag('A', options)) {
			this.m_ClassifiersArrayList = this.addCurrentBaseClassifierToClassifierArray(this.m_Classifier);
		}
	}

	/**
	 * Gets the current settings of the Classifier.
	 *
	 * @return returns a String of Options. Note that you cannot pass this string to setOptions due to the fact
	 * 			that one has to add any subClassifier one by one
	 */
	@Override
	public String[] getOptions() {

		Vector<String> result = new Vector<>();

		result.add("-A");
		for(Classifier classifier: this.m_ClassifiersArrayList) {
			result.add("" + classifier.getClass().getName());

			String [] subclassifierOptions = ((OptionHandler)classifier).getOptions();
			if(subclassifierOptions.length > 0) {
				Collections.addAll(result, subclassifierOptions);
			}
		}

		Collections.addAll(result, super.getOptions());

		return result.toArray(new String[result.size()]);


	}

	/**
	 * Add a number of classifiers to m_ClassifiersArrayList
	 */
	private ArrayList<Classifier> addCurrentBaseClassifierToClassifierArray(final Classifier classifier) {

		this.m_ClassifiersArrayList.add(classifier);
		return this.m_ClassifiersArrayList;
	}

}
