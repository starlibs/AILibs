package ai.libs.jaicore.ml.core.timeseries.dataset.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ITimeseriesAttribute;
import org.api4.java.common.timeseries.ITimeseries;

import ai.libs.jaicore.ml.core.dataset.schema.attribute.AGenericObjectAttribute;

/**
 *
 * @author mwever
 *
 * @param <O> Type of the representation of a timeseries.
 */
public abstract class ATimeseriesAttribute<O> extends AGenericObjectAttribute<ITimeseries<O>> implements ITimeseriesAttribute<O> {

	/**
	 *
	 */
	private static final long serialVersionUID = -3411560349820853762L;
	private int length;

	protected ATimeseriesAttribute(final String name, final int length) {
		super(name);
		this.length = length;
	}

	/**
	 * Get the length of this time series attribute type.
	 *
	 * @return The length respec. the number of datapoints of this time series
	 *         attribute.
	 */
	public int getLength() {
		return this.length;
	}

	public void setLength(final int length) {
		if (length < 0) {
			throw new IllegalArgumentException("the length has to be greater than or equal to zero.");
		}
		this.length = length;
	}

}
