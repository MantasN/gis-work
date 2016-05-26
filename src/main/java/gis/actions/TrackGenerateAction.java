package gis.actions;

import gis.tools.TrackGenerateTool;
import org.geotools.map.event.MapAdapter;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.swing.MapPane;
import org.geotools.swing.action.MapAction;

import java.awt.event.ActionEvent;

public class TrackGenerateAction extends MapAction {

    private TrackGenerateTool trackGenerateTool;

    public TrackGenerateAction(MapPane mapPane) {
        this(mapPane, false);
    }

    public TrackGenerateAction(MapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? TrackGenerateTool.TOOL_NAME : null;
        super.init(mapPane, toolName, TrackGenerateTool.TOOL_TIP, TrackGenerateTool.ICON_IMAGE);

        trackGenerateTool = new TrackGenerateTool();

        mapPane.getMapContent().addMapLayerListListener(new MapAdapter() {
            @Override
            public void layerRemoved(MapLayerListEvent event) {
                super.layerChanged(event);
                trackGenerateTool.layerRemoved(event.getLayer().toLayer());
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        getMapPane().setCursorTool(trackGenerateTool);
    }
}
