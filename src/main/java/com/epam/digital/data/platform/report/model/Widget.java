package com.epam.digital.data.platform.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import java.util.Objects;

public class Widget {

    private String text;
    private Object options;
    private Integer width;
    private Integer dashboardId;
    private Integer visualizationId;
    @JsonProperty(access = Access.WRITE_ONLY)
    private Visualization visualization;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getOptions() {
        return options;
    }

    public void setOptions(Object options) {
        this.options = options;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(Integer dashboardId) {
        this.dashboardId = dashboardId;
    }

    public Integer getVisualizationId() {
        return visualizationId;
    }

    public void setVisualizationId(Integer visualizationId) {
        this.visualizationId = visualizationId;
    }

    public Visualization getVisualization() {
        return visualization;
    }

    public void setVisualization(
        Visualization visualization) {
        this.visualization = visualization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Widget widget = (Widget) o;
        return Objects.equals(text, widget.text) && Objects
            .equals(options, widget.options) && Objects.equals(width, widget.width)
            && Objects.equals(dashboardId, widget.dashboardId) && Objects
            .equals(visualizationId, widget.visualizationId) && Objects
            .equals(visualization, widget.visualization);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, options, width, dashboardId, visualizationId, visualization);
    }
}
