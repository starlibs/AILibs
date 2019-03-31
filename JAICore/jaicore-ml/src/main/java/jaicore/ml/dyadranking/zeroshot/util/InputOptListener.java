package jaicore.ml.dyadranking.zeroshot.util;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

public class InputOptListener {
	
	private int[] indicesToWatch;
	
	private List<INDArray> inputList;
	
	private List<Double> outputList;
	
	public InputOptListener(int[] indicesToWatch) {
		this.indicesToWatch = indicesToWatch;
		this.inputList = new ArrayList<>();
		this.outputList = new ArrayList<>();
	}
	
	public void reportOptimizationStep(INDArray plNetInput, double plNetOutput) {
		INDArray inpToAdd = plNetInput.getColumns(indicesToWatch);
		
		inputList.add(inpToAdd);
		outputList.add(plNetOutput);
	}
	
	public List<INDArray> getInputList() {
		return inputList;
	}
	
	public List<Double> getOutputList() {
		return outputList;
	}
	
}
