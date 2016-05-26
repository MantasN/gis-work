package gis.utils;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeFileManager {

    private static final String RESULT_PATH = "C:\\Mantas\\GIS\\result_data\\";

    public static void writeFeaturesToShapeFile(List<SimpleFeature> features, String fileName) throws Exception {
        if (features.size() > 0) {
            SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
            SimpleFeature feature = features.get(0);

            typeBuilder.setName("Location");
            typeBuilder.setCRS(feature.getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem());
            typeBuilder.add("the_geom", ((Geometry) feature.getAttribute("the_geom")).getClass());

            final SimpleFeatureType TYPE = typeBuilder.buildFeatureType();

            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

            Map<String, Serializable> params = new HashMap<>();
            params.put("url", new File(RESULT_PATH + fileName).toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);

            ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema(TYPE);

            String typeName = newDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

            if (featureSource instanceof SimpleFeatureStore) {
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
                featureStore.addFeatures(collection);
            }
        }
    }

    public static void writeGeometryToShapeFile(Geometry geometry, CoordinateReferenceSystem crs, String fileName) throws Exception {

        if (geometry != null && !geometry.isEmpty()) {
            SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();

            typeBuilder.setName("Location");
            typeBuilder.setCRS(crs);
            typeBuilder.add("the_geom", geometry.getClass());

            final SimpleFeatureType TYPE = typeBuilder.buildFeatureType();

            List<SimpleFeature> simpleFeatureList = new ArrayList<>();
            SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(TYPE);
            simpleFeatureBuilder.add(geometry);
            SimpleFeature simpleFeature = simpleFeatureBuilder.buildFeature(null);
            simpleFeatureList.add(simpleFeature);

            ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

            Map<String, Serializable> params = new HashMap<>();
            params.put("url", new File(RESULT_PATH + fileName).toURI().toURL());
            params.put("create spatial index", Boolean.TRUE);

            ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
            newDataStore.createSchema(TYPE);

            String typeName = newDataStore.getTypeNames()[0];
            SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

            if (featureSource instanceof SimpleFeatureStore) {
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, simpleFeatureList);
                featureStore.addFeatures(collection);
            }
        }
    }

    public static File getShapeFile(String fileName){
        return new File(RESULT_PATH + fileName);
    }
}
