package com.cinoteck.application.views.campaign;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.cinoteck.application.LanguageSwitcher;
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
import com.vaadin.flow.component.html.Span;
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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap.Column;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.CampaignCriteria;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.user.UserDto;
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

	UserProvider userProvider = new UserProvider();
	public CampaignsView() {
		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);			
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
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


//		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//		
////		LocalDateTimeRenderer<CampaignIndexDto> dateRenderer = new LocalDateTimeRenderer<>(CampaignIndexDto.START_DATE, dateFormatter.format);
//		TextRenderer<CampaignIndexDto> dateRenderer = new TextRenderer<>(item -> item.getStartDate().getDate().format(dateFormatter));
		
//		 DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//		    TextRenderer<CampaignIndexDto> dateRenderer = new TextRenderer<>(item -> {
//		        Date startDate = (Date) item.getStartDate(); // Assuming getStartDate() returns a java.util.Date
//		        LocalDate localDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//		        return localDate.format(dateFormatter);
//		    });
		    
		    ComponentRenderer<Span, CampaignIndexDto> startDateRenderer = new ComponentRenderer<>(reportModelDto -> {
				String value = String.valueOf(reportModelDto.getStartDate()).replace("00:00:00.0", "");
				Span label = new Span(value);
				label.getStyle().set("color", "var(--lumo-body-text-color) !important");
				return label;
			});
		    
		    ComponentRenderer<Span, CampaignIndexDto> endDateRenderer = new ComponentRenderer<>(reportModelDto -> {
				String value = String.valueOf(reportModelDto.getEndDate()).replace("00:00:00.0", "");
				Span label = new Span(value);
				label.getStyle().set("color", "var(--lumo-body-text-color) !important");
				return label;
			});


		this.criteria = new CampaignCriteria();
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		grid.addColumn(CampaignIndexDto.NAME).setHeader(I18nProperties.getCaption(Captions.name)).setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.CAMPAIGN_STATUS).setHeader(I18nProperties.getCaption(Captions.actionStatusChangeDate)).setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.START_DATE).setHeader(I18nProperties.getCaption(Captions.Campaign_startDate)).setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.END_DATE).setHeader(I18nProperties.getCaption(Captions.Campaign_endDate)).setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.CAMPAIGN_YEAR).setHeader(I18nProperties.getCaption(Captions.campaignYear)).setSortable(true).setResizable(true);


		grid.setVisible(true);
		grid.setWidthFull();
		grid.setAllRowsVisible(true);
		criteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
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
		
		

		filterDisplayToggle = new Button(I18nProperties.getCaption(Captions.showFilters));
		filterDisplayToggle.getStyle().set("margin-left", "12px");
		filterDisplayToggle.getStyle().set("margin-top", "12px");
		filterDisplayToggle.setIcon(new Icon(VaadinIcon.SLIDERS));

		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.getStyle().set("margin-left", "12px");
		filterLayout.setVisible(false);

		filterDisplayToggle.addClickListener(e -> {
			if (!filterLayout.isVisible()) {
				filterLayout.setVisible(true);
				filterDisplayToggle.setText(I18nProperties.getCaption(Captions.hideFilters));

			} else {
				filterLayout.setVisible(false);
				filterDisplayToggle.setText(I18nProperties.getCaption(Captions.showFilters));
			}

		});

		searchField = new TextField();
		searchField.setLabel(I18nProperties.getCaption(Captions.campaignSearch));
		searchField.setPlaceholder(I18nProperties.getCaption(Captions.actionSearch));
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
		relevanceStatusFilter.setLabel(I18nProperties.getCaption(Captions.campaignStatus));
		relevanceStatusFilter.setItems((EntityRelevanceStatus[]) EntityRelevanceStatus.values());
		relevanceStatusFilter.setClearButtonVisible(true);
		relevanceStatusFilter.setClassName("col-sm-6, col-xs-6");

		relevanceStatusFilter.addValueChangeListener(e -> {

			criteria.relevanceStatus(e.getValue()); // Set the selected relevance status in the criteria object
			refreshGridData();

		});

		validateFormsButton = new Button(I18nProperties.getCaption(Captions.campaignValidateForms), new Icon(VaadinIcon.CHECK_CIRCLE));
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

		createButton = new Button(I18nProperties.getCaption(Captions.campaignNewCampaign), new Icon(VaadinIcon.PLUS_CIRCLE));
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
		dialog.setHeaderTitle(I18nProperties.getCaption(Captions.campaignNewCampaign));

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
		dialog.setHeaderTitle(I18nProperties.getCaption(Captions.Campaign_edit));
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

		CampaignForm formLayout = (CampaignForm) event.getSource();

		boolean isArchived = FacadeProvider.getCampaignFacade().isArchived(event.getCampaign().getUuid());

		if (isArchived) {
//			formLayout.archiveDearchive.setText("De-archive");
			dialog.setHeader("De-Archive Campaign");
			dialog.setText(
					"Are you sure you want to de-archive this campaign? This will make it appear in the active campaign directory again.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().archiveOrDearchiveCampaign(event.getCampaign().getUuid(), false);
				formLayout.updateArchiveButtonText(false);
			});

		} else {
//			formLayout.archiveDearchive.setText("Archive");

			dialog.setHeader("Archive Campaign");
			dialog.setText(
					"Are you sure you want to Archive this campaign? This will make it not appear in the normal campaign directory again.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().archiveOrDearchiveCampaign(event.getCampaign().getUuid(), true);
				formLayout.updateArchiveButtonText(true);
			});

		}
		dialog.open();
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

		if (isPublished) {

			dialog.setHeader("Publish Campaign");
			dialog.setText(
					"Are you sure you want to Publish Report from this campaign? This will make the Report Available in Aggregate Reports.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().publishandUnPublishCampaign(event.getCampaign().getUuid(), false);
				formLayout.updatePublishButtonText(false);
			});

		} else {
			dialog.setHeader("Un-Publish Campaign");
			dialog.setText(
					"Are you sure you want to Un-Publish Report from this campaign? This will make the Report Un-Available in Aggregate Reports.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().publishandUnPublishCampaign(event.getCampaign().getUuid(), true);
				formLayout.updatePublishButtonText(true);
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

		if (isOpened) {

			dialog.setHeader("Open Campaign");
			dialog.setText(
					"Are you sure you want to Open this campaign? This will make this campaign status Open.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().closeandOpenCampaign(event.getCampaign().getUuid(), false);
				formLayout.updateOpenCloseButtonText(false);
			});

		} else {
			dialog.setHeader("Close Campaign");
			dialog.setText(
					"Are you sure you want to Close this campaign?  This will make this campaign status Closed.");
			dialog.addConfirmListener(e -> {
				FacadeProvider.getCampaignFacade().closeandOpenCampaign(event.getCampaign().getUuid(), true);
				formLayout.updateOpenCloseButtonText(true);
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
				"Are you sure you want to Delete this campaign? This will make it Un-Available in the normal campaign directory again.");
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
		dialog.setHeader("Duplicate Campaign");
		dialog.setText(
				"Are you sure you want to Clone this campaign? .");
		dialog.addConfirmListener(e -> {
			FacadeProvider.getCampaignFacade().cloneCampaign(event.getCampaign().getUuid(),
					usr.getUser().getUserName());
			UI.getCurrent().getPage().reload();
		});

	}
}
