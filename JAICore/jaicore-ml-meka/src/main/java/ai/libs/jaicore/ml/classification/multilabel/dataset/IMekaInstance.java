package ai.libs.jaicore.ml.classification.multilabel.dataset;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.attributedobjects.IElementDecorator;

import weka.core.Instance;

public interface IMekaInstance extends ILabeledInstance, IElementDecorator<Instance>, Clusterable {

	@Override
	public Instance getElement();

}
