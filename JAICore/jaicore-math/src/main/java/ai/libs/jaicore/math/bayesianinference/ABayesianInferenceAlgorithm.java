package ai.libs.jaicore.math.bayesianinference;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.basic.sets.SetUtil;

public abstract class ABayesianInferenceAlgorithm extends AAlgorithm<BayesianInferenceProblem, DiscreteProbabilityDistribution> {

	protected final BayesNet net = this.getInput().getNetwork();
	protected final Collection<String> queryVariables = this.getInput().getQueryVariables();
	protected final Map<String, Boolean> evidence = this.getInput().getEvidenceVariables();
	protected final Set<String> allModelVariables = this.net.getMap().keySet();
	protected final Collection<String> hiddenVariables = SetUtil.difference(this.allModelVariables, SetUtil.union(this.queryVariables, this.evidence.keySet()));
	private DiscreteProbabilityDistribution distribution = new DiscreteProbabilityDistribution();

	public ABayesianInferenceAlgorithm(final BayesianInferenceProblem input) {
		super(input);
	}

	public BayesNet getNet() {
		return this.net;
	}

	public Collection<String> getQueryVariables() {
		return this.queryVariables;
	}

	public Map<String, Boolean> getEvidence() {
		return this.evidence;
	}

	public Set<String> getAllModelVariables() {
		return this.allModelVariables;
	}

	public Collection<String> getHiddenVariables() {
		return this.hiddenVariables;
	}

	public DiscreteProbabilityDistribution getDistribution() {
		return this.distribution;
	}

	protected void setDistribution(final DiscreteProbabilityDistribution distribution) {
		this.distribution = distribution;
	}

	@Override
	public DiscreteProbabilityDistribution call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		this.nextWithException();
		this.distribution = this.distribution.getNormalizedCopy();
		return this.distribution;
	}
}
