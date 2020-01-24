package ai.libs.jaicore.ml.weka.dataset;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.attributedobjects.IElementDecorator;

import weka.core.Instance;

public interface IWekaInstance extends ILabeledInstance, IElementDecorator<Instance>, Clusterable {

	@Override
	public Instance getElement();

}
