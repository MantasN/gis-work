package gis.utils;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.opengis.feature.Feature;

import java.util.List;

public class Calculations {
    public static Envelope2D getFeaturesBounds(List<Feature> featureList) {
        double minX = -1;
        double minY = -1;
        double maxX = -1;
        double maxY = -1;

        for(Feature feature : featureList){
            double featureMinX = feature.getBounds().getMinX();
            double featureMinY = feature.getBounds().getMinY();
            double featureMaxX = feature.getBounds().getMaxX();
            double featureMaxY = feature.getBounds().getMaxY();

            if(featureMinX < minX || minX == -1) minX = featureMinX;
            if(featureMinY < minY || minY == -1) minY = featureMinY;
            if(featureMaxX > maxX || maxX == -1) maxX = featureMaxX;
            if(featureMaxY > maxY || maxY == -1) maxY = featureMaxY;
        }

        if(minX != -1 && minY != -1 && maxX != -1 && maxY != -1){
            Envelope2D env = new Envelope2D();
            env.setFrameFromDiagonal(new DirectPosition2D(minX, minY), new DirectPosition2D(maxX, maxY));
            return env;
        }

        return null;
    }

    public static Envelope2D getFeatureBounds(Feature feature) {
        double featureMinX = feature.getBounds().getMinX();
        double featureMinY = feature.getBounds().getMinY();
        double featureMaxX = feature.getBounds().getMaxX();
        double featureMaxY = feature.getBounds().getMaxY();

        Envelope2D env = new Envelope2D();
        env.setFrameFromDiagonal(new DirectPosition2D(featureMinX, featureMinY), new DirectPosition2D(featureMaxX, featureMaxY));
        return env;
    }
}
