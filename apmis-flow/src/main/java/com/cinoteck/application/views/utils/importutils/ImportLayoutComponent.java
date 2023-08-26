package com.cinoteck.application.views.utils.importutils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

public class ImportLayoutComponent extends VerticalLayout {

	private static final long serialVersionUID = 3281659031721493105L;

	private Label headlineLabel;
	private Label infoTextLabel;
	private Button button;
	private Checkbox checkbox;

	public ImportLayoutComponent(int step, String headline, String infoText, Button buttonIcon, String buttonCaption) {
		this(step, headline, infoText, buttonIcon, buttonCaption, null, null);
	}

	public ImportLayoutComponent(int step, String headline, String infoText, Button buttonIcon, String buttonCaption,
			String checkboxCaption, String checkboxDescription) {
		setSpacing(false);
		setMargin(false);

		headlineLabel = new Label(I18nProperties.getString(Strings.step) + " " + step + ": " + headline);
		// CssStyles.style(headlineLabel, CssStyles.H3);
		add(headlineLabel);

		if (infoText != null) {
			infoTextLabel = new Label(infoText);
			add(infoTextLabel);
		}

		if (checkboxCaption != null) {
			checkbox = new Checkbox(checkboxCaption);
			checkbox.setValue(false);
			if (checkboxDescription != null) {
				HorizontalLayout checkboxBar = new HorizontalLayout();
				// checkboxBar.setDefaultComponentAlignment(Alignment.CENTER);
//				checkboxBar .setDescription(checkboxDescription);
//				CssStyles.style(checkboxBar, CssStyles.VSPACE_TOP_3);
				checkboxBar.add(checkbox);
				Label labelInfo = new Label("i");
				checkboxBar.add(labelInfo);
				add(checkboxBar);
			} else {
//				CssStyles.style(checkbox, CssStyles.VSPACE_TOP_3);
				add(checkbox);
			}
		}

		if (buttonCaption != null) {
			button = new Button();
			button.setText(buttonCaption);
			button.setIcon(buttonIcon);
			button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			add(button);
		}
	}

	public Button getButton() {
		return button;
	}

	public Checkbox getCheckbox() {
		return checkbox;
	}
}
