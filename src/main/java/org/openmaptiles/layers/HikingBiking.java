package org.openmaptiles.layers;

import static com.onthegomap.planetiler.util.MemoryEstimator.CLASS_HEADER_BYTES;
import static com.onthegomap.planetiler.util.MemoryEstimator.POINTER_BYTES;
import static com.onthegomap.planetiler.util.MemoryEstimator.estimateSize;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmRelationInfo;
import com.onthegomap.planetiler.stats.Stats;
import com.onthegomap.planetiler.util.MemoryEstimator;
import com.onthegomap.planetiler.util.Translations;
import java.util.List;
import org.openmaptiles.OpenMapTilesProfile;
import org.openmaptiles.generated.OpenMapTilesSchema;

public class HikingBiking implements OpenMapTilesSchema.HikingBiking, OpenMapTilesProfile.OsmAllProcessor,
  ForwardingProfile.OsmRelationPreprocessor {

  private static final String LAYER_NAME = "hiking_biking";
  private final Translations translations;

  @Override
  public String name() {
    return LAYER_NAME;
  }

  public HikingBiking(Translations translations, PlanetilerConfig config, Stats stats) {
    //this.classLookup = FieldMappings.Class.index();
    this.translations = translations;
  }

  @Override
  public List<OsmRelationInfo> preprocessOsmRelation(OsmElement.Relation relation) {

    if (relation.hasTag("route") && (relation.hasTag("route", "hiking") || relation.hasTag("route", "biking"))) {

      String osmcsymbol = relation.getString("osmc:symbol");
      String route = relation.getString("route");
      String network = relation.getString("network");

      return List.of(new RouteRelation(
        relation.id(),
        osmcsymbol,
        route,
        network
      ));

    }
    return null;
  }

  @Override
  public void processAllOsm(SourceFeature feature, FeatureCollector features) {

    if (!feature.canBeLine()) {
      return;
    }

    var relationInfos = feature.relationInfo(RouteRelation.class);
    if (!relationInfos.isEmpty()) {

      String osmcsymbol = null;
      String route = null;
      String network = null;

      for (var info : relationInfos) {

        RouteRelation rel = info.relation();
        osmcsymbol = rel.osmcsymbol;
        route = rel.route;
        network = rel.network;

      }

      var line = features.line(LAYER_NAME);

      String wayColor = null;
      String background = null;
      String foreground = null;
      String foreground2 = null;
      String text = null;
      String textColor = null;
      if (osmcsymbol != null) {

        if (osmcsymbol.contains(":")) {

          String[] parts = osmcsymbol.split(":");
          if (parts.length > 0 && parts[0] != null) {
            wayColor = parts[0];
            line.setAttr(Fields.OSMCSYMBOL_WAY_COLOR, wayColor);
          }
          if (parts.length > 1 && parts[1] != null) {
            background = parts[1];
            line.setAttr(Fields.OSMCSYMBOL_BACKGROUND, background);
          }
          if (parts.length > 2 && parts[2] != null) {
            foreground = parts[2];
            line.setAttr(Fields.OSMCSYMBOL_FOREGROUND, foreground);
          }
          if (parts.length > 3 && parts[3] != null) {
            foreground2 = parts[3];
            line.setAttr(Fields.OSMCSYMBOL_FOREGROUND2, foreground2);
          }
          if (parts.length > 4 && parts[4] != null) {
            text = parts[4];
            line.setAttr(Fields.OSMCSYMBOL_TEXT, text);
          }
          if (parts.length > 5 && parts[5] != null) {
            textColor = parts[5];
            line.setAttr(Fields.OSMCSYMBOL_TEXT_COLOR, textColor);
          }

          line.setAttr(Fields.OSMCSYMBOL, osmcsymbol);

        }

      }

      if (route != null) {

        line.setAttr(Fields.ROUTE, route);

      }
      if (network != null) {

        line.setAttr(Fields.NETWORK, network);

      }


      try {

        var length = feature.length() * GeoUtils.metersPerPixelAtEquator(0) * 256;
        line.setAttr(Fields.LENGTH_MI, lengthFormattedText(length, true));
        line.setAttr(Fields.LENGTH_KM, lengthFormattedText(length, false));
        line.setAttr(Fields.LENGTH_M, metersRounded(length));

      } catch (GeometryException e) {
        //e.log(stats, "waterway_decode", "Unable to get waterway length for " + feature.id());
      }

      line.setBufferPixels(BUFFER_SIZE)
        .setMinZoom(14);

    } else if (feature.hasTag("highway", "path", "cycleway", "bridleway")) {

      var line = features.line(LAYER_NAME);


      try {

        var length = feature.length() * GeoUtils.metersPerPixelAtEquator(0) * 256;
        line.setAttr(Fields.LENGTH_MI, lengthFormattedText(length, true));
        line.setAttr(Fields.LENGTH_KM, lengthFormattedText(length, false));
        line.setAttr(Fields.LENGTH_M, metersRounded(length));
        line.setBufferPixels(BUFFER_SIZE)
          .setMinZoom(14);

      } catch (GeometryException e) {
        //e.log(stats, "waterway_decode", "Unable to get waterway length for " + feature.id());
      }

    }

  }

  private String lengthFormattedText(double length, Boolean metric) {

    if (metric) {

      double converted = length / 1000;
      double result = Math.round(converted * 100.0) / 100.0;// keep two decimal points
      return String.valueOf(result);

    }

    double converted = length / 1609.344;
    double result = Math.round(converted * 100.0) / 100.0;// keep two decimal points
    return String.valueOf(result);

  }

  private String metersRounded(double length) {

    return String.valueOf(Math.round(length));

  }

  private record RouteRelation(
    long id,
    String osmcsymbol,
    String route,
    String network
  ) implements OsmRelationInfo {

    @Override
    public long estimateMemoryUsageBytes() {
      return CLASS_HEADER_BYTES + MemoryEstimator.estimateSizeLong(id) + estimateSize(osmcsymbol) + POINTER_BYTES +
        estimateSize(route) + POINTER_BYTES + estimateSize(network) +
        POINTER_BYTES;
    }
  }

}
