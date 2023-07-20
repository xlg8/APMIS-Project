package com.cinoteck.application.views.campaigndata;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;



import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("Campaign Data")
@Route(value = "campaigndata", layout = MainLayout.class)
public class CampaignDataView extends VerticalLayout {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 7588118851062483372L;

	private Grid<CampaignFormDataIndexDto> grid = new Grid<>(CampaignFormDataIndexDto.class, false);
//	private GridListDataView<CampaignFormDataIndexDto> dataView;
	private CampaignFormDataCriteria criteria;
	private CampaignFormMetaDto formMetaReference;
	private CampaignFormDataDto campaignFormDatadto;

	ComboBox<String> campaignYear = new ComboBox<>();
	ComboBox<CampaignReferenceDto> campaignz = new ComboBox<>();
	ComboBox<CampaignPhase> campaignPhase = new ComboBox<>();
	ComboBox<CampaignFormMetaReferenceDto> campaignFormCombo = new ComboBox<>();
	ComboBox<AreaReferenceDto> regionCombo = new ComboBox<>();
	ComboBox<RegionReferenceDto> provinceCombo = new ComboBox<>();
	ComboBox<DistrictReferenceDto> districtCombo = new ComboBox<>();
	ComboBox<CommunityReferenceDto> clusterCombo = new ComboBox<>();
	ComboBox<CampaignFormElementImportance> importanceSwitcher = new ComboBox<>();
	Button resetHandler = new Button();
//	Button applyHandler = new Button();
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;
	List<CampaignFormMetaReferenceDto> campaignForms;

	
	UserProvider userProvider = new UserProvider();

	CampaignFormDataEditForm campaignFormDataEditForm;

	private DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider;

	public CampaignDataView() {
		setSizeFull();
		setSpacing(false);
		criteria = new CampaignFormDataCriteria();
		createCampaignDataFilter();
		
		configureGrid(criteria);
	}
	
	private void createCampaignDataFilter() {
		setMargin(true);
		
		ComboBox<CampaignFormMetaReferenceDto> newForm = new ComboBox<>();
		newForm.setLabel(I18nProperties.getCaption(Captions.actionNewForm));
		newForm.setPlaceholder("Data Entry");
		newForm.setTooltipText("Drop down of list of available forms. Note: closed forms and expire form will not appear here");
		newForm.setClearButtonVisible(true);
		newForm.getStyle().set("padding-top", "0px !important");
		//((HasPrefixAndSuffix) newForm).setPrefixComponent(VaadinIcon.SEARCH.create());		
		
		

		Button importData = new Button(I18nProperties.getCaption(Captions.actionImport), new Icon(VaadinIcon.PLUS_CIRCLE));
		
		
		VerticalLayout filterBlock = new VerticalLayout();
		filterBlock.setSpacing(true);
		filterBlock.setMargin(true);
		filterBlock.setClassName("campaignDataFilterParent");
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(Alignment.END);
		
		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));

		
		HorizontalLayout actionButtonlayout = new HorizontalLayout();
		actionButtonlayout.setVisible(false);
		actionButtonlayout.setAlignItems(Alignment.END);
		actionButtonlayout.add(campaignYear, campaignz, campaignPhase, newForm, importData);

		HorizontalLayout level1Filters = new HorizontalLayout();
		level1Filters.setPadding(false);
		level1Filters.setVisible(false);
		level1Filters.setAlignItems(Alignment.END);
		level1Filters.add(campaignFormCombo, regionCombo, provinceCombo, districtCombo, clusterCombo,
				importanceSwitcher, resetHandler);

		

		

		displayFilters.addClickListener(e -> {
			if (!level1Filters.isVisible()) {
				actionButtonlayout.setVisible(true);
				level1Filters.setVisible(true);
				displayFilters.setText("Hide Filters");
			} else {
				actionButtonlayout.setVisible(false);
				level1Filters.setVisible(false);
				displayFilters.setText("Show Filters");
			}
		});
		

		TextArea resultField = new TextArea();
		resultField.setWidth("100%");
		
		campaignYear.setLabel("Campaign Year");
		campaignYear.getStyle().set("padding-top", "0px !important");

		campaignz.setLabel("Campaign");
		campaignz.getStyle().set("padding-top", "0px !important");

		campaignPhase.setLabel("Campaign Phase");
		campaignPhase.getStyle().set("padding-top", "0px !important");
		
		

		
		campaignFormCombo.setLabel("Form");
		campaignFormCombo.getStyle().set("padding-top", "0px !important");
		campaignFormCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");

		regionCombo.setLabel(I18nProperties.getCaption(Captions.region));
		regionCombo.getStyle().set("padding-top", "0px !important");
		regionCombo.setPlaceholder("Regions");

		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		regionCombo.setItems(regions);
		if(userProvider.getUser().getArea() != null) {
			regionCombo.setValue(userProvider.getUser().getArea());
			regionCombo.setEnabled(false);
		}

		provinceCombo.setLabel("Province");
		provinceCombo.getStyle().set("padding-top", "0px !important");
		provinceCombo.setPlaceholder("Provinces");
		provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
		provinceCombo.setItems(provinces);
		if(userProvider.getUser().getRegion() != null) {
			provinceCombo.setValue(userProvider.getUser().getRegion());
		}
		provinceCombo.setEnabled(false);

		provinceCombo.getStyle().set("padding-top", "0px");
		provinceCombo.setClassName("col-sm-6, col-xs-6");

		districtCombo.setLabel(I18nProperties.getCaption(Captions.district));
		districtCombo.getStyle().set("padding-top", "0px !important");
		districtCombo.setPlaceholder("Districts");
		districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
		districtCombo.setItems(districts);
		if(userProvider.getUser().getDistrict() != null) {
			districtCombo.setValue(userProvider.getUser().getDistrict());
		}
		districtCombo.setEnabled(false);
		districtCombo.getStyle().set("padding-top", "0px");
		districtCombo.setClassName("col-sm-6, col-xs-6");

		clusterCombo.setLabel("Cluster");
		clusterCombo.getStyle().set("padding-top", "0px !important");;
//		if(userProvider.getUser().getCommunity() != null) {
//			clusterCombo.setValue(userProvider.getUser().getCommunity());
//		}
		clusterCombo.setPlaceholder("Clusters");
		clusterCombo.setEnabled(false);

		// TODO Importance filter switcher should be visible only on the change of form
		importanceSwitcher.setLabel("Importance");
		importanceSwitcher.getStyle().set("padding-top", "0px !important");;
		importanceSwitcher.setPlaceholder("Importance");
		importanceSwitcher.setItems(CampaignFormElementImportance.values());
		importanceSwitcher.setClearButtonVisible(true);
		importanceSwitcher.setReadOnly(true);
		importanceSwitcher.setTooltipText("Select Form first");
		importanceSwitcher.addValueChangeListener(e -> {

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
					}else if(allAndImportantFormElements){
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

		});
		
		//Initialize Item lists
		List<CampaignReferenceDto> campaigns = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
		CampaignReferenceDto lastStarted = FacadeProvider.getCampaignFacade().getLastStartedCampaign();
		List<String> camYearList = campaigns.stream().map(CampaignReferenceDto::getCampaignYear).distinct()
				.collect(Collectors.toList());
		
		campaignYear.setItems(camYearList);
		campaignYear.setValue(lastStarted.getCampaignYear());
		
		List<CampaignReferenceDto> allCampaigns = campaigns.stream()
				.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());
		
		campaignz.setItems(allCampaigns);
		campaignz.setValue(lastStarted);
		
		if(userProvider.getUser().getUsertype() == UserType.EOC_USER) {
			campaignPhase.setItems(CampaignPhase.INTRA, CampaignPhase.POST);
			campaignPhase.setValue(CampaignPhase.INTRA);
		}else {
			campaignPhase.setItems(CampaignPhase.values());
			campaignPhase.setValue(CampaignPhase.PRE);
		}
		 
		campaignForms = FacadeProvider.getCampaignFormMetaFacade()
		.getAllCampaignFormMetasAsReferencesByRoundandCampaign(campaignPhase.getValue().toString().toLowerCase(),
				campaignz.getValue().getUuid());
		
		campaignFormCombo.setItems(campaignForms);
		campaignFormCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");

		newForm.setItems(campaignForms);
		
		criteria.campaign(lastStarted);
		criteria.setFormType(campaignPhase.getValue().toString());
		
		// Configure Comboboxes Value Change Listeners
		campaignYear.addValueChangeListener(e -> {
			campaignz.clear();
			List<CampaignReferenceDto> allCampaigns_ = campaigns.stream().filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());
			campaignz.setItems(allCampaigns_);
			campaignz.setValue(allCampaigns_.get(0));
		});

		campaignz.addValueChangeListener(e -> {
		if(e.getValue() != null) {
				campaignFormCombo.clear();
				newForm.clear();
				List<CampaignFormMetaReferenceDto> campaignFormReferences_ = FacadeProvider.getCampaignFormMetaFacade()
						.getAllCampaignFormMetasAsReferencesByRoundandCampaign(campaignPhase.getValue().toString().toLowerCase(),
								e.getValue().getUuid());
				campaignFormCombo.setItems(campaignFormReferences_);
				newForm.setItems(campaignFormReferences_);
				reload();
				
			}
		});

		campaignPhase.addValueChangeListener(e -> {
			
			campaignFormCombo.clear();
			newForm.clear();
			List<CampaignFormMetaReferenceDto> campaignFormReferences_ = FacadeProvider.getCampaignFormMetaFacade()
					.getAllCampaignFormMetasAsReferencesByRoundandCampaign(e.getValue().toString().toLowerCase(),
							campaignz.getValue().getUuid());
			campaignFormCombo.setItems(campaignFormReferences_);
			newForm.setItems(campaignFormReferences_);

			reload();
		});

		campaignFormCombo.addValueChangeListener(e -> {
			
			if(e.getValue() != null) {
			formMetaReference = FacadeProvider.getCampaignFormMetaFacade()
					.getCampaignFormMetaByUuid(e.getValue().getUuid());

			reload();
				//add method to update the grid after this combo changes
				importanceSwitcher.setReadOnly(false);
				importanceSwitcher.clear();
				
			}else {
				
				importanceSwitcher.clear();
				importanceSwitcher.setReadOnly(true);
				
			}

		});

		regionCombo.addValueChangeListener(e -> {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
			provinceCombo.setItems(provinces);
			provinceCombo.setEnabled(true);
			reload();
});

		provinceCombo.addValueChangeListener(e -> {
			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
			districtCombo.setItems(districts);
			districtCombo.setEnabled(true);
			reload();
		});

		districtCombo.addValueChangeListener(e -> {
			communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
			clusterCombo.setItems(communities);
			clusterCombo.setEnabled(true);
			reload();
		});

		clusterCombo.addValueChangeListener(e -> {
			reload();
		});

		
		newForm.addValueChangeListener(e -> {
			if(e.getValue() != null && campaignz != null) {
				
				CampaignFormDataEditForm cam = new CampaignFormDataEditForm(e.getValue(), campaignz.getValue(), false, null, grid);
				//add(cam);
			
				newForm.setValue(null);
			}
 		});
 		
		resetHandler.setText("Reset Filters");


		resetHandler.addClickListener(e -> {
	
//		    // Refresh the grid and update criteria as needed
	   
			UI.getCurrent().getPage().reload();
		    criteria.campaign(lastStarted);
		    criteria.setFormType(campaignPhase.getValue().toString());
		});


		
		
//		applyHandler.setText("Apply Filters");

		
		
		

		layout.add(displayFilters, actionButtonlayout);

		filterBlock.add(layout, level1Filters);

		add(filterBlock);
	}
	
	public void reload() {
		grid.getDataProvider().refreshAll();
		criteria.campaign(campaignz.getValue());
		criteria.setFormType(campaignPhase.getValue().toString());
		criteria.setCampaignFormMeta(campaignFormCombo.getValue());
		criteria.area(regionCombo.getValue());
		criteria.region(provinceCombo.getValue());
		criteria.district(districtCombo.getValue());
		criteria.community(clusterCombo.getValue());
	}



	@SuppressWarnings("deprecation")
	private void configureGrid(CampaignFormDataCriteria criteria) {
		setMargin(false);
//		grid.removeAllColumns();

		grid.setSelectionMode(SelectionMode.SINGLE);

	//	grid.setSelectionMode(SelectionMode.MULTI);

//		grid.setSizeFull();
		
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(CampaignFormDataIndexDto.CAMPAIGN).setHeader(I18nProperties.getCaption(Captions.Campaign)).setSortable(true).setResizable(true);//.setFooter(String.format("Row Count: %s", (int) FacadeProvider.getCampaignFormDataFacade().count(criteria)));
		grid.addColumn(CampaignFormDataIndexDto.FORM).setHeader("Form").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.AREA).setHeader(I18nProperties.getCaption(Captions.region)).setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.RCODE).setHeader("RCode").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.REGION).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.PCODE).setHeader("PCode").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.DISTRICT).setHeader(I18nProperties.getCaption(Captions.district)).setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.DCODE).setHeader("DCode").setSortable(true).setResizable(true);
		Column<CampaignFormDataIndexDto> comm = grid.addColumn(CampaignFormDataIndexDto.COMMUNITY).setHeader("Cluster")
				.setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.COMMUNITYNUMBER).setHeader(I18nProperties.getCaption(Captions.clusterNumber)).setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.CCODE).setHeader("CCode").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.FORM_DATE).setHeader("Form Date").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.FORM_TYPE).setHeader("Form Phase").setSortable(true).setResizable(true);

		grid.setVisible(true);
		grid.setWidthFull();
//		grid.setHeightFull();
		grid.setAllRowsVisible(false);


		grid.asSingleSelect().addValueChangeListener(e -> {
			
			
		//editCampaignFormData(e.getValue());
		CampaignFormDataDto formData = FacadeProvider.getCampaignFormDataFacade().getCampaignFormDataByUuid(e.getValue().getUuid());
		
		CampaignFormDataEditForm cam = new CampaignFormDataEditForm(formData.getCampaignFormMeta(), campaignz.getValue(), true, formData.getUuid(), grid);
				
	});

		dataProvider = DataProvider.fromFilteringCallbacks(this::fetchCampaignFormData, this::countCampaignFormData);
		grid.setDataProvider(dataProvider);

		GridExporter<CampaignFormDataIndexDto> exporter = GridExporter.createFor(grid);
//	    exporter.setExportValue(comm, item -> "" + item);
//	    exporter.setColumnPosition(lastNameCol, 1);
		exporter.setTitle("Campaign Data information"); 
		exporter.setFileName("GridExport" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		exporter.setCsvExportEnabled(true);
		exporter.setPdfExportEnabled(false);
		exporter.setExcelExportEnabled(false);
		exporter.setDocxExportEnabled(false);
//grid.getFooterRows().get(0).
//		grid.appendFooterRow();
		add(grid);

	}

	private void editCampaignFormData(CampaignFormDataIndexDto selected) {
		selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			CampaignFormDataDto formData = FacadeProvider.getCampaignFormDataFacade()
					.getCampaignFormDataByUuid(selected.getUuid());
			openFormLayout(formData);
		}
	}

	private void openFormLayout(CampaignFormDataDto formData) {

		FormLayout formLayout = new FormLayout();
		
		
		ComboBox<Object> cbCampaign = new ComboBox<>(CampaignFormDataDto.CAMPAIGN);
		cbCampaign.setItems(formData.getCampaign());
		cbCampaign.setValue(formData.getCampaign());
		cbCampaign.setEnabled(false);
		
		
		Date date = new Date();

		// Convert Date to LocalDate
		LocalDate localDate = formData.getFormDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		DatePicker formDate = new DatePicker();
		formDate.setValue(localDate);

		ComboBox<Object> cbArea = new ComboBox<>(CampaignFormDataDto.AREA);
		cbArea.setItems(formData.getArea());
		cbArea.setValue(formData.getArea());
//	        cbArea.setItems(FacadeProvider.getAreaFacade().getAllActiveAndSelectedAsReference(campaignUUID));

		ComboBox<Object> cbRegion = new ComboBox<>(CampaignFormDataDto.REGION);
		cbRegion.setItems(formData.getRegion());
		cbRegion.setValue(formData.getRegion());

		ComboBox<Object> cbDistrict = new ComboBox<>(CampaignFormDataDto.DISTRICT);
		cbDistrict.setItems(formData.getDistrict());
		cbDistrict.setValue(formData.getDistrict());

		ComboBox<Object> cbCommunity = new ComboBox<>(CampaignFormDataDto.COMMUNITY);
		cbCommunity.setItems(formData.getCommunity());
		cbCommunity.setValue(formData.getCommunity());

		formLayout.add(cbCampaign, formDate, cbArea, cbRegion, cbDistrict, cbCommunity);
		

		Dialog dialog = new Dialog();
		dialog.add(formLayout);
//		dialog.setSizeFull();
		dialog.open();
	}

	private Stream<CampaignFormDataIndexDto> fetchCampaignFormData(
			Query<CampaignFormDataIndexDto, CampaignFormDataCriteria> query) {
		return FacadeProvider.getCampaignFormDataFacade()
				.getIndexList(criteria, query.getOffset(), query.getLimit(), query.getSortOrders().stream()
						.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
								sortOrder.getDirection() == SortDirection.ASCENDING))
						.collect(Collectors.toList()))
				.stream();
	}

	private int countCampaignFormData(Query<CampaignFormDataIndexDto, CampaignFormDataCriteria> query) {
		return (int) FacadeProvider.getCampaignFormDataFacade().count(criteria);
	}

	private void export(Grid<CampaignFormDataIndexDto> grid, TextArea result) {
		// Fetch all data from the grid in the current sorted order
		Stream<CampaignFormDataIndexDto> persons = null;
		Set<CampaignFormDataIndexDto> selection = grid.asMultiSelect().getValue();
		if (selection != null && selection.size() > 0) {
			persons = selection.stream();
		} else {
//			persons = dataView.getItems();
		}

		StringWriter output = new StringWriter();
		StatefulBeanToCsv<CampaignFormDataIndexDto> writer = new StatefulBeanToCsvBuilder<CampaignFormDataIndexDto>(
				output).build();
		try {
			writer.write(persons);
		} catch (Exception e) {
			output.write("An error occured during writing: " + e.getMessage());
		}

		result.setValue(output.toString());
	}

	private void setDataProvider() {
		DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider = DataProvider
				.fromFilteringCallbacks(
						query -> FacadeProvider.getCampaignFormDataFacade()
								.getIndexList(criteria, query.getOffset(), query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()))
								.stream(),
						query -> (int) FacadeProvider.getCampaignFormDataFacade().count(criteria));
		grid.setDataProvider(dataProvider);
	}

	public void addCustomColumn(String property, String caption) {
		if (!property.toString().contains("readonly")) {

			grid.addColumn(
					e -> e.getFormValues().stream().filter(v -> v.getId().equals(property)).findFirst().orElse(null))
					.setHeader(caption).setSortable(true).setResizable(true);

		}

	}

	private void closeEditor() {
		campaignFormDataEditForm.setVisible(false);
		grid.setVisible(true);
		removeClassName("editing");
	}

}