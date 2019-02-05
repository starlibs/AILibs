package jaicore.planning.hierarchical.problems.rtn;

import java.util.Collection;

import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningDomain;

@SuppressWarnings("serial")
public class RTNPlanningDomain extends CEOCSTNPlanningDomain {

	public RTNPlanningDomain(Collection<? extends CEOCOperation> operations, Collection<? extends RTNMethod> methods) {
		super(operations, methods);
	}

	@SuppressWarnings("unchecked")
	public Collection<RTNMethod> getMethods() {
		return (Collection<RTNMethod>)super.getMethods();
	}
}
