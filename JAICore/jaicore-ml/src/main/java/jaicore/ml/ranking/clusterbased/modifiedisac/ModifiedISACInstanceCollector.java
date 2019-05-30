package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jaicore.basic.sets.SetUtil;
import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import jaicore.ml.ranking.clusterbased.datamanager.IInstanceCollector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * @author Helen
 * This class should collect Instances in form of metafeatures form a file.
 */
public class ModifiedISACInstanceCollector implements IInstanceCollector<Instance> {

	/**
	 * The collected and processed Instances
	 */
	private ArrayList<ArrayList<SetUtil.Pair<String,Double>>> collectedClassifierandPerformance;

	private int numberOfClassifier;
	private ArrayList<String> allClassifier = new ArrayList<>();

	private ArrayList<ProblemInstance<Instance>> collectedInstances = new ArrayList<>();
	private static ArrayList<String> atributesofTrainingsdata = new ArrayList<>();

	/**
	 * @return Returns the attributes of the collected and processed Instances as well
	 * as their order
	 */
	public static List<String> getAtributesofTrainingsdata() {
		return atributesofTrainingsdata;
	}

	public List<ArrayList<SetUtil.Pair<String,Double>>> getCollectedClassifierandPerformance() {
		return this.collectedClassifierandPerformance;
	}

	public int getNumberOfClassifier() {
		return this.numberOfClassifier;
	}

	public List<String> getAllClassifier() {
		return this.allClassifier;
	}

	public ModifiedISACInstanceCollector(final Instances data, final int startOfClassifierPerformanceValues, final int endOfClassifierPerformanceValues) {

		this.collectedClassifierandPerformance = new ArrayList<>();
		this.numberOfClassifier = (((endOfClassifierPerformanceValues+1)-(startOfClassifierPerformanceValues+1))+1);

		for(Instance i : data) {
			ArrayList<SetUtil.Pair<String,Double>> pandc = new ArrayList<>();
			for(int j = endOfClassifierPerformanceValues; j>=startOfClassifierPerformanceValues; j--) {
				String classi = i.attribute(j).name();
				double perfo = i.value(j);
				SetUtil.Pair<String, Double> tup = new SetUtil.Pair<>(classi,perfo);
				pandc.add(tup);
			}
			this.collectedClassifierandPerformance.add(pandc);
		}

		Instance inst = data.get(0);
		for(int i = endOfClassifierPerformanceValues;i>=startOfClassifierPerformanceValues;i--) {
			this.allClassifier.add(inst.attribute(i).name());
			data.deleteAttributeAt(i);
		}

		data.deleteAttributeAt(0);
		for(int i = 0; i<data.numAttributes();i++) {
			atributesofTrainingsdata.add(data.attribute(i).toString());
		}
		for (Instance i : data) {
			this.collectedInstances.add(new ProblemInstance<Instance>(i));
		}
	}

	private static Instances loadDefaultInstances() throws Exception {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("metaData_smallDataSets_computed.arff");
		DataSource source = new DataSource(inputStream);
		return source.getDataSet();
	}

	/** This constructor is used if the default file should be used. Parts of the Instances
	 * have to be removed for the further computation. (The dataset-ID, Classifers with performance )
	 * @throws Exception
	 */
	public ModifiedISACInstanceCollector() throws Exception {
		this(loadDefaultInstances(), 104, 125);
	}
	public void setNumberOfClassifier(final int number) {
		this.numberOfClassifier = number;
	}
	@Override
	public List<ProblemInstance<Instance>> getProblemInstances() {
		return this.collectedInstances;
	}


}
