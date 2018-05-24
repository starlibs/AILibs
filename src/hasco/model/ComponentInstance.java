package hasco.model;

import java.util.Map;

/**
 * For a given component, a composition defines all parameter values and the required interfaces
 * (recursively)
 *
 * @author fmohr
 *
 */
public class ComponentInstance {
  private final Component component;
  private final Map<String, String> parameterValues;
  /** The satisfactionOfRequiredInterfaces map maps from Interface IDs to ComopnentInstances */
  private final Map<String, ComponentInstance> satisfactionOfRequiredInterfaces;

  public ComponentInstance(final Component component, final Map<String, String> parameterValues, final Map<String, ComponentInstance> satisfactionOfRequiredInterfaces) {
    super();
    this.component = component;
    this.parameterValues = parameterValues;
    this.satisfactionOfRequiredInterfaces = satisfactionOfRequiredInterfaces;
  }

  public Component getComponent() {
    return this.component;
  }

  public Map<String, String> getParameterValues() {
    return this.parameterValues;
  }

  /**
   * @return This method returns a mapping of interface IDs to component instances.
   */
  public Map<String, ComponentInstance> getSatisfactionOfRequiredInterfaces() {
    return this.satisfactionOfRequiredInterfaces;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(this.component);
    sb.append(this.parameterValues);

    return sb.toString();
  }
}
