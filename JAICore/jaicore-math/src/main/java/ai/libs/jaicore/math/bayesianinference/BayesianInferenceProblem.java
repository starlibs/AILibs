package ai.libs.jaicore.math.bayesianinference;

import java.util.Collection;
import java.util.Map;

public class BayesianInferenceProblem {
	private final BayesNet network;
	private final Map<String, Boolean> evidenceVariables;
	private final Collection<String> queryVariables;

	public BayesianInferenceProblem(final BayesNet network, final Map<String, Boolean> evidenceVariables, final Collection<String> queryVariables) {
		super();
		this.network = network;
		this.evidenceVariables = evidenceVariables;
		this.queryVariables = queryVariables;
	}

	public BayesNet getNetwork() {
		return this.network;
	}

	public Map<String, Boolean> getEvidenceVariables() {
		return this.evidenceVariables;
	}

	public Collection<String> getQueryVariables() {
		return this.queryVariables;
	}
}
