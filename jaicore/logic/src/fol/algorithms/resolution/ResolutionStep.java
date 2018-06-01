package jaicore.logic.fol.algorithms.resolution;

import java.util.Map;

import jaicore.logic.fol.structure.Clause;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.VariableParam;

public class ResolutionStep {
	private ResolutionPair pair;
	private Clause r;
	private Map<VariableParam, LiteralParam> unificator;

	public ResolutionStep(ResolutionPair pair, Clause r, Map<VariableParam, LiteralParam> unificator) {
		super();
		this.pair = pair;
		this.r = r;
		this.unificator = unificator;
	}

	public ResolutionPair getPair() {
		return pair;
	}

	public Clause getR() {
		return r;
	}

	public Map<VariableParam, LiteralParam> getUnificator() {
		return unificator;
	}
}
