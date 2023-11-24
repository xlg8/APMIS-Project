package com.cinoteck.application.views.dashboard;

import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.AREA;
//import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.COMMUNITY;
import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.DISTRICT;
import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.REGION;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JavaScript;
import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.about.AboutView;
import com.cinoteck.application.views.campaigndata.CampaignDataView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignJurisdictionLevel;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;

@PageTitle("APMIS-Campaign Dashboard")
@Route(value = "dashboard", layout = MainLayout.class)

//@JavaScript("https://code.highcharts.com/highcharts.js")
@JavaScript("https://code.highcharts.com/maps/highmaps.js")

@JavaScript("https://code.highcharts.com/maps/modules/data.js")
@JavaScript("https://code.highcharts.com/maps/modules/drilldown.js")

@JavaScript("https://code.highcharts.com/modules/variable-pie.js")
@JavaScript("https://code.highcharts.com/modules/exporting.js")
@JavaScript("https://code.highcharts.com/modules/export-data.js")
@JavaScript("https://code.highcharts.com/modules/accessibility.js")
@JavaScript("https://code.highcharts.com/modules/no-data-to-display.js")

public class DashboardView extends VerticalLayout implements RouterLayout, BeforeEnterObserver {

	private static final long serialVersionUID = 1851726752523985165L;

	protected CampaignDashboardDataProvider dataProvider;

	Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class);
	Binder<CampaignDto> campaignBinder = new BeanValidationBinder<>(CampaignDto.class);

	CampaignSummaryGridView campaignSummaryGridView = new CampaignSummaryGridView();

	// private Map<TabSheet, Component> tabComponentMap = new LinkedHashMap<>();

	ComboBox<String> campaignYear = new ComboBox<>();
	ComboBox<CampaignReferenceDto> campaign = new ComboBox<>();
	ComboBox<CampaignPhase> campaignPhase = new ComboBox<>();
	ComboBox<AreaReferenceDto> region = new ComboBox<>();
	ComboBox<RegionReferenceDto> province = new ComboBox<>();
	ComboBox<DistrictReferenceDto> district = new ComboBox<>();
	ComboBox<CommunityReferenceDto> cluster = new ComboBox<>();
	Select<CampaignJurisdictionLevel> groupby = new Select<>();

	List<CampaignReferenceDto> campaigns, campaignss;
	List<CampaignReferenceDto> campaignPhases;
	List<AreaReferenceDto> regions;
	List<AreaReferenceDto> regionsx;
	List<RegionReferenceDto> provinces;
	List<RegionReferenceDto> provincesx;
	List<DistrictReferenceDto> districts;
	List<DistrictReferenceDto> districtsx;
	List<CommunityReferenceDto> communities;
	List<String> campaingYears = new ArrayList<>();

	boolean isCampaignChanged;
	boolean isCampaignYearChanging;

	private boolean isSubAvaialable = false;
	private CampaignJurisdictionLevel campaignJurisdictionLevel;

	private String listerCheck = "";

	private Div mainContentContainerx;

	UserProvider userProvider = new UserProvider();

	String firstSubtabId = null;

	NumberFormat arabicFormat = NumberFormat.getInstance();

	public DashboardView() {
		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
		setSpacing(false);
		// UI.getCurrent().getPage().reload();
		// UI.getCurrent().setDirection(Direction.RIGHT_TO_LEFT);

		if (VaadinService.getCurrentRequest().getWrappedSession().getAttribute("mtabtrack") != null) {

			System.out.println(VaadinService.getCurrentRequest().getWrappedSession().getAttribute("mtabtrack")
					+ " MainTab Active");
			System.out.println(
					VaadinService.getCurrentRequest().getWrappedSession().getAttribute("stabtrack") + " SubTab Active");
		}

		dataProvider = new CampaignDashboardDataProvider();
//		String deletab = FacadeProvider.getUserFacade().getCurrentUser().getUsertype().toString();

		UserProvider usr = new UserProvider();

		campaignYear.setLabel(I18nProperties.getCaption(Captions.campaignYear));
		campaigns = FacadeProvider.getCampaignFacade().getAllCampaignByStartDate();

		for (CampaignReferenceDto cmfdto : campaigns) {
			campaingYears.add(cmfdto.getCampaignYear().trim());

		}

//		List<CampaignReferenceDto> filteredCampaigns = new ArrayList<>();
//
//		for (String year : campaingYears) {
//		    for (CampaignReferenceDto cmfdto : campaigns) {
//		        if (cmfdto.getCampaignYear().trim().equals(year)) {
//		            filteredCampaigns.add(cmfdto);
//		        }
//		    }
//		}
//		
//		
//		campaingYears.clear();
//		campaingYears.addAll(setDeduplicated);

		 Set<String> setDeduplicated = new HashSet<>(campaingYears);
		campaignYear.setItems(setDeduplicated);
		campaignYear.setItemLabelGenerator(item -> {
			
			switch (userProvider.getUser().getLanguage().toString()) {
			case "Pashto":
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
				return String.valueOf(arabicFormat.format(Long.parseLong(item)));
			case "Dari":
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
				return String.valueOf(arabicFormat.format(Long.parseLong(item)));
			default:
				arabicFormat = NumberFormat.getInstance(new Locale("en"));
				return String.valueOf(arabicFormat.format(Long.parseLong(item)));
			}
		});

		campaignYear.getStyle().set("padding-top", "0px");
		campaignYear.setClassName("col-sm-6, col-xs-6");

		campaign.setLabel(I18nProperties.getCaption(Captions.Campaign));

		campaigns = FacadeProvider.getCampaignFacade().getAllCampaignByStartDate();

		campaign.setItems(campaigns);
		campaign.getStyle().set("padding-top", "0px");
		campaign.setClassName("col-sm-6, col-xs-6");

		campaignPhase.setLabel(I18nProperties.getCaption(Captions.Campaign_phase));
//		campaignPhases = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference()
		campaignPhase.setItems(CampaignPhase.values());
		campaignPhase.setItemLabelGenerator(this::getLabelForEnum);

		campaignPhase.getStyle().set("padding-top", "0px");
		campaignPhase.setClassName("col-sm-6, col-xs-6");

		// campaignFormPhaseSelector = new
		// CampaignFormPhaseSelector(lastStartedCampaign);

		System.out.println(
				"campaignPhase _ session " + VaadinSession.getCurrent().getSession().getAttribute("campaignPhase"));
		System.out.println("camp _ session" + VaadinSession.getCurrent().getSession().getAttribute("campaign"));

		if (VaadinSession.getCurrent().getSession().getAttribute("campaign") != null) {
			String sessionCheckPhase = VaadinSession.getCurrent().getSession().getAttribute("campaignPhase").toString();
			String sessionCheckCampaign = VaadinSession.getCurrent().getSession().getAttribute("campaign").toString();

			CampaignReferenceDto sx = FacadeProvider.getCampaignFacade().getReferenceByUuid(sessionCheckCampaign);

			campaign.setValue(sx);
			campaignYear.setValue(sx.getCampaignYear());
			campaignPhase.setValue(CampaignPhase.valueOf(sessionCheckPhase.replace("-campaign", "").toUpperCase()));
			dataProvider.setCampaign(sx);
			dataProvider.setFormType(sessionCheckPhase);

		} else {
			final CampaignReferenceDto lastStartedCampaign = dataProvider.getLastStartedCampaign();

			if (lastStartedCampaign != null) {

				List<CampaignReferenceDto> campaigns_x = new ArrayList<>();
				for (CampaignReferenceDto cmfdto : campaigns) {
					if (cmfdto.getCampaignYear().equals(lastStartedCampaign.getCampaignYear())) {
//						campaigns_.clear();
						campaigns_x.add(cmfdto);
					}
				}

				campaign.clear();
				campaign.setItems(campaigns_x);
				campaign.setValue(lastStartedCampaign);
				campaignYear.setValue(lastStartedCampaign.getCampaignYear());
				campaignPhase.setValue(CampaignPhase.INTRA);

			}

			dataProvider.setCampaign(lastStartedCampaign);
			dataProvider.setFormType(CampaignPhase.INTRA.toString().toLowerCase());
		}

		// Filter initializers
		region.setLabel(I18nProperties.getCaption(Captions.area));
		binder.forField(region).bind(UserDto::getArea, UserDto::setArea);

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			regionsx = FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto();
			region.setItems(regionsx);
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			regionsx = FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari();
			region.setItems(regionsx);
		} else {
			regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
			region.setItems(regions);
		}

		region.setClearButtonVisible(true);
		// TODO: this is only for debugging purpose, please remove after test
		dataProvider.setCampaignJurisdictionLevelGroupBy(campaignJurisdictionLevel.AREA);
		// dataProvider.setArea(FacadeProvider.getAreaFacade().getAreaReferenceByUuid("W5R34K-APYPCA-4GZXDO-IVJWKGIM"));

		region.getStyle().set("padding-top", "0px");
		region.setClassName("col-sm-6, col-xs-6");

		province.setLabel(I18nProperties.getCaption(Captions.region));
		binder.forField(province).bind(UserDto::getRegion, UserDto::setRegion);

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			provincesx = FacadeProvider.getRegionFacade().getAllActiveAsReferencePashto();
			province.setItems(provincesx);
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			provincesx = FacadeProvider.getRegionFacade().getAllActiveAsReferenceDari();
			province.setItems(provincesx);
		} else {
			provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
			province.setItems(provinces);
		}

		province.getStyle().set("padding-top", "0px");
		province.setClassName("col-sm-6, col-xs-6");
		// province.setEnabled(false);

		district.setLabel(I18nProperties.getCaption(Captions.district));
		binder.forField(district).bind(UserDto::getDistrict, UserDto::setDistrict);

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			districtsx = FacadeProvider.getDistrictFacade().getAllActiveAsReferencePashto();
			district.setItems(districtsx);
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			districtsx = FacadeProvider.getDistrictFacade().getAllActiveAsReferenceDari();
			district.setItems(districtsx);
		} else {
			districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
			district.setItems(districts);
		}

		district.getStyle().set("padding-top", "0px");
		district.setClassName("col-sm-6, col-xs-6");
		// district.setEnabled(false);

		groupby.setLabel(I18nProperties.getCaption(Captions.campaignDiagramGroupBy));
//		groupby.setItems(campaignJurisdictionLevel.values());
		groupby.setItems(AREA, REGION, DISTRICT);
		groupby.getStyle().set("padding-top", "0px");
		groupby.setClassName("col-sm-6, col-xs-6");

		// TODO should check user assignment first and set respectively
		groupby.setValue(campaignJurisdictionLevel.AREA);

		HorizontalLayout selectFilterLayout = new HorizontalLayout(campaignYear, campaign, campaignPhase, region,
				province, district, /* cluster, */ groupby);
		selectFilterLayout.setClassName("row pl-3");
		VerticalLayout selectFilterLayoutparent = new VerticalLayout(selectFilterLayout);
		selectFilterLayoutparent.getStyle().set("padding", "0px");
		selectFilterLayoutparent.getStyle().set("margin-left", "12px");
		selectFilterLayoutparent.setVisible(true);

		Button displayFilters = new Button(I18nProperties.getCaption(Captions.hideFilters));
		displayFilters.getStyle().set("margin-left", "12px");
		displayFilters.getStyle().set("margin-top", "12px");
		displayFilters.setIcon(new Icon(VaadinIcon.SLIDERS));

		// filter listeners

		campaign.addValueChangeListener(e -> {
			System.out.println("hgfghyuioffkjbnjkijhnbjk");
			isCampaignChanged = true;

			if (!isCampaignYearChanging) {
				UUID uuid = UUID.randomUUID();
				VaadinSession.getCurrent().getSession().setAttribute("campaignPhase",
						campaignPhase.getValue().toString().toLowerCase());
				VaadinSession.getCurrent().getSession().setAttribute("campaign", campaign.getValue().getUuid());
				// UI.getCurrent().getPage().reload();

				dataProvider.setCampaign((CampaignReferenceDto) campaign.getValue());

				remove(mainContentContainerx);
				mainContentContainerx = drawDashboardAndTabs(uuid.toString());
				add(mainContentContainerx);

				campaignYear.setValue(e.getValue().getCampaignYear());
			}
		});

		campaignYear.addValueChangeListener(e -> {
			if (!isCampaignChanged) {
				List<CampaignReferenceDto> campaigns_ = new ArrayList<>();

				campaigns = FacadeProvider.getCampaignFacade().getAllCampaignByStartDate();
				isCampaignYearChanging = true;
				campaign.clear();
				for (CampaignReferenceDto cmfdto : campaigns) {
					if (cmfdto.getCampaignYear().equals(e.getValue())) {
//						campaigns_.clear();
						campaigns_.add(cmfdto);
					}
				}
				campaign.setItems(campaigns_);
				isCampaignYearChanging = false;
				campaign.setValue(campaigns_.get(0));
			}
			isCampaignChanged = false;
		});

		campaignPhase.addValueChangeListener(e -> {

			UUID uuid = UUID.randomUUID();
			dataProvider.setFormType(campaignPhase.getValue().toString().toLowerCase());
			dataProvider.setCampaignFormPhase(campaignPhase.getValue().toString().toLowerCase());

			VaadinSession.getCurrent().getSession().setAttribute("campaignPhase",
					campaignPhase.getValue().toString().toLowerCase());
			VaadinSession.getCurrent().getSession().setAttribute("campaign", campaign.getValue().getUuid());
			// UI.getCurrent().getPage().reload();

			remove(mainContentContainerx);
			mainContentContainerx.removeAll();
			mainContentContainerx = drawDashboardAndTabs(uuid.toString());
			add(mainContentContainerx);

		});

		region.addValueChangeListener(e -> {
			changeCampaignJuridictionLevel(campaignJurisdictionLevel.AREA);

			if (e.getValue() != null) {
				dataProvider.setArea(e.getValue());
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					provincesx = FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(e.getValue().getUuid());
					province.setItems(provincesx);
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					provincesx = FacadeProvider.getRegionFacade().getAllActiveByAreaDari(e.getValue().getUuid());
					province.setItems(provincesx);
				} else {
					provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
					province.setItems(provinces);
				}
				province.setEnabled(true);
				groupby.setValue(campaignJurisdictionLevel.REGION);
			} else {
				groupby.setValue(campaignJurisdictionLevel.AREA);

			}

//			if(province.getValue() != null  ) {
//				province.clear();
//			}
//			
//			if( district.getValue() != null  ) {
//				
//				district.clear();
//			}

		});

		province.setClearButtonVisible(true);
		province.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				changeCampaignJuridictionLevel(campaignJurisdictionLevel.REGION);
				groupby.setValue(campaignJurisdictionLevel.REGION);
				dataProvider.setRegion(e.getValue());
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					districtsx = FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(e.getValue().getUuid());
					district.setItems(districtsx);
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					districtsx = FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(e.getValue().getUuid());
					district.setItems(districtsx);
				} else {
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
					district.setItems(districts);
				}
				district.setEnabled(true);
				groupby.setValue(campaignJurisdictionLevel.DISTRICT);

			} else {
				changeCampaignJuridictionLevel(campaignJurisdictionLevel.AREA);
				dataProvider.setRegion(e.getValue());
				if (district.getValue() != null) {
					district.clear();
					district.setEnabled(false);
				}
				groupby.setValue(campaignJurisdictionLevel.AREA);
			}

		});

		district.setClearButtonVisible(true);
		district.addValueChangeListener(e -> {
			changeCampaignJuridictionLevel(campaignJurisdictionLevel.DISTRICT);
			groupby.setValue(campaignJurisdictionLevel.DISTRICT);
			dataProvider.setDistrict(e.getValue());
			groupby.setValue(campaignJurisdictionLevel.COMMUNITY);
		});

		groupby.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				dataProvider.setCampaignJurisdictionLevelGroupBy(e.getValue());
				UUID uuid = UUID.randomUUID();
				remove(mainContentContainerx);
				mainContentContainerx.removeAll();
				mainContentContainerx = drawDashboardAndTabs(uuid.toString());
				add(mainContentContainerx);
			}

		});

		displayFilters.addClickListener(e -> {

			if (!selectFilterLayoutparent.isVisible()) {
				selectFilterLayoutparent.setVisible(true);
				displayFilters.setText(I18nProperties.getCaption(Captions.hideFilters));
				province.setEnabled(false);
				district.setEnabled(false);

			} else {
				selectFilterLayoutparent.setVisible(false);
				displayFilters.setText(I18nProperties.getCaption(Captions.showFilters));
			}
		});

		UUID uuid = UUID.randomUUID();

		HorizontalLayout filterLay = new HorizontalLayout();

		filterLay.add(displayFilters, selectFilterLayoutparent);
		filterLay.getStyle().set("margin-right", "0.5rem");
		filterLay.setAlignItems(Alignment.END);
		add(filterLay);
		// , mtabs, sTabs, contentContainer);
		mainContentContainerx = drawDashboardAndTabs(uuid.toString());

		add(mainContentContainerx);

		setSizeFull();
	}

	private Div drawDashboardAndTabs(String UIs) {
		Div mainContentContainer = new Div();
		mainContentContainer.setId(UIs);
		Div contentContainer = new Div();

		mainContentContainer.removeAll();
		mainContentContainer.setWidthFull();

		Tabs mtabs = new Tabs();
		mtabs.removeAll();

		mtabs.setId("maintab");
		mtabs.getStyle().set("background", "#434343");

		mtabs.setWidthFull();

		Tabs sTabs = new Tabs();
		sTabs.setId("subtabs");
		sTabs.getStyle().set("background", "#ffffff00");

		// contentContainer.getStyle().set("background", "#f1f4f6");
		final List<String> mainTabs = new ArrayList<>(dataProvider.getTabIds());
		int ctr = 0;
		int defctr = 0;

		String firstMntabId = null;
//		String firstSubtabId = null;

		// creating bucket tabs to hold tabs and subtabs
		for (String tabIdc : mainTabs) {
			ctr++;

			String mntabId = WordUtils.capitalizeFully(tabIdc);
			Tab tabx = new Tab(mntabId);
			tabx.setId("main_" + mntabId);
			tabx.getStyle().set("font-weight", "600");
//					tabx.getStyle().set("color", "600");
			mtabs.add(tabx);

			// this has to be moved to listener
			if (ctr == 1) {

				final List<String> subTabx = new ArrayList<>(dataProvider.getSubTabIds(mntabId));
				sTabs.setVisible(subTabx.size() > 1);
				isSubAvaialable = subTabx.size() > 1;
				for (String sbTabId : subTabx) {
					defctr++;

					String sbtabId = WordUtils.capitalizeFully(tabIdc);
					Tab stabx = new Tab(sbTabId);
					stabx.setId("submain_" + sbTabId);
					stabx.getStyle().set("font-weight", "500");
					stabx.getStyle().set("color", "#0D6938");
					sTabs.add(stabx);

					if (defctr == 1) {

						firstMntabId = mntabId;
						firstSubtabId = sbTabId;
					}
				}

			}
		}

		mtabs.addSelectedChangeListener(e -> {

			VaadinService.getCurrentRequest().getWrappedSession().setAttribute("mtabtrack", mtabs.getSelectedIndex());

			System.out.println("entered main tab");
			int listnrCtr = 0;

			final List<String> subTabx = new ArrayList<>(
					dataProvider.getSubTabIds(e.getSelectedTab().getId().get().toString().replaceAll("main_", "")));
			isSubAvaialable = subTabx.size() > 1;
			sTabs.setVisible(isSubAvaialable);
			sTabs.removeAll();
			String firstSbTabId = "";

			for (String sbTabId : subTabx) {
				listnrCtr++;
				if (listnrCtr == 1) {

					firstSbTabId = sbTabId;
					System.out.println(firstSbTabId + " first subtab IDDDDDDDDDDDDDDDDDDDDDD");
				}
				// String sbtabId = WordUtils.capitalizeFully(tabIdc);
				Tab stabx = new Tab(sbTabId);
				stabx.setId("submain_" + sbTabId);
				stabx.getStyle().set("font-weight", "500");
				stabx.getStyle().set("color", "#0D6938");
				sTabs.add(stabx);

			}
			if (VaadinService.getCurrentRequest().getWrappedSession().getAttribute("stabtrack") != null) {
				firstSbTabId = (String) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("stabtrack");
				firstSbTabId = firstSbTabId.replaceAll("submain_", "");
				firstSubtabId = firstSbTabId;
				System.out.println(firstSbTabId + " first subtab IIDDDDDDDDDDDDDDDDDDDDDD");
//				sTabs.setSelectedTab(
//						(String) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("stabtrack").toString());
			}
//								//Notification.show(e.getSelectedTab().getId().get().toString().replaceAll("main_", ""));
//								//Notification.show(firstSbTabId);

			contentContainer.removeAll();

			listerCheck = e.getSelectedTab().getId().get().replaceAll("main_", "");

			contentContainer.add(campaignSummaryGridView.CampaignSummaryGridViewInit(
					e.getSelectedTab().getId().get().replaceAll("main_", ""), dataProvider, campaignPhase.getValue(),
					firstSbTabId));

		});

		sTabs.addSelectedChangeListener(e -> {
			System.out.println("Subtab enteredddddddddddddddddddddddd");

			String listnrCtr = listerCheck;

			if (e.getSelectedTab() != null) {
				System.out.print(e.getSelectedTab().getId().get() + " hhhkkkkk " + e.getSelectedTab());
				VaadinService.getCurrentRequest().getWrappedSession().setAttribute("stabtrack",
						e.getSelectedTab().getId().get());

				System.out.print(
						e.getSelectedTab().getId().get().toString() + " e.getSelectedTab().getId().get().toString() ");
				String listnrCtrx = e.getSelectedTab().getId().get().replaceAll("submain_", "");
				System.out.print(listnrCtrx + " listnrCtrx");
				// Notification.show(listnrCtr +" _____________________________ "+listnrCtrx);

				contentContainer.removeAll();
				contentContainer.add(campaignSummaryGridView.CampaignSummaryGridViewInit(listnrCtr, dataProvider,
						campaignPhase.getValue(), listnrCtrx));

			}
		});

		if (firstMntabId != null && firstSubtabId != null) {
			listerCheck = firstMntabId;
			contentContainer.removeAll();
			contentContainer.add(campaignSummaryGridView.CampaignSummaryGridViewInit(firstMntabId, dataProvider,
					campaignPhase.getValue(), firstSubtabId));
		}

		contentContainer.getStyle().set("margin-top", "0.4rem");
		contentContainer.setId("tabsSheet");
		contentContainer.setSizeFull();

		try {
			if (VaadinService.getCurrentRequest().getWrappedSession().getAttribute("mtabtrack") != null) {

				mtabs.setSelectedIndex(
						(int) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("mtabtrack"));
			}
		} finally {

		}

		mainContentContainer.add(mtabs, sTabs, contentContainer);
		return mainContentContainer;
	}

	public class LazyComponent extends Div {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3141958332173506396L;

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
			groupby.setItems(REGION, DISTRICT);// , COMMUNITY);
			break;
		case DISTRICT:
			groupby.setItems(DISTRICT);// , COMMUNITY);
			break;
		case COMMUNITY:
			groupby.setItems(DISTRICT);
			break;
		}

	}

	private String getLabelForEnum(CampaignPhase campaignPhase) {
		switch (campaignPhase) {
		case PRE:
			return "Pre-Campaign";

		case POST:
			return "Post-Campaign";

		case INTRA:
			return "Intra-Campaign";

		default:
			return campaignPhase.toString();
		}
	}

//	private ItemLabelGenerator<String> getLabel() {
//	  String finalString = "";
//	    NumberFormat arabicFormat = NumberFormat.getInstance();
//
//	    for (String string : campaingYears) {
//	        switch (userProvider.getUser().getLanguage().toString()) {
//	            case "Pashto":
//	                arabicFormat = NumberFormat.getInstance(new Locale("ps"));
//	                finalString = String.valueOf(arabicFormat.format(Long.parseLong(string)));
//	                break;
//	            case "Dari":
//	                arabicFormat = NumberFormat.getInstance(new Locale("fa"));
//	                finalString = String.valueOf(arabicFormat.format(Long.parseLong(string)));
//	                break;
//	            default:
//	                arabicFormat = NumberFormat.getInstance(new Locale("en"));
//	                finalString = String.valueOf(arabicFormat.format(Long.parseLong(string)));
//	                break;
//	        }
//
//	        // If you only want to process the first element and then return, you can break here
//	        // break;
//	    }
//
//	    return finalString;
//	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		UserProvider usrP = new UserProvider();
		System.out.println("trying ti use camp data " + usrP.getUser().getUserRoles());

		if (!userProvider.hasUserRight(UserRight.DASHBOARD_CAMPAIGNS_ACCESS)) {
			event.rerouteTo(AboutView.class);
		}

	}

}
