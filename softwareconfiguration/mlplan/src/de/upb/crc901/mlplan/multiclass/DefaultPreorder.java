package de.upb.crc901.mlplan.multiclass;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jaicore.logic.fol.structure.Literal;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.stn.MethodInstance;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.structure.core.Node;

public class DefaultPreorder implements INodeEvaluator<TFDNode,Double> {
	
	private Pattern p = Pattern.compile("resolve(.*)With([^(]*)");

	private static final List<String> classifierRanking = Arrays.asList(new String[] { "RandomForest", "J48", "neighbor", "RandomTree", "tf_nn", "wekaNBMN", "MultinomialNB", "wekaNB", "BernoulliNB",
			"SimpleLogistic", "LinearSVC", "VotedPerceptron", "SMO", "LogisticRegression" });

	@Override
	public Double f(Node<TFDNode, ?> n) throws Throwable {

		if (n.getParent() == null)
			return 0.0;

		/* if no decision about the classifier has been made, return 0.0 */
		Optional<MethodInstance> methodInPlanThatChoosesClassifier = getClassifierDefiningMethodInstance(n);
		if (!methodInPlanThatChoosesClassifier.isPresent())
			return 0.0;
		
		/* if we have just decided about the classifier, assign a ranking */
		if (n.getPoint().getAppliedMethodInstance() != null && n.getPoint().getAppliedMethodInstance() == methodInPlanThatChoosesClassifier.get()) {
			String methodName = methodInPlanThatChoosesClassifier.get().getMethod().getName();
			String classifierName = methodName.substring(methodName.indexOf("With") + 4).toLowerCase();
			Optional<String> matchingClassifier = classifierRanking.stream().filter(c -> classifierName.contains(c.toLowerCase())).findFirst();
			
			if (!matchingClassifier.isPresent())
				return classifierRanking.size() * 2.0;

			/* determine chosen preprocessor */
			String preprocessor = "";
			int offset = (preprocessor.equals("")) ? 0 : classifierRanking.size();
			double f = offset + classifierRanking.indexOf(matchingClassifier.get()) * 1.0;
			return f;
		}
		
		/* otherwise, perform full computation */
		return null;
	}

	private Optional<MethodInstance> getClassifierDefiningMethodInstance(Node<TFDNode, ?> n) {
		return n.externalPath().stream().filter(n2 -> n2.getAppliedMethodInstance() != null).map(n2 -> n2.getAppliedMethodInstance())
				.filter(m -> {
					Literal task = m.getMethod().getTask();
					String taskName = task.getPropertyName();
					if (!taskName.contains("tResolve"))
						return false;
					Matcher matcher = p.matcher(m.getMethod().getName());
					matcher.find();
					return matcher.group(2).contains("weka.classifiers.");
				}).findFirst();
	}
}
