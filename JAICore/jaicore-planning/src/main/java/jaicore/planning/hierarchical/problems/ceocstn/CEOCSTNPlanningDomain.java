package jaicore.planning.hierarchical.problems.ceocstn;

import java.util.Collection;

import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.problems.stn.STNPlanningDomain;

@SuppressWarnings("serial")
public class CEOCSTNPlanningDomain<O extends CEOCOperation, M extends OCMethod> extends STNPlanningDomain<O, M> {

	public CEOCSTNPlanningDomain(Collection<O> operations, Collection<M> methods) {
		super(operations, methods);
	}
	
	public boolean isValid() {
		for (O op : getOperations()) {
			boolean isValid = !(op.getAddLists().isEmpty() && op.getDeleteLists().isEmpty());
			assert isValid : "Degenerated planning problem. Operation \"" + op.getName() + "\" has empty add list and empty delete list!";
			if (!isValid)
				return false;
		}
		return true;
	}
	
	public Collection<O> getOperations() {
		return super.getOperations();
	}
}
