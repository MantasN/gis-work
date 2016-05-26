package gis.providers;

import org.geotools.data.FeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.opengis.feature.Feature;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AttributesProvider {

    public static Map<String, Object> getFeatureAttributes(Layer layer, Feature feature) {
        if (!(layer instanceof FeatureLayer)) {
            throw new IllegalArgumentException("layer must be an instance of FeatureLayer");
        }

        Map<String, Object> attributes = new HashMap<>();

        FeatureSource featureSource = layer.getFeatureSource();
        Collection<PropertyDescriptor> descriptors = featureSource.getSchema().getDescriptors();

        for (PropertyDescriptor desc : descriptors) {
            Name name = desc.getName();
            Object value = feature.getProperty(name).getValue();

            if (value != null) {
                attributes.put(name.toString(), value);
            } else {
                attributes.put(name.toString(), "null");
            }
        }

        return attributes;
    }

}
