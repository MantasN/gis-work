package gis.gui;

import com.vividsolutions.jts.geom.*;
import gis.models.SelectedFeatureResult;
import gis.providers.AttributesProvider;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.AreaFunction;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.map.Layer;
import org.geotools.process.vector.ClipProcess;
import org.opengis.feature.Feature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class StatInfoPanel extends JFrame {

    private JTextPane textPane;
    private JButton roadsButton;
    private JButton areaButton;
    private JButton buildingsButton;

    private List<Layer> layerList;
    private SelectedFeatureResult selectedFeatureResult;
    private CoordinateReferenceSystem coordinateReferenceSystem;

    FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(null);
    AreaFunction areaFunction = new AreaFunction();

    public StatInfoPanel() {
        super("Selected features");

        setLayout(new GridLayout(1,0));

        roadsButton = new JButton("Kelių tinklas");
        roadsButton.addActionListener(x -> roadsButtonPressed());

        areaButton = new JButton("Plotai");
        areaButton.addActionListener(x -> areaButtonPressed());

        buildingsButton = new JButton("Pastatų plotai");
        buildingsButton.addActionListener(x -> buildingsButtonPressed());

        JPanel panel = new JPanel();
        panel.add(roadsButton);
        panel.add(areaButton);
        panel.add(buildingsButton);

        textPane = new JTextPane();
        textPane.setEditable(false);

        JScrollPane textView = new JScrollPane(textPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(textView);
        splitPane.setBottomComponent(panel);

        splitPane.setDividerLocation(550);
        splitPane.setPreferredSize(new Dimension(800, 600));

        add(splitPane);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void populateData(List<Layer> layerList, SelectedFeatureResult selectedFeatureResult, CoordinateReferenceSystem coordinateReferenceSystem){
        this.layerList = layerList;
        this.selectedFeatureResult = selectedFeatureResult;
        this.coordinateReferenceSystem = coordinateReferenceSystem;
    }

    private void buildingsButtonPressed() {
        if (selectedFeatureResult != null) {
            Optional<Layer> areaLayer = getLayer("PLOTAI_3");
            Optional<Layer> buildingsLayer = getLayer("PASTAT_P_3");

            if (areaLayer.isPresent() && buildingsLayer.isPresent()) {

                String areaGeometricAttrName = getLayerGeometricAttrName(areaLayer.get());

                Filter areaFilter = filterFactory.intersects(
                        filterFactory.property(areaGeometricAttrName),
                        filterFactory.literal(getSelectedFeatureGeometry())
                );

                try {
                    FeatureSource areaFeatureSource = areaLayer.get().getFeatureSource();

                    FeatureCollection hd1QueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, "GKODAS LIKE 'hd1%'"));
                    FeatureCollection hd2QueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, "GKODAS LIKE 'hd2%'"));
                    FeatureCollection hd3QueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, "GKODAS LIKE 'hd3%'"));
                    FeatureCollection hd9QueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, "GKODAS LIKE 'hd9%'"));
                    FeatureCollection hd4QueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, "GKODAS LIKE 'hd4%'"));
                    FeatureCollection hd5QueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, "GKODAS LIKE 'hd5%'"));
                    FeatureCollection ms0QueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, "GKODAS LIKE 'ms0%'"));
                    FeatureCollection ms4QueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, "GKODAS LIKE 'ms4%'"));
                    FeatureCollection pu0QueryResult = areaFeatureSource.getFeatures(getFilterQueryWithSearch(areaFilter, "GKODAS LIKE 'pu0%'"));

                    SimpleFeatureCollection hd1ClipResult = clipBySelected((SimpleFeatureCollection) hd1QueryResult);
                    SimpleFeatureCollection hd2ClipResult = clipBySelected((SimpleFeatureCollection) hd2QueryResult);
                    SimpleFeatureCollection hd3ClipResult = clipBySelected((SimpleFeatureCollection) hd3QueryResult);
                    SimpleFeatureCollection hd9ClipResult = clipBySelected((SimpleFeatureCollection) hd9QueryResult);
                    SimpleFeatureCollection hd4ClipResult = clipBySelected((SimpleFeatureCollection) hd4QueryResult);
                    SimpleFeatureCollection hd5ClipResult = clipBySelected((SimpleFeatureCollection) hd5QueryResult);
                    SimpleFeatureCollection ms0ClipResult = clipBySelected((SimpleFeatureCollection) ms0QueryResult);
                    SimpleFeatureCollection ms4ClipResult = clipBySelected((SimpleFeatureCollection) ms4QueryResult);
                    SimpleFeatureCollection pu0ClipResult = clipBySelected((SimpleFeatureCollection) pu0QueryResult);

                    double hd1BuildingsSumArea = sumAreaClippingByFeatureCollection(buildingsLayer.get(), hd1ClipResult, areaLayer.get());
                    double hd2BuildingsSumArea = sumAreaClippingByFeatureCollection(buildingsLayer.get(), hd2ClipResult, areaLayer.get());
                    double hd3BuildingsSumArea = sumAreaClippingByFeatureCollection(buildingsLayer.get(), hd3ClipResult, areaLayer.get());
                    double hd9BuildingsSumArea = sumAreaClippingByFeatureCollection(buildingsLayer.get(), hd9ClipResult, areaLayer.get());
                    double hd4BuildingsSumArea = sumAreaClippingByFeatureCollection(buildingsLayer.get(), hd4ClipResult, areaLayer.get());
                    double hd5BuildingsSumArea = sumAreaClippingByFeatureCollection(buildingsLayer.get(), hd5ClipResult, areaLayer.get());
                    double ms0BuildingsSumArea = sumAreaClippingByFeatureCollection(buildingsLayer.get(), ms0ClipResult, areaLayer.get());
                    double ms4BuildingsSumArea = sumAreaClippingByFeatureCollection(buildingsLayer.get(), ms4ClipResult, areaLayer.get());
                    double pu0BuildingsSumArea = sumAreaClippingByFeatureCollection(buildingsLayer.get(), pu0ClipResult, areaLayer.get());

                    changeText(String.format(
                            "Atpažįstamų statinių ir pastatų plotas upių plote (hd1): %.5f m2., santykis su šiuo plotu %.5f\n" +
                                    "Atpažįstamų statinių ir pastatų plotas upelių, kanalų, drenažo griovių plote (hd2): %.5f m2., santykis su šiuo plotu %.5f\n" +
                                    "Atpažįstamų statinių ir pastatų plotas ežerų plote (hd3): %.5f m2., santykis su šiuo plotu %.5f\n" +
                                    "Atpažįstamų statinių ir pastatų plotas tvenkinių plote (hd9*): %.5f m2., santykis su šiuo plotu %.5f\n" +
                                    "Atpažįstamų statinių ir pastatų plotas baseinų, kūdrų plote (hd4): %.5f m2., santykis su šiuo plotu %.5f\n" +
                                    "Atpažįstamų statinių ir pastatų plotas baltijos jūros, kuršių marių plote (hd5): %.5f m2., santykis su šiuo plotu %.5f\n" +
                                    "Atpažįstamų statinių ir pastatų plotas medžiais, krūmais apaugusių teritorijų plote (ms0): %.5f m2., santykis su šiuo plotu %.5f\n" +
                                    "Atpažįstamų statinių ir pastatų plotas pramoninių sodų masyvų plote (ms4): %.5f m2., santykis su šiuo plotu %.5f\n" +
                                    "Atpažįstamų statinių ir pastatų plotas užstatytų teritorijų plote (pu0): %.5f m2., santykis su šiuo plotu %.5f",
                            hd1BuildingsSumArea, hd1BuildingsSumArea / getFeaturesAreaSum(hd1ClipResult, areaLayer.get()),
                            hd2BuildingsSumArea, hd2BuildingsSumArea / getFeaturesAreaSum(hd2ClipResult, areaLayer.get()),
                            hd3BuildingsSumArea, hd3BuildingsSumArea / getFeaturesAreaSum(hd3ClipResult, areaLayer.get()),
                            hd9BuildingsSumArea, hd9BuildingsSumArea / getFeaturesAreaSum(hd9ClipResult, areaLayer.get()),
                            hd4BuildingsSumArea, hd4BuildingsSumArea / getFeaturesAreaSum(hd4ClipResult, areaLayer.get()),
                            hd5BuildingsSumArea, hd5BuildingsSumArea / getFeaturesAreaSum(hd5ClipResult, areaLayer.get()),
                            ms0BuildingsSumArea, ms0BuildingsSumArea / getFeaturesAreaSum(ms0ClipResult, areaLayer.get()),
                            ms4BuildingsSumArea, ms4BuildingsSumArea / getFeaturesAreaSum(ms4ClipResult, areaLayer.get()),
                            pu0BuildingsSumArea, pu0BuildingsSumArea / getFeaturesAreaSum(pu0ClipResult, areaLayer.get()))
                    );

                } catch (Exception e) {
                    DialogManager.showErrorDialog(this, "Error when trying to calculate result!");
                }

            } else {
                DialogManager.showInfoDialog(this, "Result can't be calculated, because PLOTAI_3 or PASTAT_P_3 or both layers was not found.");
            }
        } else {
            DialogManager.showErrorDialog(this, "Please select feature at first!");
        }
    }

    private void areaButtonPressed() {
        if (selectedFeatureResult != null) {
            Optional<Layer> areaLayer = getLayer("PLOTAI_3");

            if (areaLayer.isPresent()) {
                String geometricAttrName = getLayerGeometricAttrName(areaLayer.get());

                Filter filter = filterFactory.intersects(
                        filterFactory.property(geometricAttrName),
                        filterFactory.literal(getSelectedFeatureGeometry())
                );

                try {
                    FeatureSource featureSource = areaLayer.get().getFeatureSource();

                    FeatureCollection hd1QueryResult = featureSource.getFeatures(getFilterQueryWithSearch(filter, "GKODAS LIKE 'hd1%'"));
                    FeatureCollection hd2QueryResult = featureSource.getFeatures(getFilterQueryWithSearch(filter, "GKODAS LIKE 'hd2%'"));
                    FeatureCollection hd3QueryResult = featureSource.getFeatures(getFilterQueryWithSearch(filter, "GKODAS LIKE 'hd3%'"));
                    FeatureCollection hd9QueryResult = featureSource.getFeatures(getFilterQueryWithSearch(filter, "GKODAS LIKE 'hd9%'"));
                    FeatureCollection hd4QueryResult = featureSource.getFeatures(getFilterQueryWithSearch(filter, "GKODAS LIKE 'hd4%'"));
                    FeatureCollection hd5QueryResult = featureSource.getFeatures(getFilterQueryWithSearch(filter, "GKODAS LIKE 'hd5%'"));
                    FeatureCollection ms0QueryResult = featureSource.getFeatures(getFilterQueryWithSearch(filter, "GKODAS LIKE 'ms0%'"));
                    FeatureCollection ms4QueryResult = featureSource.getFeatures(getFilterQueryWithSearch(filter, "GKODAS LIKE 'ms4%'"));
                    FeatureCollection pu0QueryResult = featureSource.getFeatures(getFilterQueryWithSearch(filter, "GKODAS LIKE 'pu0%'"));

                    SimpleFeatureCollection hd1ClipResult = clipBySelected((SimpleFeatureCollection) hd1QueryResult);
                    SimpleFeatureCollection hd2ClipResult = clipBySelected((SimpleFeatureCollection) hd2QueryResult);
                    SimpleFeatureCollection hd3ClipResult = clipBySelected((SimpleFeatureCollection) hd3QueryResult);
                    SimpleFeatureCollection hd9ClipResult = clipBySelected((SimpleFeatureCollection) hd9QueryResult);
                    SimpleFeatureCollection hd4ClipResult = clipBySelected((SimpleFeatureCollection) hd4QueryResult);
                    SimpleFeatureCollection hd5ClipResult = clipBySelected((SimpleFeatureCollection) hd5QueryResult);
                    SimpleFeatureCollection ms0ClipResult = clipBySelected((SimpleFeatureCollection) ms0QueryResult);
                    SimpleFeatureCollection ms4ClipResult = clipBySelected((SimpleFeatureCollection) ms4QueryResult);
                    SimpleFeatureCollection pu0ClipResult = clipBySelected((SimpleFeatureCollection) pu0QueryResult);

                    double hd1SumArea = getFeaturesAreaSum(hd1ClipResult, areaLayer.get());
                    double hd2SumArea = getFeaturesAreaSum(hd2ClipResult, areaLayer.get());
                    double hd3SumArea = getFeaturesAreaSum(hd3ClipResult, areaLayer.get());
                    double hd9SumArea = getFeaturesAreaSum(hd9ClipResult, areaLayer.get());
                    double hd4SumArea = getFeaturesAreaSum(hd4ClipResult, areaLayer.get());
                    double hd5SumArea = getFeaturesAreaSum(hd5ClipResult, areaLayer.get());
                    double ms0SumArea = getFeaturesAreaSum(ms0ClipResult, areaLayer.get());
                    double ms4SumArea = getFeaturesAreaSum(ms4ClipResult, areaLayer.get());
                    double pu0SumArea = getFeaturesAreaSum(pu0ClipResult, areaLayer.get());

                    double selectedFeatureArea = areaFunction.getArea(getSelectedFeatureGeometry());

                    changeText(String.format(
                            "Bendras upių plotas (hd1): %.5f m2., santykis su bendru plotu %.5f\n" +
                                    "Bendras upelių, kanalų, drenažo griovių plotas (hd2): %.5f m2., santykis su bendru plotu %.5f\n" +
                                    "Bendras ežerų plotas (hd3): %.5f m2., santykis su bendru plotu %.5f\n" +
                                    "Bendras tvenkinių plotas (hd9*): %.5f m2., santykis su bendru plotu %.5f\n" +
                                    "Bendras baseinų, kūdrų plotas (hd4): %.5f m2., santykis su bendru plotu %.5f\n" +
                                    "Bendras baltijos jūros, kuršių marių plotas (hd5): %.5f m2., santykis su bendru plotu %.5f\n" +
                                    "Bendras medžiais, krūmais apaugusių teritorijų plotas (ms0): %.5f m2., santykis su bendru plotu %.5f\n" +
                                    "Bendras pramoninių sodų masyvų plotas (ms4): %.5f m2., santykis su bendru plotu %.5f\n" +
                                    "Bendras užstatytų teritorijų plotas (pu0): %.5f m2., santykis su bendru plotu %.5f",
                            hd1SumArea, hd1SumArea / selectedFeatureArea,
                            hd2SumArea, hd2SumArea / selectedFeatureArea,
                            hd3SumArea, hd3SumArea / selectedFeatureArea,
                            hd9SumArea, hd9SumArea / selectedFeatureArea,
                            hd4SumArea, hd4SumArea / selectedFeatureArea,
                            hd5SumArea, hd5SumArea / selectedFeatureArea,
                            ms0SumArea, ms0SumArea / selectedFeatureArea,
                            ms4SumArea, ms4SumArea / selectedFeatureArea,
                            pu0SumArea, pu0SumArea / selectedFeatureArea)
                    );

                } catch (Exception e) {
                    DialogManager.showErrorDialog(this, "Error when trying to calculate result!");
                }

            } else {
                DialogManager.showInfoDialog(this, "Result can't be calculated, because PLOTAI_3 layer was not found.");
            }
        } else {
            DialogManager.showErrorDialog(this, "Please select feature at first!");
        }
    }

    private void roadsButtonPressed() {
        if (selectedFeatureResult != null) {
            Optional<Layer> roadsLayer = getLayer("KELIAI_3");

            if (roadsLayer.isPresent()) {
                String geometricAttrName = getLayerGeometricAttrName(roadsLayer.get());

                Filter filter = filterFactory.intersects(
                        filterFactory.property(geometricAttrName),
                        filterFactory.literal(getSelectedFeatureGeometry())
                );

                try {
                    FeatureSource featureSource = roadsLayer.get().getFeatureSource();
                    FeatureCollection queryResult = featureSource.getFeatures(getFilterQuery(filter));

                    SimpleFeatureCollection result = clipBySelected((SimpleFeatureCollection) queryResult);

                    FeatureIterator iterator = result.features();

                    double allRoadsLength = 0;

                    try {
                        while (iterator.hasNext()) {
                            Feature feature = iterator.next();
                            allRoadsLength += areaFunction.getPerimeter(
                                    (Geometry) AttributesProvider.getFeatureAttributes(roadsLayer.get(), feature).get(geometricAttrName)
                            );
                        }
                    } finally {
                        iterator.close();
                    }

                    double selectedFeatureArea = areaFunction.getArea(getSelectedFeatureGeometry());
                    double avgLengthInArea = (allRoadsLength / 1000) / (selectedFeatureArea / 1000000);

                    changeText(String.format(
                            "Bendras kelių tinklo ilgis: %.2f km.\n" +
                                    "Vidutinis kelių tinklo ilgis vienam kvadratiniam kilometrui: %.2f km.",
                            allRoadsLength / 1000,
                            avgLengthInArea
                    ));

                } catch (Exception e) {
                    DialogManager.showErrorDialog(this, "Error when trying to calculate result!");
                }

            } else {
                DialogManager.showInfoDialog(this, "Result can't be calculated, because KELIAI_3 layer was not found.");
            }
        } else {
            DialogManager.showErrorDialog(this, "Please select feature at first!");
        }
    }

    private SimpleFeatureCollection clipBySelected(SimpleFeatureCollection simpleFeatureCollection) {
        ClipProcess clipProcess = new ClipProcess();
        return clipProcess.execute(simpleFeatureCollection, getSelectedFeatureGeometry(), true);
    }

    private double sumAreaClippingByFeatureCollection(Layer fromLayer, SimpleFeatureCollection byCollection, Layer byLayer) throws IOException {
        FeatureIterator featureIterator = byCollection.features();
        ClipProcess clipProcess = new ClipProcess();

        double sum = 0;

        try {
            while (featureIterator.hasNext()) {
                Feature feature = featureIterator.next();
                Geometry featureGeometry = (Geometry) AttributesProvider.getFeatureAttributes(byLayer, feature)
                        .get(getLayerGeometricAttrName(byLayer));

                Filter filter = filterFactory.intersects(
                        filterFactory.property(getLayerGeometricAttrName(fromLayer)),
                        filterFactory.literal(featureGeometry)
                );

                sum += getFeaturesAreaSum(
                        clipProcess.execute(
                                (SimpleFeatureCollection) fromLayer.getFeatureSource().getFeatures(getFilterQuery(filter)),
                                featureGeometry,
                                true
                        ), fromLayer);
            }
        } finally {
            featureIterator.close();
        }

        return sum;
    }

    private Geometry getSelectedFeatureGeometry() {
        return (Geometry) AttributesProvider.getFeatureAttributes(
                selectedFeatureResult.getLayer(),
                selectedFeatureResult.getFeature()).get(getLayerGeometricAttrName(selectedFeatureResult.getLayer()));
    }

    private String getLayerGeometricAttrName(Layer layer) {
        GeometryDescriptor geomDesc = layer.getFeatureSource().getSchema().getGeometryDescriptor();
        return geomDesc.getLocalName();
    }

    private double getFeaturesAreaSum(SimpleFeatureCollection simpleFeatureCollection, Layer layer) {
        FeatureIterator iterator = simpleFeatureCollection.features();

        double sumArea = 0;

        try {
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                sumArea += areaFunction.getArea(
                        (Geometry) AttributesProvider.getFeatureAttributes(layer, feature).get(getLayerGeometricAttrName(layer))
                );
            }
        } finally {
            iterator.close();
        }

        return sumArea;
    }

    private Query getFilterQuery(Filter filter) {
        Query query = new Query(null, filter);
        query.setCoordinateSystemReproject(coordinateReferenceSystem);
        return query;
    }

    private Query getFilterQueryWithSearch(Filter filter, String queryString) throws CQLException {
        Filter twoFilters = filterFactory.and(filter, CQL.toFilter(queryString));
        Query query = new Query(null, twoFilters);
        query.setCoordinateSystemReproject(coordinateReferenceSystem);
        return query;
    }

    private Optional<Layer> getLayer(String layerName){
        return layerList
                .stream()
                .filter(l -> l.getFeatureSource().getName().toString().equals(layerName))
                .findFirst();
    }

    private void changeText(String text) {
        textPane.setText(text);
    }
}