package ai.libs.mlplan.sklearn.builder;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.algorithm.Timeout;

import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.AMonteCarloCrossValidationBasedEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ISupervisedLearnerEvaluatorFactory;
import ai.libs.jaicore.ml.scikitwrapper.AScikitLearnWrapper;
import ai.libs.jaicore.ml.scikitwrapper.IScikitLearnWrapper;
import ai.libs.mlplan.core.AMLPlanBuilder;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.sklearn.AScikitLearnLearnerFactory;
import ai.libs.mlplan.sklearn.EMLPlanScikitLearnProblemType;
import ai.libs.python.IPythonConfig;
import ai.libs.python.PythonRequirementDefinition;

public class MLPlanScikitLearnBuilder extends AMLPlanBuilder<IScikitLearnWrapper, MLPlanScikitLearnBuilder> {

	private static final String[] PYTHON_REQUIRED_MODULES = ArrayUtils.addAll(AScikitLearnWrapper.PYTHON_REQUIRED_MODULES, new String[] { "tpot", "xgboost" });

	private IPythonConfig pythonConfig;
	private String[] pythonAdditionalRequiredModules;
	private final boolean skipSetupCheck;

	public static MLPlanScikitLearnBuilder forClassification() throws IOException {
		return new MLPlanScikitLearnBuilder(EMLPlanScikitLearnProblemType.CLASSIFICATION_MULTICLASS);
	}

	public static MLPlanScikitLearnBuilder forClassificationWithUnlimitedLength() throws IOException {
		return new MLPlanScikitLearnBuilder(EMLPlanScikitLearnProblemType.CLASSIFICATION_MULTICLASS_UNLIMITED_LENGTH_PIPELINES);
	}

	public static MLPlanScikitLearnBuilder forRegression() throws IOException {
		return new MLPlanScikitLearnBuilder(EMLPlanScikitLearnProblemType.REGRESSION);
	}

	public static MLPlanScikitLearnBuilder forRUL() throws IOException {
		return new MLPlanScikitLearnBuilder(EMLPlanScikitLearnProblemType.RUL);
	}

	/**
	 * Creates a new ML-Plan Builder for scikit-learn.
	 *
	 * @throws IOException
	 *             Thrown if configuration files cannot be read.
	 */
	protected MLPlanScikitLearnBuilder(final EMLPlanScikitLearnProblemType problemType) throws IOException {
		this(problemType, false);
		this.pythonAdditionalRequiredModules = problemType.getSkLearnProblemType().getPythonRequiredModules();
	}

	/**
	 * Creates a new ML-Plan Builder for scikit-learn.
	 *
	 * @param skipSetupCheck
	 *            Flag whether to skip the system's setup check, which examines whether the operating system has python installed in the required version and all the required python modules are installed.
	 * @throws IOException
	 *             Thrown if configuration files cannot be read.
	 */
	public MLPlanScikitLearnBuilder(final EMLPlanScikitLearnProblemType problemType, final boolean skipSetupCheck) throws IOException {
		super(problemType);
		this.skipSetupCheck = skipSetupCheck;
	}

	@Override
	public MLPlanScikitLearnBuilder withProblemType(final IProblemType<IScikitLearnWrapper> problemType) throws IOException {
		super.withProblemType(problemType);
		this.pythonAdditionalRequiredModules = ((EMLPlanScikitLearnProblemType) problemType).getSkLearnProblemType().getPythonRequiredModules();
		return this.getSelf();
	}

	@Override
	public MLPlanScikitLearnBuilder withSeed(final long seed) {
		super.withSeed(seed);
		if (this.getLearnerFactory() != null) {
			this.getLearnerFactory().setSeed(seed);
		}
		return this.getSelf();
	}

	@Override
	public MLPlanScikitLearnBuilder withCandidateEvaluationTimeOut(final Timeout timeout) {
		super.withCandidateEvaluationTimeOut(timeout);
		if (this.getLearnerFactory() != null) {
			this.getLearnerFactory().setTimeout(timeout);
		}
		return this.getSelf();
	}

	@Override
	public AScikitLearnLearnerFactory getLearnerFactory() {
		return (AScikitLearnLearnerFactory) super.getLearnerFactory();
	}

	@Override
	public MLPlanScikitLearnBuilder getSelf() {
		return this;
	}

	private void setDeterministicDatasetSplitter(final ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> factory) {
		if (factory instanceof AMonteCarloCrossValidationBasedEvaluatorFactory<?>) {
			((AMonteCarloCrossValidationBasedEvaluatorFactory<?>) factory).withCacheSplitSets(true);
		}
	}

	@Override
	public MLPlan<IScikitLearnWrapper> build() {
		if (!this.skipSetupCheck) {
			new PythonRequirementDefinition(AScikitLearnWrapper.PYTHON_MINIMUM_REQUIRED_VERSION_REL, AScikitLearnWrapper.PYTHON_MINIMUM_REQUIRED_VERSION_MAJ,
					AScikitLearnWrapper.PYTHON_MINIMUM_REQUIRED_VERSION_MIN, ArrayUtils.addAll(PYTHON_REQUIRED_MODULES, this.pythonAdditionalRequiredModules)).check(this.pythonConfig);
		}
		this.setDeterministicDatasetSplitter(this.getLearnerEvaluationFactoryForSearchPhase());
		this.setDeterministicDatasetSplitter(this.getLearnerEvaluationFactoryForSelectionPhase());
		return super.build();
	}
}