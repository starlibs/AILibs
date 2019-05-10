package de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.multiclass.wekamlplan.IClassifierFactory;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.CategoricalParameterDomain;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import jaicore.basic.ILoggingCustomizable;
import jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import weka.classifiers.Classifier;

/**
 * The SKLearnClassifierFactory takes a ground component instance and parses it into a <code>ScikitLearnWrapper</code> as defined in the project jaicore-ml.
 * This factory may be used in the context of HASCO, especially for ML-Plan.
 *
 * @author wever
 */
public class SKLearnClassifierFactory implements IClassifierFactory, ILoggingCustomizable {

	private static final CategoricalParameterDomain BOOL_DOMAIN = new CategoricalParameterDomain(Arrays.asList("True", "False"));

	private Logger logger = LoggerFactory.getLogger(SKLearnClassifierFactory.class);
	private String loggerName;

	@Override
	public Classifier getComponentInstantiation(final ComponentInstance groundComponent) throws ComponentInstantiationFailedException {
		this.logger.info("Parse ground component instance {} to ScikitLearnWrapper object.", groundComponent);

		StringBuilder constructInstruction = new StringBuilder();
		Set<String> importSet = new HashSet<>();
		constructInstruction.append(this.extractSKLearnConstructInstruction(groundComponent, importSet));
		StringBuilder imports = new StringBuilder();
		importSet.forEach(imports::append);

		try {
			return new ScikitLearnWrapper(constructInstruction.toString(), imports.toString(), true);
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

		importSet.add("from " + fromSB.toString() + " import " + className + "\n");

		if (groundComponent.getComponent().getName().startsWith("sklearn.feature_selection.f_classif")) {
			sb.append("sklearn.feature_selection.f_classif(features, targets)");
			return sb.toString();
		}

		sb.append(className);
		sb.append("(");
		if (groundComponent.getComponent().getName().contains("make_pipeline")) {
			sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("preprocessor"), importSet));
			sb.append(",");
			sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterfaces().get("classifier"), importSet));
		} else if (groundComponent.getComponent().getName().contains("make_union")) {
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
					sb.append(parameterValue.getValue());
				} else if (param.isCategorical() && BOOL_DOMAIN.subsumes(param.getDefaultDomain())) {
					sb.append(parameterValue.getValue());
				} else {
					try {
						sb.append(Integer.parseInt(parameterValue.getValue()));
					} catch (Exception e) {
						try {
							sb.append(Double.parseDouble(parameterValue.getValue()));
						} catch (Exception e1) {
							sb.append("\"" + parameterValue.getValue() + "\"");
						}
					}
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

}
