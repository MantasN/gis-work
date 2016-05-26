package gis.tools;

import gis.gui.FeaturesSearchPanel;
import org.geotools.geometry.Envelope2D;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import gis.utils.Calculations;

import java.awt.event.MouseEvent;

public class SearchTool extends CursorTool {

    public static final String TOOL_NAME = "Features search";
    public static final String TOOL_TIP = "Search for features";
    public static final String ICON_PATH = "/search.png";

    private FeaturesSearchPanel featuresSearchPanel;

    public void setFeaturesSearchPanel(FeaturesSearchPanel featuresSearchPanel){
        this.featuresSearchPanel = featuresSearchPanel;
    }

    @Override
    public void onMouseClicked(MapMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && featuresSearchPanel != null) {
            Envelope2D bounds = Calculations.getFeaturesBounds(featuresSearchPanel.getSelectedFeatures());
            if (bounds != null) getMapPane().setDisplayArea(bounds);
        }
    }

}
