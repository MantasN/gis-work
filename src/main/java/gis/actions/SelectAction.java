package gis.actions;

import org.geotools.map.event.MapAdapter;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.swing.MapPane;
import org.geotools.swing.action.MapAction;
import gis.tools.SelectTool;

import java.awt.event.ActionEvent;

public class SelectAction extends MapAction {

    private SelectTool selectTool;

    public SelectAction(MapPane mapPane) {
        this(mapPane, false);
    }

    public SelectAction(MapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? SelectTool.TOOL_NAME : null;
        super.init(mapPane, toolName, SelectTool.TOOL_TIP, SelectTool.ICON_IMAGE);

        selectTool = new SelectTool();

        mapPane.getMapContent().addMapLayerListListener(new MapAdapter() {
            @Override
            public void layerRemoved(MapLayerListEvent event) {
                super.layerChanged(event);
                selectTool.layerRemoved(event.getLayer().toLayer());
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        getMapPane().setCursorTool(selectTool);
    }
}
