package gis.models;

import org.opengis.feature.Feature;

import java.util.HashMap;

public class FeatureWithProperties {
    private Feature feature;
    private HashMap<String, Object> properties;

    public FeatureWithProperties(Feature feature) {
        this.feature = feature;
        this.properties = new HashMap<>();
    }

    public Feature getFeature() {
        return feature;
    }

    public HashMap<String, Object> getProperties(){
        return properties;
    }

    public void addProperty(String key, Object value){
        properties.put(key, value);
    }

    @Override
    public String toString() {
        return feature.getIdentifier().getID();
    }
}
