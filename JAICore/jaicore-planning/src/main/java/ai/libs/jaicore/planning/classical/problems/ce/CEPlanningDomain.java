package ai.libs.jaicore.planning.classical.problems.ce;

import java.util.ArrayList;
import java.util.Collection;

import ai.libs.jaicore.planning.classical.problems.strips.Operation;
import ai.libs.jaicore.planning.classical.problems.strips.PlanningDomain;

public class CEPlanningDomain extends PlanningDomain {

	public CEPlanningDomain(Collection<CEOperation> operations) {
		super(new ArrayList<Operation>(operations));
	}

}
