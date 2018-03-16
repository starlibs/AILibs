
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.kernelapprox.RBFSampler;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        gamma : float
        Parameter of RBF kernel: exp(-gamma * x^2)


    */
    public class OptionsFor_RBFSampler_gamma extends NumericRangeOptionPredicate {
        
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
            return false;
        }

        @Override
        protected boolean isLinear() {
			return true;
		}
    }
    
