package com.cinoteck.application.views.campaign;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Binding;
import javax.swing.plaf.basic.BasicOptionPaneUI.ButtonActionListener;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheet.SelectedChangeEvent;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;

public class CampaignFormGridComponent extends AbstractEditableGrid<CampaignFormMetaReferenceDto> {

    private static final long serialVersionUID = 1L;
    private CampaignDto cam;

    public CampaignFormGridComponent(List<CampaignFormMetaReferenceDto> savedCampaignFormMetas,
                                     List<CampaignFormMetaReferenceDto> allCampaignFormMetas) {
        super(savedCampaignFormMetas, allCampaignFormMetas);
        setWidthFull();
    }

    
    @Override
    protected ComponentEventListener<ClickEvent<Button>> newRowEvent() {
    
    	return event -> {
        	CampaignFormMetaReferenceDto campaignFormMetaReferenceDto =	new CampaignFormMetaReferenceDto();

			final ArrayList<CampaignFormMetaReferenceDto> gridItems = getItems();
			gridItems.add(campaignFormMetaReferenceDto);

			grid.setItems(gridItems);

//			grid.getEditor().cancel();

			grid.getEditor().editItem(campaignFormMetaReferenceDto);

			
		};
    }

    public void listenerCampaignFilter(SelectedChangeEvent event) {
    	final ArrayList<CampaignFormMetaReferenceDto> gridItems = getItems();
    	final ArrayList<CampaignFormMetaReferenceDto> gridFilteredItems = new ArrayList<>(gridItems);

        if (event.getSelectedTab().getLabel().equals("Pre-Campaign Phase")) {
            gridFilteredItems.removeIf(n -> n.getFormType().contains("Pre-Campaign"));
        } else if (event.getSelectedTab().getLabel().equals("Intra-Campaign Phase")) {
            gridFilteredItems.removeIf(n -> n.getFormType().contains("Intra-Campaign"));
        } else if (event.getSelectedTab().getLabel().equals("Post-Campaign Phase")) {
            gridFilteredItems.removeIf(n -> n.getFormType().contains("Post-Campaign"));
        }

        grid.setItems(gridFilteredItems);
        grid.getDataProvider().refreshAll();
    }

    @Override
    protected Binder<CampaignFormMetaReferenceDto> addColumnsBinder(List<CampaignFormMetaReferenceDto> allElements) {
        final Binder<CampaignFormMetaReferenceDto> binder = new Binder<>();

        ComboBox<CampaignFormMetaReferenceDto> formCombo = new ComboBox<>(Strings.entityCampaignDataForm, allElements);

        TextField dateExpiring = new TextField("Date");
        dateExpiring.setEnabled(false);


        Binder.Binding<CampaignFormMetaReferenceDto, String> dateBind = binder.forField(dateExpiring)
        		 .withValidator(
                         campaignFormMetaReferenceDto -> campaignFormMetaReferenceDto != null
                                 && campaignFormMetaReferenceDto != null,
                         I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueNull))
				.bind(campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(
						
						campaignFormMetaReferenceDto.getUuid(),campaignFormMetaReferenceDto.getDaysExpired()).toString(),
						(bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
							bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta);
							bindedCampaignFormMeta.setDateExpired(selectedCampaignFormMeta);
//							grid.getDataProvider().refreshAll();
						});
	
		
		

        Binder.Binding<CampaignFormMetaReferenceDto, CampaignFormMetaReferenceDto> formBind = binder.forField(formCombo)
                .withValidator(
                        campaignFormMetaReferenceDto -> campaignFormMetaReferenceDto != null
                                && campaignFormMetaReferenceDto.getUuid() != null,
                        I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueNull))
                .withValidator(campaignFormMetaReferenceDto -> {
                    ArrayList<CampaignFormMetaReferenceDto> items = getItems();
                    return !items.contains(campaignFormMetaReferenceDto);
                }, I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueDuplicate))
                .bind(campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(
                                campaignFormMetaReferenceDto.getUuid(), campaignFormMetaReferenceDto.getCaption(),
                                campaignFormMetaReferenceDto.getFormType()),
                        (bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
                            bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta.getUuid());
                            bindedCampaignFormMeta.setCaption(selectedCampaignFormMeta.getCaption());
                            bindedCampaignFormMeta.setFormType(selectedCampaignFormMeta.getFormType());
                            // workaround: grid doesn't refresh itself for an unknown reason
//                            grid.getDataProvider().refreshAll();
                        });

        Grid.Column<CampaignFormMetaReferenceDto> formColumn = grid.addColumn(ReferenceDto::getCaption)
                .setHeader(I18nProperties.getString(Strings.entityCampaignDataForm))
                .setEditorComponent(column -> {
                    formCombo.setRequired(false); // Allow empty selection
                    formCombo.setItems(allElements);
                    formCombo.addValueChangeListener(event -> {
                        String newItem = event.getValue().toString();
                        if (newItem != null && !allElements.contains(newItem)) {
//                            allElements.add(newItem);
                            formCombo.setItems(allElements);
                        }
                    });                    
                    formCombo.getElement().addEventListener("keydown", event -> formCombo.setOpened(true));
                    formCombo.getElement().addEventListener("click", event -> formCombo.setOpened(true));
                    return formCombo;
                });

        Grid.Column<CampaignFormMetaReferenceDto> deadlineColumn = grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired)
                .setHeader("Form Deadline (Days)")
                .setEditorComponent(column -> {
                    dateExpiring.setRequired(false); // Allow empty selection
                    return dateExpiring;
                });

//        Editor<CampaignFormMetaReferenceDto> editor = grid.getEditor();
//        editor.setBinder(binder);
//        editor.setBuffered(true);
////        editor.setSaveCaption("Save");
//
//        editor.addSaveListener(event -> {
//            // Save logic
//        });
//
//        editor.addCancelListener(event -> {
//            // Cancel logic
//        });
//
//        editor.editItem(new CampaignFormMetaReferenceDto(null, "--Please Select --", null,  0));
        
        return binder;
    }

   

    @Override
    protected Button createButton(ComponentEventListener<ClickEvent<Button>> newRowEvent) {
        Button additionalRow = new Button("Add New Row");
        additionalRow.addClickListener(newRowEvent);
        return additionalRow;
    }

    protected CampaignFormMetaReferenceDto createNewItem() {
    	CampaignFormMetaReferenceDto xx =	new CampaignFormMetaReferenceDto();

        return xx;
    }

    @Override
    public void reorderGrid() {
        // Implement the reorderGrid method as needed
    }

    public String getAdditionalRowCaption() {
        return Captions.campaignAdditionalForm;
    }

    @Override
    protected String getHeaderString() {
        return Strings.headingCampaignData;
    }
}

