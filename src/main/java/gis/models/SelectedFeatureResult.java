package gis.models;

import org.geotools.map.Layer;
import org.opengis.feature.Feature;

public class SelectedFeatureResult {
    private Feature feature;
    private Layer layer;

    public SelectedFeatureResult(Feature feature, Layer layer) {
        this.feature = feature;
        this.layer = layer;
    }

    public Feature getFeature() {
        return feature;
    }

    public Layer getLayer() {
        return layer;
    }
}
