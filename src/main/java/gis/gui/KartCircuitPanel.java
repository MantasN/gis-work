package gis.gui;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import gis.models.SelectedFeaturesResult;
import gis.utils.FeaturesTypeConverter;
import gis.utils.ShapeFileManager;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.process.vector.ClipProcess;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class KartCircuitPanel extends JFrame {

    private JLabel areaLabel;
    private JTextField areaInput;
    private JLabel roadLabel;
    private JTextField roadInput;
    private JLabel forestLabel;
    private JTextField forestInput;
    private JLabel buildingsLabel;
    private JTextField buildingsInput;
    private JButton kartCircuitButton;

    private FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(null);
    private GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

    private SelectedFeaturesResult selectedFeaturesResult;
    private List<Layer> layers;
    private MapContent mapContent;

    public KartCircuitPanel(MapContent mapContent) {
        super("Kart circuit place search");

        this.mapContent = mapContent;

        setLayout(new GridLayout(5,2));

        areaLabel = new JLabel("Teritorija ne mažesnė nei x kv. m. ploto: ");
        areaInput = new JTextField("100");
        roadLabel = new JLabel("Teritorija ne toliau, nei y m. nuo kelio: ");
        roadInput = new JTextField("100");
        forestLabel = new JLabel("Sodai ir miškai toliau, nei z m. nuo teritorijos: ");
        forestInput = new JTextField("100");
        buildingsLabel = new JLabel("Pastatų plotas teritorijoje užima ne daugiau, kaip k proc. teritorijos ploto: ");
        buildingsInput = new JTextField("10");

        kartCircuitButton = new JButton("Rasti vietą kartingų trasai");
        kartCircuitButton.addActionListener(x -> findKartCircuitButtonPressed());

        add(areaLabel);
        add(areaInput);
        add(roadLabel);
        add(roadInput);
        add(forestLabel);
        add(forestInput);
        add(buildingsLabel);
        add(buildingsInput);
        add(new JLabel());
        add(kartCircuitButton);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void populateSelectedFeatures(List<Layer> layers, SelectedFeaturesResult selectedFeaturesResult) {
        this.selectedFeaturesResult = selectedFeaturesResult;
        this.layers = layers;
    }

    private void findKartCircuitButtonPressed() {
        int minSurfaceArea = Integer.valueOf(areaInput.getText());
        int maxDistanceToRoad = Integer.valueOf(roadInput.getText());
        int minDistanceToGardensAndForest = Integer.valueOf(forestInput.getText());
        int maxRatioWithBuildings = Integer.valueOf(buildingsInput.getText());

        if (selectedFeaturesResult != null) {
            Optional<Layer> roadsLayer = getLayer("KELIAI_3");
            Optional<Layer> areaLayer = getLayer("PLOTAI_3");
            Optional<Layer> surfaceLayer = getLayer("PAVIRS_LT_P_3");
            Optional<Layer> buildingsLayer = getLayer("PASTAT_P_3");

            if (roadsLayer.isPresent() && areaLayer.isPresent() && surfaceLayer.isPresent() && buildingsLayer.isPresent()) {
                try {
                    // selected geometry
                    Geometry selectedGeometry = geometryFactory.buildGeometry(
                            selectedFeaturesResult.getSelectedFeatures()
                                    .stream()
                                    .map(fa -> ((SimpleFeature) fa.getFeature()).getAttribute(getLayerGeometricAttrName(selectedFeaturesResult.getLayer())))
                                    .collect(Collectors.toList())
                    ).union();

                    // find hydro area by selected geometry
                    String areaGeometricAttrName = getLayerGeometricAttrName(areaLayer.get());

                    Filter areaFilter = filterFactory.intersects(
                            filterFactory.property(areaGeometricAttrName),
                            filterFactory.literal(selectedGeometry)
                    );

                    FeatureSource areaFeatureSource = areaLayer.get().getFeatureSource();
                    FeatureCollection areaQueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, areaFeatureSource.getSchema().getCoordinateReferenceSystem(), "GKODAS LIKE 'hd%'"));
                    SimpleFeatureCollection areaClipResult = clipByGeometry((SimpleFeatureCollection) areaQueryResult, selectedGeometry);
                    SimpleFeatureIterator areaFeaturesIterator = areaClipResult.features();

                    // hydro geometry
                    Geometry hydroGeometry = geometryFactory.buildGeometry(
                            featureIteratorToList(areaFeaturesIterator)
                                    .stream()
                                    .map(fa -> fa.getAttribute(areaGeometricAttrName))
                                    .collect(Collectors.toList())
                    ).union();

                    // selected geometry without hydro
                    Geometry selectedWithoutHydro = selectedGeometry.difference(hydroGeometry);

                    ShapeFileManager.writeGeometryToShapeFile(selectedWithoutHydro,
                            areaLayer.get().getFeatureSource().getSchema().getCoordinateReferenceSystem(),
                            "1_selectedWithoutHydro.shp");

                    // roads in selected geometry
                    String roadsGeometricAttrName = getLayerGeometricAttrName(roadsLayer.get());

                    Filter roadsFilter = filterFactory.intersects(
                            filterFactory.property(roadsGeometricAttrName),
                            filterFactory.literal(selectedGeometry)
                    );

                    FeatureSource roadsFeatureSource = roadsLayer.get().getFeatureSource();
                    FeatureCollection roadsQueryResult = roadsFeatureSource.getFeatures(getFilterQuery(roadsFilter, roadsFeatureSource.getSchema().getCoordinateReferenceSystem()));
                    SimpleFeatureCollection roadsClipResult = clipByGeometry((SimpleFeatureCollection) roadsQueryResult, selectedGeometry);
                    SimpleFeatureIterator roadsFeaturesIterator = roadsClipResult.features();

                    // roads geometry
                    Geometry roadsGeometry = geometryFactory.buildGeometry(
                            featureIteratorToList(roadsFeaturesIterator)
                                    .stream()
                                    .map(fa -> fa.getAttribute(roadsGeometricAttrName))
                                    .collect(Collectors.toList())
                    ).union();

                    // roads buffer geometry
                    Geometry roadsBufferedGeometry = roadsGeometry.buffer(maxDistanceToRoad);

                    // roads buffer geometry without roads
                    Geometry roadsBufferedGeometryWithoutRoads = roadsBufferedGeometry.difference(roadsGeometry.buffer(2));

                    ShapeFileManager.writeGeometryToShapeFile(roadsBufferedGeometryWithoutRoads,
                            roadsLayer.get().getFeatureSource().getSchema().getCoordinateReferenceSystem(),
                            "2_roadsBufferedGeometryWithoutRoads.shp");

                    // roads and selected area without hydro intersect
                    Geometry roadsAndSelectedAreaIntersect = selectedWithoutHydro.intersection(roadsBufferedGeometryWithoutRoads);

                    ShapeFileManager.writeGeometryToShapeFile(roadsAndSelectedAreaIntersect,
                            areaLayer.get().getFeatureSource().getSchema().getCoordinateReferenceSystem(),
                            "1_2_roadsAndSelectedAreaIntersect.shp");

                    // find gardens area by selected geometry
                    FeatureCollection gardenQueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, areaFeatureSource.getSchema().getCoordinateReferenceSystem(), "GKODAS = 'ms4'"));
                    SimpleFeatureCollection gardenClipResult = clipByGeometry((SimpleFeatureCollection) gardenQueryResult, selectedGeometry);
                    SimpleFeatureIterator gardenFeaturesIterator = gardenClipResult.features();

                    // gardens geometry
                    Geometry gardensGeometry = geometryFactory.buildGeometry(
                            featureIteratorToList(gardenFeaturesIterator)
                                    .stream()
                                    .map(fa -> fa.getAttribute(areaGeometricAttrName))
                                    .collect(Collectors.toList())
                    ).union();

                    ShapeFileManager.writeGeometryToShapeFile(gardensGeometry,
                            areaLayer.get().getFeatureSource().getSchema().getCoordinateReferenceSystem(),
                            "3_gardensGeometry.shp");

                    // find forests area by selected geometry
                    FeatureCollection forestQueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, areaFeatureSource.getSchema().getCoordinateReferenceSystem(), "GKODAS = 'ms0'"));
                    SimpleFeatureCollection forestClipResult = clipByGeometry((SimpleFeatureCollection) forestQueryResult, selectedGeometry);
                    SimpleFeatureIterator forestFeaturesIterator = forestClipResult.features();

                    // forests geometry
                    Geometry forestsGeometry = geometryFactory.buildGeometry(
                            featureIteratorToList(forestFeaturesIterator)
                                    .stream()
                                    .map(fa -> fa.getAttribute(areaGeometricAttrName))
                                    .collect(Collectors.toList())
                    ).union();

                    ShapeFileManager.writeGeometryToShapeFile(forestsGeometry,
                            areaLayer.get().getFeatureSource().getSchema().getCoordinateReferenceSystem(),
                            "4_forestsGeometry.shp");

                    // forests and gardens geometry union
                    Geometry forestsAndGardens = forestsGeometry.union(gardensGeometry);

                    ShapeFileManager.writeGeometryToShapeFile(forestsAndGardens,
                            areaLayer.get().getFeatureSource().getSchema().getCoordinateReferenceSystem(),
                            "3_4_forestsAndGardens.shp");

                    // forests and gardens geometry buffer
                    Geometry forestsAndGardensBuffer = forestsAndGardens.buffer(minDistanceToGardensAndForest);

                    ShapeFileManager.writeGeometryToShapeFile(forestsAndGardensBuffer,
                            areaLayer.get().getFeatureSource().getSchema().getCoordinateReferenceSystem(),
                            "5_forestsAndGardensBuffer.shp");

                    // selected area without hydro, near the road, without near forests and gardens
                    Geometry partlySuitableAreas = roadsAndSelectedAreaIntersect.difference(forestsAndGardensBuffer);

                    ShapeFileManager.writeGeometryToShapeFile(partlySuitableAreas,
                            areaLayer.get().getFeatureSource().getSchema().getCoordinateReferenceSystem(),
                            "6_partlySuitableAreas.shp");

                    if (partlySuitableAreas.isEmpty()){
                        DialogManager.showErrorDialog(this, "Can't find area for kart circuit in selected rectangle!");
                        return;
                    }

                    // only flat areas
                    String surfaceGeometricAttrName = getLayerGeometricAttrName(surfaceLayer.get());

                    Filter surfaceFilter = filterFactory.intersects(
                            filterFactory.property(surfaceGeometricAttrName),
                            filterFactory.literal(partlySuitableAreas)
                    );

                    FeatureSource surfaceFeatureSource = surfaceLayer.get().getFeatureSource();
                    FeatureCollection surfaceQueryResult = surfaceFeatureSource.getFeatures(getFilterQuery(surfaceFilter, surfaceFeatureSource.getSchema().getCoordinateReferenceSystem()));
                    SimpleFeatureCollection surfaceClipResult = clipByGeometry((SimpleFeatureCollection) surfaceQueryResult, partlySuitableAreas);

                    SimpleFeatureIterator surfaceFeaturesIterator = surfaceClipResult.features();

                    List<SimpleFeature> flatSurfacesFeatures = FeaturesTypeConverter.multiPolygonFeaturesToPolygonFeatures(featureIteratorToList(surfaceFeaturesIterator));

                    ShapeFileManager.writeFeaturesToShapeFile(flatSurfacesFeatures, "7_flatSurfacesFeatures.shp");

                    // suitable area surface features
                    List<SimpleFeature> suitableAreaSurfaceFeatures = flatSurfacesFeatures
                            .stream()
                            .filter(sf -> ((Geometry) sf.getAttribute(surfaceGeometricAttrName)).getArea() > minSurfaceArea)
                            .collect(Collectors.toList());

                    ShapeFileManager.writeFeaturesToShapeFile(suitableAreaSurfaceFeatures, "8_suitableAreaSurfaceFeatures.shp");

                    // suitable area surface geometry
                    Geometry suitableAreaSurfaceGeometry = geometryFactory.buildGeometry(
                            suitableAreaSurfaceFeatures
                                    .stream()
                                    .map(fa -> fa.getAttribute(surfaceGeometricAttrName))
                                    .collect(Collectors.toList())
                    ).union();

                    // buildings in suitable surfaces
                    String buildingsGeometricAttrName = getLayerGeometricAttrName(buildingsLayer.get());

                    Filter buildingsFilter = filterFactory.intersects(
                            filterFactory.property(buildingsGeometricAttrName),
                            filterFactory.literal(suitableAreaSurfaceGeometry)
                    );

                    FeatureSource buildingsFeatureSource = buildingsLayer.get().getFeatureSource();
                    FeatureCollection buildingsQueryResult = buildingsFeatureSource.getFeatures(getFilterQuery(buildingsFilter, buildingsFeatureSource.getSchema().getCoordinateReferenceSystem()));
                    SimpleFeatureCollection buildingsClipResult = clipByGeometry((SimpleFeatureCollection) buildingsQueryResult, suitableAreaSurfaceGeometry);

                    SimpleFeatureIterator buildingsFeaturesIterator = buildingsClipResult.features();

                    List<SimpleFeature> buildingsInSuitableAreas = featureIteratorToList(buildingsFeaturesIterator);

                    ShapeFileManager.writeFeaturesToShapeFile(buildingsInSuitableAreas, "9_buildingsSimpleFeatureList.shp");

                    // completely suitable areas

                    List<SimpleFeature> completelySuitableAreas = suitableAreaSurfaceFeatures
                            .stream()
                            .filter(surfaceFeature -> {
                                SimpleFeatureCollection buildingsInArea = clipByGeometry(buildingsClipResult, (Geometry) surfaceFeature.getAttribute(surfaceGeometricAttrName));
                                SimpleFeatureIterator buildingsInAreaIterator = buildingsInArea.features();

                                double buildingsAreaSum = 0;

                                try {
                                    while (buildingsInAreaIterator.hasNext()) {
                                        SimpleFeature simpleFeature = buildingsInAreaIterator.next();
                                        Geometry geometry = (Geometry) simpleFeature.getAttribute(buildingsGeometricAttrName);
                                        buildingsAreaSum += geometry.getArea();
                                    }
                                } finally {
                                    buildingsInAreaIterator.close();
                                }

                                double surfaceArea = ((Geometry) surfaceFeature.getAttribute(surfaceGeometricAttrName)).getArea();
                                double buildingsInSurfaceRatio = buildingsAreaSum / surfaceArea;

                                if (buildingsInSurfaceRatio < maxRatioWithBuildings)
                                    return true;

                                return false;
                            }).collect(Collectors.toList());

                    ShapeFileManager.writeFeaturesToShapeFile(completelySuitableAreas, "10_completelySuitableAreas.shp");

                    if (completelySuitableAreas.size() > 0){
                        System.out.println("FOUND = " + completelySuitableAreas.size());

                        Random rand = new Random();

                        List<SimpleFeature> winnerList = new ArrayList<>();
                        winnerList.add(completelySuitableAreas.get(rand.nextInt(completelySuitableAreas.size())));

                        ShapeFileManager.writeFeaturesToShapeFile(winnerList, "11_result.shp");

                        FileDataStore fileDataStore = FileDataStoreFinder.getDataStore(ShapeFileManager.getShapeFile("11_result.shp"));

                        SimpleFeatureSource simpleFeatureSource = fileDataStore.getFeatureSource();
                        Style style = SLD.createSimpleStyle(simpleFeatureSource.getSchema());
                        Layer layer = new FeatureLayer(simpleFeatureSource, style);
                        mapContent.addLayer(layer);

                        DialogManager.showInfoDialog(this, "Area for kart circuit was successfully found in selected rectangle!");
                    } else {
                        DialogManager.showErrorDialog(this, "Can't find area for kart circuit in selected rectangle!");
                    }

                } catch (Exception e) {
                    System.out.println("ERROR - " + e.getMessage());
                    DialogManager.showErrorDialog(this, "Error when trying to calculate result!");
                }
            } else {
                DialogManager.showInfoDialog(this, "Result can't be calculated, because some of the KELIAI_3, PLOTAI_3, PAVIRS_LT_P_3, PASTAT_P_3 layers were not found.");
            }
        } else {
            DialogManager.showErrorDialog(this, "Please select area at first!");
        }
    }

    private List<SimpleFeature> featureIteratorToList(SimpleFeatureIterator featuresIterator) {
        List<SimpleFeature> featureList = new ArrayList<>();

        try {
            while (featuresIterator.hasNext()) {
                featureList.add(featuresIterator.next());
            }
        } finally {
            featuresIterator.close();
        }

        return featureList;
    }

    private SimpleFeatureCollection clipByGeometry(SimpleFeatureCollection simpleFeatureCollection, Geometry geometry) {
        ClipProcess clipProcess = new ClipProcess();
        return clipProcess.execute(simpleFeatureCollection, geometry, true);
    }

    private Query getFilterQuery(Filter filter, CoordinateReferenceSystem crs) {
        Query query = new Query(null, filter);
        query.setCoordinateSystemReproject(crs);
        return query;
    }

    private Query getFilterQueryWithSearch(Filter filter, CoordinateReferenceSystem crs, String queryString) throws CQLException {
        Filter twoFilters = filterFactory.and(filter, CQL.toFilter(queryString));
        Query query = new Query(null, twoFilters);
        query.setCoordinateSystemReproject(crs);
        return query;
    }

    private String getLayerGeometricAttrName(Layer layer) {
        GeometryDescriptor geomDesc = layer.getFeatureSource().getSchema().getGeometryDescriptor();
        return geomDesc.getLocalName();
    }

    private Optional<Layer> getLayer(String layerName){
        return layers
                .stream()
                .filter(l -> l.getFeatureSource().getName().toString().equals(layerName))
                .findFirst();
    }
}