package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jaicore.basic.sets.SetUtil;
import jaicore.ml.ranking.clusterbased.GroupBasedRanker;
import jaicore.ml.ranking.clusterbased.customdatatypes.Group;
import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import jaicore.ml.ranking.clusterbased.customdatatypes.RankingForGroup;
import weka.core.Instance;

/**
 * @author Helen
 *	ModifiedISAC handles the preparation of the data and the clustering of it as well as the
 * 	the search for a cluster for a new instance.
 */
public class ModifiedISAC extends GroupBasedRanker<double[], Instance, String, Double> {
	// Saves the position of the points in the original list to save their relation to the corresponding
	// instance.
	private HashMap<double[], Integer> positionOfInstance = new HashMap<double[], Integer>();
	// Saves the rankings for the found cluster in form of the cluster center and the ranking of Classifier by their name.
	private ArrayList<ClassifierRankingForGroup> rankings = new ArrayList<ClassifierRankingForGroup>();
	// Saves the found cluster
	private List<Group<double[], Instance>> foundCluster;
	// Saves the used normalizer
	private Normalizer norm;

	/* (non-Javadoc)
	 * @see jaicore.Ranker.Ranker#bulidRanker()
	 */
	@Override
	public void buildRanker() {
		try {
			ModifiedISACInstanceCollector collector = new ModifiedISACInstanceCollector();
			ArrayList<ProblemInstance<Instance>> collectedInstances = (ArrayList<ProblemInstance<Instance>>) collector
					.getProblemInstances();
			ArrayList<double[]> toClusterpoints = new ArrayList<>();

			this.norm = new Normalizer(collectedInstances);
			this.norm.setupnormalize();

			for (ProblemInstance<Instance> tmp : collectedInstances) {
				toClusterpoints.add(this.norm.normalize(tmp.getInstance().toDoubleArray()));
			}

			ModifiedISACGroupBuilder builder = new ModifiedISACGroupBuilder();
			builder.setPoints(toClusterpoints);

			int tmp = 0;
			for (ProblemInstance<Instance> i : collectedInstances) {
				this.positionOfInstance.put(i.getInstance().toDoubleArray(), tmp);
				tmp++;
			}

			this.foundCluster = builder.buildGroup(collectedInstances);
			this.constructRanking(collector);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** given the collector and the used Classifier it construct a ranking for the found classifer
	 * @param collector
	 */
	private void constructRanking(final ModifiedISACInstanceCollector collector) {
		for (Group<double[], Instance> c : this.foundCluster) {
			ArrayList<String> ranking = new ArrayList<>();
			int[] tmp = new int[collector.getNumberOfClassifier()];
			double[] clusterMean = new double[collector.getNumberOfClassifier()];
			for (ProblemInstance<Instance> prob : c.getInstances()) {
				int myIndex = 0;
				for (double[] d : this.positionOfInstance.keySet()) {
					if (Arrays.equals(d, prob.getInstance().toDoubleArray())) {
						myIndex = this.positionOfInstance.get(d);
					}
				}
				ArrayList<SetUtil.Pair<String, Double>> solutionsOfPoint = collector
						.getCollectedClassifierandPerformance().get(myIndex);
				for (int i = 0; i < solutionsOfPoint.size(); i++) {

					double perfo = solutionsOfPoint.get(i).getY();
					if (!Double.isNaN(perfo)) {

						clusterMean[i] += perfo;
						tmp[i]++;
					}
				}
			}
			for (int i = 0; i < clusterMean.length; i++) {
				clusterMean[i] = clusterMean[i] / tmp[i];
			}

			List<String> allClassifier = collector.getAllClassifier();
			HashMap<String, Double> remainingCandidiates = new HashMap<String, Double>();
			for (int i = 0; i < clusterMean.length; i++) {
				remainingCandidiates.put(allClassifier.get(i), clusterMean[i]);
			}

			while (!remainingCandidiates.isEmpty()) {
				double min = Double.MIN_VALUE;
				String classi = null;
				for (String str : remainingCandidiates.keySet()) {
					double candidate = remainingCandidiates.get(str);
					if(candidate> min) {
						classi = str;
						min = candidate;
					}
				}
				if(classi == null) {
					for(String str : remainingCandidiates.keySet()) {
						ranking.add(str);
					}
					remainingCandidiates.clear();
				}else {
					ranking.add(classi);
					remainingCandidiates.remove(classi);
				}

			}

			this.rankings.add(new ClassifierRankingForGroup(c.getId(), ranking));
		}
	}

	/* (non-Javadoc)
	 * @see jaicore.GroupBasedRanker.GroupBasedRanker#getRanking(java.lang.Object)
	 */
	@Override
	public RankingForGroup<double[], String> getRanking(final Instance prob) {
		RankingForGroup<double[], String> myRanking = null;
		try {
			double[] point = this.norm.normalize( prob.toDoubleArray());
			L1DistanceMetric dist = new L1DistanceMetric();
			double minDist = Double.MAX_VALUE;
			for (RankingForGroup<double[], String> rank : this.rankings) {
				double computedDist= dist.computeDistance(rank.getIdentifierForGroup().getIdentifier(), point);

				if (computedDist <= minDist) {
					myRanking = rank;
					minDist = computedDist;
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return myRanking;
	}

	/**
	 * @return
	 */
	public List<ClassifierRankingForGroup> getRankings() {
		return this.rankings;
	}
}
