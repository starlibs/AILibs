package jaicore.ml.classification.multiclass.reduction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import jaicore.ml.WekaUtil;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.meta.MultiClassClassifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WekaException;

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
	private boolean fromCache = false;

	public static AtomicInteger cacheRetrievals = new AtomicInteger();
	private static Map<String, Classifier> classifierCacheMap = new HashMap<>();
	private static Lock classifierCacheMapLock = new ReentrantLock();

	public MCTreeNode(final Classifier left, final Classifier right, final String baseClassifier) {
		this.containedClasses = new ArrayList<>();
	}

	public MCTreeNode(final List<Integer> containedClasses) {
		this.containedClasses = containedClasses;
	}

	public MCTreeNode(final List<Integer> containedClasses, final EMCNodeType nodeType, final String classifierID) throws Exception {
		this(containedClasses, nodeType, AbstractClassifier.forName(classifierID, null));
	}

	public MCTreeNode(final List<Integer> containedClasses, final EMCNodeType nodeType, final Classifier baseClassifier) {
		this(containedClasses);
		this.setNodeType(nodeType);
		this.setBaseClassifier(baseClassifier);
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
		if (this.classifier == null) {
			return false;
		}
		if (this.children.isEmpty()) {
			return false;
		}
		for (MCTreeNode child : this.children) {
			if (!child.isCompletelyConfigured()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		assert (this.getNodeType() != EMCNodeType.MERGE) : "MERGE node detected while building classifier. This must not happen!";
		assert !data.isEmpty() : "Cannot train MCTree with empty set of instances.";
		assert !this.children.isEmpty() : "Cannot train MCTree without children";

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

		String classifierKey = this.classifier.getClass().getName() + "#" + instancesCluster + "#" + data.size() + "#" + new HashCodeBuilder().append(data.toString()).toHashCode();

		// refactor training data with respect to the split clusters and build the classifier
		Instances trainingData = WekaUtil.mergeClassesOfInstances(data, instancesCluster);

		Classifier cachedClassifier = null;
		classifierCacheMapLock.lock();
		try {
			cachedClassifier = AbstractClassifier.makeCopy(classifierCacheMap.get(classifierKey));
			this.fromCache = true;
		} finally {
			classifierCacheMapLock.unlock();
		}
		cachedClassifier = null;

		if (cachedClassifier != null) {
			this.classifier = cachedClassifier;
		} else {
			try {
				this.classifier.buildClassifier(trainingData);
			} catch (WekaException e) {
				this.classifier = new ZeroR();
				this.classifier.buildClassifier(trainingData);
			}

			classifierCacheMapLock.lock();
			try {
				classifierCacheMap.put(classifierKey, this.classifier);
			} finally {
				classifierCacheMapLock.unlock();
			}

		}

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
		double[] dist = this.distributionForInstance(instance);
		for (int i = 0; i < dist.length; i++) {
			double score = dist[i];
			if (score > best) {
				best = score;
				selection = i;
			}
		}
		return this.containedClasses.get((int) selection);
	}

	public void distributionForInstance(final Instance instance, final double[] distribution) throws Exception {
		Instance iNew = WekaUtil.getRefactoredInstance(instance, IntStream.range(0, this.children.size()).mapToObj(x -> x + ".0").collect(Collectors.toList()));

		double[] localDistribution = new double[this.containedClasses.size()];
		localDistribution = this.classifier.distributionForInstance(iNew);

		for (MCTreeNode child : this.children) {
			child.distributionForInstance(instance, distribution);
			int indexOfChild = this.children.indexOf(child);

			for (Integer classContainedInChild : child.getContainedClasses()) {
				distribution[classContainedInChild] *= localDistribution[indexOfChild];
			}
		}
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		assert this.trained : "Cannot get distribution from untrained classifier " + this.toStringWithOffset();

	double[] classDistribution = new double[this.containedClasses.size()];
	this.distributionForInstance(instance, classDistribution);
	return classDistribution;
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
		classifierCacheMap.clear();
	}

	public static Map<String, Classifier> getClassifierCache() {
		return classifierCacheMap;
	}

	public Classifier getClassifier() {
		return this.classifier;
	}

	public void setBaseClassifier(final Classifier classifier) {

		assert classifier != null : "Cannot set null classifier!";

		this.classifierID = classifier.getClass().getName();
		switch (this.nodeType) {
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

	public void setNodeType(final EMCNodeType nodeType) {
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
		return this.toStringWithOffset("");
	}

	public String toStringWithOffset(final String offset) {
		StringBuilder sb = new StringBuilder();

		sb.append(offset);
		sb.append("(");
		sb.append(this.getContainedClasses());
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
		return new Iterator<MCTreeNode>() {

			private int currentlyTraversedChild = -1;
			private Iterator<MCTreeNode> childIterator = null;

			@Override
			public boolean hasNext() {
				if (this.currentlyTraversedChild < 0) {
					return true;
				}
				if (MCTreeNode.this.children.isEmpty()) {
					return false;
				}
				if (this.childIterator == null) {
					this.childIterator = MCTreeNode.this.children.get(this.currentlyTraversedChild).iterator();
				}
				if (this.childIterator.hasNext()) {
					return true;
				}
				if (this.currentlyTraversedChild == MCTreeNode.this.children.size() - 1) {
					return false;
				}

				/* no set the iterator to the new child and return its val */
				this.currentlyTraversedChild++;
				this.childIterator = MCTreeNode.this.children.get(this.currentlyTraversedChild).iterator();
				return this.childIterator.hasNext();
			}

			@Override
			public MCTreeNode next() {
				if (this.currentlyTraversedChild == -1) {
					this.currentlyTraversedChild++;
					return MCTreeNode.this;
				} else {
					return this.childIterator.next();
				}
			}
		};
	}
}
