package com.cinoteck.application.views.configurations;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.utils.gridexporter.GridExporter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaWithExpReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.infrastructure.area.AreaCriteria;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.user.UserRight;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ejb.TransactionRolledbackLocalException;

//import org.vaadin.olli.FileDownloadWrapper;

@PageTitle("APMIS-Regions")
@Route(value = "regions", layout = ConfigurationsView.class)
public class RegionView extends VerticalLayout implements RouterLayout {

	private static final long serialVersionUID = 7091198805223773269L;

	private AreaCriteria criteria = new AreaCriteria();

	private GridListDataView<AreaDto> dataView;
	final static TextField regionField = new TextField(I18nProperties.getCaption(Captions.area));
	final static TextField rcodeField = new TextField(I18nProperties.getCaption(Captions.Area_externalId));
	Binder<AreaDto> binder = new BeanValidationBinder<>(AreaDto.class);
	Grid<AreaDto> grid = new Grid<>(AreaDto.class, false);;
	private Button saveButton;
	private Button archiveDearchive;

	private Button createNewArea;
	private Button importArea;

	ComboBox<EntityRelevanceStatus> relevanceStatusFilter = new ComboBox<>();
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));

	HorizontalLayout layout = new HorizontalLayout();
	HorizontalLayout relevancelayout = new HorizontalLayout();
	AreaDto areaDto;
	Anchor link;
	TextField searchField;
	Button clear;
	UserProvider userProvider = new UserProvider();
	Button enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
	Button leaveBulkEdit = new Button(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
	Paragraph countRowItems;
	Button exportRegion = new Button(I18nProperties.getCaption(Captions.export));
	List<AreaDto> data;
	MenuBar dropdownBulkOperations = new MenuBar();
	SubMenu subMenu;
	ListDataProvider<AreaDto> dataProvider;
	int itemCount;// = dataProvider.getItems().size();

	String uuidsz = "";
	LocalDate localDate = LocalDate.now();
	Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());


	ConfirmDialog archiveDearchiveConfirmation;

	public RegionView() {
		setSpacing(false);
		setHeightFull();
		setSizeFull();
		addRegionFilter();
		regionGrid(criteria);

	}

	private void regionGrid(AreaCriteria criteria) {

		this.criteria = criteria;
		setSpacing(false);

		setMargin(false);
		setSizeFull();

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		ComponentRenderer<Span, AreaDto> areaExternalIdRenderer = new ComponentRenderer<>(input -> {
			NumberFormat arabicFormat = NumberFormat.getInstance();
			if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
				arabicFormat = NumberFormat.getInstance(new Locale("ps"));
			} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
				arabicFormat = NumberFormat.getInstance(new Locale("fa"));
			}
			String value = String.valueOf(arabicFormat.format(input.getExternalId()));
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});
		
		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {

			grid.addColumn(AreaDto::getPs_af).setHeader(I18nProperties.getCaption(Captions.area)).setSortable(true)
					.setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
			grid.addColumn(areaExternalIdRenderer).setHeader(I18nProperties.getCaption(Captions.Area_externalId))
					.setResizable(true).setSortable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {

			grid.addColumn(AreaDto::getFa_af).setHeader(I18nProperties.getCaption(Captions.area)).setSortable(true)
					.setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
			grid.addColumn(areaExternalIdRenderer).setHeader(I18nProperties.getCaption(Captions.Area_externalId))
					.setResizable(true).setSortable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
		} else {
			grid.addColumn(AreaDto::getName).setHeader(I18nProperties.getCaption(Captions.area)).setSortable(true)
					.setResizable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.area));
			grid.addColumn(AreaDto::getExternalId).setHeader(I18nProperties.getCaption(Captions.Area_externalId))
					.setResizable(true).setSortable(true).setAutoWidth(true)
					.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.Area_externalId));
		}
		
		grid.addColumn(AreaDto::provideActiveStatus).setHeader(I18nProperties.getCaption(Captions.relevanceStatus))
		.setResizable(true).setSortable(true).setAutoWidth(true)
		.setTooltipGenerator(e -> I18nProperties.getCaption(Captions.relevanceStatus));

		// grid.setItemDetailsRenderer(createAreaEditFormRenderer());
		grid.setVisible(true);
		grid.setAllRowsVisible(true);
//		List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();

//		List<AreaDto> regions = FacadeProvider.getAreaFacade().getIndexList(criteria, null, null, null);
//		this.dataView = grid.setItems(regions);
//		if (criteria.getRelevanceStatus() == null) {
		criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
//		}
		dataProvider = DataProvider
				.fromStream(FacadeProvider.getAreaFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);

		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EDIT))

		{

			grid.asSingleSelect().addValueChangeListener(event -> {
				if (event.getValue() != null) {
					createOrEditArea(event.getValue());
					System.out.println(
							event.getValue().getUuid_() + "Area from grid is not nullll " + event.getValue().getUuid());
				}
			});
		}

		add(grid);

		GridExporter<AreaDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.User));
		exporter.setFileName(
				"APMIS_Regions_" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);

		anchor.setClassName("exportJsonGLoss");
		anchor.setId("exportArea");
		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());

	}

	private void refreshGridData() {
		ListDataProvider<AreaDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getAreaFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);
		itemCount = dataProvider.getItems().size();
		String newText = I18nProperties.getCaption(Captions.rows) + itemCount;
		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}

	private void addRegionFilter() {

		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
			leaveBulkEdit = new Button();
			dropdownBulkOperations = new MenuBar();
		}

		setMargin(true);
		layout.setPadding(false);
		layout.setVisible(true);
		layout.setAlignItems(Alignment.END);

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

		layout.setPadding(false);
		layout.setVisible(true);
		layout.setAlignItems(Alignment.END);
		layout.setWidth("80%");

		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.getStyle().set("color", "#0D6938 !important");

		searchField = new TextField();

		searchField.setPlaceholder(I18nProperties.getCaption(Captions.actionSearch));
		searchField.setPrefixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setWidth("10%");
		searchField.setClearButtonVisible(true);
		searchField.addClassName("filterBar");
		searchField.addValueChangeListener(e -> dataView.addFilter(search -> {
			String searchTerm = searchField.getValue().trim();

			if (searchTerm.isEmpty())
				return true;

			boolean matchesDistrictName = String.valueOf(search.getName()).toLowerCase()
					.contains(searchTerm.toLowerCase());

			boolean matchesDistrictNumber = String.valueOf(search.getExternalId()).toLowerCase()
					.contains(searchTerm.toLowerCase());
//			updateRowCount();

			return matchesDistrictName || matchesDistrictNumber;

		}));

		clear = new Button(I18nProperties.getCaption(Captions.clearSearch));
		clear.getStyle().set("color", "white");
		clear.getStyle().set("background", "#0D6938");
		clear.addClickListener(e -> {
			searchField.clear();
//			updateRowCount();

		});

		Button addNew = new Button(I18nProperties.getCaption(Captions.addNewRegion));
		addNew.getElement().getStyle().set("white-space", "normal");
		addNew.getStyle().set("color", "white");
		addNew.getStyle().set("background", "#0D6938");
		addNew.addClickListener(event -> {
			createOrEditArea(areaDto);
		});

		Button importArea = new Button(I18nProperties.getCaption(Captions.actionImport));
		importArea.getStyle().set("color", "white");
		importArea.getStyle().set("background", "#0D6938");
		importArea.setIcon(new Icon(VaadinIcon.DOWNLOAD));
		importArea.addClickListener(event -> {

			ImportAreaDataDialog dialog = new ImportAreaDataDialog();
			dialog.open();

		});

		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			enterBulkEdit = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
			leaveBulkEdit = new Button();
			dropdownBulkOperations = new MenuBar();
			MenuItem bulkActionsItem = dropdownBulkOperations.addItem(I18nProperties.getCaption(Captions.bulkActions));
			subMenu = bulkActionsItem.getSubMenu();
			subMenu.addItem(I18nProperties.getCaption(Captions.archive), e -> handleArchiveDearchiveAction());

		}

		relevanceStatusFilter = new ComboBox<EntityRelevanceStatus>();
		relevanceStatusFilter.setLabel(I18nProperties.getCaption(Captions.relevanceStatus));
		relevanceStatusFilter.setItems((EntityRelevanceStatus[]) EntityRelevanceStatus.values());
		relevanceStatusFilter.setClearButtonVisible(true);
		relevanceStatusFilter.getStyle().set("width", "145px !important");
		relevanceStatusFilter.setPlaceholder("Active");
		relevanceStatusFilter.addValueChangeListener(e -> {
			criteria.relevanceStatus(e.getValue()); // Set the selected relevance status in the criteria object
			refreshGridData();
			if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ACTIVE)) {
				subMenu.removeAll();

				subMenu.addItem(I18nProperties.getCaption(Captions.archive), event -> handleArchiveDearchiveAction());
			} else if (relevanceStatusFilter.getValue().equals(EntityRelevanceStatus.ARCHIVED)) {

				subMenu.removeAll();
				subMenu.addItem(I18nProperties.getCaption(Captions.actionDearchive),
						event -> handleArchiveDearchiveAction());

			} else {
				subMenu.removeAll();
				Notification.show(I18nProperties.getString(Strings.pleaseSelectEitherArchivedUnitToCarryOutBulkAction));
			}

		});
		searchField.addClassName("filter-item");
		relevanceStatusFilter.addClassName("filter-item");
		clear.addClassName("filter-item");
		addNew.addClassName("filter-item");
		anchor.addClassName("filter-item");
		layout.add(searchField);
		layout.add(relevanceStatusFilter);
		layout.add(clear);
		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_CREATE)) {
			layout.add(addNew);
			layout.add(importArea);
		}

		exportRegion.setIcon(new Icon(VaadinIcon.UPLOAD));
		exportRegion.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");
		});

		anchor.getStyle().set("display", "none");

		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EXPORT)) {
			layout.add(exportRegion, anchor);
		}
//		int numberOfRows = (int) FacadeProvider.getAreaFacade().count(criteria);

		criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		dataProvider = DataProvider
				.fromStream(FacadeProvider.getAreaFacade().getIndexList(criteria, null, null, null).stream());

		itemCount = dataProvider.getItems().size();

		countRowItems = new Paragraph(I18nProperties.getCaption(Captions.rows) + itemCount);
		countRowItems.setId("rowCount");

		relevancelayout.add(countRowItems);
		vlayout.setWidth("99%");
		vlayout.add(displayFilters, layout, relevancelayout);
		vlayout.getStyle().set("margin-right", "0.5rem");

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

	public void archiveDearchiveAllSelectedItems(Collection<AreaDto> selectedRows) {
		archiveDearchiveConfirmation = new ConfirmDialog();

		if (selectedRows.size() == 0) {

			archiveDearchiveConfirmation.setCancelable(true);
			archiveDearchiveConfirmation.addCancelListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.setRejectable(true);
			archiveDearchiveConfirmation.addRejectListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.setConfirmText(I18nProperties.getCaption(Captions.actionOkay));

			archiveDearchiveConfirmation.setHeader(I18nProperties.getCaption(Captions.errorArchiving));
			archiveDearchiveConfirmation.setText(I18nProperties.getString(Strings.youHaveNotSeleceted));

			archiveDearchiveConfirmation.open();
//			Notification.show(I18nProperties.getString(Strings.messageNodataSelected));

		} else {
			archiveDearchiveConfirmation.setCancelable(true);
			archiveDearchiveConfirmation.setRejectable(true);
			archiveDearchiveConfirmation.setRejectText(I18nProperties.getCaption(Captions.actionNo));
			archiveDearchiveConfirmation.setConfirmText(I18nProperties.getCaption(Captions.actionYes));
			archiveDearchiveConfirmation.addCancelListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.addRejectListener(e -> archiveDearchiveConfirmation.close());
			archiveDearchiveConfirmation.open();

			for (AreaDto selectedRow : (Collection<AreaDto>) selectedRows) {
				boolean archive = selectedRow.isArchived();
				if (!archive) {
					archiveDearchiveConfirmation.setHeader(I18nProperties.getCaption(Captions.archiveSelectedRegions));
					archiveDearchiveConfirmation
							.setText(I18nProperties.getString(Strings.areYouSureYouWantToArchiveSelecetdRegions));
					archiveDearchiveConfirmation.addConfirmListener(e -> {
						FacadeProvider.getAreaFacade().archive(selectedRow.getUuid());
						
						ConfigurationChangeLogDto configurationChangeLogDto = new ConfigurationChangeLogDto();
						configurationChangeLogDto.setCreatinguser(userProvider.getUser().getUserName());
						configurationChangeLogDto.setAction_unit_type("Region");
						configurationChangeLogDto.setAction_unit_name(selectedRow.getName());
						configurationChangeLogDto.setUnit_code(selectedRow.getExternalId());
						configurationChangeLogDto.setAction_logged("Bulk Archive");
						configurationChangeLogDto.setAction_date(date);
								
						FacadeProvider.getAreaFacade().saveAreaChangeLog(configurationChangeLogDto);

						refreshGridData();
					});

				} else {
					archiveDearchiveConfirmation
							.setHeader(I18nProperties.getCaption(Captions.dearchivedSelectedRegions));
					archiveDearchiveConfirmation
							.setText(I18nProperties.getString(Strings.areYouSureYouWantToDearchiveSelectedRegions));
					archiveDearchiveConfirmation.addConfirmListener(e -> {
						FacadeProvider.getAreaFacade().dearchive(selectedRow.getUuid());
						
						ConfigurationChangeLogDto configurationChangeLogDto = new ConfigurationChangeLogDto();
						configurationChangeLogDto.setCreatinguser(userProvider.getUser().getUserName());
						configurationChangeLogDto.setAction_unit_type("Region");
						configurationChangeLogDto.setAction_unit_name(selectedRow.getName());
						configurationChangeLogDto.setUnit_code(selectedRow.getExternalId());
						configurationChangeLogDto.setAction_logged("Bulk De-Archive");
						configurationChangeLogDto.setAction_date(date);
					
						FacadeProvider.getAreaFacade().saveAreaChangeLog(configurationChangeLogDto);

						refreshGridData();
					});
				}
			}

		}
	}

	public boolean createOrEditArea(AreaDto areaDto) {
		Dialog dialog = new Dialog();
		FormLayout fmr = new FormLayout();

		TextField nameField = new TextField(I18nProperties.getCaption(Captions.name));
		TextField rCodeField = new TextField(I18nProperties.getCaption(Captions.Area_externalId));

		if (areaDto != null) {
			nameField.setValue(areaDto.getName());
			rCodeField.setValue(areaDto.getExternalId().toString());
		}
		// this can generate null

		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));
		Button discardButton = new Button(I18nProperties.getCaption(Captions.actionDiscard), e -> dialog.close());
		Button archiveButton = new Button();
		if (areaDto != null) {
			boolean isArchivedx = areaDto.isArchived();
			archiveButton.setText(isArchivedx ? "De-Archive" : "Archive");
			Collection<?> selectedRows;
			selectedRows = grid.getSelectedItems();
			Set<String> selectedRowsUuids = selectedRows.stream().map(row -> ((HasUuid) row).getUuid())
					.collect(Collectors.toSet());

			archiveButton.addClickListener(archiveEvent -> {

				if (areaDto != null) {
					archiveDearchiveConfirmation = new ConfirmDialog();
					archiveDearchiveConfirmation.setCancelable(true);
					archiveDearchiveConfirmation.addCancelListener(e -> dialog.close());
					archiveDearchiveConfirmation.setRejectable(true);
					archiveDearchiveConfirmation.setRejectText("No");
					archiveDearchiveConfirmation.addRejectListener(e -> dialog.close());
					archiveDearchiveConfirmation.setConfirmText("Yes");
					archiveDearchiveConfirmation.open();

					uuidsz = areaDto.getUuid();
					boolean isArchived = areaDto.isArchived();
					if (uuidsz != null) {

						if (isArchived == true) {

							archiveDearchiveConfirmation.setHeader(I18nProperties.getCaption(Captions.dearchiveRegion));
							archiveDearchiveConfirmation
									.setText(I18nProperties.getString(Strings.areYouSureYouWantDearchiveRegion));

							archiveDearchiveConfirmation.addConfirmListener(e -> {
								FacadeProvider.getAreaFacade().dearchive(uuidsz);
								ConfigurationChangeLogDto configurationChangeLogDto = new ConfigurationChangeLogDto();
								configurationChangeLogDto.setCreatinguser(userProvider.getUser().getUserName());
								configurationChangeLogDto.setAction_unit_type("Region");
								configurationChangeLogDto.setAction_unit_name(areaDto.getName());
								configurationChangeLogDto.setUnit_code(areaDto.getExternalId());
								configurationChangeLogDto.setAction_logged("De-Archive");
								configurationChangeLogDto.setAction_date(date);
					
								FacadeProvider.getAreaFacade().saveAreaChangeLog(configurationChangeLogDto);
								dialog.close();
								refreshGridData();
							});

						} else {

							archiveDearchiveConfirmation.setHeader(I18nProperties.getCaption(Captions.archiveRegion));
							archiveDearchiveConfirmation
									.setText(I18nProperties.getString(Strings.areYouSureYouWantToArchiveRegion));

							archiveDearchiveConfirmation.addConfirmListener(e -> {
								FacadeProvider.getAreaFacade().archive(uuidsz);
								ConfigurationChangeLogDto configurationChangeLogDto = new ConfigurationChangeLogDto();
								configurationChangeLogDto.setCreatinguser(userProvider.getUser().getUserName());
								configurationChangeLogDto.setAction_unit_type("Region");
								configurationChangeLogDto.setAction_unit_name(areaDto.getName());
								configurationChangeLogDto.setUnit_code(areaDto.getExternalId());
								configurationChangeLogDto.setAction_logged("Archive");
								configurationChangeLogDto.setAction_date(date);
				
								FacadeProvider.getAreaFacade().saveAreaChangeLog(configurationChangeLogDto);
								
								dialog.close();
								refreshGridData();
							});

						}
					}
				}
			});
		}

		saveButton.getStyle().set("margin-right", "10px");

		saveButton.addClickListener(saveEvent -> {

			String name = nameField.getValue();
			String code = rCodeField.getValue();
			String uuids = "";

			if (areaDto != null) {
				uuids = areaDto.getUuid();
				System.out.println(areaDto + "Area uuii is not nullll " + areaDto.getUuid());
			}
			if ((name != null && name != "")
					&& (!rCodeField.getValue().isBlank() || !rCodeField.getValue().isEmpty())) {
				System.out.println("Area uuii is not nullll " + uuids);
				if (uuids != null) {

					AreaDto dce = FacadeProvider.getAreaFacade().getByUuid(uuids);

					System.out.println("Area from area dto  is not nullll " + dce);
					if (dce != null) {
						dce.setName(name);
						long rcodeValue = Long.parseLong(code);
						dce.setExternalId(rcodeValue);
						boolean exceptionCheck = false;
						try {
							FacadeProvider.getAreaFacade().save(dce, true);
							Notification.show(I18nProperties.getString(Strings.saved) + name + " " + code);
							dialog.close();
							refreshGridData();
						} catch (Exception e) {
							exceptionCheck = true;
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
						}finally {
							if(!exceptionCheck) {
//								ConfigurationChangeLogDto(String creatingUser_string, String action_unit_type, String action_unit_name,
//										String unit_code, String action_logged)
								ConfigurationChangeLogDto configurationChangeLogDto = new ConfigurationChangeLogDto();
								configurationChangeLogDto.setCreatinguser(userProvider.getUser().getUserName());
								configurationChangeLogDto.setAction_unit_type("Region");
								configurationChangeLogDto.setAction_unit_name(name);
								configurationChangeLogDto.setUnit_code(rcodeValue);
								configurationChangeLogDto.setAction_logged("Region Edit");
								configurationChangeLogDto.setAction_date(date);

								FacadeProvider.getAreaFacade().saveAreaChangeLog(configurationChangeLogDto);
								
							}
						}
					} else {
						AreaDto dcex = new AreaDto();
						System.out.println(dcex);
						dcex.setName(name);
						long rcodeValue = Long.parseLong(code);
						dcex.setExternalId(rcodeValue);
						List<AreaReferenceDto> ccc = FacadeProvider.getAreaFacade().getByExternalID(rcodeValue, false);
						List<AreaReferenceDto> cccx = FacadeProvider.getAreaFacade().getByName(name, false);
						boolean checkexception = false;
						if (ccc.size() < 1 && cccx.size() < 1) {
							try {

								FacadeProvider.getAreaFacade().save(dcex, true);
								Notification.show(I18nProperties.getString(Strings.savedNewRegion) + name + " " + code);
								dialog.close();
								refreshGridData();
							} catch (Exception e) {
								checkexception = true;
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
//							        Notification.show("An error occurred while saving: " + e.getMessage());
							}finally {
								
								if(!checkexception) {
									ConfigurationChangeLogDto configurationChangeLogDto = new ConfigurationChangeLogDto();
									configurationChangeLogDto.setCreatinguser(userProvider.getUser().getUserName());
									configurationChangeLogDto.setAction_unit_type("Region");
									configurationChangeLogDto.setAction_unit_name(name);
									configurationChangeLogDto.setUnit_code(rcodeValue);
									configurationChangeLogDto.setAction_logged("Region Create");
									configurationChangeLogDto.setAction_date(date);
																	
									FacadeProvider.getAreaFacade().saveAreaChangeLog(configurationChangeLogDto);
								}
								
							}
						} else if (ccc.size() >= 1) {
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
									"Region Code already exists. Please select a unique Region Code to continue.");

							HorizontalLayout layout = new HorizontalLayout(text, closeButton);
							layout.setAlignItems(Alignment.CENTER);

							notification.add(layout);
							notification.open();
						} else if (cccx.size() >= 1) {
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
									"Region Name already exists. Please select a unique Region Name to continue.");

							HorizontalLayout layout = new HorizontalLayout(text, closeButton);
							layout.setAlignItems(Alignment.CENTER);

							notification.add(layout);
							notification.open();
						}

					}

				}
			} else if ((rCodeField.getValue().isBlank() || rCodeField.getValue().isEmpty())) {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.setPosition(Position.MIDDLE);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph("Rcode Cannot be left blank.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
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

				Paragraph text = new Paragraph("Region Name Cannot be left blank.");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
			} else {
				Notification.show(I18nProperties.getCaption(Captions.notValidValue) + code);
			}

		});

		fmr.add(nameField, rCodeField);

		if (areaDto == null) {
			dialog.setHeaderTitle(I18nProperties.getCaption(Captions.addNewRegion));
			dialog.getFooter().add(discardButton, saveButton);
		} else {
			dialog.setHeaderTitle(I18nProperties.getCaption(Captions.edit) + areaDto.getName());
			dialog.getFooter().add(archiveButton, discardButton, saveButton);

		}
		dialog.add(fmr);

		dialog.open();

		return true;
	}

}
