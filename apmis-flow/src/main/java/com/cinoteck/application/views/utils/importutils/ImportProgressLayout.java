/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.cinoteck.application.views.utils.importutils;



import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

@SuppressWarnings("serial")
public class ImportProgressLayout extends VerticalLayout {

	// Components
	private ProgressBar progressBar;
	private Label processedImportsLabel;
	private Label successfulImportsLabel;
	private Label importErrorsLabel;
	private Label importSkipsLabel;
	private Label importDuplicatesLabel;
	private Button closeCancelButton;
	private HorizontalLayout infoLayout;
	private Label infoLabel;

	private ProgressBar progressCircle;
	private Icon errorIcon;
	private Icon successIcon;
	private Icon warningIcon;
	private Component currentInfoComponent;

	private ClickListener cancelListener;

	// Counts
	private int processedImportsCount;
	private int successfulImportsCount;
	private int importErrorsCount;
	private int importSkipsCount;
	private int importDuplicatesCount;
	private int totalCount;

	private UI currentUI;

	public ImportProgressLayout(int totalCount, UI currentUI, Runnable cancelCallback, boolean duplicatesPossible) {
		this(totalCount, currentUI, cancelCallback, duplicatesPossible, true);
	}

	public ImportProgressLayout(int totalCount, UI currentUI, Runnable cancelCallback, boolean duplicatesPossible, boolean skipPossible) {
		this.totalCount = totalCount;
		this.currentUI = currentUI;

		setWidthFull();
		setMargin(true);

		// Info text and icon/progress circle
		infoLayout = new HorizontalLayout();
		infoLayout.setWidth(100, Unit.PERCENTAGE);
		infoLayout.setSpacing(true);
		initializeInfoComponents();
		currentInfoComponent = progressCircle;
		infoLayout.add(currentInfoComponent);
		infoLabel = new Label(String.format(I18nProperties.getString(Strings.infoImportProcess), totalCount));
//		infoLabel.setContentMode(ContentMode.HTML);
		infoLayout.add(infoLabel);
//		infoLayout.setExpandRatio(infoLabel, 1);

		add(infoLayout);

		// Progress bar
		progressBar = new ProgressBar(0.0f, 100.0f);
//		CssStyles.style(progressBar, CssStyles.VSPACE_TOP_3);
		add(progressBar);
		progressBar.setWidth(100, Unit.PERCENTAGE);

		// Progress info
		HorizontalLayout progressInfoLayout = new HorizontalLayout();
//		CssStyles.style(progressInfoLayout, CssStyles.VSPACE_TOP_5);
		progressInfoLayout.setSpacing(true);
		processedImportsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importProcessed), 0, totalCount));
		progressInfoLayout.add(processedImportsLabel);
		successfulImportsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importImports), 0));
//		CssStyles.style(successfulImportsLabel, CssStyles.LABEL_POSITIVE);
		progressInfoLayout.add(successfulImportsLabel);
		importErrorsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importErrors), 0));
//		CssStyles.style(importErrorsLabel, CssStyles.LABEL_CRITICAL);
		progressInfoLayout.add(importErrorsLabel);
		importDuplicatesLabel = new Label(String.format(I18nProperties.getCaption(Captions.importDuplicates), 0));
//		CssStyles.style(importDuplicatesLabel, CssStyles.LABEL_WARNING);
		if (duplicatesPossible) {
			progressInfoLayout.add(importDuplicatesLabel);
		}
		importSkipsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importSkips), 0));
//		CssStyles.style(importSkipsLabel, CssStyles.LABEL_MINOR);
		if (skipPossible) {
			progressInfoLayout.add(importSkipsLabel);
		}
		add(progressInfoLayout);
//		setComponentAlignment(progressInfoLayout, Alignment.TOP_RIGHT);
		
		progressInfoLayout.setAlignItems(Alignment.END);

		// Cancel button
		cancelListener = e -> cancelCallback.run();

		closeCancelButton = new Button(Captions.actionCancel);
		
		closeCancelButton.addClickListener(e->{
		cancelCallback.run();
		});
		
		add(closeCancelButton);
//		setComponentAlignment(closeCancelButton, Alignment.MIDDLE_RIGHT);
		
	}

	private void initializeInfoComponents() {
		progressCircle = new ProgressBar();
		progressCircle.setIndeterminate(true);
//		CssStyles.style(progressCircle, "v-progressbar-indeterminate-large");

		errorIcon = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
		errorIcon.getStyle().set("color", "red");
//		errorIcon.setWidth(35, Unit.PIXELS);
		successIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		successIcon.getStyle().set("color", "green");
//		successIcon.setWidth(35, Unit.PIXELS);
		warningIcon = new Icon(VaadinIcon.WARNING);
		warningIcon.getStyle().set("color", "yellow");
//		warningIcon.setWidth(35, Unit.PIXELS);
	}

	public void updateProgress(ImportLineResult result) {
		currentUI.access(() -> {
			processedImportsCount++;
			if (result == ImportLineResult.SUCCESS) {
				successfulImportsCount++;
				successfulImportsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importImports),successfulImportsCount ));
//				successfulImportsLabel.setValue(String.format(I18nProperties.getCaption(Captions.importImports), successfulImportsCount));
			} else if (result == ImportLineResult.ERROR) {
				importErrorsCount++;
				
				importErrorsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importErrors), importErrorsCount));
				
				
//				importErrorsLabel.setValue(String.format(I18nProperties.getCaption(Captions.importErrors), importErrorsCount));
			} else if (result == ImportLineResult.SKIPPED) {
				importSkipsCount++;
				
				importSkipsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importSkips), importSkipsCount));

//				importSkipsLabel.setValue(String.format(I18nProperties.getCaption(Captions.importSkips), importSkipsCount));
			} else if (result == ImportLineResult.DUPLICATE) {
				importDuplicatesCount++;
				
				importDuplicatesLabel = new Label(String.format(I18nProperties.getCaption(Captions.importDuplicates), importDuplicatesCount));

//				importDuplicatesLabel.setValue(String.format(I18nProperties.getCaption(Captions.importDuplicates), importDuplicatesCount));
			}
			processedImportsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importProcessed), processedImportsCount, totalCount));

			
//			processedImportsLabel.setValue(String.format(I18nProperties.getCaption(Captions.importProcessed), processedImportsCount, totalCount));
			progressBar.setValue((float) processedImportsCount / (float) totalCount);
		});
	}

	public void makeClosable(Runnable closeCallback) {
		
		closeCancelButton.setText(I18nProperties.getCaption(Captions.actionClose));
//		closeCancelButton.get .removeListener(cancelListener);
		closeCancelButton.addClickListener(e -> closeCallback.run());
	}

	public void setInfoLabelText(String text) {
		infoLabel.setText(text);
	}

	public void displayErrorIcon() {
		infoLayout.remove(currentInfoComponent);
		currentInfoComponent = errorIcon;
		infoLayout.addComponentAsFirst(currentInfoComponent);
	}

	public void displaySuccessIcon() {
		infoLayout.remove(currentInfoComponent);
		currentInfoComponent = successIcon;
		infoLayout.addComponentAsFirst(currentInfoComponent);
	}

	public void displayWarningIcon() {
		infoLayout.remove(currentInfoComponent);
		currentInfoComponent = warningIcon;
		infoLayout.addComponentAsFirst(currentInfoComponent);
	}
}
