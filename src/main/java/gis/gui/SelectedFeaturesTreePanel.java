package gis.gui;

import com.vividsolutions.jts.geom.Geometry;
import gis.models.FeatureWithProperties;
import gis.models.SelectedFeaturesResult;
import org.geotools.map.Layer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Map;

public class SelectedFeaturesTreePanel extends JFrame implements TreeSelectionListener {

    private JTextPane textPane;
    private JTree tree;

    public SelectedFeaturesTreePanel() {
        super("Selected features");

        setLayout(new GridLayout(1,0));

        tree = new JTree(new DefaultMutableTreeNode());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.setRootVisible(false);

        JScrollPane treeView = new JScrollPane(tree);

        textPane = new JTextPane();
        textPane.setEditable(false);

        JScrollPane textView = new JScrollPane(textPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(textView);

        splitPane.setDividerLocation(300);
        splitPane.setPreferredSize(new Dimension(800, 600));

        add(splitPane);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void valueChanged(TreeSelectionEvent e) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node == null) return;

        Object nodeObject = node.getUserObject();

        if (node.isLeaf()) {
            FeatureWithProperties featureWithProperties = (FeatureWithProperties) nodeObject;
            StringBuilder stringBuilder = new StringBuilder();

            for(Map.Entry<String, Object> entry : featureWithProperties.getProperties().entrySet()){
                if (entry.getValue() instanceof Geometry){
                    stringBuilder.append(entry.getKey() + " : " + entry.getValue().getClass().getSimpleName() + "\n");
                } else {
                    stringBuilder.append(entry.getKey() + " : " + entry.getValue() + "\n");
                }
            }

            changeText(stringBuilder.toString());
        } else {
            changeText("Select feature to get attribute information");
        }
    }

    public void populateTree(Map<Layer, SelectedFeaturesResult> selectedFeaturesResults){
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        root.removeAllChildren();

        changeText("Select feature to get attribute information");

        for(Map.Entry<Layer, SelectedFeaturesResult> entry : selectedFeaturesResults.entrySet()){
            if (entry.getValue().getSelectedFeatures().size() == 0) continue;

            LayerTreeNode layerNode = new LayerTreeNode(entry.getKey());
            root.add(layerNode);

            for(FeatureWithProperties featureWithProperties : entry.getValue().getSelectedFeatures()){
                layerNode.add(new DefaultMutableTreeNode(featureWithProperties));
            }
        }

        model.reload(root);
    }

    private void changeText(String text) {
        textPane.setText(text);
    }
}

