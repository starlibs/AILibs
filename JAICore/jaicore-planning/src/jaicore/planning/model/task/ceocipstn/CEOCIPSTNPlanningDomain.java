package jaicore.planning.model.task.ceocipstn;

import java.io.Serializable;
import java.util.Collection;

import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningDomain;

@SuppressWarnings("serial")
public class CEOCIPSTNPlanningDomain<O extends CEOCOperation, M extends OCIPMethod> extends CEOCSTNPlanningDomain<O, M> implements Serializable {

	
	public CEOCIPSTNPlanningDomain(Collection<O> operations, Collection<M> methods) {
		super(operations, methods);
	}
}
