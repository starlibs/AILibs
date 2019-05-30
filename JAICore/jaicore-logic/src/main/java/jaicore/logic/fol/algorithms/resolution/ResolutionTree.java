package jaicore.logic.fol.algorithms.resolution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jaicore.logic.fol.structure.Clause;

public class ResolutionTree {
	private Set<Clause> baseClauses;
	private Set<ResolutionPair> resolvedPairs = new HashSet<>();
	private Map<Clause, ResolutionStep> resolventsWithTheirSteps = new HashMap<>();

	public ResolutionTree(final Set<Clause> baseClauses) {
		super();
		this.baseClauses = baseClauses;
	}

	public void addResolutionStep(final ResolutionStep step) {
		this.resolventsWithTheirSteps.put(step.getR(), step);
		this.resolvedPairs.add(step.getPair());
	}

	public Set<Clause> getBaseClauses() {
		return this.baseClauses;
	}

	public Map<Clause, ResolutionStep> getResolventsWithTheirSteps() {
		return this.resolventsWithTheirSteps;
	}

	public boolean isClausePairAdmissible(final ResolutionPair pair) {
		if (this.resolvedPairs.contains(pair)) {
			return false;
		}
		Clause c1 = pair.getC1();
		Clause c2 = pair.getC2();
		if (this.baseClauses.contains(c1) && this.baseClauses.contains(c2)) {
			return false;
		}
		Set<Clause> parentsOfC1 = this.getAllClausesUsedToObtainResolvent(c1);
		if (parentsOfC1.contains(c2)) {
			return false;
		}
		Set<Clause> parentsOfC2 = this.getAllClausesUsedToObtainResolvent(c2);
		return !parentsOfC2.contains(c1);
	}

	public Set<Clause> getAllClausesUsedToObtainResolvent(final Clause resolvent) {
		Set<Clause> clauses = new HashSet<>();
		for (ResolutionStep step : this.getAllStepsUsedToObtainResolvent(resolvent)) {
			clauses.add(step.getPair().getC1());
			clauses.add(step.getPair().getC2());
		}
		return clauses;
	}

	public boolean containsResolvent(final Clause resolvent) {
		return this.baseClauses.contains(resolvent) || this.resolventsWithTheirSteps.containsKey(resolvent);
	}

	public boolean containsEmptyClause() {
		return this.containsResolvent(new Clause());
	}

	public Set<ResolutionStep> getAllStepsUsedToObtainResolvent(final Clause resolvent) {
		if (this.baseClauses.contains(resolvent)) {
			return new HashSet<>();
		}
		Set<ResolutionStep> steps = new HashSet<>();
		ResolutionStep step = this.resolventsWithTheirSteps.get(resolvent);
		steps.add(step);
		steps.addAll(this.getAllStepsUsedToObtainResolvent(step.getPair().getC1()));
		steps.addAll(this.getAllStepsUsedToObtainResolvent(step.getPair().getC2()));
		return steps;
	}
}