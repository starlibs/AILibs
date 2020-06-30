package ai.libs.jaicore.ml.pdm.dataset;


import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;


public class SensorTimeSeries {

   private List<Pair<Integer, Double>> timestepValuePairs;


   public SensorTimeSeries() {
      timestepValuePairs = new ArrayList<>();
   }


   /**
    * Adds a timestep-value pair to the this time series. It is assumed that the given timestep is
    * larger than any other before, so that at the end it holds: t_i < t_j for i < j.
    * 
    * @param timestep The timestep for which a value will be added
    * @param value The value of the timestep
    */
   public void addValue(int timestep, double value) {
      timestepValuePairs.add(new Pair<>(timestep, value));
   }


   /**
    * Returns the value of the given timestep if one exists, null otherwise.
    * 
    * @param timestep The timestep to get the value for
    * @return The value of the given timestep if one exists, otherwise null.
    */
   public Double getValueOrNull(int timestep) {
      for (int i = 0; i < timestepValuePairs.size(); i++) {
         if (timestepValuePairs.get(i).getX().equals(timestep)) {
            return timestepValuePairs.get(i).getY();
         }
      }
      return null;
   }


   /**
    * Returns a part of this time series starting at the given {@code fromTimestep} and ending at
    * the given {@code toTimestep} excluding.
    * 
    * @param fromTimestep The starting point of the window
    * @param toTimestep The ending point of the window (exclusive)
    * @return A window of this {@link SensorTimeSeries}
    */
   public SensorTimeSeries getWindowedTimeSeries(int fromTimestep, int toTimestep) {
      SensorTimeSeries newSensorTimeSeries = new SensorTimeSeries();
      for (int t = 0; t < timestepValuePairs.size(); t++) {
         if (fromTimestep <= timestepValuePairs.get(t).getX() && timestepValuePairs.get(t).getX() < toTimestep) {
            newSensorTimeSeries.addValue(timestepValuePairs.get(t).getX(), timestepValuePairs.get(t).getY());
         }
      }
      return newSensorTimeSeries;
   }


   public int getLength() {
      return timestepValuePairs.get(timestepValuePairs.size() - 1).getX();
   }


   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((timestepValuePairs == null) ? 0 : timestepValuePairs.hashCode());
      return result;
   }


   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      SensorTimeSeries other = (SensorTimeSeries) obj;
      if (timestepValuePairs == null) {
         if (other.timestepValuePairs != null) {
            return false;
         }
      } else if (!timestepValuePairs.equals(other.timestepValuePairs)) {
         return false;
      }
      return true;
   }


   @Override
   public String toString() {
      return "SensorTimeSeries [timestepValuePairs=" + timestepValuePairs + "]";
   }

}
