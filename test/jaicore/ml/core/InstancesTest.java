package jaicore.ml.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.junit.Assert;
import org.junit.Test;

import jaicore.ml.WekaUtil;
import jaicore.ml.interfaces.LabeledInstances;
import weka.core.Instances;

public class InstancesTest {
	
	private static final File folder = new File("testrsc/ml/orig/");
	
	@Test
	public void wekaToJAICore() throws Exception {
		Instances inst = new Instances(new BufferedReader(new FileReader(folder + File.separator + "vowel.arff")));
		inst.setClassIndex(inst.numAttributes() - 1);
		jaicore.ml.interfaces.Instances inst2 = WekaUtil.toJAICoreInstances(inst);
		Assert.assertEquals(inst2.getNumberOfColumns(), inst.numAttributes());
		Assert.assertEquals(inst.size(), inst2.size());
		
		/* write JSON to file */
		try (FileWriter fw = new FileWriter(folder.getParent() + File.separator + "transformed/vowel_unlabeled.json")) {
			fw.write(inst2.toJson());
		}
	}
	
	@Test
	public void wekaToLabeledJAICore() throws Exception {
		Instances inst = new Instances(new BufferedReader(new FileReader(folder + File.separator + "vowel.arff")));
		inst.setClassIndex(inst.numAttributes() - 1);
		jaicore.ml.interfaces.LabeledInstances<String> inst2 = WekaUtil.toJAICoreLabeledInstances(inst);
		Assert.assertEquals(inst2.getNumberOfColumns() + 1, inst.numAttributes());
		Assert.assertEquals(inst.size(), inst2.size());
		
		try (FileWriter fw = new FileWriter(folder.getParent() + File.separator + "transformed/vowel_labeled.json")) {
			fw.write(inst2.toJson());
		}
	}
	
	@Test
	public void LabeledJAICoreToWeka() throws Exception {
		LabeledInstances<String> instances = new SimpleLabeledInstancesImpl();
		instances.addAllFromJson(new File(folder + File.separator + "vowel_labeled.json"));
		
		Instances wekaInstances = WekaUtil.fromJAICoreInstances(instances);
		Assert.assertEquals(instances.getNumberOfColumns() + 1, wekaInstances.numAttributes());
		Assert.assertEquals(instances.size(), wekaInstances.size());
	}
	
	@Test
	public void UnlabeledJAICoreToWeka() throws Exception {
		jaicore.ml.interfaces.Instances instances = new SimpleInstancesImpl();
		instances.addAllFromJson(new File(folder + File.separator + "vowel_unlabeled.json"));
		
		Instances wekaInstances = WekaUtil.fromJAICoreInstances(instances);
		Assert.assertEquals(instances.getNumberOfColumns(), wekaInstances.numAttributes());
		Assert.assertEquals(instances.size(), wekaInstances.size());
	}
}