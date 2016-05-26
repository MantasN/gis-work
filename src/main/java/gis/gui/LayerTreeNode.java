package gis.gui;

import org.geotools.map.Layer;

import javax.swing.tree.DefaultMutableTreeNode;

public class LayerTreeNode extends DefaultMutableTreeNode
{
    public LayerTreeNode(Layer layer)
    {
        super(layer);
    }

    @Override
    public String toString()
    {
        Layer layer = (Layer) userObject;

        if(layer.getTitle() != null) return layer.getTitle();

        return layer.getFeatureSource().getName().toString();
    }
}