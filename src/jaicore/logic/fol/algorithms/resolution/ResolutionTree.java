package jaicore.logic.fol.algorithms.resolution;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jaicore.logic.fol.structure.Clause;

public class ResolutionTree {
	private Set<Clause> baseClauses;
	private Set<ResolutionPair> resolvedPairs = new HashSet<>();
	private Map<Clause, ResolutionStep> resolventsWithTheirSteps = new HashMap<>();

	public ResolutionTree(Set<Clause> baseClauses) {
		super();
		this.baseClauses = baseClauses;
	}

	public void addResolutionStep(ResolutionStep step) {
		this.resolventsWithTheirSteps.put(step.getR(), step);
		this.resolvedPairs.add(step.getPair());
	}

	public Set<Clause> getBaseClauses() {
		return baseClauses;
	}

	public Map<Clause, ResolutionStep> getResolventsWithTheirSteps() {
		return resolventsWithTheirSteps;
	}

	public boolean isClausePairAdmissible(ResolutionPair pair) {
		if (resolvedPairs.contains(pair))
			return false;
		Clause c1 = pair.getC1();
		Clause c2 = pair.getC2();
		if (baseClauses.contains(c1) && baseClauses.contains(c2))
			return false;
		Set<Clause> parentsOfC1 = getAllClausesUsedToObtainResolvent(c1);
		if (parentsOfC1.contains(c2))
			return false;
		Set<Clause> parentsOfC2 = getAllClausesUsedToObtainResolvent(c2);
		if (parentsOfC2.contains(c1))
			return false;
		return true;
	}

	public Set<Clause> getAllClausesUsedToObtainResolvent(Clause resolvent) {
		Set<Clause> clauses = new HashSet<>();
		for (ResolutionStep step : getAllStepsUsedToObtainResolvent(resolvent)) {
			clauses.add(step.getPair().getC1());
			clauses.add(step.getPair().getC2());
		}
		return clauses;
	}

	public boolean containsResolvent(Clause resolvent) {
		return this.baseClauses.contains(resolvent) || resolventsWithTheirSteps.containsKey(resolvent);
	}

	public boolean containsEmptyClause() {
		return containsResolvent(new Clause());
	}

	public Set<ResolutionStep> getAllStepsUsedToObtainResolvent(Clause resolvent) {
		if (baseClauses.contains(resolvent))
			return new HashSet<>();
		Set<ResolutionStep> steps = new HashSet<>();
		ResolutionStep step = resolventsWithTheirSteps.get(resolvent);
		steps.add(step);
		steps.addAll(getAllStepsUsedToObtainResolvent(step.getPair().getC1()));
		steps.addAll(getAllStepsUsedToObtainResolvent(step.getPair().getC2()));
		return steps;
	}

	public void printAsGraphViz(String filename) {
		StringBuilder str = new StringBuilder();
		str.append("digraph {\n");
		for (Clause c : baseClauses) {
			str.append("\"" + c.toString() + "\"\n");
		}
		for (Clause resolvent : resolventsWithTheirSteps.keySet()) {
			ResolutionPair pair = resolventsWithTheirSteps.get(resolvent).getPair();
			str.append("\"" + pair.getC1().toString() + "\" -> \"" + resolvent.toString() + "\"\n");
			str.append("\"" + pair.getC2().toString() + "\" -> \"" + resolvent.toString() + "\"\n");
		}
		str.append("}");

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8)) {
			writer.write(str.toString());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}