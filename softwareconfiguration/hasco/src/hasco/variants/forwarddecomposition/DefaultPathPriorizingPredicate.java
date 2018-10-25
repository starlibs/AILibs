package hasco.variants.forwarddecomposition;

import java.util.function.Predicate;

import hasco.core.HASCO;
import hasco.core.Util;
import hasco.model.ComponentInstance;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;

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
	public boolean test(N node) {
		if (hasco == null)
			throw new IllegalStateException("HASCO has not yet been set!");
		if (!(node instanceof TFDNode)) {
			throw new IllegalArgumentException("Currently we only support TFDNodes for node priorization");
		}
		if (hasco.getInput() == null)
			throw new IllegalStateException("HASCO exists, but its problem input has not been defined yet.");
		ComponentInstance inst = Util.getSolutionCompositionFromState(hasco.getInput().getComponents(), ((TFDNode) node).getState(), false);
		if (inst == null)
			return true;
		return Util.isDefaultConfiguration(inst);
	}

	public HASCO<?, N, A, ?> getHasco() {
		return hasco;
	}

	public void setHasco(HASCO<?, N, A, ?> hasco) {
		this.hasco = hasco;
	}
}
