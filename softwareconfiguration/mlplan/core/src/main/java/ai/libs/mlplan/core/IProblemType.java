package ai.libs.mlplan.core;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

public interface IProblemType {

	public String getName();

	public String getSearchSpaceConfigFileFromResource();

	public String getSearchSpaceConfigFromFileSystem();

	public String getRequestedInterface();

	public String getPreferredComponentName();

	public String getPreferredComponentListFromResource();

	public String getPreferredComponentListFromFileSystem();

	public String getPreferredBasicProblemComponentName();

	public IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMetricForSearchPhase();

	public IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMetricForSelectionPhase();

	public double getPortionOfDataReservedForSelectionPhase();

}
