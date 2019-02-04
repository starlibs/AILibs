package jaicore.planning.hierarchical.problems.rtn;

import java.util.Collection;

import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningDomain;

@SuppressWarnings("serial")
public class RTNPlanningDomain<O extends CEOCOperation, M extends RTNMethod> extends CEOCSTNPlanningDomain<O,M> {

	public RTNPlanningDomain(Collection<O> operations, Collection<M> methods) {
		super(operations, methods);
	}
}
