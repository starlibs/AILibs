package ai.libs.mlplan.multiclass.sklearn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.algorithm.Timeout;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.exceptions.ComponentInstantiationFailedException;
import ai.libs.hasco.model.CategoricalParameterDomain;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.NumericParameterDomain;
import ai.libs.hasco.model.Parameter;
import ai.libs.jaicore.ml.scikitwrapper.EBasicProblemType;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.ILearnerFactory;

/**
 * The SKLearnClassifierFactory takes a ground component instance and parses it into a <code>ScikitLearnWrapper</code> as defined in the project jaicore-ml.
 * This factory may be used in the context of HASCO, especially for ML-Plan.
 *
 * @author wever
 */
public class SKLearnClassifierFactory<P extends IPrediction, B extends IPredictionBatch> implements ILearnerFactory<ScikitLearnWrapper<P, B>>, ILoggingCustomizable {

	private static final String N_PREPROCESSOR = "preprocessor";

	private static final CategoricalParameterDomain BOOL_DOMAIN = new CategoricalParameterDomain(Arrays.asList("True", "False"));
	private static final List<String> EXCEPTIONS = Arrays.asList("None", "np.inf", "f_regression");

	private Logger logger = LoggerFactory.getLogger(SKLearnClassifierFactory.class);
	private String loggerName;

	private EBasicProblemType problemType;
	private String pathVariable;
	private String anacondaEnvironment;
	private long seed;
	private Timeout timeout;

	@Override
	public ScikitLearnWrapper<P, B> getComponentInstantiation(final ComponentInstance groundComponent) throws ComponentInstantiationFailedException {
		this.logger.debug("Parse ground component instance {} to ScikitLearnWrapper object.", groundComponent);

		StringBuilder constructInstruction = new StringBuilder();
		Set<String> importSet = new HashSet<>();
		constructInstruction.append(this.extractSKLearnConstructInstruction(groundComponent, importSet));
		StringBuilder imports = new StringBuilder();
		importSet.forEach(imports::append);

		String constructionString = constructInstruction.toString();
		this.logger.info("Created construction string: {}", constructionString);

		try {
			ScikitLearnWrapper<P, B> wrapper = new ScikitLearnWrapper<>(constructionString, imports.toString(), true, this.problemType);
			wrapper.setPathVariable(this.pathVariable);
			wrapper.setAnacondaEnvironment(this.anacondaEnvironment);
			wrapper.setSeed(this.seed);
			wrapper.setTimeout(this.timeout);
			return wrapper;
		} catch (IOException e) {
			this.logger.error("Could not create sklearn wrapper for construction {} and imports {}.", constructInstruction, imports);
			return null;
		}
	}

	public String extractSKLearnConstructInstruction(final ComponentInstance groundComponent, final Set<String> importSet) {
		StringBuilder sb = new StringBuilder();
		if (groundComponent.getComponent().getName().startsWith("mlplan.util.model.make_forward")) {
			sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("source"), importSet));
			sb.append(",");
			sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("base"), importSet));
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
			if (this.problemType == EBasicProblemType.CLASSIFICATION) {
				sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR), importSet));
				sb.append(",");
				sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier"), importSet));
			} else if (this.problemType == EBasicProblemType.RUL) {
				sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("timeseries_transformer"), importSet));
				sb.append(",");
				sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("data_cleaner"), importSet));
				sb.append(",");
				int i = 0;
				while (groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR + i) != null) {
					if (!groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR + i).getComponent().getName().equals("NoPreprocessor")) {
						sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get(N_PREPROCESSOR + i), importSet));
						sb.append(",");
					}
					i++;
				}
				sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("regressor"), importSet));
			}
		} else if (groundComponent.getComponent().getName().contains("make_union"))

		{
			sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("p1"), importSet));
			sb.append(",");
			sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("p2"), importSet));
		} else {
			boolean first = true;
			for (Entry<String, String> parameterValue : groundComponent.getParameterValues().entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}

				Parameter param = groundComponent.getComponent().getParameterWithName(parameterValue.getKey());

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

			for (Entry<String, ComponentInstance> satReqI : groundComponent.getSatisfactionOfRequiredInterfaces().entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}

				sb.append(satReqI.getKey() + "=");
				sb.append(this.extractSKLearnConstructInstruction(satReqI.getValue(), importSet));
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

	public void setProblemType(final EBasicProblemType problemType) {
		this.problemType = problemType;
	}

	public void setPathVariable(final String path) {
		this.pathVariable = path;
	}

	public void setAnacondaEnvironment(final String env) {
		this.anacondaEnvironment = env;
	}

	public void setSeed(final long seed) {
		this.seed = seed;
	}

	public void setTimeout(final Timeout timeout) {
		this.timeout = timeout;
	}

}
