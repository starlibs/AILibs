package de.upb.crc901.mlplan.multiclass.wekamlplan.sophisticated.featuregen;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.sets.SetUtil;
import jaicore.basic.sets.SetUtil.Pair;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class InteractingFeatures implements FeatureGenerator {
	
	private boolean isPrepared;
	private List<Integer> indicesToInteract = new ArrayList<>();

	@Override
	public void prepare(Instances data) throws Exception {
		ArrayList<Attribute> attributes = new ArrayList<>();
		indicesToInteract.clear();
		for (int i = 0; i < data.numAttributes(); i++) {
			if (data.attribute(i).isNumeric()) {
				attributes.add(new weka.core.Attribute("q" + i, false));
				indicesToInteract.add(i);
			}
		}
//		Instances squares = new Instances("squares", attributes, data.size());
		isPrepared = true;
	}
	
	private Instances getEmptyDataset() {
		if (!isPrepared)
			throw new IllegalStateException("Cannot get empty dataset before preparation");
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (Pair<Integer, Integer> pair : SetUtil.cartesianProduct(indicesToInteract, indicesToInteract)) {
			if (pair.getX() < pair.getY()) {
				attributes.add(new Attribute("interaction_" + pair.getX() + "_" + pair.getY(), false));
			}
		}
		return new Instances("interaction", attributes, 0);
	}

	@Override
	public Instance apply(Instance data) throws Exception {
		Instance newInstance = new DenseInstance(((int) Math.pow(indicesToInteract.size(), 2) - indicesToInteract.size()) / 2);
		int index = 0;
		for (Pair<Integer, Integer> pair : SetUtil.cartesianProduct(indicesToInteract, indicesToInteract)) {
			if (pair.getX() < pair.getY()) {
				newInstance.setValue(index ++, data.value(pair.getX()) * data.value(pair.getY()));
			}
		}
		Instances dataset = getEmptyDataset();
		dataset.add(newInstance);
		newInstance.setDataset(dataset);
		return newInstance;
	}

	@Override
	public Instances apply(Instances data) throws Exception {
		Instances newDataset = getEmptyDataset();
		for (Instance inst : data) {
			Instance modInst = apply(inst);
			newDataset.add(modInst);
			modInst.setDataset(newDataset);
		}
		return newDataset;
	}

	@Override
	public boolean isPrepared() {
		return isPrepared;
	}
}
