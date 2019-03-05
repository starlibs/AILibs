package jaicore.planning.classical.problems.strips;

import java.util.ArrayList;
import java.util.Collection;

public class StripsPlanningDomain extends PlanningDomain {

	public StripsPlanningDomain(Collection<StripsOperation> operations) {
		super(new ArrayList<Operation>(operations));
	}
}
