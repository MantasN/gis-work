package gis.tools;

import gis.gui.DialogManager;
import gis.gui.KartCircuitPanel;
import gis.models.SelectedFeatureResult;
import gis.models.SelectedFeaturesResult;
import gis.providers.SelectedFeaturesProvider;
import gis.utils.Calculations;
import gis.utils.MapColorizer;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.Feature;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class KartCircuitTool extends CursorTool {

    public static final String TOOL_NAME = "Kart circuit place search";
    public static final String TOOL_TIP = "Find place for kart circuit";
    public static final String ICON_IMAGE = "/kart.png";

    private SelectedFeaturesResult selectedFeaturesResult;
    private Map<Layer, SelectedFeaturesProvider> selectedFeaturesProviders;

    private final Point startPosDevice;
    private final Point2D startPosWorld;
    private boolean dragged;

    private KartCircuitPanel kartCircuitPanel;

    public KartCircuitTool() {
        selectedFeaturesProviders = new HashMap<>();
        startPosDevice = new Point();
        startPosWorld = new DirectPosition2D();
        dragged = false;
    }

    @Override
    public void onMouseClicked(MapMouseEvent e) {
        KartCircuitPanel kartCircuitPanel = getKartCircuitPanel();

        MapContent content = getMapPane().getMapContent();
        Point screenPos = e.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x - 2, screenPos.y - 2, 5, 5);
        AffineTransform screenToWorld = getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        ReferencedEnvelope bbox = new ReferencedEnvelope(worldRect, content.getCoordinateReferenceSystem());

        findSelectedFeature(bbox, content);

        kartCircuitPanel.populateSelectedFeatures(getMapPane().getMapContent().layers(), selectedFeaturesResult);
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
        KartCircuitPanel kartCircuitPanel = getKartCircuitPanel();

        if (dragged && !ev.getPoint().equals(startPosDevice)) {
            Envelope2D env = new Envelope2D();
            env.setFrameFromDiagonal(startPosWorld, ev.getWorldPos());
            MapContent content = getMapPane().getMapContent();

            findSelectedFeature(env, content);

            kartCircuitPanel.populateSelectedFeatures(getMapPane().getMapContent().layers(), selectedFeaturesResult);

            dragged = false;
        }
    }

    @Override
    public boolean drawDragBox() {
        return true;
    }

    private KartCircuitPanel getKartCircuitPanel() {
        if (kartCircuitPanel == null || !kartCircuitPanel.isDisplayable()) {
            kartCircuitPanel = new KartCircuitPanel(getMapPane().getMapContent());
        }

        return kartCircuitPanel;
    }

    private void findSelectedFeature(BoundingBox bbox, MapContent content) {
        boolean founded = false;

        for (Layer layer : content.layers()) {
            if (layer.isSelected() && isValidLayer(layer.getFeatureSource().getName().toString()) && !founded) {
                if (layer instanceof FeatureLayer) {
                    try {
                        SelectedFeaturesProvider selectedFeaturesProvider = selectedFeaturesProviders.get(layer);

                        if (selectedFeaturesProvider == null) {
                            selectedFeaturesProvider = new SelectedFeaturesProvider(content, layer);
                            selectedFeaturesProviders.put(layer, selectedFeaturesProvider);
                        }

                        SelectedFeaturesResult selectedFeaturesResult = selectedFeaturesProvider.getSelectedFeaturesWithAttributes(bbox);

                        if (selectedFeaturesResult.getSelectedFeatures().size() > 0) {
                            this.selectedFeaturesResult = selectedFeaturesResult;
//                            displaySelectedFeatures();
                            founded = true;
                        } else {
                            if (this.selectedFeaturesResult != null && this.selectedFeaturesResult.getLayer() == layer)
                                this.selectedFeaturesResult = null;
//                            setDefaultStyle(layer);
                        }

                    } catch (Exception ex) {
                        DialogManager.showErrorDialog(new JFrame(), "Error when trying to get selected features!");
                    }
                }
            } else {
                if (selectedFeaturesResult != null && selectedFeaturesResult.getLayer() == layer)
                    selectedFeaturesResult = null;

//                setDefaultStyle(layer);
            }
        }
    }

    private boolean isValidLayer(String layerName) {
        return layerName.equals("PAVIRS_LT_P_3");
    }

//    private void setDefaultStyle(Layer layer) {
//        ((FeatureLayer) layer).setStyle(MapColorizer.getDefaultlayerStyle(layer));
//    }
//
//    private void displaySelectedFeatures() {
//        MapColorizer.highlightSelectedFeatures(selectedFeaturesResult.getSelectedFeatures().stream()
//                .map(f -> f.getFeature().getIdentifier())
//                .collect(Collectors.toSet()), selectedFeaturesResult.getLayer());
//    }

    public void layerRemoved(Layer layer) {
        selectedFeaturesProviders.remove(layer);

        if (selectedFeaturesResult != null && selectedFeaturesResult.getLayer() == layer) {
            selectedFeaturesResult = null;
            kartCircuitPanel.populateSelectedFeatures(getMapPane().getMapContent().layers(), null);
        }
    }
}
