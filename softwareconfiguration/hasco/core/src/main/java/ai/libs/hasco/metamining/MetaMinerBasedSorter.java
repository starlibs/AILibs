package ai.libs.hasco.metamining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

/**
 * A Comparator for {@link TFDNode}s that sorts based on meta information about the underlying {@link ComponentInstance} of the node and possibly application context.
 *
 * @author Helena Graf
 *
 */
public class MetaMinerBasedSorter implements Comparator<TFDNode> {

	private Logger logger = LoggerFactory.getLogger(MetaMinerBasedSorter.class);

	/**
	 * Components for the current configuration used to convert TFDNodes to ComponentInstances
	 */
	private Collection<IComponent> components;

	/**
	 * The "MetaMiner" has access to the meta information of the given {@link ComponentInstance} and possibly its application context. It is used to derive a score of a given ComponentInstance, based on which a comparison of the given
	 * {@link TFDNode}s is made.
	 */
	private IMetaMiner metaminer;

	public MetaMinerBasedSorter(final IMetaMiner metaminer, final Collection<? extends IComponent> components) {
		if (components == null) {
			this.logger.warn("No Components in sorter!");
		}
		this.components = new ArrayList<>(components);
		this.metaminer = metaminer;
	}

	@Override
	public int compare(final TFDNode o1, final TFDNode o2) {
		if (this.convertToComponentInstance(o1) == null || this.convertToComponentInstance(o2) == null) {
			this.logger.warn("Cannot compare pipelines when one is null.");
			return 0;
		}
		if (o1.equals(o2)) {
			this.logger.info("Comparing two nodes which are the same.");
			return 0;
		}

		double score1 = this.metaminer.score(this.convertToComponentInstance(o1));
		double score2 = this.metaminer.score(this.convertToComponentInstance(o2));

		try {
			this.logger.trace("Node {} converted to {}", o1, this.convertToComponentInstance(o1).getPrettyPrint());
		} catch (IOException e) {
			this.logger.error("Logging failed due to {}", LoggerUtil.getExceptionInfo(e));
		}

		try {
			this.logger.trace("Node {} converted to {}", o2, this.convertToComponentInstance(o2).getPrettyPrint());
		} catch (IOException e) {
			this.logger.error("Logging failed due to {}", LoggerUtil.getExceptionInfo(e));
		}

		this.logger.debug("Comparing nodes with scores: {} vs {}", score1, score2);
		return (int) Math.signum(score1 - score2);
	}

	/**
	 * Converts the given TFDNode to a ComponentInstance.
	 *
	 * @param node
	 *            The TFDNode to convert
	 * @return The TFDNode as a ComponentInstance
	 */
	protected ComponentInstance convertToComponentInstance(final TFDNode node) {
		return HASCOUtil.getSolutionCompositionFromState(this.components, node.getState(), false);
	}

	/**
	 * Gets the {@link IMetaMiner}, which is used to derive a score for a given {@link TFDNode} based on its attached {@link ComponentInstance}.
	 *
	 * @return The meta miner
	 */
	public IMetaMiner getMetaminer() {
		return this.metaminer;
	}

	/**
	 * Sets the {@link IMetaMiner}, which is used to derive a score for a given {@link TFDNode} based on its attached {@link ComponentInstance}.
	 *
	 * @param metaminer
	 *            The meta miner
	 */
	public void setMetaminer(final IMetaMiner metaminer) {
		this.metaminer = metaminer;
	}
}