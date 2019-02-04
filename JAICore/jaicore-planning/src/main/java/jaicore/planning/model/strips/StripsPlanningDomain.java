package jaicore.planning.model.strips;

import java.util.ArrayList;
import java.util.Collection;

import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.PlanningDomain;

public class StripsPlanningDomain extends PlanningDomain {

	public StripsPlanningDomain(Collection<StripsOperation> operations) {
		super(new ArrayList<Operation>(operations));
	}
}
