package ai.libs.jaicore.components.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ai.libs.jaicore.basic.sets.Interval;
import ai.libs.jaicore.components.api.IParameterDomain;

public class NumericParameterDomain extends Interval implements IParameterDomain {

	private static final long serialVersionUID = 7445944456747209248L;

	@JsonCreator
	public NumericParameterDomain(@JsonProperty("integer") final boolean isInteger, @JsonProperty("min") final double min, @JsonProperty("max") final double max) {
		super(isInteger, min, max);
	}

	@Override
	public boolean subsumes(final IParameterDomain otherDomain) {
		if (!(otherDomain instanceof NumericParameterDomain)) {
			return false;
		}
		return this.subsumes((Interval)otherDomain);
	}

	@Override
	public boolean isEquals(final Object obj0, final Object obj1) {
		return Math.abs(Double.parseDouble(obj0 + "") - Double.parseDouble(obj1 + "")) < 1E-8;
	}
}
