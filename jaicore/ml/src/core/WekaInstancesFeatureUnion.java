package jaicore.ml.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaInstancesFeatureUnion {

  public Instances merge(final Instances dataA, final Instances dataB) {
    if (dataA == null || dataB == null) {
      throw new IllegalArgumentException("Instances objects must not be null.");
    }

    List<Instances> datasetList = new LinkedList<>();
    datasetList.add(dataA);
    datasetList.add(dataB);

    return this.merge(datasetList);
  }

  public Instances merge(final Collection<Instances> data) {
    if (data.size() < 1) {
      throw new IllegalArgumentException("Merge cannot be invoked with empty collection of Instances");
    } else if (data.size() == 1) {
      return data.iterator().next();
    }

    boolean allEqualSize = true;
    Iterator<Instances> dataIt = data.iterator();
    Instances currentInstances = dataIt.next();
    while (dataIt.hasNext()) {
      Instances nextInstances = dataIt.next();
      if (currentInstances.size() != nextInstances.size()) {
        allEqualSize = false;
        break;
      }
      currentInstances = nextInstances;
    }

    if (!allEqualSize) {
      throw new IllegalArgumentException("The sizes of the provided Instances objects are not equal, Instance should only differ in the features not in the instances itself.");
    }

    // First of all merge the lists of attributes to construct the meta data of the feature merged
    // dataset.
    ArrayList<Attribute> mergedAttributeList = new ArrayList<>();
    Map<Attribute, Attribute> attributeMap = new HashMap<>();

    Attribute classAttribute = null;
    String relationName = null;
    Integer size = null;

    int ns = 0;
    for (Instances dataset : data) {
      if (classAttribute == null) {
        classAttribute = dataset.classAttribute().copy(ns + "-" + dataset.classAttribute().name());
        attributeMap.put(dataset.classAttribute(), classAttribute);
      }
      if (relationName == null) {
        relationName = dataset.relationName();
      }
      if (size == null) {
        size = dataset.size();
      }

      for (int i = 0; i < dataset.numAttributes(); i++) {
        if (i != dataset.classIndex()) {
          Attribute copiedAttribute = dataset.attribute(i).copy(ns + "-" + dataset.attribute(i).name());
          mergedAttributeList.add(copiedAttribute);
          attributeMap.put(dataset.attribute(i), copiedAttribute);
        }
      }
      ns++;
    }
    mergedAttributeList.add(classAttribute);

    Instances mergedInstances = new Instances("FeatureUnionInstances-" + relationName, mergedAttributeList, size);
    mergedInstances.setClassIndex(mergedInstances.numAttributes() - 1);

    for (int i = 0; i < size; i++) {
      Instance iNew = new DenseInstance(mergedAttributeList.size());
      iNew.setDataset(mergedInstances);

      // copy attribute values from original instance objects
      for (Instances dataset : data) {
        Instance iDataset = dataset.get(i);
        for (int j = 0; j < dataset.numAttributes(); j++) {
          Attribute originalKey = null;
          for (Attribute key : attributeMap.keySet()) {
            if (key == iDataset.attribute(j)) {
              originalKey = key;
            }
          }
          if (originalKey != null) {
            iNew.setValue(attributeMap.get(dataset.attribute(j)), iDataset.value(dataset.attribute(j)));
          }
        }
      }
      mergedInstances.add(iNew);
    }

    return mergedInstances;
  }

}
