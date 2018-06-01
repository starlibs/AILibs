
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.AdditiveChi2Sampler;
    /*
        sample_steps : int, optional
        Gives the number of (complex) sampling points.

    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_AdditiveChi2Sampler_sample_steps extends NumericRangeOptionPredicate {
        
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
    
