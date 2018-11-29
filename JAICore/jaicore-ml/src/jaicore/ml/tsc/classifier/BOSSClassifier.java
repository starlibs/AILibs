package jaicore.ml.tsc.classifier;

public class BOSSClassifier extends TSClassifier<Integer> {
	
	public BOSSClassifier() {
		this.algorithm = new BOSSAlgorithm();
	}
}
