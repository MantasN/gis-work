package gis.actions;

import gis.gui.FeaturesSearchPanel;
import org.geotools.map.MapContent;
import org.geotools.map.event.MapAdapter;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.swing.MapPane;
import org.geotools.swing.action.MapAction;
import gis.tools.SearchTool;

import java.awt.event.ActionEvent;

public class SearchAction extends MapAction {

    private FeaturesSearchPanel featuresSearchPanel;
    private SearchTool searchTool;

    public SearchAction(MapPane mapPane) {
        this(mapPane, false);
    }

    public SearchAction(MapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? SearchTool.TOOL_NAME : null;
        super.init(mapPane, toolName, SearchTool.TOOL_TIP, SearchTool.ICON_PATH);
        this.searchTool = new SearchTool();
    }

    public void actionPerformed(ActionEvent e) {
        initializeFeaturesSearchPanel(getMapPane().getMapContent());
        searchTool.setFeaturesSearchPanel(featuresSearchPanel);
        getMapPane().setCursorTool(searchTool);
    }

    private void initializeFeaturesSearchPanel(MapContent mapContent) {
        if(featuresSearchPanel == null || !featuresSearchPanel.isDisplayable()){
            featuresSearchPanel = new FeaturesSearchPanel(mapContent.layers());
            mapContent.addMapLayerListListener(new MapAdapter() {
                @Override
                public void layerRemoved(MapLayerListEvent event) {
                    super.layerChanged(event);
                    featuresSearchPanel.mapLayerRemoved(event.getLayer().toLayer());
                }

                @Override
                public void layerAdded(MapLayerListEvent event) {
                    super.layerAdded(event);
                    featuresSearchPanel.mapLayerAdded(event.getLayer().toLayer());
                }
            });
        }
    }
}
