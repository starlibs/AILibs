package ai.libs.jaicore.ml.weka.classification.pipeline.featuregen;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.ml.weka.classification.pipeline.PreprocessingException;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class PolynomialFeatures implements FeatureGenerator {

	private static final long serialVersionUID = 5075237071717821149L;
	private boolean isPrepared;
	private int potence = 2;
	private List<Integer> indicesToSquare = new ArrayList<>();

	@Override
	public void prepare(final Instances data) throws PreprocessingException {
		ArrayList<Attribute> attributes = new ArrayList<>();
		this.indicesToSquare.clear();
		for (int i = 0; i < data.numAttributes(); i++) {
			if (data.attribute(i).isNumeric()) {
				attributes.add(new weka.core.Attribute("q" + i, false));
				this.indicesToSquare.add(i);
			}
		}
		this.isPrepared = true;
	}

	private Instances getEmptyDataset() {
		if (!this.isPrepared) {
			throw new IllegalStateException("Cannot get empty dataset before preparation");
		}
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (int indexToSquare : this.indicesToSquare) {
			attributes.add(new Attribute("pow_" + this.potence + "_" + indexToSquare, false));
		}
		return new Instances("potences", attributes, 0);
	}

	@Override
	public Instance apply(final Instance data) throws PreprocessingException {
		Instance copy = new DenseInstance(this.indicesToSquare.size());
		int i = 0;
		for (int index : this.indicesToSquare) {
			copy.setValue(i++,Math.pow(data.value(index), this.potence));
		}
		Instances dataset = this.getEmptyDataset();
		dataset.add(copy);
		copy.setDataset(dataset);
		return copy;
	}

	@Override
	public Instances apply(final Instances data) throws PreprocessingException {
		Instances copy = this.getEmptyDataset();
		for (Instance inst : data) {
			Instance modInst = this.apply(inst);
			copy.add(modInst);
			modInst.setDataset(copy);
		}
		return copy;
	}

	@Override
	public boolean isPrepared() {
		return this.isPrepared;
	}

	public int getPotence() {
		return this.potence;
	}

	public void setPotence(final int potence) {
		this.potence = potence;
	}
}
