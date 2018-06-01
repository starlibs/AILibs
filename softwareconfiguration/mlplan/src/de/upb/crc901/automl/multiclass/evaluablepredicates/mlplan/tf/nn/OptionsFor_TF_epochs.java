
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.tf.nn;
    /*
        sample_steps : int, optional
        Gives the number of (complex) sampling points.

    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_TF_epochs extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 10;
        }

        @Override
        protected double getMax() {
            return 500;
        }

        @Override
        protected int getSteps() {
            return 10;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
