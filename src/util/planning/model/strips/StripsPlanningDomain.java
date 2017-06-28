package util.planning.model.strips;

import java.util.ArrayList;
import java.util.Collection;

import util.planning.model.core.Operation;
import util.planning.model.core.PlanningDomain;

public class StripsPlanningDomain extends PlanningDomain {

	public StripsPlanningDomain(Collection<StripsOperation> operations) {
		super(new ArrayList<Operation>(operations));
	}

}
