package jaicore.planning.model.task.rtn;

import java.util.Collection;

import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningDomain;

@SuppressWarnings("serial")
public class RTNPlanningDomain<O extends CEOCOperation, M extends RTNMethod> extends CEOCSTNPlanningDomain<O,M> {

	public RTNPlanningDomain(Collection<O> operations, Collection<M> methods) {
		super(operations, methods);
	}
}
