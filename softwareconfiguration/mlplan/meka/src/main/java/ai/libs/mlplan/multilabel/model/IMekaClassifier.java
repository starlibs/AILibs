package ai.libs.mlplan.multilabel.model;

import org.api4.java.ai.ml.classification.IClassifier;

import meka.classifiers.multilabel.MultiLabelClassifier;

public interface IMekaClassifier extends IClassifier {

	public MultiLabelClassifier getClassifier();

}
