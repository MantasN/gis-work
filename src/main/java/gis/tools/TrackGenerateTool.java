package gis.tools;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import gis.gui.DialogManager;
import gis.models.SelectedFeatureResult;
import gis.providers.SelectedFeaturesProvider;
import gis.utils.ShapeFileManager;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TrackGenerateTool extends CursorTool {

    public static final String TOOL_NAME = "Track generation";
    public static final String TOOL_TIP = "Generate kart track in polygon";
    public static final String ICON_IMAGE = "/track.png";

    private Map<Layer, SelectedFeaturesProvider> selectedFeaturesProviders;
    private SelectedFeatureResult selectedFeatureResult;

    public TrackGenerateTool() {
        selectedFeaturesProviders = new HashMap<>();
    }

    @Override
    public void onMouseClicked(MapMouseEvent e) {
        MapContent content = getMapPane().getMapContent();

        java.awt.Point screenPos = e.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x - 2, screenPos.y - 2, 5, 5);
        AffineTransform screenToWorld = getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        ReferencedEnvelope bbox = new ReferencedEnvelope(worldRect, content.getCoordinateReferenceSystem());

        findSelectedFeature(bbox, content);

        try {
            if (selectedFeatureResult != null) {
                Layer selectedLayer = selectedFeatureResult.getLayer();
                SimpleFeature selectedFeature = (SimpleFeature) selectedFeatureResult.getFeature();
                Geometry selectedGeometry = (Geometry) selectedFeature.getAttribute("the_geom");

                Coordinate[] coordinates = selectedGeometry.getCoordinates();

                List<Coordinate> byX = Arrays.stream(coordinates).sorted((c1, c2) -> Double.compare(c1.x, c2.x)).collect(Collectors.toList());
                List<Coordinate> byY = Arrays.stream(coordinates).sorted((c1, c2) -> Double.compare(c1.y, c2.y)).collect(Collectors.toList());

                Point centroid = selectedGeometry.getCentroid();

                Coordinate eastCoord = byX
                        .stream()
                        .filter(c -> c.y > centroid.getY())
                        .filter(c -> c.x > centroid.getX())
                        .findAny().get();

                Coordinate westCoord = byX
                        .stream()
                        .filter(c -> c.x < centroid.getX())
                        .filter(c -> c.y < centroid.getY())
                        .findAny().get();

                Coordinate northCoord = byY
                        .stream()
                        .filter(c -> c.y > centroid.getY())
                        .filter(c -> c.x < centroid.getX())
                        .findAny().get();

                Coordinate southCoord = byY
                        .stream()
                        .filter(c -> c.y < centroid.getY())
                        .filter(c -> c.x > centroid.getX())
                        .findAny().get();


                double eastX = (eastCoord.x - centroid.getX()) * 0.6;
                double eastY = (eastCoord.y - centroid.getY()) * 0.6;

                eastCoord = new Coordinate(eastCoord.x - eastX, eastCoord.y - eastY);

                double westX = (centroid.getX() - westCoord.x) * 0.6;
                double westY = (centroid.getY() - westCoord.y) * 0.6;

                westCoord = new Coordinate(westCoord.x + westX, westCoord.y + westY);

                double northX = (centroid.getX() - northCoord.x) * 0.4;
                double northY = (northCoord.y - centroid.getY()) * 0.4;

                northCoord = new Coordinate(northCoord.x + northX, northCoord.y - northY);

                double southX = (southCoord.x - centroid.getX()) * 0.4;
                double southY = (centroid.getY() - southCoord.y) * 0.4;

                southCoord = new Coordinate(southCoord.x - southX, southCoord.y + southY);

                Coordinate[] result = new Coordinate[5];
                result[0] = westCoord;
                result[1] = northCoord;
                result[2] = eastCoord;
                result[3] = southCoord;
                result[4] = westCoord;

                Geometry trackGeometry = new GeometryFactory().createLineString(result).buffer(3);

                ShapeFileManager.writeGeometryToShapeFile(trackGeometry, selectedLayer.getFeatureSource().getSchema().getCoordinateReferenceSystem(), "12_track.shp");

                FileDataStore fileDataStore = FileDataStoreFinder.getDataStore(ShapeFileManager.getShapeFile("12_track.shp"));
                SimpleFeatureSource simpleFeatureSource = fileDataStore.getFeatureSource();
                Style style = SLD.createSimpleStyle(simpleFeatureSource.getSchema());
                Layer layer = new FeatureLayer(simpleFeatureSource, style);
                getMapPane().getMapContent().addLayer(layer);

                DialogManager.showInfoDialog(new JFrame(), "Track was successfully generated!");
            }
        } catch (Exception exception) {
            DialogManager.showErrorDialog(new JFrame(), "Can't generate track!");
        }
    }

    private void findSelectedFeature(BoundingBox bbox, MapContent content) {
        boolean founded = false;

        for (Layer layer : content.layers()) {
            if (layer.isSelected() && !founded) {
                if (layer instanceof FeatureLayer) {
                    try {
                        SelectedFeaturesProvider selectedFeaturesProvider = selectedFeaturesProviders.get(layer);

                        if (selectedFeaturesProvider == null) {
                            selectedFeaturesProvider = new SelectedFeaturesProvider(content, layer);
                            selectedFeaturesProviders.put(layer, selectedFeaturesProvider);
                        }

                        SelectedFeatureResult selectedFeatureResult = selectedFeaturesProvider.getSelectedFeature(bbox);

                        if (selectedFeatureResult.getFeature() != null) {
                            this.selectedFeatureResult = selectedFeatureResult;
                            founded = true;
                        } else {
                            if (this.selectedFeatureResult != null && this.selectedFeatureResult.getLayer() == layer)
                                this.selectedFeatureResult = null;
                        }

                    } catch (Exception ex) {
                        DialogManager.showErrorDialog(new JFrame(), "Error when trying to get selected features!");
                    }
                }
            } else {
               if (selectedFeatureResult != null && selectedFeatureResult.getLayer() == layer)
                   selectedFeatureResult = null;

            }
        }
    }

    public void layerRemoved(Layer layer) {
        selectedFeaturesProviders.remove(layer);

        if (selectedFeatureResult != null && selectedFeatureResult.getLayer() == layer) {
            selectedFeatureResult = null;
        }
    }
}
