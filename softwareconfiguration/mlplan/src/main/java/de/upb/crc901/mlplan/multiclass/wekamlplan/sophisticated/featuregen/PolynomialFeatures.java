package de.upb.crc901.mlplan.multiclass.wekamlplan.sophisticated.featuregen;

import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class PolynomialFeatures implements FeatureGenerator {
	
	private boolean isPrepared;
	private int potence = 2;
	private List<Integer> indicesToSquare = new ArrayList<>();

	@Override
	public void prepare(Instances data) throws Exception {
		ArrayList<Attribute> attributes = new ArrayList<>();
		indicesToSquare.clear();
		for (int i = 0; i < data.numAttributes(); i++) {
			if (data.attribute(i).isNumeric()) {
				attributes.add(new weka.core.Attribute("q" + i, false));
				indicesToSquare.add(i);
			}
		}
//		Instances squares = new Instances("squares", attributes, data.size());
		isPrepared = true;
	}
	
	private Instances getEmptyDataset() {
		if (!isPrepared)
			throw new IllegalStateException("Cannot get empty dataset before preparation");
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (int indexToSquare : indicesToSquare) {
			attributes.add(new Attribute("pow_" + potence + "_" + indexToSquare, false));
		}
		return new Instances("potences", attributes, 0);
	}

	@Override
	public Instance apply(Instance data) throws Exception {
		Instance copy = new DenseInstance(indicesToSquare.size());
		int i = 0;
		for (int index : indicesToSquare) {
			copy.setValue(i++,Math.pow(data.value(index), potence));
		}
		Instances dataset = getEmptyDataset();
		dataset.add(copy);
		copy.setDataset(dataset);
		return copy;
	}

	@Override
	public Instances apply(Instances data) throws Exception {
		Instances copy = getEmptyDataset();
		for (Instance inst : data) {
			Instance modInst = apply(inst);
			copy.add(modInst);
			modInst.setDataset(copy);
		}
		return copy;
	}

	@Override
	public boolean isPrepared() {
		return isPrepared;
	}

	public int getPotence() {
		return potence;
	}

	public void setPotence(int potence) {
		this.potence = potence;
	}
}
