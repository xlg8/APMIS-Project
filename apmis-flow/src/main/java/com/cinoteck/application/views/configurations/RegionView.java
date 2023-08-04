package com.cinoteck.application.views.configurations;

import com.cinoteck.application.UserProvider;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
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
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.infrastructure.area.AreaCriteria;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.user.UserRight;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

//import org.vaadin.olli.FileDownloadWrapper;

@PageTitle("Regions")
@Route(value = "regions", layout = ConfigurationsView.class)
public class RegionView extends VerticalLayout implements RouterLayout {

	private static final long serialVersionUID = 7091198805223773269L;

	private AreaCriteria criteria = new AreaCriteria();

	private GridListDataView<AreaDto> dataView;
	final static TextField regionField = new TextField("Region");
	final static TextField rcodeField = new TextField("RCode");
	Binder<AreaDto> binder = new BeanValidationBinder<>(AreaDto.class);
	Grid<AreaDto> grid = new Grid<>(AreaDto.class, false);;
	private Button saveButton;
	private Button archiveDearchive;

	private Button createNewArea;
	ComboBox<EntityRelevanceStatus> relevanceStatusFilter = new ComboBox<>();
	Anchor anchor = new Anchor("", "Export");

	HorizontalLayout layout = new HorizontalLayout();
	HorizontalLayout relevancelayout = new HorizontalLayout();
	AreaDto areaDto;
	Anchor link;
	TextField searchField;
	Button clear;
	UserProvider userProvider = new UserProvider();
	Button enterBulkEdit = new Button("Enter Bulk Edit Mode");
	Button leaveBulkEdit = new Button("Leave Bulk Edit");
	Paragraph countRowItems;
	
	List<AreaDto> data;
	MenuBar dropdownBulkOperations = new MenuBar();
	ListDataProvider<AreaDto> dataProvider = DataProvider
			.fromStream(FacadeProvider.getAreaFacade().getIndexList(criteria, null, null, null).stream());
	
	int itemCount = dataProvider.getItems().size();
	public RegionView() {
		setSpacing(false);
		setHeightFull();
		addRegionFilter();
		regionGrid(criteria);
		 
	}

//	private void updateRowCount() {
//		itemCount = data.size();
//		String newText = "No. of Regions : " + itemCount;
//		countRowItems.setText(newText);
//		countRowItems.setId("rowCount");
////        Notification.show("Text updated: " + newText);
//	}

	private void regionGrid(AreaCriteria criteria) {
//		criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE); Archive

		this.criteria = criteria;
		setSpacing(false);

		setMargin(false);
		setSizeFull();

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		grid.addColumn(AreaDto::getName).setHeader("Region").setSortable(true).setResizable(true).setAutoWidth(true);
		grid.addColumn(AreaDto::getExternalId).setHeader("Rcode").setResizable(true).setSortable(true)
				.setAutoWidth(true);

		// grid.setItemDetailsRenderer(createAreaEditFormRenderer());
		grid.setVisible(true);
		grid.setAllRowsVisible(true);
//		List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();

//		List<AreaDto> regions = FacadeProvider.getAreaFacade().getIndexList(criteria, null, null, null);
//		this.dataView = grid.setItems(regions);
		if (criteria.getRelevanceStatus() == null) {
			criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		}
		dataProvider = DataProvider
		.fromStream(FacadeProvider.getAreaFacade().getIndexList(criteria, null, null, null).stream());
		
		dataView = grid.setItems(dataProvider);
		 
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				createOrEditArea(event.getValue());
			}
		});

		add(grid);

		GridExporter<AreaDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle("Users");
		exporter.setFileName(
				"APMIS_Regions" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("exportArea");
		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());
		
//		exportArea();
	}

	private void refreshGridData() {
		ListDataProvider<AreaDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getAreaFacade().getIndexList(criteria, null, null, null).stream());
		
		dataView = grid.setItems(dataProvider);
		itemCount = dataProvider.getItems().size();
		String newText = "Rows : " + itemCount;
		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}

	private ComponentRenderer<AreaEditForm, AreaDto> createAreaEditFormRenderer() {
		return new ComponentRenderer<>(AreaEditForm::new);
	}

	public void exportArea() {

		// Fetch all data from the grid in the current sorted order
		Stream<AreaDto> persons = null;
//        Set<AreaDto> selection = grid.asMultiSelect().getValue();
//        if (selection != null && selection.size() > 0) {
//            persons = selection.stream();
//        } else {
		persons = dataView.getItems();
//        }

		StringWriter output = new StringWriter();
		StatefulBeanToCsv<AreaDto> writer = new StatefulBeanToCsvBuilder<AreaDto>(output).build();
		try {
			writer.write(persons);
		} catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			output.write("An error occured during writing: " + e.getMessage());
		}

		StreamResource resource = new StreamResource("my.csv",
				() -> new ByteArrayInputStream(output.toString().getBytes()));

//		link = new Anchor(resource, "Exportt");
//		layout.add(link);
//            Button downloadButton = new Button("Download text area contents as a CSV file using a button");
//            FileDownloader downloadButtonWrapper = new FileDownloader(resource);
//            downloadButtonWrapper.extend(downloadButton);

//        result.setValue(output.toString());
	}

	private class AreaEditForm extends FormLayout {

		public AreaEditForm(AreaDto areaDto) {
			Dialog formLayout = new Dialog();

			H2 header = new H2("Edit " + areaDto.getName().toString());
			this.setColspan(header, 2);
			add(header);
			Stream.of(regionField, rcodeField).forEach(e -> {
				e.setReadOnly(false);
				add(e);

			});
			saveButton = new Button("Save");
			archiveDearchive = new Button("Archive");

			saveButton.addClickListener(event -> saveArea(areaDto));

			add(archiveDearchive, saveButton);
		}
	}

	private class AreaCreateForm extends FormLayout {

		public AreaCreateForm(AreaDto areaDto) {
			Dialog formLayout = new Dialog();

			H2 header = new H2("Create New Region");
			this.setColspan(header, 2);
			add(header);
			Stream.of(regionField, rcodeField).forEach(e -> {
				e.setReadOnly(false);
				add(e);

			});
			saveButton = new Button("Save");

			saveButton.addClickListener(event -> saveArea(areaDto));

			add(saveButton);
		}
	}

	private void saveArea(AreaDto areaDto) {
		if (binder.isValid()) {
			AreaDto areavDto = binder.getBean();
			String regionValue = regionField.getValue();
			long rcodeValue = Long.parseLong(rcodeField.getValue());
			AreaDto dtoToSave = FacadeProvider.getAreaFacade().getByUuid(areaDto.getUuid());

			dtoToSave.setName(regionValue);
			dtoToSave.setExternalId(rcodeValue);

			FacadeProvider.getAreaFacade().save(dtoToSave, true);

			grid.getDataProvider().refreshItem(areaDto);
		}
	}

	public void setArea(AreaDto areaDto) {
		regionField.setValue(areaDto.getName());
		rcodeField.setValue(String.valueOf(areaDto.getExternalId()));
		binder.setBean(areaDto);
	}

	private void addRegionFilter() {
		
		
		
		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			enterBulkEdit = new Button("Enter Bulk Edit Mode");
			leaveBulkEdit = new Button();
			dropdownBulkOperations = new MenuBar();
		}
		setMargin(true);
		layout.setPadding(false);
		layout.setVisible(false);
		layout.setAlignItems(Alignment.END);

		relevancelayout.setPadding(false);
		relevancelayout.setVisible(false);
		relevancelayout.setAlignItems(Alignment.END);
		relevancelayout.setJustifyContentMode(JustifyContentMode.END);
		relevancelayout.setWidth("10%");

		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);

		vlayout.setAlignItems(Alignment.END);

		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
		displayFilters.getStyle().set("margin-left", "1em");
		displayFilters.addClickListener(e -> {
			if (layout.isVisible() == false) {
				layout.setVisible(true);
				relevancelayout.setVisible(true);
				displayFilters.setText("Hide Filters");
			} else {
				layout.setVisible(false);
				relevancelayout.setVisible(false);
				displayFilters.setText("Show Filters");
			}
		});

		layout.setPadding(false);
		layout.setVisible(false);
		layout.setAlignItems(Alignment.END);
		layout.setWidth("80%");
	

		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.getStyle().set("color", "#0D6938 !important");

		searchField = new TextField();

		searchField.setPlaceholder("Search");
		searchField.setPrefixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setWidth("30%");

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

		}
		));

		clear = new Button("Clear Search");
		clear.getStyle().set("color", "white");
		clear.getStyle().set("background", "#0D6938");
		clear.addClickListener(e -> {
			searchField.clear();
//			updateRowCount();

		});

		Button addNew = new Button("Add New Region");
		addNew.getElement().getStyle().set("white-space", "normal");
		addNew.getStyle().set("color", "white");
		addNew.getStyle().set("background", "#0D6938");
		addNew.addClickListener(event -> {
			createOrEditArea(areaDto);
		});

		Button importArea = new Button("Import");
		importArea.getStyle().set("color", "white");
		importArea.getStyle().set("background", "#0D6938");
		importArea.addClickListener(event -> {

		});

		relevanceStatusFilter = new ComboBox<EntityRelevanceStatus>();
		relevanceStatusFilter.setLabel("Campaign Status");
		relevanceStatusFilter.setItems((EntityRelevanceStatus[]) EntityRelevanceStatus.values());

		relevanceStatusFilter.addValueChangeListener(e -> {

			criteria.relevanceStatus(e.getValue()); // Set the selected relevance status in the criteria object
			refreshGridData();
//			updateRowCount();
			
		});
		searchField.addClassName("filter-item");
		relevanceStatusFilter.addClassName("filter-item");
		clear.addClassName("filter-item");
		addNew.addClassName("filter-item");
		anchor.addClassName("filter-item");
		layout.add(searchField, relevanceStatusFilter, clear, addNew, anchor);
		
//		int numberOfRows = (int) FacadeProvider.getAreaFacade().count(criteria);
		
		countRowItems = new Paragraph("Rows : " + itemCount);
		countRowItems.setId("rowCount");

		relevancelayout.add(countRowItems);
		vlayout.setWidth("99%");
		vlayout.add(displayFilters, layout, relevancelayout);
		add(vlayout);

		leaveBulkEdit.setText("Enter Bulk Edit Mode");

		enterBulkEdit.addClassName("bulkActionButton");
		Icon bulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		enterBulkEdit.setIcon(bulkModeButtonnIcon);
		layout.add(enterBulkEdit);

		enterBulkEdit.addClickListener(e -> {
			dropdownBulkOperations.setVisible(true);
			grid.setSelectionMode(Grid.SelectionMode.MULTI);
			enterBulkEdit.setVisible(false);
			leaveBulkEdit.setVisible(true);

		});

		leaveBulkEdit.setText("Leave Bulk Edit Mode");
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
//		MenuItem item = dropdownBulkOperations.addItem(Captions.bulkActions,selectedItem -> {
//			ControllerProvider.getInfrastructureController()
//			.archiveOrDearchiveAllSelectedItems(
//				true,
//				grid.asMultiSelect().getSelectedItems(),
//				InfrastructureType.AREA,
//				() -> navigateTo(criteria));
//	}
//		);
//		SubMenu subMenu = item.getSubMenu();
//		subMenu.addItem(new Checkbox("Archive"));
		dropdownBulkOperations.getStyle().set("margin-top", "5px");
//		layout.add(dropdownBulkOperations);

//		if (UserProvider.getCurrent().hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
//			dropdownBulkOperations = MenuBarHelper.createDropDown(Captions.bulkActions,
//				new MenuBarHelper.MenuBarItem(I18nProperties.getCaption(Captions.actionArchive),
//						VaadinIcons.ARCHIVE, 
//						selectedItem -> {
//					ControllerProvider.getInfrastructureController()
//						.archiveOrDearchiveAllSelectedItems(
//							true,
//							grid.asMultiSelect().getSelectedItems(),
//							InfrastructureType.AREA,
//							() -> navigateTo(criteria));
//				}, 
//						EntityRelevanceStatus.ACTIVE.equals(criteria.getRelevanceStatus())),
//				
//				new MenuBarHelper.MenuBarItem(I18nProperties.getCaption(Captions.actionDearchive), VaadinIcons.ARCHIVE, selectedItem -> {
//					ControllerProvider.getInfrastructureController()
//						.archiveOrDearchiveAllSelectedItems(
//							false,
//							grid.asMultiSelect().getSelectedItems(),
//							InfrastructureType.AREA,
//							() -> navigateTo(criteria));
//				}, EntityRelevanceStatus.ARCHIVED.equals(criteria.getRelevanceStatus())));
//
//			dropdownBulkOperations
//				.setVisible(viewConfiguration.isInEagerMode() && !EntityRelevanceStatus.ALL.equals(criteria.getRelevanceStatus()));
//			actionButtonsLayout.addComponent(dropdownBulkOperations);
//		}

	}

	public boolean createOrEditArea(AreaDto areaDto) {
		Dialog dialog = new Dialog();
		FormLayout fmr = new FormLayout();

		TextField nameField = new TextField("Name");
		TextField rCodeField = new TextField("RCode");

		if (areaDto != null) {
			nameField.setValue(areaDto.getName());
			rCodeField.setValue(areaDto.getExternalId().toString());
		}
		// this can generate null

		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

//		Button archiveButton = new Button("Archive");
//
//
//		archiveButton.addClickListener(archiveEvent -> {
//			String uuids = "";
//			if (areaDto != null) {
//				uuids = areaDto.getUuid_();
//				FacadeProvider.getAreaFacade().archive(uuids);
//			} else if (areaDto != null && (isArchived == true)) {
//				FacadeProvider.getAreaFacade().dearchive(uuids);
//			}
//		});
		Button saveButton = new Button("Save");
		Button discardButton = new Button("Discard", e -> dialog.close());
		saveButton.getStyle().set("margin-right", "10px");
		saveButton.addClickListener(saveEvent -> {

			String name = nameField.getValue();
			String code = rCodeField.getValue();

			String uuids = "";
			if (areaDto != null) {
				uuids = areaDto.getUuid_();
			}
			if (name != null && name != null) {
				if (uuids != null) {
					AreaDto dce = FacadeProvider.getAreaFacade().getByUuid(uuids);
					System.out.println(dce);
					if (dce != null) {
						dce.setName(name);

						long rcodeValue = Long.parseLong(code);
						dce.setExternalId(rcodeValue);
						FacadeProvider.getAreaFacade().save(dce, true);
						Notification.show("Saved: " + name + " " + code);
						dialog.close();
						refreshGridData();
					} else {
						AreaDto dcex = new AreaDto();
						System.out.println(dcex);
						dcex.setName(name);
						long rcodeValue = Long.parseLong(code);
						dcex.setExternalId(rcodeValue);
						FacadeProvider.getAreaFacade().save(dcex, true);
						Notification.show("Saved New Region: " + name + " " + code);
						dialog.close();
						refreshGridData();
//						grid.getDataProvider().refreshAll();
					}

				}
			} else {
				Notification.show("Not Valid Value: " + name + " " + code);
			}

		});

		fmr.add(nameField, rCodeField);

		if (areaDto == null) {
			dialog.setHeaderTitle("Add New Region");
		} else {
			dialog.setHeaderTitle("Edit " + areaDto.getName());

		}
		dialog.add(fmr);
		dialog.getFooter().add(discardButton, saveButton);

//         getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
// 		.set("display", "flex").set("align-items", "center").set("justify-content", "center");

		dialog.open();

		return true;
	}

}
