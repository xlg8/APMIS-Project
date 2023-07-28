package com.cinoteck.application.views.campaign;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

import com.vaadin.flow.component.treegrid.TreeGrid;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignTreeGridDto;
import de.symeda.sormas.api.campaign.CampaignTreeGridDtoImpl;
import de.symeda.sormas.api.campaign.diagram.CampaignDashboardElement;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDefinitionDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

@PageTitle("Edit Campaign")
@Route(value = "/data")
public class CampaignForm extends VerticalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 764300181578209719L;
	private static final String PRE_CAMPAIGN = "pre-campaign";
	private static final String INTRA_CAMPAIGN = "intra-campaign";
	private static final String POST_CAMPAIGN = "post-campaign";

	Button archiveDearchive = new Button("Archive");
	Button openCloseCampaign;
	Button duplicateCampaign;
	Button deleteCampaign;
	Button publishUnpublishCampaign;

	Button discardChanges;
	Button saveChanges;

	Binder<CampaignIndexDto> binder = new BeanValidationBinder<>(CampaignIndexDto.class);
	Binder<CampaignDto> binderx = new BeanValidationBinder<>(CampaignDto.class);

	List<CampaignIndexDto> campaignNames, startDates, endDates, descriptions;
//	CampaignRounds rounds;
	private CampaignDto campaignDto;
	private CampaignIndexDto campaignDtox;
	
	H5 campaignBasics = new H5("Campaign Basics");

	TextField campaignName = new TextField("Campaign name");
	ComboBox round = new ComboBox<>("Round");
	DatePicker startDate = new DatePicker("Start date");
	DatePicker endDate = new DatePicker("End date");
	TextField creatingUser = new TextField("Creating User");
	TextField creatingUuid = new TextField("UUID");
	TextField campaaignYear = new TextField("Campaign Year");
	UUID uuid = UUID.randomUUID();
	TextArea description = new TextArea("Description");

	TreeGrid<CampaignTreeGridDto> treeGrid = new TreeGrid<>();

	HorizontalLayout  actionButtonsLayout = new HorizontalLayout();
	
	private Set<AreaReferenceDto> areass = new HashSet<>();;
	private Set<RegionReferenceDto> region = new HashSet<>();
	private Set<DistrictReferenceDto> districts = new HashSet<>();
	private Set<CommunityReferenceDto> community = new HashSet<>();
	private Set<PopulationDataDto> popopulationDataDtoSet = new HashSet<>();

	private final VerticalLayout statusChangeLayout;
	private Boolean isCreateForm = null;

	CampaignFormMetaReferenceDto xx;
	CampaignDto formDatac;

	public CampaignForm(CampaignDto formData) {

		this.statusChangeLayout = new VerticalLayout();
		this.formDatac = formData;
		
		isCreateForm = formData == null;

		statusChangeLayout.setSpacing(false);
		statusChangeLayout.setMargin(false);
		add(statusChangeLayout);

		addClassName("campaign-form");

//		HorizontalLayout hor = new HorizontalLayout();

//		Icon vaadinIcon = new Icon(VaadinIcon.ARROW_CIRCLE_LEFT);
//		vaadinIcon.setId("formCloseIcon");
//		hor.setJustifyContentMode(JustifyContentMode.END);
//		H6 allCampaignLabel = new H6("All Campaigns");
//		allCampaignLabel.setId("formCloseIcon");
//		hor.setAlignItems(Alignment.CENTER);
//		hor.setWidthFull();
//		hor.add(vaadinIcon, allCampaignLabel);
//		hor.setHeight("5px");
//		this.setColspan(hor, 2);
//		vaadinIcon.addClickListener(event -> fireEvent(new CloseEvent(this)));
//		add(hor);
		// Configure what is passed to the fields here
		
		
		configureFields(formData);

	}

	private void configureFields(CampaignDto formData) {

		this.campaignDto = formData;

		
		
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
		round.setItems("NID", "SNID", "Case Respond", "Mopping-Up");
		

//		round.setItemLabelGenerator(CampaignDto::getRound);
		round.addValueChangeListener(e -> {
			round.getValue();
		});
		if (creatingUuid.getValue() == "" || creatingUuid.getValue() == "") {
			creatingUuid.setValue(uuid.toString().toUpperCase());
			creatingUser.setValue(curentUse);

		}

		
//		System.out.println(creatingUuid.getValue() + "craeting uuid ");
		binderx.forField(creatingUuid).bind(CampaignDto.UUID);

		binderx.forField(creatingUser).bind(CampaignDto.CREATING_USER_NAME);
		binderx.forField(campaaignYear).bind(CampaignDto.CAMPAIGN_YEAR);
		binderx.forField(campaignName).asRequired("Campaign Name is Required").bind(CampaignDto.NAME);
		binderx.forField(round).asRequired("Campaign Round is Required").bind(CampaignDto.ROUND);
//		LocalDate localDate = formData.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//		startDate.setValue(localDate);
//		binderx.forField(startDate).bind(CampaignDto.START_DATE).toString();

//		LocalDate localDatex = formData.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//		endDate.setValue(localDatex);

//		binderx.forField(endDate).bind(CampaignDto.END_DATE);
		binderx.forField(description).asRequired("Campaign Description is Required").bind(CampaignDto::getDescription,
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

		CampaignFormGridComponent comp = new CampaignFormGridComponent(
				this.campaignDto == null ? Collections.emptyList()
						: new ArrayList<>(campaignDto.getCampaignFormMetas("pre-campaign")),
						FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRound(PRE_CAMPAIGN), campaignDto, PRE_CAMPAIGN);
		tab1.add(comp);
		
		//this might blow our in new campaign saying null
		this.campaignDto = comp.getModifiedDto();
		
		tabsheet.add("Pre Campaign Forms", tab1);

		VerticalLayout tab2 = new VerticalLayout();
		
		
		
		
		
		

//		final List<CampaignDashboardElement> campaignDashboardElements = FacadeProvider.getCampaignFacade()
//				.getCampaignDashboardElements(null, PRE_CAMPAIGN);
		CampaignDashboardGridElementComponent comp1 = new CampaignDashboardGridElementComponent(
				this.campaignDto == null ? Collections.EMPTY_LIST
						: new ArrayList<>(campaignDto.getCampaignDashboardElements(PRE_CAMPAIGN)),
						getListDashboardFromType(PRE_CAMPAIGN), campaignDto, PRE_CAMPAIGN);
		tab2.add(comp1);
		tabsheet.add("Pre Campaign Dashboard", tab2);
		tabsheet.setWidthFull();
		parentTab1.add(layout);
		tabsheetParent.add("Pre-Campaign Phase", parentTab1);

		VerticalLayout parentTab2 = new VerticalLayout();

		final HorizontalLayout layoutIntra = new HorizontalLayout();
		layoutIntra.setWidthFull();

		TabSheet tabsheetIntra = new TabSheet();
		layoutIntra.add(tabsheetIntra);

		VerticalLayout tab1Intra = new VerticalLayout();

		H1 text = new H1("Content Goes Here");
		CampaignFormGridComponent compp = new CampaignFormGridComponent(
				this.campaignDto == null ? Collections.EMPTY_LIST
						: new ArrayList<>(campaignDto.getCampaignFormMetas("intra-campaign")),
						FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRound(INTRA_CAMPAIGN), campaignDto, INTRA_CAMPAIGN);
		tab1Intra.add(compp);
		this.campaignDto = compp.getModifiedDto();
		tabsheetIntra.add("Intra Campaign Forms", tab1Intra);
		tabsheetIntra.setWidthFull();

		System.out.println(this.campaignDto + "campdto null check");

		VerticalLayout tab2Intra = new VerticalLayout();

		final List<CampaignDashboardElement> intracampaignDashboardElements = FacadeProvider.getCampaignFacade()
				.getCampaignDashboardElements(null, INTRA_CAMPAIGN);
		CampaignDashboardGridElementComponent compp2 = new CampaignDashboardGridElementComponent(
				this.campaignDto == null ? Collections.EMPTY_LIST
						: new ArrayList<>(campaignDto.getCampaignDashboardElements(INTRA_CAMPAIGN)),
						getListDashboardFromType(INTRA_CAMPAIGN), campaignDto, INTRA_CAMPAIGN);
		tab2Intra.add(compp2);

		tabsheetIntra.add("Intra Campaign Dashboard", tab2Intra);
		parentTab2.add(layoutIntra);
	//	parentTab2.getStyle().set("color", "green");
		tabsheetParent.add("Intra-Campaign Phase", parentTab2);

		VerticalLayout parentTab3 = new VerticalLayout();
		final HorizontalLayout layoutPost = new HorizontalLayout();
		layoutPost.setWidthFull();

		TabSheet tabsheetPost = new TabSheet();
		layoutPost.add(tabsheetPost);

		VerticalLayout tab1Post = new VerticalLayout();

		CampaignFormGridComponent comppp = new CampaignFormGridComponent(
				this.campaignDto == null ? Collections.EMPTY_LIST
						: new ArrayList<>(campaignDto.getCampaignFormMetas(POST_CAMPAIGN)),
						FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRound(POST_CAMPAIGN), campaignDto, POST_CAMPAIGN);
		tab1Post.add(comppp);
		this.campaignDto = comppp.getModifiedDto();
		tabsheetPost.add("Post Campaign Forms", tab1Post);

		VerticalLayout tab2Post = new VerticalLayout();

		final List<CampaignDashboardElement> postcampaignDashboardElements = FacadeProvider.getCampaignFacade()
				.getCampaignDashboardElements(null, POST_CAMPAIGN);
		CampaignDashboardGridElementComponent comppp2 = new CampaignDashboardGridElementComponent(
				this.campaignDto == null ? Collections.EMPTY_LIST
						: new ArrayList<>(campaignDto.getCampaignDashboardElements(POST_CAMPAIGN)),
						getListDashboardFromType(POST_CAMPAIGN), campaignDto, POST_CAMPAIGN);
		tab2Post.add(comppp2);

		tabsheetPost.add("Post Campaign Dashboard", tab2Post);
		tabsheetPost.setWidthFull();
		parentTab3.add(layoutPost);
		tabsheetParent.add("Post-Campaign Phase", parentTab3);

		System.out.println(campaignDto + "campaign DTOoooooooooooooooooooooooooooooooooo");

		VerticalLayout parentTab4 = new VerticalLayout();
		final HorizontalLayout layoutAssocCamp = new HorizontalLayout();
		layoutAssocCamp.setWidthFull();

		treeGrid.setWidthFull();
		if (campaignDto != null) {

			treeGrid.setItems(generateTreeGridData(formData), CampaignTreeGridDto::getRegionData);

			treeGrid.setWidthFull();

			treeGrid.addHierarchyColumn(CampaignTreeGridDto::getName).setHeader("Location");

			treeGrid.addColumn(CampaignTreeGridDto::getPopulationData).setHeader("Population");

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
		tabsheetParent.add("Associate Campaign ", parentTab4);

		VerticalLayout parentTab5 = new VerticalLayout();
		parentTab5.setId("parentTab5");

		VerticalLayout poplayout = new VerticalLayout();
		poplayout.setSpacing(false);
		poplayout.setId("poplayout");

		Label lblIntroduction = new Label(I18nProperties.getString(Strings.infoPopulationDataView));

		poplayout.add(lblIntroduction);

		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, lblIntroduction);// .setHorizontalComponentAlignment(lblIntroduction,

		Button btnImport = new Button("Import");// , e -> {

		btnImport.addClickListener(e -> {
			ImportPopulationDataDialog dialog = new ImportPopulationDataDialog(null, campaignDto);
			dialog.open();
		});

		poplayout.add(btnImport);
		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, btnImport);

		Button btnExport = new Button("Export"); // poplayout.addComponent(btnExport);
		poplayout.add(btnExport);
		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, btnExport);

		parentTab5.add(poplayout);

		tabsheetParent.add("Population Data", parentTab5);
		tabsheetParent.setWidthFull();
		tabsheetParent.setId("tabsheetParent");

		
		
		

		openCloseCampaign = new Button();
		openCloseCampaign.setText("Open Campaign");
//		openCloseCampaign.getStyle().set("margin-inline-end", "auto");
		
		duplicateCampaign = new Button();
		duplicateCampaign.setText("Duplicate");
//		duplicateCampaign.getStyle().set("margin-inline-end", "auto");
		
		deleteCampaign = new Button();
		deleteCampaign.setText("Delete");
		deleteCampaign.getStyle().set("background", "red");
//		deleteCampaign.getStyle().set("margin-inline-end", "auto");
		
		publishUnpublishCampaign = new Button();
		publishUnpublishCampaign.setText("Publish Campaign");
//		publishUnpublishCampaign.getStyle().set("margin-inline-end", "auto");
		
		discardChanges = new Button();
		discardChanges.setText("Discard");
//		discardChanges.getStyle().set("margin-inline-start", "auto");
		discardChanges.addThemeVariants(ButtonVariant.LUMO_ERROR);
		
		saveChanges = new Button();
		saveChanges.setText("Save");
//		saveChanges.getStyle().set("margin-inline-start", "20px");
		saveChanges.addClickListener(e -> {
			validateAndSave();
		});

		
		
//		archiveDearchive.getStyle().set("margin-inline-end", "auto");
		// Add the buttons to the layout
		
		HorizontalLayout leftFloat = new HorizontalLayout();
		HorizontalLayout rightFloat = new HorizontalLayout();
		leftFloat.setJustifyContentMode(JustifyContentMode.START);
		
		rightFloat.setJustifyContentMode(JustifyContentMode.END);
//		rightFloat.setWidthFull();
		leftFloat.add(archiveDearchive, publishUnpublishCampaign, openCloseCampaign, duplicateCampaign,
				deleteCampaign);
		leftFloat.setWidth("50%");
		
		rightFloat.add(discardChanges, saveChanges);
		rightFloat.setWidth("50%");
		
		actionButtonsLayout.add(leftFloat, rightFloat);
//		actionButtonsLayout.getStyle().set("flex-wrap", "wrap");
		actionButtonsLayout.setWidthFull();
		//actionButtonsLayout.setJustifyContentMode(JustifyContentMode.END);
		
		// Set the justify content mode to END
		FormLayout formL = new FormLayout();
		formL.add(campaignName, round, startDate, endDate, description);
		formL.setColspan(description, 2);
		formL.setColspan(hort, 2);
		formL.setColspan(leftFloat, 1);
		formL.setColspan(rightFloat, 1);
//		formL.setColspan(actionButtonsLayout, 2);
		
		campaignBasics.getStyle().set("margin-top", "0px");
		campaignBasics.getStyle().set("margin-bottom", "0px");
		//actionButtonsLayout.setJustifyContentMode(JustifyContentMode.END);
//		actionButtonsLayout.setResponsiveSteps(
//				// Use one column by default
//				new ResponsiveStep("0", 1), new ResponsiveStep("520px", 7), new ResponsiveStep("1000px", 7));
		add(campaignBasics, formL, layoutParent,
				actionButtonsLayout); // ,
		// layoutParent,
		// actionButtons
		System.out.println(tabsheetParent.getSelectedTab().getLabel() + "selected tab ");

		System.out.println(tabsheet.getSelectedTab().getLabel() + "selected tab ");

	}

	
	public String DateGetYear(Date dates) {

		SimpleDateFormat getYearFormat = new SimpleDateFormat("yyyy");
		String currentYear = getYearFormat.format(dates);
		return currentYear;
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

	// Events
	public static abstract class CampaignFormEvent extends ComponentEvent<CampaignForm> {
		private CampaignDto campaign;

		protected CampaignFormEvent(CampaignForm source, CampaignDto campaign) {
			super(source, false);
			this.campaign = campaign;
		}

		public CampaignDto getCampaign() {
			return campaign;
		}
	}

	private void validateAndSave() {
		if (binder.validate().isOk()) {
			
			
			
			
			fireEvent(new SaveEvent(this, binderx.getBean()));
			
			UI.getCurrent().getPage().reload();
			Notification.show("Success!");
			
		} else {
			Notification.show("error in vampaing form");
		}
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

	public static class DeleteEvent extends CampaignFormEvent {
		DeleteEvent(CampaignForm source, CampaignDto contact) {
			super(source, contact);
		}

	}

	public static class CloseEvent extends CampaignFormEvent {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2954464243916580526L;

		CloseEvent(CampaignForm source) {
			super(source, null);
		}
	}

	public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
		return addListener(DeleteEvent.class, listener);
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}

	public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
		return addListener(CloseEvent.class, listener);
	}

	
	private List<CampaignDashboardElement> getListDashboardFromType(String phaseTy) {
		final List<CampaignDiagramDefinitionDto> allDiagram = FacadeProvider.getCampaignDiagramDefinitionFacade().getAll();
		
		List<CampaignDashboardElement> elements = new ArrayList<>();
		final List<CampaignDiagramDefinitionDto> filterList = allDiagram.stream().filter(e -> e.getFormType().equalsIgnoreCase(phaseTy)).collect(Collectors.toList());
		
		for(CampaignDiagramDefinitionDto lsiter : filterList) {
			CampaignDashboardElement emptyElements = new CampaignDashboardElement();
			emptyElements.setDiagramId(lsiter.getDiagramId());
			emptyElements.setPhase(phaseTy);
			elements.add(emptyElements);
		
		}
		return elements;
		
	}
}
