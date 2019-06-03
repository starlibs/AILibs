package ai.libs.jaicore.ml.tsc.classifier.trees;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.exception.PredictionException;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Random Tree extension providing leaf node information of the constructed
 * tree.
 *
 */
public class AccessibleRandomTree extends RandomTree {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Number of constructed leaf nodes.
	 */
	private int nosLeafNodes;
	/**
	 * Last leaf node in the prediction.
	 */
	private int lastNode = 0;

	private static final Logger logger = LoggerFactory.getLogger(AccessibleRandomTree.class);

	/**
	 * Internal tree object providing access to leaf node information.
	 */
	protected AccessibleTree tree = null;

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		if (this.m_zeroR != null) {
			return this.m_zeroR.distributionForInstance(instance);
		} else {
			return this.tree.distributionForInstance(instance);
		}
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		this.nosLeafNodes = 0;

		if (this.m_computeImpurityDecreases) {
			this.m_impurityDecreasees = new double[data.numAttributes()][2];
		}

		// Make sure K value is in range
		if (this.m_KValue > data.numAttributes() - 1) {
			this.m_KValue = data.numAttributes() - 1;
		}
		if (this.m_KValue < 1) {
			this.m_KValue = (int) Utils.log2(data.numAttributes() - 1.0) + 1;
		}

		// can classifier handle the data?
		this.getCapabilities().testWithFail(data);

		// remove instances with missing class
		data = new Instances(data);
		data.deleteWithMissingClass();

		// only class? -> build ZeroR model
		if (data.numAttributes() == 1) {
			logger.error("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
			this.m_zeroR = new weka.classifiers.rules.ZeroR();
			this.m_zeroR.buildClassifier(data);
			return;
		} else {
			this.m_zeroR = null;
		}

		// Figure out appropriate datasets
		Instances train = null;
		Instances backfit = null;
		Random rand = data.getRandomNumberGenerator(this.m_randomSeed);
		if (this.m_NumFolds <= 0) {
			train = data;
		} else {
			data.randomize(rand);
			data.stratify(this.m_NumFolds);
			train = data.trainCV(this.m_NumFolds, 1, rand);
			backfit = data.testCV(this.m_NumFolds, 1);
		}

		// Create the attribute indices window
		int[] attIndicesWindow = new int[data.numAttributes() - 1];
		int j = 0;
		for (int i = 0; i < attIndicesWindow.length; i++) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Thread got interrupted, thus, kill WEKA.");
			}
			if (j == data.classIndex()) {
				j++; // do not include the class
			}
			attIndicesWindow[i] = j++;
		}

		double totalWeight = 0;
		double totalSumSquared = 0;

		// Compute initial class counts
		double[] classProbs = new double[train.numClasses()];
		for (int i = 0; i < train.numInstances(); i++) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Thread got interrupted, thus, kill WEKA.");
			}
			Instance inst = train.instance(i);
			if (data.classAttribute().isNominal()) {
				classProbs[(int) inst.classValue()] += inst.weight();
				totalWeight += inst.weight();
			} else {
				classProbs[0] += inst.classValue() * inst.weight();
				totalSumSquared += inst.classValue() * inst.classValue() * inst.weight();
				totalWeight += inst.weight();
			}
		}

		double trainVariance = 0;
		if (totalWeight == 0) {
			throw new IllegalStateException("Total weight must not be 0 at this point.");
		}
		if (data.classAttribute().isNumeric()) {
			trainVariance = RandomTree.singleVariance(classProbs[0], totalSumSquared, totalWeight) / totalWeight;
			classProbs[0] /= totalWeight;
		}

		// Build tree
		this.tree = new AccessibleTree();
		this.m_Info = new Instances(data, 0);
		this.tree.buildTree(train, classProbs, attIndicesWindow, totalWeight, rand, 0, this.m_MinVarianceProp * trainVariance);

		// Backfit if required
		if (backfit != null) {
			this.tree.backfitData(backfit);
		}

	}

	/**
	 * @return the m_Tree
	 */
	public AccessibleTree getMTree() {
		return this.tree;
	}

	class AccessibleTree extends Tree {
		/**
		 * Default generated serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/** The subtrees appended to this tree. */
		protected AccessibleTree[] successors;

		/**
		 * ID of the last leaf node in the prediction.
		 */
		private int leafNodeID;

		@Override
		protected void buildTree(final Instances data, final double[] classProbs, final int[] attIndicesWindow, double totalWeight, final Random random, final int depth, final double minVariance) throws Exception {

			// Make leaf if there are no training instances
			if (data.numInstances() == 0) {
				this.m_Attribute = -1;
				this.m_ClassDistribution = null;
				this.m_Prop = null;

				if (data.classAttribute().isNumeric()) {
					this.m_Distribution = new double[2];
				}
				this.leafNodeID = AccessibleRandomTree.this.nosLeafNodes++;
				return;
			}

			double priorVar = 0;
			if (data.classAttribute().isNumeric()) {

				// Compute prior variance
				double totalSum = 0;
				double totalSumSquared = 0;
				double totalSumOfWeights = 0;
				for (int i = 0; i < data.numInstances(); i++) {
					Instance inst = data.instance(i);
					totalSum += inst.classValue() * inst.weight();
					totalSumSquared += inst.classValue() * inst.classValue() * inst.weight();
					totalSumOfWeights += inst.weight();
				}
				priorVar = AccessibleRandomTree.singleVariance(totalSum, totalSumSquared, totalSumOfWeights);
			}

			// Check if node doesn't contain enough instances or is pure or maximum depth reached
			if (data.classAttribute().isNominal()) {
				totalWeight = Utils.sum(classProbs);
			}
			if (totalWeight < 2 * AccessibleRandomTree.this.m_MinNum ||

					// Nominal case
					(data.classAttribute().isNominal() && Utils.eq(classProbs[Utils.maxIndex(classProbs)], Utils.sum(classProbs)))

					||

					// Numeric case
					(data.classAttribute().isNumeric() && priorVar / totalWeight < minVariance)

					||

					// check tree depth
					((AccessibleRandomTree.this.getMaxDepth() > 0) && (depth >= AccessibleRandomTree.this.getMaxDepth()))) {

				// Make leaf
				this.m_Attribute = -1;
				this.m_ClassDistribution = classProbs.clone();
				if (data.classAttribute().isNumeric()) {
					this.m_Distribution = new double[2];
					this.m_Distribution[0] = priorVar;
					this.m_Distribution[1] = totalWeight;
				}
				this.leafNodeID = AccessibleRandomTree.this.nosLeafNodes++;

				this.m_Prop = null;
				return;
			}

			// Compute class distributions and value of splitting
			// criterion for each attribute
			double val = -Double.MAX_VALUE;
			double split = -Double.MAX_VALUE;
			double[][] bestDists = null;
			double[] bestProps = null;
			int bestIndex = 0;

			// Handles to get arrays out of distribution method
			double[][] props = new double[1][0];
			double[][][] dists = new double[1][0][0];
			double[][] totalSubsetWeights = new double[data.numAttributes()][0];

			// Investigate K random attributes
			int attIndex = 0;
			int windowSize = attIndicesWindow.length;
			int k = AccessibleRandomTree.this.m_KValue;
			boolean gainFound = false;
			double[] tempNumericVals = new double[data.numAttributes()];
			while ((windowSize > 0) && (k-- > 0 || !gainFound)) {

				int chosenIndex = random.nextInt(windowSize);
				attIndex = attIndicesWindow[chosenIndex];

				// shift chosen attIndex out of window
				attIndicesWindow[chosenIndex] = attIndicesWindow[windowSize - 1];
				attIndicesWindow[windowSize - 1] = attIndex;
				windowSize--;

				double currSplit = data.classAttribute().isNominal() ? this.distribution(props, dists, attIndex, data) : this.numericDistribution(props, dists, attIndex, totalSubsetWeights, data, tempNumericVals);

				double currVal = data.classAttribute().isNominal() ? this.gain(dists[0], this.priorVal(dists[0])) : tempNumericVals[attIndex];

				if (Utils.gr(currVal, 0)) {
					gainFound = true;
				}

				if ((currVal > val) || ((!AccessibleRandomTree.this.getBreakTiesRandomly()) && (currVal == val) && (attIndex < bestIndex))) {
					val = currVal;
					bestIndex = attIndex;
					split = currSplit;
					bestProps = props[0];
					bestDists = dists[0];
				}
			}

			// Find best attribute
			this.m_Attribute = bestIndex;

			// Any useful split found?
			if (Utils.gr(val, 0)) {

				// Build subtrees
				this.m_SplitPoint = split;
				this.m_Prop = bestProps;
				Instances[] subsets = this.splitData(data);
				this.successors = new AccessibleTree[bestDists.length];
				double[] attTotalSubsetWeights = totalSubsetWeights[bestIndex];

				for (int i = 0; i < bestDists.length; i++) {
					this.successors[i] = new AccessibleTree();
					this.successors[i].buildTree(subsets[i], bestDists[i], attIndicesWindow, data.classAttribute().isNominal() ? 0 : attTotalSubsetWeights[i], random, depth + 1, minVariance);
				}

				// If all successors are non-empty, we don't need to store the class
				// distribution
				boolean emptySuccessor = false;
				for (int i = 0; i < subsets.length; i++) {
					if (this.successors[i].m_ClassDistribution == null) {
						emptySuccessor = true;
						break;
					}
				}
				if (emptySuccessor) {
					this.m_ClassDistribution = classProbs.clone();
				}
			} else {

				// Make leaf
				this.m_Attribute = -1;
				this.m_ClassDistribution = classProbs.clone();
				if (data.classAttribute().isNumeric()) {
					this.m_Distribution = new double[2];
					this.m_Distribution[0] = priorVar;
					this.m_Distribution[1] = totalWeight;
				}
			}
		}

		@Override
		public double[] distributionForInstance(final Instance instance) throws Exception {
			double[] returnedDist = null;

			if (this.m_Attribute > -1) {
				// Node is not a leaf
				if (instance.isMissing(this.m_Attribute)) {

					// Value is missing
					returnedDist = new double[AccessibleRandomTree.this.m_Info.numClasses()];

					// Split instance up
					for (int i = 0; i < this.successors.length; i++) {
						double[] help = this.successors[i].distributionForInstance(instance);
						if (help != null) {
							for (int j = 0; j < help.length; j++) {
								returnedDist[j] += this.m_Prop[i] * help[j];
							}
						}
					}
				} else if (AccessibleRandomTree.this.m_Info.attribute(this.m_Attribute).isNominal()) {

					// For nominal attributes
					returnedDist = this.successors[(int) instance.value(this.m_Attribute)].distributionForInstance(instance);
				} else {

					// For numeric attributes
					if (instance.value(this.m_Attribute) < this.m_SplitPoint) {
						returnedDist = this.successors[0].distributionForInstance(instance);
					} else {
						returnedDist = this.successors[1].distributionForInstance(instance);
					}
				}
			}

			// Node is a leaf or successor is empty?
			if ((this.m_Attribute == -1) || (returnedDist == null)) {
				AccessibleRandomTree.this.lastNode = this.leafNodeID;
				// Is node empty?
				if (this.m_ClassDistribution == null) {
					if (AccessibleRandomTree.this.getAllowUnclassifiedInstances()) {
						double[] result = new double[AccessibleRandomTree.this.m_Info.numClasses()];
						if (AccessibleRandomTree.this.m_Info.classAttribute().isNumeric()) {
							result[0] = Utils.missingValue();
						}
						return result;
					} else {
						throw new PredictionException("Could not obtain a prediction.");
					}
				}

				// Else return normalized distribution
				double[] normalizedDistribution = this.m_ClassDistribution.clone();
				if (AccessibleRandomTree.this.m_Info.classAttribute().isNominal()) {
					Utils.normalize(normalizedDistribution);
				}
				return normalizedDistribution;
			} else {
				return returnedDist;
			}
		}
	}

	/**
	 * @return the nosLeafNodes
	 */
	public int getNosLeafNodes() {
		return this.nosLeafNodes;
	}

	/**
	 * @return the lastNode
	 */
	public int getLastNode() {
		return this.lastNode;
	}

	/**
	 * Computes the variance for a single set
	 *
	 * @param s
	 * @param sS
	 * @param weight
	 *            the weight
	 * @return the variance
	 */
	protected static double singleVariance(final double s, final double sS, final double weight) {

		return sS - ((s * s) / weight);
	}
}