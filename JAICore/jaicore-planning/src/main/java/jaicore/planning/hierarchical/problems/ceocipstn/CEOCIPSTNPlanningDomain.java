package jaicore.planning.hierarchical.problems.ceocipstn;

import java.io.Serializable;
import java.util.Collection;

import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningDomain;

@SuppressWarnings("serial")
public class CEOCIPSTNPlanningDomain<O extends CEOCOperation, M extends OCIPMethod> extends CEOCSTNPlanningDomain<O, M> implements Serializable {

	
	public CEOCIPSTNPlanningDomain(Collection<O> operations, Collection<M> methods) {
		super(operations, methods);
	}
}
