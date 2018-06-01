
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.BernoulliRBM;
    /*
        learning_rate : float, optional
        The learning rate for weight updates. It is *highly* recommended
        to tune this hyper-parameter. Reasonable values are in the
        10**[0., -3.] range.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_BernoulliRBM_learning_rate extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.01;
        }

        @Override
        protected double getMax() {
            return 1;
        }

        @Override
        protected int getSteps() {
            return 5;
        }
        protected boolean isLinear() {
        	return false;
        }
        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
