package ai.libs.mlplan.sklearn;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.ml.scikitwrapper.AScikitLearnWrapper;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnTimeSeriesRegressionWrapper;

public class ScikitLearnTimeSeriesRegressionFactory extends AScikitLearnLearnerFactory {

	public ScikitLearnTimeSeriesRegressionFactory() {
		super();
	}

	@Override
	public String getPipelineBuildString(final IComponentInstance groundComponent, final Set<String> importSet) {
		StringBuilder sb = new StringBuilder();
		List<IComponentInstance> timeseriesFeatureGenerator = groundComponent.getSatisfactionOfRequiredInterface("timeseries_feature_generator");
		sb.append(this.getTimeseriesConstructionInstruction(timeseriesFeatureGenerator, importSet));
		sb.append(",");
		sb.append(this.extractSKLearnConstructInstruction(groundComponent.getSatisfactionOfRequiredInterface("regressor").iterator().next(), importSet));
		return sb.toString();
	}

	private String getTimeseriesConstructionInstruction(final List<IComponentInstance> timeseriesComponentInstances, final Set<String> importSet) {
		StringJoiner stringJoiner = new StringJoiner(",");
		int numberOfComponentInstancesFound = 0;
		for (IComponentInstance componentInstance : timeseriesComponentInstances.stream().sorted((o1, o2) -> o1.getComponent().getName().compareTo(o2.getComponent().getName())).collect(Collectors.toList())) {
			if (componentInstance.getComponent().getName().endsWith("UniToMultivariateNumpyBasedFeatureGenerator")) {
				for (IComponentInstance satCI : componentInstance.getSatisfactionOfRequiredInterface("univariate_ts_feature_generator").stream().sorted((o1, o2) -> o1.getComponent().getName().compareTo(o2.getComponent().getName()))
						.collect(Collectors.toList())) {
					Map<String, List<IComponentInstance>> satisfactionOfRequiredInterfaces = new HashMap<>();
					satisfactionOfRequiredInterfaces.put("univariate_ts_feature_generator", Arrays.asList(satCI));
					IComponentInstance newCI = new ComponentInstance(componentInstance.getComponent(), componentInstance.getParameterValues(), satisfactionOfRequiredInterfaces);
					stringJoiner.add(this.extractSKLearnConstructInstruction(newCI, importSet));
					numberOfComponentInstancesFound++;
				}
			} else {
				numberOfComponentInstancesFound++;
				stringJoiner.add(this.extractSKLearnConstructInstruction(componentInstance, importSet));
			}
		}

		if (numberOfComponentInstancesFound > 1) {
			return "make_union(" + stringJoiner.toString() + ")";
		}
		return stringJoiner.toString();
	}

	@Override
	public AScikitLearnWrapper<IPrediction, IPredictionBatch> getScikitLearnWrapper(final String constructionString, final String imports) throws IOException, InterruptedException {
		return new ScikitLearnTimeSeriesRegressionWrapper<>(constructionString, imports);
	}
}
