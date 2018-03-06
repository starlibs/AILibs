
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.tf.nn;
    /*
        sample_steps : int, optional
        Gives the number of (complex) sampling points.

    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_TF_learning_rate extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.01f;
        }

        @Override
        protected double getMax() {
            return 0.5;
        }

        @Override
        protected int getSteps() {
            return 10;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
