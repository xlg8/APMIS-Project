package com.cinoteck.application.views.configurations;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cinoteck.application.UserProvider;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaCriteria;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionCriteria;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserRight;

@PageTitle("APMIS-Province")
@Route(value = "province", layout = ConfigurationsView.class)
public class ProvinceView extends VerticalLayout implements RouterLayout {

	private static final long serialVersionUID = 8159316049907141477L;
	Grid<RegionIndexDto> grid = new Grid<>(RegionIndexDto.class, false);
//	List<RegionIndexDto> regions = FacadeProvider.getRegionFacade().getAllRegions();
	GridListDataView<RegionIndexDto> dataView;
	RegionIndexDto regionDto;
	RegionDto dto;

	private RegionCriteria criteria;
//	ProvinceDataProvider provinceDataProvider = new ProvinceDataProvider();	
//	ConfigurableFilterDataProvider<RegionIndexDto, Void, RegionCriteria> filteredDataProvider;

	final static TextField regionField = new TextField(I18nProperties.getCaption(Captions.area));
	final static TextField rcodeField = new TextField(I18nProperties.getCaption(Captions.Area_externalId));
	final static ComboBox<AreaReferenceDto> area = new ComboBox();
	Binder<RegionIndexDto> binder = new BeanValidationBinder<>(RegionIndexDto.class);
	private Button saveButton;
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	UserProvider currentUser = new UserProvider();
	Paragraph countRowItems;

	UserProvider userProvider = new UserProvider();
	Button enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
	Button leaveBulkEdit = new Button(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
	MenuBar dropdownBulkOperations = new MenuBar();
	SubMenu subMenu;
	ComboBox<EntityRelevanceStatus> relevanceStatusFilter = new ComboBox<>();
	ConfirmDialog archiveDearchiveConfirmation;
	String uuidsz = "";
	ListDataProvider<RegionIndexDto> dataProvider;
	int itemCount;

	public ProvinceView() {
		setSpacing(false);
		setHeightFull();
		setSizeFull();
		configureProvinceFilters();
		provinceGrid(criteria);
	}

	private void provinceGrid(RegionCriteria criteria) {
		this.criteria = criteria;
		setSpacing(false);
		setMargin(false);
		setSizeFull();

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			grid.addColumn(RegionIndexDto::getPs_af).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
			grid.addColumn(RegionIndexDto::getAreaexternalId)
					.setHeader(I18nProperties.getCaption(Captions.Area_externalId)).setResizable(true).setSortable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
			grid.addColumn(RegionIndexDto::getPs_af).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.region));
			grid.addColumn(RegionIndexDto::getExternalId)
					.setHeader(I18nProperties.getCaption(Captions.Region_externalID)).setSortable(true)
					.setResizable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Region_externalID));
			
			grid.setVisible(true);
			grid.setAllRowsVisible(true);
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {

			grid.addColumn(RegionIndexDto::getFa_af).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
			grid.addColumn(RegionIndexDto::getAreaexternalId)
					.setHeader(I18nProperties.getCaption(Captions.Area_externalId)).setResizable(true).setSortable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
			grid.addColumn(RegionIndexDto::getFa_af).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.region));
			grid.addColumn(RegionIndexDto::getExternalId)
					.setHeader(I18nProperties.getCaption(Captions.Region_externalID)).setSortable(true)
					.setResizable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Region_externalID));
			
			grid.setVisible(true);
			grid.setAllRowsVisible(true);
		} else {

			grid.addColumn(RegionIndexDto::getArea).setHeader(I18nProperties.getCaption(Captions.area))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
			grid.addColumn(RegionIndexDto::getAreaexternalId)
					.setHeader(I18nProperties.getCaption(Captions.Area_externalId)).setResizable(true).setSortable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
			grid.addColumn(RegionIndexDto::getName).setHeader(I18nProperties.getCaption(Captions.region))
					.setSortable(true).setResizable(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.region));
			grid.addColumn(RegionIndexDto::getExternalId)
					.setHeader(I18nProperties.getCaption(Captions.Region_externalID)).setSortable(true)
					.setResizable(true).setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Region_externalID));
			
			grid.setVisible(true);
			grid.setAllRowsVisible(true);
		}

		if (criteria == null) {
			criteria = new RegionCriteria();
			criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		}
		dataProvider = DataProvider
				.fromStream(FacadeProvider.getRegionFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);

		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EDIT)) {

			grid.asSingleSelect().addValueChangeListener(event -> {
				if (event.getValue() != null) {
					createOrEditProvince(event.getValue());
				}
			});
		}

		add(grid);

		GridExporter<RegionIndexDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.User));
		exporter.setFileName(
				"APMIS_Provinces" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("exportProvince");
		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());
	}

	public void setArea(RegionIndexDto regionDto) {
		regionField.setValue(regionDto.getName());
		rcodeField.setValue(String.valueOf(regionDto.getExternalId()));
		area.setValue(regionDto.getArea());
		binder.setBean(regionDto);
	}

	public void configureProvinceFilters() {
		if (criteria == null) {
			criteria = new RegionCriteria();
		}

		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
			leaveBulkEdit = new Button();
			dropdownBulkOperations = new MenuBar();
			MenuItem bulkActionsItem = dropdownBulkOperations.addItem(I18nProperties.getCaption(Captions.bulkActions));
			subMenu = bulkActionsItem.getSubMenu();
			subMenu.addItem("Archive", e -> handleArchiveDearchiveAction());

		}
//		System.out.println(criteria + "criteria from inde xxxx ");

		criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		dataProvider = DataProvider
				.fromStream(FacadeProvider.getRegionFacade().getIndexList(criteria, null, null, null).stream());

		itemCount = dataProvider.getItems().size();

//		itemCount = dataProvider.getItems().size();
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
		relevancelayout.setWidth("10%");

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

		TextField searchField = new TextField();
		searchField.setWidth("10%");
		searchField.addClassName("filterBar");
		searchField.setPlaceholder(I18nProperties.getCaption(Captions.actionSearch));
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setClearButtonVisible(true);
		searchField.addValueChangeListener(e -> {
			criteria.nameEpidLike(e.getValue());
			refreshGridData();
		});

		layout.add(searchField);

		ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>();
		regionFilter.setLabel(I18nProperties.getCaption(Captions.area));
		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.areaAllAreas));
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		regionFilter.getStyle().set("width", "145px !important");

		regionFilter.setClearButtonVisible(true);

		if (currentUser.getUser().getArea() != null) {
			regionFilter.setValue(currentUser.getUser().getArea());
//			dataView.addFilter(f -> f.getArea().getCaption().equalsIgnoreCase(regionFilter.getValue().getCaption()));
			regionFilter.setEnabled(false);
		}

		regionFilter.addValueChangeListener(e -> {
			criteria.area(regionFilter.getValue());
			refreshGridData();

		});

		layout.add(regionFilter);

		relevanceStatusFilter = new ComboBox<EntityRelevanceStatus>();
		relevanceStatusFilter.setLabel(I18nProperties.getCaption(Captions.relevanceStatus));
		relevanceStatusFilter.setItems((EntityRelevanceStatus[]) EntityRelevanceStatus.values());
		relevanceStatusFilter.getStyle().set("width", "145px !important");
		relevanceStatusFilter.setClearButtonVisible(true);
		relevanceStatusFilter.addValueChangeListener(e -> {
			System.out.println(criteria.relevanceStatus(e.getValue()) + "criteria relevance " + criteria + "jjjjjjj"
					+ e.getValue());
			criteria.relevanceStatus(e.getValue()); // Set the selected relevance status in the criteria object
			refreshGridData();
			if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ACTIVE)) {
				subMenu.removeAll();
//				criteria.relevanceStatus(e.getValue()); // Set the selected relevance status in the criteria object
//				refreshGridData();
				subMenu.addItem(I18nProperties.getCaption(Captions.archive), event -> handleArchiveDearchiveAction());
			} else if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ARCHIVED)) {
				subMenu.removeAll();
				subMenu.addItem(I18nProperties.getCaption(Captions.actionDearchive),
						event -> handleArchiveDearchiveAction());
//				criteria.relevanceStatus(e.getValue()); // Set the selected relevance status in the criteria object
//				refreshGridData();

			} else if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ALL)) {
				subMenu.removeAll();
				subMenu.addItem(I18nProperties.getString(Strings.selectActiveArchivedRelevance));
//				criteria.relevanceStatus(e.getValue()); // Set the selected relevance status in the criteria object
//				refreshGridData();
//				Notification.show("Please Select Either Active or Archived Unit to carry out a bulk action ");
			} else {
				criteria.relevanceStatus(e.getValue()); // Set the selected relevance status in the criteria object
				refreshGridData();
			}

		});
		layout.add(relevanceStatusFilter);

		Button resetButton = new Button(I18nProperties.getCaption(Captions.resetFilters));

		resetButton.addClassName("resetButton");
		resetButton.addClickListener(e -> {
			if (!searchField.isEmpty()) {
				searchField.clear();
				relevanceStatusFilter.clear();
			} else {

			}
			if (regionFilter.getValue() != null) {
				refreshGridData();
				regionFilter.clear();
				relevanceStatusFilter.clear();
			}
			if (relevanceStatusFilter.getValue() != null) {
				refreshGridData();
				relevanceStatusFilter.clear();
			}
		});
		layout.add(resetButton);

		Button addNew = new Button(I18nProperties.getCaption(Captions.addNewProvince));
		addNew.getElement().getStyle().set("white-space", "normal");
		addNew.getStyle().set("color", "white");
		addNew.getStyle().set("background", "#0D6938");
		addNew.addClickListener(event -> {
			createOrEditProvince(regionDto);
		});

		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_CREATE)) {
			layout.add(addNew);
		}

		Button exportProvince = new Button(I18nProperties.getCaption(Captions.export));
		exportProvince.setIcon(new Icon(VaadinIcon.UPLOAD));
		exportProvince.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");

		});

		Button importProvince = new Button(I18nProperties.getCaption(Captions.actionImport));
		importProvince.setIcon(new Icon(VaadinIcon.DOWNLOAD));
		importProvince.addClickListener(e -> {
			ImportProvinceDataDialog dialog = new ImportProvinceDataDialog();
			dialog.open();

		});
		anchor.getStyle().set("display", "none");
		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EXPORT)) {
			layout.add(importProvince, exportProvince, anchor);
		}
		layout.setWidth("80%");
		layout.addClassName("pl-3");
		layout.addClassName("row");
		relevancelayout.add(countRowItems);
		vlayout.setWidthFull();
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
	}

	private void handleArchiveDearchiveAction() {

		archiveDearchiveAllSelectedItems(grid.getSelectedItems());
		Notification.show(I18nProperties.getString(Strings.deleteActionSelected));
	}

	public void archiveDearchiveAllSelectedItems(Collection<RegionIndexDto> selectedRows) {
		archiveDearchiveConfirmation = new ConfirmDialog();
		if (selectedRows.size() == 0) {

			archiveDearchiveConfirmation.setCancelable(true);
			archiveDearchiveConfirmation.addCancelListener(e -> archiveDearchiveConfirmation.close());
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

			for (RegionIndexDto selectedRow : (Collection<RegionIndexDto>) selectedRows) {

				dto = new RegionDto();
				String regionUUid = selectedRow.getUuid();
				dto = FacadeProvider.getRegionFacade().getByUuid(regionUUid);
				boolean archive = dto.isArchived();

				System.out.println(archive + " archived or not " + regionUUid + "selected region  uuid");
				if (!archive) {
					archiveDearchiveConfirmation.setHeader(I18nProperties.getString(Strings.archiveSelectedProvinces));
					archiveDearchiveConfirmation
							.setText(I18nProperties.getString(Strings.areYouSureYouWantToArchiveProvinces));
					archiveDearchiveConfirmation.addConfirmListener(e -> {
						FacadeProvider.getRegionFacade().archive(selectedRow.getUuid());
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
							.setHeader(I18nProperties.getString(Strings.dearchiveSelecetdProvinces));
					archiveDearchiveConfirmation
							.setText(I18nProperties.getString(Strings.areYouSureYouWantToDearchiveSelectedProvinces));
					archiveDearchiveConfirmation.addConfirmListener(e -> {
						FacadeProvider.getRegionFacade().dearchive(selectedRow.getUuid());
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

	private void updateRowCount() {
		int numberOfRows = dataView.getItemCount();// .size(new Query<>());
		String newText = I18nProperties.getCaption(Captions.rows) + numberOfRows;

		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}

	private void reloadGrid() {
		dataView.refreshAll();
	}

	public boolean createOrEditProvince(RegionIndexDto regionDto) {
		Dialog dialog = new Dialog();
		FormLayout fmr = new FormLayout();
		TextField nameField = new TextField(I18nProperties.getCaption(Captions.name));
		TextField pCodeField = new TextField(I18nProperties.getCaption(Captions.Region_externalID));
		ComboBox<AreaReferenceDto> areaField = new ComboBox(I18nProperties.getCaption(Captions.area));
		areaField.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());

		if (regionDto != null) {
			nameField.setValue(regionDto.getName());
			pCodeField.setValue(regionDto.getExternalId().toString());
//			areaField.setItems(regionDto.getArea());
			areaField.setValue(regionDto.getArea());
			areaField.setEnabled(true);
		}

		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));
		Button discardButton = new Button(I18nProperties.getCaption(Captions.actionDiscard), e -> dialog.close());
		Button archiveButton = new Button();
		saveButton.getStyle().set("margin-right", "10px");

		if (regionDto != null) {

			dto = new RegionDto();
			String regionUUid = regionDto.getUuid();
			dto = FacadeProvider.getRegionFacade().getByUuid(regionUUid);
			boolean isArchivedx = dto.isArchived();
			archiveButton.setText(isArchivedx ? "De-Archive" : "Archive");
			archiveButton.addClickListener(archiveEvent -> {
				archiveDearchiveConfirmation = new ConfirmDialog();

				archiveDearchiveConfirmation.setRejectable(true);
				archiveDearchiveConfirmation.setRejectText("No");
				archiveDearchiveConfirmation.addRejectListener(e -> dialog.close());
				archiveDearchiveConfirmation.setConfirmText("Yes");
				archiveDearchiveConfirmation.open();

				if (regionDto != null) {
					uuidsz = dto.getUuid();

					boolean isArchived = dto.isArchived();
					if (uuidsz != null) {
						if (isArchived == true) {

							archiveDearchiveConfirmation
									.setHeader(I18nProperties.getCaption(Captions.dearchiveProvince));
							archiveDearchiveConfirmation.setText(
									I18nProperties.getString(Strings.areYouSureYouWantToDearchiveSelectedProvince));

							archiveDearchiveConfirmation.addConfirmListener(e -> {
								FacadeProvider.getRegionFacade().dearchive(uuidsz);
								dialog.close();
								refreshGridData();
							});
//							Notification.show("Dearchiving Area");

						} else {
							archiveDearchiveConfirmation.setHeader(I18nProperties.getCaption(Captions.archiveProvince));
							archiveDearchiveConfirmation
									.setText(I18nProperties.getString(Strings.areSureYouWantToArchiveProvince));

							archiveDearchiveConfirmation.addConfirmListener(e -> {
								FacadeProvider.getRegionFacade().archive(uuidsz);
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
			String code = pCodeField.getValue();

			String uuids = "";

			if (regionDto != null) {
				uuids = regionDto.getUuid();
			}
			if ((name != null && name != "")
					&& (!pCodeField.getValue().isBlank() || !pCodeField.getValue().isEmpty())) {
				RegionDto dce = FacadeProvider.getRegionFacade().getByUuid(uuids);
				if (dce != null) {
					dce.setName(name);
					long rcodeValue = Long.parseLong(code);
					dce.setExternalId(rcodeValue);
					dce.setArea(areaField.getValue());

					FacadeProvider.getRegionFacade().save(dce, true);
					Notification.show(I18nProperties.getString(Strings.saved) + name + " " + code);
					dialog.close();
					refreshGridData();
				} else {
					RegionDto dcex = new RegionDto();
					dcex.setName(name);
					long rcodeValue = Long.parseLong(code);
					dcex.setExternalId(rcodeValue);
					dcex.setArea(areaField.getValue());

					try {
						FacadeProvider.getRegionFacade().save(dcex, true);
						Notification.show(I18nProperties.getString(Strings.savedNewRegion) + name + " " + code);
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

						Paragraph text = new Paragraph("Province exist. Please create a unique province.");

						HorizontalLayout layout = new HorizontalLayout(text, closeButton);
						layout.setAlignItems(Alignment.CENTER);

						notification.add(layout);
						notification.open();
//					        Notification.show("An error occurred while saving: " + e.getMessage());
					}

				}
			} else if ((nameField.getValue().isBlank() || nameField.getValue().isEmpty())) {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.setPosition(Position.MIDDLE);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph("Province Name Cannot be left blank.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
			} else if ((pCodeField.getValue().isBlank() || pCodeField.getValue().isEmpty())) {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.setPosition(Position.MIDDLE);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph("Province Code Cannot be left blank.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
			} else if (areaField.getValue() == null) {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.setPosition(Position.MIDDLE);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph("Please Select A Valid Region.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
			} else {
				Notification.show(I18nProperties.getCaption(Captions.notValidValue) + name + " " + code);
			}

		});

		HorizontalLayout fiels = new HorizontalLayout(nameField, pCodeField, areaField);

		fmr.add(fiels);

		if (regionDto == null) {
			dialog.setHeaderTitle(I18nProperties.getCaption(Captions.addNewProvince));
			dialog.getFooter().add(discardButton, saveButton);
		} else {
			dialog.setHeaderTitle(I18nProperties.getCaption(Captions.edit) + regionDto.getName());
			dialog.getFooter().add(archiveButton, discardButton, saveButton);
		}
		dialog.add(fmr);

//        getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
//		.set("display", "flex").set("align-items", "center").set("justify-content", "center");

		dialog.open();

		return true;
	}

	private void refreshGridData() {
		ListDataProvider<RegionIndexDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getRegionFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);
		itemCount = dataProvider.getItems().size();
		String newText = I18nProperties.getCaption(Captions.rows) + itemCount;
		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}
}