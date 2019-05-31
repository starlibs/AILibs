package hasco.variants.forwarddecomposition;

import java.util.function.Predicate;

import hasco.core.HASCO;
import hasco.core.Util;
import hasco.model.ComponentInstance;
import hasco.model.ComponentUtil;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

/**
 * This is a node evaluator that assigns 0 to all nodes encoding (partial) compositions where each component refinement is with its default parameters.
 *
 * This is a somewhat cyclic component, because it needs to know the HASCO object it will advise, but it is already needed to initialize HASCO. So to use it, the hasco variable must be set after initialization.
 *
 * @author fmohr
 *
 */
public class DefaultPathPriorizingPredicate<N, A> implements Predicate<N> {

	private HASCO<?, N, A, ?> hasco;

	@Override
	public boolean test(final N node) {
		if (this.hasco == null) {
			throw new IllegalStateException("HASCO has not yet been set!");
		}
		if (!(node instanceof TFDNode)) {
			throw new IllegalArgumentException("Currently we only support TFDNodes for node priorization");
		}
		if (this.hasco.getInput() == null) {
			throw new IllegalStateException("HASCO exists, but its problem input has not been defined yet.");
		}
		ComponentInstance inst = Util.getSolutionCompositionFromState(this.hasco.getInput().getComponents(), ((TFDNode) node).getState(), false);
		if (inst == null) {
			return true;
		}
		return ComponentUtil.isDefaultConfiguration(inst);
	}

	public HASCO<?, N, A, ?> getHasco() {
		return this.hasco;
	}

	public void setHasco(final HASCO<?, N, A, ?> hasco) {
		this.hasco = hasco;
	}
}
