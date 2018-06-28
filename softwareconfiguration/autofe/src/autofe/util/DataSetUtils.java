package autofe.util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import Catalano.Imaging.FastBitmap;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public final class DataSetUtils {
	private DataSetUtils() {
		// Utility class
	}

	// 1024 / 1024 / 1024: red / green / blue channel
	public static FastBitmap cifar10InstanceToBitmap(final Instance instance) {
		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		double[] imageValues = instance.toDoubleArray();
		for (int i = 0; i < 32; i++) {
			for (int j = 0; j < 32; j++) {
				int offset = (i + 1);
				int a = 255;
				int r = (int) imageValues[offset * j];
				int g = (int) imageValues[1024 + offset * j];
				int b = (int) imageValues[2048 + offset * j];
				int p = 0;
				p = p | (a << 24);
				p = p | (r << 16);
				p = p | (g << 8);
				p = p | b;
				image.setRGB(i, j, p);
			}
		}
		return new FastBitmap(image);
	}
	
	public static INDArray cifar10InstanceToMatrix(final Instance instance) {
		return Nd4j.create(instance.toDoubleArray());
	}
	
	public static Instances bitmapsToInstances(final List<FastBitmap> images, final Instances refInstances) {
		
		if(images == null || images.size() == 0)
			throw new IllegalArgumentException("Parameter 'images' must not be null or empty!");
		
		// Create attributes
		ArrayList<Attribute> attributes = new ArrayList<>();
		for(int i=0; i<images.iterator().next().getRGBData().length; i++) {
			attributes.add(new Attribute("val" + i));
		}
		attributes.add(refInstances.classAttribute());
		
		Instances result = new Instances("Instances", attributes, refInstances.size());
		result.setClassIndex(result.numAttributes() - 1);
		
		for(int i=0; i<images.size(); i++) {
			FastBitmap image = images.get(i);
			int[] rgbData = image.getRGBData();
			Instance inst = new DenseInstance(rgbData.length + 1);
			for(int j=0; j<rgbData.length; i++) {
				inst.setValue(j, rgbData[j]);
			}
			inst.setClassValue(refInstances.get(i).classValue());
		}
		
		return result;
	}
	
	public static Instances matricesToInstances(final List<INDArray> matrices, final Instances refInstances) {
		if(matrices == null || matrices.size() == 0)
			throw new IllegalArgumentException("Parameter 'matrices' must not be null or empty!");
		
		// Create attributes
		ArrayList<Attribute> attributes = new ArrayList<>();
		for(int i=0; i<matrices.get(0).length(); i++) {
			attributes.add(new Attribute("val" + i));
		}
		attributes.add(refInstances.classAttribute());
		
		Instances result = new Instances("Instances", attributes, refInstances.size());
		result.setClassIndex(result.numAttributes() - 1);
		
		for(int i=0; i<matrices.size(); i++) {

			// Initialize instance
			Instance inst = new DenseInstance(attributes.size());
			inst.setDataset(result);
			
			// Update instance entries
			INDArray matrix = matrices.get(i);
			for(int j=0; j< matrix.length(); j++) {
				inst.setValue(j, matrix.getDouble(j));
			}
			
			// Set class value
			inst.setClassValue(refInstances.get(i).classValue());
		}
		
		return result;

	}
	
//	private int getArrayDepth(final double[] array) {
//		Object object = array;
//		int depth = 0;
//		while(object instanceof double[]) {
//			depth++;
//			object = ((double[]) object)[0];
//		}
//		return depth;
//	}
}
