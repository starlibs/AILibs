package hasco.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

public interface IParameterDomain {
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
	@JsonSubTypes({ @Type(value = NumericParameterDomain.class, name = "numeric"), @Type(value = CategoricalParameterDomain.class, name = "categorical"), @Type(value = BooleanParameterDomain.class, name = "boolean") })
	public boolean contains(Object item);

	public boolean subsumes(IParameterDomain otherDomain);
}
