package ai.libs.jaicore.search.probleminputs;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class MDPUtils {
	public static <N, A> N drawSuccessorState(final IMDP<N, A, ?> mdp, final N state, final A action) {
		return drawSuccessorState(mdp, state, action, new Random());
	}

	public static <N, A> N drawSuccessorState(final IMDP<N, A, ?> mdp, final N state, final A action, final Random rand) {

		Map<N, Double> dist = mdp.getProb(state, action);
		if (!mdp.getApplicableActions(state).contains(action)) {
			throw new IllegalArgumentException("Action " + action + " is not applicable in " + state);
		}
		double p = rand.nextDouble();
		double s = 0;
		for (Entry<N, Double> neighborWithProb : dist.entrySet()) {
			s += neighborWithProb.getValue();
			if (s >= p) {
				return neighborWithProb.getKey();
			}
		}
		throw new IllegalStateException("Up to here, a state mut have been returned!");
	}
}
