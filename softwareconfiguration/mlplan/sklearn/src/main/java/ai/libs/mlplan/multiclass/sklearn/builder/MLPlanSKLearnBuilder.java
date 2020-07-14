package ai.libs.mlplan.multiclass.sklearn.builder;

import java.io.IOException;

import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.algorithm.Timeout;

import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.MLPlanBuilder;
import ai.libs.mlplan.multiclass.sklearn.ASKLearnClassifierFactory;
import ai.libs.mlplan.multiclass.sklearn.EMLPlanSkLearnProblemType;
import ai.libs.python.IPythonConfig;
import ai.libs.python.PythonRequirementDefinition;

public class MLPlanSKLearnBuilder extends MLPlanBuilder<ScikitLearnWrapper<IPrediction, IPredictionBatch>, MLPlanSKLearnBuilder> {

	private static final int PYTHON_MINIMUM_REQUIRED_VERSION_REL = 3;
	private static final int PYTHON_MINIMUM_REQUIRED_VERSION_MAJ = 5;
	private static final int PYTHON_MINIMUM_REQUIRED_VERSION_MIN = 0;
	private static final String[] PYTHON_REQUIRED_MODULES = { "arff", "numpy", "json", "pickle", "os", "sys", "warnings", "scipy", "sklearn", "tpot" };

	private IPythonConfig pythonConfig;
	private final boolean skipSetupCheck;

	public static MLPlanSKLearnBuilder forClassification() throws IOException {
		return new MLPlanSKLearnBuilder(EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS);
	}

	public static MLPlanSKLearnBuilder forClassificationWithUnlimitedLength() throws IOException {
		return new MLPlanSKLearnBuilder(EMLPlanSkLearnProblemType.CLASSIFICATION_MULTICLASS_UNLIMITED_LENGTH_PIPELINES);
	}

	public static MLPlanSKLearnBuilder forRUL() throws IOException {
		return new MLPlanSKLearnBuilder(EMLPlanSkLearnProblemType.RUL);
	}

	/**
	 * Creates a new ML-Plan Builder for scikit-learn.
	 *
	 * @throws IOException
	 *             Thrown if configuration files cannot be read.
	 */
	protected MLPlanSKLearnBuilder(final IProblemType<ScikitLearnWrapper<IPrediction, IPredictionBatch>> problemType) throws IOException {
		this(problemType, false);
	}

	/**
	 * Creates a new ML-Plan Builder for scikit-learn.
	 *
	 * @param skipSetupCheck
	 *            Flag whether to skip the system's setup check, which examines whether the operating system has python installed in the required version and all the required python modules are installed.
	 * @throws IOException
	 *             Thrown if configuration files cannot be read.
	 */
	public MLPlanSKLearnBuilder(final IProblemType<ScikitLearnWrapper<IPrediction, IPredictionBatch>> problemType, final boolean skipSetupCheck) throws IOException {
		super(problemType);
		this.skipSetupCheck = skipSetupCheck;
	}

	public MLPlanSKLearnBuilder withAnacondaEnvironment(final String env) {
		this.getLearnerFactory().setAnacondaEnvironment(env);
		return this.getSelf();
	}

	@Override
	public MLPlanSKLearnBuilder withSeed(final long seed) {
		super.withSeed(seed);
		if (this.getLearnerFactory() != null) {
			this.getLearnerFactory().setSeed(seed);
		}
		return this.getSelf();
	}

	@Override
	public MLPlanSKLearnBuilder withCandidateEvaluationTimeOut(final Timeout timeout) {
		super.withCandidateEvaluationTimeOut(timeout);
		if (this.getLearnerFactory() != null) {
			this.getLearnerFactory().setTimeout(timeout);
		}
		return this.getSelf();
	}

	@Override
	public ASKLearnClassifierFactory getLearnerFactory() {
		return (ASKLearnClassifierFactory) super.getLearnerFactory();
	}

	@Override
	public MLPlanSKLearnBuilder getSelf() {
		return this;
	}

	@Override
	public MLPlan<ScikitLearnWrapper<IPrediction, IPredictionBatch>> build() {
		if (!this.skipSetupCheck) {
			new PythonRequirementDefinition(PYTHON_MINIMUM_REQUIRED_VERSION_REL, PYTHON_MINIMUM_REQUIRED_VERSION_MAJ, PYTHON_MINIMUM_REQUIRED_VERSION_MIN, PYTHON_REQUIRED_MODULES).check(this.pythonConfig);
		}
		return super.build();
	}
}