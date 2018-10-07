package hasco.core;

import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import hasco.knowledgebase.IParameterImportanceEstimator;
import hasco.knowledgebase.PerformanceKnowledgeBase;
import hasco.model.BooleanParameterDomain;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

public class isRefinementCompletedPredicateWithImportanceCheck implements EvaluablePredicate {

	private final Collection<Component> components;
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration;
	private final PerformanceKnowledgeBase performanceKB;
	private final IParameterImportanceEstimator importanceEstimator;
	private final double importanceThreshold;
	private final int minNumSamplesForImportanceEstimation;
	private final boolean useImportanceEstimation;
	// private final Map<ComponentInstance,Double> knownCompositionsAndTheirScore =
	// new HashMap<>();

	public isRefinementCompletedPredicateWithImportanceCheck(Collection<Component> components,
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration,
			PerformanceKnowledgeBase performanceKB, IParameterImportanceEstimator importanceEstimator,
			double importanceThreshold, int minNumSamples, boolean useImportanceEstimation) {
		super();
		this.components = components;
		this.refinementConfiguration = refinementConfiguration;
		this.importanceEstimator = importanceEstimator;
		this.importanceThreshold = importanceThreshold;
		this.minNumSamplesForImportanceEstimation = minNumSamples;
		this.performanceKB = performanceKB;
		this.useImportanceEstimation = useImportanceEstimation;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state,
			ConstantParam... partialGrounding) {
		throw new NotImplementedException("This is not an oracable predicate!");
	}

	@Override
	public boolean isOracable() {
		return false;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state,
			ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		// if(true)
		// return true;
		/* initialize routine */
		if (params.length != 2) {
			throw new IllegalArgumentException("There should be exactly two parameters additional to the state but "
					+ params.length + " were provided: " + Arrays.toString(params)
					+ ". This parameters refer to the component name that is being configured and the object itself.");
		}
		if (params[0] == null)
			throw new IllegalArgumentException("The component name must not be null.");
		if (params[1] == null)
			throw new IllegalArgumentException("The component instance reference must not be null.");
		// final String componentName = params[0].getName();
		final String objectContainer = params[1].getName();

		/* determine current values for the params */
		ComponentInstance groundComponent = Util.getGroundComponentsFromState(state, components, false)
				.get(objectContainer);
		ComponentInstance ci = Util.getSolutionCompositionFromState(components, state);
		Component component = groundComponent.getComponent();
		Map<String, String> componentParamContainers = Util.getParameterContainerMap(state, objectContainer);
		Map<String, String> componentParams = groundComponent.getParameterValues();

//		Set<String> importantParams = new HashSet<String>();
//		for (Parameter param : component.getParameters()) {
//			String parameterIdentifier = ci.getComponent().getName() + "::" + param.getName();
//			importantParams.add(parameterIdentifier);
//		}
//
//		String compositionIdentifier = Util.getComponentNamesOfComposition(ci);
//		// System.out.println("Composition Identifier in completedpred: " +
//		// compositionIdentifier);
////		if (performanceKB.getNumSamples("test", compositionIdentifier) > this.minNumSamplesForImportanceEstimation) {
//		if (performanceKB.kDistinctAttributeValuesAvailable("test", ci, minNumSamplesForImportanceEstimation)) {
//			System.out.println(minNumSamplesForImportanceEstimation + " samples are available");
//			try {
////				System.out.println("Querying fANOVA with " + performanceKB.getNumSamples("test", compositionIdentifier)
////						+ " samples!");
//				// System.out.println("Querying fANOVA with " +
//				// performanceKB.getNumSamples("test", compositionIdentifier)
//				// + " samples!");
//				System.out.println("extract important parameters for pipline " + Util.getComponentNamesOfComposition(ci));
//				importantParams = importanceEstimator.extractImportantParameters(ci, this.importanceThreshold, 2,
//						false);
//				// If there are no parameters left that are estimated to be important, return
//				// true
//				if (importantParams.isEmpty())
//					return true;
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}

		// System.out.println("Important Parameters: " + importantParams.toString());
		for (Parameter param : component.getParameters()) {
			// String paramName = ci.getComponent().getName() + "::" + param.getName();
			String paramName = component.getName() + "::" + param.getName();

			// System.out.println("Checking whether parameter " + param.getName() + " for
			// component " + component.getName()
			// + " has completed its refinement");
//			if (!importantParams.contains(paramName)) {
////				System.out.println("Skip parameter " + paramName);
//				continue;
//			}
//			System.out.println("Not skipping parameter " + paramName);
			String containerOfParam = componentParamContainers.get(param.getName());
			String currentValueOfParam = componentParams.get(param.getName());
			if (param.isNumeric()) {
				ParameterRefinementConfiguration refinementConfig = refinementConfiguration.get(component).get(param);
				List<String> interval = SetUtil.unserializeList(currentValueOfParam);
				double min = Double.parseDouble(interval.get(0));
				double max = Double.parseDouble(interval.get(1));
				double length = max - min;
				if (length > refinementConfig.getIntervalLength()) {
					return false;
				}
			} else if (param.getDefaultDomain() instanceof CategoricalParameterDomain) { // categorical params can be
																							// refined iff their current
																							// value is not the default
																							// value
				assert currentValueOfParam != null : "Param " + param.getName() + " has currently no value!";
				assert param.getDefaultValue() != null : "Param " + param.getName() + " has no default value!";
				boolean variableHasBeenSet = state.contains(new Literal("overwritten('" + containerOfParam + "')"));
				boolean variableHasBeenClosed = state.contains(new Literal("closed('" + containerOfParam + "')"));
				assert !variableHasBeenClosed || variableHasBeenSet : "Parameter " + param.getName() + " of component "
						+ component.getName() + " with default domain " + param.getDefaultDomain()
						+ " has been closed but no value has been set.";
				if (!variableHasBeenSet && !variableHasBeenClosed) {
					return false;
				}
			} else
				throw new UnsupportedOperationException(
						"Currently no support for testing parameters of type " + param.getClass().getName());
//			System.out.println(
//					"\t" + param.getName() + " (" + componentParams.get(param.getName()) + ") is still refinable.");
		}
//		System.out.println("Refinement of component " + component.getName() + " is completed." );
		return true;
	}
}
