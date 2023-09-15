package com.cinoteck.application.views.uiformbuilder;

import java.util.HashMap;
import java.util.Map;

import com.cinoteck.application.views.MainLayout;
import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.campaign.form.CampaignFormElementType;

@PageTitle("APMIS-UI-Builder")
@Route(value = "UI-Builder", layout = MainLayout.class)
public class FormBuilderView extends FormLayout {

	private final Gson gson = new Gson();
	private final Map<String, String> jsonData = new HashMap<>();

	ComboBox<CampaignFormElementType> formType = new ComboBox<CampaignFormElementType>("Type");

	ComboBox<String> formId = new ComboBox<>("Id");
	TextField caption = new TextField("Label");
	Checkbox important = new Checkbox("Important");
	TextField optionsKey = new TextField("Option Keys");
	TextField optionsValue = new TextField("Option Values");
	ComboBox<String> expression = new ComboBox<>("Expression");
	ComboBox<String> dependingOn = new ComboBox<>("Depending On");
	ComboBox<String> dependingOnValues = new ComboBox<>("Depending On Values");
	ComboBox<String> styles = new ComboBox<>("Styles");
	ComboBox<String> constraints = new ComboBox<>("Constraints");
	TextField errorMessage = new TextField("ErrorMessage");

	Button generateJson = new Button("Generate Json");

	TextArea jsonContainer = new TextArea("Generated Json");

	public FormBuilderView() {

		this.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		this.setColspan(jsonContainer, 2);
		this.setColspan(generateJson, 2);

		configure();
		valueChange();
		add(formType, formId, caption, important, optionsKey, optionsValue, expression, dependingOn, dependingOnValues,
				styles, constraints, errorMessage, generateJson, jsonContainer);		
	}

	public void configure() {

		dependingOnValues.setItems("Yes", "No");
		formType.setItems(CampaignFormElementType.values());
		constraints.setItems("Expression", "Range");
		styles.setItems("inline", "row", "first", "col-1", "col-2", "col-3", 
				"col-4", "col-5", "col-6", "col-7", "col-8", "col-9", "col-10", "col-11", "col-12");
	}

	public void valueChange() {
		
		formType.addValueChangeListener(e -> {
			
			if (formType.getValue().toString().toLowerCase().equals("section")) {
				
				caption.setVisible(false);
				important.setVisible(false);
				optionsKey.setVisible(false);
				optionsValue.setVisible(false);
				expression.setVisible(false);
				dependingOn.setVisible(false);
				dependingOnValues.setVisible(false);
				styles.setVisible(false);
				constraints.setVisible(false);
				errorMessage.setVisible(false);
			} else {
				
				caption.setVisible(true);
				important.setVisible(true);
				optionsKey.setVisible(true);
				optionsValue.setVisible(true);
				expression.setVisible(true);
				dependingOn.setVisible(true);
				dependingOnValues.setVisible(true);
				styles.setVisible(true);
				constraints.setVisible(true);
				errorMessage.setVisible(true);
			}
		});
	}

}
