package hasco.variants.forwarddecomposition;

import java.util.Collection;

import hasco.core.HASCO;
import hasco.core.IHASCOPlanningGraphGeneratorDeriver;
import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.logic.fol.structure.Monom;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;

/**
 * This is a node evaluator that assigns 0 to all nodes encoding (partial) compositions where each
 * component refinement is with its default parameters.
 * 
 * This is a somewhat cyclic component, because it needs to know the HASCO object it will advise,
 * but it is already needed to initialize HASCO. So to use it, the hasco variable must be set
 * after initialization.
 * 
 * @author fmohr
 *
 */
public class DefaultPathPriorizingNodeEvaluator<N,A> implements INodeEvaluator<N, Double> {
	
	private HASCO<?, N, A, ?> hasco;

	@Override
	public Double f(Node<N, ?> node) throws Exception {
		if (hasco == null)
			throw new IllegalStateException("HASCO has not yet been set!");
		IHASCOPlanningGraphGeneratorDeriver<N, A> planningGraphDeriver = hasco.getPlanningGraphGeneratorDeriver();
		Collection<Component> components = hasco.getInput().getComponents();
		Monom initState = hasco.getPlanningProblem().getCorePlanningProblem().getInit();
		ComponentInstance inst = Util.getSolutionCompositionForNode(planningGraphDeriver, components, initState, node);
		if (inst == null)
			return 0.0;
		boolean isDefault = Util.isDefaultConfiguration(inst);
		return isDefault ? 0.0 : null;
	}

	public HASCO<?, N, A, ?> getHasco() {
		return hasco;
	}

	public void setHasco(HASCO<?, N, A, ?> hasco) {
		this.hasco = hasco;
	}
}
