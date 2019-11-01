package ai.libs.mlplan.multiclass.wekamlplan.sophisticated.featuregen;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class InteractingFeatures implements FeatureGenerator {

	private boolean isPrepared;
	private List<Integer> indicesToInteract = new ArrayList<>();

	@Override
	public void prepare(final Instances data) throws Exception {
		ArrayList<Attribute> attributes = new ArrayList<>();
		this.indicesToInteract.clear();
		for (int i = 0; i < data.numAttributes(); i++) {
			if (data.attribute(i).isNumeric()) {
				attributes.add(new weka.core.Attribute("q" + i, false));
				this.indicesToInteract.add(i);
			}
		}
		// Instances squares = new Instances("squares", attributes, data.size());
		this.isPrepared = true;
	}

	private Instances getEmptyDataset() {
		if (!this.isPrepared) {
			throw new IllegalStateException("Cannot get empty dataset before preparation");
		}
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (Pair<Integer, Integer> pair : SetUtil.cartesianProduct(this.indicesToInteract, this.indicesToInteract)) {
			if (pair.getX() < pair.getY()) {
				attributes.add(new Attribute("interaction_" + pair.getX() + "_" + pair.getY(), false));
			}
		}
		return new Instances("interaction", attributes, 0);
	}

	@Override
	public Instance apply(final Instance data) throws Exception {
		Instance newInstance = new DenseInstance(((int) Math.pow(this.indicesToInteract.size(), 2) - this.indicesToInteract.size()) / 2);
		int index = 0;
		for (Pair<Integer, Integer> pair : SetUtil.cartesianProduct(this.indicesToInteract, this.indicesToInteract)) {
			if (pair.getX() < pair.getY()) {
				newInstance.setValue(index++, data.value(pair.getX()) * data.value(pair.getY()));
			}
		}
		Instances dataset = this.getEmptyDataset();
		dataset.add(newInstance);
		newInstance.setDataset(dataset);
		return newInstance;
	}

	@Override
	public Instances apply(final Instances data) throws Exception {
		Instances newDataset = this.getEmptyDataset();
		for (Instance inst : data) {
			Instance modInst = this.apply(inst);
			newDataset.add(modInst);
			modInst.setDataset(newDataset);
		}
		return newDataset;
	}

	@Override
	public boolean isPrepared() {
		return this.isPrepared;
	}
}
