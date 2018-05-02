package hasco.model;

import java.util.Arrays;
import java.util.Collection;

public class CategoricalParameter extends Parameter {
  private final String[] values;

  public CategoricalParameter(final String name, final String[] values, final String defaultValue) {
    super(name, defaultValue);
    this.values = values;
  }

  public CategoricalParameter(final String name, final Collection<String> values, final String defaultValue) {
    this(name, values.toArray(new String[] {}), defaultValue);
  }

  public String[] getValues() {
    return this.values;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getName() + "(" + this.getDefaultValue() + "):" + Arrays.toString(this.values));
    return sb.toString();
  }
}
