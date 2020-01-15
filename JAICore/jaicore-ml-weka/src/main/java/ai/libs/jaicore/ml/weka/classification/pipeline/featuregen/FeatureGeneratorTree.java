package ai.libs.jaicore.ml.weka.classification.pipeline.featuregen;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.weka.classification.pipeline.PreprocessingException;
import weka.core.Instance;
import weka.core.Instances;

public class FeatureGeneratorTree implements FeatureGenerator {

	private static final long serialVersionUID = 3071755243287146060L;
	private final transient Logger logger = LoggerFactory.getLogger(FeatureGeneratorTree.class);
	private final FeatureGenerator root;
	private final List<FeatureGeneratorTree> children = new ArrayList<>();
	private boolean prepared;

	public FeatureGeneratorTree(final FeatureGenerator root) {
		this.root = root;
	}

	public void addChild(final FeatureGenerator child) {
		this.children.add(new FeatureGeneratorTree(child));
	}

	public void removeChild(final FeatureGeneratorTree child) {
		this.children.removeIf(c -> c.root.equals(child));
	}

	public FeatureGenerator getRoot() {
		return this.root;
	}

	@Override
	public void prepare(final Instances data) throws PreprocessingException {

		this.logger.info("Starting preparation of FeatureGeneratorTree ({}) for {}x{}-matrix.", this.root.getClass().getName(), data.size(), data.numAttributes());

		try {
			/* prepare children and apply them in order to get the data necessary to prepare the local feature generator */
			for (FeatureGeneratorTree child : this.children) {
				child.prepare(data);
			}
			Instances mergedInstances = new Instances(data);
			for (FeatureGeneratorTree child : this.children) {
				Instances instancesGeneratedByChild = child.apply(data);
				mergedInstances = Instances.mergeInstances(mergedInstances, instancesGeneratedByChild);
			}

			/* prepare local feature generator */
			this.root.prepare(mergedInstances);
			Instances result = this.apply(data);
			this.logger.info("Preparation of FeatureGeneratorTree ({}) ready. Result will be a {}x{}-matrix", this.root.getClass().getName(), result.size(), result.numAttributes());
			this.prepared = true;
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
	}

	@Override
	public Instance apply(final Instance data) throws PreprocessingException {
		try {
			Instances instances = new Instances(data.dataset());
			instances.clear();
			instances.add(data);
			return this.apply(instances).firstInstance();
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
	}

	@Override
	public Instances apply(final Instances data) throws PreprocessingException {
		try {
			Instances mergedInstances = new Instances(data);
			for (FeatureGeneratorTree child : this.children) {
				mergedInstances = Instances.mergeInstances(mergedInstances, child.apply(data));
			}
			return this.root.apply(mergedInstances);
		} catch (Exception e) {
			throw new PreprocessingException(e);
		}
	}

	@Override
	public boolean isPrepared() {
		return this.prepared;
	}
}
