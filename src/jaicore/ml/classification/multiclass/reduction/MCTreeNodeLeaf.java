package jaicore.ml.classification.multiclass.reduction;

import java.util.Arrays;

import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class MCTreeNodeLeaf extends MCTreeNode {

	private int classIndex;

	public MCTreeNodeLeaf(final int classIndex) throws Exception {
		super(Arrays.asList(new Integer[] {classIndex}));
		this.classIndex = classIndex;
	}

	@Override
	public void addChild(final MCTreeNode newNode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		// intentionally do nothing
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		return this.classIndex;
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		return new double[] { 1.0 };
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public Capabilities getCapabilities() {
		return null;
	}

	@Override
	public String toString() {
		return this.classIndex + "";
	}

	public boolean isCompletelyConfigured() {
		return true;
	}

	public String toStringWithOffset(String offset) {
		StringBuilder sb = new StringBuilder();

		sb.append(offset);
		sb.append("(");
		sb.append(getContainedClasses());
		sb.append(")");
		return sb.toString();
	}
}
