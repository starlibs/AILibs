package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.RadiusNeighborsClassifier;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.NearestNeighbors.OptionsFor_NearestNeighbors_algorithm;
    /*
        algorithm : {'auto', 'ball_tree', 'kd_tree', 'brute'}, optional
        Algorithm used to compute the nearest neighbors:

        - 'ball_tree' will use :class:`BallTree`
        - 'kd_tree' will use :class:`KDTree`
        - 'brute' will use a brute-force search.
        - 'auto' will attempt to decide the most appropriate algorithm
          based on the values passed to :meth:`fit` method.

        Note: fitting on sparse input will override the setting of
        this parameter, using brute force.


    */
    public class OptionsFor_RadiusNeighborsClassifier_algorithm extends OptionsFor_NearestNeighbors_algorithm {
        
    }
    
