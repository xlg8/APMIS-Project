package com.cinoteck.application.views.reports;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.utils.gridexporter.GridExporter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.SortProperty;

@Route(layout = CompletionAnalysisTabsheet.class)
public class CompletionAnalysisView extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6242228590115105671L;
	private ComboBox<CampaignReferenceDto> campaign = new ComboBox<>();
	private ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>();
	private ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>();
	private ComboBox<DistrictReferenceDto> districtFilter = new ComboBox<>();
	private ComboBox<String> errorFilter = new ComboBox<>();
	private Button resetButton;

	List<CampaignReferenceDto> campaigns;
	List<CampaignReferenceDto> campaignPhases;
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	CampaignFormDataCriteria criteria;
	Grid<CampaignFormDataIndexDto> grid = new Grid<>(CampaignFormDataIndexDto.class, false);

	DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider;
	FormAccess formAccess;
	Button exportReport = new Button();
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	Icon icon = VaadinIcon.UPLOAD_ALT.create();
	CampaignReferenceDto lastStarted = FacadeProvider.getCampaignFacade().getLastStartedCampaign();

	private UserProvider userProvider = new UserProvider();

	private void refreshGridData(FormAccess formAccess) {
//		int numberOfRows = FacadeProvider.getCampaignFormDataFacade().prepareAllCompletionAnalysis();
		dataProvider = DataProvider.fromFilteringCallbacks(
				query -> FacadeProvider.getCampaignFormDataFacade()
						.getByCompletionAnalysis(criteria, query.getOffset(), query.getLimit(),
								query.getSortOrders().stream()
										.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
												sortOrder.getDirection() == SortDirection.ASCENDING))
										.collect(Collectors.toList()),
								null)
						.stream(),
				query -> Integer.parseInt(FacadeProvider.getCampaignFormDataFacade().getByCompletionAnalysisCount(
						criteria, query.getOffset(), query.getLimit(),
						query.getSortOrders().stream()
								.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
										sortOrder.getDirection() == SortDirection.ASCENDING))
								.collect(Collectors.toList()),
						null)));

		grid.setDataProvider(dataProvider);
	}

	public CompletionAnalysisView() {

		this.criteria = new CampaignFormDataCriteria();

		setHeightFull();

		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setPadding(false);
		filterLayout.setVisible(true);
		filterLayout.setAlignItems(Alignment.END);

		campaign.setLabel(I18nProperties.getCaption(Captions.Campaigns));
		campaign.setPlaceholder(I18nProperties.getCaption(Captions.campaignAllCampaigns));
		campaigns = FacadeProvider.getCampaignFacade().getAllCampaignByStartDate();
		campaign.setItems(campaigns);

		campaign.setValue(lastStarted);
		criteria.campaign(lastStarted);
		campaign.addValueChangeListener(e -> {
			CampaignReferenceDto selectedCAmpaign = e.getValue();
			if (selectedCAmpaign != null) {
				criteria.campaign(selectedCAmpaign);
				refreshGridData(formAccess);
			} else {
				criteria.campaign(null);
				refreshGridData(formAccess);
			}

		});

		regionFilter.setLabel(I18nProperties.getCaption(Captions.area));
		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.areaAllAreas));

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto());
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari());
		} else {
			regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		}
		regionFilter.setClearButtonVisible(true);
		regionFilter.addValueChangeListener(e -> {
			AreaReferenceDto selectedArea = e.getValue();
			if (selectedArea != null) {
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(e.getValue().getUuid());
					provinceFilter.setItems(provinces);
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaDari(e.getValue().getUuid());
					provinceFilter.setItems(provinces);
				} else {
					provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(selectedArea.getUuid());
					provinceFilter.setItems(provinces);
				}
				criteria.campaign(campaign.getValue());
				criteria.area(selectedArea);

				refreshGridData(formAccess);
			} else {
				criteria.area(null);
				refreshGridData(formAccess);
			}
		});

		provinceFilter.setLabel(I18nProperties.getCaption(Captions.region));
		provinceFilter.setPlaceholder(I18nProperties.getCaption(Captions.regionAllRegions));
		provinceFilter.setClearButtonVisible(true);

//		provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		provinceFilter.addValueChangeListener(e -> {
			RegionReferenceDto selectedRegion = e.getValue();
			if (selectedRegion != null) {
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(e.getValue().getUuid());
					districtFilter.setItems(districts);
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(e.getValue().getUuid());
					districtFilter.setItems(districts);
				} else {
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(selectedRegion.getUuid());
					districtFilter.setItems(districts);
				}
				criteria.region(selectedRegion);
				refreshGridData(formAccess);
			} else {
				criteria.region(null);
				refreshGridData(formAccess);
			}
		});

		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder(I18nProperties.getCaption(Captions.districtAllDistricts));
//		districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReference());
		districtFilter.setClearButtonVisible(true);
		districtFilter.addValueChangeListener(e -> {
			DistrictReferenceDto selectedDistrict = e.getValue();
			if (selectedDistrict != null) {
				criteria.district(selectedDistrict);
				refreshGridData(formAccess);
			} else {
				criteria.district(null);
				refreshGridData(formAccess);

			}
		});

		configureFiltersByUserRoles();

		errorFilter.setClearButtonVisible(true);
		errorFilter.setLabel("Error Status");
		errorFilter.setPlaceholder("Error Status");
		errorFilter.setItems("Error Report", "None Error Report");
		errorFilter.addValueChangeListener(e -> {
//			DistrictReferenceDto selectedDistrict = e.getValue();
			criteria.setError_status(e.getValue());
			refreshGridData(formAccess);

		});

		resetButton = new Button(I18nProperties.getCaption(Captions.actionResetFilters));
		resetButton.addClickListener(e -> {
			campaign.clear();
			provinceFilter.clear();
			districtFilter.clear();
			regionFilter.clear();
			criteria.area(null);
			criteria.region(null);
			criteria.district(null);

			refreshGridData(formAccess);

		});

		Button displayFilters = new Button(I18nProperties.getCaption(Captions.hideFilters),
				new Icon(VaadinIcon.SLIDERS));
		displayFilters.addClickListener(e -> {
			if (filterLayout.isVisible() == false) {
				filterLayout.setVisible(true);
				displayFilters.setText(I18nProperties.getCaption(Captions.hideFilters));
			} else {
				filterLayout.setVisible(false);
				displayFilters.setText(I18nProperties.getCaption(Captions.showFilters));
			}
		});

		exportReport.setIcon(new Icon(VaadinIcon.UPLOAD));
		exportReport.setText(I18nProperties.getCaption(Captions.export));
		exportReport.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");
		});
		anchor.getStyle().set("display", "none");

		filterLayout.setClassName("row pl-3");
		filterLayout.add(campaign, regionFilter, provinceFilter, districtFilter, errorFilter, resetButton, exportReport,
				anchor);

		HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(Alignment.END);
		layout.getStyle().set("margin-left", "15px");
		layout.add(displayFilters, filterLayout);

		add(layout);

		completionAnalysisGrid(criteria, formAccess);

	}

	public void reload() {
		grid.getDataProvider().refreshAll();
		criteria.campaign(campaign.getValue());
		criteria.area(regionFilter.getValue());
//		criteria.setFormType(campaignPhase.getValue().toString());
//		criteria.setCampaignFormMeta(campaignFormCombo.getValue());
//		criteria.area(regionCombo.getValue());
//		criteria.region(provinceCombo.getValue());
//		criteria.district(districtCombo.getValue());
//		criteria.community(clusterCombo.getValue());
	}

	@SuppressWarnings("deprecation")
	private void completionAnalysisGrid(CampaignFormDataCriteria criteria, FormAccess formAccess) {

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

//		grid.addColumn(CampaignFormDataIndexDto::getCampaign).setHeader(I18nProperties.getCaption(Captions.Campaigns)).setSortable(true).setResizable(true);

		ComponentRenderer<Span, CampaignFormDataIndexDto> cCodeRenderer = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getCcode()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> clusterNumberRenderer = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getClusternumber()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> icmSupervisor = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_a()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> icmRevisit = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_b()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> icmHouseholds = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_c()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> icmTeamMonitoring = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_d()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			grid.addColumn(CampaignFormDataIndexDto::getArea).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortProperty("region").setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortProperty("province").setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getDistrict)
					.setHeader(I18nProperties.getCaption(Captions.district)).setSortProperty("district")
					.setSortable(true).setResizable(true);
			grid.addColumn(cCodeRenderer).setHeader(I18nProperties.getCaption(Captions.Community_externalID))
					.setSortProperty("ccode").setSortable(true).setResizable(true);
			grid.addColumn(clusterNumberRenderer).setHeader(I18nProperties.getCaption(Captions.clusterNumber))
					.setSortProperty("clusterNumber").setSortable(true).setResizable(true);
			grid.addColumn(icmSupervisor).setHeader(I18nProperties.getCaption(Captions.icmSupervisorMonitoring))
					.setSortProperty("supervisor").setSortable(true).setResizable(true);
			grid.addColumn(icmRevisit).setHeader(I18nProperties.getCaption(Captions.icmRevisits))
					.setSortProperty("revisit").setSortable(true).setResizable(true);
			grid.addColumn(icmHouseholds).setHeader(I18nProperties.getCaption(Captions.icmHouseholdMonitoring))
					.setSortProperty("household").setSortable(true).setResizable(true);
			grid.addColumn(icmTeamMonitoring).setHeader(I18nProperties.getCaption(Captions.icmTeamMonitoring))
					.setSortProperty("teammonitori").setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getError_status)
					.setHeader(I18nProperties.getCaption("Error Status")).setSortProperty("errorfilter")
					.setSortable(true).setResizable(true);
			grid.setVisible(true);
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			grid.addColumn(CampaignFormDataIndexDto::getArea).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortProperty("region").setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortProperty("province").setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getDistrict)
					.setHeader(I18nProperties.getCaption(Captions.district)).setSortProperty("district")
					.setSortable(true).setResizable(true);
			grid.addColumn(cCodeRenderer).setHeader(I18nProperties.getCaption(Captions.Community_externalID))
					.setSortProperty("ccode").setSortable(true).setResizable(true);
			grid.addColumn(clusterNumberRenderer).setHeader(I18nProperties.getCaption(Captions.clusterNumber))
					.setSortProperty("clusterNumber").setSortable(true).setResizable(true);
			grid.addColumn(icmSupervisor).setHeader(I18nProperties.getCaption(Captions.icmSupervisorMonitoring))
					.setSortProperty("supervisor").setSortable(true).setResizable(true);
			grid.addColumn(icmRevisit).setHeader(I18nProperties.getCaption(Captions.icmRevisits))
					.setSortProperty("revisit").setSortable(true).setResizable(true);
			grid.addColumn(icmHouseholds).setHeader(I18nProperties.getCaption(Captions.icmHouseholdMonitoring))
					.setSortProperty("household").setSortable(true).setResizable(true);
			grid.addColumn(icmTeamMonitoring).setHeader(I18nProperties.getCaption(Captions.icmTeamMonitoring))
					.setSortProperty("teammonitori").setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getError_status)
					.setHeader(I18nProperties.getCaption("Error Status")).setSortProperty("errorfilter")
					.setSortable(true).setResizable(true);
			grid.setVisible(true);
		} else {
			grid.addColumn(CampaignFormDataIndexDto::getArea).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortProperty("region").setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortProperty("province").setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getDistrict)
					.setHeader(I18nProperties.getCaption(Captions.district)).setSortProperty("district")
					.setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getCcode)
					.setHeader(I18nProperties.getCaption(Captions.Community_externalID)).setSortProperty("ccode")
					.setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getClusternumber)
					.setHeader(I18nProperties.getCaption(Captions.clusterNumber)).setSortProperty("clusterNumber")
					.setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getAnalysis_a)
					.setHeader(I18nProperties.getCaption(Captions.icmSupervisorMonitoring))
					.setSortProperty("supervisor").setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getAnalysis_b)
					.setHeader(I18nProperties.getCaption(Captions.icmRevisits)).setSortProperty("revisit")
					.setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getAnalysis_c)
					.setHeader(I18nProperties.getCaption(Captions.icmHouseholdMonitoring)).setSortProperty("household")
					.setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getAnalysis_d)
					.setHeader(I18nProperties.getCaption(Captions.icmTeamMonitoring)).setSortProperty("teammonitori")
					.setSortable(true).setResizable(true);
			grid.addColumn(CampaignFormDataIndexDto::getError_status)
					.setHeader(I18nProperties.getCaption("Error Status")).setSortProperty("errorfilter")
					.setSortable(true).setResizable(true);
			grid.setVisible(true);
		}

		String numberOfRows = FacadeProvider.getCampaignFormDataFacade().getByCompletionAnalysisCount(criteria, null,
				null, null, null);
		criteria.campaign(lastStarted);
//		int numberOfRows = FacadeProvider.getCampaignFormDataFacade().prepareAllCompletionAnalysis();
		dataProvider = DataProvider.fromFilteringCallbacks(
				query -> FacadeProvider.getCampaignFormDataFacade()
						.getByCompletionAnalysis(criteria, query.getOffset(), query.getLimit(),
								query.getSortOrders().stream()
										.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
												sortOrder.getDirection() == SortDirection.ASCENDING))
										.collect(Collectors.toList()),
								null)
						.stream(),
				query -> Integer.parseInt(numberOfRows));
		grid.setDataProvider(dataProvider);

		GridExporter<CampaignFormDataIndexDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);

		exporter.setTitle(I18nProperties.getCaption(Captions.campaignDataInformation));

		String exportFileName = "Completion_Analysis_" + campaign.getValue().toString() + "_"
				+ new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime());

		exporter.setFileName(exportFileName);

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("campDatAnchor");

		anchor.getStyle().set("width", "100px");

		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());
		add(grid);

	}

	public void generateProvinceComboItems(UserProvider user) {
		provinceFilter.clear();

		criteria.setArea(regionFilter.getValue());
		refreshGridData(formAccess);
		if (user.getUser().getLanguage().toString().equals("Pashto")) {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(regionFilter.getValue().getUuid());
			provinceFilter.setItems(provinces);
		} else if (user.getUser().getLanguage().toString().equals("Dari")) {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaDari(regionFilter.getValue().getUuid());
			provinceFilter.setItems(provinces);
		} else {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(regionFilter.getValue().getUuid());
			provinceFilter.setItems(provinces);
		}
		provinceFilter.setEnabled(true);

	}

	public void generateDistrictComboItems(UserProvider user) {

		System.out.println("================1111111111111");
		districtFilter.clear();

		criteria.setRegion(provinceFilter.getValue());
		refreshGridData(formAccess);
		if (user.getUser().getLanguage().toString().equals("Pashto")) {
			districts = FacadeProvider.getDistrictFacade()
					.getAllActiveByRegionPashto(provinceFilter.getValue().getUuid());
			districtFilter.setItems(districts);
		} else if (user.getUser().getLanguage().toString().equals("Dari")) {
			districts = FacadeProvider.getDistrictFacade()
					.getAllActiveByRegionDari(provinceFilter.getValue().getUuid());
			districtFilter.setItems(districts);
		} else {
			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(provinceFilter.getValue().getUuid());
			districtFilter.setItems(districts);
		}
		districtFilter.setEnabled(true);

	}

	public void configureFiltersByUserRoles() {
		if (userProvider.getUser().getArea() != null) {
			regionFilter.setValue(userProvider.getUser().getArea());
			criteria.setArea(userProvider.getUser().getArea());
			regionFilter.setEnabled(false);
			generateProvinceComboItems(userProvider);
		}

		if (userProvider.getUser().getRegion() != null) {
			provinceFilter.setValue(userProvider.getUser().getRegion());
			criteria.setRegion(userProvider.getUser().getRegion());
			provinceFilter.setEnabled(false);
			generateDistrictComboItems(userProvider);
		}

		if (userProvider.getUser().getDistrict() != null) {
			districtFilter.setValue(userProvider.getUser().getDistrict());
			criteria.setDistrict(userProvider.getUser().getDistrict());
			refreshGridData(formAccess);
			districtFilter.setEnabled(false);
//		generateDistrictComboItems();
		}
	}

}
