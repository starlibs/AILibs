package ai.libs.mlplan.multiclass.sklearn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.algorithm.Timeout;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.ml.core.EScikitLearnProblemType;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.python.IPythonConfig;

/**
 * The SKLearnClassifierFactory takes a ground component instance and parses it into a <code>ScikitLearnWrapper</code> as defined in the project jaicore-ml. This factory may be used in the context of HASCO, especially for ML-Plan.
 *
 * @author wever
 */
public abstract class AScikitLearnLearnerFactory implements ILearnerFactory<ScikitLearnWrapper<IPrediction, IPredictionBatch>>, ILoggingCustomizable {

	public static final String N_PREPROCESSOR = "preprocessor";

	private static final CategoricalParameterDomain BOOL_DOMAIN = new CategoricalParameterDomain(Arrays.asList("True", "False"));
	private static final List<String> EXCEPTIONS = Arrays.asList("None", "np.inf", "f_regression");

	private final EScikitLearnProblemType sklearnProblemType;

	private Logger logger = LoggerFactory.getLogger(AScikitLearnLearnerFactory.class);
	private String loggerName;
	private IPythonConfig pythonConfig = ConfigFactory.create(IPythonConfig.class);
	private long seed;
	private Timeout timeout;

	public AScikitLearnLearnerFactory(final EScikitLearnProblemType sklearnProblemType) {
		super();
		this.sklearnProblemType = sklearnProblemType;
	}

	@Override
	public ScikitLearnWrapper<IPrediction, IPredictionBatch> getComponentInstantiation(final IComponentInstance groundComponent) throws ComponentInstantiationFailedException {
		this.logger.debug("Parse ground component instance {} to ScikitLearnWrapper object.", groundComponent);

		StringBuilder constructInstruction = new StringBuilder();
		Set<String> importSet = new HashSet<>();
		constructInstruction.append(this.extractSKLearnConstructInstruction(groundComponent, importSet));
		StringBuilder imports = new StringBuilder();
		importSet.forEach(imports::append);

		String constructionString = constructInstruction.toString();
		this.logger.info("Created construction string: {}", constructionString);

		try {
			ScikitLearnWrapper<IPrediction, IPredictionBatch> wrapper = new ScikitLearnWrapper<>(constructionString, imports.toString(), false, this.sklearnProblemType);
			if (this.pythonConfig != null) {
				wrapper.setPythonConfig(this.pythonConfig);
			}
			wrapper.setSeed(this.seed);
			wrapper.setTimeout(this.timeout);
			return wrapper;
		} catch (IOException e) {
			this.logger.error("Could not create sklearn wrapper for construction {} and imports {}.", constructInstruction, imports);
			return null;
		}
	}

	public abstract String getPipelineBuildString(final IComponentInstance groundComponent, final Set<String> importSet);

	public String extractSKLearnConstructInstruction(final IComponentInstance groundComponent, final Set<String> importSet) {
		StringBuilder sb = new StringBuilder();
		if (groundComponent.getComponent().getName().startsWith("mlplan.util.model.make_forward")) {
			sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface("source").iterator().next(), importSet));
			sb.append(",");
			sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface("base").iterator().next(), importSet));
			return sb.toString();
		}

		String[] packagePathSplit = groundComponent.getComponent().getName().split("\\.");
		StringBuilder fromSB = new StringBuilder();
		fromSB.append(packagePathSplit[0]);
		for (int i = 1; i < packagePathSplit.length - 1; i++) {
			fromSB.append("." + packagePathSplit[i]);
		}
		String className = packagePathSplit[packagePathSplit.length - 1];

		if (packagePathSplit.length > 1) {
			importSet.add("from " + fromSB.toString() + " import " + className + "\n");
		}
		if (groundComponent.getComponent().getName().startsWith("sklearn.feature_selection.f_classif")) {
			sb.append("f_classif(features, targets)");
			return sb.toString();
		}

		sb.append(className);
		sb.append("(");
		if (groundComponent.getComponent().getName().contains("make_pipeline")) {
			sb.append(this.getPipelineBuildString(groundComponent, importSet));
		} else if (groundComponent.getComponent().getName().contains("make_union"))

		{
			sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface("p1").iterator().next(), importSet));
			sb.append(",");
			sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface("p2").iterator().next(), importSet));
		} else {
			boolean first = true;
			for (Entry<String, String> parameterValue : groundComponent.getParameterValues().entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}

				IParameter param = groundComponent.getComponent().getParameter(parameterValue.getKey());

				sb.append(parameterValue.getKey() + "=");
				if (param.isNumeric()) {
					if (((NumericParameterDomain) param.getDefaultDomain()).isInteger()) {
						sb.append((int) Double.parseDouble(parameterValue.getValue()));
					} else {
						sb.append(Double.parseDouble(parameterValue.getValue()));
					}
				} else if (param.isCategorical()) {
					if (BOOL_DOMAIN.subsumes(param.getDefaultDomain()) || EXCEPTIONS.contains(parameterValue.getValue())) {
						sb.append(parameterValue.getValue());
					} else { // if the categorical parameter contains numeric values, try to parse it as int or as double, and use the value itself if neither works
						try {
							sb.append(Integer.parseInt(parameterValue.getValue()));
						} catch (NumberFormatException e) {
							try {
								sb.append(Double.parseDouble(parameterValue.getValue()));
							} catch (NumberFormatException e1) {
								sb.append("\"" + parameterValue.getValue() + "\"");
							}
						}
					}
				} else {
					throw new UnsupportedOperationException("The given parameter type is unknown for parameter " + param);
				}
			}

			for (Entry<String, List<IComponentInstance>> satReqI : groundComponent.getSatisfactionOfRequiredInterfaces().entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}

				sb.append(satReqI.getKey() + "=");
				sb.append(this.extractSKLearnConstructInstruction(satReqI.getValue().iterator().next(), importSet));
			}
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		this.logger.debug("Switching logger name to {}", name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.debug("Switched SKLearnClassifierFactory logger to {}", name);
	}

	public void setPythonConfig(final IPythonConfig pythonConfig) {
		this.pythonConfig = pythonConfig;
	}

	public void setSeed(final long seed) {
		this.seed = seed;
	}

	public void setTimeout(final Timeout timeout) {
		this.timeout = timeout;
	}

}
