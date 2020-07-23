package ai.libs.jaicore.ml.pdm.dataset;


import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IObjectAttributeValue;


public class SensorTimeSeriesAttributeValue implements IObjectAttributeValue<SensorTimeSeries> {

   private final SensorTimeSeriesAttribute attribute;
   private final SensorTimeSeries value;


   public SensorTimeSeriesAttributeValue(SensorTimeSeriesAttribute attribute, SensorTimeSeries value) {
      super();
      this.attribute = attribute;
      this.value = value;
   }


   @Override
   public IAttribute getAttribute() {
      return attribute;
   }


   @Override
   public SensorTimeSeries getValue() {
      return value;
   }

}
