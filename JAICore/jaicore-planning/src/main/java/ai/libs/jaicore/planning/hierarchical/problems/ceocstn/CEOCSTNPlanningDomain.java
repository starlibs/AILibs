package ai.libs.jaicore.planning.hierarchical.problems.ceocstn;

import java.util.Collection;

import ai.libs.jaicore.planning.classical.problems.ceoc.CEOCOperation;
import ai.libs.jaicore.planning.hierarchical.problems.stn.STNPlanningDomain;

@SuppressWarnings("serial")
public class CEOCSTNPlanningDomain extends STNPlanningDomain {

	public CEOCSTNPlanningDomain(final Collection<? extends CEOCOperation> operations, final Collection<? extends OCMethod> methods) {
		super(operations, methods);
	}

	@Override
	public boolean isValid() {
		for (CEOCOperation op : this.getOperations()) {
			boolean isValid = !(op.getAddLists().isEmpty() && op.getDeleteLists().isEmpty());
			assert isValid : "Degenerated planning problem. Operation \"" + op.getName() + "\" has empty add list and empty delete list!";
			if (!isValid) {
				return false;
			}
		}
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<? extends CEOCOperation> getOperations() {
		return (Collection<CEOCOperation>)super.getOperations();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<? extends OCMethod> getMethods() {
		return (Collection<? extends OCMethod>)super.getMethods();
	}
}
