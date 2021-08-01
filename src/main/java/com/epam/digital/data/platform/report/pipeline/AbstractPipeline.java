package com.epam.digital.data.platform.report.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.json.JsonParseException;
import com.epam.digital.data.platform.report.model.Context;

public abstract class AbstractPipeline {

  protected final ObjectMapper objectMapper;

  protected AbstractPipeline(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public abstract void process(List<File> files, Context context);

  public abstract boolean isApplicable(List<File> files);

  protected <T> T readValue(File file, Class<T> clazz) {
    T object;

    try {
      object = objectMapper.readValue(file, clazz);
    } catch (IOException e) {
      throw new JsonParseException(e);
    }

    return object;
  }

  protected <T> List<T> readList(File file, Class<T> clazz) {
    List<T> list = new ArrayList<>();

    try {
      for (JsonNode jsonNode : objectMapper.readTree(file)) {
        list.add(objectMapper.treeToValue(jsonNode, clazz));
      }
    } catch (IOException e) {
      throw new JsonParseException(e);
    }

    return list;
  }

  protected <T> List<T> readPage(File file, Class<T> clazz) {
    List<T> list = new ArrayList<>();
    JsonNode tree;

    try {
      tree = objectMapper.readTree(file);
      for (JsonNode jsonNode : tree.get("results")) {
        list.add(objectMapper.treeToValue(jsonNode, clazz));
      }
    } catch (IOException e) {
      throw new JsonParseException(e);
    }

    return list;
  }
}
