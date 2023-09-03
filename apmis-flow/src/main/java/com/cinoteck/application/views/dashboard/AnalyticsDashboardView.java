package com.cinoteck.application.views.dashboard;

import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.AREA;
import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.COMMUNITY;
import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.DISTRICT;
import static de.symeda.sormas.api.campaign.CampaignJurisdictionLevel.REGION;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JavaScript;
import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.campaigndata.CampaignDataView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.SerializableSupplier;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignJurisdictionLevel;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRole;

@PageTitle("APMIS-Analytics Dashboard")
@Route(value = "analytics", layout = MainLayout.class)

@JavaScript("https://code.highcharts.com/highcharts.js")
@JavaScript("https://code.highcharts.com/modules/variable-pie.js")
@JavaScript("https://code.highcharts.com/modules/exporting.js")
@JavaScript("https://code.highcharts.com/modules/export-data.js")
@JavaScript("https://code.highcharts.com/modules/accessibility.js")
@JavaScript("https://code.highcharts.com/modules/no-data-to-display.js")

public class AnalyticsDashboardView extends VerticalLayout implements RouterLayout , BeforeEnterObserver {

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

	List<CampaignReferenceDto> campaigns;
	List<CampaignReferenceDto> campaignPhases;
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;
	List<String> campaingYears = new ArrayList<>();

	boolean isCampaignChanged;

	private boolean isSubAvaialable = false;
	private CampaignJurisdictionLevel campaignJurisdictionLevel;

	private String listerCheck = "";

	private Div mainContentContainerx;

	UserProvider userProvider = new UserProvider();

	public AnalyticsDashboardView() {
		Button openAnalytics = new Button("Open Analytics");
		openAnalytics.addClickListener(e->{
			UI.getCurrent().getPage().open("https://staging.afghanistan-apmis.com/main");
		});
		openAnalytics.getStyle().set("margin-left", "20px");
		
		 Html html = new Html("<iframe href='https://staging.afghanistan-apmis.com/main' style='width:100%; height:100%;'></iframe>");

		Button openAnalyticsx = new Button("Open Analytics In Frame");
		openAnalyticsx.addClickListener(e->{
			add(html);		});
		
		
		openAnalyticsx.getStyle().set("margin-left", "20px");
		HorizontalLayout hksfhvskf = new HorizontalLayout();
		hksfhvskf.add( openAnalyticsx);
		
		
		add(hksfhvskf);
		setSizeFull();
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// TODO Auto-generated method stub
		
	}


}
