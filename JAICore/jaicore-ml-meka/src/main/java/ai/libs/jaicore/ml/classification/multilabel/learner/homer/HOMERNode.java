package ai.libs.jaicore.ml.classification.multilabel.learner.homer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.ArrayUtil;
import meka.classifiers.multilabel.AbstractMultiLabelClassifier;
import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.F;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;

public class HOMERNode extends AbstractMultiLabelClassifier {

	/**
	 *
	 */
	private static final long serialVersionUID = -2634579245812714183L;

	private static final Logger LOGGER = LoggerFactory.getLogger(HOMERNode.class);
	private static final boolean HIERARCHICAL_STRING = false;
	private static final double THRESHOLD = 0.5;

	private List<HOMERNode> children;

	private MultiLabelClassifier baselearner;
	private String baselearnerName;
	private boolean doThreshold = false;

	public HOMERNode(final HOMERNode... nodes) {
		this(Arrays.asList(nodes));
	}

	public HOMERNode(final List<HOMERNode> nodes) {
		this.children = nodes;
		Collections.sort(this.children, (o1, o2) -> {
			List<Integer> o1Labels = new LinkedList<>(o1.getLabels());
			List<Integer> o2Labels = new LinkedList<>(o2.getLabels());
			Collections.sort(o1Labels);
			Collections.sort(o2Labels);
			return o1Labels.get(0).compareTo(o2Labels.get(0));
		});
		this.baselearner = new BR();
	}

	public void setThreshold(final boolean doThreshold) {
		this.doThreshold = doThreshold;
	}

	public void setBaselearner(final MultiLabelClassifier baselearner) {
		this.baselearner = baselearner;
	}

	public String getBaselearnerName() {
		return this.baselearnerName;
	}

	public void setBaselearnerName(final String baselearnerName) {
		this.baselearnerName = baselearnerName;
	}

	public List<HOMERNode> getChildren() {
		return this.children;
	}

	/**
	 * @return The set of labels this node is responsible for.
	 */
	public Collection<Integer> getLabels() {
		Collection<Integer> labels = new HashSet<>();
		this.children.stream().map(HOMERNode::getLabels).forEach(labels::addAll);
		return labels;
	}

	@Override
	public void buildClassifier(final Instances trainingSet) throws Exception {
		LOGGER.debug("Build node with {} as a base learner", this.baselearnerName);
		Instances currentDataset = this.prepareInstances(trainingSet);
		List<Integer> removeInstances = new ArrayList<>();
		for (int i = 0; i < trainingSet.size(); i++) {
			boolean addedLabel = false;
			for (int j = 0; j < this.children.size(); j++) {
				int currentI = i;
				if (this.children.get(j).getLabels().stream().mapToDouble(x -> trainingSet.get(currentI).value(x)).sum() > 0) {
					addedLabel = true;
					currentDataset.get(i).setValue(j, 1.0);
				} else {
					currentDataset.get(i).setValue(j, 0.0);
				}
			}

			if (!addedLabel) {
				removeInstances.add(i);
			}
		}
		for (int i = removeInstances.size() - 1; i >= 0; i--) {
			currentDataset.remove((int) removeInstances.get(i));
		}

		this.baselearner.buildClassifier(currentDataset);

		for (HOMERNode child : this.children) {
			if (child.getLabels().size() > 1) {
				child.buildClassifier(trainingSet);
			}
		}
	}

	@Override
	public double[] distributionForInstance(final Instance testInstance) throws Exception {
		Instances copy = new Instances(testInstance.dataset(), 0);
		copy.add(testInstance.copy(testInstance.toDoubleArray()));

		Instances prepared = this.prepareInstances(copy);

		int length;
		int[] tDist = {};
		double[] dist = {};
		if (this.doThreshold) {
			tDist = ArrayUtil.thresholdDoubleToBinaryArray(this.baselearner.distributionForInstance(prepared.get(0)), THRESHOLD);
			length = tDist.length;
		} else {
			dist = this.baselearner.distributionForInstance(prepared.get(0));
			length = dist.length;
		}
		double[] returnDist = new double[testInstance.classIndex()];

		for (int i = 0; i < length; i++) {
			if (this.doThreshold && tDist[i] == 1) {
				if (this.children.get(i).getLabels().size() == 1) {
					returnDist[this.children.get(i).getLabels().iterator().next()] = 1.0;
				} else {
					ArrayUtil.add(returnDist, this.children.get(i).distributionForInstance(testInstance));
				}
			} else if (!this.doThreshold) {
				if (this.children.get(i).getLabels().size() == 1) {
					returnDist[this.children.get(i).getLabels().iterator().next()] = dist[i];
				} else {
					double[] childDist = this.children.get(i).distributionForInstance(testInstance);
					for (Integer childLabel : this.children.get(i).getLabels()) {
						returnDist[childLabel] = childDist[childLabel] * dist[i];
					}
				}
			}
		}
		return returnDist;
	}

	public Instances prepareInstances(final Instances dataset) throws Exception {
		Instances currentDataset = F.keepLabels(dataset, dataset.classIndex(), new int[] {});
		for (int i = this.children.size() - 1; i >= 0; i--) {
			Collection<Integer> labels = this.children.get(i).getLabels();
			Add add = new Add();
			add.setAttributeName(labels.stream().map(x -> dataset.attribute(x).name()).collect(Collectors.joining("&")));
			add.setAttributeIndex("first");
			add.setNominalLabels("0,1");
			add.setInputFormat(currentDataset);
			currentDataset = Filter.useFilter(currentDataset, add);
		}
		currentDataset.setClassIndex(this.children.size());

		return currentDataset;
	}

	public boolean isLeaf() {
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!HIERARCHICAL_STRING) {
			String actualBaselearnerName = this.baselearner.getOptions()[1];
			sb.append(actualBaselearnerName.substring(actualBaselearnerName.lastIndexOf('.') + 1, actualBaselearnerName.length()));
			sb.append("(");
			sb.append(this.children.stream().map(HOMERNode::toString).collect(Collectors.joining(",")));
			sb.append(")");
		}
		return sb.toString();
	}
}
