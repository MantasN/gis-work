package gis.utils;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.ArrayList;
import java.util.List;

public class FeaturesTypeConverter {
    public static List<SimpleFeature> multiPolygonFeaturesToPolygonFeatures(List<SimpleFeature> multiPolygonFeatures) throws Exception {
        List<SimpleFeature> polygonFeaturesList = new ArrayList<>();

        if (multiPolygonFeatures.size() > 0) {
            SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();

            typeBuilder.setName("Location");
            typeBuilder.setCRS(multiPolygonFeatures.get(0).getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem());
            typeBuilder.add("the_geom", Polygon.class);

            final SimpleFeatureType TYPE = typeBuilder.buildFeatureType();

            for (SimpleFeature feature : multiPolygonFeatures) {
                MultiPolygon multiPolygon = (MultiPolygon) feature.getAttribute("the_geom");

                for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                    SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(TYPE);
                    simpleFeatureBuilder.add(multiPolygon.getGeometryN(i));
                    polygonFeaturesList.add(simpleFeatureBuilder.buildFeature(null));
                }

            }
        }

        return polygonFeaturesList;
    }
}
