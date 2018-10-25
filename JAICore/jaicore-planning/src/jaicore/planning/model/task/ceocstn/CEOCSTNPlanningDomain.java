package jaicore.planning.model.task.ceocstn;

import java.util.Collection;

import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.stn.STNPlanningDomain;

@SuppressWarnings("serial")
public class CEOCSTNPlanningDomain<O extends CEOCOperation, M extends OCMethod> extends STNPlanningDomain<O, M> {

	public CEOCSTNPlanningDomain(Collection<O> operations, Collection<M> methods) {
		super(operations, methods);
	}
	
	public Collection<O> getOperations() {
		return super.getOperations();
	}
}
