package gis.models;

import org.geotools.map.Layer;

import java.util.*;

public class SelectedFeaturesResult {

    private Layer layer;
    private Map<String, FeatureWithProperties> selectFeaturesResultList;

    public SelectedFeaturesResult(Layer layer) {
        this.layer = layer;
        this.selectFeaturesResultList = new HashMap<>();
    }

    public Layer getLayer(){
        return layer;
    }

    public List<FeatureWithProperties> getSelectedFeatures(){
        Collection<FeatureWithProperties> collection = selectFeaturesResultList.values();
        return new ArrayList<>(collection);
    }

    public void addFeature(FeatureWithProperties featureWithProperties){
        selectFeaturesResultList.put(featureWithProperties.getFeature().getIdentifier().getID(), featureWithProperties);
    }

    public static void merge(SelectedFeaturesResult result, SelectedFeaturesResult result2){
        if(result.layer.equals(result2.layer)){
            result2.getSelectedFeatures().forEach(result::addFeature);
        }
    }
}
