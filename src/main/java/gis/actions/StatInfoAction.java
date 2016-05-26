package gis.actions;

import gis.tools.StatInfoTool;
import org.geotools.map.event.MapAdapter;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.swing.MapPane;
import org.geotools.swing.action.MapAction;

import java.awt.event.ActionEvent;

public class StatInfoAction extends MapAction {

    private StatInfoTool statInfoTool;

    public StatInfoAction(MapPane mapPane) {
        this(mapPane, false);
    }

    public StatInfoAction(MapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? StatInfoTool.TOOL_NAME : null;
        super.init(mapPane, toolName, StatInfoTool.TOOL_TIP, StatInfoTool.ICON_IMAGE);

        statInfoTool = new StatInfoTool();

        mapPane.getMapContent().addMapLayerListListener(new MapAdapter() {
            @Override
            public void layerRemoved(MapLayerListEvent event) {
                super.layerChanged(event);
                statInfoTool.layerRemoved(event.getLayer().toLayer());
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        getMapPane().setCursorTool(statInfoTool);
    }
}
