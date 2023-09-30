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



import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

@SuppressWarnings("serial")
public class ImportProgressLayout extends VerticalLayout {

	// Components
	private ProgressBar progressBarMain;
	private Span processedImportsLabel = new Span();
	private Span successfulImportsLabel = new Span();
	private Span importErrorsLabel = new Span();
	private Span importSkipsLabel = new Span();
	private Span importDuplicatesLabel = new Span();
	private Button closeCancelButton;
	private HorizontalLayout infoLayout;
	private Span infoLabel = new Span();

	private ProgressBar progressCircle;
	private Icon errorIcon;
	private Icon successIcon;
	private Icon warningIcon;
	private Component currentInfoComponent;

//	private ClickListener cancelListener;

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
		
		infoLabel.removeAll();
		Span infoLabel_s = new Span(new Html("<div>"+String.format(I18nProperties.getString(Strings.infoImportProcess), totalCount)+"</div>"));
		
		infoLabel.add(infoLabel_s);
		
		
		
//		Notification.show(System.currentTimeMillis()+"");
//		infoLabel.setContentMode(ContentMode.HTML);
		infoLayout.add(infoLabel);
//		infoLayout.setExpandRatio(infoLabel, 1);

		add(infoLayout);

		// Progress bar
		progressBarMain = new ProgressBar(0.0f, 100.0f);
//		CssStyles.style(progressBar, CssStyles.VSPACE_TOP_3);
		add(progressBarMain);
		progressBarMain.setWidth(100, Unit.PERCENTAGE);

		// Progress info
		HorizontalLayout progressInfoLayout = new HorizontalLayout();
//		CssStyles.style(progressInfoLayout, CssStyles.VSPACE_TOP_5);
		progressInfoLayout.setSpacing(true);
		
		Label _processedImportsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importProcessed), 0, totalCount));
		processedImportsLabel.add(_processedImportsLabel);
		progressInfoLayout.add(processedImportsLabel);
		
		Label _successfulImportsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importImports), 0));
		successfulImportsLabel.add(_successfulImportsLabel);
		successfulImportsLabel.getStyle().set("color", "green");
		progressInfoLayout.add(successfulImportsLabel);
		
		Label _importErrorsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importErrors), 0));
		importErrorsLabel.add(_importErrorsLabel);
		progressInfoLayout.add(importErrorsLabel);
		
		Label _importDuplicatesLabel = new Label(String.format(I18nProperties.getCaption(Captions.importDuplicates), 0));
		importDuplicatesLabel.add(_importDuplicatesLabel);
		if (duplicatesPossible) {
			progressInfoLayout.add(importDuplicatesLabel);
		}
		
		importSkipsLabel = new Span();
		Label _importSkipsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importSkips), 0));
		importSkipsLabel.add(_importSkipsLabel);
//		CssStyles.style(importSkipsLabel, CssStyles.LABEL_MINOR);
		if (skipPossible) {
			progressInfoLayout.add(importSkipsLabel);
		}
		add(progressInfoLayout);
//		setComponentAlignment(progressInfoLayout, Alignment.TOP_RIGHT);
		
		progressInfoLayout.setAlignItems(Alignment.END);

		// Cancel button
//		cancelListener = e -> cancelCallback.run();

		closeCancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel));
		
		closeCancelButton.addClickListener(e->{
		cancelCallback.run();
		});
		
		add(closeCancelButton);
//		setComponentAlignment(closeCancelButton, Alignment.MIDDLE_RIGHT);
		
	}

	private void initializeInfoComponents() {
		progressCircle = new ProgressBar();
		
		progressCircle.setIndeterminate(true);
		progressCircle.setWidth("15%");
//		CssStyles.style(progressCircle, "v-progressbar-indeterminate-large");

		errorIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE_O);
		errorIcon.getStyle().set("color", "red!important");
//		errorIcon.setWidth(35, Unit.PIXELS);
		successIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		successIcon.getStyle().set("color", "green!important");
//		successIcon.setWidth(35, Unit.PIXELS);
		warningIcon = new Icon(VaadinIcon.WARNING);
		warningIcon.getStyle().set("color", "#abab3d!important");
//		warningIcon.setWidth(35, Unit.PIXELS);
	}

	public void updateProgress(ImportLineResult result) {
//		System.out.println("__________________________________________cc____________"+result);
		currentUI.access(() -> {
			
			processedImportsCount++;
			System.out.println("updateProgress(ImportLineResult result): "+processedImportsCount);
			
			if (result == ImportLineResult.SUCCESS) {
				System.out.println("________if (result == ImportLineResult.SUCCESS) {___________");
//				Notification.show("++++++++++++"+ImportLineResult.SUCCESS.name());
				successfulImportsCount++;
				successfulImportsLabel.removeAll();
				Label _successfulImportsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importImports),successfulImportsCount ));
				successfulImportsLabel.add(_successfulImportsLabel);
//				successfulImportsLabel.setValue(String.format(I18nProperties.getCaption(Captions.importImports), successfulImportsCount));
			} else if (result == ImportLineResult.ERROR) {
				
				importErrorsCount++;
//				Notification.show(ImportLineResult.ERROR.name()+"   ============  " + importErrorsCount);
				importErrorsLabel.removeAll();
				Label _importErrorsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importErrors), importErrorsCount));
				_importErrorsLabel.getStyle().set("color", "error");
				importErrorsLabel.add(_importErrorsLabel);
				
//				importErrorsLabel.setValue(String.format(I18nProperties.getCaption(Captions.importErrors), importErrorsCount));
			} else if (result == ImportLineResult.SKIPPED) {
				importSkipsCount++;
				importSkipsLabel.removeAll();
				Label _importSkipsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importSkips), importSkipsCount));
				importSkipsLabel.add(_importSkipsLabel);

//				importSkipsLabel.setValue(String.format(I18nProperties.getCaption(Captions.importSkips), importSkipsCount));
			} else if (result == ImportLineResult.DUPLICATE) {
				importDuplicatesCount++;
				importDuplicatesLabel.removeAll();
				Label _importDuplicatesLabel = new Label(String.format(I18nProperties.getCaption(Captions.importDuplicates), importDuplicatesCount));
				importDuplicatesLabel.add(_importDuplicatesLabel);

//				importDuplicatesLabel.setValue(String.format(I18nProperties.getCaption(Captions.importDuplicates), importDuplicatesCount));
			}
			processedImportsLabel.removeAll();
			Label _processedImportsLabel = new Label(String.format(I18nProperties.getCaption(Captions.importProcessed), processedImportsCount, totalCount));
			processedImportsLabel.add(_processedImportsLabel);

			//Notification.show("currentUI.access(() -> {.....");
//			processedImportsLabel.setValue(String.format(I18nProperties.getCaption(Captions.importProcessed), processedImportsCount, totalCount));
			float resultx = (float) processedImportsCount / totalCount;
			float percentage = resultx * 100;
			System.out.println("sssssssssssssssssssssss "+percentage);
			progressBarMain.setValue(percentage);
		});
	}

	public void makeClosable(Runnable closeCallback) {
		
		closeCancelButton.setText(I18nProperties.getCaption(Captions.actionClose));
//		closeCancelButton.get .removeListener(cancelListener);
		closeCancelButton.addClickListener(e -> closeCallback.run());
	}

	public void setInfoLabelText(String text, VaadinIcon ic, String infoType) {
		System.out.println("ssssssssetInfoLabelTextsssssss "+text);
		infoLabel.removeAll();
		
		Span infoLabel_ = new Span(new Html("<div>"+text+"</div>"));
		
		infoLabel.add(infoLabel_);
		
		infoLabel.getStyle().set("color", infoType);
//		infoLabel.getElement().getThemeList().add(infoType);
	}

	public void displayErrorIcon() {
		infoLayout.remove(currentInfoComponent);
		currentInfoComponent = errorIcon;
	//	Notification.show(System.currentTimeMillis()+"SettingerrorIcon");
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
	
	private Icon createIcon(VaadinIcon vaadinIcon) {
	        Icon icon = vaadinIcon.create();
	        icon.getStyle().set("padding", "var(--lumo-space-xs");
	        return icon;
	    }
}
