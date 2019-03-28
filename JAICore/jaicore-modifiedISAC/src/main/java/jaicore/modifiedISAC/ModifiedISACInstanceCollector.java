package jaicore.modifiedISAC;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import DataManager.IInstanceCollector;
import jaicore.CustomDataTypes.Performance;
import jaicore.CustomDataTypes.ProblemInstance;
import jaicore.CustomDataTypes.Solution;
import jaicore.CustomDataTypes.Tuple;
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
	private ArrayList<ArrayList<Tuple<Solution<String>,Performance<Double>>>> collectedClassifierandPerformance;
	
	private int numberOfClassifier;
	private ArrayList<String> allClassifier = new ArrayList<String>();

	private ArrayList<ProblemInstance<Instance>> collectedInstances = new ArrayList<ProblemInstance<Instance>>();
	private static ArrayList<String> AtributesofTrainingsdata = new ArrayList<String>();

	/**
	 * @return Returns the attributes of the collected and processed Instances as well
	 * as their order
	 */
	public static ArrayList<String> getAtributesofTrainingsdata() {
		return AtributesofTrainingsdata;
	}
	
	public  ArrayList<ArrayList<Tuple<Solution<String>,Performance<Double>>>> getCollectedClassifierandPerformance() {
		return collectedClassifierandPerformance;
	}
	
	public int getNumberOfClassifier() {
		return numberOfClassifier;
	}
	
	public ArrayList<String> getAllClassifier() {
		return allClassifier;
	}
	
	public ModifiedISACInstanceCollector(Instances data, int startOfClassifierPerformanceValues, int endOfClassifierPerformanceValues) {
		
		collectedClassifierandPerformance = new ArrayList<ArrayList<Tuple<Solution<String>,Performance<Double>>>>();
		//TODO änder das 
		numberOfClassifier = (((endOfClassifierPerformanceValues+1)-(startOfClassifierPerformanceValues+1))+1);
	
		for(Instance i : data) {
			ArrayList<Tuple<Solution<String>,Performance<Double>>> pandc = new ArrayList<Tuple<Solution<String>,Performance<Double>>>();	
			for(int j = endOfClassifierPerformanceValues; j>=startOfClassifierPerformanceValues; j--) {
				Solution<String> classi = new Solution<String>(i.attribute(j).name());
				Performance<Double> perfo = new Performance<Double>(i.value(j));
				Tuple<Solution<String>, Performance<Double>> tup = new Tuple<Solution<String>,Performance<Double>>(classi,perfo);
				pandc.add(tup);
			}
			collectedClassifierandPerformance.add(pandc);
		}
		
		Instance inst = data.get(0);
		for(int i = endOfClassifierPerformanceValues;i>=startOfClassifierPerformanceValues;i--) {
			allClassifier.add(inst.attribute(i).name());
			data.deleteAttributeAt(i);
		}

		data.deleteAttributeAt(0);
		for(int i = 0; i<data.numAttributes();i++) {
			AtributesofTrainingsdata.add(data.attribute(i).toString());			
		}
		for (Instance i : data) {
			collectedInstances.add(new ProblemInstance<Instance>(i));
		}
	}
	
	private static Instances loadDefaultInstances() throws Exception {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("metaData_smallDataSets_computed.arff");
		DataSource source = new DataSource(inputStream);
		Instances data = source.getDataSet();
		return data;
	}

	/** This constructor is used if the default file should be used. Parts of the Instances
	 * have to be removed for the further computation. (The dataset-ID, Classifers with performance )
	 * @throws Exception
	 */
	public ModifiedISACInstanceCollector() throws Exception {		
		this(loadDefaultInstances(), 104, 125);
	}
	public void setNumberOfClassifier(int number) {
		this.numberOfClassifier = number;
	}
	@Override
	public List<ProblemInstance<Instance>> getProblemInstances() {
		return this.collectedInstances;
	}


}
