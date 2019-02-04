package hasco.metamining;

import java.util.Comparator;

import hasco.core.HASCO;
import hasco.core.Util;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;

/**
 * A Comparator for {@link TFDNode}s that sorts based on meta information about
 * the underlying {@link ComponentInstance} of the node and possibly application
 * context.
 * 
 * @author Helena Graf
 *
 */
public class MetaMinerBasedSorter implements Comparator<TFDNode> {

	/**
	 * The ComponentLoader object used to get the available components
	 */
	private ComponentLoader loader;

	/**
	 * The "MetaMiner" has access to the meta information of the given
	 * {@link ComponentInstance} and possibly its application context. It is used to
	 * derive a score of a given ComponentInstance, based on which a comparison of
	 * the given {@link TFDNode}s is made.
	 */
	private IMetaMiner metaminer;

	/**
	 * Creates a new MetaminerBasedSorter object that compares given
	 * {@link TFDNode}s based on the attached {@link ComponentInstance}.
	 * 
	 * @param metaminer
	 *            The {@link IMetaMiner} used to score the ComponentInstances
	 *            attached to a TFDNode. Has to already have been built.
	 * @param hasco
	 *            The {@link HASCO} object used to get the available components
	 */
	public MetaMinerBasedSorter(IMetaMiner metaminer, ComponentLoader loader) {
		this.loader = loader;
		this.metaminer = metaminer;
	}

	@Override
	public int compare(TFDNode o1, TFDNode o2) {
		double score1 = metaminer.score(convertToComponentInstance(o1));
		double score2 = metaminer.score(convertToComponentInstance(o2));

		return (int) Math.signum(score1 - score2);
	}

	private ComponentInstance convertToComponentInstance(TFDNode node) {
		return Util.getSolutionCompositionFromState(loader.getComponents(), node.getState(), false);
	}

	/**
	 * Gets the {@link IMetaMiner}, which is used to derive a score for a given
	 * {@link TFDNode} based on its attached {@link ComponentInstance}.
	 * 
	 * @return The meta miner
	 */
	public IMetaMiner getMetaminer() {
		return metaminer;
	}

	/**
	 * Sets the {@link IMetaMiner}, which is used to derive a score for a given
	 * {@link TFDNode} based on its attached {@link ComponentInstance}.
	 * 
	 * @param metaminer
	 *            The meta miner
	 */
	public void setMetaminer(IMetaMiner metaminer) {
		this.metaminer = metaminer;
	}
}
