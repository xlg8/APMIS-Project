package com.cinoteck.application.views.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.NewItemHandler;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.diagram.CampaignDashboardElement;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDefinitionDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;

public class CampaignDashboardGridElementComponent extends AbstractEditableGrid<CampaignDashboardElement> {

	public static String formPhase;

	public String getFormPhase() {
		return formPhase;
	}

	public void setFormPhase(String formPhase) {
		this.formPhase = formPhase;
	}

	public CampaignDashboardGridElementComponent(List<CampaignDashboardElement> savedElements,
			List<CampaignDashboardElement> allElements) {
		super(savedElements, allElements);
		setWidthFull();
//		addColumnsBinder(allElements);
	}

	protected Binder<CampaignDashboardElement> addColumnsBinder(List<CampaignDashboardElement> allElements) {

		final String formTy = allElements.isEmpty() ? "takes away null pointer" : allElements.get(0).getPhase();

		Binder<CampaignDashboardElement> binder = new Binder<>();

		final List<CampaignDiagramDefinitionDto> campaignDiagramDefinitionDtos = FacadeProvider
				.getCampaignDiagramDefinitionFacade().getAll().stream()
				.filter(e -> e.getFormType().equalsIgnoreCase(formTy)).collect(Collectors.toList());

		final Map<String, String> diagramIdCaptionMap = campaignDiagramDefinitionDtos.stream().collect(Collectors
				.toMap(CampaignDiagramDefinitionDto::getDiagramId, CampaignDiagramDefinitionDto::getDiagramCaption));

//		 final Map<String, String> diagramIdCaptionMapPhase =
//		 campaignDiagramDefinitionDtos.stream()
//		 .collect(Collectors.toMap(CampaignDiagramDefinitionDto::getDiagramId,
//		 CampaignDiagramDefinitionDto::getFormType));

		ComboBox<String> diagramIdCaptionCombo = new ComboBox<>(Captions.campaignDashboardChart,
				diagramIdCaptionMap.keySet());
		diagramIdCaptionCombo.setItemLabelGenerator(diagramId -> diagramIdCaptionMap.get(diagramId));
//		diagramIdCaptionCombo.setEmptySelectionAllowed(false);

		Binder.Binding<CampaignDashboardElement, String> diagramIdCaptionBind = binder.bind(diagramIdCaptionCombo,
				CampaignDashboardElement::getDiagramId, CampaignDashboardElement::setDiagramId);
		/*
		 * cde -> new DiagramIdCaption(cde.getDiagramId(),entityCampaignDataForm
		 * diagramIdCaptionMap.get(cde.getDiagramId())), (campaignDashboardElement,
		 * diagramIdCaption) -> {
		 * campaignDashboardElement.setDiagramId(diagramIdCaption.getDiagramId()); });
		 */

		final Grid.Column<CampaignDashboardElement> diagramIdColumn = grid
				.addColumn(campaignDashboardElement -> diagramIdCaptionMap.get(campaignDashboardElement.getDiagramId()))
				.setHeader(I18nProperties.getCaption(Captions.campaignDashboardChart))
				.setEditorComponent(diagramIdCaptionCombo);

		final List<String> existingTabIds = allElements.stream().map(e -> e.getTabId())
				.filter(s -> StringUtils.isNotEmpty(s)).distinct().collect(Collectors.toList());

		// System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		// "+existingTabIds.toString());

		final ComboBox<String> tabIdCombo = new ComboBox<>(Captions.campaignDashboardTabName);
		tabIdCombo.setItems(existingTabIds);
		tabIdCombo.setAllowCustomValue(true);
		tabIdCombo.setPreventInvalidInput(true);
		tabIdCombo.addValueChangeListener(event -> {
			String newValue = event.getValue();
			if (newValue != null && !existingTabIds.contains(newValue)) {
				tabIdCombo.setItems(existingTabIds);
			}
		});
		tabIdCombo.addValueChangeListener(event -> {
		    String newValue = event.getValue();
		    if (newValue != null && !existingTabIds.contains(newValue)) {
		        existingTabIds.add(newValue);
		        tabIdCombo.setItems(existingTabIds);
		    }
		});
		tabIdCombo.setClearButtonVisible(true);
		tabIdCombo.setRequired(true);
		tabIdCombo.setErrorMessage("Please select a valid option or enter a new one.");

		final Binder.Binding<CampaignDashboardElement, String> tabIdBind = binder.bind(tabIdCombo,
				CampaignDashboardElement::getTabId, CampaignDashboardElement::setTabId);

		List<String> itemsList = new ArrayList<>();
		ListDataProvider<String> dataProvider = new ListDataProvider<>(itemsList); // Create a ListDataProvider
		tabIdCombo.setDataProvider(dataProvider); // Set the data provider to the ComboBox
		tabIdCombo.addCustomValueSetListener(event -> {
			String newItem = event.getDetail(); // Retrieve the new item entered by the user
			itemsList.add(newItem); // Add the new item to the list
			dataProvider.refreshAll(); // Refresh the data provider to reflect the changes in the ComboBox
		});
//		tabIdCombo.setEmptySelectionAllowed(false);
//		tabIdCombo.setTextInputAllowed(true);
//		tabIdCombo.setNewItemProvider((ComboBox.NewItemProvider<String>) s -> Optional.of(s));
//		tabIdCombo.setNewItemSupplier(s -> Optional.of(s));

		

		grid.addColumn(campaignDashboardElement -> campaignDashboardElement.getTabId())
				.setHeader(I18nProperties.getCaption(Captions.campaignDashboardTabName));
//		tabIdColumn.setEditorBinding(tabIdBind);

		final List<String> existingSubTabIds = allElements.stream()
				.map(campaignDiagramDefinitionDto -> campaignDiagramDefinitionDto.getSubTabId())
				.filter(s -> StringUtils.isNotEmpty(s)).distinct().collect(Collectors.toList());

		final ComboBox<String> subTabIdCombo = new ComboBox<>(Captions.campaignDashboardSubTabName, existingSubTabIds);
		List<String> itemsListt = new ArrayList<>();
		ListDataProvider<String> dataProviderr = new ListDataProvider<>(itemsListt); // Create a ListDataProvider
		subTabIdCombo.setDataProvider(dataProviderr); // Set the data provider to the ComboBox
		subTabIdCombo.addCustomValueSetListener(event -> {
			String newItem = event.getDetail(); // Retrieve the new item entered by the user
			itemsList.add(newItem); // Add the new item to the list
			dataProvider.refreshAll(); // Refresh the data provider to reflect the changes in the ComboBox
		});

		final Binder.Binding<CampaignDashboardElement, String> subTabIdBind = binder.bind(subTabIdCombo,
				CampaignDashboardElement::getSubTabId, CampaignDashboardElement::setSubTabId);

		grid.addColumn(campaignDashboardElement -> campaignDashboardElement.getSubTabId())
				.setHeader(I18nProperties.getCaption(Captions.campaignDashboardSubTabName));
//		subTabIdColumn.setEditorBinding(subTabIdBind);

		TextField width = new TextField(Captions.campaignDashboardChartWidth);

		Binder.Binding<CampaignDashboardElement, String> widthBind = binder.forField(width)
				.withValidator(percentValidator(),
						I18nProperties.getValidationError(Validations.campaignDashboardChartPercentage))
				.bind(campaignDashboardElement -> intToString(campaignDashboardElement.getWidth()),
						(c, s) -> c.setWidth(new Integer(s)));

		grid.addColumn(campaignDashboardElement -> intToString(campaignDashboardElement.getWidth()))
				.setHeader(I18nProperties.getCaption(Captions.campaignDashboardChartWidth));
//		widthColumn.setEditorBinding(widthBind);

		TextField height = new TextField(Captions.campaignDashboardChartHeight);
		Binder.Binding<CampaignDashboardElement, String> heightBind = binder.forField(height)
				.withValidator(percentValidator(),
						I18nProperties.getValidationError(Validations.campaignDashboardChartPercentage))
				.bind(
				campaignDashboardElement -> intToString(campaignDashboardElement.getHeight()),
				(c, s) -> c.setHeight(new Integer(s)));

		grid.addColumn(campaignDashboardElement -> intToString(campaignDashboardElement.getHeight()))
				.setHeader(I18nProperties.getCaption(Captions.campaignDashboardChartHeight));
//		heightColumn.setEditorBinding(heightBind);

		TextField order = new TextField(Captions.campaignDashboardOrder);
		order.setEnabled(false);
		Binder.Binding<CampaignDashboardElement, String> orderBind = binder.bind(order,
				campaignDashboardElement -> intToString(campaignDashboardElement.getOrder()),
				(c, s) -> c.setOrder(new Integer(s)));

		grid.addColumn(campaignDashboardElement -> intToString(campaignDashboardElement.getOrder()))
				.setHeader(I18nProperties.getCaption(Captions.campaignDashboardOrder));
//		orderColumn.setEditorBinding(orderBind);

		TextField phase = new TextField(Captions.campaignDashboardPhase);
		phase.setEnabled(false);

		Binder.Binding<CampaignDashboardElement, String> phaseBind = binder.bind(phase,
				CampaignDashboardElement::getPhase, CampaignDashboardElement::setPhase);

		grid.addColumn(campaignDashboardElement -> campaignDashboardElement.getPhase())
				.setHeader(I18nProperties.getCaption(Captions.campaignDashboardPhase));
//		phaseColumn.setEditorBinding(phaseBind);

		diagramIdCaptionCombo.addValueChangeListener(e -> {
//			Notification.show(e.getComponent().getId());
			phase.setValue(formTy);

		});

//		add(grid);

		return binder;
	}

	@Override
	protected ComponentEventListener<ClickEvent<Button>> newRowEvent() {
		return event -> {
			final CampaignDashboardElement campaignDashboardElement = new CampaignDashboardElement();
			final ArrayList<CampaignDashboardElement> gridItems = getItems();
			gridItems.add(campaignDashboardElement);
			campaignDashboardElement.setOrder(gridItems.indexOf(campaignDashboardElement));
			
			grid.setItems(gridItems);

			grid.getEditor().editItem(campaignDashboardElement);
		};
	}

	private SerializablePredicate<String> percentValidator() {
		return s -> new Integer(s) % 5 == 0;
	}

	private String intToString(Integer h) {
		return h != null ? h.toString() : StringUtils.EMPTY;
	}

	@Override
	protected void reorderGrid() {
		// TODO Auto-generated method stub
		final ArrayList<CampaignDashboardElement> gridItems = getItems();
		gridItems.forEach(campaignDashboardElement -> campaignDashboardElement
				.setOrder(gridItems.indexOf(campaignDashboardElement)));

	}

	@Override
	protected String getHeaderString() {
		// TODO Auto-generated method stub
		return Strings.headingCampaignDashboard;
	}

	@Override
	protected String getAdditionalRowCaption() {
		// TODO Auto-generated method stub
		return Captions.campaignAdditionalChart;
	}

	@Override
	protected CampaignDashboardElement createNewItem() {
		CampaignDashboardElement newItem = new CampaignDashboardElement();
		return newItem;
	}

	@Override
	protected Button createButton(ComponentEventListener<ClickEvent<Button>> newRowEvent) {
		Button addButton = new Button("Add New Row");
		addButton.addClickListener(newRowEvent);
		return addButton;
	}

	/*
	 * public static class DiagramIdCaption implements Serializable {
	 * 
	 * private String diagramId; private String diagramCaption;
	 * 
	 * public DiagramIdCaption(String diagramId, String diagramCaption) {
	 * campaignDiagramSeries this.diagramId = diagramId; this.diagramCaption =
	 * diagramCaption; }
	 * 
	 * public String getDiagramId() { return diagramId; }
	 * 
	 * public void setDiagramId(String diagramId) { this.diagramId = diagramId; }
	 * 
	 * public String getDiagramCaption() { return diagramCaption; }
	 * 
	 * public void setDiagramCaption(String diagramCaption) { this.diagramCaption =
	 * diagramCaption; }
	 * 
	 * @Override public String toString() { return this.diagramCaption; } }
	 */

}
