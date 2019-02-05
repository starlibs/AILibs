package jaicore.planning.hierarchical.problems.ceocstn;

import java.util.Collection;

import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.problems.stn.STNPlanningDomain;

@SuppressWarnings("serial")
public class CEOCSTNPlanningDomain extends STNPlanningDomain {

	public CEOCSTNPlanningDomain(Collection<? extends CEOCOperation> operations, Collection<? extends OCMethod> methods) {
		super(operations, methods);
	}
	
	public boolean isValid() {
		for (CEOCOperation op : getOperations()) {
			boolean isValid = !(op.getAddLists().isEmpty() && op.getDeleteLists().isEmpty());
			assert isValid : "Degenerated planning problem. Operation \"" + op.getName() + "\" has empty add list and empty delete list!";
			if (!isValid)
				return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<? extends CEOCOperation> getOperations() {
		return (Collection<CEOCOperation>)super.getOperations();
	}
	
	@SuppressWarnings("unchecked")
	public Collection<? extends OCMethod> getMethods() {
		return (Collection<? extends OCMethod>)super.getMethods();
	}
}
