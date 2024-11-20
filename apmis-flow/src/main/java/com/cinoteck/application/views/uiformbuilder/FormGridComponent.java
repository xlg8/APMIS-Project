package com.cinoteck.application.views.uiformbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;

import de.symeda.sormas.api.MapperUtil;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;

public class FormGridComponent extends VerticalLayout {

	protected static final Logger logger = LogManager.getLogger(FormGridComponent.class);

	private final org.slf4j.Logger loggers = LoggerFactory.getLogger(getClass());

	ComboBox<CampaignFormElementType> formType = new ComboBox<CampaignFormElementType>("Type");
	TextField formId = new TextField("Id *");
	TextField caption = new TextField("Label");
	ComboBox<Boolean> important = new ComboBox<Boolean>("Important");
	TextField options = new TextField("Option");
	TextField expression = new TextField("Expression");
	Button expressions = new Button("Generate Expression");
	ComboBox<String> dependingOn = new ComboBox<String>("Depending On");
//	TextField dependingOn = new TextField("Depending On");
	ComboBox<String> dependingOnValues = new ComboBox<>("Depending On Values");
	TextField styles = new TextField("Styles");
	ComboBox<String> constraints = new ComboBox<>("Constraints");
	TextField min = new TextField("Min");
	TextField max = new TextField("Max");
	TextField defaultValues = new TextField("Default Values");
	TextField errorMessage = new TextField("Error Message");
	TextField comment = new TextField("Comment");

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
//	ObjectMapper objectMapper = new ObjectMapper();
	Dialog dialog;
	Button save = new Button("Save");

	public FormGridComponent(CampaignFormMetaDto campaignFormMetaDto) {

		this.campaignFormMetaDto = campaignFormMetaDto;
		this.addClassName("form-grid-element");

		formId.setVisible(false);
		important.setVisible(false);
		options.setVisible(false);
		expression.setVisible(false);
		expressions.setVisible(false);
		dependingOn.setVisible(false);
		dependingOnValues.setVisible(false);
		min.setVisible(false);
		max.setVisible(false);
		styles.setVisible(false);
		constraints.setVisible(false);
		errorMessage.setVisible(false);
		comment.setVisible(false);
		defaultValues.setVisible(false);

		caption.setValueChangeMode(ValueChangeMode.EAGER);

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

		Set<CampaignFormElementType> formTypeAll = new TreeSet<>(Arrays.asList(CampaignFormElementType.values()));
		formTypeAll.remove(CampaignFormElementType.CHECKBOX);
		formTypeAll.remove(CampaignFormElementType.CHECKBOXBASIC);
		formTypeAll.remove(CampaignFormElementType.DECIMAL);
		formTypeAll.remove(CampaignFormElementType.COMMENT);
		formTypeAll.remove(CampaignFormElementType.ARRAY);
		formTypeAll.remove(CampaignFormElementType.RADIO);
		formTypeAll.remove(CampaignFormElementType.RADIOBASIC);
		caption.setHelperText("Enter the Label size by wrapping your Label with a <h1> to <h6> tag");
		options.setHelperText("Enter your option in this format [[key:bike, caption:bike, order:0]]");
		expression.setHelperText("Please enter expression value with care");
		formId.setHelperText("Append \"-readonly\" at the end of the Id value. If you want it to be Read Only");
		dependingOnValues.setItems("true", "false");
		formType.setItems(formTypeAll);
		constraints.setItems("Expression", "Range");
		important.setItems(true, false);
		styles.setHelperText("Examples of all styles: inline, row, first, col-1, col-2, col-3, col-4, "
				+ "col-5, col-6, col-7, col-8, col-9, col-10, col-11, col-12 add them in a comma seperated format");

		List<CampaignFormElement> listofelements = campaignFormMetaDto.getCampaignFormElements();
		List<String> listofthem = new ArrayList<>();
		if (campaignFormMetaDto.getCampaignFormElements() != null) {
			for (CampaignFormElement campaignFormElement : listofelements) {
				listofthem.add(campaignFormElement.getId());
			}
		}
		dependingOn.setItems(listofthem);
	}

	private String generateValueBasedOnSource(String sourceValue) {
		String manipulatingId = sourceValue.replaceAll(" ", "_");
		manipulatingId = manipulatingId.replaceAll("readonly", "");
		manipulatingId = manipulatingId.replaceAll("-readonly", "");
		manipulatingId = manipulatingId.replaceAll("<.*?>", "");
		return manipulatingId;
	}

	private boolean checkForUniqueId(String currentFormId) {

		boolean codeStopRunning = true;
		List<CampaignFormElement> listofFormElements = new ArrayList<>();

		if (campaignFormMetaDto.getCampaignFormElements() != null) {
			listofFormElements = campaignFormMetaDto.getCampaignFormElements();
		}

		List<String> listofId = new ArrayList<>();
		for (CampaignFormElement campaignFormElement : listofFormElements) {
			if (campaignFormElement.getId().equalsIgnoreCase(currentFormId)) {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.setPosition(Position.MIDDLE);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph("Form Id is not Unique");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
				codeStopRunning = false;
				break;
			}
		}

		return codeStopRunning;
	}

	VerticalLayout editForm() {

		VerticalLayout vrsub = new VerticalLayout();

		Button plus = new Button(new Icon(VaadinIcon.PLUS));
		Button del = new Button(new Icon(VaadinIcon.DEL_A));
		del.getStyle().set("background-color", "red!important");

		Button moreFields = new Button("Show More Fields");

		Icon cancelIcon = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
		cancelIcon.getStyle().set("color", "red !important");
		Button cancel = new Button("Cancel", cancelIcon);
		cancel.getStyle().set("color", "red !important");
		cancel.getStyle().set("background", "white");
		cancel.getStyle().set("border", "1px solid red");

		Icon saveIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		saveIcon.getStyle().set("color", "green");
		save.setIcon(saveIcon);
//		Button save = new Button("Save", saveIcon);

		formLayout.setVisible(false);
		formLayout.setId("target-section");
		vr3.setVisible(false);

		vr1.add(plus, del);

		vr3.add(save, cancel, moreFields);
		vr3.setJustifyContentMode(JustifyContentMode.END);
		vr3.setSpacing(true);

		vrsub.add(vr1, formLayout, vr3);
		grid.addSelectionListener(ee -> {

			clearFields();
			UI.getCurrent().getPage().executeJs(
					"document.getElementById('target-section').scrollIntoView({ behavior: 'smooth', block: 'start' });");
			int size = ee.getAllSelectedItems().size();
			if (size > 0) {

				CampaignFormElement selectedCamp = ee.getFirstSelectedItem().get();
				formBeenEdited = selectedCamp;
				boolean isSingleSelection = size == 1;
				vr1.setEnabled(isSingleSelection);
				vr3.setEnabled(isSingleSelection);

				formLayout.setVisible(true);
				vr3.setVisible(true);
				formType.setValue(generateType(formBeenEdited.getType()));
				formType.setVisible(true);

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
					List<String> option = new ArrayList<>();
					String value = "";
					for (MapperUtil values : formBeenEdited.getOptions()) {
						MapperUtil mapperUtil = new MapperUtil();
						mapperUtil.setKey(values.getKey());
						mapperUtil.setCaption(values.getCaption());
						mapperUtil.setOrder(values.getOrder());
						option.add(mapperUtil.toString());
					}

					value = String.valueOf(option);

					options.setValue(value);
					options.setVisible(true);
				}

				expressions.setText("Edit Expression");
				if (formBeenEdited.getExpression() != null) {
					expression.setValue(formBeenEdited.getExpression());
					expression.setVisible(true);
				}

				if (formBeenEdited.getDependingOn() != null) {
					dependingOn.setValue(formBeenEdited.getDependingOn());
					dependingOn.setVisible(true);
				}

				if (formBeenEdited.getDependingOnValues() != null) {
//					dependingOnValues.setValue(Arrays.toString(formBeenEdited.getDependingOnValues()));
					dependingOnValues.setValue(Arrays.toString(formBeenEdited.getDependingOnValues()).substring(1,
							Arrays.toString(formBeenEdited.getDependingOnValues()).length() - 1));
					dependingOnValues.setVisible(true);
				}

				if (formBeenEdited.getStyles() != null) {
					styles.setValue(Arrays.toString(formBeenEdited.getStyles()).substring(1,
							Arrays.toString(formBeenEdited.getStyles()).length() - 1));
					styles.setVisible(true);
				}

//				if (formBeenEdited.getConstraints() != null) {
//					if (formBeenEdited.getConstraints()[0].toLowerCase().equals("expression")) {
//						constraints.setValue(Arrays.toString(new String[] { "Expression" }));
//						constraints.setVisible(true);
//						max.setVisible(false);
//						min.setVisible(false);
//					} else {
//						constraints.setValue(Arrays.toString(new String[] { "Range" }));
//						constraints.setVisible(true);
//
////						min.setValue(Double.parseDouble(formBeenEdited.getConstraints()[1].substring(4,
////								formBeenEdited.getConstraints()[1].length())));
//						min.setValue(formBeenEdited.getConstraints()[0].substring(4,
//								formBeenEdited.getConstraints()[1].length()));
//						min.setVisible(true);
//						logger.debug(min.getValue() + " minnnnn value");
////						max.setValue(Double.parseDouble(formBeenEdited.getConstraints()[0].substring(4,
////						formBeenEdited.getConstraints()[0].length())));
//						max.setValue(formBeenEdited.getConstraints()[1].substring(4,
//								formBeenEdited.getConstraints()[0].length()));
//						max.setVisible(true);
//						logger.debug(min.getValue() + " Maxxxxxxxxxxxxxxx value");
//					}
//				}

				if (formBeenEdited.getConstraints() != null) {
					if (formBeenEdited.getConstraints()[0].toLowerCase().equals("expression")) {
						constraints.setValue("Expression");
						constraints.setVisible(true);
						max.setVisible(false);
						min.setVisible(false);
					} else {
						constraints.setValue("Range");
						constraints.setVisible(true);

//						min.setValue(Double.parseDouble(formBeenEdited.getConstraints()[1].substring(4,
//								formBeenEdited.getConstraints()[1].length())));
						min.setValue(formBeenEdited.getConstraints()[0].substring(4,
								formBeenEdited.getConstraints()[1].length()));
						min.setVisible(true);
						logger.debug(min.getValue() + " minnnnn value");
//						max.setValue(Double.parseDouble(formBeenEdited.getConstraints()[0].substring(4,
//						formBeenEdited.getConstraints()[0].length())));
						max.setValue(formBeenEdited.getConstraints()[1].substring(4,
								formBeenEdited.getConstraints()[0].length()));
						max.setVisible(true);
						logger.debug(min.getValue() + " Maxxxxxxxxxxxxxxx value");
					}
				}

				if (formBeenEdited.getErrormessage() != null) {
					errorMessage.setValue(formBeenEdited.getErrormessage());
					errorMessage.setVisible(true);
				}

				if (formBeenEdited.getComment() != null) {
					comment.setValue(formBeenEdited.getComment());
					comment.setVisible(true);
				}

				if (formBeenEdited.getDefaultvalue() != null) {
					defaultValues.setValue(formBeenEdited.getDefaultvalue());
					defaultValues.setVisible(true);
				}

				save.setText("Update");
			}

		});

		plus.addClickListener(e ->

		{

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

			if (formBeenEdited != null) {

				campaignFormMetaDto.getCampaignFormElements().remove(formBeenEdited);
				Notification notification = new Notification("Selected Form Element Deleted", 3000, Position.MIDDLE);
				notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
				notification.open();
				grid.setItems(campaignFormMetaDto.getCampaignFormElements());
			} else {

				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.setPosition(Position.MIDDLE);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph("No Form Element Selected");
				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
			}
		});

		moreFields.addClickListener(e -> {

			formId.setVisible(true);
			important.setVisible(true);
//			options.setVisible(true);
			styles.setVisible(true);
			errorMessage.setVisible(true);
			comment.setVisible(true);
			defaultValues.setVisible(true);
		});

		cancel.addClickListener(e -> {

			vr1.setVisible(true);
			formLayout.setVisible(false);
			vr3.setVisible(false);
			caption.setVisible(false);
			important.setVisible(false);
			options.setVisible(false);
			styles.setVisible(false);
			constraints.setVisible(false);
			errorMessage.setVisible(false);
			comment.setVisible(false);
			defaultValues.setVisible(false);
			expressions.setText("Generate Expression");

			clearFields();
			save.setText("Save");
			grid.setItems(campaignFormMetaDto.getCampaignFormElements());
		});

		save.addClickListener(e -> {

//			vr1.setVisible(true);
//			formLayout.setVisible(false);
//			vr3.setVisible(false);

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

//				[[key:bike, caption:bike, order:0], [key:bicycle, caption:bicycle, order:1], [key:car, caption:car, order:2]]
				if (!options.getValue().isEmpty()) {
					List<MapperUtil> option = new ArrayList<>();

					Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
					Matcher matcher = pattern.matcher(options.getValue());

					while (matcher.find()) {
						String keyValuePairs = matcher.group(1);

						String[] pairs = keyValuePairs.split(", ");

						MapperUtil mapperUtil = new MapperUtil();

						for (String pair : pairs) {
							String[] parts = pair.split(":");
							String key = parts[0];
							String value = parts[1];

							key = key.trim();
							value = value.trim();

							key = key.replaceAll("\\[", "");
							value = value.replaceAll("^\"|\"$", "");

							switch (key) {
							case "key":
								mapperUtil.setKey(value);
								break;
							case "caption":
								mapperUtil.setCaption(value);
								break;
							case "order":
								mapperUtil.setOrder(value);
								break;
							}
						}
						option.add(mapperUtil);
					}
					newForm.setOptions(option);
				}

				if (constraints.getValue() != null) {

					if (constraints.getValue().toString().toLowerCase().equals("expression")) {

						newForm.setConstraints(constraints.getValue().split(","));
					} else {

						if (min.getValue() != null && max.getValue() != null
								&& Integer.parseInt(min.getValue()) < Integer.parseInt(max.getValue())) {

							String valueOfMinMAx = "max=" + min.getValue() + " min=" + max.getValue();
							newForm.setConstraints(valueOfMinMAx.split(" "));
						} else {

							Notification notification = new Notification();
							notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
							notification.setPosition(Position.MIDDLE);
							Button closeButton = new Button(new Icon("lumo", "cross"));
							closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
							closeButton.getElement().setAttribute("aria-label", "Close");
							closeButton.addClickListener(event -> {
								notification.close();
							});

							Paragraph text = new Paragraph(
									"Minimium must be smaller than Maximium and both must not be empty");
							HorizontalLayout layout = new HorizontalLayout(text, closeButton);
							layout.setAlignItems(Alignment.CENTER);

							notification.add(layout);
							notification.open();
							return;
						}
					}

				}

				if (!styles.getValue().isEmpty()) {

					newForm.setStyles(styles.getValue().split(","));
					if (newForm.getStyles() == null) {
						newForm.setStyles(new String[0]);
					}
				}

				expressions.setText("Generate Expression");
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

				if (!comment.getValue().isEmpty()) {

					newForm.setComment(comment.getValue());
				}

				if (!defaultValues.getValue().isEmpty()) {

					newForm.setDefaultvalue(defaultValues.getValue());
				}

				if (!formId.getValue().toString().isEmpty()) {
					if (checkForUniqueId(formId.getValue())) {
						if (campaignFormMetaDto.getCampaignFormElements() == null) {
							elementList.add(newForm);
							campaignFormMetaDto.setCampaignFormElements(elementList);
							logger.debug("Campaignformelement is empty here at the moment");
							grid.setItems(campaignFormMetaDto.getCampaignFormElements());
						} else {
							elementList = new ArrayList<>();
							elementList.addAll(campaignFormMetaDto.getCampaignFormElements());
							elementList.add(newForm);
							campaignFormMetaDto.setCampaignFormElements(elementList);
							grid.setItems(campaignFormMetaDto.getCampaignFormElements());
							
							vr1.setVisible(true);
							formLayout.setVisible(false);
							vr3.setVisible(false);
						}

						getGridData();
						Notification notification = new Notification("New Form Element Saved", 3000, Position.MIDDLE);
						notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
						notification.open();
					}
				} else {
					Notification notification = new Notification();
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.setPosition(Position.MIDDLE);
					Button closeButton = new Button(new Icon("lumo", "cross"));
					closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
					closeButton.getElement().setAttribute("aria-label", "Close");
					closeButton.addClickListener(event -> {
						notification.close();
					});

					Paragraph text = new Paragraph("Form Element Id Field cannot be left");
					HorizontalLayout layout = new HorizontalLayout(text, closeButton);
					layout.setAlignItems(Alignment.CENTER);

					notification.add(layout);
					notification.open();
				}

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

//					[[key:DC, caption:DC, order:2], [key:CS, caption:CS, order:1], [key:VSM, caption:Vaccinator/SM, order:1]]
					if (!options.getValue().isEmpty()) {
						List<MapperUtil> option = new ArrayList<>();

						Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
						Matcher matcher = pattern.matcher(options.getValue());

						while (matcher.find()) {
							String keyValuePairs = matcher.group(1);

							String[] pairs = keyValuePairs.split(", ");

							MapperUtil mapperUtil = new MapperUtil();

							for (String pair : pairs) {
								String[] parts = pair.split(":");
								String key = parts[0];
								String value = parts[1];

								key = key.trim();
								value = value.trim();

								key = key.replaceAll("\\[", "");
								value = value.replaceAll("^\"|\"$", "");

								switch (key) {
								case "key":
									mapperUtil.setKey(value);
									break;
								case "caption":
									mapperUtil.setCaption(value);
									break;
								case "order":
									mapperUtil.setOrder(value);
									break;
								}
							}
							option.add(mapperUtil);
						}
						newForm.setOptions(option);
					}

					if (constraints.getValue() != null) {

						if (constraints.getValue().toString().equalsIgnoreCase("expression")) {

							newForm.setConstraints(constraints.getValue().split(","));
						} else {

							if (!min.getValue().isEmpty() && !max.getValue().isEmpty()) {
								if (min.getValue() != null && max.getValue() != null
										&& Integer.parseInt(min.getValue()) < Integer.parseInt(max.getValue())) {

									String valueOfMinMAx = "max=" + min.getValue() + " min=" + max.getValue();
									newForm.setConstraints(valueOfMinMAx.split(" "));
								} else {

									Notification notification = new Notification();
									notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
									notification.setPosition(Position.MIDDLE);
									Button closeButton = new Button(new Icon("lumo", "cross"));
									closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
									closeButton.getElement().setAttribute("aria-label", "Close");
									closeButton.addClickListener(event -> {
										notification.close();
									});

									Paragraph text = new Paragraph(
											"Minimium must be smaller than Maximium and both must not be empty");
									HorizontalLayout layout = new HorizontalLayout(text, closeButton);
									layout.setAlignItems(Alignment.CENTER);

									notification.add(layout);
									notification.open();
								}
							}
						}

					}

					if (!styles.getValue().isEmpty()) {

						newForm.setStyles(styles.getValue().split(","));
					}

					expressions.setText("Edit Expression");
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

					if (!comment.getValue().isEmpty()) {
//						System.out.println("hereeeeeeeeeeeeeeee");
						newForm.setComment(comment.getValue());
//						
//						if (!errorMessage.getValue().isEmpty()) {
//							System.out.println("thiagooooooooooo " + errorMessage.getValue());
//							newForm.setErrormessage(errorMessage.getValue());
//						} else {
//							newForm.setErrormessage(" ");
//						}						
//						System.out.println("weyyyyyyyyyyyy");
					}

					if (!defaultValues.getValue().isEmpty()) {

						newForm.setDefaultvalue(defaultValues.getValue());
					}

					if (!formId.getValue().toString().isEmpty()) {
						if (formBeenEdited.getId().equals(formId.getValue())) {
							List<CampaignFormElement> using = new LinkedList<>();
							using = campaignFormMetaDto.getCampaignFormElements();
							int index = using.indexOf(formBeenEdited);

							using.set(index, newForm);
							campaignFormMetaDto.setCampaignFormElements(using);
							grid.setItems(campaignFormMetaDto.getCampaignFormElements());
							getGridData();
							
							vr1.setVisible(true);
							formLayout.setVisible(false);
							vr3.setVisible(false);
							
							Notification notification = new Notification("Form Element Updated", 3000, Position.MIDDLE);
							notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
							notification.open();
						} else {
							if (checkForUniqueId(formId.getValue())) {
								List<CampaignFormElement> using = new LinkedList<>();
								using = campaignFormMetaDto.getCampaignFormElements();
								int index = using.indexOf(formBeenEdited);

								using.set(index, newForm);
								campaignFormMetaDto.setCampaignFormElements(using);
								grid.setItems(campaignFormMetaDto.getCampaignFormElements());
								getGridData();
								
								vr1.setVisible(true);
								formLayout.setVisible(false);
								vr3.setVisible(false);
								
								Notification notification = new Notification("Form Element Updated", 3000,
										Position.MIDDLE);
								notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
								notification.open();
							} else {
								System.out.println("x-factor");
							}
						}
					} else {
						Notification notification = new Notification();
						notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
						notification.setPosition(Position.MIDDLE);
						Button closeButton = new Button(new Icon("lumo", "cross"));
						closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
						closeButton.getElement().setAttribute("aria-label", "Close");
						closeButton.addClickListener(event -> {
							notification.close();
						});

						Paragraph text = new Paragraph("Form Element Id Field cannot be left");
						HorizontalLayout layout = new HorizontalLayout(text, closeButton);
						layout.setAlignItems(Alignment.CENTER);

						notification.add(layout);
						notification.open();

					}

				} else {
					Notification notification = new Notification();
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.setPosition(Position.MIDDLE);
					Button closeButton = new Button(new Icon("lumo", "cross"));
					closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
					closeButton.getElement().setAttribute("aria-label", "Close");
					closeButton.addClickListener(event -> {
						notification.close();
					});

					Paragraph text = new Paragraph("Select an Form Element to edit Please");

					HorizontalLayout layout = new HorizontalLayout(text, closeButton);
					layout.setAlignItems(Alignment.CENTER);

					notification.add(layout);
					notification.open();
				}
			}
		});

		formLayout.add(formType, caption, formId, important, options, expression, dependingOn, dependingOnValues,
				styles, constraints, min, max, defaultValues, errorMessage, comment, expressions);

		formLayout.setColspan(formType, 2);
		formLayout.setColspan(formId, 2);
		formLayout.setColspan(caption, 2);
		formLayout.setColspan(important, 2);
		formLayout.setColspan(expression, 2);
		formLayout.setColspan(expressions, 2);
		formLayout.setColspan(dependingOn, 2);
		formLayout.setColspan(dependingOnValues, 2);
		formLayout.setColspan(styles, 2);
		formLayout.setColspan(constraints, 2);
		formLayout.setColspan(min, 2);
		formLayout.setColspan(max, 2);
		formLayout.setColspan(defaultValues, 2);
		formLayout.setColspan(errorMessage, 2);
		formLayout.setColspan(comment, 2);

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

		ComponentRenderer<Span, CampaignFormElement> constraintRenderer = new ComponentRenderer<>(input -> {
			String value = Arrays.toString(input.getConstraints());
//			Span label = new Span(value);
			Span label = new Span();
			if (input.getConstraints() == null) {
				label = new Span("");
			} else {
				label = new Span(value);
			}
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormElement> styleRenderer = new ComponentRenderer<>(input -> {
			String value = Arrays.toString(input.getStyles());
			Span label = new Span();
			if (input.getStyles() == null) {
				label = new Span("");
			} else {
				label = new Span(value);
			}
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormElement> dependingOnValuesRenderer = new ComponentRenderer<>(input -> {
			String value = Arrays.toString(input.getDependingOnValues());
			Span label = new Span();

			if (input.getDependingOnValues() == null) {
				label = new Span("");
			} else {
				label = new Span(value);
			}

			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Span, CampaignFormElement> optionsRenderer = new ComponentRenderer<>(input -> {
			List<String> option = new ArrayList<>();
			Span label = new Span("");

			if (input.getOptions() != null) {
				for (MapperUtil values : input.getOptions()) {
					MapperUtil mapperUtil = new MapperUtil();
					mapperUtil.setKey(values.getKey());
					mapperUtil.setCaption(values.getCaption());
					mapperUtil.setOrder(values.getOrder());
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
//		grid.addColumn(CampaignFormElement::getComment).setHeader("Comment").setSortable(true)
//		.setResizable(true);

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
		expression.setValue("");
		caption.setValue("");
		options.setValue("");
		important.setValue(false);
//		constraints.setValue("Expression");
		constraints.clear();
		min.clear();
		max.clear();
		dependingOn.setValue("");
		dependingOnValues.clear();
		styles.setValue("");
		errorMessage.setValue("");
		comment.setValue("");
		defaultValues.setValue("");
	}

	public void valueChange() {

		formType.addValueChangeListener(e -> {

			if (e.getValue().toString().toLowerCase().equals("section")
					|| e.getValue().toString().toLowerCase().equals("daywise")) {

				formId.setVisible(false);
				important.setVisible(false);
				options.setVisible(false);
				expression.setVisible(false);
				expressions.setVisible(false);
				dependingOn.setVisible(false);
				dependingOnValues.setVisible(false);
				min.setVisible(false);
				max.setVisible(false);
				styles.setVisible(false);
				constraints.setVisible(false);
				errorMessage.setVisible(false);
				comment.setVisible(false);
				defaultValues.setVisible(false);
			} else if (e.getValue().toString().toLowerCase().equals("number")
					|| e.getValue().toString().toLowerCase().equals("range")) {

				constraints.setVisible(true);
				expression.setVisible(true);
				expressions.setVisible(true);
				dependingOn.setVisible(true);
				dependingOnValues.setVisible(true);
			} else if (e.getValue().toString().toLowerCase().equals("dropdown")) {

				formId.setVisible(true);
				important.setVisible(true);
				options.setVisible(true);
				expression.setVisible(false);
				expressions.setVisible(false);
				dependingOn.setVisible(false);
				dependingOnValues.setVisible(false);
				min.setVisible(false);
				max.setVisible(false);
				styles.setVisible(false);
				constraints.setVisible(false);
				errorMessage.setVisible(false);
				comment.setVisible(false);
				defaultValues.setVisible(false);
			} else {

				formId.setVisible(false);
				important.setVisible(false);
				options.setVisible(false);
				styles.setVisible(false);
				errorMessage.setVisible(false);
				comment.setVisible(false);
				defaultValues.setVisible(false);
				constraints.setVisible(false);
			}
		});

		constraints.addValueChangeListener(e -> {
			if (e.getValue().toString().toLowerCase().substring(0, e.getValue().toString().toLowerCase().length())
					.equals("expression")) {
				min.setVisible(false);
				max.setVisible(false);
			} else {
				min.setVisible(true);
				max.setVisible(true);
			}
		});

		expressions.addClickListener(e -> {
			dialog = new Dialog();
			dialog.open();
			expressionPopUp(expression.getValue());
		});

	}

	public void expressionPopUp(String expressions) {

		dialog.setWidth("250px");
		dialog.setHeaderTitle("Expression Magic");
		dialog.setClassName("expressionDialog");

		VerticalLayout expressionLayout = new VerticalLayout();
		ComboBox<String> ids = new ComboBox<String>("Form Ids");
		TextField expressionEdit = new TextField("Expression Editor");
		expressionEdit.setValue(expressions);
		expressionEdit.setClassName("expressionEdit");

		Button addExpression = new Button("Add");
		Button saveExpression = new Button("Save");
		Button cancelExpression = new Button("Cancel", e -> dialog.close());

		H3 inputH3 = new H3("Current input: ");
		H3 selectionH3 = new H3("Selection: ");

		expressionLayout.remove(ids, expressionEdit);
		expressionLayout.add(ids, expressionEdit);
		dialog.add(expressionLayout);
		dialog.getFooter().add(addExpression);
		dialog.getFooter().add(saveExpression);
		dialog.getFooter().add(cancelExpression);

		List<CampaignFormElement> listofelements = campaignFormMetaDto.getCampaignFormElements();
		List<String> listofthem = new ArrayList<>();
		for (CampaignFormElement campaignFormElement : listofelements) {
			listofthem.add(campaignFormElement.getId());
		}
		ids.setItems(listofthem);

		saveExpression.addClickListener(e -> {
			expression.setValue(expressionEdit.getValue());
			dialog.close();
		});

		addExpression.addClickListener(e -> {

			if (ids.getValue() == null) {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.setPosition(Position.MIDDLE);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph("Please select an id to be added");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
			} else if (!ids.getValue().toString().isEmpty() && ids.getValue().toString() != null) {

				StringBuilder editingExpression = new StringBuilder(expressionEdit.getValue());
				editingExpression.append(" " + ids.getValue());
				expressionEdit.setValue(editingExpression.toString());
				ids.clear();
			}
		});
	}

	public List<CampaignFormElement> getGridData() {
		return grid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
	}

}
