package com.cinoteck.application.views.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.CampaignJurisdictionLevel;
import de.symeda.sormas.api.campaign.data.translation.TranslationElement;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDataDto;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDefinitionDto;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramSeries;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramTranslations;
import de.symeda.sormas.api.campaign.diagram.DiagramType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

import com.cinoteck.application.UserProvider;

public class CampaignDashboardDiagramComponent extends Div {

	/**
	 * 
	 */
	private static final long serialVersionUID = 540884288695393865L;
	private static final double MAX_YAXIS_VALUE_DYNAMIC_CHART_HEIGHT_LOWER_BOUND = 70.0;
	private static final double MAX_YAXIS_VALUE_DYNAMIC_CHART_HEIGHT_UPPER_BOUND = 100.0;

	private final CampaignDiagramDefinitionDto diagramDefinition;

	private final Map<String, Map<Object, CampaignDiagramDataDto>> diagramDataBySeriesAndXAxis = new HashMap<>();
	private final Map<Object, String> xAxisInfo;
	private final Map<CampaignDashboardTotalsReference, Double> totalValuesMap;
	private boolean totalValuesWithoutStacks;
	private boolean showPercentages;
	private boolean showAsColumnChart;
	private boolean showDataLabels = false;
	private boolean rotateDataLabels = true;
	private boolean ignoreTotalsError = false;
	private boolean pieChart = false;
	private boolean cardChart = false;
	private String chartType;
	private String secondaryChartType;
	private Map mapSeries = new HashMap();

	private BarChartCardComponent chartComponent;
	// private NumberCardComponent cardComponent;
	private ProgressBarCardComponent percentageCardComponent;
	private String randomx = "";

	public CampaignDashboardDiagramComponent(CampaignDiagramDefinitionDto diagramDefinition,
			List<CampaignDiagramDataDto> diagramDataList, Map<CampaignDashboardTotalsReference, Double> totalValuesMap,
			CampaignJurisdictionLevel campaignJurisdictionLevelGroupBy, int cardWidth) {
		this.diagramDefinition = diagramDefinition;
		this.showPercentages = diagramDefinition.isPercentageDefault();
		this.totalValuesMap = totalValuesMap;

		this.secondaryChartType = diagramDefinition.getDiagramType().name().toLowerCase();

		this.chartType = secondaryChartType.equalsIgnoreCase(DiagramType.DOUGHNUT.toString()) ? "pie"
				: secondaryChartType;
		pieChart = (chartType.equalsIgnoreCase(DiagramType.PIE.toString())) ? true : false;

		if (this.totalValuesMap != null && this.totalValuesMap.keySet().stream().noneMatch(r -> r.getStack() != null)) {
			totalValuesWithoutStacks = true;
		}

		showAsColumnChart = DiagramType.COLUMN == diagramDefinition.getDiagramType();

		setId(randomx);
		removeClassNames("col-lg-12", "col-lg-11", "col-lg-10", "col-lg-9", "col-lg-8", "col-lg-7", "col-lg-6",
				"col-lg-5", "col-lg-4", "col-lg-3", "col-lg-2", "col-lg-1");

		getStyle().set("padding-right", "1px!important");
		getStyle().set("padding-left", "10px!important");

		addClassName("col-md-12");
		addClassName("col-lg-" + cardWidth);

		final String chartrandom = generateShortUUID();
		randomx = chartrandom;

		// Notification.show(randomx);
		final Map<Object, String> axisInfo = new HashMap<>();
		StringBuilder mapData = new StringBuilder(" ");
		for (CampaignDiagramDataDto diagramData : diagramDataList) {
			final Object groupingKey = diagramData.getGroupingKey();
			if (!axisInfo.containsKey(groupingKey)) {

				axisInfo.put(groupingKey, diagramData.getGroupingCaption());
   
				if (chartType.equalsIgnoreCase(DiagramType.MAP.toString())) {
					String mapHascCode = "";
					String val = campaignJurisdictionLevelGroupBy+"";
					String val_ = CampaignJurisdictionLevel.REGION.toString();
					if (val.equalsIgnoreCase("Region")) {
						mapHascCode = FacadeProvider.getRegionFacade()
								.getMapDiagramRegionHascByUuid(groupingKey.toString());
					} else {
						mapHascCode = FacadeProvider.getDistrictFacade()
								.getMapDiagramDistrictHascByUuid(groupingKey.toString());
					}
					// ['AFG_1115', 10], ['AFG_617', 100], ['AFG_1203', 45]\n"
					if (mapHascCode != null && !mapHascCode.isEmpty()) {
						
						mapData.append("['" + mapHascCode + "', " + diagramData.getValueSum() + "], ");
						
					}
					
				}
			}
		

			String seriesKey = diagramData.getFormId() + diagramData.getFieldId();
			if (!diagramDataBySeriesAndXAxis.containsKey(seriesKey)) {
				diagramDataBySeriesAndXAxis.put(seriesKey, new HashMap<>());
			}
			Map<Object, CampaignDiagramDataDto> objectCampaignDiagramDataDtoMap = diagramDataBySeriesAndXAxis
					.get(seriesKey);
			if (!pieChart) {
				if (objectCampaignDiagramDataDtoMap.containsKey(groupingKey)) {
					throw new RuntimeException("Campaign diagram data map already contains grouping");
				}
			}
			objectCampaignDiagramDataDtoMap.put(groupingKey, diagramData);
		}
		
		xAxisInfo = axisInfo.entrySet().stream()
				.sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getValue(), o2.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		
		if (chartType.equalsIgnoreCase(DiagramType.MAP.toString())) {
		buildMapChart(getDiagramCaption(), mapData.deleteCharAt(mapData.length() - 1)) ;
		} else {
			buildDiagramChart(getDiagramCaption(), campaignJurisdictionLevelGroupBy);
		}

	}
	
	private void buildMapChart(String maptitle, StringBuilder mapData_) {
		String mapConfigurator = " "
				+ "const all_data = [\n"
				//+ "    ['11', 10],  ['32', 100], ['15', 45], ['19', 77], ['AFG_503', 10],  ['AFG_505', 100], ['AFG_3204', 45], ['AFG_502', 77], ['AFG_2603', 10],  ['AFG_3208', 20], ['AFG_2611', 45], ['AFG_3111', 9],  ['AFG_3205', 77], ['AFG_3110', 25] "
				
+ mapData_+"\n"
				+ " ];\n"
				+ "const drilldown = async function (e) {\n"
				+ "    if (!e.seriesOptions) {\n"
				+ "        const chart = this,\n"
				+ "            mapKey = `${e.point.drilldown}`;\n"
				+ "\n"
				+ "        // Handle error, the timeout is cleared on success\n"
				+ "        let fail = setTimeout(() => {\n"
				+ "            if (!Highcharts.maps[mapKey]) {\n"
				+ "                chart.showLoading(`\n"
				+ "                    <i class=\"icon-frown\"></i>\n"
				+ "                    Failed loading ${e.point.name}\n"
				+ "                `);\n"
				+ "                fail = setTimeout(() => {\n"
				+ "                    chart.hideLoading();\n"
				+ "                }, 1000);\n"
				+ "            }\n"
				+ "        }, 3000);\n"
				+ "\n"
				+ "        // Show the Font Awesome spinner\n"
				+ "        chart.showLoading('<i class=\"icon-spinner icon-spin icon-3x\"></i>');\n"
				+ "\n"
				+ "        // Load the drilldown map\n"
				+ "        const topology = await fetch(\n"
				+ "            `maps/${mapKey}.json`\n"
				+ "        ).then(response => response.json());\n"
				+ "\n"
				+ "        const data = Highcharts.geojson(topology);\n"
				+ "\n"
				+ "        // Set a non-random bogus value\n"
				+ "        data.forEach((d, i) => {\n"
				+ "            \n"
				+ "            const targetItem = all_data.find(item => item[0] === d.properties['hasc']);\n"
				+ "            console.log('value ===== '+typeof targetItem );\n"
				+ "            if (typeof targetItem !== 'undefined') {\n"
				+ "            dds = targetItem.toString();\n"
				+ "                const finals = dds.substring(dds.indexOf(',') + 1);\n"
				+ "                //dds.substring(0, dds.indexOf(','));\n"
				+ "\n"
				+ "            console.log(finals);\n"
				+ "\n"
				+ "                const decimalNumber = parseInt(finals); \n"
				+ "                d.value = decimalNumber;\n"
				+ "            }else{\n"
				+ "                d.value = 0;\n"
				+ "            }\n"
				+ "    });\n"
				+ "\n"
				+ "        \n"
				+ "        console.log(mapKey);\n"
				+ "         console.log('____________________________');\n"
				+ "\n"
				+ "        // Apply the recommended map view if any\n"
				+ "        // chart.mapView.update(\n"
				+ "        //     Highcharts.merge(\n"
				+ "        //         {\n"
				+ "        //             insets: undefined,\n"
				+ "        //             padding: 0\n"
				+ "        //         },\n"
				+ "        //         topology.objects.default['hc-recommended-mapview']\n"
				+ "        //     )\n"
				+ "        // );\n"
				+ "\n"
				+ "        // Hide loading and add series\n"
				+ "        chart.hideLoading();\n"
				+ "        clearTimeout(fail);\n"
				+ "        chart.addSeriesAsDrilldown(e.point, {\n"
				+ "            name: e.point.name,\n"
				+ "            data,\n"
				+ "            dataLabels: {\n"
				+ "                enabled: true,\n"
				+ "                format: '{point.name}'\n"
				+ "            }\n"
				+ "        });\n"
				+ "    }\n"
				+ "};\n"
				+ "\n"
				+ "// On drill up, reset to the top-level map view\n"
				+ "const afterDrillUp = function (e) {\n"
				+ "    if (e.seriesOptions.custom && e.seriesOptions.custom.mapView) {\n"
				+ "        e.target.mapView.update(\n"
				+ "            Highcharts.merge(\n"
				+ "                { insets: undefined },\n"
				+ "                e.seriesOptions.custom.mapView\n"
				+ "            ),\n"
				+ "            false\n"
				+ "        );\n"
				+ "    }\n"
				+ "};\n"
				+ "\n"
				+ "(async () => {\n"
				+ "\n"
				+ "    const topology = await fetch(\n"
				+ "        'maps/afgallprovince.json'\n"
				+ "    ).then(response => response.json());\n"
				+ "\n"
				+ "    const data = Highcharts.geojson(topology);\n"
				+ "\n"
				+ "    // const mapView = topology.objects.default['hc-recommended-mapview'];\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "    // Set drilldown pointers\n"
				+ "    data.forEach((d, i) => {\n"
				+ "        d.drilldown = d.properties['province'];\n"
				+ "        \n"
				+ "        const targetItem = all_data.find(item => item[0] === d.properties['hasc']);\n"
				+ "        if (typeof targetItem !== 'undefined') {\n"
				+ "        const decimalNumber = parseInt(targetItem); \n"
				+ "        d.value = decimalNumber;\n"
				+ "}else{\n"
				+ "    d.value = 0;\n"
				+ "}\n"
				+ "    });\n"
				+ "\n"
				+ "    // Instantiate the map\n"
				+ "    Highcharts.mapChart('"+randomx+"', {\n"
				+ "        chart: {\n"
				+ "            events: {\n"
				+ "                drilldown,\n"
				+ "                afterDrillUp\n"
				+ "            }\n"
				+ "        },\n"
				+ "\n"
				+ "        title: {\n"
				+ "            text: '" + StringEscapeUtils.escapeEcmaScript(maptitle) + "', style: { fontSize: '15px' }\n"
				+ "        },\n"
				+ "\n"
				+ "        colorAxis: {\n"
				+ "            min: 0,\n"
				+ "            minColor: '#E6E7E8',\n"
				+ "            maxColor: '#005645'\n"
				+ "        },\n"
				+ "\n"
				+ "      //  mapView,\n"
				+ "\n"
				+ "        mapNavigation: {\n"
				+ "            enabled: true,\n"
				+ "            buttonOptions: {\n"
				+ "                verticalAlign: 'bottom'\n"
				+ "            }\n"
				+ "        },\n"
				+ "\n"
				+ "        plotOptions: {\n"
				+ "            map: {\n"
				+ "                states: {\n"
				+ "                    hover: {\n"
				+ "                        color: '#EEDD66'\n"
				+ "                    }\n"
				+ "                },\n"
				+ "                borderColor: '#A0A0A0'\n"
				+ "            }\n"
				+ "        },\n"
				+ "        colorAxis: {\n"
				+ "            dataClasses:[{\n"
				+ "            color: '#dc143c',\n"
				+ "            from:0,\n"
				+ "            name: 'low (0-5)',\n"
				+ "            to:5\n"
				+ "            },\n"
				+ "            {\n"
				+ "            color: '#E9AE03',\n"
				+ "            from:5,\n"
				+ "            name: 'medium (5-25)',\n"
				+ "            to:25\n"
				+ "            },\n"
				+ "            {\n"
				+ "            color: '#0ee103',\n"
				+ "            from:25,\n"
				+ "            name: 'average (25-50)',\n"
				+ "            to:50\n"
				+ "            },\n"
				+ "            {\n"
				+ "            color: '#E92C03',\n"
				+ "            from:51,\n"
				+ "            name: 'high (50-100)',\n"
				+ "            to:100\n"
				+ "            }\n"
				+ "            ]\n"
				+ "        },\n"
				+ "        series: [{\n"
				+ "            data: data,\n"
				+ "            borderColor: '#A0A0A0',\n"
				+ "            nullColor: '#E7E6E5',\n"
				+ "            name: 'AFGHANISTAN'//,\n"
				+ "            // dataLabels: {\n"
				+ "            //     enabled: true,\n"
				+ "            //     format: '{point.properties.postal-code}'\n"
				+ "            // },\n"
				+ "            // custom: {\n"
				+ "            //     mapView\n"
				+ "            // }\n"
				+ "        }],\n"
				+ "\n"
				+ "        drilldown: {\n"
				+ "            activeDataLabelStyle: {\n"
				+ "                color: '#FFFFFF',\n"
				+ "                textDecoration: 'none',\n"
				+ "                textOutline: '1px #000000'\n"
				+ "            },\n"
				+ "            breadcrumbs: {\n"
				+ "                floating: true"//,\n"
				//+ "                relativeTo: 'spacingBox'\n"
				+ "            },\n"
				+ "            drillUpButton: {\n"
				+ "                relativeTo: 'spacingBox',\n"
				+ "                position: {\n"
				+ "                    x: 0,\n"
				+ "                    y: 60\n"
				+ "                }\n"
				+ "            }\n"
				+ "        }\n"
				+ "    });\n"
				+ "\n"
				+ "})();";
				
		
		
		
		String mapConfigurator_ = ""
				+ "(async () => {\n"
				+ "\n"
				+ "    const topology = await fetch(\n"
				+ "        'ours.json'\n"
				+ "    ).then(response => response.json());\n"
				+ "    const data = [\n"
				+ mapData_+"\n"
				+ "    ];\n"
				+ "    Highcharts.mapChart('"+randomx+"', {\n"
				+ "        chart: {\n"
				+ "            map: topology\n"
				+ "        },\n"
				+ "title:{ text: '" + StringEscapeUtils.escapeEcmaScript(maptitle) + "', style: { fontSize: '15px' } },\n"
//				+ "        title: {\n"
//				+ "            text: '"+maptitle+"'\n"
//				+ "        },\n"
				+ "        mapNavigation: {\n"
				+ "            enabled: true,\n"
				+ "            buttonOptions: {\n"
				+ "                verticalAlign: 'bottom'\n"
				+ "            }\n"
				+ "        },\n"
				+ "        colorAxis: {\n"
				//TODO: implement data and datarange here.
				+ "            dataClasses:[{\n"
				+ "color: '#dc143c',\n"
				+ "from:0,\n"
				+ "name: 'low',\n"
				+ "to:50\n"
				+ "},\n"
				+ "{\n"
				+ "color: '#00ff00',\n"
				+ "from:51,\n"
				+ "name: 'high',\n"
				+ "to:100\n"
				+ "}\n"
				+ "]\n"
				+ "        },\n"
				+ "        series: [{\n"
				+ "         \n"
				+ "            data: data,\n"
				+ "            name: 'Name of Analysis',\n"
				+ "            states: {\n"
				+ "                hover: {\n"
				+ "                    color: '#BADA55'\n"
				+ "                }\n"
				+ "            }\n"
//				+ "            ,\n"
//				+ "            dataLabels: {\n"
//				+ "                enabled: true,\n"
//				+ "                format: '{point.name}'\n"
//				+ "            }\n"
				+ "        }]\n"
				+ "    });\n"
				+ "})();";
		
		chartComponent = new BarChartCardComponent(mapConfigurator, randomx, DiagramType.MAP);
		System.out.println(mapConfigurator);
		this.add(chartComponent);
	}

	@ClientCallable
	public void buildDiagramChart(String title, CampaignJurisdictionLevel campaignJurisdictionLevelGroupBy) {
		final StringBuilder hcjs = new StringBuilder();
		
		if (chartType.equalsIgnoreCase(DiagramType.CARD.toString())) {

			cardChart = true;

			appendSeries(campaignJurisdictionLevelGroupBy, hcjs);

			return;
		} else {
		//@formatter:off
		hcjs.append(""
				+ "var stuffx = ['column', 'bar', 'line', 'pie'],\n"
				+ "        counterx = 0;\n"
				+ "var "+randomx+" = new Highcharts.Chart({"
				+ "chart:{ "
				+ " renderTo: '"+randomx+"', "
				+ " type: '"+chartType+"', "
				+ " backgroundColor: 'white', "
//				+ " borderRadius: '1', "
//				+ " borderWidth: '1', "
				+ " spacing: [20, 20, 20, 20], "
				+ "},"
				+ "credits:{ enabled: false },"
				+ "exporting:{ "
				+ " enabled: true,");
		//@formatter:on

			hcjs.append(" menuItemDefinitions: { switchChart1: {\n" 
					+ "                onclick: function() {\n"
					+ " 			counterx = (counterx + 1) % stuffx.length;\n"
					+ "                    var chartType = this.options.chart.type;\n" 
					+ "                    this.update({\n" 
					+ "                        chart: {\n"
					+ "                            type: 'bar'\n" 
					+ "                        }\n"
					+ "                    })\n" 
					+ "                },\n"
					+ "                text: 'Show as Bar'\n"
					+ "            },"
					+ "\n"
					+ "switchChart2: {\n" 
							+ "                onclick: function() {\n"
							+ " 			counterx = (counterx + 1) % stuffx.length;\n"
							+ "                    var chartType = this.options.chart.type;\n" 
							+ "                    this.update({\n" 
							+ "                        chart: {\n"
							+ "                            type: 'column'\n" 
							+ "                        }\n"
							+ "                    })\n" 
							+ "                },\n"
							+ "                text: 'Show as Column'\n"
							+ "            },\n"
							+ "\n"
							+ "switchChart3: {\n" 
									+ "                onclick: function() {\n"
									+ " 			counterx = (counterx + 1) % stuffx.length;\n"
									+ "                    var chartType = this.options.chart.type;\n" 
									+ "                    this.update({\n" 
									+ "                        chart: {\n"
									+ "                            type: 'line'\n" 
									+ "                        }\n"
									+ "                    })\n" 
									+ "                },\n"
									+ "                text: 'Show as Line'\n"
									+ "            }\n");

//			hcjs.append(",  toggleLabels: { onclick: function() { changeDiagramLabels_"
//					+ diagramDefinition.getDiagramId() + "(); }, text: '"
//					+ (showDataLabels ? I18nProperties.getCaption(Captions.dashboardHideDataLabels)
//							: I18nProperties.getCaption(Captions.dashboardShowDataLabels))
//					+ "' } ");
			
			//getElement().callJsFunction("changeDiagramPercentage_" + diagramDefinition.getDiagramId());
			
			
//			if (totalValuesMap != null) {
//				hcjs.append(", togglePercentages: { onclick: function() { window.changeDiagramPercentage_"
//						+ diagramDefinition.getDiagramId() + "(); }, text: '"
//						+ (showPercentages ? I18nProperties.getCaption(Captions.dashboardShowTotalValues)
//								: I18nProperties.getCaption(Captions.dashboardShowPercentageValues))
//						+ "' } ");
//			}

			hcjs.append(" }, ");

			hcjs.append(" buttons:{ contextButton:{ theme:{ fill: 'transparent' }, ").append(
					"menuItems: ['switchChart1','switchChart2','switchChart3','viewFullscreen', 'printChart', 'separator', 'downloadPNG', 'downloadJPEG', 'downloadPDF', 'downloadSVG', 'separator', 'downloadCSV', 'downloadXLS', 'viewData'");

			//hcjs.append(", 'separator', 'toggleLabels'");
			if (totalValuesMap != null) {
			//	hcjs.append(", 'togglePercentages'");
			}

			// hcjs.append(", 'toggleChartType'");
			hcjs.append("]");

			final Map<String, Long> stackMap = diagramDefinition.getCampaignDiagramSeries().stream()
					.map(CampaignDiagramSeries::getStack).filter(Objects::nonNull)
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		//@formatter:off
		final int legendMargin = stackMap.size() > 1 ? 60 : 30;
		hcjs.append("} } }," 
				+ "borderColor: 'transparent',"
				+ "legend: { backgroundColor: 'transparent', margin: " + legendMargin + " },"
				+ "colors: ['#4472C4', '#ED7D31', '#A5A5A5', '#FFC000', '#5B9BD5', '#70AD47', '#FF0000', '#6691C4','#ffba08','#519e8a','#ed254e','#39a0ed','#FF8C00','#344055','#D36135','#82d173'],"
				+ "title:{ text: '" + StringEscapeUtils.escapeEcmaScript(title) + "', style: { fontSize: '15px' } },");
		//@formatter:on

			appendAxisInformation(hcjs, stackMap, campaignJurisdictionLevelGroupBy);
			appendPlotOptions(hcjs, stackMap);
			appendSeries(campaignJurisdictionLevelGroupBy, hcjs);

			hcjs.append("});");

			//TODO This should have all the JAVASCRIPT String for the entire card.
		//	System.out.println(hcjs.toString());
			
			
			//addClassName("col-md-12");
			//addClassName("col-lg-6");
			
		//	System.out.println(hcjs.toString());	
			chartComponent = new BarChartCardComponent(hcjs.toString(), randomx, DiagramType.COLUMN);
			this.add(chartComponent);
		}
	}

	@ClientCallable
	public void greetxc(String name) {
		// System.out.println("--------------------------------------------------------------------Hi,
		// " + name);
	}

	private void appendAxisInformation(StringBuilder hcjs, Map<String, Long> stackMap,
			CampaignJurisdictionLevel campaignJurisdictionLevelGroupBy) {
		final List<Object> noPopulationDataLocations = new LinkedList<>();
		if (Objects.nonNull(totalValuesMap)) {
			for (Object key : xAxisInfo.keySet()) {
				if ((Double.valueOf(0)).equals(totalValuesMap.get(new CampaignDashboardTotalsReference(key, null)))) {
					noPopulationDataLocations.add(xAxisInfo.get(key));
				}
			}
		}
		if (pieChart) {
			hcjs.append(" exporting: {\n" 
		+ "        showTable: false," 
					+ "togglePercentages: false\n"
					+ "    }, \nxAxis: {");
		} else {
			hcjs.append("xAxis: {");

		}

		if (Objects.nonNull(diagramDefinition.getCampaignSeriesTotal())) {
			Optional<CampaignDiagramSeries> isPopulationGroupUsed = diagramDefinition.getCampaignSeriesTotal().stream()
					.filter(series -> Objects.nonNull(series.getPopulationGroup())).findFirst();
			if (showPercentages && isPopulationGroupUsed.isPresent()
					&& !CollectionUtils.isEmpty(noPopulationDataLocations)) {
				hcjs.append("title: {" + "        text:'"
						+ String.format(I18nProperties.getString(Strings.errorNoPopulationDataLocations),
								String.join(", ", noPopulationDataLocations.toString()))
						+ "' },");
			} else {
				hcjs.append("title: {" + "text:'" + campaignJurisdictionLevelGroupBy.toString() + "' },");
			}
		} else {
			hcjs.append("title: {" + "text:'" + campaignJurisdictionLevelGroupBy.toString() + "' },");
		}
		if (stackMap.size() > 1) {
			hcjs.append("opposite: true,");
		}
		hcjs.append("categories: [");
		int op = 0;
		for (String caption : xAxisInfo.values()) {

			hcjs.append("'").append(StringEscapeUtils.escapeEcmaScript(caption)).append("',");

			mapSeries.put(op++, caption);

		}
		hcjs.append("]},");
//highcharts-data-table

		//@formatter:off
		final String restrictMaxValueProperty = totalsNeedClampTo100() ? "max: 100, " : "";
		hcjs.append("yAxis: {" + restrictMaxValueProperty + "min: 0, title: { text: '"+ (showPercentages
				? I18nProperties.getCaption(Captions.dashboardProportion)
				: I18nProperties.getCaption(Captions.dashboardAggregatedNumber)) +"'}");
		if (stackMap.size() > 1) {
			hcjs.append(
					", stackLabels: {enabled: true,verticalAlign: 'bottom', allowOverlap: true, crop: false, rotation: 45, x:20,y: 20, overflow: 'none',y: 24,formatter: function() {  return this.stack;},style: {  color: 'grey'}}");
		}
		hcjs.append("},");
		//@formatter:on

	}

	private void appendSeries(CampaignJurisdictionLevel campaignJurisdictionLevelGroupBy, StringBuilder hcjs) {
		String innerS = secondaryChartType.equalsIgnoreCase(DiagramType.DOUGHNUT.toString()) ? " innerSize: '50%',"
				: "";

		if (pieChart) {
			hcjs.append("series: [{ " + innerS + " data: [");
		} else if (cardChart) {

		} else {
			hcjs.append("series: [");
		}
		for (CampaignDiagramSeries series : diagramDefinition.getCampaignDiagramSeries()) {
			String seriesKey = series.getFormId() + series.getFieldId();

			// System.out.println("card? "+cardChart+ "check
			// 1"+diagramDataBySeriesAndXAxis.containsKey(seriesKey));
			if (!diagramDataBySeriesAndXAxis.containsKey(seriesKey) && !cardChart) {
				// System.out.println("passed card checker");

				continue;
			}

			Map<Object, CampaignDiagramDataDto> seriesData = diagramDataBySeriesAndXAxis.get(seriesKey);
			if (seriesData == null && cardChart) {

				// System.out.println("passed card check 1.1.0");
				String fieldNamex = assembleFieldnameCardwithoutValue(series, seriesKey);
				// assembleFieldnameCardwithoutValue
				appendCardData(hcjs, series, seriesData, fieldNamex);
				continue;
			}

			Collection<CampaignDiagramDataDto> values = seriesData.values();
			String fieldName = assembleFieldname(values, series, seriesKey);
//			if (showPercentages) {
//				if (campaignJurisdictionLevelGroupBy == CampaignJurisdictionLevel.COMMUNITY) {
//					fieldName = I18nProperties.getString(Strings.populationDataByCommunity);
//				}
//			}

			if (chartType.equalsIgnoreCase(DiagramType.PIE.toString())) {
				// hcjs.append("data: [{");
				appendPieData(campaignJurisdictionLevelGroupBy == CampaignJurisdictionLevel.COMMUNITY, hcjs, series,
						seriesData, fieldName);
				// hcjs.append("]},");
			} else if (cardChart) {
				appendCardData(hcjs, series, seriesData, fieldName);

			} else {

				hcjs.append("{ name:'").append(StringEscapeUtils.escapeEcmaScript(fieldName)).append("', data: [");
				appendData(campaignJurisdictionLevelGroupBy == CampaignJurisdictionLevel.COMMUNITY, hcjs, series,
						seriesData);

				final String stack = series.getStack();
				final String color = series.getColor();
				if (color != null || stack != null) {
					hcjs.append("],");
					if (stack != null) {
						hcjs.append("stack:'").append(StringEscapeUtils.escapeEcmaScript(getStackCaption(stack)))
								.append("'");
						hcjs.append(color != null ? "," : "");
					}
					if (color != null) {
						hcjs.append("color:'").append(StringEscapeUtils.escapeEcmaScript(color)).append("'");
					}
					hcjs.append("},");
				} else {
					hcjs.append("]},");
				}
			}
		}

		if (chartType.equalsIgnoreCase(DiagramType.PIE.toString())) {
			hcjs.append("]},");
		}
		hcjs.append("]");
		// System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ "+hcjs);
	}

	private String assembleFieldname(final Collection<CampaignDiagramDataDto> values,
			final CampaignDiagramSeries series, final String defaultValue) {
		CampaignDiagramTranslations translations = getCampaignDiagramTranslations();
		if (translations != null && translations.getSeriesNames() != null) {
			TranslationElement seriesName = translations.getSeriesNames().stream()
					.filter(s -> s.getElementId().equalsIgnoreCase(defaultValue)).findFirst().orElse(null);
			if (seriesName != null) {
				return seriesName.getCaption();
			}
		}
		if (series.getCaption() != null && !series.getCaption().isEmpty()) {
			return series.getCaption();
		}
		Iterator<CampaignDiagramDataDto> iterator = values.iterator();
		return iterator.hasNext() ? iterator.next().getFieldCaption() : defaultValue;
	}

	private String assembleFieldnameCardwithoutValue(final CampaignDiagramSeries series, final String defaultValue) {
		CampaignDiagramTranslations translations = getCampaignDiagramTranslations();
		if (translations != null && translations.getSeriesNames() != null) {
			TranslationElement seriesName = translations.getSeriesNames().stream()
					.filter(s -> s.getElementId().equalsIgnoreCase(defaultValue)).findFirst().orElse(null);
			if (seriesName != null) {
				return seriesName.getCaption();
			}
		}
		if (series.getCaption() != null && !series.getCaption().isEmpty()) {
			return series.getCaption();
		}
		// Iterator<CampaignDiagramDataDto> iterator = values.iterator();
		return "No Caption set";
	}

	private boolean totalsNeedClampTo100() {
		if (!showPercentages || totalValuesMap == null) {
			return false;
		}
		boolean result = false;
		for (CampaignDiagramSeries series : diagramDefinition.getCampaignDiagramSeries()) {
			String seriesKey = series.getFormId() + series.getFieldId();
			if (!diagramDataBySeriesAndXAxis.containsKey(seriesKey))
				continue;
			Map<Object, CampaignDiagramDataDto> seriesData = diagramDataBySeriesAndXAxis.get(seriesKey);
			for (Object axisKey : xAxisInfo.keySet()) {
				if (seriesData.containsKey(axisKey)) {
					Double totalValue = totalValuesMap
							.get(new CampaignDashboardTotalsReference(seriesData.get(axisKey).getGroupingKey(),
									totalValuesWithoutStacks ? null : series.getStack()));
					if (totalValue != null && totalValue > 0) {
						final double originalValue = seriesData.get(axisKey).getValueSum().doubleValue() / totalValue
								* 100;
						final double scaledValue = BigDecimal.valueOf(originalValue)
								.setScale(originalValue < 2 ? 1 : 0, RoundingMode.HALF_UP).doubleValue();
						if (scaledValue > MAX_YAXIS_VALUE_DYNAMIC_CHART_HEIGHT_UPPER_BOUND) {
							return false;
						}
						result |= scaledValue > MAX_YAXIS_VALUE_DYNAMIC_CHART_HEIGHT_LOWER_BOUND;
					}
				}
			}
		}
		return result;
	}

	private void appendData(boolean isCommunityGrouping, StringBuilder hcjs, CampaignDiagramSeries series,
			Map<Object, CampaignDiagramDataDto> seriesData) {
		for (Object axisKey : xAxisInfo.keySet()) {
			if (seriesData.containsKey(axisKey)) {
				if (showPercentages && totalValuesMap != null) {
					Double totalValue = totalValuesMap
							.get(new CampaignDashboardTotalsReference(seriesData.get(axisKey).getGroupingKey(),
									totalValuesWithoutStacks ? null : series.getStack()));
					if (totalValue == null) {
						if (!isCommunityGrouping && !ignoreTotalsError) {
//							Notification.show(String.format(
//									I18nProperties.getString(Strings.errorCampaignDiagramTotalsCalculationError),
//									getDiagramCaption()));
							ignoreTotalsError = true; // only show once
						}
					} else if (totalValue > 0) {
						final double originalValue = seriesData.get(axisKey).getValueSum().doubleValue() / totalValue
								* 100;
						final double scaledValue = BigDecimal.valueOf(originalValue)
								.setScale(originalValue < 2 ? 1 : 0, RoundingMode.HALF_UP).doubleValue();
						hcjs.append(scaledValue).append(",");
					} else {
						hcjs.append("0,");
					}
				} else {
					hcjs.append(seriesData.get(axisKey).getValueSum().doubleValue()).append(",");
				}
			} else {
				hcjs.append("0,");
			}
		}
		// System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ "+hcjs);
	}

	private void appendPieData(boolean isCommunityGrouping, StringBuilder hcjs, CampaignDiagramSeries series,
			Map<Object, CampaignDiagramDataDto> seriesData, String fieldName) {

		// System.out.println(isCommunityGrouping+""+hcjs+""+

		int iii = 0;
		for (Object axisKey : xAxisInfo.keySet()) {

			// System.out.println(mapSeries.get(iii++) +" ==== "+iii+"
			// +++++++++++++++++++++++++++++++++++++ "+axisKey);
			if (seriesData.containsKey(axisKey)) {
				/*
				 * if (showPercentages && totalValuesMap != null) { Double totalValue =
				 * totalValuesMap.get( new CampaignDashboardTotalsReference(
				 * seriesData.get(axisKey).getGroupingKey(), totalValuesWithoutStacks ? null :
				 * series.getStack())); if (totalValue == null) { if (!isCommunityGrouping &&
				 * !ignoreTotalsError) { Notification.show(
				 * String.format(I18nProperties.getString(Strings.
				 * errorCampaignDiagramTotalsCalculationError), getDiagramCaption()),
				 * ERROR_MESSAGE); ignoreTotalsError = true; // only show once } } else if
				 * (totalValue > 0) { final double originalValue =
				 * seriesData.get(axisKey).getValueSum().doubleValue() / totalValue * 100; final
				 * double scaledValue = BigDecimal.valueOf(originalValue).setScale(originalValue
				 * < 2 ? 1 : 0, RoundingMode.HALF_UP).doubleValue();
				 * hcjs.append(scaledValue).append(","); } else { hcjs.append("0,"); } }
				 */
				// else {

				hcjs.append("{name: '" + seriesData.get(axisKey).getStack() + "', y: "
						+ seriesData.get(axisKey).getValueSum().doubleValue()).append("},");
				// }
			} else {
				hcjs.append("0,");
			}
		}
		mapSeries.clear();
	}

	private void appendCardData(StringBuilder hcjs, CampaignDiagramSeries series,
			Map<Object, CampaignDiagramDataDto> seriesData, String fieldName) {
		int qq = 0;

		String perce = "";
		if (showPercentages) {
			perce = "%";

		}

		if (xAxisInfo.size() > 0) {
			for (Object axisKey : xAxisInfo.keySet()) {
				if (qq > 0) {
					break;
				}

				if (seriesData.containsKey(axisKey)) {

					// System.out.println(totalValuesMap.size()+ " ______________
					// "+seriesData.size());

					if ((showPercentages && totalValuesMap != null) && cardChart) {

//						for (Object axisKeddy : totalValuesMap.keySet()) {
//							System.out.println(totalValuesMap.get(axisKey));
//						}

//						System.out.println(seriesData.get(axisKey).getValueSum() + "_____check point 1a_________"
//								+ totalValuesWithoutStacks + " 0000 " + series.getStack());
//						System.out.println(seriesData.get(axisKey).getFieldCaption());
//						System.out.println(seriesData.get(axisKey).getFormId());
						Double valxx = 0.0d;
						for (Map.Entry<CampaignDashboardTotalsReference, Double> entry : totalValuesMap.entrySet()) {
							valxx = entry.getValue();
							// System.out.println(entry.getValue()+" ____________ "+valxx);
						}

						Double totalValue = valxx;
//								totalValuesMap
//								.get(new CampaignDashboardTotalsReference(seriesData.get(axisKey).getGroupingKey(),
//										totalValuesWithoutStacks ? null : series.getStack()));

//						System.out.println(seriesData.get(axisKey).getValueSum().toString() + " ======== "
//								+ series.getStack() + " _____check point 2__++_______" + totalValue);
						if (totalValue == null) {
							// System.out.println("totalValueyyyyyyyyyyyyyyyyyyyyyy" + totalValue);
							if (!ignoreTotalsError) {
//								Notification.show(String.format(
//										I18nProperties.getString(Strings.errorCampaignDiagramTotalsCalculationError),
//										getDiagramCaption()));
								ignoreTotalsError = true; // only show once
							}
						} else if (totalValue > 0) {

							final double originalValue = seriesData.get(axisKey).getValueSum().doubleValue()
									/ totalValue * 100;
							final double scaledValue = BigDecimal.valueOf(originalValue)
									.setScale(originalValue < 2 ? 1 : 0, RoundingMode.HALF_UP).doubleValue();

							//@formatter:off
							//percentage
							hcjs.append(scaledValue);
							//@formatter:on

						} else {
							// percentage
							hcjs.append("0");
							//@formatter:on
						}
					} else if (showPercentages && totalValuesMap != null) {

//						for (Object axisKeddy : totalValuesMap.keySet()) {
//						//	System.out.println(totalValuesMap.get(axisKey));
//						}

//						System.out.println(seriesData.get(axisKey).getValueSum() + "_____check point 1a_________"
//								+ totalValuesWithoutStacks + " 0000 " + series.getStack());
//						System.out.println(seriesData.get(axisKey).getFieldCaption());
//						System.out.println(seriesData.get(axisKey).getFormId());

						Double totalValue = totalValuesMap
								.get(new CampaignDashboardTotalsReference(seriesData.get(axisKey).getGroupingKey(),
										totalValuesWithoutStacks ? null : series.getStack()));

//						System.out.println(seriesData.get(axisKey).getValueSum().toString() + " ======== "
//								+ series.getStack() + " _____check point 2__++_______" + totalValue);
						if (totalValue == null) {
							// System.out.println("totalValueyyyyyyyyyyyyyyyyyyyyyy" + totalValue);
							if (!ignoreTotalsError) {
//								Notification.show(String.format(
//										I18nProperties.getString(Strings.errorCampaignDiagramTotalsCalculationError),
//										getDiagramCaption()));
								ignoreTotalsError = true; // only show once
							}
						} else if (totalValue > 0) {

							final double originalValue = seriesData.get(axisKey).getValueSum().doubleValue()
									/ totalValue * 100;
							final double scaledValue = BigDecimal.valueOf(originalValue)
									.setScale(originalValue < 2 ? 1 : 0, RoundingMode.HALF_UP).doubleValue();

							//@formatter:off
							//percentage
							hcjs.append(scaledValue);
							//@formatter:on

						} else {
							hcjs.append("0");
							//@formatter:on
						}
					} else {

						String temNum = seriesData.get(axisKey).getValueSum().toString();
						double amount = Double.parseDouble(temNum);

						final double scaledValuex = BigDecimal.valueOf(amount)
								.setScale(amount < 2 ? 1 : 0, RoundingMode.HALF_UP).doubleValue();

						// System.out.println("+__________CARD TOTAL = +: " + scaledValuex);

						Double totalValue = 0.0;
						if (totalValuesMap != null) {

							Double valxx = 0.0d;
							for (Map.Entry<CampaignDashboardTotalsReference, Double> entry : totalValuesMap
									.entrySet()) {
								valxx = entry.getValue();
								// System.out.println(entry.getValue()+" ____Card without total________
								// "+valxx);
							}

							totalValue = valxx;

//							totalValue = totalValuesMap
//									.get(new CampaignDashboardTotalsReference(seriesData.get(axisKey).getGroupingKey(),
//											totalValuesWithoutStacks ? null : series.getStack()));

//							System.out.println(seriesData.get(axisKey).getValueSum().toString() + " ======== "
//									+ series.getStack() + " _____check point 2_________" + totalValue);
							if (totalValue == null) {
								// System.out.println("totalValueyyyyyyyyyyyyyyyyyyyyyy" + totalValue);
								if (!ignoreTotalsError) {
//									Notification.show(String.format(
//											I18nProperties
//													.getString(Strings.errorCampaignDiagramTotalsCalculationError),
//											getDiagramCaption()));
									ignoreTotalsError = true; // only show once
								}
							} else if (totalValue > 0) {

								final double originalValue = seriesData.get(axisKey).getValueSum().doubleValue()
										/ totalValue * 100;
								// final double scaledValue =
								// BigDecimal.valueOf(originalValue).setScale(originalValue < 2 ? 1 : 0,
								// RoundingMode.HALF_UP).doubleValue();

							}
						}
						double scaledValue = scaledValuex / totalValue * 100;

						DecimalFormat formatter = new DecimalFormat("#,###.##");
						//@formatter:off
						//""+perce+"
						hcjs.append(formatter.format(amount));
						//@formatter:on
					}
				} else {
					// percentage
					//@formatter:off
					hcjs.append("0");
					//@formatter:on
				}
				if (cardChart) {
					qq = 1;
				}
			}
		} else {
		//@formatter:off
			hcjs.append("No Data");
		//@formatter:on
		}

//		addClassName("col-md-12");
//		removeClassName("col-lg-6");
//		addClassName("col-lg-3");

		System.out.println("facts: hcjs:" + hcjs + " --series.getStack(): " + series.getStack()
				+ " --series.getColor(): " + series.getColor() + " --perce: " + perce);

		percentageCardComponent = new ProgressBarCardComponent(randomx, hcjs.toString(), series.getStack(),
				series.getColor(), !(perce.isEmpty() || perce.isBlank()));
		this.add(percentageCardComponent);

		mapSeries.clear();
	}

	private void appendPlotOptions(StringBuilder hcjs, Map<String, Long> stackMap) {
		if (stackMap.size() > 0 || showDataLabels || rotateDataLabels) {
			hcjs.append("plotOptions: {");

			if (stackMap.size() > 0) {
				hcjs.append("column: { stacking: 'normal', borderWidth: 0}");
			}
			if (showDataLabels) {
				hcjs.append(stackMap.size() > 0 ? ", " : "").append(
						"series: { dataLabels: { enabled: true, formatter:function() { if (this.y != 0) return this.y; }, style: { fontSize: 14 + 'px' }");
				if (showPercentages && totalValuesMap != null) {
					hcjs.append(", format: '{y}%'");
				}
				hcjs.append("}}");
			}

			if (rotateDataLabels && chartType == DiagramType.COLUMN.name().toLowerCase()) {
				hcjs.append((stackMap.size() > 0 || showDataLabels) ? ", " : "").append(
						"labels: { rotation: -45, style: {fontSize: '13px', fontFamily: 'Verdana, sans-serif'}}");
			}

			if (pieChart && !secondaryChartType.equalsIgnoreCase(DiagramType.DOUGHNUT.toString())) {
				hcjs.append((stackMap.size() > 0 || showDataLabels || rotateDataLabels) ? ", " : "");
				hcjs.append(" pie: {\n" + "            allowPointSelect: true,\n" + "            cursor: 'pointer',\n"
						+ "            dataLabels: {\n" + "                enabled: true,\n"
						+ "                format: '<b>{point.name}</b>: {point.percentage:.1f} %'\n"
						+ "            },  showInLegend: " + showDataLabels + "\n" + "        }");
			} else if (pieChart && secondaryChartType.equalsIgnoreCase(DiagramType.DOUGHNUT.toString())) {

				hcjs.append(", pie: {\n" + "            dataLabels: {\n" + "                enabled: true,\n"
						+ "                distance: -50,\n"
						+ " format: '<b>{point.name}</b>: {point.percentage:.1f} %',\n" + "                style: {\n"
						+ "                    fontWeight: 'bold',\n" + "                    color: 'white'\n"
						+ "                }\n" + "            },\n" + "            startAngle: -90,\n"
						+ "            endAngle: 90,\n" + "            center: ['50%', '75%'],\n"
						+ "            size: '110%'\n" + "        }");

			}

			hcjs.append("},");
		}

		if (showPercentages && totalValuesMap != null) {
			if (!pieChart) {
				hcjs.append("tooltip:{ valueSuffix: ' %' }, ");
			}
		}
	}

	public boolean isShowPercentages() {
		return showPercentages;
	}

	public void setShowPercentages(boolean showPercentages) {
		this.showPercentages = showPercentages;
	}

	public boolean isShowDataLabels() {
		return showDataLabels;
	}

	public void setShowDataLabels(boolean showDataLabels) {
		this.showDataLabels = showDataLabels;
	}

	public boolean isShowAsColumnChart() {
		return showAsColumnChart;
	}

	public void setShowAsColumnChart(boolean showAsColumnChart) {
		this.showAsColumnChart = showAsColumnChart;
	}

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public String getDiagramCaption() {
		String diagramCaption = diagramDefinition.getDiagramCaption();
		CampaignDiagramTranslations translations = getCampaignDiagramTranslations();
		if (translations != null) {
			diagramCaption = translations.getDiagramCaption();
		}
		return diagramCaption;
	}

	private String getStackCaption(String stackName) {
		CampaignDiagramTranslations translations = getCampaignDiagramTranslations();
		if (translations != null && translations.getStackCaptions() != null) {
			TranslationElement stackCaption = translations.getStackCaptions().stream()
					.filter(s -> s.getElementId().equalsIgnoreCase(stackName)).findFirst().orElse(null);
			if (stackCaption != null) {
				return stackCaption.getCaption();
			}
		}
		return stackName;
	}

	private CampaignDiagramTranslations getCampaignDiagramTranslations() {
		Language userLanguage = Language.EN;// UserProvider.getCurrent().getUser().getLanguage();

		CampaignDiagramTranslations translations = null;
		if (userLanguage != null && diagramDefinition.getCampaignDiagramTranslations() != null) {
			translations = diagramDefinition.getCampaignDiagramTranslations().stream()
					.filter(t -> t.getLanguageCode().equals(userLanguage.getLocale().toString())).findFirst()
					.orElse(null);
		}
		return translations;
	}

	public static String generateShortUUID() {
		UUID uuid = UUID.randomUUID();
		byte[] bytes = ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits())
				.putLong(uuid.getLeastSignificantBits()).array();
		String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

		// Replace non-alphabetic characters with alphabets only
		encoded = encoded.replaceAll("[^A-Za-z]", "");
		return encoded;
	}
}