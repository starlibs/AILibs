package jaicore.planning.model.task.ceocstn;

import java.util.Collection;

import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.stn.STNPlanningDomain;

@SuppressWarnings("serial")
public class CEOCSTNPlanningDomain extends STNPlanningDomain {

	public CEOCSTNPlanningDomain(Collection<? extends CEOCOperation> operations, Collection<? extends OCMethod> methods) {
		super(operations, methods);
	}

	@SuppressWarnings("unchecked")
	public Collection<? extends CEOCOperation> getOperations() {
		return (Collection<? extends CEOCOperation>)super.getOperations();
	}

	@SuppressWarnings("unchecked")
	public Collection<? extends OCMethod> getMethods() {
		return (Collection<? extends OCMethod>)super.getMethods();
	}
}
