package ai.libs.jaicore.search.algorithms.standard.random;

import java.util.List;

import org.api4.java.datastructure.graph.ILabeledPath;

public class RandomSearchUtil {

	public static <N, A> boolean checkValidityOfPathCompletion(final ILabeledPath<N, A> prefix, final ILabeledPath<N, A> completion) throws InterruptedException {
		List<N> prefixNodes = prefix.getNodes();
		List<A> prefixArcs = prefix.getArcs();
		List<N> completionNodes = completion.getNodes();
		List<A> completionArcs = completion.getArcs();
		if (completionArcs.size() != completionNodes.size() - 1) {
			System.err.println("Incorrect number of arcs!");
			return false;
		}
		if (prefixArcs.size() != prefixNodes.size() - 1) {
			System.err.println("Incorrect number of arcs!");
			return false;
		}
		if (prefixNodes.size() > completionNodes.size()) {
			System.err.println("Completion is shorter than prefix!");
			return false;
		}
		int l = prefixNodes.size();
		for (int i = 0; i < l; i++) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (prefixNodes.get(i) != completionNodes.get(i)) {
				System.err.println("The " + i + "-th node on the path does not match the respective node in the prefix:\n\tPath:\t" + completionNodes.get(i) + "\n\tPrefix:\t" + prefixNodes.get(i));
				return false;
			}
			if (i < l-1 && prefixArcs.get(i) != completionArcs.get(i)) {
				System.err.println("The " + i + "-th arc on the path does not match the respective node in the prefix:\n\tPath:\t" + completionArcs.get(i) + "\n\tPrefix:\t" + prefixArcs.get(i));
				return false;
			}
		}
		int n = completionNodes.size();
		for (int i = l; i < n; i++) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (prefixNodes.contains(completionNodes.get(i))) {
				System.err.println("A node contained in the completion (without prefix) must not also be contained in the prefix already. The following node is contained twice:\n\t" + completionNodes.get(i));
				return false;
			}
		}
		return true;
	}
}
