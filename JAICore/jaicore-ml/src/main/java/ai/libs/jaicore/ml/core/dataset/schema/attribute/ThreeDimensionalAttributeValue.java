package ai.libs.jaicore.ml.core.dataset.schema.attribute;

/**
 * Holds values of {@link ThreeDimensionalAttribute}
 *
 * @author Lukas Fehring
 *
 */
public class ThreeDimensionalAttributeValue extends MultidimensionalAttributeValue<double[][][]> {

	public ThreeDimensionalAttributeValue(final MultidimensionalAttribute<double[][][]> attribute, final double[][][] value) {
		super(attribute, value);
	}

}
