package jaicore.ml.classification.multiclass.reduction.reducer;

import java.util.Set;

import jaicore.ml.classification.multiclass.reduction.EMCNodeType;
import weka.classifiers.Classifier;

class Decision {

	private final Set<String> lft;
	private final Set<String> rgt;
	private final EMCNodeType classificationType;
	private final Classifier baseClassifier;

	public Decision(Set<String> lft, Set<String> rgt, EMCNodeType classificationType, Classifier baseClassifier) {
		super();
		this.lft = lft;
		this.rgt = rgt;
		this.classificationType = classificationType;
		this.baseClassifier = baseClassifier;
	}

	public Set<String> getLft() {
		return lft;
	}

	public Set<String> getRgt() {
		return rgt;
	}

	public EMCNodeType getClassificationType() {
		return classificationType;
	}

	public Classifier getBaseClassifier() {
		return baseClassifier;
	}

	@Override
	public String toString() {
		return "Decision [lft=" + lft + ", rgt=" + rgt + ", classificationType=" + classificationType + ", baseClassifier=" + baseClassifier + "]";
	}
}