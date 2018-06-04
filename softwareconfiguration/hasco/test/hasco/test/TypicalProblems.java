package hasco.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import com.google.common.util.concurrent.AtomicDouble;

import hasco.core.HASCO;
import hasco.core.HASCOFD;
import hasco.core.HASCOFD.TFDSearchSpaceUtilFactory;
import hasco.core.Util;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import jaicore.basic.FileUtil;
import jaicore.order.SetUtil.Pair;
import jaicore.graphvisualizer.gui.FXController;
import jaicore.graphvisualizer.gui.FXGui;
import jaicore.graphvisualizer.gui.Recorder;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.search.structure.core.Node;

public class TypicalProblems {

	class Problem {
		private Component component;
		private List<String> parameters;
		private Map<List<String>, Double> scores = new HashMap<>();
		private DescriptiveStatistics scoreStats = new DescriptiveStatistics();
	}

	private Problem getProblem(String csvFile) throws IOException {

		/* read data */
		List<String> lines = FileUtil.readFileAsList(csvFile);
		List<String> attributes = Arrays.asList(lines.get(0).split(",")).stream().map(f -> f.substring(1)).collect(Collectors.toList());
		List<String> parameters = attributes.subList(0, attributes.size() - 1);
		List<List<String>> data = new ArrayList<>();
		for (int i = 1; i < lines.size(); i++) {
			data.add(Arrays.asList(lines.get(i).split(",")).stream().map(e -> e.trim()).collect(Collectors.toList()));
		}

		/* create the component */
		Component c = new Component("component");
		c.addProvidedInterface("iface");
		for (int i = 0; i < parameters.size(); i++) {

			final int index = i;
			Collection<String> possibleValues = data.stream().map(row -> row.get(index)).collect(Collectors.toSet());
			c.addParameter(new Parameter(parameters.get(i), new CategoricalParameterDomain(possibleValues), possibleValues.iterator().next()));
		}
		;

		/* create the score table */
		Problem prob = new Problem();
		prob.component = c;
		prob.parameters = parameters;
		for (List<String> row : data) {
			List<String> attributeValues = row.subList(0, row.size() - 1);
			Double val = Double.valueOf(row.get(row.size() - 1));
			if (csvFile.contains("obj1"))
				val *= -1;
			prob.scores.put(attributeValues, val);
			prob.scoreStats.addValue(val);
		}
		return prob;
	}
	

	@Test
	public void test() throws IOException {
		try (BufferedWriter fw = new BufferedWriter(new FileWriter("results-traditional.csv", true))) {
			for (File f : FileUtil.getFilesOfFolder(new File("testrsc"))) {
				if (!f.getName().endsWith(".csv"))
					continue;
				System.out.println(f.getName());
				String line = "";
				for (float share : new float[] { 0.05f, 0.5f }) {
					for (int seed = 0; seed < 20; seed++) {
						double score = conductAndReport(f.getName(), share);
						if (f.getName().contains("obj1"))
							score *= -1;
						if (line.isEmpty())
							line += "\t";
						else
							line += ", ";
						line += score;
						String lineInFile = f.getName() + "," + share + "," + seed + "," + score + "\n";
						System.out.println(lineInFile);
						fw.write(lineInFile);
						fw.flush();
					}
				}
				System.out.println(line);
			}
		}
	}

	private double conductAndReport(String filename, float shareOfVisibleData) throws IOException {
		Problem prob = getProblem("testrsc/" + filename);
		Pair<List<String>, Double> solution = getSolutionAndScoreForHASCO(prob, shareOfVisibleData);
		return solution.getY();
		// System.out.println(solution.getX() + ": " + solution.getY());
		// System.out.println(prob.scoreStats.getMin());
	}

	private Pair<List<String>, Double> getSolutionAndScoreForHASCO(Problem prob, float shareOfVisibleData) {
		Collection<Component> components = new ArrayList<>();
		components.add(prob.component);
		final TFDSearchSpaceUtilFactory searchSpaceFactory = new TFDSearchSpaceUtilFactory();
		final int maxEvaluations = Math.round(shareOfVisibleData * prob.scores.size());
		// System.out.println("Allowing maximum " + maxEvaluations + " evaluations.");
		final AtomicInteger evaluationCounter = new AtomicInteger(0);
		final AtomicDouble bestScore = new AtomicDouble(Double.MAX_VALUE);
		List<List<String>> bestFoundSolution = new ArrayList<>(); // dummy, only setting first value
		bestFoundSolution.add(null);

		final HASCOFD<List<String>> hascoalg = new HASCOFD<>(ci -> {
			List<String> vals = prob.parameters.stream().map(p -> ci.getParameterValues().get(p)).collect(Collectors.toList());
			return vals;
		}, n -> {

			/* get the random score of a completion */
			ComponentInstance inst = hasco.core.Util.getSolutionCompositionForNode(searchSpaceFactory, components, new Monom(), n);
			Monom finalState = hasco.core.Util.getFinalStateOfPlan(new Monom(), searchSpaceFactory.getPathToPlanConverter().getPlan(n.externalPath()));
			Map<Parameter, String> partialParametrization = Util.getParametrizations(finalState, components, true).get("solution");
			if (partialParametrization == null)
				return 0.0;

			/* get all samples that still match this */
			List<List<String>> configs = prob.scores.keySet().stream().filter(c -> {
				for (Parameter param : partialParametrization.keySet()) {
					int indexOfParam = prob.parameters.indexOf(param.getName());
					boolean coincidingParamValues = c.get(indexOfParam).equals(partialParametrization.get(param));
					if (!coincidingParamValues)
						return false;
				}
				return true;
			}).collect(Collectors.toList());
			Collections.shuffle(configs);
			if (configs.isEmpty())
				return Double.MAX_VALUE;

			/* draw 3 samples */
			DescriptiveStatistics stats = new DescriptiveStatistics();
			for (int i = 0; i < Math.min(configs.size(), 3); i++) {
				stats.addValue(prob.scores.get(configs.get(i)));
				int val = evaluationCounter.incrementAndGet();
				if (val >= maxEvaluations)
					return null; // this will eventually return a solution
			}

			/* return min */
			double min = stats.getMin();
			if (min < bestScore.get()) {
				bestScore.set(min);
				bestFoundSolution.set(0, configs.get(0));
			}
			return min;
		}, null, "iface", l -> prob.scores.containsKey(l) ? prob.scores.get(l) : Double.MAX_VALUE);
		hascoalg.addComponent(prob.component);
//		 new SimpleGraphVisualizationWindow<Node<TFDNode,Double>>(hascoalg).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());

		/* */
		int maxIterations = Math.round(prob.scores.size() * shareOfVisibleData);
		// System.out.println("Admitting " + Math.round(shareOfVisibleData * 100) + "% of the data, which are " + maxIterations + "/" + prob.scores.size() + " data points.");
		int i = 0;
		HASCO<List<String>, TFDNode, String, Double, ForwardDecompositionSolution>.HASCOSolutionIterator it = hascoalg.iterator();
		
		Recorder<Node<TFDNode, String>> rec = new Recorder<>();
        hascoalg.registerListener(rec);
      //  rec.setTooltipGenerator(new TFDTooltipGenerator<>());

		/* run algorithm */
		it.hasNext();
		it.next();
		
		System.out.println("OK");
		while (i >= 0);

		return new Pair<>(bestFoundSolution.get(0), bestScore.get());
		// while (it.hasNext()) {
		// Solution<ForwardDecompositionSolution,List<String>> solution = it.next();
		// System.out.println("Found solution " + solution.getSolution());
		// double score = (Double)it.getAnnotationsOfSolution(solution).get("f");
		// if (score == Double.MAX_VALUE)
		// continue;
		// if (score < bestFoundScore) {
		// bestFoundScore = score;
		// bestFoundSolution = solution.getSolution();
		// }
		//
		// i++;
		// if (i >= maxIterations)
		// break;
		// }
		// return new Pair<>(bestFoundSolution, bestFoundScore);
	}

}
