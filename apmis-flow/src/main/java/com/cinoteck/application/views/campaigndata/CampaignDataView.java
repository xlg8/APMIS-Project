package com.cinoteck.application.views.campaigndata;

import java.awt.Panel;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;



import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
//import com.vaadin.componentfactory.Popup;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.server.PWA;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("Campaign Data")
@Route(value = "campaigndata", layout = MainLayout.class)
public class CampaignDataView extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7588118851062483372L;

	private Grid<CampaignFormDataIndexDto> grid = new Grid<>(CampaignFormDataIndexDto.class, false);
//	private GridListDataView<CampaignFormDataIndexDto> dataView;
	private CampaignFormDataCriteria criteria;
	private CampaignFormMetaDto formMetaReference;

	ComboBox<String> campaignYear = new ComboBox<>();
	ComboBox<CampaignReferenceDto> campaignz = new ComboBox<>();
	ComboBox<CampaignPhase> campaignPhase = new ComboBox<>();
	ComboBox<CampaignFormMetaReferenceDto> campaignFormCombo = new ComboBox<>();
	ComboBox<AreaReferenceDto> regionCombo = new ComboBox<>();
	ComboBox<RegionReferenceDto> provinceCombo = new ComboBox<>();
	ComboBox<DistrictReferenceDto> districtCombo = new ComboBox<>();
	ComboBox<CommunityReferenceDto> clusterCombo = new ComboBox<>();
	ComboBox<CampaignFormElementImportance> importanceSwitcher = new ComboBox<>();
	Button resetHandler = new Button();
	Button applyHandler = new Button();
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;

	CampaignFormDataEditForm campaignFormDataEditForm;

	private DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider;

	public CampaignDataView() {
		setHeightFull();
		criteria = new CampaignFormDataCriteria();
		createCampaignDataFilter();
		configureGrid(criteria);
	}

	@SuppressWarnings("deprecation")
	private void configureGrid(CampaignFormDataCriteria criteria) {
		grid.removeAllColumns();
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(CampaignFormDataIndexDto.CAMPAIGN).setHeader("Campaign").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.FORM).setHeader("Form").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.AREA).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.RCODE).setHeader("RCode").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.REGION).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.PCODE).setHeader("PCode").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.DISTRICT).setHeader("District").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.DCODE).setHeader("DCode").setSortable(true).setResizable(true);
		Column<CampaignFormDataIndexDto> comm = grid.addColumn(CampaignFormDataIndexDto.COMMUNITY).setHeader("Cluster")
				.setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.COMMUNITYNUMBER).setHeader("Cluster Number").setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.CCODE).setHeader("CCode").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.FORM_DATE).setHeader("Form Date").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.FORM_TYPE).setHeader("Form Phase").setSortable(true).setResizable(true);

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setHeightFull();
		grid.setAllRowsVisible(false);

//		List<CampaignFormDataIndexDto> campaigns = FacadeProvider.getCampaignFormDataFacade()
//				.getIndexList(criteria, 1, null, null).stream().collect(Collectors.toList());
//
//		dataView = grid.setItems(campaigns);

		grid.asSingleSelect().addValueChangeListener(event -> editCampaignFormData(event.getValue()));

		dataProvider = DataProvider.fromFilteringCallbacks(this::fetchCampaignFormData, this::countCampaignFormData);
		grid.setDataProvider(dataProvider);
//
//		DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider = 
//				DataProvider
//				.fromFilteringCallbacks(
//						query -> FacadeProvider.getCampaignFormDataFacade()
//								.getIndexList(criteria, query.getOffset(), query.getLimit(),
//										query.getSortOrders().stream()
//												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
//														sortOrder.getDirection() == SortDirection.ASCENDING))
//												.collect(Collectors.toList()))
//								.stream(),
//						query -> (int) FacadeProvider.getCampaignFormDataFacade().count(criteria));

		GridExporter<CampaignFormDataIndexDto> exporter = GridExporter.createFor(grid);
//	    exporter.setExportValue(comm, item -> "" + item);
//	    exporter.setColumnPosition(lastNameCol, 1);
		exporter.setTitle("Campaign Data information");
		exporter.setFileName("GridExport" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		exporter.setCsvExportEnabled(true);

		add(grid);

	}

	private void editCampaignFormData(CampaignFormDataIndexDto selected) {
		selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			CampaignFormDataDto formData = FacadeProvider.getCampaignFormDataFacade()
					.getCampaignFormDataByUuid(selected.getUuid());
			openFormLayout(formData);
		}
	}

	private void openFormLayout(CampaignFormDataDto formData) {

		System.out.println(formData.getUuid() + "tttttttttttttttttttttttttttttttttttttttttttttttt");
		FormLayout formLayout = new FormLayout();
		String campaignUUID = "UUGEMB-KLKRIM-UILNND-3TZJ2F4Y";
		// Add fields from formData to the formLayout

		// Example: Assuming you have a field called "name" in the CampaignFormDataDto
		
		
		
		ComboBox<Object> cbCampaign = new ComboBox<>(CampaignFormDataDto.CAMPAIGN);
		cbCampaign.setItems(formData.getCampaign());
		cbCampaign.setValue(formData.getCampaign());
		cbCampaign.setEnabled(false);
		
		
		Date date = new Date();

		// Convert Date to LocalDate
		LocalDate localDate = formData.getFormDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		DatePicker formDate = new DatePicker();
		formDate.setValue(localDate);

		ComboBox<Object> cbArea = new ComboBox<>(CampaignFormDataDto.AREA);
		cbArea.setItems(formData.getArea());
		cbArea.setValue(formData.getArea());
//	        cbArea.setItems(FacadeProvider.getAreaFacade().getAllActiveAndSelectedAsReference(campaignUUID));

		ComboBox<Object> cbRegion = new ComboBox<>(CampaignFormDataDto.REGION);
		cbRegion.setItems(formData.getRegion());
		cbRegion.setValue(formData.getRegion());

		ComboBox<Object> cbDistrict = new ComboBox<>(CampaignFormDataDto.DISTRICT);
		cbDistrict.setItems(formData.getDistrict());
		cbDistrict.setValue(formData.getDistrict());

		ComboBox<Object> cbCommunity = new ComboBox<>(CampaignFormDataDto.COMMUNITY);
		cbCommunity.setItems(formData.getCommunity());
		cbCommunity.setValue(formData.getCommunity());

		formLayout.add(cbCampaign, formDate, cbArea, cbRegion, cbDistrict, cbCommunity);
		
//	    String jsonString = "{\"textProperty\":\"Hello\",\"dropdownProperty\":\"option2\",\"dropdownOptions\":[\"option1\",\"option2\",\"option3\"],\"yesNoProperty\":\"Yes\"}";
//        JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);
//
//        for (String propertyName : jsonObject.keys()) {
//            JsonValue propertyValue = jsonObject.get(propertyName);
//
//            if (propertyName.equals("dropdownProperty")) {
//                ComboBox<String> comboBox = new ComboBox<>(propertyName);
//                if (propertyValue instanceof JsonArray) {
//                    JsonArray dropdownOptions = (JsonArray) propertyValue;
//                    for (int i = 0; i < dropdownOptions.length(); i++) {
//                        comboBox.add(dropdownOptions.getString(i));
//                    }
//                }
//                comboBox.setValue(propertyValue.asString());
//                formLayout.add(comboBox);
//            } else if (propertyName.equals("yesNoProperty")) {
//                RadioButtonGroup<String> radioButtonGroup = new RadioButtonGroup<>(propertyName);
//                radioButtonGroup.setItems("Yes", "No");
//                radioButtonGroup.setValue(propertyValue.asString());
//                formLayout.add(radioButtonGroup);
//            } else {
//                TextField textField = new TextField(propertyName);
//                textField.setValue(propertyValue.asString());
//                formLayout.add(textField);
//            }
//        }

		// Open the formLayout in a dialog or another suitable way
		// Example: Using a Dialog
		Dialog dialog = new Dialog();
		dialog.add(formLayout);
		dialog.open();
	}

	private Stream<CampaignFormDataIndexDto> fetchCampaignFormData(
			Query<CampaignFormDataIndexDto, CampaignFormDataCriteria> query) {
		return FacadeProvider.getCampaignFormDataFacade()
				.getIndexList(criteria, query.getOffset(), query.getLimit(), query.getSortOrders().stream()
						.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
								sortOrder.getDirection() == SortDirection.ASCENDING))
						.collect(Collectors.toList()))
				.stream();
	}

	private int countCampaignFormData(Query<CampaignFormDataIndexDto, CampaignFormDataCriteria> query) {
		return (int) FacadeProvider.getCampaignFormDataFacade().count(criteria);
	}

	private void export(Grid<CampaignFormDataIndexDto> grid, TextArea result) {
		// Fetch all data from the grid in the current sorted order
		Stream<CampaignFormDataIndexDto> persons = null;
		Set<CampaignFormDataIndexDto> selection = grid.asMultiSelect().getValue();
		if (selection != null && selection.size() > 0) {
			persons = selection.stream();
		} else {
//			persons = dataView.getItems();
		}

		StringWriter output = new StringWriter();
		StatefulBeanToCsv<CampaignFormDataIndexDto> writer = new StatefulBeanToCsvBuilder<CampaignFormDataIndexDto>(
				output).build();
		try {
			writer.write(persons);
		} catch (Exception e) {
			output.write("An error occured during writing: " + e.getMessage());
		}

		result.setValue(output.toString());
	}

	private void createCampaignDataFilter() {
//		setMargin(true);

		Button newForm = new Button("NEW FORM");
		newForm.setId("push-me");
//		Popup popup = new Popup();
//		popup.setFor("push-me");
		Div text = new Div();
		text.setText("element 1");
		Div text2 = new Div();
		text2.setText("element 2");
//        popup.add(text, text2);
//        popup.setVisible(false);
		newForm.addClickListener(e -> {
//			openDialog(e);
//			if(!popup.isVisible()) {
//				popup.show();
//				popup.setVisible(true);
//			}else {
//				 popup.hide();	
//			}
//			
//		        if (popup.isOpened()) {
//		            popup.hide();
//		        } else {
//		            popup.show();
//		        }
		});
//	    popup.setOpened(true);

		Button importData = new Button("IMPORT", new Icon(VaadinIcon.PLUS_CIRCLE));
		Button exportData = new Button("EXPORT", new Icon(VaadinIcon.DOWNLOAD));
		TextArea resultField = new TextArea();
		resultField.setWidth("100%");
		exportData.addClickListener(

				e -> {
					this.export(grid, resultField);
				});

		campaignYear.setLabel("Campaign Year");
		List<CampaignReferenceDto> campaigns = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
		List<String> camYearList = campaigns.stream().map(CampaignReferenceDto::getCampaignYear).distinct()
				.collect(Collectors.toList());
		campaignYear.setItems(camYearList);

		List<CampaignFormMetaReferenceDto> campaignForms;
		campaignz.setLabel("Campaign");

		campaignPhase.setLabel("Campaign Phase");

		campaignFormCombo.setLabel("Form");
		campaignForms = FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferences();
		campaignFormCombo.setItems(campaignForms);

		regionCombo.setLabel("Region");
		regionCombo.setPlaceholder("Regions");

		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		regionCombo.setItems(regions);

		provinceCombo.setLabel("Province");
		provinceCombo.setPlaceholder("Provinces");
		provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
		provinceCombo.setItems(provinces);

		provinceCombo.getStyle().set("padding-top", "0px");
		provinceCombo.setClassName("col-sm-6, col-xs-6");

		districtCombo.setLabel("District");
		districtCombo.setPlaceholder("Districts");
		districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
		districtCombo.setItems(districts);

		districtCombo.getStyle().set("padding-top", "0px");
		districtCombo.setClassName("col-sm-6, col-xs-6");

		clusterCombo.setPlaceholder("Clusters");

		// TODO Importance filter switcher should be visible only on the change of form
		importanceSwitcher.setPlaceholder("Importance");
		importanceSwitcher.setItems(CampaignFormElementImportance.values());
		importanceSwitcher.setClearButtonVisible(true);
		importanceSwitcher.addValueChangeListener(e -> {

			if (formMetaReference != null) {
				grid.removeAllColumns();
				configureGrid(criteria);
//				Notification.show(formMetaReference.getUuid() + "yyyyyyyyyyyyyyyyy" + formMetaReference.getCaption());

				final boolean onlyImportantFormElements = e.getValue() == CampaignFormElementImportance.ALL;
				Notification.show(onlyImportantFormElements + "ttttttttttttttttt");
				final List<CampaignFormElement> campaignFormElements = formMetaReference.getCampaignFormElements();
				for (CampaignFormElement element : campaignFormElements) {
					if (element.isImportant() || !onlyImportantFormElements) {
						String caption = null;
//						if (translations != null) {
//							caption = translations.getTranslations().stream()
//									.filter(t -> t.getElementId().equals(element.getId()))
//									.map(TranslationElement::getCaption).findFirst().orElse(null);
//						}
						if (caption == null) {
							caption = element.getCaption();
						}

						if (caption != null) {
							addCustomColumn(element.getId(), caption);
//							executeJavaScript();
						}
					}
				}
			}

		});
		// Configure Comboboxes Value Change Listeners

		campaignYear.addValueChangeListener(e -> {
			campaignz.clear();
			List<CampaignReferenceDto> stf = campaigns.stream()
					.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());
			campaignz.setItems(stf);
			campaignz.setValue(stf.get(0));
		});

		campaignz.addValueChangeListener(e -> {
			campaignPhase.setItems(CampaignPhase.values());

//			dataView.addFilter(t -> t.getCampaign().toString().equalsIgnoreCase(campaignz.getValue().toString()));
		});

		campaignPhase.addValueChangeListener(e -> {
//			dataView.addFilter(t -> t.getFormType().toString()
//					.equalsIgnoreCase(campaignPhase.getValue().toString().toLowerCase()));
//			
//			fillNewFormDropdown(newFormPanel);
//			newFormButton.setEnabled(true);
//			criteria.setFormPhase(e.getValue());
		});

		campaignFormCombo.addValueChangeListener(e -> {
//			dataView.addFilter(t -> t.getForm().toString().equalsIgnoreCase(campaignFormCombo.getValue().toString()));

			formMetaReference = FacadeProvider.getCampaignFormMetaFacade()
					.getCampaignFormMetaByUuid(e.getValue().getUuid());

			criteria.setCampaignFormMeta(campaignFormCombo.getValue());

		});

		regionCombo.addValueChangeListener(e -> {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
			provinceCombo.setItems(provinces);
//			dataView.addFilter(t -> t.getArea().toString().equalsIgnoreCase(regionCombo.getValue().toString()));
		});

		provinceCombo.addValueChangeListener(e -> {
			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
			districtCombo.setItems(districts);
//			dataView.addFilter(t -> t.getRegion().toString().equalsIgnoreCase(provinceCombo.getValue().toString()));
		});

		districtCombo.addValueChangeListener(e -> {
			communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
			clusterCombo.setItems(communities);
//			dataView.addFilter(t -> t.getDistrict().toString().equalsIgnoreCase(districtCombo.getValue().toString()));
		});

		clusterCombo.addValueChangeListener(e -> {
//			dataView.addFilter(t -> t.getCommunity().toString().equalsIgnoreCase(clusterCombo.getValue().toString()));
		});

		resetHandler.setText("Reset Filters");

		applyHandler.setText("Apply Filters");

		HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(Alignment.END);

		HorizontalLayout level1Filters = new HorizontalLayout();
		level1Filters.setPadding(false);
		level1Filters.setVisible(false);
		level1Filters.getStyle().set("margin-left", "12px");
		level1Filters.setAlignItems(Alignment.END);
		level1Filters.add(campaignFormCombo, regionCombo, provinceCombo, districtCombo, clusterCombo,
				importanceSwitcher, resetHandler, applyHandler);

		HorizontalLayout actionButtonlayout = new HorizontalLayout();
		actionButtonlayout.setVisible(false);
		actionButtonlayout.setAlignItems(Alignment.END);
		actionButtonlayout.add(campaignYear, campaignz, campaignPhase, newForm, importData, exportData);

		VerticalLayout filterBlock = new VerticalLayout();

		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
		displayFilters.addClickListener(e -> {
			if (!level1Filters.isVisible()) {
				actionButtonlayout.setVisible(true);
				level1Filters.setVisible(true);
				displayFilters.setText("Hide Filters");
			} else {
				actionButtonlayout.setVisible(false);
				level1Filters.setVisible(false);
				displayFilters.setText("Show Filters");
			}
		});

		layout.add(displayFilters, actionButtonlayout);

		filterBlock.add(layout, level1Filters);

		add(filterBlock);
	}

	private void setDataProvider() {
		DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider = DataProvider
				.fromFilteringCallbacks(
						query -> FacadeProvider.getCampaignFormDataFacade()
								.getIndexList(criteria, query.getOffset(), query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()))
								.stream(),
						query -> (int) FacadeProvider.getCampaignFormDataFacade().count(criteria));
		grid.setDataProvider(dataProvider);
	}

	private void fillNewFormDropdown(Panel containerPanel) {

		CampaignReferenceDto campaignReferenceDto = campaignz.getValue();
		String phase = campaignPhase.getValue().toString();
		Set<FormAccess> userFormAccess = UserProvider.getCurrent().getFormAccess();

		CampaignDto campaignDto = FacadeProvider.getCampaignFacade().getByUuid(campaignReferenceDto.getUuid());

		containerPanel.removeAll();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String stringDate = dateFormat.format(campaignDto.getStartDate());

		LocalDate date1 = LocalDate.parse(stringDate);
		LocalDate date2 = LocalDate.now();
		int days = Period.between(date1, date2).getDays();

		if (phase != null && campaignReferenceDto != null) {

			List<CampaignFormMetaReferenceDto> campaignFormReferences = FacadeProvider.getCampaignFormMetaFacade()
					.getAllCampaignFormMetasAsReferencesByRoundandCampaign(phase.toLowerCase(),
							campaignReferenceDto.getUuid());

			Collections.sort(campaignFormReferences);

			Div containerContent = new Div();
			containerContent.setWidthFull();

			for (CampaignFormMetaReferenceDto campaignForm : campaignFormReferences) {

				int isShown = days - campaignForm.getDaysExpired();
				boolean hideFromList = isShown < 0;

				Button campaignFormButton = new Button(campaignForm.toString());
				campaignFormButton.addClickListener(e -> {
					if (!hideFromList) {
//	                    ControllerProvider.getCampaignController().navigateToFormDataView(campaignReferenceDto.getUuid(),
//	                            campaignForm.getUuid());
//	                    newFormButton.setOpened(false);
					}
				});

				campaignFormButton.setWidthFull();
				campaignFormButton.getStyle().remove("margin-bottom");
				campaignFormButton.getStyle().set("text-transform", "lowercase");

				if (!hideFromList) {
					campaignFormButton.addClickListener(e -> {
						Notification notf = new Notification(
								campaignForm.getCaption() + " is now closed for data entry");
						notf.setPosition(Position.TOP_END);
//	                    notf.setDuration(Duration.ofSeconds(3));
						notf.open();
					});
				}

				containerContent.add(campaignFormButton);
			}

			add(containerContent);

			if (campaignFormReferences.size() >= 10) {
//	            containerPanel.setHeight("400px");
//	            containerPanel.setWidth(containerContent.getWidth() + 20.0f, Unit.PIXELS);
			} else {
//	            containerPanel.setHeight(null);
//	            containerPanel.setWidth(containerContent.getWidth(), Unit.PIXELS);
			}
		}
	}

	public void addCustomColumn(String property, String caption) {
		if (!property.toString().contains("readonly")) {

			grid.addColumn(
					e -> e.getFormValues().stream().filter(v -> v.getId().equals(property)).findFirst().orElse(null))
					.setHeader(caption).setSortable(true).setResizable(true);

		}

	}
//
//	public void editCampaignFormData() {
//		campaignFormDataEditForm = new CampaignFormDataEditForm();
////			campaignFormDataEditForm. .setUser(contact);
//		campaignFormDataEditForm.setVisible(true);
//		campaignFormDataEditForm.setSizeFull();
//		grid.setVisible(false);
////			setFiltersVisible(false);
//
//	}

	private void closeEditor() {
//		campaignFormDataEditForm.setUser(null);
		campaignFormDataEditForm.setVisible(false);
//		setFiltersVisible(true);
		grid.setVisible(true);
		removeClassName("editing");
	}

}
