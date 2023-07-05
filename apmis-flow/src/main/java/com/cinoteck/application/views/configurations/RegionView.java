package com.cinoteck.application.views.configurations;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaCriteria;
import de.symeda.sormas.api.infrastructure.area.AreaDto;

import java.util.List;
import java.util.stream.Stream;

@PageTitle("Regions")
@Route(value = "regions", layout = ConfigurationsView.class)
public class RegionView extends VerticalLayout implements RouterLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7091198805223773269L;

	private final AreaCriteria criteria;

	GridListDataView<AreaDto> dataView;
	final static TextField regionField = new TextField("Region");
	final static TextField rcodeField = new TextField("RCode");
	Binder<AreaDto> binder = new BeanValidationBinder<>(AreaDto.class);
	Grid<AreaDto> grid;
	private Button saveButton;

	public RegionView() {
		this.criteria = new AreaCriteria();
		setSpacing(false);

		addRegionFilter();
		
		setMargin(false);
		setSizeFull();
		grid = new Grid<>(AreaDto.class, false);
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		grid.addColumn(AreaDto::getName).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(AreaDto::getExternalId).setHeader("Rcode").setResizable(true).setSortable(true);

		//grid.setItemDetailsRenderer(createAreaEditFormRenderer());
		grid.setVisible(true);
		grid.setAllRowsVisible(true);
		List<AreaDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReferenceAndPopulation();
		this.dataView = grid.setItems(regions);

		
		add(grid);
		
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				createOrEditArea(event.getValue());
			}
		});
		}
	


	private ComponentRenderer<AreaEditForm, AreaDto> createAreaEditFormRenderer() {
		return new ComponentRenderer<>(AreaEditForm::new);
	}

	private class AreaEditForm extends FormLayout {

		public AreaEditForm(AreaDto areaDto) {
			Dialog formLayout  = new Dialog();
			
			H2 header = new H2("Edit " + areaDto.getName().toString());
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

	public void addRegionFilter() {
		setMargin(true);
		HorizontalLayout layout = new HorizontalLayout();
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
		searchIcon.getStyle().set("color", "green !important");

		TextField searchField = new TextField();

		searchField.setPlaceholder("Search");
		searchField.setPrefixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setWidth("30%");

		searchField.addClassName("filterBar");
		searchField.addValueChangeListener(
				e -> dataView.addFilter(search -> {
			String searchTerm = searchField.getValue().trim();

			if (searchTerm.isEmpty())
				return true;

			boolean matchesDistrictName = String.valueOf(search.getName()).toLowerCase()
					.contains(searchTerm.toLowerCase());

			return matchesDistrictName;

		}));

		Button clear = new Button("Clear Search");
		clear.getStyle().set("color", "white");
		clear.getStyle().set("background", "#0C5830");
		clear.addClickListener(e -> {
			searchField.clear();

		});
		
		ComboBox relevanceStatusFilter = new ComboBox<>();

		relevanceStatusFilter.setItems(EntityRelevanceStatus.values());

		relevanceStatusFilter.setItemLabelGenerator(status -> {
			if (status == EntityRelevanceStatus.ARCHIVED) {
				return I18nProperties.getCaption(Captions.areaArchivedAreas);
			} else if (status == EntityRelevanceStatus.ACTIVE) {
				return I18nProperties.getCaption(Captions.areaActiveAreas);
			} else if (status == EntityRelevanceStatus.ALL) {
				return I18nProperties.getCaption(Captions.areaAllAreas);
			}
			// Handle other enum values if needed
			return status.toString();
		});
		
		relevanceStatusFilter.addValueChangeListener(e -> {
			
		});
		layout.add(searchField, clear, relevanceStatusFilter);

		vlayout.add(displayFilters, layout);
		add(vlayout);
	}
	
	public boolean createOrEditArea(AreaDto areaDto) {
		 Dialog dialog = new Dialog();
		 FormLayout fmr = new FormLayout();
         TextField nameField = new TextField("Name");
         nameField.setValue(areaDto.getName());
         TextField rCodeField = new TextField("RCode");
         //this can generate null
         rCodeField.setValue(areaDto.getExternalId().toString());
         dialog.setCloseOnEsc(false);
 		dialog.setCloseOnOutsideClick(false);
 		
         Button saveButton = new Button("Save");
         saveButton.addClickListener(saveEvent -> {
        	 
             String firstName = nameField.getValue();
             String lastName = rCodeField.getValue();

            String uuids = areaDto.getUuid_();
            System.out.println(lastName+"________________"+uuids+"__________________"+firstName);
 			if(firstName != null && lastName != null) {
 				
 				AreaDto dce = FacadeProvider.getAreaFacade().getByUuid(uuids);
 				
 				System.out.println(dce);
 				
 				System.out.println(dce.getCreationDate() +" ====== "+dce.getName()+"-----"+dce.getUuid());
 				
 				dce.setName(firstName);
	 			long rcodeValue = Long.parseLong(lastName);
	 			dce.setExternalId(rcodeValue);
	 			
	 			FacadeProvider.getAreaFacade().save(dce, true);
	 			
	 			
	             // Perform save operation or any desired logic here
	
	             Notification.show("Saved: " + firstName + " " + lastName);
	             dialog.close();
             
 			} else {
 				Notification.show("Not Valid Value: " + firstName + " " + lastName);
 			}
 			
             
         });

         fmr.add(nameField, rCodeField, saveButton);
         dialog.add(fmr);
         
//         getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
// 		.set("display", "flex").set("align-items", "center").set("justify-content", "center");
 		
         
         
         dialog.open();
         
         return true;
	}

}
