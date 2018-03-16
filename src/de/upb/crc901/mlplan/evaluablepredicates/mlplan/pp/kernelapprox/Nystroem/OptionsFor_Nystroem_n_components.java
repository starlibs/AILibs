
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.kernelapprox.Nystroem;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_components : int
        Number of features to construct.
        How many data points will be used to construct the mapping.


    */
    public class OptionsFor_Nystroem_n_components extends NumericRangeOptionPredicate {
        
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
    
