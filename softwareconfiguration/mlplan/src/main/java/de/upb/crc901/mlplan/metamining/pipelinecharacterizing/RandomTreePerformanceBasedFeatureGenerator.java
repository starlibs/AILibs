package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

import java.util.HashMap;
import java.util.Map;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
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
	public void train(Instances data) throws AlgorithmException {
		// Step 1: Train Random Tree
		try {
		randomTree.buildClassifier(data);
		} catch(Exception e) {
			throw new AlgorithmException(e, "Random Tree could not be trained!");
		}

		// Step 2: Count the nodes in the tree (DF Traversal Index Mapping)
		addIndexToMap(0, randomTree.getM_Tree());
		tree = randomTree.getM_Tree();
	}

	private int addIndexToMap(int subTreeIndex, Tree subTree) {
		nodesIndices.put(subTree, subTreeIndex);
		subTreeIndex++;

		int numberOfSuccessors = 0;
		if (subTree.getM_Successors() != null) {
			for (int i = 0; i < subTree.getM_Successors().length; i++) {
				subTreeIndex += numberOfSuccessors;
				numberOfSuccessors += addIndexToMap(subTreeIndex, subTree.getM_Successors()[i]) + 1;
			}
		}
		return numberOfSuccessors;
	}

	@Override
	public Vector predict(Vector intermediatePipelineRepresentation) {
		Vector pipelineRepresentation = new DenseDoubleVector(nodesIndices.size(), nonOccurenceValue);

		// Query the RandomTree
		Tree subTree = tree;
		while (subTree != null) {
			if (subTree.getM_Attribute() == -1) {
				// We are at a leaf node - The current node occurs
				pipelineRepresentation.setValue(nodesIndices.get(subTree), occurenceValue);

				// We are at a leaf - stop
				subTree = null;
			} else if (allowUnsetValues
					&& !isValueUnset(intermediatePipelineRepresentation.getValue(subTree.getM_Attribute()))
					|| !allowUnsetValues) {
				// The current node occurs
				pipelineRepresentation.setValue(nodesIndices.get(subTree), occurenceValue);

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
				setSubTreeToValue(subTree, outgoingUnsetValueValue, pipelineRepresentation);
				subTree = null;
			}
		}

		return pipelineRepresentation;
	}

	private boolean isValueUnset(double value) {
		if (Double.isNaN(incomingUnsetValueValue)) {
			return Double.isNaN(value);
		} else {
			return value == incomingUnsetValueValue;
		}
	}

	private void setSubTreeToValue(Tree subTree, double value, Vector featureRepresentation) {
		featureRepresentation.setValue(nodesIndices.get(subTree), value);

		if (subTree.getM_Successors() != null) {
			for (int i = 0; i < subTree.getM_Successors().length; i++) {
				setSubTreeToValue(subTree.getM_Successors()[i], value, featureRepresentation);
			}
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		try {
			builder.append(randomTree);
		} catch (Exception e) {
			builder.append("Can not print tree");
		}
		builder.append(System.lineSeparator());
		builder.append(nodesIndices);
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
		return incomingUnsetValueValue;
	}

	/**
	 * Allow incoming feature vectors to have missing values.
	 * 
	 * @param unsetValueValue
	 *            the value that represents a value to be missing in incoming
	 *            feature vectors.
	 */
	public void setAllowNonOccurence(double unsetValueValue) {
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
		return outgoingUnsetValueValue;
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
	public void setOutgoingUnsetValueValue(double outgoingUnsetValueValue) {
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
		return occurenceValue;
	}

	/**
	 * Set the value that this feature generator sets for nodes in the tree that are
	 * encountered during the traversal based on a given feature vector.
	 * 
	 * @param occurenceValue
	 *            the value that this feature generator sets for nodes in the tree
	 *            that are encountered
	 */
	public void setOccurenceValue(double occurenceValue) {
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
		return nonOccurenceValue;
	}

	/**
	 * Set the value that this feature generator sets for nodes in the tree that are
	 * not encountered during the traversal based on a given feature vector.
	 * 
	 * @param nonOccurenceValue
	 *            the value that this feature generator sets for nodes in the tree
	 *            that are not encountered
	 */
	public void setNonOccurenceValue(double nonOccurenceValue) {
		this.nonOccurenceValue = nonOccurenceValue;
	}
}