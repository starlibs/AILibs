package jaicore.planning.model.conditional;

import java.util.ArrayList;
import java.util.Collection;

import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.PlanningDomain;

public class CEPlanningDomain extends PlanningDomain {

	public CEPlanningDomain(Collection<CEOperation> operations) {
		super(new ArrayList<Operation>(operations));
	}

}
