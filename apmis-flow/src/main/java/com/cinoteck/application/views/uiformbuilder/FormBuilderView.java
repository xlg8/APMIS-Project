package com.cinoteck.application.views.uiformbuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.campaign.CampaignForm;
import com.cinoteck.application.views.uiformbuilder.FormBuilderLayout.SaveEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Modality;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormCriteria;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;

@PageTitle("APMIS-Form-Manager")
@Route(value = "Form-Manager-Wizard", layout = MainLayout.class)
public class FormBuilderView extends VerticalLayout {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	HorizontalLayout filterWithButton;
	HorizontalLayout filterLayout;
	Button displayFilters;
	TextField search;
	ComboBox<CampaignPhase> formType;
	ComboBox<FormAccess> formAccess;
	ComboBox<Modality> modality;
	ComboBox<EntityRelevanceStatus> relevanceStatusFilter;

	HorizontalLayout buttonLayout;
	Button newForm;
	Button bulkModeButton = new Button("Enter Bulk Edit Mode", new Icon(VaadinIcon.CLIPBOARD_CHECK));
	Button leaveBulkModeButton = new Button("Leave Bulk Edit Mode", new Icon(VaadinIcon.CLIPBOARD_CHECK));
	Button dearchiveForms = new Button("Dearchive", new Icon(VaadinIcon.UNLOCK));
	Button archiveForms = new Button("Archive", new Icon(VaadinIcon.ARCHIVE));

	CampaignFormMetaDto campaignFormMetaDto;

	UserProvider userProvider = new UserProvider();
	HorizontalLayout hr = new HorizontalLayout();
	ConfirmDialog confirmationPopup = new ConfirmDialog();
	ConfirmDialog confirmationPopups = new ConfirmDialog();

	private FormBuilderDataProvider formBuilderDataProvider = new FormBuilderDataProvider();
	private ConfigurableFilterDataProvider<CampaignFormMetaDto, Void, CampaignFormCriteria> filterDataProvider;

	CampaignFormCriteria criteria = new CampaignFormCriteria();

	private Grid<CampaignFormMetaDto> grid = new Grid<>(CampaignFormMetaDto.class, false);
	private GridListDataView<CampaignFormMetaDto> dataView;

	public FormBuilderView() {

		this.setSizeFull();
		this.setHeightFull();
		this.setWidthFull();
		this.addClassName("uibuilderview");

		filterDataProvider = formBuilderDataProvider.withConfigurableFilter();

		criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		filterDataProvider.setFilter(criteria);
//		filterDataProvider.refreshAll();
		refreshGridData();

		configureView();
		configureGrid();
		setHeightFull();
		setSizeFull();

		buttonLayout.getStyle().set("margin-left", "10px");
		buttonLayout.setAlignItems(Alignment.END);
		filterWithButton.getStyle().set("margin-left", "10px");
		filterWithButton.setAlignItems(Alignment.END);
		displayFilters.getStyle().set("margin-top", "10px");
		buttonLayout.add(newForm, bulkModeButton, leaveBulkModeButton, dearchiveForms, archiveForms);
		filterLayout.add(search, formType, formAccess, modality, relevanceStatusFilter);
		filterWithButton.add(displayFilters, filterLayout);
		add(buttonLayout, filterWithButton, grid);
	}

	public void configureView() {

		filterWithButton = new HorizontalLayout();
		filterLayout = new HorizontalLayout();
		displayFilters = new Button("Hide Filters", new Icon(VaadinIcon.SLIDERS));

		search = new TextField("Search");
		search.setClearButtonVisible(true);
		formType = new ComboBox<>("Campaign Phase");
		formType.setItems(CampaignPhase.values());
		formType.setClearButtonVisible(true);
		formAccess = new ComboBox<>("Form Access");
		formAccess.setItems(FormAccess.values());
		formAccess.setClearButtonVisible(true);
		modality = new ComboBox<>("Modality");
		modality.setItems(Modality.values());
		modality.setClearButtonVisible(true);

		displayFilters.addClickListener(e -> {
			if (filterLayout.isVisible() == false) {
				filterLayout.setVisible(true);
				displayFilters.setText(I18nProperties.getCaption(Captions.hideFilters));
			} else {
				filterLayout.setVisible(false);
				displayFilters.setText(I18nProperties.getCaption(Captions.showFilters));
			}
		});

		buttonLayout = new HorizontalLayout();
		newForm = new Button("New Forms", new Icon(VaadinIcon.PLUS_CIRCLE_O));
		newForm.addClickListener(e -> {

			campaignFormMetaDto = new CampaignFormMetaDto();
			newForm(campaignFormMetaDto);
		});

		search.setValueChangeMode(ValueChangeMode.EAGER);
		search.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.setFormName(e.getValue().toString());
				filterDataProvider.setFilter(criteria);

//				filterDataProvider.refreshAll();
				refreshGridData();
			} else {

				criteria.setFormName(null);
				filterDataProvider.setFilter(criteria);

//				filterDataProvider.refreshAll();
				refreshGridData();
			}
		});

		formType.addValueChangeListener(e -> {
			if (e.getValue() != null) {

				criteria.setFormType(e.getValue().toString().toLowerCase());
				filterDataProvider.setFilter(criteria);

//				filterDataProvider.refreshAll();
				refreshGridData();
			} else {

				criteria.setFormType(null);
				filterDataProvider.setFilter(criteria);

//				filterDataProvider.refreshAll();
				refreshGridData();
			}
		});

		formAccess.addValueChangeListener(e -> {

			if (e.getValue() != null) {

				FormAccess formAccess = e.getValue();
				criteria.setFormCategory(formAccess);
				filterDataProvider.setFilter(criteria);

//				filterDataProvider.refreshAll();
				refreshGridData();
			} else {

				criteria.setFormCategory(null);
				filterDataProvider.setFilter(criteria);

//				filterDataProvider.refreshAll();
				refreshGridData();
			}
		});

		modality.addValueChangeListener(e -> {

			if (e.getValue() != null) {

				criteria.setModality(e.getValue().toString());
				filterDataProvider.setFilter(criteria);

//				filterDataProvider.refreshAll();
				refreshGridData();
			} else {

				criteria.setModality(null);
				filterDataProvider.setFilter(criteria);

//				filterDataProvider.refreshAll();
				refreshGridData();
			}
		});

		leaveBulkModeButton.setVisible(false);
		archiveForms.setVisible(false);
		dearchiveForms.setVisible(false);
		bulkModeButton.addClickListener(e -> {
			grid.setSelectionMode(Grid.SelectionMode.MULTI);
			bulkModeButton.setVisible(false);
			leaveBulkModeButton.setVisible(true);
			archiveForms.setVisible(true);
			dearchiveForms.setVisible(true);
			grid.getDataProvider().refreshAll();
		});

		leaveBulkModeButton.addClickListener(e -> {
			grid.setSelectionMode(Grid.SelectionMode.SINGLE);
			bulkModeButton.setVisible(true);
			leaveBulkModeButton.setVisible(false);
			archiveForms.setVisible(false);
			dearchiveForms.setVisible(false);
			grid.getDataProvider().refreshAll();
		});

		archiveForms.addClickListener(e -> {
			archiveFormPopup();
		});

		dearchiveForms.addClickListener(e -> {
			dearchiveFormPopup();
		});

		relevanceStatusFilter = new ComboBox<EntityRelevanceStatus>();
		relevanceStatusFilter.setLabel(I18nProperties.getCaption(Captions.campaignStatus));
		relevanceStatusFilter.setItems((EntityRelevanceStatus[]) EntityRelevanceStatus.values());
		relevanceStatusFilter.setClearButtonVisible(true);
		relevanceStatusFilter.setClassName("col-sm-6, col-xs-6");

		relevanceStatusFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {

				EntityRelevanceStatus entityRelevanceStatus = e.getValue();
				criteria.relevanceStatus(entityRelevanceStatus);
				filterDataProvider.setFilter(criteria);

//				filterDataProvider.refreshAll();
				refreshGridData();
			} else {

				criteria.relevanceStatus(null);
				filterDataProvider.setFilter(criteria);

//				filterDataProvider.refreshAll();
				refreshGridData();
			}
		});
	}

	public void configureGrid() {

		TextRenderer<CampaignFormMetaDto> creationDateRenderer = new TextRenderer<>(dto -> {
			Date timestamp = dto.getCreationDate();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			return dateFormat.format(timestamp);
		});

		TextRenderer<CampaignFormMetaDto> changeDateRenderer = new TextRenderer<>(dto -> {
			Date timestamp = dto.getChangeDate();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			return dateFormat.format(timestamp);
		});

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(CampaignFormMetaDto.FORM_NAME).setHeader("Form Name").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormMetaDto.FORM_CATEGORY).setHeader("Form Category").setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormMetaDto.FORM_TYPE).setHeader("Campaign Phase").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormMetaDto::getModality).setHeader("Modality").setSortable(true).setResizable(true);
//		grid.addColumn(creationDateRenderer).setHeader("Creation Date").setSortable(true).setResizable(true);
//		grid.addColumn(changeDateRenderer).setHeader("Change Date").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormMetaDto.DAYSTOEXPIRE).setHeader("Days To Expire").setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormMetaDto.DISTRICTENTRY).setHeader("District Data Entry").setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormMetaDto.LANGUAGE_CODE).setHeader("Language Code").setSortable(true);

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setAllRowsVisible(true);

//		grid.setDataProvider(filterDataProvider);
		
		ListDataProvider<CampaignFormMetaDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getCampaignFormMetaFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);
		if (userProvider.hasUserRight(UserRight.CAMPAIGN_EDIT)) {

			grid.asSingleSelect().addValueChangeListener(event -> editForm(event.getValue()));
		}

	}

	private void editForm(CampaignFormMetaDto formData) {

		FormBuilderLayout formLayout = new FormBuilderLayout(formData, false);
		formLayout.setForm(formData);

		formLayout.addSaveListener(this::saveForm);
		Dialog dialog = new Dialog();
		dialog.add(formLayout);
		dialog.setHeaderTitle("Editing Form");
		dialog.setSizeFull();
		dialog.open();
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);
		dialog.setModal(true);
		dialog.setClassName("edit-form");
	}

	private void newForm(CampaignFormMetaDto formData) {

		FormBuilderLayout formLayout = new FormBuilderLayout(formData, true);
		formLayout.setForm(formData);

		formLayout.addSaveListener(this::saveForm);
		Dialog dialog = new Dialog();
		dialog.add(formLayout);
		dialog.setHeaderTitle("New Form");
		dialog.setSizeFull();
		dialog.open();
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);
		dialog.setModal(true);
		dialog.setClassName("new-form");
	}

	private void archiveFormPopup() {

		if (grid.getSelectedItems().size() < 1) {

			Notification notification = Notification.show("Please Select Forms to Archive");
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
		} else {
			confirmationPopup.setHeader("Archive Forms");

			confirmationPopup.setText("You are about to Archive " + grid.getSelectedItems().size() + " Forms");
			confirmationPopup.setCloseOnEsc(false);
			confirmationPopup.setCancelable(true);
			confirmationPopup.addCancelListener(e -> confirmationPopup.close());

			confirmationPopup.setRejectable(true);
			confirmationPopup.setRejectText("Cancel");
			confirmationPopup.addRejectListener(e -> confirmationPopup.close());

			confirmationPopup.setConfirmText("Archive");
			List<CampaignFormMetaDto> gridListConverted = new ArrayList<>(grid.getSelectedItems());
			confirmationPopup.addConfirmListener(e -> archiveForms(gridListConverted));
			confirmationPopup.open();
		}
	}

	private void dearchiveFormPopup() {

		if (grid.getSelectedItems().size() < 1) {

			Notification notification = Notification.show("Please Select Forms to Dearchive");
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
		} else {
			confirmationPopups.setHeader("Dearchive Forms");

			confirmationPopups.setText("You are about to dearchive " + grid.getSelectedItems().size() + " Forms");
			confirmationPopups.setCloseOnEsc(false);
			confirmationPopups.setCancelable(true);
			confirmationPopups.addCancelListener(e -> confirmationPopups.close());

			confirmationPopups.setRejectable(true);
			confirmationPopups.setRejectText("Cancel");
			confirmationPopups.addRejectListener(e -> confirmationPopups.close());

			confirmationPopups.setConfirmText("Dearchive");
			List<CampaignFormMetaDto> gridListConverted = new ArrayList<>(grid.getSelectedItems());
			confirmationPopups.addConfirmListener(e -> dearchiveForms(gridListConverted));
			confirmationPopups.open();
		}
	}

	private void dearchiveForms(List<CampaignFormMetaDto> selectedItems) {

		if (selectedItems.size() < 1) {
			Notification notification = Notification.show("Please Select Form to Dearchive");
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
		} else {

			List<String> uuids = selectedItems.stream().map(CampaignFormMetaDto::getUuid).collect(Collectors.toList());
			FacadeProvider.getCampaignFormMetaFacade().dearchiveForms(uuids);

			Notification notification = Notification.show("All Selected Form have been Dearchived");
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
			grid.setSelectionMode(Grid.SelectionMode.SINGLE);
//			filterDataProvider.refreshAll();
			refreshGridData();
			leaveBulkModeButton.setVisible(false);
			archiveForms.setVisible(false);
			dearchiveForms.setVisible(false);
			bulkModeButton.setVisible(true);
		}
	}

	private void archiveForms(List<CampaignFormMetaDto> selectedItems) {

		if (selectedItems.size() < 1) {

			Notification notification = Notification.show("Please Select Form to Archive");
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
		} else {

			List<String> uuids = selectedItems.stream().map(CampaignFormMetaDto::getUuid).collect(Collectors.toList());
			FacadeProvider.getCampaignFormMetaFacade().archiveForms(uuids);

			Notification notification = Notification.show("All Selected Form have been Archived");
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
			grid.setSelectionMode(Grid.SelectionMode.SINGLE);
//			filterDataProvider.refreshAll();
			refreshGridData();
			leaveBulkModeButton.setVisible(false);
			archiveForms.setVisible(false);
			dearchiveForms.setVisible(false);
			bulkModeButton.setVisible(true);
		}
	}

	private void saveForm(FormBuilderLayout.SaveEvent event) {
		FacadeProvider.getCampaignFormMetaFacade().saveCampaignFormMeta(event.getForm());
	}
	
	private void refreshGridData() {
		ListDataProvider<CampaignFormMetaDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getCampaignFormMetaFacade().getIndexList(criteria, null, null, null).stream());
		dataView = grid.setItems(dataProvider);
	}

}
