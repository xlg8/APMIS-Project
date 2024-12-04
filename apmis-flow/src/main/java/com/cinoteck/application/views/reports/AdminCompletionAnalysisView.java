package com.cinoteck.application.views.reports;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.SortProperty;

@Route(layout = CompletionAnalysisTabsheet.class)
public class AdminCompletionAnalysisView extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6119982616227990093L;
	private ComboBox<CampaignReferenceDto> campaign = new ComboBox<>();
	private ComboBox<CampaignFormMetaReferenceDto> adminFormsInCampaign = new ComboBox<>();

	private ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>();
	private ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>();
	private ComboBox<DistrictReferenceDto> districtFilter = new ComboBox<>();
	private Button resetButton;

	List<CampaignReferenceDto> campaigns;
	List<CampaignFormMetaReferenceDto> adminFormInCampaignList;
	List<CampaignReferenceDto> campaignPhases;
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	CampaignFormDataCriteria criteria = new CampaignFormDataCriteria();
	final Grid<CampaignFormDataIndexDto> grid_ = new Grid<>(CampaignFormDataIndexDto.class, false);
	CampaignReferenceDto lastStarted = FacadeProvider.getCampaignFacade().getLastStartedCampaign();

	DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider;
	FormAccess formAccess;

	Button exportReport = new Button();
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	Icon icon = VaadinIcon.UPLOAD_ALT.create();

	private UserProvider userProvider = new UserProvider();

	private void refreshGridData(FormAccess formAccess) {
		System.out.println("______________AMIN_____________");

		dataProvider = DataProvider.fromFilteringCallbacks(
				// Fetching the data
				query -> {
					try {
						// Retrieve the data using the FacadeProvider
						return FacadeProvider.getCampaignFormDataFacade()
								.getByCompletionAnalysisAdmin(criteria, query.getOffset(), query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()),
										null)
								.stream();
					} catch (Exception e) {
						// Log the exception and return an empty stream if an error occurs
						e.printStackTrace();
						return Stream.empty();
					}
				},
				// Counting the number of rows
				query -> {
					try {
						// Get the count from the FacadeProvider
						return Integer.parseInt(
								FacadeProvider.getCampaignFormDataFacade().getByCompletionAnalysisCountAdmin(criteria,
										query.getOffset(), query.getLimit(), null, null));
					} catch (Exception e) {
						// Log the exception and return 0 if an error occurs
						e.printStackTrace();
						return 0;
					}
				});
		grid_.setDataProvider(dataProvider);

	}

	public AdminCompletionAnalysisView() {
		System.out.println("++++++ADMINNNNNNN+++++++");
		this.criteria = new CampaignFormDataCriteria();

		setHeightFull();

		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setPadding(false);
		filterLayout.setVisible(true);
		filterLayout.setAlignItems(Alignment.END);
		filterLayout.setId("adminAnalysisID");

		campaign.setLabel(I18nProperties.getCaption(Captions.Campaigns));
		campaign.setPlaceholder(I18nProperties.getCaption(Captions.campaignAllCampaigns));
		campaigns = FacadeProvider.getCampaignFacade().getAllCampaignByStartDate();
		campaign.setItems(campaigns);
		campaign.setValue(lastStarted);
		criteria.campaign(lastStarted);
		campaign.addValueChangeListener(e -> {
			CampaignReferenceDto selectedCAmpaign = e.getValue();

			System.out.println(selectedCAmpaign + "selectedCAmpaignselectedCAmpaign");
			if (selectedCAmpaign != null) {
				criteria.campaign(selectedCAmpaign);
//				adminFormsInCampaign.clear();
				adminFormInCampaignList = FacadeProvider.getCampaignFormMetaFacade()
						.getCampaignFormByCampaignAndFormType(criteria.getCampaign().getUuid(), "ADMIN");
				adminFormsInCampaign.setItems(adminFormInCampaignList);
				System.out.println(adminFormInCampaignList
						+ "adminFormInCampaignListadminFormInCampaignListadminFormInCampaignList");

				refreshGridData(null);
			} else {
				criteria.campaign(null);
				adminFormInCampaignList = new ArrayList<CampaignFormMetaReferenceDto>();
				adminFormsInCampaign.setItems(adminFormInCampaignList);

				refreshGridData(null);
			}

		});
		

		adminFormsInCampaign.setLabel(I18nProperties.getCaption(Captions.Campaigns) + " Admin Forms");
		adminFormsInCampaign.setPlaceholder(I18nProperties.getCaption(Captions.campaignAllCampaigns) + " Admin Forms");
		adminFormInCampaignList = FacadeProvider.getCampaignFormMetaFacade()
				.getCampaignFormByCampaignAndFormType(criteria.getCampaign().getUuid(), "ADMIN");
		adminFormsInCampaign.setItems(adminFormInCampaignList);
		adminFormsInCampaign.setValue(adminFormInCampaignList.get(0));
		adminFormsInCampaign.addValueChangeListener(e -> {
			if (e.getValue() == null) {
					criteria.setCampaignFormMeta(e.getValue());
					refreshGridData(null);
			}else{// + "value uuuuuuuuidiiiiiiiiiiiidididididi");
				criteria.setCampaignFormMeta(e.getValue());
				e.getValue().getUuid();
				refreshGridData(null);

				if (e.getValue().toString().contains("3")) {
					grid_.getColumnByKey("dayOne").setVisible(true);
					grid_.getColumnByKey("dayTwo").setVisible(true);
					grid_.getColumnByKey("dayThree").setVisible(true);
					grid_.getColumnByKey("dayFour").setVisible(false);
					grid_.getColumnByKey("dayFive").setVisible(false);
					grid_.getColumnByKey("daySix").setVisible(false);
					grid_.getColumnByKey("daySeven").setVisible(false);
				} else if (e.getValue().toString().contains("4")) {
					grid_.getColumnByKey("dayOne").setVisible(true);
					grid_.getColumnByKey("dayTwo").setVisible(true);
					grid_.getColumnByKey("dayThree").setVisible(true);
					grid_.getColumnByKey("dayFour").setVisible(true);
					grid_.getColumnByKey("dayFive").setVisible(false);
					grid_.getColumnByKey("daySix").setVisible(false);
					grid_.getColumnByKey("daySeven").setVisible(false);
				} else if (e.getValue().toString().contains("7")) {
					grid_.getColumnByKey("dayOne").setVisible(true);
					grid_.getColumnByKey("dayTwo").setVisible(true);
					grid_.getColumnByKey("dayThree").setVisible(true);
					grid_.getColumnByKey("dayFour").setVisible(true);
					grid_.getColumnByKey("dayFive").setVisible(true);
					grid_.getColumnByKey("daySix").setVisible(true);
					grid_.getColumnByKey("daySeven").setVisible(true);
				}
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
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(selectedArea.getUuid());
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					provinceFilter.setItems(
							FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(selectedArea.getUuid()));
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					provinceFilter
							.setItems(FacadeProvider.getRegionFacade().getAllActiveByAreaDari(selectedArea.getUuid()));
				} else {
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
//		provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		provinceFilter.setClearButtonVisible(true);
		provinceFilter.addValueChangeListener(e -> {
			RegionReferenceDto selectedRegion = e.getValue();
			if (selectedRegion != null) {
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					districtFilter.setItems(
							FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(selectedRegion.getUuid()));
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					districtFilter.setItems(
							FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(selectedRegion.getUuid()));
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
			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
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
		filterLayout.add(campaign, adminFormsInCampaign, regionFilter, provinceFilter, districtFilter, resetButton,
				exportReport, anchor);

		HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(Alignment.END);
		layout.getStyle().set("margin-left", "15px");
		layout.add(displayFilters, filterLayout);

		add(layout);

		completionAnalysisGrid(criteria, formAccess);

	}

	public void reload() {
		grid_.getDataProvider().refreshAll();
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

		grid_.setSelectionMode(SelectionMode.SINGLE);
		grid_.setMultiSort(true, MultiSortPriority.APPEND);
		grid_.setSizeFull();
		grid_.setColumnReorderingAllowed(true);

//		grid_.addColumn(CampaignFormDataIndexDto::getCampaign).setHeader(I18nProperties.getCaption(Captions.Campaigns)).setSortable(true).setResizable(true);

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

		ComponentRenderer<Span, CampaignFormDataIndexDto> day1Renderer = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_a_()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> day2Renderer = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_b_()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> day3Renderer = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_c_()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> day4Renderer = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_d_()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> day5Renderer = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_e_()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> day6Renderer = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_f_()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormDataIndexDto> day7Renderer = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getAnalysis_g_()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			grid_.addColumn(CampaignFormDataIndexDto::getArea).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortProperty("region").setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortProperty("province").setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getDistrict)
					.setHeader(I18nProperties.getCaption(Captions.district)).setSortProperty("district")
					.setSortable(true).setResizable(true);
			grid_.addColumn(cCodeRenderer).setHeader(I18nProperties.getCaption(Captions.Community_externalID))
					.setSortProperty("ccode").setSortable(true).setResizable(true);
			grid_.addColumn(clusterNumberRenderer).setHeader(I18nProperties.getCaption(Captions.clusterNumber))
					.setSortProperty("clusterNumberr").setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getCommunity)
					.setHeader(I18nProperties.getCaption(Captions.community)).setSortProperty("cluster")
					.setSortable(true).setResizable(true);
			grid_.addColumn(day1Renderer).setHeader("Day 1").setSortProperty("dayOne").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmSupervisorMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(day2Renderer).setHeader("Day 2").setSortProperty("dayTwo").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmRevisits)).setSortable(true).setResizable(true);
			grid_.addColumn(day3Renderer).setHeader("Day 3").setSortProperty("dayThree").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmHouseholdMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(day4Renderer).setHeader("Day 4").setSortProperty("dayFour").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmTeamMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(day5Renderer).setHeader("Day 5").setSortProperty("dayFive").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmRevisits)).setSortable(true).setResizable(true);
			grid_.addColumn(day6Renderer).setHeader("Day 6").setSortProperty("daySix").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmHouseholdMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(day7Renderer).setHeader("Day 7").setSortProperty("daySeven").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmTeamMonitoring)).setSortable(true).setResizable(true);

		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			grid_.addColumn(CampaignFormDataIndexDto::getArea).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortProperty("region").setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortProperty("province").setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getDistrict)
					.setHeader(I18nProperties.getCaption(Captions.district)).setSortProperty("district")
					.setSortable(true).setResizable(true);
			grid_.addColumn(cCodeRenderer).setHeader(I18nProperties.getCaption(Captions.Community_externalID))
					.setSortProperty("ccode").setSortable(true).setResizable(true);
			grid_.addColumn(clusterNumberRenderer).setHeader(I18nProperties.getCaption(Captions.clusterNumber))
					.setSortProperty("clusterNumberr").setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getCommunity)
					.setHeader(I18nProperties.getCaption(Captions.community)).setSortProperty("cluster")
					.setSortable(true).setResizable(true);
			grid_.addColumn(day1Renderer).setHeader("Day 1").setSortProperty("dayOne").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmSupervisorMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(day2Renderer).setHeader("Day 2").setSortProperty("dayTwo").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmRevisits)).setSortable(true).setResizable(true);
			grid_.addColumn(day3Renderer).setHeader("Day 3").setSortProperty("dayThree").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmHouseholdMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(day4Renderer).setHeader("Day 4").setSortProperty("dayFour").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmTeamMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(day5Renderer).setHeader("Day 5").setSortProperty("dayFive").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmRevisits)).setSortable(true).setResizable(true);
			grid_.addColumn(day6Renderer).setHeader("Day 6").setSortProperty("daySix").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmHouseholdMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(day7Renderer).setHeader("Day 7").setSortProperty("daySeven").setSortable(true)
					.setResizable(true);// I18nProperties.getCaption(Captions.icmTeamMonitoring)).setSortable(true).setResizable(true);

		} else {
			grid_.addColumn(CampaignFormDataIndexDto::getArea).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortProperty("region").setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortProperty("province").setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getDistrict)
					.setHeader(I18nProperties.getCaption(Captions.district)).setSortProperty("district")
					.setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getCcode)
					.setHeader(I18nProperties.getCaption(Captions.Community_externalID)).setSortProperty("ccode")
					.setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getClusternumber)
					.setHeader(I18nProperties.getCaption(Captions.clusterNumber)).setSortProperty("clusterNumberr")
					.setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getCommunity)
					.setHeader(I18nProperties.getCaption(Captions.community)).setSortProperty("cluster")
					.setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getAnalysis_a_).setHeader("Day 1").setSortProperty("dayOne")
					.setKey("dayOne").setSortable(true).setResizable(true);// I18nProperties.getCaption(Captions.icmSupervisorMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getAnalysis_b_).setHeader("Day 2").setSortProperty("dayTwo")
					.setKey("dayTwo").setSortable(true).setResizable(true);// I18nProperties.getCaption(Captions.icmRevisits)).setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getAnalysis_c_).setHeader("Day 3").setSortProperty("dayThree")
					.setKey("dayThree").setSortable(true).setResizable(true);// I18nProperties.getCaption(Captions.icmHouseholdMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getAnalysis_d_).setHeader("Day 4").setSortProperty("dayFour")
					.setKey("dayFour").setSortable(true).setResizable(true);// I18nProperties.getCaption(Captions.icmTeamMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getAnalysis_e_).setHeader("Day 5").setSortProperty("dayFive")
					.setKey("dayFive").setSortable(true).setResizable(true);// I18nProperties.getCaption(Captions.icmRevisits)).setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getAnalysis_f_).setHeader("Day 6").setSortProperty("daySix")
					.setKey("daySix").setSortable(true).setResizable(true);// I18nProperties.getCaption(Captions.icmHouseholdMonitoring)).setSortable(true).setResizable(true);
			grid_.addColumn(CampaignFormDataIndexDto::getAnalysis_g_).setHeader("Day 7").setSortProperty("daySeven")
					.setKey("daySeven").setSortable(true).setResizable(true);// I18nProperties.getCaption(Captions.icmTeamMonitoring)).setSortable(true).setResizable(true);

		}

		grid_.setVisible(true);
//		int numberOfRows = FacadeProvider.getCampaignFormDataFacade()
//				.getByCompletionAnalysisCount(null, null, null, null,formAccess );
		criteria.campaign(lastStarted);

		dataProvider = DataProvider.fromFilteringCallbacks(
				// Fetching the data
				query -> {
					try {
						// Retrieve the data using the FacadeProvider
						return FacadeProvider.getCampaignFormDataFacade()
								.getByCompletionAnalysisAdmin(criteria, query.getOffset(), query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()),
										null)
								.stream();
					} catch (Exception e) {
						// Log the exception and return an empty stream if an error occurs
						e.printStackTrace();
						return Stream.empty();
					}
				},
				// Counting the number of rows
				query -> {
					try {
						// Get the count from the FacadeProvider
						return Integer.parseInt(
								FacadeProvider.getCampaignFormDataFacade().getByCompletionAnalysisCountAdmin(criteria,
										query.getOffset(), query.getLimit(), null, null));
					} catch (Exception e) {
						// Log the exception and return 0 if an error occurs
						e.printStackTrace();
						return 0;
					}
				});
		grid_.setDataProvider(dataProvider);

		GridExporter<CampaignFormDataIndexDto> exporter = GridExporter.createFor(grid_);
		exporter.setAutoAttachExportButtons(false);

		exporter.setTitle(I18nProperties.getCaption(Captions.campaignDataInformation));
		exporter.setFileName("Admin Completion Analysis Report"
				+ new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("campDatAnchor");

		anchor.getStyle().set("width", "100px");

		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());
		add(grid_);

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
//	generateDistrictComboItems();
		}
	}

}
