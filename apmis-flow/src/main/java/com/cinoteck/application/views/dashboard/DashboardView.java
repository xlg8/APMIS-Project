package com.cinoteck.application.views.dashboard;

import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.AREA;
import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.COMMUNITY;
import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.DISTRICT;
import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.REGION;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.WordUtils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dnd.DragSource;
import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignJurisdictionLevel;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserDto;

@PageTitle("Campaign Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)

@JavaScript("https://code.highcharts.com/highcharts.js")
@JavaScript("https://code.highcharts.com/modules/exporting.js")
@JavaScript("https://code.highcharts.com/modules/export-data.js")
@JavaScript("https://code.highcharts.com/modules/accessibility.js")

//@StyleSheet("https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css")
//@JavaScript("https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js")
public class DashboardView extends VerticalLayout implements RouterLayout {

	protected CampaignDashboardDataProvider dataProvider;

	Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class);
	Binder<CampaignDto> campaignBinder = new BeanValidationBinder<>(CampaignDto.class);

	CampaignSummaryGridView campaignSummaryGridView = new CampaignSummaryGridView();

	private Map<TabSheet, Component> tabComponentMap = new LinkedHashMap<>();
	ComboBox<CampaignReferenceDto> campaign = new ComboBox<>();
	ComboBox<CampaignPhase> campaignPhase = new ComboBox<>();
	ComboBox<AreaReferenceDto> region = new ComboBox<>();
	ComboBox<RegionReferenceDto> province = new ComboBox<>();
	ComboBox<DistrictReferenceDto> district = new ComboBox<>();
	ComboBox<CommunityReferenceDto> cluster = new ComboBox<>();
	Select<CampaignJurisdictionLevel> groupby = new Select<>();
	
	
	List<CampaignReferenceDto> campaigns;
	List<CampaignReferenceDto> campaignPhases;
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;
	
	private boolean isSubAvaialable = false;
	private CampaignJurisdictionLevel campaignJurisdictionLevel;

	public DashboardView() {
		setSpacing(true);

		dataProvider = new CampaignDashboardDataProvider();

		campaign.setLabel("Campaign");
		campaigns = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
		campaign.setItems(campaigns);

		campaign.getStyle().set("padding-top", "0px");
		campaign.setClassName("col-sm-6, col-xs-6");

		campaign.addValueChangeListener(e -> {
			dataProvider.setCampaign((CampaignReferenceDto) campaign.getValue());
			Notification.show("Cmapaign has chnaged TODO = " + e.getValue().getCaption());
			// dashboardView.refreshDashboard();
		});

		final CampaignReferenceDto lastStartedCampaign = dataProvider.getLastStartedCampaign();

		campaignPhase.setLabel("Campaign Phase");
//		campaignPhases = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference()
		campaignPhase.setItems(CampaignPhase.values());
		campaignPhase.getStyle().set("padding-top", "0px");
		campaignPhase.setClassName("col-sm-6, col-xs-6");

		// campaignFormPhaseSelector = new
		// CampaignFormPhaseSelector(lastStartedCampaign);

		campaignPhase.addValueChangeListener(e -> {
			dataProvider.setFormType(campaignPhase.getValue().toString().toLowerCase());
			// dataProvider.refreshDashboard();
		});

		if (lastStartedCampaign != null) {
			campaign.setValue(lastStartedCampaign);
			campaignPhase.setValue(CampaignPhase.INTRA);
		}

		Notification.show(CampaignPhase.INTRA.toString().toLowerCase());

		dataProvider.setCampaign(lastStartedCampaign);
		dataProvider.setFormType(CampaignPhase.INTRA.toString().toLowerCase());

		region.setLabel("Region");
		binder.forField(region).bind(UserDto::getArea, UserDto::setArea);
		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		region.setItems(regions);
		region.setValue(FacadeProvider.getAreaFacade().getAreaReferenceByUuid("W5R34K-APYPCA-4GZXDO-IVJWKGIM"));
		region.addValueChangeListener(e -> {
			Notification.show("area changing... " + e.getValue());
			changeCampaignJuridictionLevel(campaignJurisdictionLevel.AREA);
			dataProvider.setArea(e.getValue());
			provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
			province.setItems(provinces);
		});
		
		
		
		
		
		//TODO: this is only for debugging purpose, please remove after test
		dataProvider.setCampaignJurisdictionLevelGroupBy(campaignJurisdictionLevel.AREA);
		dataProvider.setArea(FacadeProvider.getAreaFacade().getAreaReferenceByUuid("W5R34K-APYPCA-4GZXDO-IVJWKGIM"));
		
		
		
		
		
		
		region.getStyle().set("padding-top", "0px");
		region.setClassName("col-sm-6, col-xs-6");

		province.setLabel("Province");
		binder.forField(province).bind(UserDto::getRegion, UserDto::setRegion);
		provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
		province.setItems(provinces);
		province.addValueChangeListener(e -> {
			Notification.show("Region changing... " + e.getValue());
			changeCampaignJuridictionLevel(campaignJurisdictionLevel.REGION);
			dataProvider.setRegion(e.getValue());
			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
			district.setItems(districts);
		});
		province.getStyle().set("padding-top", "0px");
		province.setClassName("col-sm-6, col-xs-6");

		district.setLabel("District");
		binder.forField(district).bind(UserDto::getDistrict, UserDto::setDistrict);
		districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
		district.setItems(districts);
		district.addValueChangeListener(e -> {
			changeCampaignJuridictionLevel(campaignJurisdictionLevel.DISTRICT);
			dataProvider.setDistrict(e.getValue());
			communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
			cluster.setItemLabelGenerator(CommunityReferenceDto::getCaption);
			cluster.setItems(communities);
		});
		district.getStyle().set("padding-top", "0px");
		district.setClassName("col-sm-6, col-xs-6");

		cluster.setLabel("Cluster");

		cluster.getStyle().set("padding-top", "0px");
		cluster.setClassName("col-sm-6, col-xs-6");

		
		
		
		
		groupby.setLabel("Group By");
		groupby.setItems(campaignJurisdictionLevel.values());
		groupby.getStyle().set("padding-top", "0px");
		groupby.setClassName("col-sm-6, col-xs-6");
		
		groupby.addValueChangeListener(e -> {
			dataProvider.setCampaignJurisdictionLevelGroupBy(e.getValue());
		});
		
		

		HorizontalLayout selectFilterLayout = new HorizontalLayout(campaign, campaignPhase, region, province, district,
				cluster, groupby);
		selectFilterLayout.setClassName("row pl-3");
		VerticalLayout selectFilterLayoutparent = new VerticalLayout(selectFilterLayout);
		selectFilterLayoutparent.getStyle().set("padding", "0px");
		selectFilterLayoutparent.getStyle().set("margin-left", "12px");
		selectFilterLayoutparent.setVisible(false);

		Button displayFilters = new Button("Show Filters");
		displayFilters.getStyle().set("margin-left", "12px");
		displayFilters.getStyle().set("margin-top", "12px");
		displayFilters.setIcon(new Icon(VaadinIcon.SLIDERS));

		displayFilters.addClickListener(e -> {
			if (!selectFilterLayoutparent.isVisible()) {
				selectFilterLayoutparent.setVisible(true);
				displayFilters.setText("Hide Filters");

			} else {
				selectFilterLayoutparent.setVisible(false);
				displayFilters.setText("Show Filters");
			}
		});

		Tabs mtabs = new Tabs();
		mtabs.setId("maintab");
		mtabs.getStyle().set("background", "#434343");
		mtabs.getStyle().set("color", "#ffffff00");
		mtabs.getStyle().set("width", "100%");

		Tabs sTabs = new Tabs();
		sTabs.setId("subtabs");
		sTabs.getStyle().set("background", "#0D6938");
		Div contentContainer = new Div();

		// contentContainer.getStyle().set("background", "#f1f4f6");
		final List<String> mainTabs = new ArrayList<>(dataProvider.getTabIds());
		int ctr = 0;
		
		// creating bucket tabs to hold tabs and subtabs
		for (String tabIdc : mainTabs) {
			ctr++;

			String mntabId = WordUtils.capitalizeFully(tabIdc);
			Tab tabx = new Tab(mntabId);
			tabx.setId("main_" + mntabId);
			mtabs.add(tabx);

			// this has to be moved to listener
			if (ctr == 1) {
				final List<String> subTabx = new ArrayList<>(dataProvider.getSubTabIds(mntabId));
				sTabs.setVisible(subTabx.size() > 1);
				isSubAvaialable = subTabx.size() > 1 ;
				for (String sbTabId : subTabx) {
//					String sbtabId = WordUtils.capitalizeFully(tabIdc);
					Tab stabx = new Tab(sbTabId);
					stabx.setId("submain_" + sbTabId);
					sTabs.add(stabx);

				}
			}
		}

		mtabs.addSelectedChangeListener(e -> {
			int listnrCtr = 0;
			
				final List<String> subTabx = new ArrayList<>(dataProvider.getSubTabIds(e.getSelectedTab().getId().get().toString().replaceAll("main_", "")));
				isSubAvaialable = subTabx.size() > 1 ;
				sTabs.setVisible(isSubAvaialable);
				sTabs.removeAll();
				String firstSbTabId = "";
					
						for (String sbTabId : subTabx) {
							listnrCtr++;
							if(listnrCtr == 1) {
								firstSbTabId = sbTabId;
							}
		//					String sbtabId = WordUtils.capitalizeFully(tabIdc);
							Tab stabx = new Tab(sbTabId);
							stabx.setId("submain_" + sbTabId);
							sTabs.add(stabx);
		
						}
//						Notification.show(e.getSelectedTab().getId().get().toString().replaceAll("main_", ""));
//						Notification.show(firstSbTabId);
						
			contentContainer.removeAll();
			
			
			
			contentContainer.add(campaignSummaryGridView.CampaignSummaryGridViewInit(
					e.getSelectedTab().getId().get().replaceAll("main_", ""), dataProvider,
					campaignPhase.getValue(), firstSbTabId));
			
			
			
		
		});

		contentContainer.setWidthFull();
		contentContainer.setId("tabsSheet");
		contentContainer.setSizeFull();

		add(displayFilters, selectFilterLayoutparent, mtabs, sTabs, contentContainer);
		setSizeFull();
	}

	//
//		
//		contentContainer.add(tabComponentMap.get(this));
//		
//
//	
	// tabComponentMap.put(new Tab("Campaign Summary"), new
	// CampaignSummaryGridView());
////		tabComponentMap.put(new Tab("Admin Coverage By Day"), new AdminCovByDayGridView());
////		tabComponentMap.put(new Tab("Admin Coverage: Doses"), new AdminCovByDosesGridView());
////		tabComponentMap.put(new Tab("Coverage Summary"), new AdminCovByDosesGridView());
//		return tabSheet;// new TabSheet(tabComponentMap.keySet().toArray(new Tab[] {}));
//
//	}
//	

	public class LazyComponent extends Div {
		public LazyComponent(SerializableSupplier<? extends Component> supplier) {
			addAttachListener(e -> {
				if (getElement().getChildCount() == 0) {
					add(supplier.get());
				}
			});
		}
	}
	
	
	private void changeCampaignJuridictionLevel(CampaignJurisdictionLevel campaignJurisdictionLevelz) {
		groupby.clear();
		switch (campaignJurisdictionLevelz) {
		case AREA:
			groupby.setItems(AREA, REGION, DISTRICT);
			break;
		case REGION:
			groupby.setItems(REGION, DISTRICT, COMMUNITY);
			break;
		case DISTRICT:
			groupby.setItems(DISTRICT, COMMUNITY);
			break;
		case COMMUNITY:
			groupby.setItems(COMMUNITY);
			break;
		}

		
	}

}
