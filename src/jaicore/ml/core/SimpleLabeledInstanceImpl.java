package jaicore.ml.core;

import jaicore.ml.interfaces.LabeledInstance;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class SimpleLabeledInstanceImpl<L> extends ArrayList<Double> implements LabeledInstance<L> {

  private L label;

  public SimpleLabeledInstanceImpl() {
    super();
  }

  public SimpleLabeledInstanceImpl(final L label) {
    super();
    this.label = label;
  }

  public SimpleLabeledInstanceImpl(final LabeledInstance<L> toCopy, final L label) {
    super();
    this.addAll(toCopy);
    this.label = label;
  }

  @Override
  public String toJson() {
    return null;
  }

  @Override
  public int getNumberOfColumns() {
    return this.size();
  }

  @Override
  public void setLabel(final L label) {
    this.label = label;
  }

  @Override
  public L getLabel() {
    return this.label;
  }

  @Override
  public String toString() {
    return "{data=" + super.toString() + ", label=" + this.label + "}";
  }
}
