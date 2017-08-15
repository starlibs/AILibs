package jaicore.ml.classification.multiclass.reduction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jaicore.ml.WekaUtil;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.meta.MultiClassClassifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

public class MCTreeNode implements Classifier, ITreeClassifier, Serializable, Iterable<MCTreeNode> {

	/**
	 *
	 */
	private static final long serialVersionUID = 8873192747068561266L;

	private EMCNodeType nodeType;
	private List<MCTreeNode> children = new ArrayList<>();
	private Classifier classifier;
	private String classifierID;
	private final List<Integer> containedClasses;
	private boolean trained = false;

	private static ClassifierCache classifierCache = new ClassifierCache();

	public MCTreeNode(List<Integer> containedClasses) {
		this.containedClasses = containedClasses;
	}

	public MCTreeNode(List<Integer> containedClasses, final EMCNodeType nodeType, final String classifierID) throws Exception {
		this(containedClasses, nodeType, AbstractClassifier.forName(classifierID, null));
	}

	public MCTreeNode(List<Integer> containedClasses, final EMCNodeType nodeType, final Classifier baseClassifier) throws Exception {
		this(containedClasses);
		setNodeType(nodeType);
		setBaseClassifier(baseClassifier);
	}

	public EMCNodeType getNodeType() {
		return this.nodeType;
	}

	public void addChild(final MCTreeNode newNode) {
		if (newNode.getNodeType() == EMCNodeType.MERGE) {
			for (MCTreeNode child : newNode.getChildren()) {
				this.children.add(child);
			}
		} else {
			this.children.add(newNode);
		}
	}

	public List<MCTreeNode> getChildren() {
		return this.children;
	}

	public Collection<Integer> getContainedClasses() {
		return this.containedClasses;
	}

	public boolean isCompletelyConfigured() {
		if (this.classifier == null)
			return false;
		if (this.children.isEmpty())
			return false;
		for (MCTreeNode child : children) {
			if (!child.isCompletelyConfigured())
				return false;
		}
		return true;
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		assert (this.getNodeType() != EMCNodeType.MERGE) : "MERGE node detected while building classifier. This must not happen!";
		assert !data.isEmpty() : "Cannot train MCTree with empty set of instances.";
		assert !children.isEmpty() : "Cannot train MCTree without children";

		// sort class split into clusters
		List<Set<String>> instancesCluster = new ArrayList<>();
		IntStream.range(0, this.children.size()).forEach(x -> instancesCluster.add(new HashSet<>()));
		int index = 0;
		for (MCTreeNode child : this.children) {
			for (Integer classIndex : child.getContainedClasses()) {
				instancesCluster.get(index).add(data.classAttribute().value(classIndex));
			}
			index++;
		}

		// refactor training data with respect to the split clusters and build the classifier
		Instances trainingData = WekaUtil.mergeClassesOfInstances(data, instancesCluster);
		this.classifier.buildClassifier(trainingData);
		
		// recursively build classifiers for children
		this.children.stream().parallel().forEach(child -> {
			try {
				child.buildClassifier(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		this.trained = true;
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		double selection = -1;
		double best = 0;
		double[] dist = distributionForInstance(instance);
		for (int i = 0; i < dist.length; i++) {
			double score = dist[i];
			if (score > best) {
				best = score;
				selection = i;
			}
		}
		return containedClasses.get((int) selection);
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		
		assert trained : "Cannot get distribution from untrained classifier " + this.toStringWithOffset();
		
		/* compute distributions of child nodes */
		List<double[]> distributions = new ArrayList<>();
		int size = 0;
		for (MCTreeNode child : children) {
			double[] distributionForChild = child.distributionForInstance(instance);
			distributions.add(distributionForChild);
			size += distributionForChild.length;
		}
		assert size == this.containedClasses.size() : "Distribution cannot have " + size + " elements where " + containedClasses.size()
				+ " classes require probabilities. Tree is: " + toStringWithOffset();

		/* compute local distribution */
		Instance iNew = WekaUtil.getRefactoredInstance(instance, IntStream.range(0, this.children.size()).mapToObj(x -> x + ".0").collect(Collectors.toList()));
		double[] distributionForThisNode = this.classifier.distributionForInstance(iNew);
		
		/* compute product of probabilities */
		double[] distribution = new double[size];
		int indexForChild = 0;
		for (double[] distOfChild : distributions) {
			double factor = distributionForThisNode[indexForChild];
			int indexOfClassInChild = 0;
			for (double val : distOfChild) {
				int indexInDistribution = containedClasses.indexOf(children.get(indexForChild).containedClasses.get(indexOfClassInChild));
				distribution[indexInDistribution] = val * factor;
				indexOfClassInChild ++;
			}
			indexForChild++;
		}
		return distribution;
	}

	@Override
	public Capabilities getCapabilities() {
		return this.classifier.getCapabilities();
	}

	@Override
	public int getHeight() {
		return 1 + this.children.stream().map(x -> x.getHeight()).mapToInt(x -> (int) x).max().getAsInt();
	}

	@Override
	public int getDepthOfFirstCommonParent(final List<Integer> classes) {
		for (MCTreeNode child : this.children) {
			if (child.getContainedClasses().containsAll(classes)) {
				return 1 + child.getDepthOfFirstCommonParent(classes);
			}
		}
		return 1;
	}

	public static void clearCache() {
		classifierCache.clear();
	}

	public static ClassifierCache getClassifierCache() {
		return classifierCache;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public void setBaseClassifier(Classifier classifier) {
		
		assert classifier != null : "Cannot set null classifier!";

		this.classifierID = classifier.getClass().getName();
		switch (nodeType) {
		case ONEVSREST: {
			MultiClassClassifier mcc = new MultiClassClassifier();
			mcc.setClassifier(classifier);
			this.classifier = mcc;
			break;
		}
		case ALLPAIRS: {
			MultiClassClassifier mcc = new MultiClassClassifier();
			try {
				mcc.setOptions(new String[] { "-M", "" + 3 });
			} catch (Exception e) {
				e.printStackTrace();
			}
			mcc.setClassifier(classifier);
			this.classifier = mcc;
			break;
		}
		case DIRECT:
			this.classifier = classifier;
			break;
		default:
			break;
		}
	}

	public void setNodeType(EMCNodeType nodeType) {
		this.nodeType = nodeType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("(");
		sb.append(this.classifierID);
		sb.append(":");
		sb.append(this.nodeType);
		sb.append(")");

		sb.append("{");

		boolean first = true;
		for (MCTreeNode child : this.children) {
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
		return toStringWithOffset("");
	}

	public String toStringWithOffset(String offset) {
		StringBuilder sb = new StringBuilder();

		sb.append(offset);
		sb.append("(");
		sb.append(getContainedClasses());
		sb.append(":");
		sb.append(this.classifierID);
		sb.append(":");
		sb.append(this.nodeType);
		sb.append(") {");
		boolean first = true;
		for (MCTreeNode child : this.children) {
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

	@Override
	public Iterator<MCTreeNode> iterator() {
		Iterator<MCTreeNode> iterator = new Iterator<MCTreeNode>() {

			int currentlyTraversedChild = -1;
			Iterator<MCTreeNode> childIterator = null;

			@Override
			public boolean hasNext() {
				if (currentlyTraversedChild < 0) // if we are examening the root
					return true;
				if (children.isEmpty())
					return false;
				if (childIterator == null) {
					childIterator = children.get(currentlyTraversedChild).iterator();
				}
				if (childIterator.hasNext()) // if the current child has more items
					return true;
				if (currentlyTraversedChild == children.size() - 1) // if there are no more children
					return false;

				/* no set the iterator to the new child and return its val */
				currentlyTraversedChild++;
				childIterator = children.get(currentlyTraversedChild).iterator();
				return childIterator.hasNext();
			}

			@Override
			public MCTreeNode next() {
				if (currentlyTraversedChild == -1) {
					currentlyTraversedChild++;
					return MCTreeNode.this;
				} else {
					return childIterator.next();
				}
			}
		};
		return iterator;
	}
}
