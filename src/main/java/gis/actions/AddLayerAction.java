package gis.actions;

import gis.gui.DialogManager;
import gis.utils.MapColorizer;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.GridReaderLayer;
import org.geotools.map.Layer;
import org.geotools.styling.*;
import org.geotools.swing.MapPane;
import org.geotools.swing.action.MapAction;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.style.ContrastMethod;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class AddLayerAction extends MapAction {

    private final String TOOL_NAME = "Add layer";
    private final String TOOL_TIP = "Add new layer to map";
    private final String ICON_PATH = "/open.png";

    public AddLayerAction(MapPane mapPane) {
        this(mapPane, false);
    }

    public AddLayerAction(MapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? TOOL_NAME : null;
        super.init(mapPane, toolName, TOOL_TIP, ICON_PATH);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            File file = JFileDataStoreChooser.showOpenFile(new String[]{"shp", "jpg", "tif"}, null);

            if (file == null) return;

            boolean adjust = false;

            if (getMapPane().getMapContent().layers().size() == 0)
                adjust = true;

            String filePath = file.getPath().toLowerCase();

            if (filePath.endsWith(".jpg") || filePath.endsWith(".tif") || filePath.endsWith(".tiff")){
                addRaster(file);
            } else if (filePath.endsWith(".shp")){
                addShp(file);
            }

            if (adjust)
                adjustCRS();

        } catch (Exception ex){
            DialogManager.showErrorDialog(new JFrame(), "Error when adding new layer!");
        }
    }

    private void addShp(File file) throws Exception {
        FileDataStore fileDataStore = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource simpleFeatureSource = fileDataStore.getFeatureSource();
        Style style = SLD.createSimpleStyle(simpleFeatureSource.getSchema());
        Layer layer = new FeatureLayer(simpleFeatureSource, style);
        getMapPane().getMapContent().addLayer(layer);
    }

    private void addRaster(File file) throws Exception {
        AbstractGridFormat format = GridFormatFinder.findFormat(file);
        AbstractGridCoverage2DReader reader = format.getReader(file);
        Style style = MapColorizer.getGreyscaleRasterStyle();
        Layer layer = new GridReaderLayer(reader, style);
        getMapPane().getMapContent().addLayer(layer);
    }

    private void adjustCRS() {
        for (Layer layer : getMapPane().getMapContent().layers()) {
            ReferencedEnvelope bounds = layer.getBounds();
            if (bounds != null) {
                CoordinateReferenceSystem crs = bounds.getCoordinateReferenceSystem();
                if (crs != null) {
                    getMapPane().getMapContent().getViewport().setCoordinateReferenceSystem(crs);
                    return;
                }
            }
        }
    }
}