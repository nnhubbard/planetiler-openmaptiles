package org.openmaptiles.addons;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;
import org.openmaptiles.Layer;
import org.openmaptiles.OpenMapTilesProfile;

public class Power implements Layer, OpenMapTilesProfile.OsmAllProcessor {

  private static final String LAYER_NAME = "power";

  @Override
  public String name() {
    return LAYER_NAME;
  }

  @Override
  public void processAllOsm(SourceFeature feature, FeatureCollector features) {
    if (feature.canBeLine() && feature.hasTag("power", "line")) {
      features.line("power")
        .setBufferPixels(4)
        .setMinZoom(12)
        .setAttr("class", "line");
    } else if (feature.canBePolygon() && feature.hasTag("power", "substation", "plant", "generator", "switchgear")) {
      features.polygon("power")
        .setBufferPixels(4)
        .setMinZoom(12)
        .setAttr("class", feature.getTag("power"));
    } else if (feature.hasTag("power", "tower")) {
      features.point("power")
        .setBufferPixels(4)
        .setMinZoom(12)
        .setAttr("class", feature.getTag("power"));
    }
  }
}
