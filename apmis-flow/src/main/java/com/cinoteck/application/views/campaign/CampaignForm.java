package com.cinoteck.application.views.campaign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.AbstractGridMultiSelectionModel;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
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
import de.symeda.sormas.api.campaign.CampaignTreeGridDto;
import de.symeda.sormas.api.campaign.CampaignTreeGridDtoImpl;
import de.symeda.sormas.api.campaign.diagram.CampaignDashboardElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaFacade;
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
public class CampaignForm extends FormLayout {
	private static final String PRE_CAMPAIGN = "pre-campaign";
	private static final String INTRA_CAMPAIGN = "intra-campaign";
	private static final String POST_CAMPAIGN = "post-campaign";

	Binder<CampaignDto> binder = new BeanValidationBinder<>(CampaignDto.class);

	List<CampaignDto> campaignNames, startDates, endDates, descriptions;
	CampaignRounds rounds;

	TextField campaignName = new TextField("Campaign name");
	ComboBox round = new ComboBox<>("Round");
	DatePicker startDate = new DatePicker("Start date");
	DatePicker endDate = new DatePicker("End date");
	TextField creatingUser = new TextField("Creating User");
	TextField creatingUuid = new TextField("UUID");
	TextField campaaignYear = new TextField("Campaign Year");

	TextArea description = new TextArea("Description");


	TreeGrid<CampaignTreeGridDto> treeGrid = new TreeGrid<>();

	private Set<AreaReferenceDto> areass = new HashSet<>();;
	private Set<RegionReferenceDto> region = new HashSet<>();
	private Set<DistrictReferenceDto> districts = new HashSet<>();
	private Set<CommunityReferenceDto> community = new HashSet<>();
	private Set<PopulationDataDto> popopulationDataDtoSet = new HashSet<>();

	private final VerticalLayout statusChangeLayout;
	private Boolean isCreateForm = null;
	private CampaignDto campaignDto;
	CampaignFormMetaReferenceDto xx;

	public CampaignForm(CampaignDto campaignDto) {
//super();
		this.campaignDto = campaignDto;
		this.statusChangeLayout = new VerticalLayout();
		
		isCreateForm = campaignDto == null;
//		if (isCreateForm) {
//			hideValidationUntilNextCommit();
//		}
		statusChangeLayout.setSpacing(false);
        statusChangeLayout.setMargin(false);
        add(statusChangeLayout);

		addClassName("campaign-form");

		HorizontalLayout hor = new HorizontalLayout();

		Icon vaadinIcon = new Icon(VaadinIcon.ARROW_CIRCLE_LEFT);
		vaadinIcon.setId("formCloseIcon");
//		hor.setJustifyContentMode(JustifyContentMode.END);
		H6 allCampaignLabel = new H6("All Campaigns");
		allCampaignLabel.setId("formCloseIcon");
		hor.setAlignItems(Alignment.CENTER);
		hor.setWidthFull();
		hor.add(vaadinIcon, allCampaignLabel);
//		hor.setHeight("5px");
		this.setColspan(hor, 2);
		vaadinIcon.addClickListener(event -> fireEvent(new CloseEvent(this)));
		add(hor);
		// Configure what is passed to the fields here
		configureFields(xx);

	}

	private void configureFields(CampaignFormMetaReferenceDto xx) {
		H2 camapaignBasics = new H2("Campaign basics");
		this.setColspan(camapaignBasics, 2);
		this.setColspan(description, 2);
		description.getStyle().set("height", "10rem");

		creatingUser.setReadOnly(true);
		creatingUuid.setReadOnly(true);
		campaaignYear.setReadOnly(true);

		creatingUser.setWidthFull();
		creatingUuid.setWidthFull();
		campaaignYear.setWidthFull();

		binder.forField(creatingUuid).bind(CampaignDto.UUID);
		binder.forField(creatingUser).bind(CampaignDto.CREATING_USER_NAME);
		binder.forField(campaaignYear).bind(CampaignDto.CAMPAIGN_YEAR);

		HorizontalLayout hort = new HorizontalLayout();
		hort.add(creatingUuid, creatingUser, campaaignYear);
		hort.setJustifyContentMode(JustifyContentMode.BETWEEN);
		this.setColspan(hort, 2);

//		binder.forField(description).bind(CampaignDto.DESCRIPTION);

		binder.forField(campaignName).asRequired("Campaign Name is Required").bind(CampaignDto::getName,
				CampaignDto::setName);

//		campaignRoundField.setItems();
		binder.forField(round).asRequired("Campaign Round is Required").bind(CampaignDto.ROUND);
		round.setItems(CampaignRounds.values());
//		round.setItemLabelGenerator(CampaignDto::getRound);
		round.addValueChangeListener(e -> {

		});

		binder.forField(startDate).bind(CampaignDto.START_DATE).toString();
		binder.forField(endDate).bind(CampaignDto.END_DATE);

		binder.forField(description).asRequired("Campaign Description is Required").bind(CampaignDto::getDescription,
				CampaignDto::setDescription);

		final HorizontalLayout layoutParent = new HorizontalLayout();
		layoutParent.setWidthFull();
		this.setColspan(layoutParent, 2);
		TabSheet tabsheetParent = new TabSheet();
		layoutParent.add(tabsheetParent);

		VerticalLayout parentTab1 = new VerticalLayout();
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setWidthFull();

		TabSheet tabsheet = new TabSheet();
		layout.add(tabsheet);

		VerticalLayout tab1 = new VerticalLayout();
//		H1 text = new H1("Content Goes Here");
//		campaignFormGridComponent = new CampaignFormGridComponent(
//				this.campaignDto == null ? Collections.EMPTY_LIST
//						: new ArrayList<>(campaignDto.getCampaignFormMetas("pre-campaign")),
//				FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRound("pre-campaign"));
//		tab1.add(campaignFormGridComponent);
//		tab1.setCaption("Pre Campaign Forms");
		
//		CampaignFormGridComponent comp = new CampaignFormGridComponent(
//				this.campaignDto == null ? Collections.EMPTY_LIST
//						: new ArrayList<>(campaignDto.getCampaignFormMetas("pre-campaign"))
//						,FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRound("pre-campaign"));
		CampaignFormGridComponent comp = new CampaignFormGridComponent("pre-campaign");
		tab1.add(comp);
		
		tabsheet.add("Pre Campaign Forms", tab1);

		VerticalLayout tab2 = new VerticalLayout();
//		tab2.addComponent(campaignDashboardGridComponent);
//		tab2.setCaption("Pre Campaign Dashboard");
		final List<CampaignDashboardElement> campaignDashboardElements = FacadeProvider.getCampaignFacade()
				.getCampaignDashboardElements(null, PRE_CAMPAIGN);
		CampaignDashboardGridElementComponent comp1 = new CampaignDashboardGridElementComponent(this.campaignDto == null
				? Collections.EMPTY_LIST
				: FacadeProvider.getCampaignFacade().getCampaignDashboardElements(campaignDto.getUuid(), PRE_CAMPAIGN),
				campaignDashboardElements);
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
		CampaignFormGridComponent compp = new CampaignFormGridComponent("intra-campaign");
		tab1Intra.add(compp);
		tabsheetIntra.add("Intra Campaign Forms", tab1Intra);
		tabsheetIntra.setWidthFull();

		VerticalLayout tab2Intra = new VerticalLayout();
//		tab2.addComponent(campaignDashboardGridComponent);
//		tab2.setCaption("Pre Campaign Dashboard");
		tabsheetIntra.add("Intra Campaign Dashboard", tab2Intra);
		parentTab2.add(layoutIntra);
		parentTab2.getStyle().set("color", "green");
		tabsheetParent.add("Intra-Campaign Phase", parentTab2);

		VerticalLayout parentTab3 = new VerticalLayout();
		final HorizontalLayout layoutPost = new HorizontalLayout();
		layoutPost.setWidthFull();

		TabSheet tabsheetPost = new TabSheet();
		layoutPost.add(tabsheetPost);

		VerticalLayout tab1Post = new VerticalLayout();
//		tab1.addComponent(campaignFormsGridComponent);
//		tab1.setCaption("Pre Campaign Forms");
//		CampaignFormMetaFacade campaignFormMetaFacade = FacadeProvider.getCampaignFormMetaFacade();
//		List<CampaignFormMetaReferenceDto> campaignFormMetas = campaignFormMetaFacade.getAllCampaignFormMetasAsReferencesByRound("post-campaign");
		CampaignFormGridComponent comppp = new CampaignFormGridComponent("post-campaign");
//		System.out.println(this.campaignDto.getUuid() + "tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");
		tab1Post.add(comppp);
		tabsheetPost.add("Post Campaign Forms", tab1Post);
		

		VerticalLayout tab2Post = new VerticalLayout();
//		tab2.addComponent(campaignDashboardGridComponent);
//		tab2.setCaption("Pre Campaign Dashboard");
		tabsheetPost.add("Post Campaign Dashboard", tab2Post);
		tabsheetPost.setWidthFull();
		parentTab3.add(layoutPost);
		tabsheetParent.add("Post-Campaign Phase", parentTab3);

		System.out.println(campaignDto + "campaign DTOoooooooooooooooooooooooooooooooooo");
		
			
		VerticalLayout parentTab4 = new VerticalLayout();
		final HorizontalLayout layoutAssocCamp = new HorizontalLayout();
		layoutAssocCamp.setWidthFull();

		treeGrid.setWidthFull();

		List<AreaReferenceDto> areas = FacadeProvider.getAreaFacade().getAllActiveAsReference();

		treeGrid.setItems(generateTreeGridData(), CampaignTreeGridDto::getRegionData);

		treeGrid.setWidthFull();

		treeGrid.addHierarchyColumn(CampaignTreeGridDto::getName).setHeader("Location");

		treeGrid.addColumn(CampaignTreeGridDto::getPopulationData).setHeader("Population");


	    GridMultiSelectionModel<CampaignTreeGridDto> selectionModel
	      = (GridMultiSelectionModel<CampaignTreeGridDto>) treeGrid.setSelectionMode(SelectionMode.MULTI);
	    if(campaignDto != null) {
	    
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
		
//		VerticalLayout parentTab5 = new VerticalLayout();
//		final HorizontalLayout layoutPopulationData = new HorizontalLayout();
//		layoutPopulationData.setWidthFull();
//		
//
//		parentTab5.add(layoutPopulationData);
//		tabsheetParent.add("Population Data ", parentTab5);

		VerticalLayout parentTab5 = new VerticalLayout();
		parentTab5.setId("parentTab5");

		VerticalLayout poplayout = new VerticalLayout();
		poplayout.setSpacing(false);
		poplayout.setId("poplayout");

		Label lblIntroduction = new Label(I18nProperties.getString(Strings.infoPopulationDataView));

		poplayout.add(lblIntroduction);

		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, lblIntroduction);// .setHorizontalComponentAlignment(lblIntroduction,
//																						// Alignment.CENTER);
//		ComboBox campaignFilter = new ComboBox<>();
//		Dialog dialog = new Dialog();
//		campaignFilter.setId(CampaignDto.NAME);
//		campaignFilter.setRequired(true);
//		campaignFilter.setItems(FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference());
//		campaignFilter.setEnabled(false);
//		
//		Label lblCollectionDateInfo = new Label(I18nProperties.getString(Strings.infoPopulationCollectionDate));
//		dialog.add(lblCollectionDateInfo);
//		dialog.add(campaignFilter);
		Button btnImport = new Button("Import");// , e -> {
//			Window window = VaadinUiUtil.showPopupWindow(new InfrastructureImportLayout(InfrastructureType.POPULATION_DATA, campaignDto));
//			window.setCaption(I18nProperties.getString(Strings.headingImportPopulationData));
//		});
		btnImport.addClickListener(e -> {
			ImportPopulationDataDialog dialog = new ImportPopulationDataDialog(null, campaignDto);
			dialog.open();
		});

		poplayout.add(btnImport);
		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, btnImport);

		Button btnExport = new Button("Export"); // poplayout.addComponent(btnExport);
		poplayout.add(btnExport);
		poplayout.setHorizontalComponentAlignment(Alignment.CENTER, btnExport);
//
//		StreamResource populationDataExportResource = DndUtil.createPopulationDataExportResource();
//		new DefaultFileDownloader(populationDataExportResource).extend(btnExport);

		parentTab5.add(poplayout);

		// parentTab5.addComponent(treeGrid);
//		parentTab5.getStyle().set("background", "red");
		tabsheetParent.add("Population Data", parentTab5);
		tabsheetParent.setWidthFull();
		tabsheetParent.setId("tabsheetParent");

		CampaignActionButtons actionButtons = new CampaignActionButtons();
		this.setColspan(actionButtons, 2);

		add(camapaignBasics, hort, campaignName, round, startDate, endDate, description, layoutParent, actionButtons);
	}

	public void setCampaign(CampaignDto user) {
		binder.setBean(user);
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

	private List<CampaignTreeGridDto> generateTreeGridData() {
        List<CampaignTreeGridDto> gridData = new ArrayList<>();
        List<AreaReferenceDto> areas = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		
        for (AreaReferenceDto area_ : areas) {
        	CampaignTreeGridDto areaData = new CampaignTreeGridDto(area_.getCaption(), area_.getExternalId(), "Area", area_.getUuid(), "area");
        	System.out.println(areaData + "uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
        	List<RegionReferenceDto> regions_ = FacadeProvider.getRegionFacade().getAllActiveByArea(area_.getUuid());
        	 for (RegionReferenceDto regions_x : regions_) {
        		 CampaignTreeGridDto regionData = new CampaignTreeGridDto(regions_x.getCaption(), regions_x.getExternalId(), area_.getUuid(), regions_x.getUuid(), "region");
//        		 List<DistrictReferenceDto> district_ = FacadeProvider.getDistrictFacade().getAllActiveByRegion( regions_x.getUuid());
//        		 ArrayList arr = new ArrayList<>();
//        		 for (DistrictDto district_x : district_) {
//        			 arr.add(new CampaignTreeGridDtoImpl(district_x.getName(), district_x.getPopulationData(), district_x.getRegionId(),
//        					 district_x.getRegionUuid_(), district_x.getUuid_(), "district", district_x.getSelectedPopulationData()));
//         		};
//        		 
//        		 regionData.setRegionData(arr);
//        		 
        		 areaData.addRegionData(regionData);
            }
        	
        	 gridData.add(areaData);
        }
        return gridData;
    }

}
