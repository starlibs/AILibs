package ai.libs.jaicore.ml.hpo.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.ml.hpo.ga.gene.IGene;
import ai.libs.jaicore.ml.hpo.ga.gene.IntGene;
import ai.libs.jaicore.ml.hpo.ga.gene.NominalGene;
import ai.libs.jaicore.ml.hpo.ga.gene.RealGene;

public class ComponentInstanceHPOGAInput implements IComponentInstanceHPOGAInput {

	private static final String SEP = "#";
	private final IComponentInstance ci;
	private final IObjectEvaluator<IComponentInstance, Double> evaluator;

	private Map<String, IParameter> paramMap = new TreeMap<>();

	public ComponentInstanceHPOGAInput(final IComponentInstance ci, final IObjectEvaluator<IComponentInstance, Double> evaluator) {
		this.ci = ci;
		this.evaluator = evaluator;
		this.extractParameters("", ci);
	}

	private void extractParameters(final String prefix, final IComponentInstance ci) {
		for (IParameter param : ci.getComponent().getParameters()) {
			this.paramMap.put(prefix + param.getName(), param);
		}

		for (Entry<String, List<IComponentInstance>> entry : ci.getSatisfactionOfRequiredInterfaces().entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				this.extractParameters(prefix + entry.getKey() + SEP + i + SEP, entry.getValue().get(i));
			}
		}
	}

	@Override
	public IComponentInstance getComponentInstanceToOptimize() {
		return this.ci;
	}

	@Override
	public IObjectEvaluator<IComponentInstance, Double> getEvaluator() {
		return this.evaluator;
	}

	@Override
	public IIndividual newRandomIndividual(final Random rand) {
		List<IGene> genotype = new ArrayList<>();
		for (Entry<String, IParameter> p : this.paramMap.entrySet()) {
			if (p.getValue().isCategorical()) {
				IGene nominal = new NominalGene(Arrays.stream(((CategoricalParameterDomain) p.getValue().getDefaultDomain()).getValues()).collect(Collectors.toList()), rand);
				genotype.add(nominal);
			} else {
				NumericParameterDomain dom = (NumericParameterDomain) p.getValue().getDefaultDomain();
				if (dom.isInteger()) {
					genotype.add(new IntGene((int) dom.getMin(), (int) dom.getMax(), rand));
				} else {
					genotype.add(new RealGene(dom.getMin(), dom.getMax(), rand));
				}
			}
		}

		return new Individual(genotype, 1);
	}

	@Override
	public IComponentInstance convertIndividualToComponentInstance(final IIndividual individual) {
		IComponentInstance ci = new ComponentInstance((ComponentInstance) this.getComponentInstanceToOptimize());
		int paramIndex = 0;
		for (Entry<String, IParameter> p : this.paramMap.entrySet()) {
			List<String> parameterTrace = SetUtil.explode(p.getKey(), SEP);
			IComponentInstance currentCI = ci;

			// find place where to put param
			while (parameterTrace.size() > 1) {
				String reqIName = parameterTrace.remove(0);
				int ciInstanceIndex = Integer.parseInt(parameterTrace.remove(0));
				currentCI = ci.getSatisfactionOfRequiredInterface(reqIName).get(ciInstanceIndex);
			}

			// set value of the parameter
			String value = individual.getGene(paramIndex).getValueAsString();
			currentCI.getParameterValues().put(parameterTrace.get(0), value);
			paramIndex++;
		}
		return ci;
	}

}
