package ai.libs.jaicore.components.api;

import java.util.Collection;

public interface IComponent {

	/**
	 * @return Name of the component
	 */
	public String getName();

	/**
	 * @return Names of interfaces offered by this component
	 */
	public Collection<String> getProvidedInterfaces();

	/**
	 * @return The required interfaces of this component
	 */
	public Collection<IRequiredInterfaceDefinition> getRequiredInterfaces();

	/**
	 * @param id internal id of the required interface in the component
	 * @return The interface description for the respective required interface id
	 */
	public IRequiredInterfaceDefinition getRequiredInterfaceDescriptionById(String id);

	/**
	 * @return The parameters of this component
	 */
	public Collection<IParameter> getParameters();

	/**
	 * @param name The name of the desired parameter
	 * @return The parameter object
	 */
	public IParameter getParameter(String name);

	public Collection<IParameterDependency> getParameterDependencies();
}
