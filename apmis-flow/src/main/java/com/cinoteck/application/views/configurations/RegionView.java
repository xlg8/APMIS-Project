package com.cinoteck.application.views.configurations;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import de.symeda.sormas.api.infrastructure.area.AreaCriteria;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;

//import org.vaadin.olli.FileDownloadWrapper;

@PageTitle("Regions")
@Route(value = "regions", layout = ConfigurationsView.class)
public class RegionView extends VerticalLayout implements RouterLayout {

	private static final long serialVersionUID = 7091198805223773269L;

	private AreaCriteria criteria;

	private GridListDataView<AreaDto> dataView;
	final static TextField regionField = new TextField("Region");
	final static TextField rcodeField = new TextField("RCode");
	Binder<AreaDto> binder = new BeanValidationBinder<>(AreaDto.class);
	Grid<AreaDto> grid = new Grid<>(AreaDto.class, false);;
	private Button saveButton;
	private Button archiveDearchive;

	private Button createNewArea;
	ComboBox<EntityRelevanceStatus> relevanceStatusFilter = new ComboBox<>();

	HorizontalLayout layout = new HorizontalLayout();
	AreaDto areaDto;
	Anchor link;
	TextField searchField;
	Button clear;

	public RegionView() {
		setSpacing(false);
		setHeightFull();
		addRegionFilter();
		regionGrid();

	}

	private void regionGrid() {
//		criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE); Archive

		this.criteria = new AreaCriteria();
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
		ListDataProvider<AreaDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getAreaFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				createOrEditArea(event.getValue());
			}
		});

		add(grid);
		exportArea();
	}

	private void refreshGridData() {
		ListDataProvider<AreaDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getAreaFacade().getIndexList(criteria, null, null, null).stream());
		dataView = grid.setItems(dataProvider);
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
		setMargin(true);
		layout.setPadding(false);
		layout.setVisible(false);
		layout.setAlignItems(Alignment.END);

		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);

		vlayout.setAlignItems(Alignment.END);

		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
		displayFilters.getStyle().set("margin-left", "1em");
		displayFilters.addClickListener(e -> {
			if (layout.isVisible() == false) {
				layout.setVisible(true);
				displayFilters.setText("Hide Filters");
			} else {
				layout.setVisible(false);
				displayFilters.setText("Show Filters");
			}
		});

		layout.setPadding(false);
		layout.setVisible(false);
		layout.setAlignItems(Alignment.END);

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

			return matchesDistrictName || matchesDistrictNumber;

		}));

		clear = new Button("Clear Search");
		clear.getStyle().set("color", "white");
		clear.getStyle().set("background", "#0D6938");
		clear.addClickListener(e -> {
			searchField.clear();

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

//		ListDataProvider<AreaDto> dataProvider = (ListDataProvider<AreaDto>) grid.getDataProvider();
//		this.criteria = new AreaCriteria();
//		relevanceStatusFilter = new ComboBox<EntityRelevanceStatus>();
//		relevanceStatusFilter.setLabel("Campaign Status");
//		relevanceStatusFilter.setItems((EntityRelevanceStatus[]) EntityRelevanceStatus.values());
//
////		relevanceStatusFilter.setItems(EntityRelevanceStatus.values().toString());
//		relevanceStatusFilter.setValue(EntityRelevanceStatus.ACTIVE);
//		if (relevanceStatusFilter.getValue() == EntityRelevanceStatus.ACTIVE) {
//			criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
//			refreshGridData();
//		}
//		
//		relevanceStatusFilter.addValueChangeListener(e -> {
////			dataView.removeFilters();
//			if (relevanceStatusFilter.getValue().toString() == "Archived Regions") {
//				criteria.relevanceStatus(EntityRelevanceStatus.ARCHIVED);
//				refreshGridData();
////			 dataView.addFilter(t -> t.isArchived());
//			} else if (relevanceStatusFilter.getValue().toString() == "Active") {
//				criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
//				refreshGridData();
////			 dataView.addFilter(t -> !t.isArchived());
//			} else if (relevanceStatusFilter.getValue().toString() == "All Regions") {
//				criteria.relevanceStatus(EntityRelevanceStatus.ALL);
//				refreshGridData();
////			 dataView.removeFilters();
//			}
//
//		});

		relevanceStatusFilter = new ComboBox<EntityRelevanceStatus>();
		relevanceStatusFilter.setLabel("Campaign Status");
		relevanceStatusFilter.setItems((EntityRelevanceStatus[]) EntityRelevanceStatus.values());
		relevanceStatusFilter.addValueChangeListener(e -> {

			criteria.relevanceStatus(e.getValue()); // Set the selected relevance status in the criteria object
			refreshGridData();

		});

		layout.add(searchField, clear, relevanceStatusFilter, addNew, importArea);

		vlayout.add(displayFilters, layout);
		add(vlayout);
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
						grid.getDataProvider().refreshAll();
					} else {
						AreaDto dcex = new AreaDto();
						System.out.println(dcex);
						dcex.setName(name);
						long rcodeValue = Long.parseLong(code);
						dcex.setExternalId(rcodeValue);
						FacadeProvider.getAreaFacade().save(dcex, true);
						Notification.show("Saved New Region: " + name + " " + code);
						dialog.close();
						grid.getDataProvider().refreshAll();
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
		dialog.getFooter().add( discardButton, saveButton);

//         getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
// 		.set("display", "flex").set("align-items", "center").set("justify-content", "center");

		dialog.open();

		return true;
	}

}
