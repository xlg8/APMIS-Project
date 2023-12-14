
package com.cinoteck.application.views.reports;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.campaigndata.CampaignFormElementImportance;
import com.cinoteck.application.views.utils.gridexporter.GridExporter;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import com.vaadin.flow.component.grid.Grid.Column;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.CampaignJurisdictionLevel;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.data.translation.TranslationElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsCriteria;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserType;

@SuppressWarnings("serial")
@Route(layout = ReportView.class)
public class AggregateReportView extends VerticalLayout implements RouterLayout {
	ComboBox<String> campaignYear = new ComboBox<>();
	ComboBox<CampaignReferenceDto> campaignz = new ComboBox<>();
	CampaignStatisticsCriteria criteria;
	ComboBox<String> groupBy = new ComboBox<>();
	ComboBox<CampaignFormMetaReferenceDto> campaignFormCombo = new ComboBox<>();
	ComboBox<AreaReferenceDto> regionCombo = new ComboBox<>();
	ComboBox<RegionReferenceDto> provinceCombo = new ComboBox<>();
	ComboBox<DistrictReferenceDto> districtCombo = new ComboBox<>();
	ComboBox<CommunityReferenceDto> clusterCombo = new ComboBox<>();
	ComboBox<CampaignFormElementImportance> importanceSwitcher = new ComboBox<>();

	Button resetHandler = new Button();

	Button exportReport = new Button();

	private Grid<CampaignStatisticsDto> grid = new Grid<>(CampaignStatisticsDto.class, false);
	ListDataProvider<CampaignStatisticsDto> dataProvider;
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;
	List<CampaignFormMetaReferenceDto> campaignForms;
	private CampaignFormMetaDto formMetaReference;

	private UserProvider userProvider = new UserProvider();

	private Consumer<CampaignFormMetaReferenceDto> formMetaChangedCallback;

	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	Icon icon = VaadinIcon.UPLOAD_ALT.create();

	public AggregateReportView() {
		setSpacing(false);
		criteria = new CampaignStatisticsCriteria();
		criteria.setGroupingLevel(CampaignJurisdictionLevel.AREA);
		addFilter();
		configureGrid(criteria);
	}

	public void configureGrid(CampaignStatisticsCriteria criteria) {
		this.criteria = criteria;
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		setSizeFull();
		grid.setColumnReorderingAllowed(true);

//		ComponentRenderer<Span, CampaignStatisticsDto> areaTRanslationRenderer = new ComponentRenderer<>(input -> {
//			String value = input.getArea();
//			
//			Span label = new Span(value);
//			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
//			return label;
//		});

		ComponentRenderer<Span, CampaignStatisticsDto> formCountRender = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}

			String value = String.valueOf(arabicFormat.format(input.getFormCount()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		grid.addColumn(CampaignStatisticsDto.CAMPAIGN).setHeader(I18nProperties.getCaption(Captions.Campaign))
				.setSortable(true).setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Campaign));
		grid.addColumn(CampaignStatisticsDto.FORM).setHeader(I18nProperties.getCaption(Captions.formname))
				.setSortable(true).setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.formname));
		grid.addColumn(CampaignStatisticsDto.AREA).setHeader(I18nProperties.getCaption(Captions.area)).setSortable(true)
				.setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
		grid.addColumn(CampaignStatisticsDto.REGION).setHeader(I18nProperties.getCaption(Captions.region))
				.setSortable(true).setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.region)).setVisible(false);
		grid.addColumn(CampaignStatisticsDto.DISTRICT).setHeader(I18nProperties.getCaption(Captions.district))
				.setSortable(true).setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.district)).setVisible(false);
		grid.addColumn(CampaignStatisticsDto.COMMUNITY).setHeader(I18nProperties.getCaption(Captions.community))
				.setSortable(true).setResizable(true).setAutoWidth(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.community)).setVisible(false);

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			grid.addColumn(formCountRender).setHeader(I18nProperties.getCaption(Captions.formCount)).setSortable(true)
					.setResizable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.formCount));
			grid.setVisible(true);
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			grid.addColumn(formCountRender).setHeader(I18nProperties.getCaption(Captions.formCount)).setSortable(true)
					.setResizable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.formCount));
			grid.setVisible(true);
		} else {
			grid.addColumn(CampaignStatisticsDto.FORM_COUNT).setHeader(I18nProperties.getCaption(Captions.formCount))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.formCount));
			grid.setVisible(true);
		}
		dataProvider = DataProvider.fromStream(getGridData().stream());

		grid.setDataProvider(dataProvider);

		GridExporter<CampaignStatisticsDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);

		exporter.setTitle(I18nProperties.getCaption(Captions.campaignDataInformation));
		String exportFileName = "Aggregate_Report_" + campaignz.getValue().toString() + "_"
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

	public void addFilter() {
		UserProvider user = new UserProvider();
		AreaReferenceDto userArea = user.getUser().getArea();
		RegionReferenceDto userRegion = user.getUser().getRegion();
		DistrictReferenceDto userDistrict = user.getUser().getDistrict();
		CommunityReferenceDto userCommunity = null;
		if (userArea != null) {
			regionCombo.setEnabled(false);
			if (user.getUser().getLanguage().toString().equals("Pashto")) {
				provinceCombo.setItems(FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(userArea.getUuid()));
			} else if (user.getUser().getLanguage().toString().equals("Dari")) {
				provinceCombo.setItems(FacadeProvider.getRegionFacade().getAllActiveByAreaDari(userArea.getUuid()));
			} else {
				provinceCombo.setItems(FacadeProvider.getRegionFacade().getAllActiveByArea(userArea.getUuid()));
			}
			if (userRegion != null) {
				provinceCombo.setEnabled(false);
				if (user.getUser().getLanguage().toString().equals("Pashto")) {
					districtCombo.setItems(FacadeProvider.getDistrictFacade()
							.getAllActiveByRegionPashto(user.getUser().getRegion().getUuid()));
				} else if (user.getUser().getLanguage().toString().equals("Dari")) {
					districtCombo.setItems(FacadeProvider.getDistrictFacade()
							.getAllActiveByRegionDari(user.getUser().getRegion().getUuid()));
				} else {
					districtCombo.setItems(FacadeProvider.getDistrictFacade()
							.getAllActiveByRegion(user.getUser().getRegion().getUuid()));
				}
				if (userDistrict != null) {
					districtCombo.setEnabled(false);
					// communityFilter.addItems(FacadeProvider.getCommunityFacade().getAllActiveByDistrict(userDistrict.getUuid()));
					// if (userCommunity != null) {
					// communityFilter.setEnabled(false);
					// }
				}
			}
		}

		criteria.setRegion(user.getUser().getRegion());// .setArea(user.getArea());
		criteria.setDistrict(user.getUser().getDistrict());
//		criteria.region(user.getRegion());// .setRegion(user.getRegion());
//		criteria.district(user.getDistrict()); // .setDistrict(user.getDistrict());
////		

		Button displayFilters = new Button(I18nProperties.getCaption(Captions.hideFilters),
				new Icon(VaadinIcon.SLIDERS));

		HorizontalLayout actionButtonlayout = new HorizontalLayout();
		actionButtonlayout.setVisible(true);
		actionButtonlayout.setAlignItems(Alignment.END);

		actionButtonlayout.setClassName("row pl-3");
		actionButtonlayout.add(campaignYear, campaignz, groupBy, campaignFormCombo, regionCombo, provinceCombo,
				districtCombo, importanceSwitcher, resetHandler, exportReport, anchor);

		displayFilters.addClickListener(e -> {
			if (!actionButtonlayout.isVisible()) {
				actionButtonlayout.setVisible(true);
				displayFilters.setText(I18nProperties.getCaption(Captions.hideFilters));
			} else {
				actionButtonlayout.setVisible(false);
				displayFilters.setText(I18nProperties.getCaption(Captions.showFilters));
			}
		});

		campaignYear.setLabel(I18nProperties.getCaption(Captions.campaignYear));
		campaignYear.getStyle().set("padding-top", "0px !important");
		campaignYear.getStyle().set("width", "145px !important");

		campaignz.setLabel(I18nProperties.getCaption(Captions.Campaign));
		campaignz.getStyle().set("padding-top", "0px !important");
		campaignz.getStyle().set("width", "145px !important");

		groupBy.setLabel(I18nProperties.getCaption(Captions.campaignDiagramGroupBy));
		groupBy.getStyle().set("padding-top", "0px !important");
		groupBy.getStyle().set("width", "145px !important");

		// Initialize Item lists
		List<CampaignReferenceDto> campaigns = FacadeProvider.getCampaignFacade().getAllCampaignByStartDate();
		CampaignReferenceDto lastStarted = FacadeProvider.getCampaignFacade().getLastStartedCampaign();
		List<String> camYearList = campaigns.stream().map(CampaignReferenceDto::getCampaignYear).distinct()
				.collect(Collectors.toList());

		campaignYear.setItems(camYearList);
		campaignYear.setValue(lastStarted.getCampaignYear());

		List<CampaignReferenceDto> allCampaigns = campaigns.stream()
				.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());

		campaignz.setItems(allCampaigns);
		campaignz.setValue(lastStarted);
		campaignz.setClearButtonVisible(true);
		criteria.setCampaign(lastStarted);

//		if(userProvider.getUser().getUsertype() == UserType.EOC_USER) {
//			campaignPhase.setItems(CampaignPhase.INTRA, CampaignPhase.POST);
//			campaignPhase.setValue(CampaignPhase.INTRA);
//		}else {
//			campaignPhase.setItems(CampaignPhase.values());
//			campaignPhase.setValue(CampaignPhase.PRE);
//		}

//		criteria.campaign(lastStarted);

		campaignFormCombo.setLabel(I18nProperties.getCaption(Captions.campaignCampaignForm));
		campaignFormCombo.getStyle().set("padding-top", "0px !important");
		campaignFormCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");
		campaignFormCombo.getStyle().set("width", "145px !important");
		campaignFormCombo.setClearButtonVisible(true);

		List<CampaignFormMetaReferenceDto> campaignFormReferences_ = FacadeProvider.getCampaignFormMetaFacade()
				.getCampaignFormMetasAsReferencesByCampaign(campaignz.getValue().getUuid());

		campaignFormCombo.setItems(campaignFormReferences_);
		campaignFormCombo.addValueChangeListener(event -> {
			System.out.println(event.getValue() + "event valueeeeeeeeeeeeeeeeeeeeeeeeee");
			if (event.getValue() != null) {
				formMetaReference = FacadeProvider.getCampaignFormMetaFacade()
						.getCampaignFormMetaByUuid(event.getValue().getUuid());
				criteria.setCampaignFormMeta(event.getValue());

				reloadData();
				importanceSwitcher.setReadOnly(false);
				importanceSwitcher.clear();
			} else {
				criteria.setCampaignFormMeta(event.getValue());
				reloadData();
				importanceSwitcher.clear();
				importanceSwitcher.setReadOnly(true);

			}
		});

		regionCombo.setLabel(I18nProperties.getCaption(Captions.area));
		regionCombo.getStyle().set("padding-top", "0px !important");
		regionCombo.getStyle().set("width", "145px !important");
		regionCombo.setClearButtonVisible(true);
		regionCombo.setPlaceholder(I18nProperties.getCaption(Captions.area));

		if (user.getUser().getLanguage().toString().equals("Pashto")) {
			regionCombo.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto());
		} else if (user.getUser().getLanguage().toString().equals("Dari")) {
			regionCombo.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari());
		} else {
			regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
			regionCombo.setItems(regions);
		}

		regionCombo.setEnabled(true);
		regionCombo.addValueChangeListener(e -> {
			criteria.setArea(e.getValue());
			reloadData();
			if (user.getUser().getLanguage().toString().equals("Pashto")) {
				provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(e.getValue().getUuid());
				provinceCombo.setItems(provinces);
			} else if (user.getUser().getLanguage().toString().equals("Dari")) {
				provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaDari(e.getValue().getUuid());
				provinceCombo.setItems(provinces);
			} else {
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
				provinceCombo.setItems(provinces);
			}
			provinceCombo.setEnabled(true);
		});

		provinceCombo.setLabel(I18nProperties.getCaption(Captions.region));
		provinceCombo.getStyle().set("padding-top", "0px !important");
		provinceCombo.getStyle().set("width", "145px !important");
		provinceCombo.setClearButtonVisible(true);

		provinceCombo.setPlaceholder(I18nProperties.getCaption(Captions.region));
		provinceCombo.setEnabled(false);
		provinceCombo.getStyle().set("padding-top", "0px");
		provinceCombo.setClassName("col-sm-6, col-xs-6");

		provinceCombo.addValueChangeListener(e -> {
			criteria.setRegion(e.getValue());
			reloadData();
			if (user.getUser().getLanguage().toString().equals("Pashto")) {
				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(e.getValue().getUuid());
				districtCombo.setItems(districts);
			} else if (user.getUser().getLanguage().toString().equals("Dari")) {
				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(e.getValue().getUuid());
				districtCombo.setItems(districts);
			} else {
				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
				districtCombo.setItems(districts);
			}
			districtCombo.setEnabled(true);

		});

		districtCombo.setLabel(I18nProperties.getCaption(Captions.district));
		districtCombo.getStyle().set("padding-top", "0px !important");
		districtCombo.getStyle().set("width", "145px !important");
		districtCombo.setClearButtonVisible(true);

		districtCombo.setPlaceholder(I18nProperties.getCaption(Captions.district));
		districtCombo.setEnabled(false);
		districtCombo.getStyle().set("padding-top", "0px");
		districtCombo.setClassName("col-sm-6, col-xs-6");
		districtCombo.addValueChangeListener(e -> {
			criteria.setDistrict(e.getValue());

			reloadData();
		});

		if (userProvider.getUser().getArea() != null) {
			regionCombo.setValue(userProvider.getUser().getArea());
			criteria.setArea(userProvider.getUser().getArea());
			regionCombo.setEnabled(false);
			generateProvinceComboItems(userProvider);
		}

		if (userProvider.getUser().getRegion() != null) {
			provinceCombo.setValue(userProvider.getUser().getRegion());
			criteria.setRegion(userProvider.getUser().getRegion());
			provinceCombo.setEnabled(false);
			generateDistrictComboItems(userProvider);
		}

		if (userProvider.getUser().getDistrict() != null) {
			districtCombo.setValue(userProvider.getUser().getDistrict());
			criteria.setDistrict(userProvider.getUser().getDistrict());
			reloadData();
			districtCombo.setEnabled(false);
//			generateDistrictComboItems();
		}

		groupBy.setItems(

				I18nProperties.getCaption(Captions.Campaign_area), I18nProperties.getCaption(Captions.Campaign_region),
				I18nProperties.getCaption(Captions.Campaign_district)

		);

		importanceSwitcher.setLabel(I18nProperties.getCaption(Captions.importance));
		importanceSwitcher.getStyle().set("padding-top", "0px !important");
		importanceSwitcher.setClassName("col-sm-6, col-xs-6");
		importanceSwitcher.getStyle().set("width", "145px !important");

		importanceSwitcher.setPlaceholder(I18nProperties.getCaption(Captions.importance));
		importanceSwitcher.setItems(CampaignFormElementImportance.values());
		importanceSwitcher.setClearButtonVisible(true);
		importanceSwitcher.setReadOnly(true);
		importanceSwitcher.setTooltipText(I18nProperties.getCaption(Captions.selectFormFirst));

		importanceSwitcher.addValueChangeListener(e -> {
			System.out.println(e.getValue() + " importance value at every value changfe");
			if (e.getValue() != null) {
				if (formMetaReference != null) {
					grid.removeAllColumns();
					configureGrid(criteria);

					final boolean allAndImportantFormElements = e.getValue() == CampaignFormElementImportance.ALL;
					final boolean onlyImportantFormElements = e.getValue() == CampaignFormElementImportance.IMPORTANT;

					final List<CampaignFormElement> campaignFormElements = formMetaReference.getCampaignFormElements();

					for (CampaignFormElement element : campaignFormElements) {

						if (element.isImportant() && onlyImportantFormElements) {
							String caption = null;
							if (caption == null) {
								caption = element.getCaption();
							}

							if (caption != null) {
								addCustomColumn(element.getId(), caption);
							}
						} else if (allAndImportantFormElements) {
							String caption = null;
							if (caption == null) {
								caption = element.getCaption();
							}
							if (caption != null) {
								addCustomColumn(element.getId(), caption);
							}
						} else if (element.isImportant()) {
							String caption = null;
							if (caption == null) {
								caption = element.getCaption();
							}
							if (caption != null) {
								addCustomColumn(element.getId(), caption);
							}
						}
					}
				}
			} else {
				grid.removeAllColumns();
				configureGrid(criteria);

			}

			System.out.println(formMetaReference + "form meta reference at evenery value changge ");

		});
		System.out.println(campaignFormCombo.getValue() + " initial campaign fformr  combovalue ");
		campaignYear.addValueChangeListener(e -> {
			campaignz.clear();
			List<CampaignReferenceDto> allCampaigns_ = campaigns.stream()
					.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());
			campaignz.setItems(allCampaigns_);
			campaignz.setValue(allCampaigns_.get(0));
		});

		campaignz.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				criteria.setCampaign(e.getValue());
				reloadData();
				List<CampaignFormMetaReferenceDto> campaignFormReferences_byCampUUIDx = FacadeProvider
						.getCampaignFormMetaFacade().getCampaignFormMetasAsReferencesByCampaign(e.getValue().getUuid());

				campaignFormCombo.clear();
				campaignFormCombo.setItems(campaignFormReferences_byCampUUIDx);

			} else {
				criteria.setCampaign(e.getValue());
				reloadData();
			}
		});

		groupBy.setClearButtonVisible(true);
		groupBy.addValueChangeListener(e -> {

			// TODO: Improve the initialization of this code
			CampaignJurisdictionLevel groupingValue = CampaignJurisdictionLevel.AREA;
			if (e.getValue() != null) {
				String selectorValue = e.getValue().toString();
				if (selectorValue.equals(I18nProperties.getCaption(Captions.area))) {
					groupingValue = CampaignJurisdictionLevel.AREA;
				} else if (selectorValue.equals(I18nProperties.getCaption(Captions.region))) {
					groupingValue = CampaignJurisdictionLevel.REGION;
				} else if (selectorValue.equals(I18nProperties.getCaption(Captions.district))) {
					groupingValue = CampaignJurisdictionLevel.DISTRICT;
				} else {
					// TODO add throwable here to make sure user does not inject insto the system
				}
			}

			criteria.setGroupingLevel(groupingValue);
			setColumnsVisibility(groupingValue);
			reloadData();
		});

		resetHandler.setText(I18nProperties.getCaption(Captions.resetFilters));

		resetHandler.addClickListener(e -> {

//		    // Refresh the grid and update criteria as needed

			UI.getCurrent().getPage().reload();
//		    criteria.campaign(lastStarted);
//		    criteria.setFormType(campaignPhase.getValue().toString());
		});

		exportReport.setIcon(new Icon(VaadinIcon.UPLOAD));
		exportReport.setText(I18nProperties.getCaption(Captions.export));
		exportReport.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");
		});
		anchor.getStyle().set("display", "none");

		VerticalLayout filterBlock = new VerticalLayout();
		filterBlock.setSpacing(true);
		filterBlock.setMargin(true);
		filterBlock.setClassName("campaignDataFilterParent");

		HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(Alignment.END);

		layout.add(displayFilters, actionButtonlayout);

		filterBlock.add(layout);

		add(filterBlock);
	}

	public void addCustomColumn(String property, String caption) {
		Column<CampaignStatisticsDto> newColumn = grid.addColumn(
				e -> e.getStatisticsData().stream().filter(v -> v.getId().equals(property)).findFirst().orElse(null));
		newColumn.setSortable(false);
		newColumn.setHeader(caption);// .setCaption(caption);
		newColumn.setId(property);
		newColumn.setResizable(true);
		newColumn.setTooltipGenerator(e -> newColumn.getHeaderText());

	}

	public void setColumnsVisibility(CampaignJurisdictionLevel groupingLevel) {
		setAreaColumnVisible(CampaignJurisdictionLevel.AREA.equals(groupingLevel)
				|| CampaignJurisdictionLevel.REGION.equals(groupingLevel));
		setRegionColumnVisible(CampaignJurisdictionLevel.REGION.equals(groupingLevel)
				|| CampaignJurisdictionLevel.DISTRICT.equals(groupingLevel)
				|| CampaignJurisdictionLevel.COMMUNITY.equals(groupingLevel));
		setDistrictColumnVisible(CampaignJurisdictionLevel.DISTRICT.equals(groupingLevel)
				|| CampaignJurisdictionLevel.COMMUNITY.equals(groupingLevel));
		setCommunityColumnVisible(CampaignJurisdictionLevel.COMMUNITY.equals(groupingLevel));
	}

	private void setAreaColumnVisible(boolean visible) {
		// getColumn(CampaignStatisticsDto.AREA).setHidden(!visible);
	}

	private void setRegionColumnVisible(boolean visible) {
		grid.getColumnByKey(CampaignStatisticsDto.REGION).setVisible(visible);

	}

	private void setDistrictColumnVisible(boolean visible) {
		grid.getColumnByKey(CampaignStatisticsDto.DISTRICT).setVisible(visible);
	}

	private void setCommunityColumnVisible(boolean visible) {
		grid.getColumnByKey(CampaignStatisticsDto.COMMUNITY).setVisible(visible);
	}

	public void reloadData() {

		dataProvider = DataProvider.fromStream(getGridData().stream());

		grid.setDataProvider(dataProvider);
//	     
	}

	public void setCriteria(CampaignStatisticsCriteria criteria) {
		this.criteria = criteria;
		reloadData();
	}

	private CampaignStatisticsCriteria getCriteria() {
		return criteria;
	}

	private List<CampaignStatisticsDto> getGridData() {
		return FacadeProvider.getCampaignStatisticsFacade().getCampaignStatistics(getCriteria());
	}

	protected void applyDependenciesOnNewValue(CampaignStatisticsCriteria criteria) {
		campaignFormCombo.clear();
		if (criteria.getCampaign() != null) {
			if (UserProvider.getCurrent().hasUserType(UserType.EOC_USER)) {
				campaignFormCombo.setItems(FacadeProvider.getCampaignFormMetaFacade()
						.getCampaignFormMetaAsReferencesByCampaignPostCamapaign(criteria.getCampaign().getUuid()));
			} else {
				campaignFormCombo.setItems(FacadeProvider.getCampaignFormMetaFacade()
						.getCampaignFormMetasAsReferencesByCampaign(criteria.getCampaign().getUuid()));
			}
		} else {
			campaignFormCombo
					.setItems(FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferences());
		}
	}

	public void setFormMetaChangedCallback(Consumer<CampaignFormMetaReferenceDto> formMetaChangedCallback) {
		this.formMetaChangedCallback = formMetaChangedCallback;
		campaignFormCombo.addValueChangeListener(e -> formMetaChangedCallback.accept(e.getValue()));
	}

	public void generateProvinceComboItems(UserProvider user) {
		provinceCombo.clear();

		criteria.setArea(regionCombo.getValue());
		reloadData();
		if (user.getUser().getLanguage().toString().equals("Pashto")) {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(regionCombo.getValue().getUuid());
			provinceCombo.setItems(provinces);
		} else if (user.getUser().getLanguage().toString().equals("Dari")) {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByAreaDari(regionCombo.getValue().getUuid());
			provinceCombo.setItems(provinces);
		} else {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(regionCombo.getValue().getUuid());
			provinceCombo.setItems(provinces);
		}
		provinceCombo.setEnabled(true);

	}

	public void generateDistrictComboItems(UserProvider user) {

		System.out.println("================1111111111111");
		districtCombo.clear();

		criteria.setRegion(provinceCombo.getValue());
		reloadData();
		if (user.getUser().getLanguage().toString().equals("Pashto")) {
			districts = FacadeProvider.getDistrictFacade()
					.getAllActiveByRegionPashto(provinceCombo.getValue().getUuid());
			districtCombo.setItems(districts);
		} else if (user.getUser().getLanguage().toString().equals("Dari")) {
			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(provinceCombo.getValue().getUuid());
			districtCombo.setItems(districts);
		} else {
			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(provinceCombo.getValue().getUuid());
			districtCombo.setItems(districts);
		}
		districtCombo.setEnabled(true);

	}

}
