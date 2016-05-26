package gis.tools;

import gis.gui.DialogManager;
import gis.gui.StatInfoPanel;
import gis.models.SelectedFeatureResult;
import gis.providers.SelectedFeaturesProvider;
import gis.utils.Calculations;
import gis.utils.MapColorizer;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class StatInfoTool extends CursorTool {

    public static final String TOOL_NAME = "Statistic info";
    public static final String TOOL_TIP = "Show selected feature statistic info";
    public static final String ICON_IMAGE = "/stat_info.png";

    private Map<Layer, SelectedFeaturesProvider> selectedFeaturesProviders;
    private StatInfoPanel statInfoPanel;

    private SelectedFeatureResult selectedFeatureResult;

    public StatInfoTool() {
        selectedFeaturesProviders = new HashMap<>();
    }

    @Override
    public void onMouseClicked(MapMouseEvent e) {
        StatInfoPanel statInfoPanel = getStatInfoPanel();

        if (e.getButton() == MouseEvent.BUTTON3 && selectedFeatureResult != null) {

            Envelope2D bounds = Calculations.getFeatureBounds(selectedFeatureResult.getFeature());
            if (bounds != null) getMapPane().setDisplayArea(bounds);

            return;
        }

        MapContent content = getMapPane().getMapContent();

        Point screenPos = e.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x - 2, screenPos.y - 2, 5, 5);
        AffineTransform screenToWorld = getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        ReferencedEnvelope bbox = new ReferencedEnvelope(worldRect, content.getCoordinateReferenceSystem());

        findSelectedFeature(bbox, content);

        statInfoPanel.populateData(getMapPane().getMapContent().layers(), selectedFeatureResult, getMapPane().getMapContent().getCoordinateReferenceSystem());
    }

    @Override
    public boolean drawDragBox() {
        return false;
    }

    private StatInfoPanel getStatInfoPanel() {
        if(statInfoPanel == null || !statInfoPanel.isDisplayable()){
            statInfoPanel = new StatInfoPanel();
        }

        return statInfoPanel;
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

                        SelectedFeatureResult selectedFeatureResult = selectedFeaturesProvider.getSelectedFeature(bbox);

                        if (selectedFeatureResult.getFeature() != null) {
                            this.selectedFeatureResult = selectedFeatureResult;
                            displaySelectedFeature(selectedFeatureResult);
                            founded = true;
                        } else {
                            if (this.selectedFeatureResult != null && this.selectedFeatureResult.getLayer() == layer)
                                this.selectedFeatureResult = null;
                            setDefaultStyle(layer);
                        }

                    } catch (Exception ex) {
                        DialogManager.showErrorDialog(new JFrame(), "Error when trying to get selected features!");
                    }
                }
            } else {
               if (selectedFeatureResult != null && selectedFeatureResult.getLayer() == layer)
                   selectedFeatureResult = null;

                setDefaultStyle(layer);
            }
        }
    }

    private boolean isValidLayer(String layerName) {
        return layerName.equals("RIBOS_P_3") || layerName.equals("PLOTAI_3");
    }

    private void setDefaultStyle(Layer layer) {
        ((FeatureLayer) layer).setStyle(MapColorizer.getDefaultlayerStyle(layer));
    }

    private void displaySelectedFeature(SelectedFeatureResult selectedFeatureResult) {
        Set<FeatureId> featureIdSet = new HashSet<>();
        featureIdSet.add(selectedFeatureResult.getFeature().getIdentifier());
        MapColorizer.highlightSelectedFeatures(featureIdSet, selectedFeatureResult.getLayer());
    }

    public void layerRemoved(Layer layer) {
        selectedFeaturesProviders.remove(layer);

        if (selectedFeatureResult != null && selectedFeatureResult.getLayer() == layer) {
            selectedFeatureResult = null;
            statInfoPanel.populateData(getMapPane().getMapContent().layers(), selectedFeatureResult, getMapPane().getMapContent().getCoordinateReferenceSystem());
        }
    }
}
