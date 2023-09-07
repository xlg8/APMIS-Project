package com.cinoteck.application.views.campaign;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.utils.DownloadFlowUtilityView;
import com.cinoteck.application.views.utils.DownloadUtil;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;

import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;
import com.vaadin.shared.Position;
//import com.vaadin.server.FileDownloader;
//import com.vaadin.server.StreamResource;
//import com.vaadin.ui.AbstractComponent;
import com.vaadin.flow.component.treegrid.TreeGrid;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignLogDto;
import de.symeda.sormas.api.campaign.CampaignTreeGridDto;
import de.symeda.sormas.api.campaign.CampaignTreeGridDtoImpl;
import de.symeda.sormas.api.campaign.diagram.CampaignDashboardElement;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDefinitionDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;

@PageTitle("APMIS-Edit Campaign")
@Route(value = "/data")
public class CampaignForm extends VerticalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 764300181578209719L;
	private static final String PRE_CAMPAIGN = "pre-campaign";
	private static final String INTRA_CAMPAIGN = "intra-campaign";
	private static final String POST_CAMPAIGN = "post-campaign";

	Button archiveDearchive = new Button(I18nProperties.getCaption(Captions.actionArchive));

	Button openCloseCampaign;
	Button duplicateCampaign;
	Button deleteCampaign;
	Button publishUnpublishCampaign;

	Button discardChanges;
	Button saveChanges;

	Button logButton;

	Binder<CampaignIndexDto> binder = new BeanValidationBinder<>(CampaignIndexDto.class);
	Binder<CampaignDto> binderx = new BeanValidationBinder<>(CampaignDto.class);

	List<CampaignIndexDto> campaignNames, startDates, endDates, descriptions;
//	CampaignRounds rounds;
	private CampaignDto campaignDto;
	private CampaignIndexDto campaignDtox;

	H5 campaignBasics = new H5(I18nProperties.getCaption(Captions.campaignBasics));

	TextField campaignName = new TextField(I18nProperties.getCaption(Captions.Campaign_name));
	ComboBox round = new ComboBox<>(I18nProperties.getCaption(Captions.round));
	DatePicker startDate = new DatePicker(I18nProperties.getCaption(Captions.Campaign_startDate));
	DatePicker endDate = new DatePicker(I18nProperties.getCaption(Captions.Campaign_endDate));
	TextField creatingUser = new TextField(I18nProperties.getCaption(Captions.Campaign_creatingUser));
	TextField creatingUuid = new TextField(I18nProperties.getCaption(Captions.uuid));
	TextField campaaignYear = new TextField(I18nProperties.getCaption(Captions.campaignYear));

	UUID uuid = UUID.randomUUID();
	TextArea description = new TextArea(I18nProperties.getCaption(Captions.description));

	TreeGrid<CampaignTreeGridDto> treeGrid = new TreeGrid<>();

	HorizontalLayout actionButtonsLayout = new HorizontalLayout();

	private Set<AreaReferenceDto> areass = new HashSet<>();;
	private Set<RegionReferenceDto> region = new HashSet<>();
	private Set<DistrictReferenceDto> districts = new HashSet<>();
	private Set<CommunityReferenceDto> community = new HashSet<>();
	private Set<PopulationDataDto> popopulationDataDtoSet = new HashSet<>();

	private final VerticalLayout statusChangeLayout;
	private Boolean isCreateForm = null;

	CampaignFormMetaReferenceDto xx;
	CampaignDto formDatac;
	boolean isArchived;
	boolean isPublished;
	boolean isOpenClose;
	boolean editMode;

	Set<CampaignFormMetaReferenceDto> selectedFormData = new HashSet<>();
	List<CampaignFormMetaReferenceDto> vvvv;
	CampaignFormGridComponent comp;

	CampaignFormGridComponent compp;

	CampaignFormGridComponent comppp;

	CampaignDashboardGridElementComponent comp1;

	
	CampaignDashboardGridElementComponent compp2;

	CampaignDashboardGridElementComponent comppp2;

	public CampaignForm(CampaignDto formData) {

		this.statusChangeLayout = new VerticalLayout();
		this.formDatac = formData;

		isCreateForm = formData == null;

		statusChangeLayout.setSpacing(false);
		statusChangeLayout.setMargin(false);
		add(statusChangeLayout);

		addClassName("campaign-form");
		configureFields(formData);

	}

	public LocalDate convertToLocalDateViaMilisecond(Date dateToConvert) {
		return Instant.ofEpochMilli(dateToConvert.getDate()).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	private boolean validateDates() {
		LocalDate startDateValue = startDate.getValue();
		LocalDate endDateValue = endDate.getValue();

		if (startDateValue == null || endDateValue == null) {

			Notification notification = new Notification();
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//			notification.setPosition(Position.MIDDLE_CENTER);
			Button closeButton = new Button(new Icon("lumo", "cross"));
			closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			closeButton.getElement().setAttribute("aria-label", "Close");
			closeButton.addClickListener(event -> {
				notification.close();
			});

			Paragraph text = new Paragraph(
					"Please Check the Input Data : Enter a valid Start Date and End Date to continue.");

			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);

			notification.add(layout);
			notification.open();

//			saveChanges.setTooltipText("Please Check the Input Data for Errors");
			saveChanges.setEnabled(false);
//
			return false;
		}

		if (startDateValue.isAfter(endDateValue)) {
			startDate.setInvalid(true);
			endDate.setInvalid(true);

			Notification notification = new Notification();
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//			notification.setPosition(Position.MIDDLE_CENTER);
			Button closeButton = new Button(new Icon("lumo", "cross"));
			closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			closeButton.getElement().setAttribute("aria-label", "Close");
			closeButton.addClickListener(event -> {
				notification.close();
			});

			Paragraph text = new Paragraph(
					"Please Check the Input Data : End Date has to be after or on the same day as Start Date.");

			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);

			notification.add(layout);
			notification.open();
			saveChanges.setEnabled(false);
//			saveChanges.setTooltipText("Please Check the Input Data for Errors");
//			startDate.setHelperText("Please Check the Input Data : End Date has to be after or on the same day as Start Date.");
//			endDate.setHelperText("Please Check the Input Data : End Date has to be after or on the same day as Start Date.");

			return false; // Start date is after end date
		}

		// Clear invalid state if dates are valid
		startDate.setInvalid(false);
		endDate.setInvalid(false);

		saveChanges.setEnabled(true);

		return true; // Dates are valid
	}

	private void configureFields(CampaignDto formData) {

		this.campaignDto = formData;
		CampaignsView view = new CampaignsView();
		description.getStyle().set("height", "10rem");

		creatingUser.setReadOnly(true);
		creatingUuid.setReadOnly(true);
		campaaignYear.setReadOnly(true);

		UserProvider usr = new UserProvider();
		String curentUse = usr.getUserName();

		creatingUser.setWidthFull();
		creatingUuid.setWidthFull();
		campaaignYear.setWidthFull();

		HorizontalLayout hort = new HorizontalLayout();
		hort.add(creatingUuid, creatingUser, campaaignYear);
		hort.setJustifyContentMode(JustifyContentMode.BETWEEN);

//		round.setItems(CampaignRounds.values());
		round.setItems("NID", "SNID", "Case Respond", "Mopping-Up", "Training");

//		round.setItemLabelGenerator(CampaignDto::getRound);

		if (creatingUuid.getValue() == "" || creatingUuid.getValue() == "") {
			creatingUuid.setValue(uuid.toString().toUpperCase());
			creatingUser.setValue(curentUse);

		}

//		System.out.println(creatingUuid.getValue() + "craeting uuid ");
		binderx.forField(creatingUuid).bind(CampaignDto.UUID);

		binderx.forField(creatingUser).bind(CampaignDto.CREATING_USER_NAME);
		binderx.forField(campaaignYear).bind(CampaignDto.CAMPAIGN_YEAR);

		binderx.forField(campaignName).asRequired(I18nProperties.getString(Strings.campaignNameRequired))
				.bind(CampaignDto.NAME);
		binderx.forField(round).asRequired(I18nProperties.getString(Strings.campaignRoundrequired))
				.bind(CampaignDto.ROUND);

		binderx.forField(startDate).withConverter(new LocalDateToDateConverter()).bind(CampaignDto::getStartDate,
				CampaignDto::setStartDate);

		binderx.forField(endDate).withConverter(new LocalDateToDateConverter()).bind(CampaignDto::getEndDate,
				CampaignDto::setEndDate);

		if (formData != null) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d. M. yyyy");
			if (formData.getStartDate() != null) {
				LocalDate timestamp = formData.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate localDatex = formData.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				String formString = timestamp.format(dateTimeFormatter);
				LocalDate localDate = LocalDate.parse(formString, dateTimeFormatter);
				startDate.setValue(localDate);
				endDate.setValue(localDatex);
			}

		}
		startDate.addValueChangeListener(e -> {
			LocalDate selectedDate = e.getValue();
			int selectedYear = selectedDate.getYear();
			String selectedYearAsString = Integer.toString(selectedYear);

			if (endDate.getValue() != null) {
				validateDates();
				campaaignYear.setValue(selectedYearAsString);
				System.out.println(selectedYearAsString + "Selected Yearaaaaaaaaaaa: " + selectedYear);

			} else if (formData == null || formData != null) {
				campaaignYear.setValue(selectedYearAsString);

				System.out.println(selectedYearAsString + "Selected Year: " + selectedYear);
			}
		});

		endDate.addValueChangeListener(e -> {
			if (startDate.getValue() != null) {
				validateDates();

			}
		});

		binderx.forField(description).asRequired(I18nProperties.getString(Strings.campaignDescriptionRequired)).bind(
				CampaignDto::getDescription,

				CampaignDto::setDescription);

		final HorizontalLayout layoutParent = new HorizontalLayout();
		layoutParent.setWidthFull();

		TabSheet tabsheetParent = new TabSheet();
		layoutParent.add(tabsheetParent);
		VerticalLayout parentTab1 = new VerticalLayout();
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();

		TabSheet tabsheet = new TabSheet();
		layout.add(tabsheet);

		VerticalLayout tab1 = new VerticalLayout();
		
		comp=new CampaignFormGridComponent(this.campaignDto==null?Collections.emptyList():new ArrayList<>(campaignDto.getCampaignFormMetas(PRE_CAMPAIGN)),FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRound(PRE_CAMPAIGN),campaignDto,PRE_CAMPAIGN);

		compp=new CampaignFormGridComponent(this.campaignDto==null?Collections.EMPTY_LIST:new ArrayList<>(campaignDto.getCampaignFormMetas(INTRA_CAMPAIGN)),FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRound(INTRA_CAMPAIGN),campaignDto,INTRA_CAMPAIGN);

		comppp=new CampaignFormGridComponent(this.campaignDto==null?Collections.EMPTY_LIST:new ArrayList<>(campaignDto.getCampaignFormMetas(POST_CAMPAIGN)),FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRound(POST_CAMPAIGN),campaignDto,POST_CAMPAIGN);

		comp1=new CampaignDashboardGridElementComponent(this.campaignDto==null?Collections.EMPTY_LIST:new ArrayList<>(campaignDto.getCampaignDashboardElements(PRE_CAMPAIGN)),getListDashboardFromType(PRE_CAMPAIGN),campaignDto,PRE_CAMPAIGN);

		compp2=new CampaignDashboardGridElementComponent(this.campaignDto==null?Collections.EMPTY_LIST:new ArrayList<>(campaignDto.getCampaignDashboardElements(INTRA_CAMPAIGN)),getListDashboardFromType(INTRA_CAMPAIGN),campaignDto,INTRA_CAMPAIGN);

		comppp2=new CampaignDashboardGridElementComponent(this.campaignDto==null?Collections.EMPTY_LIST:new ArrayList<>(campaignDto.getCampaignDashboardElements(INTRA_CAMPAIGN)),getListDashboardFromType(POST_CAMPAIGN),campaignDto,POST_CAMPAIGN);


		tab1.add(comp);

		// this might blow our in new campaign saying null
//		this.campaignDto = comp.getModifiedDto();

		tabsheet.add(I18nProperties.getCaption(Captions.preCampaignForms), tab1);

		VerticalLayout tab2 = new VerticalLayout();

		tab2.add(comp1);
		tabsheet.add(I18nProperties.getCaption(Captions.preCampaignDashboard), tab2);
		tabsheet.setWidthFull();
		parentTab1.add(layout);
		tabsheetParent.add(I18nProperties.getCaption(Captions.preCampaignPhase), parentTab1);

		VerticalLayout parentTab2 = new VerticalLayout();

		VerticalLayout parentTab3 = new VerticalLayout();

		final HorizontalLayout layoutIntra = new HorizontalLayout();
		layoutIntra.setWidthFull();

		TabSheet tabsheetIntra = new TabSheet();
		layoutIntra.add(tabsheetIntra);

		VerticalLayout tab1Intra = new VerticalLayout();

		H1 text = new H1(I18nProperties.getString(Strings.contentGoeshere));

		tab1Intra.add(compp);
//		this.campaignDto = compp.getModifiedDto();
		tabsheetIntra.add(I18nProperties.getCaption(Captions.intraCampaignForms), tab1Intra);
		tabsheetIntra.setWidthFull();

		System.out.println(this.campaignDto + "campdto null check");

		VerticalLayout tab2Intra = new VerticalLayout();

//		final List<CampaignDashboardElement> intracampaignDashboardElements = FacadeProvider.getCampaignFacade()
//				.getCampaignDashboardElements(null, INTRA_CAMPAIGN);

		tab2Intra.add(compp2);

		tabsheetIntra.add(I18nProperties.getCaption(Captions.intraCampaignDashboard), tab2Intra);
		parentTab2.add(layoutIntra);
		// parentTab2.getStyle().set("color", "green");

		tabsheetParent.add(I18nProperties.getCaption(Captions.intraCampaignPhase), parentTab2);

		final HorizontalLayout layoutPost = new HorizontalLayout();
		layoutPost.setWidthFull();

		TabSheet tabsheetPost = new TabSheet();
		layoutPost.add(tabsheetPost);

		VerticalLayout tab1Post = new VerticalLayout();

		tab1Post.add(comppp);
//		this.campaignDto = comppp.getModifiedDto();
		tabsheetPost.add(I18nProperties.getCaption(Captions.postCampaignForms), tab1Post);

		VerticalLayout tab2Post = new VerticalLayout();

//		final List<CampaignDashboardElement> postcampaignDashboardElements = FacadeProvider.getCampaignFacade()
//				.getCampaignDashboardElements(null, POST_CAMPAIGN);

		tab2Post.add(comppp2);

		tabsheetPost.add(I18nProperties.getCaption(Captions.postCampaignDashboard), tab2Post);
		tabsheetPost.setWidthFull();
		parentTab3.add(layoutPost);
		tabsheetParent.add(I18nProperties.getCaption(Captions.postCampaignPhase), parentTab3);

		System.out.println(campaignDto + "campaign DTOoooooooooooooooooooooooooooooooooo");

		VerticalLayout parentTab4 = new VerticalLayout();
		final HorizontalLayout layoutAssocCamp = new HorizontalLayout();
		layoutAssocCamp.setWidthFull();

		treeGrid.setWidthFull();
		if (campaignDto != null) {

			treeGrid.setItems(generateTreeGridData(formData), CampaignTreeGridDto::getRegionData);

			treeGrid.setWidthFull();

			treeGrid.addHierarchyColumn(CampaignTreeGridDto::getName)
					.setHeader(I18nProperties.getCaption(Captions.Location));

			treeGrid.addColumn(CampaignTreeGridDto::getPopulationData)
					.setHeader(I18nProperties.getCaption(Captions.View_configuration_populationdata_short));

			GridMultiSelectionModel<CampaignTreeGridDto> selectionModel = (GridMultiSelectionModel<CampaignTreeGridDto>) treeGrid
					.setSelectionMode(SelectionMode.MULTI);

			for (AreaReferenceDto root : campaignDto.getAreas()) {
////
				for (CampaignTreeGridDto areax : treeGrid.getTreeData().getRootItems()) {
////
					if (areax.getUuid().equals(root.getUuid())) {
////					
						treeGrid.select(areax);
					}
//////				
					for (RegionReferenceDto region_root : campaignDto.getRegion()) {
////
						for (CampaignTreeGridDto regionx : treeGrid.getTreeData().getChildren(areax)) {
////
							if (regionx.getUuid().equals(region_root.getUuid())) {
								treeGrid.select(regionx);
							}
////						
							for (DistrictReferenceDto district_root : campaignDto.getDistricts()) {
////
								for (CampaignTreeGridDto districtx : treeGrid.getTreeData().getChildren(regionx)) {
////
									if (districtx.getUuid().equals(district_root.getUuid())) {
										treeGrid.select(districtx);
									}
								}
							}
						}
					}
				}
			}
		}
		for (int i = 0; i < treeGrid.getTreeData().getRootItems().size(); i++) {

//			 ftg.setIsClicked(777L);
		}

		treeGrid.addItemClickListener(e -> {

			boolean isSelectedClicked = false;
			// we set 777 to the clicked and selected items and check for it.

			if (e.getItem().getIsClicked() != null) {

				if (e.getItem().getIsClicked() == 777L) {

					// deselect this item
					treeGrid.deselect(e.getItem());

					// deselect its children
					treeGrid.getTreeData().getChildren(e.getItem())
							.forEach(ee -> treeGrid.deselect((CampaignTreeGridDto) ee));

					// deselect its grandchildren
					for (CampaignTreeGridDto firstChildren : treeGrid.getTreeData().getChildren(e.getItem())) {

						treeGrid.getTreeData().getChildren(firstChildren)
								.forEach(ee -> treeGrid.deselect((CampaignTreeGridDto) ee));
					}

					if (!e.getItem().getParentUuid().equals("Area")) {
						// treeGrid.deselect(treeGrid.getTreeData().getParent(e.getItem()));
					}

					e.getItem().setIsClicked(7L);

				} else {

					treeGrid.select(e.getItem());
					e.getItem().setIsClicked(777L);

					treeGrid.getTreeData().getChildren(e.getItem())
							.forEach(ee -> treeGrid.select((CampaignTreeGridDto) ee));

					for (CampaignTreeGridDto firstChildren : treeGrid.getTreeData().getChildren(e.getItem())) {

						treeGrid.getTreeData().getChildren(firstChildren)
								.forEach(ee -> treeGrid.select((CampaignTreeGridDto) ee));
					}

				}
			} else {

				treeGrid.select(e.getItem());
				e.getItem().setIsClicked(777L);
				treeGrid.getTreeData().getChildren(e.getItem())
						.forEach(ee -> treeGrid.select((CampaignTreeGridDto) ee));

				for (CampaignTreeGridDto firstChildren : treeGrid.getTreeData().getChildren(e.getItem())) {

					treeGrid.getTreeData().getChildren(firstChildren)
							.forEach(ee -> treeGrid.select((CampaignTreeGridDto) ee));
				}

			}

			for (CampaignTreeGridDto ftg : treeGrid.getSelectionModel().getSelectedItems()) {
				ftg.setIsClicked(777L);
			}
		});

		// check class of selection and cast to the appropriate class v-tree8-expander
		// collapsed .v-tree .v-tree .sormas .v-tree-expander
		treeGrid.addSelectionListener(event -> {
			areass.clear();
			region.clear();
			districts.clear();
			community.clear();
			popopulationDataDtoSet.clear();

			for (int i = 0; i < event.getAllSelectedItems().size(); i++) {

				if (((CampaignTreeGridDto) event.getAllSelectedItems().toArray()[i]).getLevelAssessed() == "area") {
					AreaReferenceDto selectedArea = FacadeProvider.getAreaFacade().getAreaReferenceByUuid(
							((CampaignTreeGridDto) event.getAllSelectedItems().toArray()[i]).getUuid());
					areass.add(selectedArea);
				}
				if (((CampaignTreeGridDto) event.getAllSelectedItems().toArray()[i]).getLevelAssessed() == "region") {
					RegionReferenceDto selectedRegion = FacadeProvider.getRegionFacade().getRegionReferenceByUuid(
							((CampaignTreeGridDto) event.getAllSelectedItems().toArray()[i]).getUuid());
					region.add(selectedRegion);
				}
				if (((CampaignTreeGridDto) event.getAllSelectedItems().toArray()[i]).getLevelAssessed() == "district") {
					DistrictReferenceDto selectedDistrict = FacadeProvider.getDistrictFacade()
							.getDistrictReferenceByUuid(
									((CampaignTreeGridDto) event.getAllSelectedItems().toArray()[i]).getUuid());
					districts.add(selectedDistrict);
				}

				if (((CampaignTreeGridDto) event.getAllSelectedItems().toArray()[i]).getLevelAssessed() == "district") {

					PopulationDataDto popopulationDataDto = new PopulationDataDto();

					popopulationDataDto
							.setCampaign(FacadeProvider.getCampaignFacade().getReferenceByUuid(campaignDto.getUuid()));
					popopulationDataDto.setDistrict(FacadeProvider.getDistrictFacade().getDistrictReferenceByUuid(
							((CampaignTreeGridDto) event.getAllSelectedItems().toArray()[i]).getUuid()));
					popopulationDataDtoSet.add(popopulationDataDto);
				}

			}

			if (campaignDto != null) {
				campaignDto.setAreas((Set<AreaReferenceDto>) areass);
				campaignDto.setRegion((Set<RegionReferenceDto>) region);
				campaignDto.setDistricts((Set<DistrictReferenceDto>) districts);
				// System.out.println("==================== "+popopulationDataDtoSet.size());
				campaignDto.setPopulationdata((Set<PopulationDataDto>) popopulationDataDtoSet);
				campaignDto.setCommunity((Set<CommunityReferenceDto>) community);
			}
		});

		parentTab4.add(treeGrid);

		parentTab4.add(layoutAssocCamp);
		tabsheetParent.add(I18nProperties.getCaption(Captions.associateCampaign), parentTab4);

		VerticalLayout parentTab5 = new VerticalLayout();
		parentTab5.setId("parentTab5");

		VerticalLayout poplayout = new VerticalLayout();
		poplayout.setSpacing(false);
		poplayout.setId("poplayout");

		Label lblIntroduction = new Label(I18nProperties.getString(Strings.infoPopulationDataView));

		poplayout.add(lblIntroduction);

		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, lblIntroduction);// .setHorizontalComponentAlignment(lblIntroduction,

		Button btnImport = new Button(I18nProperties.getCaption(Captions.actionImport));// , e -> {

		btnImport.addClickListener(e -> {
			if (campaignDto != null) {
				ImportPopulationDataDialog dialog = new ImportPopulationDataDialog(InfrastructureType.POPULATION_DATA,
						campaignDto);
				dialog.open();
			} else {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

				Div textx = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));

				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				HorizontalLayout layoutx = new HorizontalLayout(textx, closeButton);
				layoutx.setAlignItems(Alignment.CENTER);
				notification.setPosition(Notification.Position.MIDDLE);
				notification.add(layoutx);
				notification.open();
			}
		});

		poplayout.add(btnImport);
		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, btnImport);

		if (campaignDto != null) {
			Button btnExport = new Button(I18nProperties.getCaption(Captions.export)); // poplayout.addComponent(btnExport);
			poplayout.add(btnExport);
			poplayout.setHorizontalComponentAlignment(Alignment.CENTER, btnExport);

			StreamResource populationDataStreamResource = DownloadFlowUtilityView
					.createPopulationDataExportResource(campaignDto.getUuid());

			populationDataStreamResource.setContentType("text/csv");
			populationDataStreamResource.setCacheTime(0);

			// Create an anchor to trigger the download
			Anchor downloadAnchor = new Anchor(populationDataStreamResource, "Download CSV");
			downloadAnchor.getElement().setAttribute("download", true);
			downloadAnchor.getStyle().set("display", "none");

			poplayout.add(downloadAnchor);

			// Simulate a click event on the hidden anchor to trigger the download

			btnExport.addClickListener(e -> {
				downloadAnchor.getElement().callJsFunction("click");
				Notification.show("downloading...");
			});
		}
//		StreamResource populationDataExportResource = DownloadUtil.createPopulationDataExportResource();
//		new FileDownloader(populationDataExportResource).extend(btnExportDummy);
		poplayout.addClickListener(e -> {
			if (campaignDto == null) {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

				Div textx = new Div(new Text(I18nProperties.getString(Strings.infoSaveCampaignFirst)));

				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				HorizontalLayout layoutx = new HorizontalLayout(textx, closeButton);
				layoutx.setAlignItems(Alignment.CENTER);

				notification.add(layoutx);
				notification.setPosition(Notification.Position.MIDDLE);
				notification.open();
			}
		});
		parentTab5.add(poplayout);
//		

		tabsheetParent.add(I18nProperties.getCaption(Captions.View_configuration_populationdata), parentTab5);
		tabsheetParent.setWidthFull();
		tabsheetParent.setId("tabsheetParent");

		openCloseCampaign = new Button();

		openCloseCampaign.setText(I18nProperties.getString(Strings.openCampaign));
		duplicateCampaign = new Button(I18nProperties.getString(Strings.duplicate));
		duplicateCampaign.addClickListener(e -> {
			duplicateCampaign();
//			updatePublishButtonText(isPublished);
		});

		deleteCampaign = new Button();
		deleteCampaign.setText(I18nProperties.getCaption(Captions.actionDelete));
		deleteCampaign.getStyle().set("background", "red");
		deleteCampaign.addClickListener(e -> {
			deleteCampaign();
		});

		logButton = new Button();
		logButton.setText("Log");
		logButton.addClickListener(e -> {
			Notification.show("clicked");
			logEventMethod();
		});

		publishUnpublishCampaign = new Button();

		if (campaignDto != null) {
			isArchived = FacadeProvider.getCampaignFacade().isArchived(campaignDto.getUuid());
			isPublished = FacadeProvider.getCampaignFacade().isPublished(campaignDto.getUuid());
			isOpenClose = FacadeProvider.getCampaignFacade().isClosedd(campaignDto.getUuid());
			updateArchiveButtonText(isArchived);
			updatePublishButtonText(isPublished);
			updateOpenCloseButtonText(isOpenClose);
		}

		publishUnpublishCampaign.addClickListener(e -> {
			publishUnpublish();
		});

		openCloseCampaign.addClickListener(e -> {
			openCloseCampaign();
		});

		archiveDearchive.addClickListener(e -> {
			archive();

		});

		discardChanges = new Button();
		discardChanges.setText(I18nProperties.getCaption(Captions.actionDiscard));

		discardChanges.addThemeVariants(ButtonVariant.LUMO_ERROR);
		discardChanges.addClickListener(e -> {

			discardChanges();
		});

		saveChanges = new Button();

		saveChanges.setText(I18nProperties.getCaption(Captions.actionSave));

		saveChanges.addClickListener(e -> {
			validateAndSave(editMode);
		});

		HorizontalLayout leftFloat = new HorizontalLayout();
		HorizontalLayout rightFloat = new HorizontalLayout();
		leftFloat.setJustifyContentMode(JustifyContentMode.START);

		rightFloat.setJustifyContentMode(JustifyContentMode.END);
		if (campaignDto != null) {
			leftFloat.add(archiveDearchive, publishUnpublishCampaign, openCloseCampaign, duplicateCampaign,
					deleteCampaign, logButton);
		} else {
			publishUnpublishCampaign.setText(I18nProperties.getString(Strings.headingPublishCampaign));
			openCloseCampaign.setText("Open Campaign");
			leftFloat.add(publishUnpublishCampaign, openCloseCampaign, logButton);
		}
		leftFloat.setWidth("50%");

		rightFloat.add(discardChanges, saveChanges);
		rightFloat.setWidth("50%");

		actionButtonsLayout.add(leftFloat, rightFloat);
		actionButtonsLayout.setWidthFull();

		FormLayout formL = new FormLayout();
		HorizontalLayout header = new HorizontalLayout();
		header.add(creatingUser, creatingUuid, campaaignYear);
		formL.add(header, campaignName, round, startDate, endDate, description);

		formL.setColspan(header, 2);
		formL.setColspan(description, 2);
		formL.setColspan(hort, 2);
		formL.setColspan(leftFloat, 1);
		formL.setColspan(rightFloat, 1);
//		formL.setColspan(actionButtonsLayout, 2);
		round.addValueChangeListener(e -> {
			roundChange();
		});

		campaignBasics.getStyle().set("margin-top", "0px");
		campaignBasics.getStyle().set("margin-bottom", "0px");
		add(campaignBasics, formL, layoutParent, actionButtonsLayout); // ,

	}

	public String DateGetYear(Date dates) {

		SimpleDateFormat getYearFormat = new SimpleDateFormat("yyyy");
		String currentYear = getYearFormat.format(dates);
		return currentYear;
	}

	public void updateArchiveButtonText(boolean isArchived) {
		this.isArchived = isArchived;
		if (isArchived) {
			archiveDearchive.setText("De-Archive");

		} else {

			archiveDearchive.setText("Archive");
		}

	}

	public void updatePublishButtonText(boolean isPublished) {
		this.isPublished = isPublished;
		if (isPublished) {
			publishUnpublishCampaign.setText("Publish");
		} else {

			publishUnpublishCampaign.setText("Un-Publish");
		}

	}

	public void updateOpenCloseButtonText(boolean isOpenClose) {
		this.isOpenClose = isOpenClose;
		if (isOpenClose) {
			openCloseCampaign.setText("Open Campaign");
		} else {

			openCloseCampaign.setText("Close Campaign");
		}

	}

	private List<CampaignTreeGridDto> generateTreeGridData(CampaignDto formData) {
		List<CampaignTreeGridDto> gridData = new ArrayList<>();
		this.campaignDto = formData;

		List<AreaDto> areas = FacadeProvider.getAreaFacade().getAllActiveAsReferenceAndPopulation(formData);
		for (AreaDto area_ : areas) {
			CampaignTreeGridDto areaData = new CampaignTreeGridDto(area_.getName(), area_.getAreaid(), "Area",
					area_.getUuid_(), "area");
			List<RegionDto> regions_ = FacadeProvider.getRegionFacade()
					.getAllActiveAsReferenceAndPopulation(area_.getAreaid(), campaignDto.getUuid());
			for (RegionDto regions_x : regions_) {
				CampaignTreeGridDto regionData = new CampaignTreeGridDto(regions_x.getName(), regions_x.getRegionId(),
						regions_x.getAreaUuid_(), regions_x.getUuid_(), "region");
				List<DistrictDto> district_ = FacadeProvider.getDistrictFacade()
						.getAllActiveAsReferenceAndPopulation(regions_x.getRegionId(), campaignDto);
				ArrayList arr = new ArrayList<>();
				for (DistrictDto district_x : district_) {
					arr.add(new CampaignTreeGridDtoImpl(district_x.getName(), district_x.getPopulationData(),
							district_x.getRegionId(), district_x.getRegionUuid_(), district_x.getUuid_(), "district",
							district_x.getSelectedPopulationData()));
				}
				;

				regionData.setRegionData(arr);

				areaData.addRegionData(regionData);
			}

			gridData.add(areaData);
		}
		return gridData;
	}

	private void discardChanges() {
		UI currentUI = UI.getCurrent();
		if (currentUI != null) {
			Dialog dialog = (Dialog) this.getParent().get();
			dialog.close();
		}
	}

	// Events
	public static abstract class CampaignFormEvent extends ComponentEvent<CampaignForm> {
		private CampaignDto campaign;

		protected CampaignFormEvent(CampaignForm source, CampaignDto campaign) {
			super(source, false);
			this.campaign = campaign;
		}

		public CampaignDto getCampaign() {
			if (campaign == null) {
				campaign = new CampaignDto();
//				campaign.setUuid(uuid.toString().toUpperCase());
				return campaign;
			} else {
				return campaign;

			}
		}
	}

	public void validateAndSave(boolean editMode) {
		this.editMode = editMode;
		if (formDatac == null) {

			UserReferenceDto user = new UserReferenceDto();
			UserProvider usr = new UserProvider();
			user.setUuid(usr.getUuid());
			formDatac = new CampaignDto();
			formDatac.setUuid(creatingUuid.getValue());
			formDatac.setCreatingUser(user);
			LocalDate localDate = startDate.getValue();
			Date startdate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			LocalDate endxDate = endDate.getValue();
			Date endxDatex = Date.from(endxDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
			formDatac.setCampaignYear(campaaignYear.getValue().toString());

			formDatac.setName(campaignName.getValue());
			formDatac.setRound(round.getValue().toString());
			formDatac.setStartDate(startdate);
			formDatac.setEndDate(endxDatex);
			formDatac.setDescription(description.getValue());
			formDatac.setCampaignStatus(formDatac.campaignStatus = "Closed");
			List<CampaignDashboardElement> preCampaigngridData = comp1.getGridData();
			List<CampaignDashboardElement> intraCampaigngridData = compp2.getGridData();
			List<CampaignDashboardElement> postCampaigngridData = comppp2.getGridData();
			
			List<CampaignDashboardElement> superList = new ArrayList<>();

			// Add items from preCampaigngridData if they are not null
			for (CampaignDashboardElement item : preCampaigngridData) {
			    if (item != null) {
			        superList.add(item);
			    }
			}

			// Add items from intraCampaigngridData if they are not null
			for (CampaignDashboardElement item : intraCampaigngridData) {
			    if (item != null) {
			        superList.add(item);
			    }
			}

			// Add items from postCampaigngridData if they are not null
			for (CampaignDashboardElement item : postCampaigngridData) {
			    if (item != null) {
			        superList.add(item);
			    }
			}
			
			
			List<CampaignFormMetaReferenceDto> preCampaignDashboardgridData = comp.getSavedElements();
			List<CampaignFormMetaReferenceDto> intraCampaignDashboardgridData = compp.getSavedElements();
			List<CampaignFormMetaReferenceDto> postCampaignDashboardgridData = comppp.getSavedElements();

			Set<CampaignFormMetaReferenceDto> superSet = new HashSet<>();
			
			for (CampaignFormMetaReferenceDto item : preCampaignDashboardgridData) {
			    if (item != null) {
			        superSet.add(item);
			    }
			}

			// Add items from intraCampaignDashboardgridData if they are not null
			for (CampaignFormMetaReferenceDto item : intraCampaignDashboardgridData) {
			    if (item != null) {
			        superSet.add(item);
			    }
			}

			// Add items from postCampaignDashboardgridData if they are not null
			for (CampaignFormMetaReferenceDto item : postCampaignDashboardgridData) {
			    if (item != null) {
			        superSet.add(item);
			    }
			}
			
			formDatac.setCampaignDashboardElements(superList);
			formDatac.setCampaignFormMetas(superSet);

			fireEvent(new SaveEvent(this, formDatac));
			UI.getCurrent().getPage().reload();

			System.out.println("eskelebetiolebetetttttttthvkfvskv");

		} else {
			if (binder.validate().isOk()) {

				fireEvent(new SaveEvent(this, binderx.getBean()));

				UI.getCurrent().getPage().reload();

				Notification.show(I18nProperties.getString(Strings.headingUploadSuccess) + "!");

			} else {
				Notification.show(I18nProperties.getString(Strings.errorCampaignForm));
			}
		}
	}

	private void archive() {
//		updateArchiveButtonText(isArchived);
//		UI.getCurrent().getPage().reload();
		try {
			fireEvent(new ArchiveEvent(this, binderx.getBean()));
		} finally {
//			isArchived = FacadeProvider.getCampaignFacade().isArchived(campaignDto.getUuid());
//			updateArchiveButtonText(isArchived);
		}

	}

	private void publishUnpublish() {
		fireEvent(new PublishUnpublishEvent(this, binderx.getBean()));
	}

	private void logEventMethod() {
		fireEvent(new LogCampaignEvent(this, binderx.getBean()));
	}

	private void deleteCampaign() {

		fireEvent(new DeleteEvent(this, binderx.getBean()));
//		updatePublishButtonText(isArchived);
//		UI.getCurrent().getPage().reload();

	}

	private void duplicateCampaign() {

		fireEvent(new DuplicateEvent(this, binderx.getBean()));
//		updatePublishButtonText(isArchived);
//		UI.getCurrent().getPage().reload();

	}

	private void openCloseCampaign() {

		fireEvent(new OpenCloseEvent(this, binderx.getBean()));

	}

	private void roundChange() {

		fireEvent(new RoundChangeEvent(this, binderx.getBean()));

	}

	public void setCampaign(CampaignDto campaignIndexDto) {
		// TODO Auto-generated method stub
		binderx.setBean(campaignIndexDto);
	}

	public static class SaveEvent extends CampaignFormEvent {
		SaveEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);
		}
	}

	public static class RoundChangeEvent extends CampaignFormEvent {
		RoundChangeEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);
		}
	}

	public class ArchiveEvent extends CampaignFormEvent {
		ArchiveEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);

		}
	}

	public static class PublishUnpublishEvent extends CampaignFormEvent {
		PublishUnpublishEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);
		}
	}

	public static class LogCampaignEvent extends CampaignFormEvent {
		LogCampaignEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);
		}
	}

	public static class OpenCloseEvent extends CampaignFormEvent {
		OpenCloseEvent(CampaignForm source, CampaignDto campaign) {
			super(source, campaign);
		}
	}

	public static class DeleteEvent extends CampaignFormEvent {
		DeleteEvent(CampaignForm source, CampaignDto contact) {
			super(source, contact);
		}

	}

	public static class DuplicateEvent extends CampaignFormEvent {
		DuplicateEvent(CampaignForm source, CampaignDto contact) {
			super(source, contact);
		}

	}

	public static class CloseEvent extends CampaignFormEvent {
		CloseEvent(CampaignForm source) {
			super(source, null);
		}
	}

	public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
		return addListener(DeleteEvent.class, listener);
	}

	public Registration addDuplicateListener(ComponentEventListener<DuplicateEvent> listener) {
		return addListener(DuplicateEvent.class, listener);
	}

	public Registration addArchiveListener(ComponentEventListener<ArchiveEvent> listener) {
		return addListener(ArchiveEvent.class, listener);
	}

	public Registration addPublishListener(ComponentEventListener<PublishUnpublishEvent> listener) {
		return addListener(PublishUnpublishEvent.class, listener);
	}

	public Registration addLogListener(ComponentEventListener<LogCampaignEvent> listener) {
		return addListener(LogCampaignEvent.class, listener);
	}

	public Registration addOpenCloseListener(ComponentEventListener<OpenCloseEvent> listener) {
		return addListener(OpenCloseEvent.class, listener);
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}

	public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
		return addListener(CloseEvent.class, listener);
	}

	public Registration addRoundChangeListener(ComponentEventListener<RoundChangeEvent> listener) {
		return addListener(RoundChangeEvent.class, listener);
	}

	private List<CampaignDashboardElement> getListDashboardFromType(String phaseTy) {
		final List<CampaignDiagramDefinitionDto> allDiagram = FacadeProvider.getCampaignDiagramDefinitionFacade()
				.getAll();

		List<CampaignDashboardElement> elements = new ArrayList<>();
		final List<CampaignDiagramDefinitionDto> filterList = allDiagram.stream()
				.filter(e -> e.getFormType().equalsIgnoreCase(phaseTy)).collect(Collectors.toList());

		for (CampaignDiagramDefinitionDto lsiter : filterList) {
			CampaignDashboardElement emptyElements = new CampaignDashboardElement();
			emptyElements.setDiagramId(lsiter.getDiagramId());
			emptyElements.setPhase(phaseTy);
			elements.add(emptyElements);

		}
		return elements;

	}

}
