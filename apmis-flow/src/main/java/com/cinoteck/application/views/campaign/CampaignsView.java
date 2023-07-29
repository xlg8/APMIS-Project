package com.cinoteck.application.views.campaign;

import java.util.Collections;
import java.util.List;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap.Column;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignCriteria;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.utils.ValidationRuntimeException;

@PageTitle("APMIS - All Campaigns")
@Route(value = "campaign", layout = MainLayout.class)
public class CampaignsView extends VerticalLayout {

	private Button filterDisplayToggle;
	private Button validateFormsButton;
	private Button createButton;
	private TextField searchField;
	private ComboBox<EntityRelevanceStatus> relevanceStatusFilter;
	VerticalLayout campaignsFilterLayout = new VerticalLayout();

//	private Grid<CampaignDto> grid = new Grid<>(CampaignDto.class, false);
//	private GridListDataView<CampaignDto> dataView;

	private Grid<CampaignIndexDto> grid = new Grid<>(CampaignIndexDto.class, false);
	private GridListDataView<CampaignIndexDto> dataView;
	List<CampaignIndexDto> campaigns;

	CampaignCriteria criteria = new CampaignCriteria();
	List<CampaignIndexDto> indexList = FacadeProvider.getCampaignFacade().getIndexList(criteria, null, null, null);
	ListDataProvider<CampaignIndexDto> indexDataProvider = new ListDataProvider<>(indexList);

	private CampaignForm campaignForm;
	CampaignDto dto;
	private List<CampaignReferenceDto> campaignName, campaignRound, campaignStartDate, campaignEndDate,
			campaignDescription;

	public CampaignsView() {

		setSpacing(false);
		setHeightFull();
		createFilterBar();
		campaignsGrid();

	}

	private boolean matchesTerm() {
		return false;
	}

	private void campaignsGrid() {

		criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);

		this.criteria = new CampaignCriteria();
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		grid.addColumn(CampaignIndexDto.NAME).setHeader("Name").setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.CAMPAIGN_STATUS).setHeader("Status").setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.START_DATE).setHeader("Start Date").setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.END_DATE).setHeader("End Date").setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.CAMPAIGN_YEAR).setHeader("Campaign Year").setSortable(true).setResizable(true);

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setAllRowsVisible(true);

		ListDataProvider<CampaignIndexDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getCampaignFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);
		grid.asSingleSelect().addValueChangeListener(event -> editCampaign(event.getValue()));
		add(grid);
	}

	private void refreshGridData() {
		ListDataProvider<CampaignIndexDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getCampaignFacade().getIndexList(criteria, null, null, null).stream());
		dataView = grid.setItems(dataProvider);
	}

	private Component createStatusComponent(CampaignDto item) {

		CampaignIndexDto indexDto = indexDataProvider.getItems().stream()
				.filter(index -> index.getCampaignStatus().equals(item.getCampaignStatus())).findFirst().orElse(null);

		String statusText = indexDto != null ? indexDto.getCampaignStatus() : "";
		Label statusLabel = new Label(statusText);

		return statusLabel;
	}

	private void createFilterBar() {
		HorizontalLayout filterToggleLayout = new HorizontalLayout();
		filterToggleLayout.setAlignItems(Alignment.END);

		filterDisplayToggle = new Button("Show Filters");
		filterDisplayToggle.getStyle().set("margin-left", "12px");
		filterDisplayToggle.getStyle().set("margin-top", "12px");
		filterDisplayToggle.setIcon(new Icon(VaadinIcon.SLIDERS));

		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.getStyle().set("margin-left", "12px");
		filterLayout.setVisible(false);

		filterDisplayToggle.addClickListener(e -> {
			if (!filterLayout.isVisible()) {
				filterLayout.setVisible(true);
				filterDisplayToggle.setText("Hide Filters");

			} else {
				filterLayout.setVisible(false);
				filterDisplayToggle.setText("Show Filters");
			}

		});

		searchField = new TextField();
		searchField.setLabel("Search Campaign");
		searchField.setPlaceholder("Search");
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setClassName("col-sm-6, col-xs-6");

		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> dataView.addFilter(search -> {
			String searchTerm = searchField.getValue().trim();
			if (searchTerm.isEmpty())
				return true;
			boolean matchesDistrictName = String.valueOf(search.getName()).toLowerCase()
					.contains(searchTerm.toLowerCase());
			return matchesDistrictName;
		}));

		relevanceStatusFilter = new ComboBox<EntityRelevanceStatus>();
		relevanceStatusFilter.setLabel("Campaign Status");
		relevanceStatusFilter.setItems((EntityRelevanceStatus[]) EntityRelevanceStatus.values());
		relevanceStatusFilter.setClearButtonVisible(true);
		relevanceStatusFilter.setClassName("col-sm-6, col-xs-6");

		relevanceStatusFilter.addValueChangeListener(e -> {

			criteria.relevanceStatus(e.getValue()); // Set the selected relevance status in the criteria object
			refreshGridData();

		});

		validateFormsButton = new Button("Validate Forms", new Icon(VaadinIcon.CHECK_CIRCLE));
		validateFormsButton.setClassName("col-sm-6, col-xs-6");
		validateFormsButton.addClickListener(e -> {
			try {
				FacadeProvider.getCampaignFormMetaFacade().validateAllFormMetas();
				Notification.show(I18nProperties.getString(Strings.messageAllCampaignFormsValid), 3000,
						Position.TOP_CENTER);
			} catch (ValidationRuntimeException ee) {

				Notification.show(I18nProperties.getString(Strings.messageAllCampaignFormsNotValid), 8000,
						Position.MIDDLE);

			}

		});

		createButton = new Button("New Campaign", new Icon(VaadinIcon.PLUS_CIRCLE));
		createButton.setClassName("col-sm-6, col-xs-6");
		createButton.addClickListener(e -> {
			newCampaign(dto);
		});
		filterLayout.add(searchField, relevanceStatusFilter);
		filterToggleLayout.add(filterDisplayToggle, filterLayout, validateFormsButton, createButton);
		filterToggleLayout.setClassName("row pl-3");
		campaignsFilterLayout.add(filterToggleLayout);

		add(campaignsFilterLayout);
	}

	private void editCampaign(CampaignIndexDto selected) {
		selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			CampaignDto formData = FacadeProvider.getCampaignFacade().getByUuid(selected.getUuid());
			openFormLayout(formData);
		}
	}

	private void newCampaign(CampaignDto formData) {
		CampaignForm formLayout = new CampaignForm(formData);
		formLayout.addSaveListener(this::saveCampaign);
		formLayout.addOpenCloseListener(this::openCloseCampaign);
//		formLayout.addArchiveListener(this::archiveCampaign);
		Dialog dialog = new Dialog();
		dialog.add(formLayout);
		dialog.setHeaderTitle("New Campaign");
		dialog.setSizeFull();
		dialog.open();
		dialog.setDraggable(true);
		dialog.setResizable(true);
	}

	private void openFormLayout(CampaignDto formData) {
		Dialog dialog = new Dialog();
		CampaignForm formLayout = new CampaignForm(formData);
		formLayout.setCampaign(formData);
		formLayout.addSaveListener(this::saveCampaign);
		formLayout.addArchiveListener(this::archiveDearchiveCampaign);
		formLayout.addPublishListener(this::publishUnpublishCampaign);
		formLayout.addOpenCloseListener(this::openCloseCampaign);
		formLayout.addDeleteListener(this::deleteCampaign);
		formLayout.addDuplicateListener(this::duplicateCampaign);
		dialog.add(formLayout);
		dialog.setHeaderTitle("Edit Campaign");
		dialog.setSizeFull();
		dialog.open();
		dialog.setDraggable(true);
		dialog.setResizable(true);
	}

	private void setFiltersVisible(boolean state) {
		filterDisplayToggle.setVisible(state);
		createButton.setVisible(state);
		validateFormsButton.setVisible(state);
		relevanceStatusFilter.setVisible(state);
		searchField.setVisible(state);
	}

	private void saveCampaign(CampaignForm.SaveEvent event) {
		FacadeProvider.getCampaignFacade().saveCampaign(event.getCampaign());
	}

	private void archiveDearchiveCampaign(CampaignForm.ArchiveEvent event) {
		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setCancelable(true);
		dialog.addCancelListener(e -> dialog.close());
		dialog.setRejectable(true);
		dialog.setRejectText("No");
		dialog.addRejectListener(e -> dialog.close());
		dialog.setConfirmText("Yes");
		dialog.open();
		CampaignForm formLayout = (CampaignForm) event.getSource();
		boolean isArchived = FacadeProvider.getCampaignFacade().isArchived(event.getCampaign().getUuid());

		if (isArchived == true) {

			dialog.setHeader("De-Archive Campaign");
			dialog.setText(
					"Are you sure you want to de-archive this campaign? This will make it appear in the normal campaign directory again.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().archiveOrDearchiveCampaign(event.getCampaign().getUuid(), false);
				formLayout.getChildren().forEach(child -> child.getElement().executeJs("this.requestLayout()"));
			});

		} else {
			dialog.setHeader("Archive Campaign");
			dialog.setText(
					"Are you sure you want to Archive this campaign? This will make it appear in the normal campaign directory again.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().archiveOrDearchiveCampaign(event.getCampaign().getUuid(), true);
				formLayout.getChildren().forEach(child -> child.getElement().executeJs("this.requestLayout()"));
			});

		}
		formLayout.getChildren().forEach(child -> child.getElement().executeJs("this.requestLayout()"));
	}

	private void publishUnpublishCampaign(CampaignForm.PublishUnpublishEvent event) {
		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setCancelable(true);
		dialog.addCancelListener(e -> dialog.close());
		dialog.setRejectable(true);
		dialog.setRejectText("No");
		dialog.addRejectListener(e -> dialog.close());
		dialog.setConfirmText("Yes");
		dialog.open();
		CampaignForm formLayout = (CampaignForm) event.getSource();
		boolean isPublished = FacadeProvider.getCampaignFacade().isPublished(event.getCampaign().getUuid());

		if (isPublished == true) {

			dialog.setHeader("Publish Campaign");
			dialog.setText(
					"Are you sure you want to Un-Publish this campaign? This will make it appear in the normal campaign directory again.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().publishandUnPublishCampaign(event.getCampaign().getUuid(), false);
				formLayout.getChildren().forEach(child -> child.getElement().executeJs("this.requestLayout()"));
			});

		} else {
			dialog.setHeader("Un-Publish Campaign");
			dialog.setText(
					"Are you sure you want to Publish this campaign? This will make it appear in the normal campaign directory again.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().publishandUnPublishCampaign(event.getCampaign().getUuid(), true);
				formLayout.getChildren().forEach(child -> child.getElement().executeJs("this.requestLayout()"));
			});

		}
		formLayout.getChildren().forEach(child -> child.getElement().executeJs("this.requestLayout()"));
	}

	private void openCloseCampaign(CampaignForm.OpenCloseEvent event) {
		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setCancelable(true);
		dialog.addCancelListener(e -> dialog.close());
		dialog.setRejectable(true);
		dialog.setRejectText("No");
		dialog.addRejectListener(e -> dialog.close());
		dialog.setConfirmText("Yes");
		dialog.open();
		CampaignForm formLayout = (CampaignForm) event.getSource();
		boolean isOpened = FacadeProvider.getCampaignFacade().isClosedd(event.getCampaign().getUuid());

		if (isOpened == true) {

			dialog.setHeader("Open Campaign");
			dialog.setText(
					"Are you sure you want to Open this campaign? This will make it appear in the normal campaign directory again.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().closeandOpenCampaign(event.getCampaign().getUuid(), false);
				formLayout.getChildren().forEach(child -> child.getElement().executeJs("this.requestLayout()"));
			});

		} else {
			dialog.setHeader("Close Campaign");
			dialog.setText(
					"Are you sure you want to Close this campaign? This will make it appear in the normal campaign directory again.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().closeandOpenCampaign(event.getCampaign().getUuid(), true);
				formLayout.getChildren().forEach(child -> child.getElement().executeJs("this.requestLayout()"));
			});

		}
		formLayout.getChildren().forEach(child -> child.getElement().executeJs("this.requestLayout()"));
	}

	private void deleteCampaign(CampaignForm.DeleteEvent event) {
		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setCancelable(true);
		dialog.addCancelListener(e -> dialog.close());
		dialog.setRejectable(true);
		dialog.setRejectText("No");
		dialog.addRejectListener(e -> dialog.close());
		dialog.setConfirmText("Yes");
		dialog.open();
//		CampaignForm formLayout = (CampaignForm) event.getSource();
//	    boolean isOpened = FacadeProvider.getCampaignFacade().isClosedd(event.getCampaign().getUuid());
		dialog.setHeader("Delete Campaign");
		dialog.setText(
				"Are you sure you want to Delete this campaign? This will make it appear in the normal campaign directory again.");
		dialog.addConfirmListener(e -> {
			FacadeProvider.getCampaignFacade().deleteCampaign(event.getCampaign().getUuid());
			UI.getCurrent().getPage().reload();
		});

	}
	
	private void duplicateCampaign(CampaignForm.DuplicateEvent event) {
		UserProvider usr = new UserProvider();
		ConfirmDialog dialog = new ConfirmDialog();
		dialog.setCancelable(true);
		dialog.addCancelListener(e -> dialog.close());
		dialog.setRejectable(true);
		dialog.setRejectText("No");
		dialog.addRejectListener(e -> dialog.close());
		dialog.setConfirmText("Yes");
		dialog.open();
//		CampaignForm formLayout = (CampaignForm) event.getSource();
//	    boolean isOpened = FacadeProvider.getCampaignFacade().isClosedd(event.getCampaign().getUuid());
		dialog.setHeader("Delete Campaign");
		dialog.setText(
				"Are you sure you want to Clone this campaign? This will make it appear in the normal campaign directory again.");
		dialog.addConfirmListener(e -> {
			FacadeProvider.getCampaignFacade().cloneCampaign(event.getCampaign().getUuid(), usr.getUser().getUserName());
			UI.getCurrent().getPage().reload();
		});

	}
}
