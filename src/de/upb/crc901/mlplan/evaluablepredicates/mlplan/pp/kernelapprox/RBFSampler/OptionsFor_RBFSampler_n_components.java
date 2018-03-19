
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.kernelapprox.RBFSampler;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_components : int
        Number of Monte Carlo samples per original feature.
        Equals the dimensionality of the computed feature space.


    */
    public class OptionsFor_RBFSampler_n_components extends NumericRangeOptionPredicate {
        
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

        @Override
        protected boolean isLinear() {
			return true;
		}
    }
    
