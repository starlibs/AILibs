package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.MiniBatchKMeans;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        compute_labels : boolean, default=True
        Compute label assignment and inertia for the complete dataset
        once the minibatch optimization has converged in fit.


    */
    public class OptionsFor_MiniBatchKMeans_compute_labels extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
