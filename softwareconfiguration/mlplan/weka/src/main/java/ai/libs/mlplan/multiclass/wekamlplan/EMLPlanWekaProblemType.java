package ai.libs.mlplan.multiclass.wekamlplan;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.InstanceWiseF1;
import ai.libs.mlplan.core.IProblemType;

public enum EMLPlanWekaProblemType implements IProblemType {

	CLASSIFICATION_MULTICLASS("automl/searchmodels/weka/weka-all-autoweka.json", "conf/mlplan-weka.json", "mlplan/weka-preferenceList.txt", "conf/mlpan-weka-preferenceList.txt", "AbstractClassifier", "BasicClassifier",
			EClassificationPerformanceMeasure.ERRORRATE, EClassificationPerformanceMeasure.ERRORRATE, 0.0), //
	CLASSIFICATION_MULTICLASS_TINY("automl/searchmodels/weka/tinytest.json", EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFromFileSystem(),
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPreferredComponentListFromResource(), EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPreferredComponentListFromFileSystem(),
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getRequestedInterface(), EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPreferredBasicProblemComponentName(),
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPerformanceMetricForSearchPhase(), EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPerformanceMetricForSelectionPhase(),
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPortionOfDataReservedForSelectionPhase()), //
	CLASSIFICATION_MULTILABEL("automl/searchmodels/meka/mlplan-meka.json", "conf/searchmodels/mlplan-meka.json", "mlplan/meka-preferenceList.txt", "conf/mlpan-meka-preferenceList.txt", "MLClassifier",
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPreferredBasicProblemComponentName(), new InstanceWiseF1(), new InstanceWiseF1(), EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getPortionOfDataReservedForSelectionPhase());
	;

	private final String searchSpaceConfigFileFromResource;
	private final String systemSearchSpaceConfigFromFileSystem;

	private final String preferedComponentsListFromResource;
	private final String preferedComponentsListFromFileSystem;

	private final String requestedHascoInterface;
	private final String requestedBasicProblemInterface;
	//	private final String requestedBaseLearnerInterface;

	private final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSearchPhase;
	private final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSelectionPhase;
	private final double portionOfDataReservedForSelectionPhase;

	private EMLPlanWekaProblemType(final String searchSpaceConfigFileFromResource, final String systemSearchSpaceConfigFromFileSystem, final String preferedComponentsListFromResource, final String preferedComponentsListFromFileSystem,
			final String requestedHascoInterface, final String requestedBasicProblemInterface, final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSearchPhase,
			final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMetricForSelectionPhase, final double portionOfDataReservedForSelectionPhase) {

		this.searchSpaceConfigFileFromResource = searchSpaceConfigFileFromResource;
		this.systemSearchSpaceConfigFromFileSystem = systemSearchSpaceConfigFromFileSystem;

		this.preferedComponentsListFromResource = preferedComponentsListFromResource;
		this.preferedComponentsListFromFileSystem = preferedComponentsListFromFileSystem;

		this.requestedHascoInterface = requestedHascoInterface;
		this.requestedBasicProblemInterface = requestedBasicProblemInterface;
		//		this.requestedBaseLearnerInterface = requestedBaseLearnerInterface;

		this.performanceMetricForSearchPhase = performanceMetricForSearchPhase;
		this.performanceMetricForSelectionPhase = performanceMetricForSelectionPhase;
		this.portionOfDataReservedForSelectionPhase = portionOfDataReservedForSelectionPhase;
	}

	@Override
	public String getSearchSpaceConfigFileFromResource() {
		return this.searchSpaceConfigFileFromResource;
	}

	@Override
	public String getSearchSpaceConfigFromFileSystem() {
		return this.systemSearchSpaceConfigFromFileSystem;
	}

	@Override
	public String getPreferredComponentListFromResource() {
		return this.preferedComponentsListFromResource;
	}

	@Override
	public String getPreferredComponentListFromFileSystem() {
		return this.preferedComponentsListFromFileSystem;
	}

	@Override
	public String getRequestedInterface() {
		return this.requestedHascoInterface;
	}

	@Override
	public String getPreferredComponentName() {
		return this.getPreferredComponentName(this.requestedHascoInterface);
	}

	@Override
	public String getPreferredBasicProblemComponentName() {
		return this.getPreferredComponentName(this.requestedBasicProblemInterface);
	}

	private String getPreferredComponentName(final String requestedInterface) {
		return "resolve" + requestedInterface + "With";
	}

	//	@Override
	//	public String getRequestedBaseLearnerInterface() {
	//		return this.requestedBaseLearnerInterface;
	//	}

	@Override
	public IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMetricForSearchPhase() {
		return this.performanceMetricForSearchPhase;
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure<?, ?> getPerformanceMetricForSelectionPhase() {
		return this.performanceMetricForSelectionPhase;
	}

	@Override
	public double getPortionOfDataReservedForSelectionPhase() {
		return this.portionOfDataReservedForSelectionPhase;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName() + "." + this.toString();
	}

}
