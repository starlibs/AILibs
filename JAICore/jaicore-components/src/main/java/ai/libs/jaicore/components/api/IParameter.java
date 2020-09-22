package ai.libs.jaicore.components.api;

import java.io.Serializable;

public interface IParameter extends Serializable {

	public String getName();

	public IParameterDomain getDefaultDomain();

	public Object getDefaultValue();

	public boolean isDefaultValue(final Object value);

	public boolean isNumeric();

	public boolean isCategorical();
}
