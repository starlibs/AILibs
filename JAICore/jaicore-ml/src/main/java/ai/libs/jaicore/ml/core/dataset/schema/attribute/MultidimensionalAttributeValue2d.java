package ai.libs.jaicore.ml.core.dataset.schema.attribute;

/**
 * subclass with the single functionallity of defining the generic value TODO is it needed?
 *
 * @author Lukas Fehring
 *
 */
public class MultidimensionalAttributeValue2d extends MultidimensionalAttributeValue<double[][]> {

	public MultidimensionalAttributeValue2d(final MultidimensionalAttribute2d attribute, final double[][] value) {
		super(attribute, value);
	}

}
