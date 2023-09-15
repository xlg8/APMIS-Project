package com.cinoteck.application.views.configurations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionCriteria;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("APMIS-Clusters")
@Route(value = "clusters", layout = ConfigurationsView.class)
public class ClusterView extends VerticalLayout {

	private static final long serialVersionUID = 5091856954264511639L;
//	private GridListDataView<CommunityDto> dataView;
	private CommunityCriteriaNew criteria = new CommunityCriteriaNew();

	ClusterDataProvider clusterDataProvider = new ClusterDataProvider();
	CommunityDto communityDto;
	ConfigurableFilterDataProvider<CommunityDto, Void, CommunityCriteriaNew> filteredDataProvider;

	Grid<CommunityDto> grid = new Grid<>(CommunityDto.class, false);
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	UserProvider currentUser = new UserProvider();
	Paragraph countRowItems;

	UserProvider userProvider = new UserProvider();
	Button enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
	Button leaveBulkEdit = new Button(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
	MenuBar dropdownBulkOperations = new MenuBar();
	SubMenu subMenu;
	ConfirmDialog archiveDearchiveConfirmation;
	String uuidsz = "";
	ListDataProvider<CommunityDto> dataProvider;
	int itemCount;

	TextField searchField = new TextField();
	ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>(I18nProperties.getCaption(Captions.area));
	ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>(I18nProperties.getCaption(Captions.region));
	ComboBox<DistrictReferenceDto> districtFilter = new ComboBox<>(I18nProperties.getCaption(Captions.district));
	Button resetFilters = new Button(I18nProperties.getCaption(Captions.resetFilters));
	ComboBox<EntityRelevanceStatus> relevanceStatusFilter = new ComboBox<>(
			I18nProperties.getCaption(Captions.relevanceStatus));

	@SuppressWarnings("deprecation")
	public ClusterView() {
		setSpacing(false);
		setHeightFull();
		setSizeFull();
		addFilters();
		clusterGrid();

	}

	private void clusterGrid() {

		System.out.println(criteria + "tttttttttttttttttttttttttttttttttttttttttttttttt");
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(CommunityDto::getAreaname).setHeader(I18nProperties.getCaption(Captions.area)).setSortable(true)
				.setResizable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
		grid.addColumn(CommunityDto::getAreaexternalId).setHeader(I18nProperties.getCaption(Captions.Area_externalId))
				.setResizable(true).setSortable(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
		grid.addColumn(CommunityDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region)).setSortable(true)
				.setResizable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.region));
		grid.addColumn(CommunityDto::getRegionexternalId)
				.setHeader(I18nProperties.getCaption(Captions.Region_externalID)).setResizable(true).setSortable(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Region_externalID));
		grid.addColumn(CommunityDto::getDistrict).setHeader(I18nProperties.getCaption(Captions.district))
				.setSortable(true).setResizable(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.district));
		grid.addColumn(CommunityDto::getDistrictexternalId)
				.setHeader(I18nProperties.getCaption(Captions.District_externalID)).setResizable(true).setSortable(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.District_externalID));
		grid.addColumn(CommunityDto::getName).setHeader(I18nProperties.getCaption(Captions.community)).setSortable(true)
				.setResizable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.community));
		grid.addColumn(CommunityDto::getExternalId).setHeader(I18nProperties.getCaption(Captions.Community_externalID))
				.setResizable(true).setSortable(true)
				.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Community_externalID));

		grid.setVisible(true);

//		if (criteria == null) {
//			criteria = new CommunityCriteriaNew();
//			criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
//		}
		criteria.relevanceStatus(EntityRelevanceStatus.ALL);
        refreshGridData();

		dataProvider = DataProvider
				.fromStream(FacadeProvider.getCommunityFacade().getIndexList(criteria, null, null, null).stream());

//		 dataProvider = DataProvider.fromFilteringCallbacks(
//				query -> FacadeProvider.getCommunityFacade()
//					.getIndexList(
//							criteria,
//						query.getOffset(),
//						query.getLimit(),
//						query.getSortOrders()
//							.stream()
//							.map(sortOrder -> new SortProperty(sortOrder.getSorted(), sortOrder.getDirection() == SortDirection.ASCENDING))
//							.collect(Collectors.toList()))
//					.stream(),
//				query -> {
//					return (int) FacadeProvider.getCommunityFacade().count(query.getFilter().orElse(null));
//				});
//		grid.setDataProvider(dataProvider);
		grid.setItems(dataProvider);
//		dataView = grid.setItems(dataProvider);

		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EDIT)) {

			grid.asSingleSelect().addValueChangeListener(event -> {
				if (event.getValue() != null) {
					createOrEditCluster(event.getValue());
				}
			});
		}

		add(grid);

		GridExporter<CommunityDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);

		exporter.setTitle(I18nProperties.getCaption(Captions.mainMenuUsers));
		exporter.setFileName(
				"APMIS_Clusters" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("exportCluster");
		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());

	}

	// TODO: Hide the filter bar on smaller screens
	public Component addFilters() {

//		if (criteria == null) {
//			criteria = new CommunityCriteriaNew();
//		}
		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
			leaveBulkEdit = new Button();
			dropdownBulkOperations = new MenuBar();
			MenuItem bulkActionsItem = dropdownBulkOperations.addItem(I18nProperties.getCaption(Captions.bulkActions));
			subMenu = bulkActionsItem.getSubMenu();
			subMenu.addItem("Archive", e -> handleArchiveDearchiveAction());

		}

		criteria.relevanceStatus(EntityRelevanceStatus.ARCHIVED);
		dataProvider = DataProvider
				.fromStream(FacadeProvider.getCommunityFacade().getIndexList(criteria, null, null, null).stream());

		itemCount = dataProvider.getItems().size();
		countRowItems = new Paragraph(I18nProperties.getCaption(Captions.rows) + itemCount);
//
		countRowItems.setId("rowCount");

		HorizontalLayout layout = new HorizontalLayout();
		layout.setPadding(false);
		layout.setVisible(true);
		layout.setAlignItems(Alignment.END);

		HorizontalLayout relevancelayout = new HorizontalLayout();
		relevancelayout.setPadding(false);
		relevancelayout.setVisible(true);
		relevancelayout.setAlignItems(Alignment.END);
		relevancelayout.setJustifyContentMode(JustifyContentMode.END);
		relevancelayout.setClassName("row");

		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);

		vlayout.setAlignItems(Alignment.END);

		Button displayFilters = new Button(I18nProperties.getCaption(Captions.hideFilters),
				new Icon(VaadinIcon.SLIDERS));
		displayFilters.getStyle().set("margin-left", "1em");
		displayFilters.addClickListener(e -> {
			if (layout.isVisible() == false) {
				layout.setVisible(true);
				relevancelayout.setVisible(true);

				displayFilters.setText(I18nProperties.getCaption(Captions.hideFilters));
			} else {
				layout.setVisible(false);
				relevancelayout.setVisible(false);

				displayFilters.setText(I18nProperties.getCaption(Captions.showFilters));
			}
		});

		layout.setPadding(false);

		TextField searchField = new TextField();
		ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>(I18nProperties.getCaption(Captions.area));
		ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>(I18nProperties.getCaption(Captions.region));
		ComboBox<DistrictReferenceDto> districtFilter = new ComboBox<>(I18nProperties.getCaption(Captions.district));
		Button resetFilters = new Button(I18nProperties.getCaption(Captions.resetFilters));
		ComboBox<EntityRelevanceStatus> relevanceStatusFilter = new ComboBox<>(
				I18nProperties.getCaption(Captions.relevanceStatus));

		searchField.addClassName("filterBar");
		searchField.setPlaceholder(I18nProperties.getCaption(Captions.actionSearch));
		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.getStyle().set("color", "#0D6938");
		searchField.setPrefixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setWidth("10%");
		layout.add(searchField);

		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.areaAllAreas));
		regionFilter.setClearButtonVisible(true);
		regionFilter.getStyle().set("width", "145px !important");
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		if (currentUser.getUser().getArea() != null) {
			regionFilter.setValue(currentUser.getUser().getArea());
			criteria.area(currentUser.getUser().getArea());
			provinceFilter.setItems(
					FacadeProvider.getRegionFacade().getAllActiveByArea(currentUser.getUser().getArea().getUuid()));
			regionFilter.setEnabled(false);
			refreshGridData();
		}

		layout.add(regionFilter);

		provinceFilter.setPlaceholder(I18nProperties.getCaption(Captions.regionAllRegions));
		provinceFilter.setClearButtonVisible(true);
		provinceFilter.getStyle().set("width", "145px !important");

//		provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		if (currentUser.getUser().getRegion() != null) {
			provinceFilter.setValue(currentUser.getUser().getRegion());
			criteria.region(currentUser.getUser().getRegion());
			districtFilter.setItems(FacadeProvider.getDistrictFacade()
					.getAllActiveByRegion(currentUser.getUser().getRegion().getUuid()));
			provinceFilter.setEnabled(false);
			refreshGridData();
		}
		layout.add(provinceFilter);

		districtFilter.setPlaceholder(I18nProperties.getCaption(Captions.districtAllDistricts));
		districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReference());
		districtFilter.getStyle().set("width", "145px !important");

		if (currentUser.getUser().getDistrict() != null) {
			districtFilter.setValue(currentUser.getUser().getDistrict());
			criteria.district(currentUser.getUser().getDistrict());
			districtFilter.setEnabled(false);
			refreshGridData();
		}
		layout.add(districtFilter);

		relevanceStatusFilter.setItems(EntityRelevanceStatus.values());
		relevanceStatusFilter.getStyle().set("width", "145px !important");

		relevanceStatusFilter.setItemLabelGenerator(status -> {
			if (status == EntityRelevanceStatus.ARCHIVED) {
				return I18nProperties.getCaption(Captions.communityArchivedCommunities);
			} else if (status == EntityRelevanceStatus.ACTIVE) {
				return I18nProperties.getCaption(Captions.communityActiveCommunities);
			} else if (status == EntityRelevanceStatus.ALL) {
				return I18nProperties.getCaption(Captions.communityAllCommunities);
			}
			// Handle other enum values if needed
			return status.toString();
		});

		layout.add(resetFilters);

		searchField.addValueChangeListener(e -> {
			criteria.nameLike(e.getValue());
			refreshGridData();
			resetFilters.setVisible(true);

		});

		regionFilter.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid()));
				AreaReferenceDto area = e.getValue();
				criteria.area(area);
				refreshGridData();
				System.out.println(regionFilter.getValue() + "region filtervalue ");
				resetFilters.setVisible(true);
			} else {
				criteria.area(null);
				refreshGridData();
			}

		});

		provinceFilter.addValueChangeListener(e -> {
			if (provinceFilter.getValue() != null) {
			districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid()));
//			filteredDataProvider.setFilter(criteria);
			RegionReferenceDto province = e.getValue();
			criteria.region(province);
			refreshGridData();
		}else {
				criteria.region(null);
				refreshGridData();
			}

		});
		districtFilter.addValueChangeListener(e -> {
			if (districtFilter.getValue() != null) {
//			filteredDataProvider.setFilter(criteria);
			DistrictReferenceDto district = e.getValue();
			criteria.district(district);
			refreshGridData();
			
		}else {
				criteria.district(null);
				refreshGridData();
			}
		});

		relevanceStatusFilter.addValueChangeListener(e -> {
			
			EntityRelevanceStatus selectedStatus = e.getValue();
            criteria.relevanceStatus(selectedStatus);
            refreshGridData();
//			if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ACTIVE)) {
//				subMenu.removeAll();
//				subMenu.addItem("Archive", event -> handleArchiveDearchiveAction());
//				criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
//				refreshGridData();
//			} else if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ARCHIVED)) {
//				subMenu.removeAll();
//				subMenu.addItem("De-Archive", event -> handleArchiveDearchiveAction());
//				criteria.relevanceStatus(EntityRelevanceStatus.ARCHIVED);
//				refreshGridData();
//
//			} else {
//				subMenu.removeAll();
//				subMenu.addItem(I18nProperties.getString(Strings.selectActiveArchivedRelevance));
//				criteria.relevanceStatus(EntityRelevanceStatus.ALL);
//				refreshGridData();
//			}

		});

		resetFilters.addClassName("resetButton");
//		resetFilters.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		resetFilters.setVisible(false);

		Button addNew = new Button(I18nProperties.getCaption(Captions.addNewCluster));
		addNew.getElement().getStyle().set("white-space", "normal");
		addNew.getStyle().set("color", "white");
		addNew.getStyle().set("background", "#0D6938");
		addNew.addClickListener(event -> {
			createOrEditCluster(communityDto);
		});

		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_CREATE)) {
			layout.add(addNew);
		}
		Button exportCluster = new Button("Export");
		exportCluster.setIcon(new Icon(VaadinIcon.UPLOAD));

		exportCluster.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");

		});
		
		Button importCluster = new Button("Import");
		importCluster.setIcon(new Icon(VaadinIcon.DOWNLOAD));

		importCluster.addClickListener(e -> {
			ImportClusterDataDialog dialog = new ImportClusterDataDialog();
			dialog.open();

		});
		anchor.getStyle().set("display", "none");
		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EXPORT)) {
			layout.add(importCluster, exportCluster, anchor);
		}
//		layout.addComponentAsFirst(anchor);
		layout.setWidth("75%");
		layout.addClassName("pl-3");
		layout.addClassName("row");

		relevancelayout.add(relevanceStatusFilter, countRowItems);
		vlayout.setWidth("99%");
		vlayout.add(displayFilters, layout, relevancelayout);
		add(vlayout);

		dropdownBulkOperations.getStyle().set("margin-top", "5px");

		enterBulkEdit.addClassName("bulkActionButton");
		Icon bulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		enterBulkEdit.setIcon(bulkModeButtonnIcon);
		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			layout.add(enterBulkEdit);
		}

		enterBulkEdit.addClickListener(e -> {
			dropdownBulkOperations.setVisible(true);
			grid.setSelectionMode(Grid.SelectionMode.MULTI);
			enterBulkEdit.setVisible(false);
			leaveBulkEdit.setVisible(true);

		});

		leaveBulkEdit.setText(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
		leaveBulkEdit.addClassName("leaveBulkActionButton");
		leaveBulkEdit.setVisible(false);
		Icon leaveBulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		leaveBulkEdit.setIcon(leaveBulkModeButtonnIcon);
		layout.add(leaveBulkEdit);
		layout.addClassName("pl-3");
		layout.addClassName("row");

		leaveBulkEdit.addClickListener(e -> {
			grid.setSelectionMode(Grid.SelectionMode.SINGLE);
			enterBulkEdit.setVisible(true);
			leaveBulkEdit.setVisible(false);
			dropdownBulkOperations.setVisible(false);
		});
		dropdownBulkOperations.setVisible(false);
		layout.add(dropdownBulkOperations);

		resetFilters.addClickListener(e -> {

			if (!searchField.isEmpty()) {
				refreshGridData();
				searchField.clear();
			}
			if (!regionFilter.isEmpty()) {
				refreshGridData();
				regionFilter.clear();
			}
			if (!provinceFilter.isEmpty()) {
				refreshGridData();
				provinceFilter.clear();
			}
			if (!districtFilter.isEmpty()) {
				refreshGridData();
				districtFilter.clear();
			}
			if (!relevanceStatusFilter.isEmpty()) {
				refreshGridData();
				relevanceStatusFilter.clear();
			}
			refreshGridData();

		});
		return vlayout;
	}

	private void handleArchiveDearchiveAction() {

		archiveDearchiveAllSelectedItems(grid.getSelectedItems());
		Notification.show(I18nProperties.getString(Strings.deleteActionSelected));
	}

	public void archiveDearchiveAllSelectedItems(Collection<CommunityDto> selectedRows) {
		archiveDearchiveConfirmation = new ConfirmDialog();
		if (selectedRows.size() == 0) {

			archiveDearchiveConfirmation.setRejectable(true);
			archiveDearchiveConfirmation.addRejectListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.setConfirmText("Ok");

			archiveDearchiveConfirmation.setHeader(I18nProperties.getCaption(Captions.errorArchiving));
			archiveDearchiveConfirmation.setText(I18nProperties.getString(Strings.youHaveNotSeleceted));

			archiveDearchiveConfirmation.open();
		} else {

			archiveDearchiveConfirmation.setRejectable(true);
			archiveDearchiveConfirmation.setRejectText("No");
			archiveDearchiveConfirmation.setConfirmText("Yes");
			archiveDearchiveConfirmation.addRejectListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.open();

			for (CommunityDto selectedRow : (Collection<CommunityDto>) selectedRows) {
				;
				boolean archive = communityDto.isArchived();

				if (!archive) {
					archiveDearchiveConfirmation.setHeader(I18nProperties.getCaption(Captions.archiveSelectedCluster));
					archiveDearchiveConfirmation
							.setText(I18nProperties.getString(Strings.areSureYouWantArchiveCluster));
					archiveDearchiveConfirmation.addConfirmListener(e -> {
						FacadeProvider.getCommunityFacade().archive(selectedRow.getUuid());
//						if (leaveBulkEdit.isVisible()) {
//							leaveBulkEdit.setVisible(false);
//							enterBulkEdit.setVisible(true);
//							grid.setSelectionMode(Grid.SelectionMode.SINGLE);
//							dropdownBulkOperations.setVisible(false);
//						}
						refreshGridData();
					});
					Notification.show(I18nProperties.getString(Strings.archivingSelected));
				} else {
					archiveDearchiveConfirmation
							.setHeader(I18nProperties.getCaption(Captions.dearchiveSelectedClusters));
					archiveDearchiveConfirmation
							.setText(I18nProperties.getString(Strings.areYouSureYouWantToDearchiveSelected));
					archiveDearchiveConfirmation.addConfirmListener(e -> {
						FacadeProvider.getCommunityFacade().dearchive(selectedRow.getUuid());
//						if (leaveBulkEdit.isVisible()) {
//							leaveBulkEdit.setVisible(false);
//							enterBulkEdit.setVisible(true);
//							grid.setSelectionMode(Grid.SelectionMode.SINGLE);
//							dropdownBulkOperations.setVisible(false);
//						}
						refreshGridData();
					});

					Notification.show(I18nProperties.getString(Strings.dearchiveSelectedRows));
				}
			}

		}
	}

	private void updateRowCount() {
		int numberOfRows = filteredDataProvider.size(new Query<>());
		String newText = I18nProperties.getCaption(Captions.rows) + numberOfRows;

		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}

	private void refreshGridData() {
		 dataProvider = DataProvider
				.fromStream(FacadeProvider.getCommunityFacade().getIndexList(criteria, null, null, null).stream());
		grid.setItems(dataProvider);
//		dataView = grid.setItems(dataProvider);
		itemCount = dataProvider.getItems().size();

		String newText = I18nProperties.getCaption(Captions.rows) + itemCount;
		countRowItems.setText(newText);
		countRowItems.setId("rowCount");

//		dataView = grid.setItems(dataProvider);
	}

	public boolean createOrEditCluster(CommunityDto communityDto) {
		Dialog dialog = new Dialog();
		FormLayout fmr = new FormLayout();
		TextField nameField = new TextField(I18nProperties.getCaption(Captions.name));
//		
		TextField clusterNumber = new TextField(I18nProperties.getCaption(Captions.clusterNumber));

		TextField cCodeField = new TextField(I18nProperties.getCaption(Captions.Community_externalID));
		ComboBox<RegionReferenceDto> provinceOfDistrict = new ComboBox<>(I18nProperties.getCaption(Captions.region));
		ComboBox<DistrictReferenceDto> districtOfCluster = new ComboBox<>(I18nProperties.getCaption(Captions.district));
		provinceOfDistrict.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());

		provinceOfDistrict.addValueChangeListener(e -> {
//			districtOfCluster.clear();
			districtOfCluster.setItems(
					FacadeProvider.getDistrictFacade().getAllActiveByRegion(provinceOfDistrict.getValue().getUuid()));
		});
		if (communityDto != null) {
			nameField.setValue(communityDto.getName());
			clusterNumber.setValue(communityDto.getClusterNumber().toString());
			cCodeField.setValue(communityDto.getExternalId().toString());
//			provinceOfDistrict.setItems(communityDto.getRegion());
			provinceOfDistrict.setValue(communityDto.getRegion());
			provinceOfDistrict.setEnabled(true);
//			districtOfCluster.setItems(communityDto.getDistrict());
			districtOfCluster.setValue(communityDto.getDistrict());
			districtOfCluster.setEnabled(true);
		}

		// this can generate null
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));
		Button discardButton = new Button(I18nProperties.getCaption(Captions.actionDiscard), e -> dialog.close());
		saveButton.getStyle().set("margin-right", "10px");
		Button archiveButton = new Button();

		if (communityDto != null) {

//			dto = new Co();
			String regionUUid = communityDto.getUuid();
//			dto = FacadeProvider.getCommunityFacade().getByUuid(regionUUid);
			boolean isArchivedx = communityDto.isArchived();
			archiveButton.setText(isArchivedx ? "De-Archive" : "Archive");

			Collection<?> selectedRows;
			selectedRows = grid.getSelectedItems();
			Set<String> selectedRowsUuids = selectedRows.stream().map(row -> ((HasUuid) row).getUuid())
					.collect(Collectors.toSet());
			System.out.println(selectedRowsUuids + " selected row infracsucfhshfvshfhjvs");
			archiveButton.addClickListener(archiveEvent -> {
//				String uuidsz = "";
				if (communityDto != null) {
					archiveDearchiveConfirmation = new ConfirmDialog();
					archiveDearchiveConfirmation.setCancelable(true);
					archiveDearchiveConfirmation.addCancelListener(e -> dialog.close());
					archiveDearchiveConfirmation.setRejectable(true);
					archiveDearchiveConfirmation.setRejectText("No");
					archiveDearchiveConfirmation.addRejectListener(e -> dialog.close());
					archiveDearchiveConfirmation.setConfirmText("Yes");
					archiveDearchiveConfirmation.open();
					uuidsz = communityDto.getUuid();

					System.out.println(uuidsz + "areeeeeeeeeeeeeeeeeaaaaaaaaaaaaaaa by uuid");
					boolean isArchived = communityDto.isArchived();
					if (uuidsz != null) {
						if (isArchived == true) {
							archiveDearchiveConfirmation
									.setHeader(I18nProperties.getCaption(Captions.dearchiveCluster));
							archiveDearchiveConfirmation
									.setText(I18nProperties.getString(Strings.areYouSureYouWantToDearchive));

							archiveDearchiveConfirmation.addConfirmListener(e -> {
								FacadeProvider.getCommunityFacade().dearchive(uuidsz);
								dialog.close();
								refreshGridData();
							});
						} else {
							archiveDearchiveConfirmation.setHeader(I18nProperties.getCaption(Captions.archiveCluster));
							archiveDearchiveConfirmation
									.setText(I18nProperties.getString(Strings.areSureYouWantToArchive));

							archiveDearchiveConfirmation.addConfirmListener(e -> {
								FacadeProvider.getCommunityFacade().archive(uuidsz);
								dialog.close();
								refreshGridData();
							});

						}
					}
				}
			});
		}

		saveButton.addClickListener(saveEvent -> {

			String name = nameField.getValue();
			String clusterNum = clusterNumber.getValue();
			String code = cCodeField.getValue();

			String uuids = "";
			if (communityDto != null) {
				uuids = communityDto.getUuid();
			}

			if (name != null && code != null) {

				CommunityDto dce = FacadeProvider.getCommunityFacade().getByUuid(uuids);
				if (dce != null) {
					dce.setName(name);
					int clusternumbervalue = Integer.parseInt(clusterNum);
					dce.setClusterNumber(clusternumbervalue);
					long ccodeValue = Long.parseLong(code);
					dce.setExternalId(ccodeValue);
					dce.setRegion(provinceOfDistrict.getValue());
					dce.setDistrict(districtOfCluster.getValue());

					FacadeProvider.getCommunityFacade().save(dce, true);

					Notification.show(I18nProperties.getString(Strings.saved) + name + " " + code);
					dialog.close();
					refreshGridData();
				} else {
					CommunityDto dcex = new CommunityDto();
					dcex.setName(name);
					int clusternumbervalue = Integer.parseInt(clusterNum);
					dcex.setClusterNumber(clusternumbervalue);
					Long ccodeValue = Long.parseLong(code);
					dcex.setExternalId(ccodeValue);
					dcex.setRegion(provinceOfDistrict.getValue());
					dcex.setDistrict(districtOfCluster.getValue());
					Long finder = 1L;

					// Code block to get pcode of selected district, ends at line
					List<DistrictIndexDto> pcode = FacadeProvider.getDistrictFacade().getAllDistricts();
					for (DistrictIndexDto districtIndexDto : pcode) {
						String checkerName = districtIndexDto.getName();

						if (checkerName.trim().equals(districtOfCluster.getValue().toString().trim())) {							
							DistrictReferenceDto nuller = new DistrictReferenceDto(districtIndexDto.getUuid(),
									districtIndexDto.getName(), districtIndexDto.getExternalId());
							dcex.setDistrict(nuller);
						}
					}

					if (dcex.getDistrict().getExternalId() != null) {

						Long cCodeConstruction = dcex.getDistrict().getExternalId();
						if (clusterNum.length() == 1) {

							clusterNum = "00" + clusterNum.toString();
						} else if (clusterNum.length() == 2) {

							clusterNum = "0" + clusterNum.toString();
						}

						cCodeConstruction = Long.parseLong(cCodeConstruction + clusterNum);

						if (ccodeValue.equals(cCodeConstruction)) {
							try {
								FacadeProvider.getCommunityFacade().save(dcex, true);
								Notification.show(I18nProperties.getString(Strings.saved) + name + " " + code);
								dialog.close();
								refreshGridData();

							} catch (Exception e) {
								Notification notification = new Notification();
								notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
								notification.setPosition(Position.MIDDLE);
								Button closeButton = new Button(new Icon("lumo", "cross"));
								closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
								closeButton.getElement().setAttribute("aria-label", "Close");
								closeButton.addClickListener(event -> {
									notification.close();
								});

//								Paragraph text = new Paragraph(
//										"An unexpected error occurred. Please contact your supervisor or administrator and inform them about it.");

								Paragraph text = new Paragraph(
										"Cluster number taken, choose another");
								
								HorizontalLayout layout = new HorizontalLayout(text, closeButton);
								layout.setAlignItems(Alignment.CENTER);

								notification.add(layout);
								notification.open();
							}
						} else {

							Notification notification = new Notification();
							notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
							notification.setPosition(Position.MIDDLE);
							Button closeButton = new Button(new Icon("lumo", "cross"));
							closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
							closeButton.getElement().setAttribute("aria-label", "Close");
							closeButton.addClickListener(event -> {
								notification.close();
							});

							Paragraph text = new Paragraph(
									"You have entered a wrong Ccode, check Dcode with Cluster Number and try again");

							HorizontalLayout layout = new HorizontalLayout(text, closeButton);
							layout.setAlignItems(Alignment.CENTER);

							notification.add(layout);
							notification.open();
						}

					}
				}
			} else {
				Notification.show(I18nProperties.getCaption(Captions.notValidValue) + name + " " + code);
			}

		});

//		dialog.setHeaderTitle("Edit " + communityDto.getName());
		if (communityDto == null) {
			dialog.setHeaderTitle(I18nProperties.getCaption(Captions.addNewCluster));
			dialog.getFooter().add(discardButton, saveButton);
		} else {
			dialog.setHeaderTitle(I18nProperties.getCaption(Captions.edit) + communityDto.getName());
			dialog.getFooter().add(archiveButton, discardButton, saveButton);

		}
		fmr.add(nameField, cCodeField, clusterNumber, provinceOfDistrict, districtOfCluster);
		dialog.add(fmr);

//      getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
//		.set("display", "flex").set("align-items", "center").set("justify-content", "center");

		dialog.open();

		return true;
	}

}