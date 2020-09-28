package ai.libs.jaicore.components.api;

public interface INumericParameterRefinementConfiguration {

	public boolean isInitRefinementOnLogScale();

	public double getFocusPoint();

	public double getLogBasis();

	public boolean isInitWithExtremalPoints();

	public int getRefinementsPerStep();

	public double getIntervalLength();
}
