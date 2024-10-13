package org.openmaptiles.addons;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;
import org.openmaptiles.Layer;
import org.openmaptiles.OpenMapTilesProfile;

public class Skiing implements Layer, OpenMapTilesProfile.OsmAllProcessor {

  private static final String LAYER_NAME = "skiing";

  @Override
  public String name() {
    return LAYER_NAME;
  }

  @Override
  public void processAllOsm(SourceFeature feature, FeatureCollector features) {

    if (feature.canBeLine()) {

      if (feature.hasTag("aerialway", "cable_car", "gondola", "chair_lift", "mixed_lift", "drag_lift", "t-bar", "j-bar",
        "platter", "magic_carpet")) {

        features.line("skiing")
          .setBufferPixels(4)
          .setMinZoom(12)
          .setAttr("class", feature.getTag("aerialway"));

      }
      if (feature.hasTag("piste:type", "downhill", "nordic", "skitour", "sled", "hike", "sleigh", "ice_skate",
        "snow_park", "playground", "ski_jump")) {

        features.line("skiing")
          .setBufferPixels(4)
          .setMinZoom(12)
          .setAttr("class", "piste")
          .setAttr("pistetype", feature.getTag("piste:type"));

      }
      if (feature.hasTag("piste:difficulty", "novice", "easy", "intermediate", "advanced", "expert", "freeride",
        "extreme")) {

        features.line("skiing")
          .setBufferPixels(4)
          .setMinZoom(12)
          .setAttr("class", "piste")
          .setAttr("pistedifficulty", feature.getTag("piste:difficulty"));

      }

    } else if (feature.hasTag("aerialway", "station", "pylon")) {

      features.point("skiing")
        .setBufferPixels(4)
        .setMinZoom(12)
        .setAttr("class", feature.getTag("aerialway"));

    }

  }

}
