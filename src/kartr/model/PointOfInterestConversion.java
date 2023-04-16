package kartr.model;

import jakarta.json.*;
import jakarta.json.stream.*;
import java.io.*;
import java.util.*;

public class PointOfInterestConversion {
  public static JsonArray convertToJson(List<PointOfInterest> source, int accId) {
    JsonBuilderFactory factory = Json.createBuilderFactory(null);
    JsonArrayBuilder pois = factory.createArrayBuilder();
    for (PointOfInterest poi : source) {
      JsonObjectBuilder poiBuilder = factory.createObjectBuilder();
      JsonArrayBuilder stories = factory.createArrayBuilder();

      poiBuilder.add("poi_id", poi.getId());
      poiBuilder.add("longitude", poi.getLongitude());
      poiBuilder.add("latitude", poi.getLatitude());
      poiBuilder.add("displayname", poi.getDisplayname());

      for (int storyId : poi.getStories()) {
        stories.add(storyId);
      }
      poiBuilder.add("stories", stories);

      if (accId == poi.getAccountId() && accId != 0) {
        poiBuilder.add("is_owner", true);
      }

      pois.add(poiBuilder);
    }
    return pois.build();
  }

  public static JsonObject convertToJsonWithStatus(
      List<PointOfInterest> source, String status, int accId) {
    // i'm not using the same "method-structure" as in StoryConversion (using
    // a prepareJson method returning a ObjectBuilder and adding the
    // status here, as the list of pois is an Array and not an Object)
    JsonBuilderFactory factory = Json.createBuilderFactory(null);
    JsonObjectBuilder res = factory.createObjectBuilder();
    res.add("status", status);
    res.add("pois", convertToJson(source, accId));

    return res.build();
  }
}
