package com.cinoteck.application.views.dashboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDataDto;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDefinitionDto;

@Route(layout = DashboardView.class)
public class CampaignSummaryGridView extends VerticalLayout {

	/**
	* 
	*/
	private static final long serialVersionUID = -1665531559323782811L;

	public CampaignSummaryGridView() {
	}

	public Component CampaignSummaryGridViewInit(String mainTabIdCoded, CampaignDashboardDataProvider dataProvider,
			CampaignPhase formTyp, String subTabIdCoded) {
		// TODO
		dataProvider.setFormType(formTyp.toString().toLowerCase());

		Div dashboardContainer = new Div();
		dashboardContainer.setClassName("container col");

		dashboardContainer.setClassName("row col");
		dashboardContainer.getStyle().set("display", "flex");
		dashboardContainer.getStyle().set("flex-direction", "row");

		Map<CampaignDashboardDiagramDto, List<CampaignDiagramDataDto>> campaignFormDataMap = dataProvider
				.getCampaignFormDataMap(mainTabIdCoded, subTabIdCoded);

		// Convert the map to a list of key-value pairs
		List<Map.Entry<CampaignDashboardDiagramDto, List<CampaignDiagramDataDto>>> entryList = new ArrayList<>(
				campaignFormDataMap.entrySet());

		// Sort the list based on the keys
		entryList.sort(Comparator.comparing(entry -> entry.getKey().getCampaignDashboardElement().getOrder()));

		for (Map.Entry<CampaignDashboardDiagramDto, List<CampaignDiagramDataDto>> campaignDashboardDiagramDto_diagramData : entryList) {

			final CampaignDiagramDefinitionDto campaignDiagramDefinitionDto = campaignDashboardDiagramDto_diagramData
					.getKey().getCampaignDiagramDefinitionDto();
			final int chartWidth = campaignDashboardDiagramDto_diagramData.getKey().getCampaignDashboardElement()
					.getWidth();

			int chartWidthBoostraped = (int) Math.ceil((double) chartWidth / 100 * 12);
			

			System.out.println(campaignDashboardDiagramDto_diagramData.getKey().getCampaignDiagramDefinitionDto().getDiagramType() +" : " + campaignDashboardDiagramDto_diagramData.getKey().getCampaignDashboardElement().getWidth() + "  ++++++++++++  " + chartWidthBoostraped);

			campaignDashboardDiagramDto_diagramData.getKey().getCampaignDiagramDefinitionDto().getDiagramId();

			final CampaignDashboardDiagramComponent diagramComponent = new CampaignDashboardDiagramComponent(
					campaignDiagramDefinitionDto, campaignDashboardDiagramDto_diagramData.getValue(),
					dataProvider.getCampaignFormTotalsMap(mainTabIdCoded, subTabIdCoded)
							.get(campaignDashboardDiagramDto_diagramData.getKey()),
					dataProvider.getCampaignJurisdictionLevelGroupBy(), chartWidthBoostraped);

			dashboardContainer.add(diagramComponent);

		}

		return dashboardContainer;
	}

}
