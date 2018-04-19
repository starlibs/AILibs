package hasco.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Component {
  private final String name;
  private final Collection<String> requiredInterfaces = new HashSet<>(), providedInterfaces = new HashSet<>();
  private final List<Parameter> parameters = new ArrayList<>();

  public Component(final String name) {
    super();
    this.name = name;
  }

  public Component(final String name, final Collection<String> requiredInterfaces, final Collection<String> providedInterfaces, final List<Parameter> parameters) {
    this(name);
    this.requiredInterfaces.addAll(requiredInterfaces);
    this.providedInterfaces.addAll(providedInterfaces);
    this.parameters.addAll(parameters);
  }

  public String getName() {
    return this.name;
  }

  public Collection<String> getRequiredInterfaces() {
    return this.requiredInterfaces;
  }

  public Collection<String> getProvidedInterfaces() {
    return this.providedInterfaces;
  }

  public List<Parameter> getParameters() {
    return this.parameters;
  }

  public void addProvidedInterface(final String interfaceName) {
    this.providedInterfaces.add(interfaceName);
  }

  public void addRequiredInterface(final String interfaceName) {
    this.requiredInterfaces.add(interfaceName);
  }

  public void addParameter(final Parameter param) {
    this.parameters.add(param);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(this.providedInterfaces);
    sb.append(":");
    sb.append(this.name);
    sb.append("(");
    boolean first = true;
    for (Parameter p : this.parameters) {
      if (first) {
        first = false;
      } else {
        sb.append(",");
      }
      sb.append(p);
    }
    sb.append(")");
    sb.append(":");
    sb.append(this.requiredInterfaces);

    return sb.toString();
  }
}
