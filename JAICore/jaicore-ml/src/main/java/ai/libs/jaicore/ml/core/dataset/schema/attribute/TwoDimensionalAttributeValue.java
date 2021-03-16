package ai.libs.jaicore.ml.core.dataset.schema.attribute;

/**
 * Holds values of {@link TwoDimensionalAttribute}
 *
 * @author Lukas Fehring
 *
 */
public class TwoDimensionalAttributeValue extends MultidimensionalAttributeValue<double[][]> {

	public TwoDimensionalAttributeValue(final TwoDimensionalAttribute attribute, final double[][] value) {
		super(attribute, value);
	}

}
