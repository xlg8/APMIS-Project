///*******************************************************************************
// * SORMAS® - Surveillance Outbreak Response Management & Analysis System
// * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <https://www.gnu.org/licenses/>.
// *******************************************************************************/
//package com.cinoteck.application.utils.importutils;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.vaadin.olli.FileDownloadWrapper;
//
//import com.vaadin.flow.component.Text;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.checkbox.Checkbox;
//import com.vaadin.flow.component.html.Anchor;
//import com.vaadin.flow.component.html.Div;
//import com.vaadin.flow.component.html.Label;
//import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.icon.Icon;
//import com.vaadin.flow.component.icon.VaadinIcon;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.server.StreamResource;
//import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
//import com.vaadin.server.FileDownloader;
//import com.vaadin.server.Page;
//
//
//import de.symeda.sormas.api.i18n.Captions;
//import de.symeda.sormas.api.i18n.I18nProperties;
//import de.symeda.sormas.api.i18n.Strings;
//import de.symeda.sormas.api.importexport.DatabaseTable;
//import de.symeda.sormas.api.importexport.DatabaseTableType;
//import de.symeda.sormas.api.utils.DateHelper;
//import com.vaadin.flow.component.orderedlayout.FlexComponent;
//
//
//public class DatabaseExportView extends VerticalLayout {
//
//	private static final long serialVersionUID = 1557269026685787333L;
//
//	public static final String VIEW_NAME = "/database-export";
//
//	private VerticalLayout databaseExportLayout;
//	private Map<Checkbox, DatabaseTable> databaseTableToggles;
//
//	public DatabaseExportView() {
//
////		super(VIEW_NAME);
//
//		databaseTableToggles = new HashMap<>();
//		databaseExportLayout = new VerticalLayout();
//		databaseExportLayout.setSpacing(false);
//		databaseExportLayout.setMargin(false);
//		HorizontalLayout headerLayout = new HorizontalLayout();
//		headerLayout.setSpacing(true);
//		headerLayout.setMargin(false);
//		Label infoLabel = new Label(I18nProperties.getString(Strings.infoDatabaseExportTables));
//		headerLayout.add(infoLabel);
////		headerLayout.setComponentAlignment(infoLabel, Alignment.CENTER);
//		headerLayout.add(createSelectionButtonsLayout());
//		databaseExportLayout.add(headerLayout);
//		databaseExportLayout.add(createDatabaseTablesLayout());
//
//		Button exportButton = new Button(Captions.export);//ButtonHelper.createIconButton(Captions.export, VaadinIcons.DOWNLOAD, null, ValoTheme.BUTTON_PRIMARY);
//
//	Label exportLabel = new Label("Download");
//	
//		
//		StreamResource streamResource = DownloadUtil
//			.createDatabaseExportStreamResource(this, "sormas_export_" + DateHelper.formatDateForExport(new Date()) + ".zip", "application/zip");
//		FileDownloadWrapper fileDownloader = new FileDownloadWrapper(streamResource);
////		exportLabel.addc
//		
//		fileDownloader.wrapComponent(exportButton);
//
//		databaseExportLayout.add(exportButton);
//		databaseExportLayout.setMargin(true);
//		databaseExportLayout.setSpacing(true);
//
//		add(databaseExportLayout);
//	}
//
////	public void showExportErrorNotification() {
//////		new Notification(
//////			I18nProperties.getString(Strings.headingDatabaseExportFailed),
//////			I18nProperties.getString(Strings.messageDatabaseExportFailed),
//////			Type.ERROR_MESSAGE,
//////			false).show(Page.getCurrent());
////		
////		Noti
////	}
//	
//	public void  showExportErrorNotification() {
//	    Notification notification = new Notification();
//	    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//
//	    Icon icon = VaadinIcon.WARNING.create();
//	  
//	    Button retryBtn = new Button("Retry",
//	            clickEvent -> notification.close());
//	    retryBtn.getStyle().set("margin", "0 0 0 var(--lumo-space-l)");
//
//	    HorizontalLayout layout = new HorizontalLayout(icon, retryBtn,
//	          notification);
//	    layout.setAlignItems(FlexComponent.Alignment.CENTER);
//
//	    notification.add(layout);
//	    notification.open();
////	    return notification;
//	}
//
//	private HorizontalLayout createSelectionButtonsLayout() {
//		HorizontalLayout selectionButtonsLayout = new HorizontalLayout();
//		selectionButtonsLayout.setMargin(false);
//		selectionButtonsLayout.setSpacing(true);
//
//		Button selectAll = new Button(Captions.actionSelectAll);// ButtonHelper.createButton(Captions.actionSelectAll, 
//		selectAll.addClickListener(e -> {
//		
//			for (Checkbox checkBox : databaseTableToggles.keySet()) {
//				checkBox.setValue(true);
//			}
//		});
//
//		selectionButtonsLayout.add(selectAll);
//
//		Button selectAllSormasData = new Button(Captions.exportSelectSormasData);//ButtonHelper.createButton(Captions.exportSelectSormasData, 
//		selectAllSormasData.addClickListener(e-> {
//			for (Checkbox checkBox : databaseTableToggles.keySet()) {
//				if (databaseTableToggles.get(checkBox).getDatabaseTableType() == DatabaseTableType.SORMAS) {
//					checkBox.setValue(true);
//				} else {
//					checkBox.setValue(false);
//				}
//			}
//		});
//
//		selectionButtonsLayout.add(selectAllSormasData);
//
//		Button deselectAll =  new Button(Captions.actionDeselectAll);//ButtonHelper.createButton(Captions.actionDeselectAll, 
//		deselectAll.addClickListener(e -> {
//			for (Checkbox checkBox : databaseTableToggles.keySet()) {
//				checkBox.setValue(false);
//			}
//		})	;
////		e -> {
////			for (CheckBox checkBox : databaseTableToggles.keySet()) {
////				checkBox.setValue(false);
////			}
////		}, ValoTheme.BUTTON_LINK);
//
//		selectionButtonsLayout.add(deselectAll);
//
//		return selectionButtonsLayout;
//	}
//
//	private HorizontalLayout createDatabaseTablesLayout() {
//		HorizontalLayout databaseTablesLayout = new HorizontalLayout();
//		databaseTablesLayout.setMargin(false);
//		databaseTablesLayout.setSpacing(true);
//
//		VerticalLayout sormasDataLayout = new VerticalLayout();
//		sormasDataLayout.setMargin(false);
//		sormasDataLayout.setSpacing(false);
//		Label sormasDataHeadline = new Label(I18nProperties.getCaption(Captions.exportSormasData));
////		sormasDataHeadline.addClassName("lowHeaderText");
////		CssStyles.style(sormasDataHeadline, CssStyles.H4);
//		sormasDataLayout.add(sormasDataHeadline);
//
//		VerticalLayout infrastructureDataLayout = new VerticalLayout();
//		infrastructureDataLayout.setMargin(false);
//		infrastructureDataLayout.setSpacing(false);
//		Label infrastructureDataHeadline = new Label(I18nProperties.getCaption(Captions.exportInfrastructureData));
////		CssStyles.style(infrastructureDataHeadline, CssStyles.H4);
//		infrastructureDataLayout.add(infrastructureDataHeadline);
//
//		VerticalLayout configurationDataLayout = new VerticalLayout();
//		configurationDataLayout.setMargin(false);
//		configurationDataLayout.setSpacing(false);
//		Label configurationDataHeadline = new Label(I18nProperties.getCaption(Captions.exportConfigurationData));
////		CssStyles.style(configurationDataHeadline, CssStyles.H4);
//		configurationDataLayout.add(configurationDataHeadline);
//
//		for (DatabaseTable databaseTable : DatabaseTable.values()) {
//			Checkbox checkBox = new Checkbox(databaseTable.toString());
//			int indent = getIndent(databaseTable);
//			if (indent == 1) {
////				CssStyles.style(checkBox, CssStyles.INDENT_LEFT_1);
//			} else if (indent == 2) {
////				CssStyles.style(checkBox, CssStyles.INDENT_LEFT_2);
//			} else if (indent == 3) {
////				CssStyles.style(checkBox, CssStyles.INDENT_LEFT_3);
//			}
//
//			switch (databaseTable.getDatabaseTableType()) {
//			case SORMAS:
//				sormasDataLayout.add(checkBox);
//				break;
//			case INFRASTRUCTURE:
//				infrastructureDataLayout.add(checkBox);
//				break;
//			case CONFIGURATION:
//				configurationDataLayout.add(checkBox);
//				break;
//			default:
//				throw new IllegalArgumentException(databaseTable.getDatabaseTableType().toString());
//			}
//
//			databaseTableToggles.put(checkBox, databaseTable);
//		}
//
//		databaseTablesLayout.add(sormasDataLayout);
//		databaseTablesLayout.add(infrastructureDataLayout);
//		databaseTablesLayout.add(configurationDataLayout);
//		return databaseTablesLayout;
//	}
//
//	private int getIndent(DatabaseTable databaseTable) {
//
//		int indent = 0;
//		while (databaseTable.getParentTable() != null) {
//			indent++;
//			databaseTable = databaseTable.getParentTable();
//		}
//		return indent;
//	}
//
////	@Override
////	public void enter(ViewChangeEvent event) {
////		super.enter(event);
////	}
//
//	public Map<Checkbox, DatabaseTable> getDatabaseTableToggles() {
//		return databaseTableToggles;
//	}
//}
