package ai.libs.mlplan.metamining.pipelinecharacterizing;

import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.math.linearalgebra.Vector;
import weka.classifiers.trees.RandomTree;
import weka.classifiers.trees.RandomTree.Tree;
import weka.core.Instances;

/**
 * A {@link AWEKAPerformanceDecisionTreeBasedFeatureGenerator} that uses a
 * {@link RandomTree}.
 *
 * @author Helena Graf
 *
 */
public class RandomTreePerformanceBasedFeatureGenerator extends AWEKAPerformanceDecisionTreeBasedFeatureGenerator {

	private RandomTree randomTree = new RandomTree();
	private Tree tree;
	private Map<Tree, Integer> nodesIndices = new HashMap<>();
	private boolean allowUnsetValues = false;
	private double incomingUnsetValueValue = Double.NaN;
	private double outgoingUnsetValueValue = 0;
	private double occurenceValue = 1;
	private double nonOccurenceValue = -1;

	@Override
	public void train(final Instances data) throws AlgorithmException {
		// Step 1: Train Random Tree
		try {
			this.randomTree.buildClassifier(data);
		} catch(Exception e) {
			throw new AlgorithmException(e, "Random Tree could not be trained!");
		}

		// Step 2: Count the nodes in the tree (DF Traversal Index Mapping)
		this.addIndexToMap(0, this.randomTree.getM_Tree());
		this.tree = this.randomTree.getM_Tree();
	}

	private int addIndexToMap(int subTreeIndex, final Tree subTree) {
		this.nodesIndices.put(subTree, subTreeIndex);
		subTreeIndex++;

		int numberOfSuccessors = 0;
		if (subTree.getM_Successors() != null) {
			for (int i = 0; i < subTree.getM_Successors().length; i++) {
				subTreeIndex += numberOfSuccessors;
				numberOfSuccessors += this.addIndexToMap(subTreeIndex, subTree.getM_Successors()[i]) + 1;
			}
		}
		return numberOfSuccessors;
	}

	@Override
	public Vector predict(final Vector intermediatePipelineRepresentation) {
		Vector pipelineRepresentation = new DenseDoubleVector(this.nodesIndices.size(), this.nonOccurenceValue);

		// Query the RandomTree
		Tree subTree = this.tree;
		while (subTree != null) {
			if (subTree.getM_Attribute() == -1) {
				// We are at a leaf node - The current node occurs
				pipelineRepresentation.setValue(this.nodesIndices.get(subTree), this.occurenceValue);

				// We are at a leaf - stop
				subTree = null;
			} else if (this.allowUnsetValues
					&& !this.isValueUnset(intermediatePipelineRepresentation.getValue(subTree.getM_Attribute()))
					|| !this.allowUnsetValues) {
				// The current node occurs
				pipelineRepresentation.setValue(this.nodesIndices.get(subTree), this.occurenceValue);

				if (intermediatePipelineRepresentation.getValue(subTree.getM_Attribute()) < subTree.getM_SplitPoint()) {
					// we go to the left
					subTree = subTree.getM_Successors()[0];
				} else {
					// we go to the right
					subTree = subTree.getM_Successors()[1];
				}

			} else {
				// We do allow unset values and the value is unset - set the subtree to non
				// occurence and end the traversal
				this.setSubTreeToValue(subTree, this.outgoingUnsetValueValue, pipelineRepresentation);
				subTree = null;
			}
		}

		return pipelineRepresentation;
	}

	private boolean isValueUnset(final double value) {
		if (Double.isNaN(this.incomingUnsetValueValue)) {
			return Double.isNaN(value);
		} else {
			return value == this.incomingUnsetValueValue;
		}
	}

	private void setSubTreeToValue(final Tree subTree, final double value, final Vector featureRepresentation) {
		featureRepresentation.setValue(this.nodesIndices.get(subTree), value);

		if (subTree.getM_Successors() != null) {
			for (int i = 0; i < subTree.getM_Successors().length; i++) {
				this.setSubTreeToValue(subTree.getM_Successors()[i], value, featureRepresentation);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		try {
			builder.append(this.randomTree);
		} catch (Exception e) {
			builder.append("Can not print tree");
		}
		builder.append(System.lineSeparator());
		builder.append(this.nodesIndices);
		builder.append(System.lineSeparator());
		return builder.toString();
	}

	/**
	 * Get the value that is assumed to mean a missing value for incoming feature
	 * values. Only relevant if missing values for incoming feature vectors are
	 * allowed.
	 *
	 * @return the value that is assumed to mean a missing value for incoming
	 *         feature values
	 */
	public double getIncomingUnsetValueValue() {
		return this.incomingUnsetValueValue;
	}

	/**
	 * Allow incoming feature vectors to have missing values.
	 *
	 * @param unsetValueValue
	 *            the value that represents a value to be missing in incoming
	 *            feature vectors.
	 */
	public void setAllowNonOccurence(final double unsetValueValue) {
		this.allowUnsetValues = true;
		this.incomingUnsetValueValue = unsetValueValue;
	}

	/**
	 * Disallow incoming feature vectors from having missing values.
	 */
	public void disallowNonOccurence() {
		this.allowUnsetValues = false;
	}

	/**
	 * Get the value that this feature generator sets for areas of the trees that
	 * are not encountered because an attribute that is used as a split in a node
	 * that is encountered is not set in a given feature representation.
	 *
	 * @return the produced value for areas of the tree that are blocked by a
	 *         missing feature value
	 */
	public double getOutgoingUnsetValueValue() {
		return this.outgoingUnsetValueValue;
	}

	/**
	 * Set the value that this feature generator sets for areas of the trees that
	 * are not encountered because an attribute that is used as a split in a node
	 * that is encountered is not set in a given feature representation.
	 *
	 * @param outgoingUnsetValueValue
	 *            the produced value for areas of the tree that are blocked by a
	 *            missing feature value
	 */
	public void setOutgoingUnsetValueValue(final double outgoingUnsetValueValue) {
		this.outgoingUnsetValueValue = outgoingUnsetValueValue;
	}

	/**
	 * Get the value that this feature generator sets for nodes in the tree that are
	 * encountered during the traversal based on a given feature vector.
	 *
	 * @return the value that this feature generator sets for nodes in the tree that
	 *         are encountered
	 */
	public double getOccurenceValue() {
		return this.occurenceValue;
	}

	/**
	 * Set the value that this feature generator sets for nodes in the tree that are
	 * encountered during the traversal based on a given feature vector.
	 *
	 * @param occurenceValue
	 *            the value that this feature generator sets for nodes in the tree
	 *            that are encountered
	 */
	public void setOccurenceValue(final double occurenceValue) {
		this.occurenceValue = occurenceValue;
	}

	/**
	 * Get the value that this feature generator sets for nodes in the tree that are
	 * not encountered during the traversal based on a given feature vector.
	 *
	 * @return the value that this feature generator sets for nodes in the tree that
	 *         are not encountered
	 */
	public double getNonOccurenceValue() {
		return this.nonOccurenceValue;
	}

	/**
	 * Set the value that this feature generator sets for nodes in the tree that are
	 * not encountered during the traversal based on a given feature vector.
	 *
	 * @param nonOccurenceValue
	 *            the value that this feature generator sets for nodes in the tree
	 *            that are not encountered
	 */
	public void setNonOccurenceValue(final double nonOccurenceValue) {
		this.nonOccurenceValue = nonOccurenceValue;
	}
}