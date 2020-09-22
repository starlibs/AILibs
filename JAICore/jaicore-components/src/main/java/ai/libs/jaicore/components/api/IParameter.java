package ai.libs.jaicore.components.api;

public interface IParameter {

	public String getName();

	public IParameterDomain getDefaultDomain();

	public Object getDefaultValue();

	public boolean isDefaultValue(final Object value);

	public boolean isNumeric();

	public boolean isCategorical();
}
