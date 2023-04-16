package kartr.model;

import jakarta.json.*;
import jakarta.json.stream.*;
import java.io.*;
import java.util.*;

public class StoryConversion {
  public static JsonObject convertToJson(Story source, boolean isOwner) {
    return prepareJson(source, isOwner).build();
  }

  private static JsonObjectBuilder prepareJson(Story source, boolean isOwner) {
    // does not include all information from Story object. omitting accountId
    JsonBuilderFactory factory = Json.createBuilderFactory(null);
    JsonObjectBuilder story = factory.createObjectBuilder();
    JsonArrayBuilder files = factory.createArrayBuilder();

    for (String file : source.getFiles()) {
      files.add(file);
    }

    story.add("story_id", source.getId());
    story.add("headline", source.getHeadline());
    story.add("content", source.getContent());
    story.add("poi_id", source.getPoiId());
    story.add("account_displayname", source.getAccountDisplayname());
    story.add("files", files);
    if (isOwner) {
      story.add("is_owner", true);
    }

    return story;
  }

  public static JsonObject convertToJsonWithStatus(Story source, String status, boolean isOwner) {
    JsonObjectBuilder story = prepareJson(source, isOwner);
    story.add("status", status);
    return story.build();
  }
}
