package ai.libs.jaicore.logic.fol.algorithms.resolution;

import java.util.stream.Collectors;

import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.Clause;
import ai.libs.jaicore.logic.fol.structure.Literal;

public class SolverFactory {
	private static SolverFactory singleton = new SolverFactory();

	private SolverFactory() {
	}

	public static SolverFactory getInstance() {
		return singleton;
	}

	public Solver getSolver(final CNFFormula formula) {

		/* check if formula is in horn */
		boolean isHorn = true;
		for (Clause c : formula) {
			if (c.stream().filter(Literal::isPositive).limit(2).collect(Collectors.toList()).size() > 1) {
				isHorn = false;
				break;
			}
		}
		if (isHorn) {
			Solver solver = new UnitResolutionSolver();
			solver.addFormula(formula);
			return solver;
		}

		throw new IllegalArgumentException("Formula " + formula + " is not in HORN!");
	}
}
