package hasco.model;

public class ParameterRefinementConfiguration {
  private final boolean initRefinementOnLogScale = true;
  private final int refinementsPerStep;
  private final double intervalLength;

  public ParameterRefinementConfiguration(final int refinementsPerStep, final double intervalLength) {
    super();
    this.refinementsPerStep = refinementsPerStep;
    this.intervalLength = intervalLength;
  }

  public boolean isInitRefinementOnLogScale() {
    return this.initRefinementOnLogScale;
  }

  public int getRefinementsPerStep() {
    return this.refinementsPerStep;
  }

  public double getIntervalLength() {
    return this.intervalLength;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("[InitiallyLogScale:");
    sb.append(this.initRefinementOnLogScale);
    sb.append(",RefinementsPerStep:");
    sb.append(this.refinementsPerStep);
    sb.append(",intervalLength:");
    sb.append(this.intervalLength);
    sb.append("]");

    return sb.toString();
  }
}
