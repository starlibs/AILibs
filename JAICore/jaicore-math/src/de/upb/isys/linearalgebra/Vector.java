package de.upb.isys.linearalgebra;


/**
 * This interface defines all methods, which are required by all vector implementations.
 * 
 * @author Alexander Hetzer
 */
public interface Vector {

   /**
    * Adds the given double array interpreted as a vector to this vector.
    * 
    * @param vectorAsArray The vector to be added as array.
    */
   public void addVector(double[] vectorAsArray);


   /**
    * Adds the given double array interpreted as a vector to a copy of this vector and returns the
    * result of the operation.
    * 
    * @param vectorAsArray The vector to be added as an array.
    * @return The result vector of this operation.
    */
   public Vector addVectorToCopy(double[] vectorAsArray);


   /**
    * Subtracts the given double array interpreted as a vector from this vector.
    * 
    * @param vectorAsArray The vector to be subtracted as an array.
    */
   public void subtractVector(double[] vectorAsArray);


   /**
    * Subtracts the given double array interpreted as a vector from a copy of this vector and
    * returns the result of this operation.
    * 
    * @param vectorAsArray The vector to be subtracted as an array.
    * @return The result vector of this operation.
    */
   public Vector subtractVectorFromCopy(double[] vectorAsArray);


   /**
    * Multiplies this vector with the given double array interpreted as a pairwise vector and stores
    * the result in this vector.
    * 
    * @param vectorAsArray The vector to be multiplied with.
    */
   public void multiplyByVectorPairwise(double[] vectorAsArray);


   /**
    * Multiplies a copy of this vector with the given double array interpreted as a pairwise vector
    * and returns the result of the operation.
    * 
    * @param vectorAsArray The vector to be multiplied with.
    * @return The result vector of this operation.
    */
   public Vector multiplyByVectorPairwiseToCopy(double[] vectorAsArray);


   /**
    * Divides this vector by the given double array interpreted as a pairwise vector and stores the
    * result in this vector.
    * 
    * @param vectorAsArray The vector to be divided by.
    */
   public void divideByVectorPairwise(double[] vectorAsArray);


   /**
    * Divides a copy of this vector by the given double array interpreted as a pairwise vector and
    * returns the result of this operation.
    * 
    * @param vectorAsArray The operand vector as a double array.
    * @return The result vector of this operation.
    */
   public Vector divideByVectorPairwiseToCopy(double[] vectorAsArray);


   /**
    * Computes the dot product of the given vector and this vector.
    * 
    * @param vectorAsArray The vector to compute dot product with.
    * @return The dot product of the given and this vector.
    */
   public double dotProduct(double[] vectorAsArray);


   /**
    * Returns the number of dimensions of this vector.
    * 
    * @return The number of dimensions of this vector.
    */
   public int length();


   /**
    * Returns the value at the given index of this vector.
    * 
    * @param index The index of the value to look for.
    * @return The value at the given index.
    */
   public double getValue(int index);


   /**
    * Sets the value at the given index of this vector to the given value.
    * 
    * @param index The index to set the value at.
    * @param value The value to be set at the specified index.
    */
   public void setValue(int index, double value);


   /**
    * Adds the given constant to this vector.
    * 
    * @param constant The constant to be added.
    */
   public void addConstant(double constant);


   /**
    * Adds the given constant to a copy of this vector and returns the result of this operation.
    * 
    * @param constant The constant to be added.
    * @return The result vector of this operation.
    */
   public Vector addConstantToCopy(double constant);


   /**
    * Adds the given vector to this vector. The result is stored in this vector.
    * 
    * @param vector The vector to be added to this vector.
    */
   public void addVector(Vector vector);


   /**
    * Adds the given vector to a copy of this vector and returns the result of the operation.
    * 
    * @param vector The vector to be added to this vector.
    * @return The result vector of this operation.
    */
   public Vector addVectorToCopy(Vector vector);


   /**
    * Subtracts the given constant from this vector.
    * 
    * @param constant The constant to be subtracted.
    */
   public void subtractConstant(double constant);


   /**
    * Subtracts the given constant from a copy of this vector and returns the result of the
    * operation.
    * 
    * @param constant The constant to be subtracted.
    * @return The result vector of this operation.
    */
   public Vector subtractConstantFromCopy(double constant);


   /**
    * Subtracts the given vector from this vector. The result is stored in this vector.
    * 
    * @param vector The vector to subtract from this vector.
    */
   public void subtractVector(Vector vector);


   /**
    * Subtracts the given vector from a copy of this vector and returns the result of this
    * operation.
    * 
    * @param vector The vector to subtract from this vector.
    * @return The result vector of this operation.
    */
   public Vector subtractVectorFromCopy(Vector vector);


   /**
    * Multiplies this vector with the given vector pairwisely and stores the result in this vector.
    * 
    * @param vector The vector to be multiplied with.
    */
   public void multiplyByVectorPairwise(Vector vector);


   /**
    * Multiplies a copy of this vector with the given vector pairwisely and returns the result of
    * this operation.
    * 
    * @param vector The vector to be multiplied with.
    * @return The result vector of this operation.
    */
   public Vector multiplyByVectorPairwiseToCopy(Vector vector);


   /**
    * Multiplies this vector with the given constant.
    * 
    * @param constant The constant to multiply this vector with.
    */
   public void multiplyByConstant(double constant);


   /**
    * Multiplies a copy of this vector with the given constant and returns the result of this
    * operation.
    * 
    * @param constant The constant to multiply this vector with.
    * @return The result vector of this operation.
    */
   public Vector multiplyByConstantToCopy(double constant);


   /**
    * Divides this vector by the given pairwise vector and stores the result in this vector.
    * 
    * @param vector The vector to be divided by.
    */
   public void divideByVectorPairwise(Vector vector);


   /**
    * Divides a copy of this vector by the given pairwise vector and returns the result of this
    * operation.
    * 
    * @param vector The vector to be divided by.
    * @return The result vector of this operation.
    */
   public Vector divideByVectorPairwiseToCopy(Vector vector);


   /**
    * Divides this vector by the given constant.
    * 
    * @param constant The constant to divide this vector by.
    */
   public void divideByConstant(double constant);


   /**
    * Divides a copy of this vector by the given constant and returns the result of this operation.
    * 
    * @param constant The constant to divide this vector by.
    * @return The result vector of this operation.
    */
   public Vector divideByConstantToCopy(double constant);


   /**
    * Computes the dot product of the given vector and this vector.
    * 
    * @param vector The vector to compute dot product with.
    * @return The dot product of the given and this vector.
    */
   public double dotProduct(Vector vector);


   /**
    * Replaces the current values of the vectors with the square roots of the elements.
    * 
    */
   public void squareRoot();


   /**
    * Takes the square root of each element of the vector and creates a new vector with these
    * elements.
    * 
    * @return The vector consisting of the square rooted entries of this vector.
    */
   public Vector squareRootToCopy();


   /**
    * Checks if this vector is sparse.
    * 
    * @return {@code true}, if this vector is sparse, {@code false} if not.
    */
   public boolean isSparse();


   /**
    * Returns this vector as a double array.
    * 
    * @return The current vector as a double array.
    */
   public double[] asArray();


   /**
    * Returns this vector as {@link DenseDoubleVector}.
    * 
    * @return The current vector as a dense vector.
    */
   public DenseDoubleVector toDenseVector();


   /**
    * Returns this vector as {@link SparseDoubleVector}.
    * 
    * @return The current vector as a sparse vector.
    */
   public SparseDoubleVector toSparseVector();


   /**
    * Creates a deep copy of this vector.
    * 
    * @return A deep copy of this vector.
    */
   public Vector duplicate();


   /**
    * Normalizes this vector to the unit interval.
    */
   public void normalize();


   /**
    * Increments the value at the given index by the given amount.
    * 
    * @param index The index of the value to be incremented.
    * @param amount The amount to add to the value at the given index.
    */
   public void incrementValueAt(int index, double amount);


   /**
    * Returns the sum over the values of the dimensions of this vector.
    * 
    * @return The sum over the values of the dimensions.
    */
   public double sum();


   /**
    * Computes the mean of this vector.
    * 
    * @return The mean of this vector.
    */
   public double mean();


   /**
    * Returns the standard deviation over the values in the vector.
    * 
    * @return The standard deviation over the values in the vector.
    */
   public double standardDeviation();


   /**
    * Sets all dimensions of this vector to {@code 0}.
    * 
    */
   public void zeroAllDimensions();


   /**
    * Fills this vector with random values from the unit interval.
    */
   public void fillRandomly();


   /**
    * Returns the Euclidean norm (length) of this vector.
    * 
    * @return The Euclidean norm of this vector.
    */
   public double euclideanNorm();


   /**
    * Returns the average of all values contained in this vector.
    * 
    * @return The average of the single values.
    */
   public Double average();


   public static Vector merge(Vector v1, Vector v2) {
      DenseDoubleVector resultingVector = new DenseDoubleVector(v1.length() + v2.length());
      for (int i = 0; i < v1.length(); i++) {
         resultingVector.setValue(i, v1.getValue(i));
      }
      for (int i = 0; i < v2.length(); i++) {
         resultingVector.setValue(v1.length() + i, v2.getValue(i));
      }
      return resultingVector;
   }

}

