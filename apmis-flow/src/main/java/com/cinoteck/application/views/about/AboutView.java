package com.cinoteck.application.views.about;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.utils.SessionTimeout;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.report.JsonDictionaryReportModelDto;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
public class AboutView extends VerticalLayout {

	Grid<JsonDictionaryReportModelDto> grid = new Grid<>(JsonDictionaryReportModelDto.class, false);

	UserProvider userProvider = new UserProvider();
	
	List<JsonDictionaryReportModelDto> analysis = FacadeProvider.getCampaignFormDataFacade().getByJsonFormDefinitonToCSV();
	GridListDataView<JsonDictionaryReportModelDto> dataView ;
	
 

    public AboutView() {	
    	if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);			
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
    	FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
    	Div aboutView = new Div();
    	SessionTimeout sessionTimeout = new SessionTimeout();

		aboutView.getStyle().set("height", "100%");
		aboutView.getStyle().set("padding-left", "90px");
		aboutView.getStyle().set("padding-right", "90px");

		Div apmisImageContainer = new Div();
		apmisImageContainer.getStyle().set("height", "140px");
		apmisImageContainer.getStyle().set("display", "flex");
		apmisImageContainer.getStyle().set("justify-content", "center");
		apmisImageContainer.getStyle().set("margin-bottom", "30px");
		apmisImageContainer.getStyle().set("padding-top", "30px");

		Image img = new Image("images/apmislogo.png", "APMIS-LOGO");
		img.getStyle().set("max-height", "-webkit-fill-available");

		apmisImageContainer.add(img);

		Div aboutText = new Div();

		Paragraph text = new Paragraph(I18nProperties.getDescription(Descriptions.about_description));
		text.getStyle().set("color", "black");
		text.getStyle().set("font-size", "20px");
		text.getStyle().set("margin-bottom", "30px");
		text.getStyle().set("text-align", "justify");
		aboutText.add(text);
		
		

		aboutView.add(apmisImageContainer, aboutText);
		add(aboutView, 
       
       );
		configureActionButtonVisibility();

		

	}

	public void configureActionButtonVisibility() {
		grid.addColumn(JsonDictionaryReportModelDto::getFormid).setHeader("Id");
		grid.addColumn(JsonDictionaryReportModelDto::getId).setHeader("Form Id ");
		grid.addColumn(JsonDictionaryReportModelDto::getCaption).setHeader("Caption");
		grid.addColumn(JsonDictionaryReportModelDto::getFormtype).setHeader("Form Type");
		grid.addColumn(JsonDictionaryReportModelDto::getModality).setHeader("Campaign Modality");
		grid.addColumn(JsonDictionaryReportModelDto::getDatatype).setHeader("Data Type");

		dataView = grid.setItems(analysis);
		add(grid);
		grid.setVisible(false);

		Button displayActionButtons = new Button("Show Action Buttons");
		displayActionButtons.setIcon(new Icon(VaadinIcon.SLIDERS));

		Button getUserGuide =  new Button(I18nProperties.getCaption(Captions.aboutuserguides));

		getUserGuide.setIcon(new Icon(VaadinIcon.NURSE));

		Button getTechnicalGuide = new Button(I18nProperties.getCaption(Captions.abouttechguides));
		getTechnicalGuide.setIcon(new Icon(VaadinIcon.DIPLOMA_SCROLL));

		Button getMobileGuide = new Button(I18nProperties.getCaption(Captions.mobileUserGuide));
		getMobileGuide.setIcon(new Icon(VaadinIcon.MOBILE));
		getMobileGuide.addClassName("wrap-button-label"); // Add a CSS class for styling

		GridExporter<JsonDictionaryReportModelDto> exporter = GridExporter.createFor(grid);

		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getString(Strings.apmisJsonGlossary));
		exporter.setFileName(I18nProperties.getString(Strings.apmisJsonGlossary)
				+ new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
		Anchor excelLink = new Anchor("", I18nProperties.getCaption(Captions.exportFormGlossary));
		excelLink.setHref(exporter.getCsvStreamResource());

		Button exportJsonGloassary = new Button(I18nProperties.getCaption(Captions.exportFormGlossary));
		exportJsonGloassary.setIcon(new Icon(VaadinIcon.FILE_TABLE));
		exportJsonGloassary.addClickListener(e -> {
			excelLink.getElement().setAttribute("download", true);
			excelLink.getElement().callJsFunction("click");

		});

		getUserGuide.addClickListener(e -> {
			UI.getCurrent().getPage().open(
					"https://staging.afghanistan-apmis.com/sormas-ui/VAADIN/themes/sormas/img/APMIS_User_Guide.pdf");
		});

		getTechnicalGuide.addClickListener(e -> {
			UI.getCurrent().getPage().open(
					"https://staging.afghanistan-apmis.com/sormas-ui/VAADIN/themes/sormas/img/APMIS_Technical_Manual.pdf");
		});

		getMobileGuide.addClickListener(e -> {
			UI.getCurrent().getPage().open(
					"https://staging.afghanistan-apmis.com/sormas-ui/VAADIN/themes/sormas/img/Apmis_MobileUser_Guide.pdf");
		});
		;

		excelLink.setClassName("exportJsonGLoss2");

		excelLink.getStyle().set("color", "0D6938 !important");
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.getStyle().set("padding-left", "90px");
		buttonsLayout.setWidth("100%");
//		buttonsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
		buttonsLayout.add(getUserGuide, getTechnicalGuide, getMobileGuide, excelLink);
		
		
		VerticalLayout releaseDetailsLayout = new VerticalLayout();
		Paragraph versionNum = new Paragraph("APMIS Version Number : APMIS 5.0.0");
		versionNum.getStyle().set("font-size", "15px");
		versionNum.getStyle().set("font-weight", "500");
		versionNum.getStyle().set("color", "#0D6938");
		
		Paragraph releaseDate = new Paragraph("Release Date : 15/08/2023");
		releaseDate.getStyle().set("font-size", "15px");
		releaseDate.getStyle().set("font-weight", "500");
		releaseDate.getStyle().set("color", "#0D6938");
		
		
		releaseDetailsLayout.getStyle().set("padding-left", "90px !important");
		releaseDetailsLayout.setWidth("100%");
		releaseDetailsLayout.add(versionNum, releaseDate);
		add(buttonsLayout, releaseDetailsLayout);

	}
}
