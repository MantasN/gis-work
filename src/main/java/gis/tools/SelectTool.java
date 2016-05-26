package gis.tools;

import gis.gui.DialogManager;
import gis.gui.SelectedFeaturesTreePanel;
import gis.models.SelectedFeaturesResult;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.Feature;
import org.opengis.geometry.BoundingBox;
import gis.providers.SelectedFeaturesProvider;
import gis.utils.Calculations;
import gis.utils.MapColorizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SelectTool extends CursorTool {

    public static final String TOOL_NAME = "Features attributes";
    public static final String TOOL_TIP = "Show selected features attributes";
    public static final String ICON_IMAGE = "/select.png";

    private Map<Layer, SelectedFeaturesResult> selectedFeaturesResults;
    private Map<Layer, SelectedFeaturesProvider> selectedFeaturesProviders;

    private final Point startPosDevice;
    private final Point2D startPosWorld;
    private boolean dragged;

    private SelectedFeaturesTreePanel selectedFeaturesTreePanel;

    public SelectTool() {
        selectedFeaturesResults = new HashMap<>();
        selectedFeaturesProviders = new HashMap<>();
        startPosDevice = new Point();
        startPosWorld = new DirectPosition2D();
        dragged = false;
    }

    @Override
    public void onMouseClicked(MapMouseEvent e) {
        SelectedFeaturesTreePanel selectedFeaturesTreePanel = getSelectedFeaturesTreePanel();

        if (e.getButton() == MouseEvent.BUTTON3) {
            List<Feature> features = selectedFeaturesResults.values().stream()
                    .flatMap(sf -> sf.getSelectedFeatures().stream())
                    .map(ff -> ff.getFeature())
                    .collect(Collectors.toList());

            Envelope2D bounds = Calculations.getFeaturesBounds(features);
            if (bounds != null) getMapPane().setDisplayArea(bounds);

            return;
        }

        MapContent content = getMapPane().getMapContent();

        Point screenPos = e.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x - 2, screenPos.y - 2, 5, 5);
        AffineTransform screenToWorld = getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        ReferencedEnvelope bbox = new ReferencedEnvelope(worldRect, content.getCoordinateReferenceSystem());

        if(e.isControlDown()){
            findSelectedFeatures(bbox, content, true);
        } else {
            findSelectedFeatures(bbox, content, false);
        }

        selectedFeaturesTreePanel.populateTree(selectedFeaturesResults);
    }

    @Override
    public void onMousePressed(MapMouseEvent ev) {
        startPosDevice.setLocation(ev.getPoint());
        startPosWorld.setLocation(ev.getWorldPos());
    }

    @Override
    public void onMouseDragged(MapMouseEvent ev) {
        dragged = true;
    }

    @Override
    public void onMouseReleased(MapMouseEvent ev) {
        SelectedFeaturesTreePanel selectedFeaturesTreePanel = getSelectedFeaturesTreePanel();

        if (dragged && !ev.getPoint().equals(startPosDevice)) {
            Envelope2D env = new Envelope2D();
            env.setFrameFromDiagonal(startPosWorld, ev.getWorldPos());
            MapContent content = getMapPane().getMapContent();

            if(ev.isControlDown()){
                findSelectedFeatures(env, content, true);
            } else {
                findSelectedFeatures(env, content, false);
            }

            selectedFeaturesTreePanel.populateTree(selectedFeaturesResults);

            dragged = false;
        }
    }

    @Override
    public boolean drawDragBox() {
        return true;
    }

    private SelectedFeaturesTreePanel getSelectedFeaturesTreePanel() {
        if(selectedFeaturesTreePanel == null || !selectedFeaturesTreePanel.isDisplayable()){
            selectedFeaturesTreePanel = new SelectedFeaturesTreePanel();
        }

        return selectedFeaturesTreePanel;
    }

    private void findSelectedFeatures(BoundingBox bbox, MapContent content, boolean merge) {
        for (Layer layer : content.layers()) {
            if (layer.isSelected()) {
                if (layer instanceof FeatureLayer) {
                    try {
                        SelectedFeaturesProvider selectedFeaturesProvider = selectedFeaturesProviders.get(layer);

                        if (selectedFeaturesProvider == null) {
                            selectedFeaturesProvider = new SelectedFeaturesProvider(content, layer);
                            selectedFeaturesProviders.put(layer, selectedFeaturesProvider);
                        }

                        SelectedFeaturesResult selectedFeaturesResult = selectedFeaturesProvider.getSelectedFeaturesWithAttributes(bbox);

                        if(merge && selectedFeaturesResults.containsKey(layer)){
                            SelectedFeaturesResult.merge(selectedFeaturesResults.get(layer), selectedFeaturesResult);
                        } else {
                            selectedFeaturesResults.put(layer, selectedFeaturesResult);
                        }

                        displaySelectedFeatures(selectedFeaturesResults.get(layer));

                    } catch (Exception ex) {
                        DialogManager.showErrorDialog(new JFrame(), "Error when trying to get selected features!");
                    }
                }
            } else {
                if (selectedFeaturesResults.containsKey(layer)) selectedFeaturesResults.remove(layer);
                setDefaultStyle(layer);
            }
        }
    }

    private void setDefaultStyle(Layer layer) {
        ((FeatureLayer) layer).setStyle(MapColorizer.getDefaultlayerStyle(layer));
    }

    private void displaySelectedFeatures(SelectedFeaturesResult selectedFeaturesResult) {
        MapColorizer.highlightSelectedFeatures(selectedFeaturesResult.getSelectedFeatures().stream()
               .map(f -> f.getFeature().getIdentifier())
               .collect(Collectors.toSet()), selectedFeaturesResult.getLayer());
    }

    public void layerRemoved(Layer layer) {
        selectedFeaturesProviders.remove(layer);
        selectedFeaturesResults.remove(layer);

        if (selectedFeaturesTreePanel != null)
            selectedFeaturesTreePanel.populateTree(selectedFeaturesResults);
    }
}
