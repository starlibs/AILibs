package ai.libs.jaicore.search.algorithms.standard.random;

import java.util.List;

import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomSearchUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(RandomSearchUtil.class);

	private RandomSearchUtil() {
		/* prevent instantiation */
	}

	public static <N, A> boolean checkValidityOfPathCompletion(final ILabeledPath<N, A> prefix, final ILabeledPath<N, A> completion) throws InterruptedException {
		List<N> prefixNodes = prefix.getNodes();
		List<A> prefixArcs = prefix.getArcs();
		List<N> completionNodes = completion.getNodes();
		List<A> completionArcs = completion.getArcs();
		if (completionArcs.size() != completionNodes.size() - 1) {
			LOGGER.error("Incorrect number of arcs!");
			return false;
		}
		if (prefixArcs.size() != prefixNodes.size() - 1) {
			LOGGER.error("Incorrect number of arcs!");
			return false;
		}
		if (prefixNodes.size() > completionNodes.size()) {
			LOGGER.error("Completion is shorter than prefix!");
			return false;
		}
		int l = prefixNodes.size();
		for (int i = 0; i < l; i++) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (prefixNodes.get(i) != completionNodes.get(i)) {
				LOGGER.error("The {}-th node on the path does not match the respective node in the prefix:\n\tPath:\t{}\n\tPrefix:\t{}", i, completionNodes.get(i), prefixNodes.get(i));
				return false;
			}
			if (i < l-1 && prefixArcs.get(i) != completionArcs.get(i)) {
				LOGGER.error("The {}-th arc on the path does not match the respective arc in the prefix:\n\tPath:\t{}\n\tPrefix:\t{}", i, completionArcs.get(i), prefixArcs.get(i));
				return false;
			}
		}
		int n = completionNodes.size();
		for (int i = l; i < n; i++) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (prefixNodes.contains(completionNodes.get(i))) {
				LOGGER.error("A node contained in the completion (without prefix) must not also be contained in the prefix already. The following node is contained twice:\n\t{}", completionNodes.get(i));
				return false;
			}
		}
		return true;
	}
}
