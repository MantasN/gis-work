package gis.gui;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import gis.models.FeatureTableModel;
import org.geotools.data.*;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.map.FeatureLayer;
import org.geotools.map.GridReaderLayer;
import org.geotools.map.Layer;
import org.opengis.feature.Feature;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import gis.utils.MapColorizer;

public class FeaturesSearchPanel extends JFrame {

    private Map<String, Layer> activeLayers;

    private JMenuBar bar;
    private JComboBox featureTypeCBox;
    private JTextField textField;
    private JTable table;
    private JButton button;

    public FeaturesSearchPanel(List<Layer> layers) {
        super("Search features");

        setLayout(new BorderLayout());

        bar = new JMenuBar();
        setJMenuBar(bar);

        featureTypeCBox = new JComboBox();
        featureTypeCBox.addActionListener(x -> filterFeatures());
        bar.add(featureTypeCBox);

        textField = new JTextField();
        textField.setText("include");
        textField.addActionListener(x -> filterFeatures());
        add(textField, BorderLayout.NORTH);

        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setPreferredScrollableViewportSize(new Dimension(800, 600));
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        button = new JButton("Highlight selected features in map");
        button.addActionListener(x -> highlightSelected());
        add(button, BorderLayout.PAGE_END);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);

        initialize(layers);
    }

    private void initialize(List<Layer> layers) {
        activeLayers = new HashMap<>();
        layers.stream()
                .filter(layer -> layer instanceof FeatureLayer)
                .forEach(layer -> activeLayers.put(layer.getFeatureSource().getName().toString(), layer));
        ComboBoxModel cbm = new DefaultComboBoxModel(activeLayers.keySet().toArray());
        featureTypeCBox.setModel(cbm);
        filterFeatures();
    }

    private void filterFeatures() {
        if (featureTypeCBox.getSelectedItem() != null) {
            try {
                Filter filter = CQL.toFilter(textField.getText());
                FeatureSource featureSource = activeLayers.get(featureTypeCBox.getSelectedItem().toString()).getFeatureSource();
                FeatureTableModel model = new FeatureTableModel(featureSource.getFeatures(filter));
                table.setModel(model);
            } catch (Exception e) {
                DialogManager.showErrorDialog(this, "Error when querying features! Please check your query and try again.");
            }
        } else {
            table.setModel(new DefaultTableModel());
        }
    }

    public List<Feature> getSelectedFeatures(){
        return Arrays.stream(table.getSelectedRows())
                .mapToObj(rowNumber -> ((FeatureTableModel) table.getModel()).getFeatureAt(rowNumber))
                .collect(Collectors.toList());
    }

    private void highlightSelected() {
        if (featureTypeCBox.getSelectedItem() != null) {

            Set<FeatureId> selectedFeatures = Arrays.stream(table.getSelectedRows())
                    .mapToObj(rowNumber -> new FeatureIdImpl(table.getValueAt(rowNumber, 0).toString()))
                    .collect(Collectors.toSet());

            MapColorizer.highlightSelectedFeatures(selectedFeatures, activeLayers.get(featureTypeCBox.getSelectedItem().toString()));

            activeLayers.entrySet().stream()
                    .filter(e -> !e.getKey().equals(featureTypeCBox.getSelectedItem().toString()))
                    .forEach(e -> ((FeatureLayer) e.getValue()).setStyle(MapColorizer.getDefaultlayerStyle(e.getValue())));
        }
    }

    public void mapLayerRemoved(Layer layer) {
        activeLayers.remove(layer.getFeatureSource().getName().toString());
        ComboBoxModel cbm = new DefaultComboBoxModel(activeLayers.keySet().toArray());
        featureTypeCBox.setModel(cbm);
        filterFeatures();
    }

    public void mapLayerAdded(Layer layer) {
        activeLayers.put(layer.getFeatureSource().getName().toString(), layer);
        ComboBoxModel cbm = new DefaultComboBoxModel(activeLayers.keySet().toArray());
        featureTypeCBox.setModel(cbm);
        filterFeatures();
    }
}