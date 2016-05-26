package gis.providers;

import com.vividsolutions.jts.geom.Geometry;
import gis.models.SelectedFeatureResult;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.opengis.feature.Feature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;
import gis.models.FeatureWithProperties;
import gis.models.SelectedFeaturesResult;
import java.util.Collection;

public class SelectedFeaturesProvider {

    private static final FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(null);

    private MapContent mapContent;
    private Layer layer;
    private String geometricAttrName;

    public SelectedFeaturesProvider(MapContent mapContent, Layer layer) {
        if (!(layer instanceof FeatureLayer)) {
            throw new IllegalArgumentException("layer must be an instance of FeatureLayer");
        }

        this.mapContent = mapContent;
        this.layer = layer;

        GeometryDescriptor geomDesc = layer.getFeatureSource().getSchema().getGeometryDescriptor();
        geometricAttrName = geomDesc.getLocalName();
    }

    public SelectedFeaturesResult getSelectedFeaturesWithAttributes(BoundingBox bbox) throws Exception {
        SelectedFeaturesResult result = new SelectedFeaturesResult(layer);

        Filter filter = filterFactory.bbox(filterFactory.property(geometricAttrName), bbox);

        Query query = new Query(null, filter);
        query.setCoordinateSystemReproject(mapContent.getCoordinateReferenceSystem());

        FeatureSource featureSource = layer.getFeatureSource();

        Collection<PropertyDescriptor> descriptors = featureSource.getSchema().getDescriptors();
        FeatureCollection queryResult = featureSource.getFeatures(query);
        FeatureIterator iterator = queryResult.features();

        try {
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                FeatureWithProperties featureWithProperties = new FeatureWithProperties(feature);

                for (PropertyDescriptor desc : descriptors) {
                    Name name = desc.getName();
                    Object value = feature.getProperty(name).getValue();

                    if (value != null) {
                        featureWithProperties.addProperty(name.toString(), value);
                    } else {
                        featureWithProperties.addProperty(name.toString(), "null");
                    }
                }

                result.addFeature(featureWithProperties);
            }
        } finally {
            iterator.close();
        }

        return result;
    }

    public SelectedFeatureResult getSelectedFeature(BoundingBox bbox) throws Exception {
        Filter filter = filterFactory.bbox(filterFactory.property(geometricAttrName), bbox);

        Query query = new Query(null, filter);
        query.setCoordinateSystemReproject(mapContent.getCoordinateReferenceSystem());

        FeatureSource featureSource = layer.getFeatureSource();

        FeatureCollection queryResult = featureSource.getFeatures(query);
        FeatureIterator iterator = queryResult.features();

        try {
            if (iterator.hasNext())
                return new SelectedFeatureResult(iterator.next(), layer);
        } finally {
            iterator.close();
        }
        return new SelectedFeatureResult(null, layer);
    }

}
