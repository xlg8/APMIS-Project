package com.cinoteck.application;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import de.symeda.sormas.api.i18n.I18nProperties;

public class RowCount extends HorizontalLayout {

	private Label labelValue;

	public RowCount(String labelCaption, long rowsCount) {
		createLayout(labelCaption, rowsCount);
	}

	private void createLayout(String caption, long rowsCount) {

		setMargin(false);
//		addStyleName(CssStyles.VSPACE_4);
		setSpacing(true);
		setWidth(100, Unit.PERCENTAGE);
//		setDefaultComponentAlignment(Alignment.CENTER);

		Label labelCaption = new Label(I18nProperties.getString(caption) + ":");
//		labelCaption.addStyleNames(CssStyles.LABEL_BOLD, CssStyles.VSPACE_TOP_NONE, CssStyles.ALIGN_RIGHT);
		labelCaption.setSizeFull();
		add(labelCaption);
//		setExpandRatio(labelCaption, 1);

		labelValue = new Label(String.valueOf(rowsCount));
//		labelValue.addStyleNames(CssStyles.LABEL_BOLD, CssStyles.VSPACE_TOP_NONE, CssStyles.ALIGN_RIGHT);
		labelValue.setSizeUndefined();
		add(labelValue);
	}

	public void update(long rowsCount) {
		labelValue.setText(String.valueOf(rowsCount));
	}

}
