package ai.libs.mlplan.meka;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.ml.classification.multilabel.learner.MekaClassifier;
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
public class MekaPipelineFactory implements IMekaPipelineFactory {

	private static final String PARAMETER_NAME_WITH_DASH_WARNING = "Required interface of component {} has dash or underscore in interface id {}";

	/* loggin */
	private static final Logger logger = LoggerFactory.getLogger(MekaPipelineFactory.class);

	@Override
	public IMekaClassifier getComponentInstantiation(final IComponentInstance ci) throws ComponentInstantiationFailedException {
		MultiLabelClassifier instance = null;
		try {
			instance = (MultiLabelClassifier) this.getClassifier(ci);
			return new MekaClassifier(instance);
		} catch (Exception e) {
			throw new ComponentInstantiationFailedException(e, "Could not instantiate " + ci.getComponent().getName());
		}
	}

	private Classifier getClassifier(final IComponentInstance ci) throws Exception {
		Classifier c = (Classifier) Class.forName(ci.getComponent().getName()).newInstance();
		List<String> optionsList = getOptionsForParameterValues(ci);

		for (Entry<String, List<IComponentInstance>> reqI : ci.getSatisfactionOfRequiredInterfaces().entrySet()) {
			if (reqI.getKey().startsWith("-") || reqI.getKey().startsWith("_")) {
				logger.warn(PARAMETER_NAME_WITH_DASH_WARNING, ci.getComponent(), reqI.getKey());
			}

			IComponentInstance subCI = reqI.getValue().iterator().next();
			if (!reqI.getKey().equals("B") && !(c instanceof SingleClassifierEnhancer) && !(reqI.getKey().equals("K") && ci.getComponent().getName().endsWith("SMO"))) {
				logger.warn("Classifier {} is not a single classifier enhancer and still has an unexpected required interface: {}. Try to set this configuration in the form of options.", ci.getComponent().getName(), reqI);
				optionsList.add("-" + reqI.getKey());
				optionsList.add(subCI.getComponent().getName());
				if (!subCI.getParameterValues().isEmpty() || !subCI.getSatisfactionOfRequiredInterfaces().isEmpty()) {
					optionsList.add("--");
					optionsList.addAll(this.getOptionsRecursively(subCI));
				}
			}
		}
		if (c instanceof OptionHandler) {
			((OptionHandler) c).setOptions(optionsList.toArray(new String[0]));
		}
		for (Entry<String, List<IComponentInstance>> reqI : ci.getSatisfactionOfRequiredInterfaces().entrySet()) {
			if (reqI.getKey().startsWith("-") || reqI.getKey().startsWith("_")) {
				logger.warn(PARAMETER_NAME_WITH_DASH_WARNING, ci.getComponent(), reqI.getKey());
			}
			IComponentInstance subCI = reqI.getValue().iterator().next();
			if (reqI.getKey().equals("K") && ci.getComponent().getName().endsWith("SMO")) {
				logger.debug("Set kernel for SMO to be {}", subCI.getComponent().getName());
				Kernel k = (Kernel) Class.forName(subCI.getComponent().getName()).newInstance();
				k.setOptions(getOptionsForParameterValues(subCI).toArray(new String[0]));
			} else if (reqI.getKey().equals("B") && (c instanceof MultipleClassifiersCombiner)) {
				Classifier[] classifiers = this.getListOfBaseLearners(subCI).toArray(new Classifier[0]);
				((MultipleClassifiersCombiner) c).setClassifiers(classifiers);
			} else if (reqI.getKey().equals("W") && (c instanceof SingleClassifierEnhancer)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Set {} as a base classifier for {}", subCI.getComponent().getName(), ci.getComponent().getName());
				}
				((SingleClassifierEnhancer) c).setClassifier(this.getClassifier(subCI));
			}
		}

		return c;
	}

	private List<Classifier> getListOfBaseLearners(final IComponentInstance ci) throws Exception {
		List<Classifier> baseLearnerList = new LinkedList<>();
		if (ci.getComponent().getName().equals("MultipleBaseLearnerListElement")) {
			baseLearnerList.add(this.getClassifier(ci.getSatisfactionOfRequiredInterface("classifier").iterator().next()));
		} else if (ci.getComponent().getName().equals("MultipleBaseLearnerListChain")) {
			baseLearnerList.add(this.getClassifier(ci.getSatisfactionOfRequiredInterface("classifier").iterator().next()));
			baseLearnerList.addAll(this.getListOfBaseLearners(ci.getSatisfactionOfRequiredInterface("chain").iterator().next()));
		}
		return baseLearnerList;
	}

	public static List<String> getOptionsForParameterValues(final IComponentInstance ci) {
		List<String> optionsList = new LinkedList<>();
		for (Entry<String, String> parameterValue : ci.getParameterValues().entrySet()) {
			IParameter param = ci.getComponent().getParameter(parameterValue.getKey());
			if (param.isDefaultValue(parameterValue.getValue()) || parameterValue.getKey().toLowerCase().contains("activator") || parameterValue.getValue().equals("false")) {
				continue;
			}

			if (parameterValue.getValue().equals("true")) {
				optionsList.add("-" + parameterValue.getKey());
			} else {
				optionsList.add("-" + parameterValue.getKey());
				if (ci.getComponent().getParameter(parameterValue.getKey()).isNumeric()) {
					NumericParameterDomain numDom = (NumericParameterDomain) ci.getComponent().getParameter(parameterValue.getKey()).getDefaultDomain();
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

	private List<String> getOptionsRecursively(final IComponentInstance ci) {
		List<String> optionsList = getOptionsForParameterValues(ci);

		for (Entry<String, List<IComponentInstance>> reqI : ci.getSatisfactionOfRequiredInterfaces().entrySet()) {
			if (reqI.getKey().startsWith("-") || reqI.getKey().startsWith("_")) {
				logger.warn(PARAMETER_NAME_WITH_DASH_WARNING, ci.getComponent(), reqI.getKey());
			}

			optionsList.add("-" + reqI.getKey());
			IComponentInstance subComponentInstance = reqI.getValue().iterator().next();
			if (reqI.getKey().equals("B") || reqI.getKey().equals("K")) {
				List<String> valueList = new LinkedList<>();
				valueList.add(subComponentInstance.getComponent().getName());
				valueList.addAll(this.getOptionsRecursively(subComponentInstance));
				optionsList.add(SetUtil.implode(valueList, " "));
			} else {
				optionsList.add(subComponentInstance.getComponent().getName());
				if (!subComponentInstance.getParameterValues().isEmpty() || !subComponentInstance.getSatisfactionOfRequiredInterfaces().isEmpty()) {
					optionsList.add("--");
					optionsList.addAll(this.getOptionsRecursively(subComponentInstance));
				}
			}
		}

		return optionsList;
	}
}