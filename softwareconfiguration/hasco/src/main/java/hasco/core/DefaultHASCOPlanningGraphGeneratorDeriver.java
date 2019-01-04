package hasco.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;
import jaicore.planning.core.Plan;
import jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningGraphGeneratorDeriver;
//github.com/fmohr/AILibs.git
import jaicore.search.core.interfaces.GraphGenerator;

/**
 * This class only serves to facilitate the usage of HASCO when passing a IPlanningGraphGeneratorDeriver.
 * HASCO requires a IHASCOPlanningGraphGeneratorDeriver, which only takes away some of the generics of IPlanningGraphGeneratorDeriver,
 * but this implies that you cannot just use arbitrary IPlanningGraphGeneratorDeriver objects anymore.
 * To circumvent this problem, this class implements the IHASCOPlanningGraphGeneratorDeriver and wraps any IPlanningGraphGeneratorDeriver.
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 */
public class DefaultHASCOPlanningGraphGeneratorDeriver<N, A> implements IHASCOPlanningGraphGeneratorDeriver<N, A> {

	private final IHierarchicalPlanningGraphGeneratorDeriver<CEOCIPSTNPlanningProblem, N, A> wrappedDeriver;

	public DefaultHASCOPlanningGraphGeneratorDeriver(final IHierarchicalPlanningGraphGeneratorDeriver<CEOCIPSTNPlanningProblem, N, A> wrappedDeriver) {
		super();
		this.wrappedDeriver = wrappedDeriver;
	}

	@Override
	public GraphGenerator<N, A> transform(final CEOCIPSTNPlanningProblem problem) {
		return this.wrappedDeriver.transform(problem);
	}

	@Override
	public Plan getPlan(final List<N> path) {
		return this.wrappedDeriver.getPlan(path);
	}

	public IHierarchicalPlanningGraphGeneratorDeriver<CEOCIPSTNPlanningProblem, N, A> getWrappedDeriver() {
		return this.wrappedDeriver;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("wrappedDeriver", this.wrappedDeriver);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
