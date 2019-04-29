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
import weka.core.OptionHandler;

/**
* A pipeline factory that converts a given ComponentInstance that consists of
* components that correspond to MEKA algorithms to a MultiLabelClassifier.
*
*/
public class MekaPipelineFactory implements IClassifierFactory {

	/* loggin */
	private static final Logger logger = LoggerFactory.getLogger(MekaPipelineFactory.class);

	@Override
	public Classifier getComponentInstantiation(final ComponentInstance ci) throws ComponentInstantiationFailedException {
		MultiLabelClassifier instance = null;
		List<String> optionsList = null;
		try {
			instance = (MultiLabelClassifier) Class.forName(ci.getComponent().getName()).newInstance();
			if (instance instanceof OptionHandler) {
				optionsList = this.getOptionsRecursively(ci);
				instance.setOptions(optionsList.toArray(new String[0]));
			}
		} catch (Exception e) {
			throw new ComponentInstantiationFailedException(e, "Could not instantiate " + ci.getComponent().getName() + " with options " + optionsList);
		}
		return instance;
	}

	private List<String> getOptionsRecursively(final ComponentInstance ci) {
		List<String> optionsList = new LinkedList<>();

		for (Entry<String, String> parameterValue : ci.getParameterValues().entrySet()) {
			if (parameterValue.getKey().startsWith("-") || parameterValue.getKey().startsWith("_")) {
				System.out.println("Parameter of component " + ci.getComponent() + " has dash or underscore in parameter name " + parameterValue.getKey());
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

		for (Entry<String, ComponentInstance> reqI : ci.getSatisfactionOfRequiredInterfaces().entrySet()) {
			if (reqI.getKey().startsWith("-") || reqI.getKey().startsWith("_")) {
				System.out.println("Parameter of component " + ci.getComponent() + " has dash or underscore in parameter name " + reqI.getKey());
			}

			optionsList.add("-" + reqI.getKey());
			if (reqI.getKey().equals("B")) {
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