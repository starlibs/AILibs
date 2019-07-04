package ai.libs.jaicore.planning.hierarchical.problems.stn;

import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;

@SuppressWarnings("serial")
public class STNPlanningProblem implements IHTNPlanningProblem {

	private final STNPlanningDomain domain;
	private final CNFFormula knowledge;
	private final Monom init;
	private final TaskNetwork network;
	private static final boolean SORT_NETWORK_BASED_ON_NUMBER_PREFIXES = true;

	public STNPlanningProblem(final STNPlanningDomain domain, final CNFFormula knowledge, final Monom init, final TaskNetwork network) {
		super();
		this.domain = domain;
		this.knowledge = knowledge;
		this.init = init;
		this.network = network;
	}

	@Override
	public STNPlanningDomain getDomain() {
		return this.domain;
	}

	@Override
	public CNFFormula getKnowledge() {
		return this.knowledge;
	}

	@Override
	public Monom getInit() {
		return this.init;
	}

	@Override
	public TaskNetwork getNetwork() {
		return this.network;
	}

	public boolean isSortNetworkBasedOnNumberPrefixes() {
		return SORT_NETWORK_BASED_ON_NUMBER_PREFIXES;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.domain == null) ? 0 : this.domain.hashCode());
		result = prime * result + ((this.init == null) ? 0 : this.init.hashCode());
		result = prime * result + ((this.knowledge == null) ? 0 : this.knowledge.hashCode());
		result = prime * result + ((this.network == null) ? 0 : this.network.hashCode());
		result = prime * result + (SORT_NETWORK_BASED_ON_NUMBER_PREFIXES ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		STNPlanningProblem other = (STNPlanningProblem) obj;
		if (this.domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!this.domain.equals(other.domain)) {
			return false;
		}
		if (this.init == null) {
			if (other.init != null) {
				return false;
			}
		} else if (!this.init.equals(other.init)) {
			return false;
		}
		if (this.knowledge == null) {
			if (other.knowledge != null) {
				return false;
			}
		} else if (!this.knowledge.equals(other.knowledge)) {
			return false;
		}
		if (this.network == null) {
			if (other.network != null) {
				return false;
			}
		} else if (!this.network.equals(other.network)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("domain", this.domain);
		fields.put("knowledge", this.knowledge);
		fields.put("init", this.init);
		fields.put("network", this.network);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
