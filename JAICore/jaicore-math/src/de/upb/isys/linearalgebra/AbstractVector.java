package de.upb.isys.linearalgebra;

/**
 * An abstract vector class, implementing several common methods for different vector implementations. All vector implementations should subclass this class.
 * 
 * @author Alexander Hetzer
 */
public abstract class AbstractVector implements Vector {

	@Override
	public void squareRoot() {
		for (int i = 0; i < length(); i++) {
			setValue(i, Math.sqrt(getValue(i)));
		}

	}

	@Override
	public Vector squareRootToCopy() {
		Vector copy = this.duplicate();
		for (int i = 0; i < length(); i++) {
			copy.setValue(i, Math.sqrt(getValue(i)));
		}
		return copy;
	}

	@Override
	public void incrementValueAt(int index, double amount) {
		setValue(index, amount + getValue(index));
	}

	@Override
	public double sum() {
		double sum = 0;
		for (int i = 0; i < length(); i++) {
			sum += getValue(i);
		}
		return sum;
	}

	@Override
	public double mean() {
		return sum() / length();
	}

	@Override
	public double standardDeviation() {
		double mean = mean();
		double std = 0.0;
		for (int i = 0; i < length(); i++) {
			std += Math.pow(mean - getValue(i), 2);
		}
		std = std / length();
		return Math.sqrt(std);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(");
		sb.append(getValue(0));
		for (int i = 1; i < length(); i++) {
			sb.append(",").append(getValue(i));
		}
		sb.append(")");
		return sb.toString();
	}

	protected double [] kroneckerProductInternal(double[] vectorAsArray) {
		double [] kroneckerProduct = new double [ this.length() * vectorAsArray.length];
		int counter = 0;
		for (int i = 0; i < this.length(); i++) {
			for (int j = 0; j < vectorAsArray.length; j++) {
				kroneckerProduct [counter++] =  this.getValue(i) * vectorAsArray[j];
			}
		}
		return kroneckerProduct;
	}
	
	
	@Override
	public void zeroAllDimensions() {
		for (int i = 0; i < length(); i++) {
			setValue(i, 0);
		}
	}

	@Override
	public double euclideanNorm() {
		return Math.sqrt(this.dotProduct(this));
	}

	@Override
	public Vector addVectorToCopy(double[] vectorAsArray) {
		Vector vector = duplicate();
		vector.addVector(vectorAsArray);
		return vector;
	}

	@Override
	public Vector subtractVectorFromCopy(double[] vectorAsArray) {
		Vector vector = duplicate();
		vector.subtractVector(vectorAsArray);
		return vector;
	}

	@Override
	public Vector multiplyByVectorPairwiseToCopy(double[] vectorAsArray) {
		Vector vector = duplicate();
		vector.multiplyByVectorPairwise(vectorAsArray);
		return vector;
	}

	@Override
	public Vector divideByVectorPairwiseToCopy(double[] vectorAsArray) {
		Vector vector = duplicate();
		vector.divideByVectorPairwise(vectorAsArray);
		return vector;
	}

	@Override
	public Vector addConstantToCopy(double constant) {
		Vector vector = duplicate();
		vector.addConstant(constant);
		return vector;
	}

	@Override
	public Vector addVectorToCopy(Vector vector) {
		Vector vectorCopy = duplicate();
		vectorCopy.addVector(vector);
		return vectorCopy;
	}

	@Override
	public Vector subtractConstantFromCopy(double constant) {
		Vector vectorCopy = duplicate();
		vectorCopy.subtractConstant(constant);
		return vectorCopy;
	}

	@Override
	public Vector subtractVectorFromCopy(Vector vector) {
		Vector vectorCopy = duplicate();
		vectorCopy.subtractVector(vector);
		return vectorCopy;
	}

	@Override
	public Vector multiplyByVectorPairwiseToCopy(Vector vector) {
		Vector vectorCopy = duplicate();
		vectorCopy.multiplyByVectorPairwise(vector);
		return vectorCopy;
	}

	@Override
	public Vector multiplyByConstantToCopy(double constant) {
		Vector vectorCopy = duplicate();
		vectorCopy.multiplyByConstant(constant);
		return vectorCopy;
	}

	@Override
	public Vector divideByVectorPairwiseToCopy(Vector vector) {
		Vector vectorCopy = duplicate();
		vectorCopy.divideByVectorPairwise(vector);
		return vectorCopy;
	}

	@Override
	public Vector divideByConstantToCopy(double constant) {
		Vector vectorCopy = duplicate();
		vectorCopy.divideByConstant(constant);
		return vectorCopy;
	}

	@Override
	public Double average() {
		double basisAverage = 0;
		for (int i = 0; i < this.length(); i++) {
			basisAverage += this.getValue(i);
		}
		basisAverage = basisAverage / this.length();
		return basisAverage;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractVector) {
			AbstractVector vector = (AbstractVector) obj;
			for (int i = 0; i < vector.length(); i++) {
				if (Math.abs(vector.getValue(i) - this.getValue(i)) > 0.00000000000000001) {
					return false;
				}
			}

			return true;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		int hashCode = 1;
		for (int i = 0; i < length(); i++) {
			hashCode = 31 * hashCode + (Double.valueOf(getValue(i)).hashCode());
		}
		return hashCode;
	}

}
