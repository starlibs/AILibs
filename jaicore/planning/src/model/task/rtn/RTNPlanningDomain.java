package jaicore.planning.model.task.rtn;

import java.util.Collection;

import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningDomain;

@SuppressWarnings("serial")
public class RTNPlanningDomain extends CEOCSTNPlanningDomain {

	public RTNPlanningDomain(Collection<? extends CEOCOperation> operations, Collection<RTNMethod> methods) {
		super(operations, methods);
	}
}
