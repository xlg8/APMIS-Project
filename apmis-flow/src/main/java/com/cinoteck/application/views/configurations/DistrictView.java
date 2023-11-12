package com.cinoteck.application.views.configurations;

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
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
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
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionCriteria;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("APMIS-Districts")
@Route(value = "districts", layout = ConfigurationsView.class)
public class DistrictView extends VerticalLayout {

	private static final long serialVersionUID = 1370022184569877189L;

	DistrictCriteria criteria;
	DistrictIndexDto districtIndexDto;
	DistrictDto dto;
	DistrictDataProvider districtDataProvider = new DistrictDataProvider();

	ConfigurableFilterDataProvider<DistrictIndexDto, Void, DistrictCriteria> filteredDataProvider;

	Grid<DistrictIndexDto> grid = new Grid<>(DistrictIndexDto.class, false);

	ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>(I18nProperties.getCaption(Captions.area));

	ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>(I18nProperties.getCaption(Captions.region));

	TextField searchField = new TextField();

	Button resetFilters = new Button(I18nProperties.getCaption(Captions.resetFilters));
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	ComboBox<String> riskFilter = new ComboBox<>(I18nProperties.getCaption(Captions.risk));
	ComboBox<EntityRelevanceStatus> relevanceStatusFilter = new ComboBox<>(
			I18nProperties.getCaption(Captions.relevanceStatus));
	Paragraph countRowItems;
	UserProvider currentUser = new UserProvider();
	Button enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
	Button leaveBulkEdit = new Button(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
	MenuBar dropdownBulkOperations = new MenuBar();
	SubMenu subMenu;
	ConfirmDialog archiveDearchiveConfirmation;
	String uuidsz = "";
	GridListDataView<DistrictIndexDto> dataView;
	ListDataProvider<DistrictIndexDto> dataProvider;
	int itemCount;
	UserProvider userProvider = new UserProvider();

	@SuppressWarnings("deprecation")
	public DistrictView() {

		this.criteria = new DistrictCriteria();
		setSpacing(false);
		setHeightFull();
		setSizeFull();
		addFiltersLayout();
		districtGrid(criteria);

	}

	private void districtGrid(DistrictCriteria criteria) {
		this.criteria = criteria;
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {

			grid.addColumn(DistrictIndexDto::getPs_af).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
			grid.addColumn(DistrictIndexDto::getAreaexternalId)
					.setHeader(I18nProperties.getCaption(Captions.Area_externalId)).setResizable(true).setSortable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
			grid.addColumn(DistrictIndexDto::getPs_af).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.region));
			grid.addColumn(DistrictIndexDto::getRegionexternalId)
					.setHeader(I18nProperties.getCaption(Captions.Region_externalID)).setResizable(true)
					.setSortable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Region_externalID));
			grid.addColumn(DistrictIndexDto::getPs_af).setHeader(I18nProperties.getCaption(Captions.district))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.district));
			grid.addColumn(DistrictIndexDto::getExternalId)
					.setHeader(I18nProperties.getCaption(Captions.District_externalID)).setResizable(true)
					.setSortable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.District_externalID));
			grid.setVisible(true);
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {

			grid.addColumn(DistrictIndexDto::getFa_af).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
			grid.addColumn(DistrictIndexDto::getAreaexternalId)
					.setHeader(I18nProperties.getCaption(Captions.Area_externalId)).setResizable(true).setSortable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
			grid.addColumn(DistrictIndexDto::getFa_af).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.region));
			grid.addColumn(DistrictIndexDto::getRegionexternalId)
					.setHeader(I18nProperties.getCaption(Captions.Region_externalID)).setResizable(true)
					.setSortable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Region_externalID));
			grid.addColumn(DistrictIndexDto::getFa_af).setHeader(I18nProperties.getCaption(Captions.district))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.district));
			grid.addColumn(DistrictIndexDto::getExternalId)
					.setHeader(I18nProperties.getCaption(Captions.District_externalID)).setResizable(true)
					.setSortable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.District_externalID));
			grid.setVisible(true);
		} else {

			grid.addColumn(DistrictIndexDto::getAreaname).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
			grid.addColumn(DistrictIndexDto::getAreaexternalId)
					.setHeader(I18nProperties.getCaption(Captions.Area_externalId)).setResizable(true).setSortable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
			grid.addColumn(DistrictIndexDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.region));
			grid.addColumn(DistrictIndexDto::getRegionexternalId)
					.setHeader(I18nProperties.getCaption(Captions.Region_externalID)).setResizable(true)
					.setSortable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Region_externalID));
			grid.addColumn(DistrictIndexDto::getName).setHeader(I18nProperties.getCaption(Captions.district))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.district));
			grid.addColumn(DistrictIndexDto::getExternalId)
					.setHeader(I18nProperties.getCaption(Captions.District_externalID)).setResizable(true)
					.setSortable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.District_externalID));
			grid.setVisible(true);
		}

		if (criteria == null) {
			criteria = new DistrictCriteria();
			criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		}

		dataProvider = DataProvider
				.fromStream(FacadeProvider.getDistrictFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);
//		grid.setDataProvider(filteredDataProvider);

		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EDIT)) {

			grid.asSingleSelect().addValueChangeListener(event -> {
				if (event.getValue() != null) {
					createOrEditDistrict(event.getValue());
				}
			});
		}

		add(grid);

		GridExporter<DistrictIndexDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.mainMenuUsers));
		exporter.setFileName(
				"APMIS_District" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("exportArea");
		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());
	}

	public Component addFiltersLayout() {

		if (criteria == null) {
			criteria = new DistrictCriteria();
		}
		if (currentUser.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
			leaveBulkEdit = new Button();
			dropdownBulkOperations = new MenuBar();
			MenuItem bulkActionsItem = dropdownBulkOperations.addItem(I18nProperties.getCaption(Captions.bulkActions));
			subMenu = bulkActionsItem.getSubMenu();
			subMenu.addItem(I18nProperties.getCaption(Captions.actionArchive), e -> handleArchiveDearchiveAction());

		}

		criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		dataProvider = DataProvider
				.fromStream(FacadeProvider.getDistrictFacade().getIndexList(criteria, null, null, null).stream());

		itemCount = dataProvider.getItems().size();
		countRowItems = new Paragraph(I18nProperties.getCaption(Captions.rows) + itemCount);

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

		searchField.addClassName("filterBar");
		searchField.setPlaceholder("Search");
		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.getStyle().set("color", "#0D6938");
		searchField.setPrefixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setWidth("10%");
		searchField.setClearButtonVisible(true);
		layout.add(searchField);

		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.areaAllAreas));
		regionFilter.getStyle().set("width", "145px !important");
		regionFilter.setClearButtonVisible(true);
		
		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto());
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari());
		} else {
			regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());			
		}
		
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

		if (currentUser.getUser().getRegion() != null) {
			provinceFilter.setValue(currentUser.getUser().getRegion());
//			filteredDataProvider.setFilter(criteria.region(currentUser.getUser().getRegion()));
			criteria.region(currentUser.getUser().getRegion());
			provinceFilter.setEnabled(false);
			refreshGridData();
		}

		layout.add(provinceFilter);

		searchField.addValueChangeListener(e -> {
			criteria.nameEpidLike(e.getValue());// nameLike(e.getValue());
			resetFilters.setVisible(true);
			refreshGridData();
		});

		regionFilter.addValueChangeListener(e -> {
			if (regionFilter.getValue() != null) {
				AreaReferenceDto area = e.getValue();
				criteria.area(area);
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {					
					provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(e.getValue().getUuid()));
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {				
					provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByAreaDari(e.getValue().getUuid()));
				} else {
					provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid()));
				}	
				refreshGridData();
			} else {
				criteria.area(null);
				refreshGridData();
			}
			resetFilters.setVisible(true);

		});

		provinceFilter.addValueChangeListener(e -> {
			if (regionFilter.getValue() != null) {
				RegionReferenceDto province = e.getValue();
				criteria.region(province);
				refreshGridData();
			} else {
				criteria.region(null);
				refreshGridData();
			}

		});

		riskFilter.setClearButtonVisible(true);
		riskFilter.setItems("Low Risk (LR)", "Medium Risk (MR)", "High Risk (HR)");
		riskFilter.getStyle().set("width", "145px !important");

		riskFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.risk(e.getValue().toString());
//				filteredDataProvider.setFilter(criteria.risk(e.getValue().toString()));
				refreshGridData();
			} else {
				criteria.risk(null);
				refreshGridData();
			}
			refreshGridData();

		});

		relevanceStatusFilter.setItems(EntityRelevanceStatus.values());
		relevanceStatusFilter.getStyle().set("width", "145px !important");
		relevanceStatusFilter.setClearButtonVisible(true);
		relevanceStatusFilter.setItemLabelGenerator(status -> {
			if (status == EntityRelevanceStatus.ARCHIVED) {
				return I18nProperties.getCaption(Captions.archived);
			} else if (status == EntityRelevanceStatus.ACTIVE) {
				return I18nProperties.getCaption(Captions.active);
			} else if (status == EntityRelevanceStatus.ALL) {
				return I18nProperties.getCaption(Captions.all);

			}
			// Handle other enum values if needed
			return status.toString();
		});
		relevanceStatusFilter.addValueChangeListener(e -> {
			if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ACTIVE)) {
				subMenu.removeAll();
				subMenu.addItem(I18nProperties.getCaption(Captions.archive), event -> handleArchiveDearchiveAction());
			} else if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ARCHIVED)) {
				subMenu.removeAll();
				subMenu.addItem(I18nProperties.getCaption(Captions.actionDearchive),
						event -> handleArchiveDearchiveAction());

			} else {
				subMenu.removeAll();
				subMenu.addItem(I18nProperties.getString(Strings.selectActiveArchivedRelevance));
			}
			criteria.relevanceStatus(e.getValue());
//			filteredDataProvider.setFilter(criteria.relevanceStatus((EntityRelevanceStatus) e.getValue()));
			refreshGridData();
			if (relevanceStatusFilter.getValue() == null) {
				criteria.relevanceStatus(null);
				refreshGridData();
			}
		});
		layout.add(riskFilter);
		layout.add(relevanceStatusFilter);
		relevancelayout.add(countRowItems);

		resetFilters.addClassName("resetButton");
//		resetFilters.setVisible(false);
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
			if (!riskFilter.isEmpty()) {
				refreshGridData();
				riskFilter.clear();
			}
			if (!relevanceStatusFilter.isEmpty()) {
				refreshGridData();
				relevanceStatusFilter.clear();
			}
			refreshGridData();
			updateRowCount();

		});
		layout.add(resetFilters);

		Button addNew = new Button(I18nProperties.getCaption(Captions.addNewDistrict));
		addNew.getElement().getStyle().set("white-space", "normal");
		addNew.getStyle().set("color", "white");
		addNew.getStyle().set("background", "#0D6938");
		addNew.addClickListener(event -> {
			createOrEditDistrict(districtIndexDto);
		});

		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_CREATE)) {
			layout.add(addNew);
		}

		Button importDistrict = new Button(I18nProperties.getCaption(Captions.actionImport));
		importDistrict.setIcon(new Icon(VaadinIcon.DOWNLOAD));
		importDistrict.addClickListener(e -> {
			ImportDistrictDataDialog dialog = new ImportDistrictDataDialog();
			dialog.open();
		});

		Button exportDistrict = new Button(I18nProperties.getCaption(Captions.export));
		exportDistrict.setIcon(new Icon(VaadinIcon.UPLOAD));
		exportDistrict.addClickListener(e -> {
			anchor.getElement().setAttribute("download", true);
			anchor.getElement().callJsFunction("click");

		});
		anchor.getStyle().set("display", "none");
		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EXPORT)) {
			layout.add(importDistrict, exportDistrict, anchor);
		}
		layout.setWidth("88%");

		layout.addClassName("pl-3");
		layout.addClassName("row");
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

		return vlayout;
	}

	private void handleArchiveDearchiveAction() {

		archiveDearchiveAllSelectedItems(grid.getSelectedItems());
		Notification.show(I18nProperties.getString(Strings.deleteActionSelected));
	}

	public void archiveDearchiveAllSelectedItems(Collection<DistrictIndexDto> selectedRows) {
		archiveDearchiveConfirmation = new ConfirmDialog();

		if (selectedRows.size() == 0) {

			archiveDearchiveConfirmation.setRejectable(true);
			archiveDearchiveConfirmation.addRejectListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.setConfirmText("Ok");

			archiveDearchiveConfirmation.setHeader(I18nProperties.getCaption(Captions.errorArchiving));
			archiveDearchiveConfirmation.setText(I18nProperties.getString(Strings.youHaveNotSeleceted));

			archiveDearchiveConfirmation.open();
		} else {
			archiveDearchiveConfirmation.setCancelable(true);
			archiveDearchiveConfirmation.setRejectable(true);
			archiveDearchiveConfirmation.setRejectText("No");
			archiveDearchiveConfirmation.setConfirmText("Yes");
			archiveDearchiveConfirmation.addCancelListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.addRejectListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.open();

			for (DistrictIndexDto selectedRow : (Collection<DistrictIndexDto>) selectedRows) {

				dto = new DistrictDto();
				String regionUUid = selectedRow.getUuid();
				dto = FacadeProvider.getDistrictFacade().getByUuid(regionUUid);
				boolean archive = dto.isArchived();

				System.out.println(archive + " archived or not " + regionUUid + "selected region  uuid");
				if (!archive) {
					archiveDearchiveConfirmation
							.setHeader(I18nProperties.getCaption(Captions.archiveSelectedDistricts));
					archiveDearchiveConfirmation
							.setText(I18nProperties.getString(Strings.areYouSureYouWantToArchiveDistrict));
					archiveDearchiveConfirmation.addConfirmListener(e -> {
						FacadeProvider.getDistrictFacade().archive(selectedRow.getUuid());
//						if (leaveBulkEdit.isVisible()) {
//							leaveBulkEdit.setVisible(false);
//							enterBulkEdit.setVisible(true);
//							grid.setSelectionMode(Grid.SelectionMode.SINGLE);
//							dropdownBulkOperations.setVisible(false);
//						}
						refreshGridData();
					});
//					Notification.show("Archiving Selected Rows ");
				} else {
					archiveDearchiveConfirmation
							.setHeader(I18nProperties.getCaption(Captions.dearchiveSelectedDistricts));
					archiveDearchiveConfirmation
							.setText(I18nProperties.getString(Strings.areYouSureYouWantToDearchiveSelectedDistricts));
					archiveDearchiveConfirmation.addConfirmListener(e -> {
						FacadeProvider.getDistrictFacade().dearchive(selectedRow.getUuid());
//						if (leaveBulkEdit.isVisible()) {
//							leaveBulkEdit.setVisible(false);
//							enterBulkEdit.setVisible(true);
//							grid.setSelectionMode(Grid.SelectionMode.SINGLE);
//							dropdownBulkOperations.setVisible(false);
//						}
						refreshGridData();
					});
//					Notification.show("De- Archiving Selected Rows ");
				}
			}

		}
	}

	public void clearFilters() {

	}

	public boolean createOrEditDistrict(DistrictIndexDto districtIndexDto) {
		Dialog dialog = new Dialog();
		FormLayout fmr = new FormLayout();

		TextField nameField = new TextField(I18nProperties.getCaption(Captions.name));

		TextField dCodeField = new TextField(I18nProperties.getCaption(Captions.District_externalID));
		ComboBox<RegionReferenceDto> provinceOfDistrict = new ComboBox<>(I18nProperties.getCaption(Captions.region));
		provinceOfDistrict.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		ComboBox<String> risk = new ComboBox<>(I18nProperties.getCaption(Captions.risk));
		risk.setItems("Low Risk (LW)", "Medium Risk (MD)", "High Risk (HR)");

		if (districtIndexDto != null) {
			nameField.setValue(districtIndexDto.getName());
			dCodeField.setValue(districtIndexDto.getExternalId().toString());
//			provinceOfDistrict.setItems(districtIndexDto.getRegion());
			provinceOfDistrict.setValue(districtIndexDto.getRegion());
//		provinceOfDistrict.isReadOnly();
			provinceOfDistrict.setEnabled(true);
		}

		// this can generate null

		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));
		Button discardButton = new Button(I18nProperties.getCaption(Captions.actionDiscard), e -> dialog.close());
		saveButton.getStyle().set("margin-right", "10px");
		Button archiveButton = new Button();

		if (districtIndexDto != null) {

			dto = new DistrictDto();
			String regionUUid = districtIndexDto.getUuid();
			dto = FacadeProvider.getDistrictFacade().getByUuid(regionUUid);
			boolean isArchivedx = dto.isArchived();
			archiveButton.setText(isArchivedx ? "De-Archive" : "Archive");

			Collection<?> selectedRows;
			selectedRows = grid.getSelectedItems();
			Set<String> selectedRowsUuids = selectedRows.stream().map(row -> ((HasUuid) row).getUuid())
					.collect(Collectors.toSet());
			System.out.println(selectedRowsUuids + " selected row infracsucfhshfvshfhjvs");
			archiveButton.addClickListener(archiveEvent -> {

				if (districtIndexDto != null) {

					archiveDearchiveConfirmation = new ConfirmDialog();
					archiveDearchiveConfirmation.setCancelable(true);
					archiveDearchiveConfirmation.addCancelListener(e -> dialog.close());
					archiveDearchiveConfirmation.setRejectable(true);
					archiveDearchiveConfirmation.setRejectText("No");
					archiveDearchiveConfirmation.addRejectListener(e -> dialog.close());
					archiveDearchiveConfirmation.setConfirmText("Yes");
					archiveDearchiveConfirmation.open();
					uuidsz = dto.getUuid();

					System.out.println(uuidsz + "areeeeeeeeeeeeeeeeeaaaaaaaaaaaaaaa by uuid");
					boolean isArchived = dto.isArchived();
					if (uuidsz != null) {
						if (isArchived == true) {

							archiveDearchiveConfirmation
									.setHeader(I18nProperties.getCaption(Captions.dearchiveDistrict));
							archiveDearchiveConfirmation
									.setText(I18nProperties.getString(Strings.areYouSureWantToDearchiveDistrict));

							archiveDearchiveConfirmation.addConfirmListener(e -> {
								FacadeProvider.getDistrictFacade().dearchive(uuidsz);
								dialog.close();
								refreshGridData();
							});
//							Notification.show("Dearchiving Area");

						} else {
							archiveDearchiveConfirmation.setHeader(I18nProperties.getCaption(Captions.archiveDistrict));
							archiveDearchiveConfirmation
									.setText(I18nProperties.getString(Strings.areYouSureWantARchiveDistrict));

							archiveDearchiveConfirmation.addConfirmListener(e -> {
								FacadeProvider.getDistrictFacade().archive(uuidsz);
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
			String code = dCodeField.getValue();

			String uuids = "";
			if (districtIndexDto != null) {
				uuids = districtIndexDto.getUuid();
			}
			if (name != null && code != null) {

				DistrictDto dce = FacadeProvider.getDistrictFacade().getDistrictByUuid(uuids);
				if (dce != null) {
					dce.setName(name);
					long rcodeValue = Long.parseLong(code);
					dce.setExternalId(rcodeValue);
					dce.setRegion(provinceOfDistrict.getValue());
					if (dce.getRisk() != null) {
						dce.setRisk(risk.getValue());
					}
					FacadeProvider.getDistrictFacade().save(dce, true);
					Notification.show(I18nProperties.getString(Strings.saved) + name + " " + code);
					dialog.close();
					refreshGridData();
				} else {
					DistrictDto dcex = new DistrictDto();

					dcex.setName(name);
					long rcodeValue = Long.parseLong(code);
					dcex.setExternalId(rcodeValue);
					dcex.setRegion(provinceOfDistrict.getValue());
					dcex.setRisk(risk.getValue());

					try {
						FacadeProvider.getDistrictFacade().save(dcex, true);
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

						Paragraph text = new Paragraph(
								"An unexpected error occurred. Please contact your supervisor or administrator and inform them about it.");

						HorizontalLayout layout = new HorizontalLayout(text, closeButton);
						layout.setAlignItems(Alignment.CENTER);

						notification.add(layout);
						notification.open();
//					        Notification.show("An error occurred while saving: " + e.getMessage());
					}

				}
			} else {
				Notification.show(I18nProperties.getCaption(Captions.notValidValue) + name + " " + code);
			}

		});

//		dialog.setHeaderTitle("Edit " + districtIndexDto.getName());
		if (districtIndexDto == null) {
			dialog.setHeaderTitle(I18nProperties.getCaption(Captions.addNewDistrict));
			dialog.getFooter().add(discardButton, saveButton);
		} else {
			dialog.setHeaderTitle(I18nProperties.getCaption(Captions.edit) + districtIndexDto.getName());
			dialog.getFooter().add(archiveButton, discardButton, saveButton);

		}
		fmr.add(nameField, dCodeField, provinceOfDistrict, risk);
		dialog.add(fmr);

//       getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
//		.set("display", "flex").set("align-items", "center").set("justify-content", "center");

		dialog.open();

		return true;
	}

	private void updateRowCount() {
		int numberOfRows = filteredDataProvider.size(new Query<>());
		String newText = I18nProperties.getCaption(Captions.rows) + numberOfRows;

		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}

	private void refreshGridData() {
		ListDataProvider<DistrictIndexDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getDistrictFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);
		itemCount = dataProvider.getItems().size();
		String newText = I18nProperties.getCaption(Captions.rows) + itemCount;
		countRowItems.setText(newText);
		countRowItems.setId("rowCount");

//		dataView = grid.setItems(dataProvider);
	}

}