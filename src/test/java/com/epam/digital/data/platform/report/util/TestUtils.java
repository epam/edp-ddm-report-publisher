package com.epam.digital.data.platform.report.util;

import com.epam.digital.data.platform.report.model.Context;
import java.util.HashMap;

public class TestUtils {

  public static Context mockContext() {
    var context = new Context();
    var mappedIds = new HashMap<Integer, Integer>();

    mappedIds.put(0, 1);
    context.setDataSourceId(1);
    context.setMappedIds(mappedIds);

    return context;
  }
}
