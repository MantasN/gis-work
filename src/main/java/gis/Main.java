package gis;

import gis.actions.*;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.*;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

import javax.swing.*;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        MapContent map = new MapContent();
        map.setTitle("GIS - Mantas Neviera");

        JMapFrame mapFrame = new JMapFrame(map);
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);
        mapFrame.enableLayerTable(true);

        JToolBar toolbar = mapFrame.getToolBar();
        toolbar.addSeparator();

        toolbar.add(new JButton(new AddLayerAction(mapFrame.getMapPane())));

        SelectAction selectAction = new SelectAction(mapFrame.getMapPane());
        toolbar.add(new JButton(selectAction));

        SearchAction searchAction = new SearchAction(mapFrame.getMapPane());
        toolbar.add(new JButton(searchAction));

        StatInfoAction statInfoAction = new StatInfoAction(mapFrame.getMapPane());
        toolbar.add(new JButton(statInfoAction));

        KartCircuitAction kartCircuitAction = new KartCircuitAction(mapFrame.getMapPane());
        toolbar.add(new JButton(kartCircuitAction));

        TrackGenerateAction trackGenerateAction = new TrackGenerateAction(mapFrame.getMapPane());
        toolbar.add(new JButton(trackGenerateAction));

        mapFrame.setSize(1024, 768);
        mapFrame.setVisible(true);

        // for quicker work
        File[] filesToLoad = new File[]{
                new File("C:\\Mantas\\GIS\\lt_data\\LT10shp\\RIBOS_P_3.shp"),
                new File("C:\\Mantas\\GIS\\lt_data\\LT10shp\\KELIAI_3.shp"),
                new File("C:\\Mantas\\GIS\\lt_data\\LT10shp\\PLOTAI_3.shp"),
                new File("C:\\Mantas\\GIS\\lt_data\\LT10shp\\PASTAT_P_3.shp"),
                new File("C:\\Mantas\\GIS\\lt_data\\LT200shp\\PAVIRS_LT_P_3.shp")
        };

        loadFiles(filesToLoad, mapFrame.getMapContent());
    }

    private static void loadFiles(File[] filesToLoad, MapContent mapContent) throws Exception {
        for (File file : filesToLoad){
            FileDataStore fileDataStore = FileDataStoreFinder.getDataStore(file);
            SimpleFeatureSource simpleFeatureSource = fileDataStore.getFeatureSource();
            Style style = SLD.createSimpleStyle(simpleFeatureSource.getSchema());
            Layer layer = new FeatureLayer(simpleFeatureSource, style);
            mapContent.addLayer(layer);
            Thread.sleep(500);
        }
    }

}