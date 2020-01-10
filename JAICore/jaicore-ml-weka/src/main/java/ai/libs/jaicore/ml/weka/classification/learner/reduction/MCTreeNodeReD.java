package ai.libs.jaicore.ml.weka.classification.learner.reduction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.basic.StringUtil;
import ai.libs.jaicore.ml.weka.WekaUtil;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WekaException;

public class MCTreeNodeReD extends AMCTreeNode<String> {
	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = 8873192747068561266L;

	private boolean debugMode = false;

	@SuppressWarnings("serial")
	private class ChildNode implements Serializable {
		private List<String> containedClasses;
		private Classifier childNodeClassifier;

		private ChildNode(final List<String> containedClasses, final Classifier childNodeClassifier) {
			this.containedClasses = containedClasses;
			this.childNodeClassifier = childNodeClassifier;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			if (this.childNodeClassifier instanceof MCTreeNodeReD) {
				sb.append(this.childNodeClassifier.toString());
			} else {
				sb.append(this.childNodeClassifier.getClass().getSimpleName() + "(");
				sb.append(StringUtil.implode(this.containedClasses, ","));
				sb.append(")");
			}

			return sb.toString();
		}

		public String toStringWithOffset(final String offset) {
			StringBuilder sb = new StringBuilder();

			if (this.childNodeClassifier instanceof MCTreeNodeReD) {
				sb.append(((MCTreeNodeReD) this.childNodeClassifier).toStringWithOffset(offset + "\t"));
			} else {
				sb.append(offset);
				sb.append("(");
				sb.append(this.containedClasses);
				sb.append(":");
				sb.append(this.childNodeClassifier.getClass().getSimpleName());
				sb.append(")");
			}

			return sb.toString();
		}
	}

	/**
	 * Classifier assigned to this inner node.
	 */
	private Classifier innerNodeClassifier;

	/**
	 * List of children of this tree node.
	 */
	private List<ChildNode> children = new ArrayList<>();
	/**
	 * Flag indicating whether this node is already trained.
	 */
	private boolean trained = false;

	public MCTreeNodeReD(final String innerNodeClassifier, final Collection<String> leftChildClasses, final String leftChildClassifier, final Collection<String> rightChildClasses, final String rightChildClassifier) throws Exception {
		this(innerNodeClassifier, leftChildClasses, AbstractClassifier.forName(leftChildClassifier, null), rightChildClasses, AbstractClassifier.forName(rightChildClassifier, null));
	}

	public MCTreeNodeReD(final Classifier innerNodeClassifier, final Collection<String> leftChildClasses, final Classifier leftChildClassifier, final Collection<String> rightChildClasses, final Classifier rightChildClassifier) {
		this(innerNodeClassifier, Arrays.asList(leftChildClasses, rightChildClasses), Arrays.asList(leftChildClassifier, rightChildClassifier));
	}

	public MCTreeNodeReD(final String innerNodeClassifier, final Collection<String> leftChildClasses, final Classifier leftChildClassifier, final Collection<String> rightChildClasses, final Classifier rightChildClassifier)
			throws Exception {
		this(AbstractClassifier.forName(innerNodeClassifier, new String[] {}), leftChildClasses, leftChildClassifier, rightChildClasses, rightChildClassifier);
	}

	public MCTreeNodeReD(final Classifier innerNodeClassifier, final List<Collection<String>> childClasses, final List<Classifier> childClassifier) {
		this();
		if (childClasses.size() != childClassifier.size()) {
			throw new IllegalArgumentException("Number of child classes does not equal the number of child classifiers");
		}
		this.innerNodeClassifier = innerNodeClassifier;
		for (int i = 0; i < childClasses.size(); i++) {
			this.addChild(new ArrayList<>(childClasses.get(i)), childClasses.get(i).size() > 1 ? childClassifier.get(i) : new ConstantClassifier());
		}
	}

	public MCTreeNodeReD(final MCTreeNodeReD copy) throws Exception {
		this(copy.innerNodeClassifier.getClass().getName(), copy.children.get(0).containedClasses, WekaUtil.cloneClassifier(copy.children.get(0).childNodeClassifier), copy.children.get(1).containedClasses,
				WekaUtil.cloneClassifier(copy.children.get(1).childNodeClassifier));
	}

	protected MCTreeNodeReD() {
		super(new ArrayList<>());
	}

	public void addChild(final List<String> childClasses, final Classifier childClassifier) {
		assert !this.trained : "Cannot insert children after the tree node has been trained!";
	if (childClassifier instanceof MCTreeMergeNode) {
		this.children.addAll(((MCTreeMergeNode) childClassifier).getChildren());
	} else {
		this.children.add(new ChildNode(childClasses, childClassifier));
	}
	this.getContainedClasses().addAll(childClasses);
	}

	/**
	 * @return Returns a list of the child nodes of this node.
	 */
	public List<ChildNode> getChildren() {
		return this.children;
	}

	public boolean isCompletelyConfigured() {
		if (this.innerNodeClassifier == null || this.children.isEmpty()) {
			return false;
		}
		for (ChildNode child : this.children) {
			if (child.childNodeClassifier instanceof MCTreeNodeReD && !((MCTreeNodeReD) child.childNodeClassifier).isCompletelyConfigured()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<String> getContainedClasses() {
		return (List<String>)super.getContainedClasses();
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		if (data.isEmpty()) {
			throw new IllegalArgumentException("Cannot train MCTree with empty set of instances.");
		}
		assert !this.children.isEmpty() : "Cannot train MCTree without children";
		assert !this.trained : "Cannot retrain MCTreeNodeReD";
		if (this.debugMode) {
			if (!this.getContainedClasses().containsAll(WekaUtil.getClassesActuallyContainedInDataset(data))) {
				throw new IllegalStateException("The classes for which this MCTreeNodeReD has been defined (" + this.getContainedClasses()
				+ ") is not a superset of the given training data (" + WekaUtil.getClassesActuallyContainedInDataset(data) + ") ...");
			}
			if (!WekaUtil.getClassesActuallyContainedInDataset(data).containsAll(this.getContainedClasses())) {
				throw new IllegalStateException("The classes for which this MCTreeNodeReD has been defined (" + this.getContainedClasses() + ") is not a subset of the given training data ("
						+ WekaUtil.getClassesActuallyContainedInDataset(data) + ") ...");
			}
		}

		/* resort the contained classes based on the input data. This is necessary, because the order of classes in the given dataset might differ from the order of classes initially declared for the tree */
		this.getContainedClasses().clear();
		for (int i = 0; i < data.numClasses(); i++) {
			this.getContainedClasses().add(data.classAttribute().value(i));
		}

		/* create subsets of the training data filtering for the respective class values and build child classifier */
		List<Set<String>> instancesClusters = new ArrayList<>();
		int childNum = 0;
		for (ChildNode child : this.getChildren()) {
			childNum++;
			assert (!child.containedClasses.isEmpty()) : "Contained classes of child must not be empty";
			Instances childData = WekaUtil.getEmptySetOfInstancesWithRefactoredClass(data, child.containedClasses);
			for (Instance i : data) {
				String className = i.classAttribute().value((int) Math.round(i.classValue()));
				if (child.containedClasses.contains(className)) {
					Instance iNew = WekaUtil.getRefactoredInstance(i, child.containedClasses);
					iNew.setClassValue(className);
					iNew.setDataset(childData);
					childData.add(iNew);
				}
			}
			assert child.containedClasses.containsAll(WekaUtil.getClassesActuallyContainedInDataset(childData)) : "There are data for the child node that are not contained in its declaration";
			assert WekaUtil.getClassesActuallyContainedInDataset(childData).containsAll(child.containedClasses) : "There are classes declared in the child, but no corresponding data have been passed";
			try {
				child.childNodeClassifier.buildClassifier(childData);
			} catch (Exception e) {
				throw new TrainingException("Cannot train classifier in child #" + childNum, e);
			}
			instancesClusters.add(new HashSet<>(child.containedClasses));
		}

		/* build inner classifier with refactored training data */
		Instances trainingData = WekaUtil.mergeClassesOfInstances(data, instancesClusters);
		try {
			this.innerNodeClassifier.buildClassifier(trainingData);
		} catch (WekaException e) {
			this.innerNodeClassifier = new ZeroR();
			this.innerNodeClassifier.buildClassifier(trainingData);
		} catch (Exception e) {
			throw new TrainingException("Cannot train inner classifier", e);
		}

		this.trained = true;
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		assert this.trained : "Cannot get distribution from untrained classifier " + this.toStringWithOffset();

	// compute distribution of the children clusters of the inner node's classifier
	Instance refactoredInstance = WekaUtil.getRefactoredInstance(instance);
	double[] innerNodeClassifierDistribution = this.innerNodeClassifier.distributionForInstance(refactoredInstance);

	// recursively compute distribution for instance for all the children and assign the probabilities
	// to classDistribution array
	double[] classDistribution = new double[this.getContainedClasses().size()];
	for (int childIndex = 0; childIndex < this.children.size(); childIndex++) {
		ChildNode child = this.children.get(childIndex);
		double[] childDistribution = child.childNodeClassifier.distributionForInstance(WekaUtil.getRefactoredInstance(instance, child.containedClasses));
		assert childDistribution.length == child.containedClasses.size() : "Mismatch of child classes (" + child.containedClasses.size() + ") and distribution in child (" + childDistribution.length + ")";
		for (int i = 0; i < childDistribution.length; i++) {
			String classValue = child.containedClasses.get(i);
			classDistribution[this.getContainedClasses().indexOf(classValue)] = childDistribution[i] * innerNodeClassifierDistribution[childIndex];
		}
	}

	double sum = Arrays.stream(classDistribution).sum();
	assert (sum - 1E-8 <= 1.0 && sum + 1E-8 >= 1.0) : "Distribution does not sum up to 1; actual some of distribution entries: " + sum;

	return classDistribution;
	}

	@Override
	public Capabilities getCapabilities() {
		return this.innerNodeClassifier.getCapabilities();
	}

	public int getHeight() {
		int maxHeightChildren = 0;
		for (ChildNode child : this.children) {
			if (child.childNodeClassifier instanceof MCTreeNodeReD) {
				maxHeightChildren = Math.max(((MCTreeNodeReD) child.childNodeClassifier).getHeight(), maxHeightChildren);
			}
		}
		return 1 + maxHeightChildren;
	}

	public int getDepthOfFirstCommonParent(final List<String> classes) {
		for (ChildNode child : this.children) {
			if (child.containedClasses.containsAll(classes)) {
				int depth = 1;
				if (child.childNodeClassifier instanceof MCTreeNodeReD) {
					depth += ((MCTreeNodeReD) child.childNodeClassifier).getDepthOfFirstCommonParent(classes);
				}
				return depth;
			}
		}
		return 1;
	}

	public Classifier getClassifier() {
		return this.innerNodeClassifier;
	}

	public void setBaseClassifier(final Classifier classifier) {
		if (classifier == null) {
			throw new IllegalArgumentException("Cannot set null classifier!");
		}
		this.innerNodeClassifier = classifier;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("(");
		sb.append(this.innerNodeClassifier.getClass().getSimpleName());
		sb.append(")");

		sb.append("{");

		boolean first = true;
		for (ChildNode child : this.children) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}

			sb.append(child);
		}
		sb.append("}");
		return sb.toString();
	}

	public String toStringWithOffset() {
		return this.toStringWithOffset("");
	}

	public String toStringWithOffset(final String offset) {
		StringBuilder sb = new StringBuilder();

		sb.append(offset);
		sb.append("(");
		sb.append(this.getContainedClasses());
		sb.append(":");
		sb.append(this.innerNodeClassifier.getClass().getSimpleName());
		sb.append(") {");
		boolean first = true;
		for (ChildNode child : this.children) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append("\n");
			sb.append(child.toStringWithOffset(offset + "  "));
		}
		sb.append("\n");
		sb.append(offset);
		sb.append("}");
		return sb.toString();
	}
}
