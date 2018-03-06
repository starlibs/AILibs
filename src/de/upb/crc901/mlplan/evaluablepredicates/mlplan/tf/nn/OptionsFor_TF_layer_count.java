
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.tf.nn;
    /*
        sample_steps : int, optional
        Gives the number of (complex) sampling points.

    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_TF_layer_count extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 3;
        }

        @Override
        protected int getSteps() {
            return 2;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
