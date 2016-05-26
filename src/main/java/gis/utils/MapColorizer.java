package gis.utils;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.Geometries;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.*;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.style.ContrastMethod;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class MapColorizer {

    private static final Color SELECTED_COLOUR = Color.YELLOW;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 1.0f;
    private static final float OPACITY = 1.0f;

    private static final StyleFactory sf = CommonFactoryFinder.getStyleFactory();
    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

    public static Style getDefaultlayerStyle(Layer layer) {
        return SLD.createSimpleStyle(layer.getFeatureSource().getSchema());
    }

    public static Style getGreyscaleRasterStyle() {
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        SelectedChannelType sct = sf.createSelectedChannelType(String.valueOf(1), ce);

        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

    public static void highlightSelectedFeatures(Set<FeatureId> selectedFeatures, Layer layer) {
        Style style = MapColorizer.createSelectedStyle(selectedFeatures, layer);
        ((FeatureLayer) layer).setStyle(style);

        // for correct draw
        try { Thread.sleep(600);} catch (Exception ignored){}

    }

    private static Style createSelectedStyle(Set<FeatureId> selectedIds, Layer layer) {
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();

        List<Rule> rulesList = layer.getStyle().featureTypeStyles().get(0).rules();

        rulesList.stream().filter(rule -> rule.getFilter() == null).forEach(rule -> fts.rules().add(rule));

        Rule selectedRule = createRule(SELECTED_COLOUR, SELECTED_COLOUR, layer);
        selectedRule.setFilter(ff.id(selectedIds));

        fts.rules().add(selectedRule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    private static Rule createRule(Color outlineColor, Color fillColor, Layer layer) {
        GeometryDescriptor geomDesc = layer.getFeatureSource().getSchema().getGeometryDescriptor();
        String geometryAttributeName = geomDesc.getLocalName();

        Class<? extends Geometry> geomClass = (Class<? extends Geometry>) geomDesc.getType().getBinding();
        Geometries geometries = Geometries.getForBinding(geomClass);

        Symbolizer symbolizer;
        Fill fill;

        org.geotools.styling.Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(LINE_WIDTH));

        if (geometries == Geometries.POLYGON || geometries == Geometries.MULTIPOLYGON) {
            stroke = sf.createStroke(ff.literal(Color.BLACK), ff.literal(LINE_WIDTH));
            fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
            symbolizer = sf.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
        } else if (geometries == Geometries.POINT || geometries == Geometries.MULTIPOINT){
            fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
            Mark mark = sf.getCircleMark();
            mark.setFill(fill);
            mark.setStroke(stroke);
            Graphic graphic = sf.createDefaultGraphic();
            graphic.graphicalSymbols().clear();
            graphic.graphicalSymbols().add(mark);
            graphic.setSize(ff.literal(POINT_SIZE));
            symbolizer = sf.createPointSymbolizer(graphic, geometryAttributeName);
        } else {
            symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
        }

        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);

        return rule;
    }
}
