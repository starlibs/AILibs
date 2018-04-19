package hasco.model;

public class NumericParameter extends Parameter {
  private final boolean isInteger;
  private final double min, max;

  public NumericParameter(final String name, final boolean isInteger, final double defaultValue, final double min, final double max) {
    super(name, defaultValue);
    this.isInteger = isInteger;
    this.min = min;
    this.max = max;
  }

  public boolean isInteger() {
    return this.isInteger;
  }

  public double getMin() {
    return this.min;
  }

  public double getMax() {
    return this.max;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(super.getName());
    if (this.isInteger) {
      sb.append(":int");
    } else {
      sb.append(":double");
    }

    sb.append("[default:");
    sb.append(this.getDefaultValue());
    sb.append(",min:");
    sb.append(this.min);
    sb.append(",max:");
    sb.append(this.max);
    sb.append("]");

    return sb.toString();
  }
}
