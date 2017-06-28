package util.planning.model.ceoc;

import java.util.ArrayList;
import java.util.Collection;

import util.planning.model.core.Operation;
import util.planning.model.core.PlanningDomain;

public class CEOCPlanningDomain extends PlanningDomain {

	public CEOCPlanningDomain(Collection<CEOCOperation> operations) {
		super(new ArrayList<Operation>(operations));
	}

}
