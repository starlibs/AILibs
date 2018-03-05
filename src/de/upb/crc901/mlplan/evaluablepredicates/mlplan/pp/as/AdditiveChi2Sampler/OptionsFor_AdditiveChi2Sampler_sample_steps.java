
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.AdditiveChi2Sampler;
    /*
        sample_steps : int, optional
        Gives the number of (complex) sampling points.

    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_AdditiveChi2Sampler_sample_steps extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 1;
        }

        @Override
        protected int getSteps() {
            return -1;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
