package de.upb.crc901.mlplan.multilabel;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.multiclass.wekamlplan.IClassifierFactory;
import hasco.exceptions.ComponentInstantiationFailedException;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import jaicore.basic.sets.SetUtil;
import meka.classifiers.multilabel.MultiLabelClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.MultipleClassifiersCombiner;
import weka.classifiers.SingleClassifierEnhancer;
import weka.classifiers.functions.supportVector.Kernel;
import weka.core.OptionHandler;

/**
* A pipeline factory that converts a given ComponentInstance that consists of
* components that correspond to MEKA algorithms to a MultiLabelClassifier.
*
*/
public class MekaPipelineFactory implements IClassifierFactory {

	private static final String PARAMETER_NAME_WITH_DASH_WARNING = "Required interface of component {} has dash or underscore in interface id {}";

	/* loggin */
	private static final Logger logger = LoggerFactory.getLogger(MekaPipelineFactory.class);

	@Override
	public Classifier getComponentInstantiation(final ComponentInstance ci) throws ComponentInstantiationFailedException {
		MultiLabelClassifier instance = null;
		try {
			instance = (MultiLabelClassifier) this.getClassifier(ci);
			return instance;
		} catch (Exception e) {
			throw new ComponentInstantiationFailedException(e, "Could not instantiate " + ci.getComponent().getName());
		}
	}

	private Classifier getClassifier(final ComponentInstance ci) throws Exception {
		Classifier c = (Classifier) Class.forName(ci.getComponent().getName()).newInstance();
		List<String> optionsList = this.getOptionsForParameterValues(ci);

		for (Entry<String, ComponentInstance> reqI : ci.getSatisfactionOfRequiredInterfaces().entrySet()) {
			if (reqI.getKey().startsWith("-") || reqI.getKey().startsWith("_")) {
				logger.warn(PARAMETER_NAME_WITH_DASH_WARNING, ci.getComponent(), reqI.getKey());
			}
			if (!reqI.getKey().equals("B") && !(c instanceof SingleClassifierEnhancer) && !(reqI.getKey().equals("K") && ci.getComponent().getName().endsWith("SMO"))) {
				logger.warn("Classifier {} is not a single classifier enhancer and still has an unexpected required interface: {}. Try to set this configuration in the form of options.", ci.getComponent().getName(), reqI);
				optionsList.add("-" + reqI.getKey());
				optionsList.add(reqI.getValue().getComponent().getName());
				if (!reqI.getValue().getParameterValues().isEmpty() || !reqI.getValue().getSatisfactionOfRequiredInterfaces().isEmpty()) {
					optionsList.add("--");
					optionsList.addAll(this.getOptionsRecursively(reqI.getValue()));
				}
			}
		}
		if (c instanceof OptionHandler) {
			((OptionHandler) c).setOptions(optionsList.toArray(new String[0]));
		}
		for (Entry<String, ComponentInstance> reqI : ci.getSatisfactionOfRequiredInterfaces().entrySet()) {
			if (reqI.getKey().startsWith("-") || reqI.getKey().startsWith("_")) {
				logger.warn(PARAMETER_NAME_WITH_DASH_WARNING, ci.getComponent(), reqI.getKey());
			}
			if (reqI.getKey().equals("K") && ci.getComponent().getName().endsWith("SMO")) {
				ComponentInstance kernelCI = reqI.getValue();
				logger.debug("Set kernel for SMO to be {}", kernelCI.getComponent().getName());
				Kernel k = (Kernel) Class.forName(kernelCI.getComponent().getName()).newInstance();
				k.setOptions(this.getOptionsForParameterValues(kernelCI).toArray(new String[0]));
			} else if (reqI.getKey().equals("B") && (c instanceof MultipleClassifiersCombiner)) {
				Classifier[] classifiers = this.getListOfBaseLearners(reqI.getValue()).toArray(new Classifier[0]);
				((MultipleClassifiersCombiner) c).setClassifiers(classifiers);
			} else if (reqI.getKey().equals("W") && (c instanceof SingleClassifierEnhancer)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Set {} as a base classifier for {}", reqI.getValue().getComponent().getName(), ci.getComponent().getName());
				}
				((SingleClassifierEnhancer) c).setClassifier(this.getClassifier(reqI.getValue()));
			}
		}

		return c;
	}

	private List<Classifier> getListOfBaseLearners(final ComponentInstance ci) throws Exception {
		List<Classifier> baseLearnerList = new LinkedList<>();
		if (ci.getComponent().getName().equals("MultipleBaseLearnerListElement")) {
			baseLearnerList.add(this.getClassifier(ci.getSatisfactionOfRequiredInterfaces().get("classifier")));
		} else if (ci.getComponent().getName().equals("MultipleBaseLearnerListChain")) {
			baseLearnerList.add(this.getClassifier(ci.getSatisfactionOfRequiredInterfaces().get("classifier")));
			baseLearnerList.addAll(this.getListOfBaseLearners(ci.getSatisfactionOfRequiredInterfaces().get("chain")));
		}
		return baseLearnerList;
	}

	private List<String> getOptionsForParameterValues(final ComponentInstance ci) {
		List<String> optionsList = new LinkedList<>();
		for (Entry<String, String> parameterValue : ci.getParameterValues().entrySet()) {

			if (parameterValue.getKey().startsWith("-") || parameterValue.getKey().startsWith("_")) {
				logger.warn(PARAMETER_NAME_WITH_DASH_WARNING, ci.getComponent(), parameterValue);
			}

			if (parameterValue.getValue().equals("true")) {
				optionsList.add("-" + parameterValue.getKey());
			} else if (parameterValue.getKey().toLowerCase().contains("activator") || parameterValue.getValue().equals("false")) {
				// ignore this parameter
			} else {
				optionsList.add("-" + parameterValue.getKey());
				if (ci.getComponent().getParameterWithName(parameterValue.getKey()).isNumeric()) {
					NumericParameterDomain numDom = (NumericParameterDomain) ci.getComponent().getParameterWithName(parameterValue.getKey()).getDefaultDomain();
					if (numDom.isInteger()) {
						optionsList.add(((int) Double.parseDouble(parameterValue.getValue())) + "");
					} else {
						optionsList.add(parameterValue.getValue());
					}
				} else {
					optionsList.add(parameterValue.getValue());
				}

			}
		}
		return optionsList;
	}

	private List<String> getOptionsRecursively(final ComponentInstance ci) {
		List<String> optionsList = this.getOptionsForParameterValues(ci);

		for (Entry<String, ComponentInstance> reqI : ci.getSatisfactionOfRequiredInterfaces().entrySet()) {
			if (reqI.getKey().startsWith("-") || reqI.getKey().startsWith("_")) {
				logger.warn(PARAMETER_NAME_WITH_DASH_WARNING, ci.getComponent(), reqI.getKey());
			}

			optionsList.add("-" + reqI.getKey());
			if (reqI.getKey().equals("B") || reqI.getKey().equals("K")) {
				List<String> valueList = new LinkedList<>();
				valueList.add(reqI.getValue().getComponent().getName());
				valueList.addAll(this.getOptionsRecursively(reqI.getValue()));
				optionsList.add(SetUtil.implode(valueList, " "));
			} else {
				optionsList.add(reqI.getValue().getComponent().getName());
				if (!reqI.getValue().getParameterValues().isEmpty() || !reqI.getValue().getSatisfactionOfRequiredInterfaces().isEmpty()) {
					optionsList.add("--");
					optionsList.addAll(this.getOptionsRecursively(reqI.getValue()));
				}
			}
		}

		return optionsList;
	}
}