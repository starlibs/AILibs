package ai.libs.hasco.core;

import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.planning.core.Plan;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningToGraphSearchReduction;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

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
public class DefaultHASCOPlanningReduction<N, A> implements IHASCOPlanningReduction<N, A> {

	private final IHierarchicalPlanningToGraphSearchReduction<N, A, ? super CEOCIPSTNPlanningProblem, ? extends Plan, ? extends GraphSearchInput<N,A>, ? super SearchGraphPath<N, A>> wrappedDeriver;

	public DefaultHASCOPlanningReduction(final IHierarchicalPlanningToGraphSearchReduction<N, A, ? super CEOCIPSTNPlanningProblem, ? extends Plan, ? extends GraphSearchInput<N,A>, ? super SearchGraphPath<N, A>> wrappedDeriver) {
		super();
		this.wrappedDeriver = wrappedDeriver;
	}

	@Override
	public GraphSearchInput<N, A> encodeProblem(final CEOCIPSTNPlanningProblem problem) {
		return this.wrappedDeriver.encodeProblem(problem);
	}

	@Override
	public Plan decodeSolution(final SearchGraphPath<N, A> path) {
		return this.wrappedDeriver.decodeSolution(path);
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("wrappedDeriver", this.wrappedDeriver);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
