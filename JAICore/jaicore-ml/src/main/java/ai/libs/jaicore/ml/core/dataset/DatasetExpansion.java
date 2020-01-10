package ai.libs.jaicore.ml.core.dataset;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public class DatasetExpansion {
	private final List<IAttribute> attributesBeforeExpansion;
	private final List<IAttribute> expansionAttributes;
	private final Map<IAttribute, Function<ILabeledInstance, Double>> transformations;
	public DatasetExpansion(final List<IAttribute> attributesBeforeExpansion, final List<IAttribute> expansionAttributes, final Map<IAttribute, Function<ILabeledInstance, Double>> transformations) {
		super();
		this.attributesBeforeExpansion = attributesBeforeExpansion;
		this.expansionAttributes = expansionAttributes;
		this.transformations = transformations;
	}


}
