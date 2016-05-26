package gis.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.FeatureCollection;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

public class FeatureTableModel extends AbstractTableModel {

    private Collection<PropertyDescriptor> descriptors;
    private List<Object[]> valuesCache = new ArrayList<>();
    private List<Feature> featureCache = new ArrayList<>();
    private TableFiller tableFiller;
    private boolean error;

    public FeatureTableModel(FeatureCollection features){
        this.descriptors = features.getSchema().getDescriptors();
        this.tableFiller = new TableFiller(features);
        this.tableFiller.execute();
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0)
            return "Feature ID";

        return descriptors.stream().collect(Collectors.toList()).get(column - 1).getName().toString();
    }

    public int getColumnCount() {
        if (error) return 1;

        return descriptors.size() + 1;
    }

    public int getRowCount() {
        if (error) return 1;

        return valuesCache.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < valuesCache.size())
            return valuesCache.get(rowIndex)[columnIndex];

        return null;
    }

    public Feature getFeatureAt(int rowIndex){
        if (rowIndex < featureCache.size())
            return featureCache.get(rowIndex);

        return null;
    }

    public void dispose() {
        tableFiller.cancel(false);
    }

    private class TableFiller extends SwingWorker<List<Object[]>, Object[]> {
        private FeatureCollection features;

        TableFiller(FeatureCollection features) {
            this.features = features;
        }

        public List<Object[]> doInBackground() {
            List<Object[]> list = new ArrayList<>();

            final NullProgressListener listener = new NullProgressListener();
            try {
                features.accepts(feature -> {
                    ArrayList<Object> row = new ArrayList<>();

                    for (PropertyDescriptor desc : descriptors) {
                        Name name = desc.getName();
                        Object value = feature.getProperty(name).getValue();

                        if (value != null) {
                            if (value instanceof Geometry) {
                                row.add(value.getClass().getSimpleName());
                            } else {
                                row.add(value);
                            }
                        } else {
                            row.add(null);
                        }
                    }

                    row.add(0, feature.getIdentifier().getID());

                    publish(row.toArray());
                    featureCache.add(feature);

                    if(isCancelled()) listener.setCanceled(true);

                }, listener);
            } catch (Exception e) {
                error = true;
            }

            return list;
        }

        @Override
        protected void process(List<Object[]> chunks) {
            int from = valuesCache.size();
            valuesCache.addAll(chunks);
            fireTableRowsInserted(from, valuesCache.size());
        }
    }

}
