package ai.libs.jaicore.ml.classification.multilabel.learner.plt;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.tree.ITree;
import meka.classifiers.multilabel.AbstractMultiLabelClassifier;
import meka.core.F;
import meka.core.MLUtils;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class ProbabilisticLabelTree extends AbstractMultiLabelClassifier {

	private static final Logger L = LoggerFactory.getLogger(ProbabilisticLabelTree.class);

	public static final String FN_LABEL = "label";
	public static final String FN_BASELEARNER = "baselearner";

	/**
	 *
	 */
	private static final long serialVersionUID = -1780592274053495557L;

	// model structure
	private ITree tree;

	/* Treshold to consider a label still to be relevant. */
	private double threshold = 0.5;

	// state variables
	private int numLabels = -1;
	private Instances genericSchema = null;

	public ProbabilisticLabelTree(final ITree tree) {
		this.tree = tree;
	}

	public ITree getTree() {
		return this.tree;
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		this.numLabels = data.classIndex();
		Instances training = F.keepLabels(new Instances(data), data.classIndex(), new int[] { 0 });
		this.genericSchema = new Instances(training, 0);
		this.genericSchema.setClassIndex(0);

		double[][] labels = MLUtils.getYfromD(data);
		for (ITree successor : this.tree.getSuccessors()) {
			this.buildNode(successor, labels, training);
		}
	}

	protected void buildNode(final ITree node, final double[][] labels, final Instances trainingData) throws Exception {
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		for (ITree n : node.getSuccessors()) {
			this.buildNode(n, labels, trainingData);
		}

		Classifier baselearner = (Classifier) node.getAnnotation(FN_BASELEARNER);
		Instances baselearnerData = new Instances(trainingData);
		for (int i = 0; i < baselearnerData.size(); i++) {
			int currentI = i;
			baselearnerData.get(i).setValue(0, node.getLeaves().stream().mapToDouble(x -> labels[currentI][(int) x.getAnnotation(FN_LABEL)]).sum() > 0 ? 1.0 : 0.0);
		}

		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		baselearnerData.setClassIndex(0);
		baselearner.buildClassifier(baselearnerData);
	}

	@Override
	public double[] distributionForInstance(final Instance instance) throws Exception {
		if (this.numLabels == -1 || this.genericSchema == null) {
			throw new IllegalStateException("Model needs to be trained first before making predictions");
		}

		// transform instance
		Instances d = new Instances(instance.dataset(), 0);
		d.add(instance);
		d = F.keepLabels(d, d.classIndex(), new int[] { 0 });
		Instance transformedInstance = d.get(0);
		transformedInstance.setDataset(this.genericSchema);

		// compute distribution
		double[] dist = new double[this.numLabels];
		IntStream.range(0, dist.length).forEach(x -> dist[x] = 1.0);
		this.distributionForInstance(this.tree, transformedInstance, dist);
		return dist;
	}

	private void distributionForInstance(final ITree n, final Instance instance, final double[] dist) throws Exception {
		for (ITree child : n.getSuccessors()) {
			Classifier c = (Classifier) child.getAnnotation(FN_BASELEARNER);
			double relevance = c.distributionForInstance(instance)[1];
			L.debug("{} --> {}", child.getLeaves().stream().map(x -> x.getAnnotation(FN_LABEL) + "").collect(Collectors.joining("-")), relevance);

			boolean cutoff = true;
			for (ITree leaf : child.getLeaves()) {
				int index = (int) leaf.getAnnotation(FN_LABEL);
				dist[index] *= relevance;
				cutoff &= dist[index] < this.threshold;
			}
			if (!cutoff && !child.isLeaf()) {
				this.distributionForInstance(child, instance, dist);
			}
		}
	}

	@Override
	public String toString() {
		return this.tree.toString(t -> {
			if (t.getParent() == null) {
				return "PLT";
			} else {
				String n = ((Classifier) t.getAnnotation(FN_BASELEARNER)).getClass().getSimpleName();
				if (t.isLeaf()) {
					n += "(" + t.getAnnotation(FN_LABEL) + ")";
				}
				return n;
			}
		});
	}

}
