package ai.libs.jaicore.ml.classification.multilabel.learner.homer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.api4.java.ai.ml.classification.multilabel.learner.IMultiLabelClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.classification.multilabel.learner.AMultiLabelClassifier;
import ai.libs.jaicore.ml.classification.multilabel.learner.MekaClassifier;
import meka.classifiers.multilabel.BR;

public class HOMERNode extends AMultiLabelClassifier implements IMultiLabelClassifier {

	private static final Logger LOGGER = LoggerFactory.getLogger(HOMERNode.class);
	private static final boolean HIERARCHICAL_STRING = false;

	private List<HOMERNode> children;
	private IMultiLabelClassifier baselearner;

	public HOMERNode(final HOMERNode... nodes) {
		this.children = Arrays.asList(nodes);
		this.baselearner = new MekaClassifier(new BR());
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
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (!HIERARCHICAL_STRING) {
			sb.append("(");
			sb.append(this.children.stream().map(HOMERNode::toString).collect(Collectors.joining("),(")));
			sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {

	}

	@Override
	public IMultiLabelClassification predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		return null;
	}

}
