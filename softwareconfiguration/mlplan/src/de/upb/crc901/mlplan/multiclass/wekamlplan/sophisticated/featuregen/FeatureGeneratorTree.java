package de.upb.crc901.mlplan.multiclass.wekamlplan.sophisticated.featuregen;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instance;
import weka.core.Instances;

public class FeatureGeneratorTree implements FeatureGenerator {
	
	private final Logger logger = LoggerFactory.getLogger(FeatureGeneratorTree.class);
    private final FeatureGenerator root;
    private final List<FeatureGeneratorTree> children = new ArrayList<>();
    private boolean prepared;

    public FeatureGeneratorTree(FeatureGenerator root) {
        this.root = root;
    }
    
    public void addChild(FeatureGenerator child) {
    	children.add(new FeatureGeneratorTree(child));
    }
    
    public void removeChild(FeatureGeneratorTree child) {
    	children.removeIf(c -> c.root.equals(child));
    }

	public FeatureGenerator getRoot() {
		return root;
	}

	@Override
	public void prepare(Instances data) throws Exception {
		
		logger.info("Starting preparation of FeatureGeneratorTree ({}) for {}x{}-matrix.", root.getClass().getName(), data.size(), data.numAttributes());
		
		/* prepare children and apply them in order to get the data necessary to prepare the local feature generator */
		for (FeatureGeneratorTree child : children)
			child.prepare(data);
		Instances mergedInstances = new Instances(data);
		for (FeatureGeneratorTree child : children) {
			Instances instancesGeneratedByChild = child.apply(data);
			mergedInstances = Instances.mergeInstances(mergedInstances, instancesGeneratedByChild);
		}
		
		/* prepare local feature generator */
		this.root.prepare(mergedInstances);
		Instances result = apply(data);
		logger.info("Preparation of FeatureGeneratorTree ({}) ready. Result will be a {}x{}-matrix", root.getClass().getName(), result.size(), result.numAttributes());
		prepared = true;
	}

	@Override
	public Instance apply(Instance data) throws Exception {
		Instances instances = new Instances(data.dataset());
		instances.clear();
		instances.add(data);
		return apply(instances).firstInstance();
	}

	@Override
	public Instances apply(Instances data) throws Exception {
		Instances mergedInstances = new Instances(data);
		for (FeatureGeneratorTree child : children)
			mergedInstances = Instances.mergeInstances(mergedInstances, child.apply(data));
		return root.apply(mergedInstances);
	}

	@Override
	public boolean isPrepared() {
		return prepared;
	}
}
