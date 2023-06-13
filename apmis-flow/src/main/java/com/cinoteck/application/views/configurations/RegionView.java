package com.cinoteck.application.views.configurations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.user.UserDto;

import java.util.List;
import java.util.stream.Stream;

@PageTitle("Regions")
@Route(value = "regions", layout = ConfigurationsView.class)
public class RegionView extends VerticalLayout implements RouterLayout {

    GridListDataView<AreaDto> dataView;
    final static TextField regionField = new TextField("Region");
    final static TextField rcodeField = new TextField("RCode");
    Binder<AreaDto> binder = new BeanValidationBinder<>(AreaDto.class);
    Grid<AreaDto> grid;
    private Button saveButton;

    public RegionView() {

        grid = new Grid<>(AreaDto.class, false);

        grid.setSelectionMode(SelectionMode.SINGLE);
        grid.setMultiSort(true, MultiSortPriority.APPEND);
        grid.setSizeFull();
        grid.setColumnReorderingAllowed(true);
        grid.addColumn(AreaDto::getName).setHeader("Region").setSortable(true).setResizable(true);
        grid.addColumn(AreaDto::getExternalId).setHeader("Rcode").setResizable(true).setSortable(true);

        grid.setItemDetailsRenderer(createAreaEditFormRenderer());
        grid.setVisible(true);
        grid.setAllRowsVisible(true);
        List<AreaDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReferenceAndPopulation();
        this.dataView = grid.setItems(regions);

        addRegionFilter();
        add(grid);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                AreaDto selectedArea = event.getValue();
                setArea(selectedArea);
            }
        });
    }

    private ComponentRenderer<AreaEditForm, AreaDto> createAreaEditFormRenderer() {
        return new ComponentRenderer<>(AreaEditForm::new);
    }

    private class AreaEditForm extends FormLayout {

        public AreaEditForm() {
            Stream.of(regionField, rcodeField).forEach(e -> {
                e.setReadOnly(false);
                add(e);
            });
            saveButton = new Button("Save");
            
            saveButton.addClickListener(event -> saveArea());
            add(saveButton);
        }
    }
    
    private void saveArea() {
        if (binder.isValid()) {
            AreaDto areaDto = binder.getBean();
            String regionValue = regionField.getValue();
            long rcodeValue = Long.parseLong(rcodeField.getValue());
            
            areaDto.setName(regionValue);
            areaDto.setExternalId(rcodeValue);
            
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
        searchField.addValueChangeListener(e -> dataView.addFilter(search -> {
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

        relevanceStatusFilter.getStyle().set("color", "green");
        relevanceStatusFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());

        relevanceStatusFilter.setItems((Object[]) EntityRelevanceStatus.values());
        relevanceStatusFilter.setItems(EntityRelevanceStatus.ACTIVE,
                I18nProperties.getCaption(Captions.districtActiveDistricts));
        relevanceStatusFilter.setItems(EntityRelevanceStatus.ARCHIVED,
                I18nProperties.getCaption(Captions.districtArchivedDistricts));
        relevanceStatusFilter.setItems(EntityRelevanceStatus.ALL,
                I18nProperties.getCaption(Captions.districtAllDistricts));
        relevanceStatusFilter.addValueChangeListener(e -> {

        });
        layout.add(searchField, clear, relevanceStatusFilter);

        vlayout.add(displayFilters, layout);
        add(vlayout);
    }

}
