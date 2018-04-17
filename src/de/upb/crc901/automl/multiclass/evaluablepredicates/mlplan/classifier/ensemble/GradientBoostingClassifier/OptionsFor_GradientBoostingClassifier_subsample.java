
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.ensemble.GradientBoostingClassifier;
    /*
        subsample : float, optional (default=1.0)
        The fraction of samples to be used for fitting the individual base
        learners. If smaller than 1.0 this results in Stochastic Gradient
        Boosting. `subsample` interacts with the parameter `n_estimators`.
        Choosing `subsample < 1.0` leads to a reduction of variance
        and an increase in bias.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_GradientBoostingClassifier_subsample extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.1;
        }

        @Override
        protected double getMax() {
            return 10;
        }

        @Override
        protected int getSteps() {
            return 10;
        }
        
        protected boolean isLinear() {
        	return false;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
