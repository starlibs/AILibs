package hasco.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type")
@JsonSubTypes({
	@Type(value = NumericParameterDomain.class, name = "numeric"),
	@Type(value = CategoricalParameterDomain.class, name = "categorical"),
	@Type(value = BooleanParameterDomain.class, name = "boolean")	
})

public abstract class ParameterDomain {
	
	abstract public boolean contains(Object item);
	
	abstract public boolean subsumes(ParameterDomain otherDomain);
}
