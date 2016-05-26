package gis.actions;

import gis.tools.KartCircuitTool;
import org.geotools.map.event.MapAdapter;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.swing.MapPane;
import org.geotools.swing.action.MapAction;

import java.awt.event.ActionEvent;

public class KartCircuitAction extends MapAction {

    private KartCircuitTool kartCircuitTool;

    public KartCircuitAction(MapPane mapPane) {
        this(mapPane, false);
    }

    public KartCircuitAction(MapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? KartCircuitTool.TOOL_NAME : null;
        super.init(mapPane, toolName, KartCircuitTool.TOOL_TIP, KartCircuitTool.ICON_IMAGE);

        kartCircuitTool = new KartCircuitTool();

        mapPane.getMapContent().addMapLayerListListener(new MapAdapter() {
            @Override
            public void layerRemoved(MapLayerListEvent event) {
                super.layerChanged(event);
                kartCircuitTool.layerRemoved(event.getLayer().toLayer());
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        getMapPane().setCursorTool(kartCircuitTool);
    }

}

