package com.epam.digital.data.platform.report.model;

import java.util.Map;

public class Context {
    private int dataSourceId;
    private Map<Integer, Integer> mappedIds;

    public int getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public Map<Integer, Integer> getMappedIds() {
        return mappedIds;
    }

    public void setMappedIds(Map<Integer, Integer> mappedIds) {
        this.mappedIds = mappedIds;
    }
}
