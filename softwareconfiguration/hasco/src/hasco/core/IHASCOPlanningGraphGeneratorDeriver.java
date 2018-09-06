package hasco.core;

import jaicore.planning.graphgenerators.IPlanningGraphGeneratorDeriver;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.OCMethod;

public interface IHASCOPlanningGraphGeneratorDeriver<N, A>
		extends IPlanningGraphGeneratorDeriver<CEOCOperation, OCMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCMethod, CEOCAction>, N, A> {

}
