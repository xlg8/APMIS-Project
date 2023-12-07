package com.cinoteck.application.views.uiformbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import de.symeda.sormas.api.MapperUtil;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.user.UserDto;

public class FormGridComponent extends VerticalLayout {

//	static Logger log = Logger.getLogger(FormGridComponent.class);
	protected static final Logger logger = LogManager.getLogger(FormGridComponent.class);
	
	private final org.slf4j.Logger loggers = LoggerFactory.getLogger(getClass());

	ComboBox<CampaignFormElementType> formType = new ComboBox<CampaignFormElementType>("Type");
	TextField formId = new TextField("Id");
	TextField caption = new TextField("Label");
	ComboBox<Boolean> important = new ComboBox<Boolean>("Important");
	TextField options = new TextField("Option");
	TextField expression = new TextField("Expression");
	TextField dependingOn = new TextField("Depending On");
	ComboBox<String> dependingOnValues = new ComboBox<>("Depending On Values");
//	MultiSelectComboBox<String> styles = new MultiSelectComboBox<>("Styles");
	TextField styles = new TextField("Styles");
	ComboBox<String> constraints = new ComboBox<>("Constraints");
	NumberField min = new NumberField("Min");
	NumberField max = new NumberField("Max");
	TextField defaultValues = new TextField("Default Values");
	TextField errorMessage = new TextField("Error Message");

	CampaignFormMetaDto campaignFormMetaDto;
	CampaignFormElement formBeenEdited;
	CampaignFormElement newForm;
	List<CampaignFormElement> formSet = new ArrayList<>();
	FormLayout formLayout = new FormLayout();

	List<CampaignFormElement> elementList = new ArrayList<>();

	HorizontalLayout vr3 = new HorizontalLayout();
	HorizontalLayout vr1 = new HorizontalLayout();

	private Grid<CampaignFormElement> grid = new Grid<>(CampaignFormElement.class, false);
	private GridListDataView<CampaignFormElement> dataView;

	private boolean isNewForm = false;

	public FormGridComponent(CampaignFormMetaDto campaignFormMetaDto) {

		this.campaignFormMetaDto = campaignFormMetaDto;
		this.addClassName("form-grid-element");

		caption.setVisible(false);
		important.setVisible(false);
		options.setVisible(false);
		expression.setVisible(false);
		dependingOn.setVisible(false);
		dependingOnValues.setVisible(false);
		min.setVisible(false);
		max.setVisible(false);
		styles.setVisible(false);
		constraints.setVisible(false);
		errorMessage.setVisible(false);
		defaultValues.setVisible(false);

		setSizeFull();
		valueChange();
		configureFields();
		configureGrid();
		add(getContent());
	}

	private Component getContent() {

		VerticalLayout editorLayout = editForm();
		editorLayout.getStyle().remove("width");
		HorizontalLayout layout = new HorizontalLayout(grid, editorLayout);
		layout.setFlexGrow(4, grid);
		layout.setFlexGrow(0, editorLayout);
		layout.setSizeFull();
		return layout;
	}

	private void configureFields() {

		options.setHelperText("Enter options keys in a comma seperated format like so Urban, Urban, Rural, Rural");
		expression.setHelperText("Please enter expression value with care");
		formId.setHelperText("Append \"-readonly\" at the end of the Id value. If you want it to be Read Only");
		dependingOnValues.setItems("Yes", "No");
		formType.setItems(CampaignFormElementType.values());
		constraints.setItems("Expression", "Range");
		important.setItems(true, false);
//		styles.setItems("inline", "row", "first", "col-1", "col-2", "col-3", "col-4", "col-5", "col-6", "col-7",
//				"col-8", "col-9", "col-10", "col-11", "col-12");
	}

	VerticalLayout editForm() {

		VerticalLayout vrsub = new VerticalLayout();

		Button plus = new Button(new Icon(VaadinIcon.PLUS));
		Button del = new Button(new Icon(VaadinIcon.DEL_A));
		del.getStyle().set("background-color", "red!important");

		Button moreFields = new Button("Show More Fields");
		Button cancel = new Button("Cancel");
		Button save = new Button("Save");

		formLayout.setVisible(false);
		vr3.setVisible(false);

		vr1.add(plus, del);

		vr3.add(save, cancel, moreFields);
		vr3.setJustifyContentMode(JustifyContentMode.END);
		vr3.setSpacing(true);

		vrsub.add(vr1, formLayout, vr3);
		grid.addSelectionListener(ee -> {

			int size = ee.getAllSelectedItems().size();
			if (size > 0) {

				clearFields();
				CampaignFormElement selectedCamp = ee.getFirstSelectedItem().get();
				formBeenEdited = selectedCamp;
				boolean isSingleSelection = size == 1;
				vr1.setEnabled(isSingleSelection);
				vr3.setEnabled(isSingleSelection);

				formLayout.setVisible(true);
				vr3.setVisible(true);
				formType.setValue(generateType(formBeenEdited.getType()));
				formType.setVisible(true);

				logger.debug(" gghgsfjgsvjgsdvhgssjf");
				loggers.debug(" secondeeeeeeeeeeeeeeeeeeeeeeeeed");
				if (formBeenEdited.getId() != null) {
					formId.setValue(formBeenEdited.getId());
					formId.setVisible(true);
				}

				if (formBeenEdited.getCaption() != null) {
					caption.setValue(formBeenEdited.getCaption());
					caption.setVisible(true);
				}

				important.setValue(formBeenEdited.isImportant());
				important.setVisible(true);

				if (formBeenEdited.getOptions() != null) {
					options.setValue(formBeenEdited.getOptions().toString());
					options.setVisible(true);
				}

				if (formBeenEdited.getExpression() != null) {
					expression.setValue(formBeenEdited.getExpression());
					expression.setVisible(true);
				}

				if (formBeenEdited.getDependingOn() != null) {
					dependingOn.setValue(formBeenEdited.getDependingOn());
					dependingOn.setVisible(true);
				}

				if (formBeenEdited.getDependingOnValues() != null) {
					dependingOnValues.setValue(Arrays.toString(formBeenEdited.getDependingOnValues()));
					dependingOnValues.setVisible(true);
				}

				if (formBeenEdited.getStyles() != null) {
					styles.setValue(Arrays.toString(formBeenEdited.getStyles()));
					styles.setVisible(true);
				}

				if (formBeenEdited.getConstraints() != null) {
					constraints.setValue(Arrays.toString(formBeenEdited.getConstraints()));
					constraints.setVisible(true);
				}

				if (formBeenEdited.getMin() != null) {
					min.setValue(Double.parseDouble(formBeenEdited.getMin()));
					min.setVisible(true);
				}

				if (formBeenEdited.getMax() != null) {
					max.setValue(Double.parseDouble(formBeenEdited.getMax()));
					max.setVisible(true);
				}

				if (formBeenEdited.getExpression() != null) {
					errorMessage.setValue(formBeenEdited.getExpression());
					errorMessage.setVisible(true);
				}

				if (formBeenEdited.getExpression() != null) {
					defaultValues.setValue(formBeenEdited.getDefaultvalue());
					defaultValues.setVisible(true);
				}

				save.setText("Update");
			}

		});

		plus.addClickListener(e -> {

			save.setText("Save");

			formLayout.setVisible(true);
			vr3.setVisible(true);
			vr1.setVisible(false);

			clearFields();
			if (campaignFormMetaDto == null) {
				campaignFormMetaDto = new CampaignFormMetaDto();
				grid.setItems(campaignFormMetaDto.getCampaignFormElements());
			}

			grid.setHeight("auto !important");
		});

		del.addClickListener(e -> {

//			formBeenEdited = new CampaignFormElement();
			if (formBeenEdited != null) {

				campaignFormMetaDto.getCampaignFormElements().remove(formBeenEdited);
				Notification.show("Selected Form Element Deleted");
				grid.setItems(campaignFormMetaDto.getCampaignFormElements());
			} else {

				Notification.show("No Form Element Selected");
			}
		});

		moreFields.addClickListener(e -> {

			caption.setVisible(true);
			important.setVisible(true);
			options.setVisible(true);
			styles.setVisible(true);
			errorMessage.setVisible(true);
			defaultValues.setVisible(true);
		});

		cancel.addClickListener(e -> {

//			CampaignFormElement newform = new CampaignFormElement();

			vr1.setVisible(true);
			formLayout.setVisible(false);
			vr3.setVisible(false);
			caption.setVisible(false);
			important.setVisible(false);
			options.setVisible(false);
			styles.setVisible(false);
			constraints.setVisible(false);
			errorMessage.setVisible(false);
			defaultValues.setVisible(false);

			clearFields();
			save.setText("Save");
			grid.setItems(campaignFormMetaDto.getCampaignFormElements());
		});

		save.addClickListener(e -> {

			vr1.setVisible(true);
			formLayout.setVisible(false);
			vr3.setVisible(false);

			if (((Button) e.getSource()).getText().equals("Save")) {

				CampaignFormElement newForm = new CampaignFormElement();

				if (!formType.getValue().toString().isEmpty()) {

					newForm.setType(formType.getValue().toString());
				}

				if (!formId.getValue().toString().isEmpty()) {

					newForm.setId(formId.getValue());
				}

				if (!caption.getValue().isEmpty()) {

					newForm.setCaption(caption.getValue());
				}

				if (important.getValue() != null) {
					newForm.setImportant(important.getValue());
				}

				if (!options.getValue().isEmpty()) {

					List<MapperUtil> option = new ArrayList<>();
					for (String values : options.getValue().split(",")) {

						MapperUtil mapperUtil = new MapperUtil();
						mapperUtil.setKey(values);
						mapperUtil.setCaption(values);
						option.add(mapperUtil);
					}

					newForm.setOptions(option);
				}

				if (constraints.getValue() != null) {

					if (constraints.getValue().toString().equalsIgnoreCase("Expression")) {

						newForm.setConstraints(constraints.getValue().split(","));
					} else {

						if (min.getValue() != null && max.getValue() != null && min.getValue() < max.getValue()) {

							String valueOfMinMAx = min.getValue() + " " + max.getValue();
							newForm.setConstraints(valueOfMinMAx.split(" "));
						} else {

							Notification.show("Minimium must be smaller than Maximium and both must not be empty");
						}
					}

				}

				if (!styles.getValue().isEmpty()) {

					newForm.setStyles(styles.getValue().split(","));
				}

				if (!expression.getValue().isEmpty()) {

					newForm.setExpression(expression.getValue());
				}

				if (!dependingOn.getValue().isEmpty()) {

					newForm.setDependingOn(dependingOn.getValue());
				}

				if (dependingOnValues.getValue() != null) {

					newForm.setDependingOnValues(dependingOnValues.getValue().split(","));
				}

				if (!errorMessage.getValue().isEmpty()) {

					newForm.setErrormessage(errorMessage.getValue());
				}

				if (!defaultValues.getValue().isEmpty()) {

					newForm.setDefaultvalue(defaultValues.getValue());
				}

				elementList.add(newForm);
				campaignFormMetaDto.setCampaignFormElements(elementList);
				grid.setItems(campaignFormMetaDto.getCampaignFormElements());

				getGridData();
				Notification.show("New Form Element Saved");
			} else {

				if (formBeenEdited != null) {

					CampaignFormElement newForm = new CampaignFormElement();

					if (!formType.getValue().toString().isEmpty()) {

						newForm.setType(formType.getValue().toString());
					}

					if (!formId.getValue().isEmpty()) {

						newForm.setId(formId.getValue());
					}

					if (!caption.getValue().isEmpty()) {

						newForm.setCaption(caption.getValue());
					}

					if (important.getValue() != null) {
						newForm.setImportant(important.getValue());
					}

					if (!options.getValue().isEmpty()) {

						List<MapperUtil> option = new ArrayList<>();
						for (String values : options.getValue().split(",")) {

							MapperUtil mapperUtil = new MapperUtil();
							mapperUtil.setKey(values);
							mapperUtil.setCaption(values);
							option.add(mapperUtil);
						}

						newForm.setOptions(option);
					}

					if (constraints.getValue() != null) {

						if (constraints.getValue().toString().equalsIgnoreCase("Expression")) {

							newForm.setConstraints(constraints.getValue().split(","));
						} else {

							if (min.getValue() != null && max.getValue() != null && min.getValue() < max.getValue()) {

								String valueOfMinMAx = min.getValue() + " " + max.getValue();
								newForm.setConstraints(valueOfMinMAx.split(" "));
							} else {

								Notification.show("Minimium must be smaller than Maximium and both must not be empty");
							}
						}

					}

					if (!styles.getValue().isEmpty()) {

						newForm.setStyles(styles.getValue().split(","));
					}

					if (!expression.getValue().isEmpty()) {

						newForm.setExpression(expression.getValue());
					}

					if (!dependingOn.getValue().isEmpty()) {

						newForm.setDependingOn(dependingOn.getValue());
					}

					if (dependingOnValues.getValue() != null) {

						newForm.setDependingOnValues(dependingOnValues.getValue().split(","));
					}

					if (!errorMessage.getValue().isEmpty()) {

						newForm.setErrormessage(errorMessage.getValue());
					}

					if (!defaultValues.getValue().isEmpty()) {

						newForm.setDefaultvalue(defaultValues.getValue());
					}

					if (campaignFormMetaDto.getCampaignFormElements() == null) {

						campaignFormMetaDto.setCampaignFormElements(new ArrayList<>());
					}

					List<CampaignFormElement> using = new LinkedList<>();
					using = campaignFormMetaDto.getCampaignFormElements();
					int index = using.indexOf(formBeenEdited);

					using.set(index, newForm);

//					campaignFormMetaDto.getCampaignFormElements().remove(formBeenEdited);					
					campaignFormMetaDto.setCampaignFormElements(using);
					grid.setItems(campaignFormMetaDto.getCampaignFormElements());
					getGridData();

					Notification.show("Form Element Updated");
				} else {
					Notification.show("Select an Form Element to edit Please");
				}
			}
		});

		formLayout.add(formType, formId, caption, important, options, expression, dependingOn, dependingOnValues,
				styles, constraints, min, max, defaultValues, errorMessage);

		formLayout.setColspan(formType, 2);
		formLayout.setColspan(formId, 2);
		formLayout.setColspan(caption, 2);
		formLayout.setColspan(important, 2);
		formLayout.setColspan(expression, 2);
		formLayout.setColspan(dependingOn, 2);
		formLayout.setColspan(dependingOnValues, 2);
		formLayout.setColspan(styles, 2);
		formLayout.setColspan(constraints, 2);
		formLayout.setColspan(min, 2);
		formLayout.setColspan(max, 2);
		formLayout.setColspan(defaultValues, 2);
		formLayout.setColspan(errorMessage, 2);

		return vrsub;
	}

	private CampaignFormElementType generateType(String type) {
		if (type.equalsIgnoreCase("LABEL")) {
			return CampaignFormElementType.LABEL;
		} else if (type.equalsIgnoreCase("ARRAY")) {
			return CampaignFormElementType.ARRAY;
		} else if (type.equalsIgnoreCase("CHECKBOX")) {
			return CampaignFormElementType.CHECKBOX;
		} else if (type.equalsIgnoreCase("CHECKBOXBASIC")) {
			return CampaignFormElementType.CHECKBOXBASIC;
		} else if (type.equalsIgnoreCase("COMMENT")) {
			return CampaignFormElementType.COMMENT;
		} else if (type.equalsIgnoreCase("DATE")) {
			return CampaignFormElementType.DATE;
		} else if (type.equalsIgnoreCase("DAYWISE")) {
			return CampaignFormElementType.DAYWISE;
		} else if (type.equalsIgnoreCase("DECIMAL")) {
			return CampaignFormElementType.DECIMAL;
		} else if (type.equalsIgnoreCase("DROPDOWN")) {
			return CampaignFormElementType.DROPDOWN;
		} else if (type.equalsIgnoreCase("DROPDOWN")) {
			return CampaignFormElementType.DROPDOWN;
		} else if (type.equalsIgnoreCase("NUMBER")) {
			return CampaignFormElementType.NUMBER;
		} else if (type.equalsIgnoreCase("RADIO")) {
			return CampaignFormElementType.RADIO;
		} else if (type.equalsIgnoreCase("RADIOBASIC")) {
			return CampaignFormElementType.RADIOBASIC;
		} else if (type.equalsIgnoreCase("RANGE")) {
			return CampaignFormElementType.RANGE;
		} else if (type.equalsIgnoreCase("SECTION")) {
			return CampaignFormElementType.SECTION;
		} else if (type.equalsIgnoreCase("TEXT")) {
			return CampaignFormElementType.TEXT;
		} else if (type.equalsIgnoreCase("TEXTBOX")) {
			return CampaignFormElementType.TEXTBOX;
		} else if (type.equalsIgnoreCase("YES_NO")) {
			return CampaignFormElementType.YES_NO;
		}

		return CampaignFormElementType.LABEL;
	}

	void configureGrid() {

//		grid.setSelectionMode(SelectionMode.SINGLE);
//		grid.setMultiSort(true, MultiSortPriority.APPEND);
//		grid.setSizeFull();
//		grid.setColumnReorderingAllowed(true);

		ComponentRenderer<Span, CampaignFormElement> constraintRenderer = new ComponentRenderer<>(ijnput -> {
			String value = Arrays.toString(ijnput.getConstraints());
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormElement> styleRenderer = new ComponentRenderer<>(ijnput -> {
			String value = Arrays.toString(ijnput.getStyles());
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormElement> dependingOnValuesRenderer = new ComponentRenderer<>(ijnput -> {
			String value = Arrays.toString(ijnput.getDependingOnValues());
			Span label = new Span(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormElement> optionsRenderer = new ComponentRenderer<>(ijnput -> {

			List<String> option = new ArrayList<>();
			Span label = new Span("");

			if (ijnput.getOptions() != null) {

				for (MapperUtil values : ijnput.getOptions()) {

					MapperUtil mapperUtil = new MapperUtil();
					mapperUtil.setKey(values.getKey());
					mapperUtil.setCaption(values.getCaption());
					option.add(mapperUtil.toString());
				}

				String value = String.valueOf(option);
				label = new Span(value);
			}

			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		grid.addColumn(CampaignFormElement::getId).setHeader("Id").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormElement::getCaption).setHeader("Caption").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormElement::getType).setHeader("Type").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormElement::getExpression).setHeader("Expression").setSortable(true).setResizable(true);
//		grid.addColumn(CampaignFormElement::getMin).setHeader("Min").setSortable(true).setResizable(true);
//		grid.addColumn(CampaignFormElement::getMax).setHeader("Max").setSortable(true).setResizable(true);
		grid.addColumn(styleRenderer).setHeader("Styles").setSortable(true).setResizable(true);
		grid.addColumn(optionsRenderer).setHeader("Options").setSortable(true).setResizable(true);
		grid.addColumn(constraintRenderer).setHeader("Constraint").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormElement::getDependingOn).setHeader("Depending On").setSortable(true)
				.setResizable(true);
		grid.addColumn(dependingOnValuesRenderer).setHeader("Depending On Value").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormElement::isImportant).setHeader("Important").setSortable(true).setResizable(true);
//		grid.addColumn(CampaignFormElement::isWarnonerror).setHeader("Warned Error").setSortable(true)
//				.setResizable(true);
//		grid.addColumn(CampaignFormElement::isIgnoredisable).setHeader("Ignoredisable").setSortable(true)
//				.setResizable(true);
//		grid.addColumn(CampaignFormElement::getDefaultvalue).setHeader("Default Value").setSortable(true)
//				.setResizable(true);
		grid.addColumn(CampaignFormElement::getErrormessage).setHeader("Error Message").setSortable(true)
				.setResizable(true);

		List<CampaignFormElement> existingElements = campaignFormMetaDto.getCampaignFormElements();

		existingElements = existingElements == null ? new ArrayList<>() : existingElements;
		ListDataProvider<CampaignFormElement> dataprovider = DataProvider.fromStream(existingElements.stream());

		dataView = grid.setItems(dataprovider);
		grid.setVisible(true);
		grid.setAllRowsVisible(true);
	}

	public void clearFields() {

		formType.setValue(CampaignFormElementType.LABEL);
		formId.setValue("");
		caption.setValue("");
		options.setValue("");
		important.setValue(false);
		constraints.clear();
		min.setValue(null);
		max.setValue(null);
		dependingOn.setValue("");
		dependingOnValues.clear();
//		styles.setValue(Collections.emptySet());
		styles.setValue("");
		errorMessage.setValue("");
		defaultValues.setValue("");
	}

	public void valueChange() {

		formType.addValueChangeListener(e -> {

			if (e.getValue().toString().toLowerCase().equals("section")
					|| e.getValue().toString().toLowerCase().equals("daywise")) {

				caption.setVisible(false);
				important.setVisible(false);
				options.setVisible(false);
				expression.setVisible(false);
				dependingOn.setVisible(false);
				dependingOnValues.setVisible(false);
				min.setVisible(false);
				max.setVisible(false);
				styles.setVisible(false);
				constraints.setVisible(false);
				errorMessage.setVisible(false);
				defaultValues.setVisible(false);
			} else if (e.getValue().toString().toLowerCase().equals("number")
					|| e.getValue().toString().toLowerCase().equals("range")) {

				constraints.setVisible(true);
				expression.setVisible(true);
				dependingOn.setVisible(true);
				dependingOnValues.setVisible(true);
			} else {

				caption.setVisible(false);
				important.setVisible(false);
				options.setVisible(false);
				styles.setVisible(false);
				errorMessage.setVisible(false);
				defaultValues.setVisible(false);
				constraints.setVisible(false);
			}

			if (!e.getValue().toString().trim().isEmpty()) {
//				jsonData.put("type", e.getValue().toString());
			}

		});

//		formId.addValueChangeListener(e -> {
//
//			if (!e.getValue().toString().trim().isEmpty()) {
////				jsonData.put("id", e.getValue().toString());
//			}
//		});
//
//		caption.addValueChangeListener(e -> {
//
//			if (!e.getValue().toString().trim().isEmpty()) {
////				jsonData.put("caption", e.getValue().toString());
//			}
//		});
//
//		important.addValueChangeListener(e -> {
//
//			if (e.getValue().toString().trim() != null) {
////				jsonData.put("important", "true");
//			}
//		});
//
//		dependingOn.addValueChangeListener(e -> {
//
//			if (e.getValue().toString().trim() != null) {
//
////				jsonData.put("dependingOn", e.getValue());
//			}
//		});
//
//		dependingOnValues.addValueChangeListener(e -> {
//
//			if (e.getValue().toString().trim() != null) {
////				jsonData.put("dependingOnValues", e.getValue().toLowerCase());
//			}
//		});
//
//		styles.addValueChangeListener(e -> {
//
//			if (e.getValue().toString().trim() != null) {
////				jsonDa/ta.put("styles", e.getValue().toString());
//			}
//		});
//
//		constraints.addValueChangeListener(e -> {
//
//			if (e.getValue().toString().trim() != null && e.getValue().toString().toLowerCase().equals("expression")) {
////				jsonData.put("constraints", "expression");
//				min.setVisible(false);
//				max.setVisible(false);
//			} else {
//				min.setVisible(true);
//				max.setVisible(true);
//			}
//		});
//
//		min.addValueChangeListener(e -> {
//
//			if (e.getValue() != null) {
////				jsonData.put("constraints", "[min=" + e.getValue().toString() + ", ");
//			}
//		});
//
//		max.addValueChangeListener(e -> {
//
//			if (e.getValue() != null) {
//
////				String minValue = jsonData.get("constraints");
////				if (minValue != null) {
////
//////					jsonData.put("constraints", minValue + "max=" + e.getValue().toString() + "]");
////				}
//			}
//
//		});
//
//		errorMessage.addValueChangeListener(e -> {
//
//			if (e.getValue().toString().trim() != null) {
//
////				jsonData.put("errormessage", e.getValue().toString());
//			}
//		});
	}

	public List<CampaignFormElement> getGridData() {
		return grid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
	}

}
