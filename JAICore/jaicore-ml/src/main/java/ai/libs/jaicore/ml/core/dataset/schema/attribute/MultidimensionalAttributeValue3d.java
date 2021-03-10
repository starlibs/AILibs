package ai.libs.jaicore.ml.core.dataset.schema.attribute;

/**
 * subclass with the single functionallity of defining the generic value TODO is it needed?
 *
 * @author Lukas Fehring
 *
 */
public class MultidimensionalAttributeValue3d extends MultidimensionalAttributeValue<double[][][]> {

	public MultidimensionalAttributeValue3d(final MultidimensionalAttribute<double[][][]> attribute, final double[][][] value) {
		super(attribute, value);
	}

}
