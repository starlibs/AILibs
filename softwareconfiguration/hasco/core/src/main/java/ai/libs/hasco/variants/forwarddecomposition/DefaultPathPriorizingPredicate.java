package ai.libs.hasco.variants.forwarddecomposition;

import java.util.Objects;
import java.util.function.Predicate;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.IHascoAware;
import ai.libs.hasco.core.Util;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.ComponentUtil;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

/**
 * This is a node evaluator that assigns 0 to all nodes encoding (partial) compositions where each component refinement is with its default parameters.
 *
 * This is a somewhat cyclic component, because it needs to know the HASCO object it will advise, but it is already needed to initialize HASCO. So to use it, the hasco variable must be set after initialization.
 *
 * @author fmohr
 *
 */
public class DefaultPathPriorizingPredicate<N, A> implements Predicate<N>, IHascoAware {

	private HASCO<?, N, A, ?> hasco;

	@Override
	public boolean test(final N node) {
		Objects.requireNonNull(node);
		if (this.hasco == null) {
			throw new IllegalStateException("HASCO has not yet been set!");
		}
		if (!(node instanceof TFDNode)) {
			throw new IllegalArgumentException("Currently we only support TFDNodes for node priorization");
		}
		if (this.hasco.getInput() == null) {
			throw new IllegalStateException("HASCO exists, but its problem input has not been defined yet.");
		}
		TFDNode tfd = (TFDNode) node;
		Monom stateAfterLastAction = tfd.getState();

		/* now check whether the last edge was a method that will necessary induce a certain successor state  */
		ComponentInstance inst = Util.getSolutionCompositionFromState(this.hasco.getInput().getComponents(), stateAfterLastAction, false);
		if (inst == null) {
			return true;
		}
		return ComponentUtil.isDefaultConfiguration(inst);
	}

	@Override
	public void setHascoReference(final HASCO hasco) {
		this.hasco = hasco;
	}

	@Override
	public HASCO getHASCOReference() {
		return this.hasco;
	}
}
