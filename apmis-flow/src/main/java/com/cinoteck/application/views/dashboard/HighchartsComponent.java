package com.cinoteck.application.views.dashboard;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

@Tag("highcharts-component")
//@NpmPackage(value = "highcharts", version = "11.0.0")
@JavaScript("https://code.highcharts.com/highcharts.js")
@CssImport(value = "/styles/highcharts.css")
@JsModule("/highcharts-component.js")
public class HighchartsComponent extends Component {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6518662193659442084L;

	public HighchartsComponent() {
        initializeChart();
    }

    private void initializeChart() {
        // Create a new Highcharts chart configuration
        JsonObject chartConfig = Json.createObject();

        JsonObject chart = Json.createObject();
        chart.put("type", "bar");
        chartConfig.put("chart", chart);

        // Set chart data
        JsonArray categories = Json.createArray();
        categories.set(0, "Category 1");
        categories.set(1, "Category 2");
        categories.set(2, "Category 3");

        JsonArray seriesData = Json.createArray();
        seriesData.set(0, 10);
        seriesData.set(1, 5);
        seriesData.set(2, 15);

        JsonObject series = Json.createObject();
        series.put("name", "Series 1");
        series.put("data", seriesData);

        JsonArray seriesArray = Json.createArray();
        seriesArray.set(0, series);

        JsonObject data = Json.createObject();
        data.put("categories", categories);
        data.put("series", seriesArray);

        chartConfig.put("data", data);

        // Pass the chart configuration as JSON string to the setOptions method
        setOptions(chartConfig.toJson());
    }

    public void setOptions(String optionsJson) {
        getElement().callJsFunction("setOptions", optionsJson);
    }
}
