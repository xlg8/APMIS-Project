package com.cinoteck.application.views.uiformbuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.docx4j.model.datastorage.XPathEnhancerParser.main_return;

import com.cinoteck.application.views.MainLayout;
import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.campaign.form.CampaignFormElementType;

@PageTitle("APMIS-UI-Builder")
@Route(value = "UI-Builder", layout = MainLayout.class)
public class FormBuilderView extends FormLayout {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	private final Gson gson = new Gson();
	private final Map<String, String> jsonData = new LinkedHashMap<String, String>();

	ComboBox<CampaignFormElementType> formType = new ComboBox<CampaignFormElementType>("Type");
	TextField formId = new TextField("Id");
	TextField caption = new TextField("Label");
	Checkbox important = new Checkbox("Important");
	TextField options = new TextField("Option");
	TextField expression = new TextField("Expression");
	TextField dependingOn = new TextField("Depending On");
	ComboBox<String> dependingOnValues = new ComboBox<>("Depending On Values");
	MultiSelectComboBox<String> styles = new MultiSelectComboBox<>("Styles");

	ComboBox<String> constraints = new ComboBox<>("Constraints");
	NumberField min = new NumberField("Min");
	NumberField max = new NumberField("Max");

	TextField errorMessage = new TextField("ErrorMessage");

	Button generateJson = new Button("Generate Json");

	Button clearTextArea = new Button("Clear Generated Json");

	TextArea jsonContainer = new TextArea("Generated Json");

	StringBuilder appender;

	public FormBuilderView() {

		this.addClassName("uibuilderview");

		this.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));
		this.setColspan(jsonContainer, 2);
		this.setColspan(generateJson, 2);
		this.setColspan(clearTextArea, 2);

		expression.setVisible(false);
		dependingOn.setVisible(false);
		dependingOnValues.setVisible(false);
		min.setVisible(false);
		max.setVisible(false);

		configure();
		valueChange();
		add(formType, formId, caption, important, options, expression, dependingOn, dependingOnValues, styles,
				constraints, min, max, errorMessage, generateJson, clearTextArea, jsonContainer);
	}

	public void configure() {

		options.setHelperText("Enter options keys in a comma seperated format like so Urban, Urban, Rural, Rural");
		expression.setHelperText("Please enter expression value with care");
		formId.setHelperText("Append \"-readonly\" at the end of the Id value. If you want it to be Read Only");
		dependingOnValues.setItems("Yes", "No");
		formType.setItems(CampaignFormElementType.values());
		constraints.setItems("Expression", "Range");
		styles.setItems("inline", "row", "first", "col-1", "col-2", "col-3", "col-4", "col-5", "col-6", "col-7",
				"col-8", "col-9", "col-10", "col-11", "col-12");
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
			} else if (e.getValue().toString().toLowerCase().equals("number")
					|| e.getValue().toString().toLowerCase().equals("range")) {

				expression.setVisible(true);
				dependingOn.setVisible(true);
				dependingOnValues.setVisible(true);
			} else {

				caption.setVisible(true);
				important.setVisible(true);
				options.setVisible(true);
				styles.setVisible(true);
				constraints.setVisible(true);
				errorMessage.setVisible(true);
			}

			if (!e.getValue().toString().trim().isEmpty()) {
				jsonData.put("type", e.getValue().toString());
			}

		});

		formId.addValueChangeListener(e -> {

			if (!e.getValue().toString().trim().isEmpty()) {
				jsonData.put("id", e.getValue().toString());
			}
		});

		caption.addValueChangeListener(e -> {

			if (!e.getValue().toString().trim().isEmpty()) {
				jsonData.put("caption", e.getValue().toString());
			}
		});

		important.addClickListener(e -> {

//			important.setValue(true);
			if (e.getSource().getValue() != false) {
				jsonData.put("important", "true");
			}
		});

		dependingOn.addValueChangeListener(e -> {

			if (e.getValue().toString().trim() != null) {

				jsonData.put("dependingOn", e.getValue());
			}
		});

		dependingOnValues.addValueChangeListener(e -> {

			if (e.getValue().toString().trim() != null) {
				jsonData.put("dependingOnValues", e.getValue().toLowerCase());
			}
		});

		styles.addValueChangeListener(e -> {

			if (e.getValue().toString().trim() != null) {
				jsonData.put("styles", e.getValue().toString());
			}
		});

		constraints.addValueChangeListener(e -> {

			if (e.getValue().toString().trim() != null && e.getValue().toString().toLowerCase().equals("expression")) {
				jsonData.put("constraints", "expression");
				min.setVisible(false);
				max.setVisible(false);
			} else {
				min.setVisible(true);
				max.setVisible(true);
			}
		});

		min.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				jsonData.put("constraints", "[min=" + e.getValue().toString() + ", ");
			}
		});

		max.addValueChangeListener(e -> {

			if (e.getValue() != null) {

				String minValue = jsonData.get("constraints");
				if (minValue != null) {

					jsonData.put("constraints", minValue + "max=" + e.getValue().toString() + "]");
				}
			}

		});

		errorMessage.addValueChangeListener(e -> {

			if (e.getValue().toString().trim() != null) {

				jsonData.put("errormessage", e.getValue().toString());
			}
		});

		generateJson.addClickListener(e -> {

			if (!options.getValue().trim().isEmpty()) {

				StringBuilder optionDataBuild = new StringBuilder();

				String[] splittingString = options.getValue().toString().split(",");

				for (int i = 0; i < splittingString.length; i += 2) {

					if (i + 1 < splittingString.length) {

						if (i == 0) {
							optionDataBuild.append("[{key:" + splittingString[i].toString());
							optionDataBuild.append(", caption:" + splittingString[i].toString() + "}, ");
						} else {
							if (i == splittingString.length - 2) {
								optionDataBuild.append("{key:" + splittingString[i].toString());
								optionDataBuild.append(", caption:" + splittingString[i].toString() + "}]");
							} else {
								optionDataBuild.append("{key:" + splittingString[i].toString());
								optionDataBuild.append(", caption:" + splittingString[i].toString() + "}, ");
							}
						}
					}
				}

				jsonData.put("options", optionDataBuild.toString());
				generateJson();
			} else {
				generateJson();
			}

		});

		clearTextArea.addClickListener(e -> {

			jsonContainer.clear();
		});

	}

	public void generateJson() {

		if (!jsonContainer.getValue().trim().isEmpty()) {
			appender = new StringBuilder(jsonContainer.getValue() + ",");
		} else {

			appender = new StringBuilder(jsonContainer.getValue());
		}

		if (!jsonData.isEmpty()) {

			appender.append(gson.toJson(jsonData));
			String main = appender.toString().replaceAll("\\\\u003d", "=");
			jsonContainer.setValue(main);

			jsonData.clear();
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
			styles.setValue(Collections.emptySet());
			errorMessage.setValue("");
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

			Paragraph text = new Paragraph("Please enter data");

			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);

			notification.add(layout);
			notification.open();
		}
	}

}
