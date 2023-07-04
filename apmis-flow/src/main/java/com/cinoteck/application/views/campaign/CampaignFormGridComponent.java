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

public class CampaignFormGridComponent<T> extends AbstractEditableGrid<CampaignFormMetaReferenceDto> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	CampaignDto cam;

	public CampaignFormGridComponent(List<CampaignFormMetaReferenceDto> savedCampaignFormMetas,
			List<CampaignFormMetaReferenceDto> allCampaignFormMetas, String phase) {
		super(savedCampaignFormMetas, allCampaignFormMetas);

		setWidthFull();
	}

	
	@Override
	protected ComponentEventListener <ClickEvent<Button>> newRowEvent() {
		return event -> {
			final ArrayList<CampaignFormMetaReferenceDto> gridItems = getItems();
			gridItems.add(new CampaignFormMetaReferenceDto(null, " --Please select--", null, 0));

			grid.setItems(gridItems);

			grid.getEditor().cancel();
			
			int selectedIndex = 0; // Replace this with the actual index you want to edit
			CampaignFormMetaReferenceDto selectedItem = gridItems.get(selectedIndex );


			grid.getEditor().editItem(selectedItem);

		};
	}
	
    public void ListnerCampaignFilter(SelectedChangeEvent event) {
        List<CampaignFormMetaReferenceDto> gridItemss = getItems();
        List<CampaignFormMetaReferenceDto> gridItems;

        if (event.getSelectedTab().getLabel().equals("Pre-Campaign Phase")) {
            gridItems = new ArrayList<>(gridItemss);
            gridItems.removeIf(n -> n.getFormType().contains("Pre-Campaign"));
            grid.setItems(gridItems);
        } else if (event.getSelectedTab().getLabel().equals("Intra-Campaign Phase")) {
            gridItems = new ArrayList<>(gridItemss);
            gridItems.removeIf(n -> n.getFormType().contains("Intra-Campaign"));
            grid.setItems(gridItems);
        } else if (event.getSelectedTab().getLabel().equals("Post-Campaign Phase")) {
            gridItems = new ArrayList<>(gridItemss);
            gridItems.removeIf(n -> n.getFormType().contains("Post-Campaign"));
            grid.setItems(gridItems);
        }

        grid.getDataProvider().refreshAll();
    }

    @Override
	protected Binder<CampaignFormMetaReferenceDto> addColumnsBinder(List<CampaignFormMetaReferenceDto> allElements) {

		final Binder<CampaignFormMetaReferenceDto> binder = new Binder<>();

		ComboBox<CampaignFormMetaReferenceDto> formCombo = new ComboBox<>(Strings.entityCampaignDataForm, allElements);
		
		TextField dateExpiring = new TextField("Date");
		
		dateExpiring.setEnabled(false);
		
		Binder.Binding<CampaignFormMetaReferenceDto, String> dateBind = binder.forField(dateExpiring)
				.bind(campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(
						
						campaignFormMetaReferenceDto.getUuid(),campaignFormMetaReferenceDto.getDaysExpired()).toString(),
						(bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
							bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta);
							String str = selectedCampaignFormMeta;
							int number = Integer.parseInt(str);
							bindedCampaignFormMeta.setDaysExpired(number);
							grid.getDataProvider().refreshAll();
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
							// workarround: grid doesn't refresh itself for unknown reason
							grid.getDataProvider().refreshAll();
						});
		

		Grid.Column<CampaignFormMetaReferenceDto> formColumn = grid.addColumn(ReferenceDto::getCaption)
		        .setHeader(I18nProperties.getString(Strings.entityCampaignDataForm))
		        .setEditorComponent(column -> {
//		        	 ComboBox<CampaignFormMetaReferenceDto> formCombox = new ComboBox<>(Strings.entityCampaignDataForm, allElements);
		            formCombo.setRequired(false); // Allow empty selection
		            return formCombo;
		        });

		Grid.Column<CampaignFormMetaReferenceDto> deadlineColumn = grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired)
		        .setHeader("Form Deadline (Days)")
		        .setEditorComponent(column -> {
		        	dateExpiring.setRequired(false); // Allow empty selection
		            return dateExpiring;
		        });

		formColumn.setId("formtb");


		return binder;
	}


	@Override
	public void reorderGrid() {
	}

	public String getAdditionalRowCaption() {
		return Captions.campaignAdditionalForm;
	}



	@Override
	protected Button createButton( ComponentEventListener<ClickEvent<Button>> newRowEvent) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected String getHeaderString() {
		
		return Strings.headingCampaignData;
	}


}
