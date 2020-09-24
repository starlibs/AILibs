package ai.libs.hasco.core.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfiguration;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.theories.EvaluablePredicate;

public class IsRefinementCompletedPredicate implements EvaluablePredicate {

	private final Logger logger = LoggerFactory.getLogger(IsRefinementCompletedPredicate.class);
	private final Collection<IComponent> components;
	private final INumericParameterRefinementConfigurationMap refinementConfiguration;

	public IsRefinementCompletedPredicate(final Collection<? extends IComponent> components, final INumericParameterRefinementConfigurationMap refinementConfiguration) {
		super();
		this.components = new ArrayList<>(components);
		this.refinementConfiguration = refinementConfiguration;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(final Monom state, final ConstantParam... partialGrounding) {
		throw new NotImplementedException("This is not an oracable predicate!");
	}

	@Override
	public boolean isOracable() {
		return false;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(final Monom state, final ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean test(final Monom state, final ConstantParam... params) {
		/* initialize routine */
		if (params.length != 2) {
			throw new IllegalArgumentException("There should be exactly two parameters additional to the state but " + params.length + " were provided: " + Arrays.toString(params)
			+ ". This parameters refer to the component name that is being configured and the object itself.");
		}
		if (params[0] == null) {
			throw new IllegalArgumentException("The component name must not be null.");
		}
		if (params[1] == null) {
			throw new IllegalArgumentException("The component instance reference must not be null.");
		}
		final String objectContainer = params[1].getName();

		/* determine current values for the params */
		ComponentInstance groundComponent = HASCOUtil.getGroundComponentsFromState(state, this.components, false).get(objectContainer);
		IComponent component = groundComponent.getComponent();
		Map<String, String> componentParamContainers = HASCOUtil.getParameterContainerMap(state, objectContainer);
		for (IParameter param : component.getParameters()) {
			String containerOfParam = componentParamContainers.get(param.getName());
			String currentValueOfParam = groundComponent.getParameterValue(param);
			boolean variableHasBeenSet = state.contains(new Literal("overwritten('" + containerOfParam + "')"));
			boolean variableHasBeenClosed = state.contains(new Literal("closed('" + containerOfParam + "')"));

			assert variableHasBeenSet == groundComponent.getParametersThatHaveBeenSetExplicitly().contains(param);
			assert !variableHasBeenClosed || variableHasBeenSet : "Parameter " + param.getName() + " of component " + component.getName() + " with default domain " + param.getDefaultDomain() + " has been closed but no value has been set.";

			INumericParameterRefinementConfiguration refinementConfig = this.refinementConfiguration.getRefinement(component, param);

			if (param.isNumeric()) {
				double min = 0;
				double max = 0;
				if (currentValueOfParam != null) {
					List<String> interval = SetUtil.unserializeList(currentValueOfParam);
					min = Double.parseDouble(interval.get(0));
					max = Double.parseDouble(interval.get(1));
				} else {
					min = ((NumericParameterDomain) param.getDefaultDomain()).getMin();
					max = ((NumericParameterDomain) param.getDefaultDomain()).getMax();
				}
				double lengthStopCriterion = refinementConfig.getIntervalLength();
				double length = max - min;

				if (refinementConfig.isInitRefinementOnLogScale() && (max / min - 1) > lengthStopCriterion || !refinementConfig.isInitRefinementOnLogScale() && length > lengthStopCriterion) {
					this.logger.info("Test for isRefinementCompletedPredicate({},{}) is negative. Interval length of [{},{}] is {}. Required length to consider an interval atomic is {}", params[0].getName(), objectContainer, min, max,
							length, refinementConfig.getIntervalLength());
					return false;
				}
			} else if (param.getDefaultDomain() instanceof CategoricalParameterDomain) { // categorical params can be refined iff the have not been set and closed before
				assert param.getDefaultValue() != null : "Param " + param.getName() + " has no default value!";
				if (!variableHasBeenSet && !variableHasBeenClosed) {
					this.logger.info("Test for isRefinementCompletedPredicate({},{}) is negative", params[0].getName(), objectContainer);
					return false;
				}
			} else {
				throw new UnsupportedOperationException("Currently no support for testing parameters of type " + param.getClass().getName());
			}
		}
		this.logger.info("Test for isRefinementCompletedPredicate({},{}) is positive", params[0].getName(), objectContainer);
		return true;
	}
}