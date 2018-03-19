
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.kernelapprox.SkewedChi2Sampler;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        skewedness : float
        "skewedness" parameter of the kernel. Needs to be cross-validated.


    */
    public class OptionsFor_SkewedChi2Sampler_skewedness extends NumericRangeOptionPredicate {
        
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
    
