package com.cinoteck.application.views.about;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.cinoteck.application.views.MainLayout;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.report.JsonDictionaryReportModelDto;

@PageTitle("APMIS | About")
@Route(value = "about", layout = MainLayout.class)
public class AboutView extends VerticalLayout {
	
	Grid<JsonDictionaryReportModelDto> grid = new Grid<>(JsonDictionaryReportModelDto.class, false);
	
	
	List<JsonDictionaryReportModelDto> analysis = FacadeProvider.getCampaignFormDataFacade().getByJsonFormDefinitonToCSV();
	GridListDataView<JsonDictionaryReportModelDto> dataView ;
	
 

    public AboutView() {
    	 Div aboutView = new Div();
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
		
		Paragraph text = new Paragraph(
				"The Afghanistan Polio Management Information System (APMIS) is an online data system that simplifies and improves the use and management of polio immunization-related data. APMIS facilitates field data entry, immunization data storage, data visualization, and real-time monitoring of polio immunization activities in Afghanistan.  Using this system will assist in evaluating immunization campaign activities and identifying program challenges.");
		text.getStyle().set("color", "green");
		text.getStyle().set("font-size", "20px");
		text.getStyle().set("margin-bottom", "30px");
		text.getStyle().set("text-align", "justify");
		aboutText.add(text);

		aboutView.add(apmisImageContainer, aboutText);
		add(aboutView);
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
		
		Button displayActionButtons =  new Button("Show Action Buttons");
		displayActionButtons.setIcon(new Icon(VaadinIcon.SLIDERS));
		
		
		Button getUserGuide =  new Button("User Guide");
		getUserGuide.setIcon(new Icon(VaadinIcon.NURSE));
		getUserGuide.setVisible(false);
		
		Button getTechnicalGuide =  new Button("Technical Guide");
		getTechnicalGuide.setIcon(new Icon(VaadinIcon.DIPLOMA_SCROLL));
		getTechnicalGuide.setVisible(false);
		
		Button getMobileGuide =  new Button("Mobile User Guide");
		getMobileGuide.setIcon(new Icon(VaadinIcon.MOBILE));
		getMobileGuide.setVisible(false);
		getMobileGuide.addClassName("wrap-button-label"); // Add a CSS class for styling

		
		
		GridExporter<JsonDictionaryReportModelDto> exporter = GridExporter.createFor(grid);

	    exporter.setAutoAttachExportButtons(false);
	    exporter.setTitle("APMIS Json Glossary");
	    exporter.setFileName("APMIS Json Glossary" + new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime()));
	    Anchor excelLink= new Anchor("", "Export Forms & Diagrams Glossary");
	    excelLink.setHref(exporter.getCsvStreamResource());
	    excelLink.getElement().setAttribute("download", true);
		
		excelLink.setVisible(false);
		
		displayActionButtons.addClickListener(e->{
			if(getUserGuide.isVisible() == false) {
				getUserGuide.setVisible(true);
				getTechnicalGuide.setVisible(true);
				excelLink.setVisible(true);
				getMobileGuide.setVisible(true);
				displayActionButtons.setText("Hide Action Buttons");
			}else {
			getUserGuide.setVisible(false);
			getTechnicalGuide.setVisible(false);
			excelLink.setVisible(false);
			getMobileGuide.setVisible(false);
			displayActionButtons.setText("Show Action Buttons");
			} 
		});
		
		getUserGuide.addClickListener(e->{
			  UI.getCurrent().getPage().open("https://staging.afghanistan-apmis.com/sormas-ui/VAADIN/themes/sormas/img/APMIS_User_Guide.pdf");
		});
		
		getTechnicalGuide.addClickListener(e->{
			  UI.getCurrent().getPage().open("https://staging.afghanistan-apmis.com/sormas-ui/VAADIN/themes/sormas/img/APMIS_Technical_Manual.pdf");
		});
		
		getMobileGuide.addClickListener(e->{
			  UI.getCurrent().getPage().open("https://staging.afghanistan-apmis.com/sormas-ui/VAADIN/themes/sormas/img/Apmis_MobileUser_Guide.pdf");
		});
		
		excelLink.setClassName("exportJsonGLoss2");
		
		excelLink.getStyle().set("color", "0D6938 !important");
		HorizontalLayout buttonsLayout  = new HorizontalLayout();
		buttonsLayout.getStyle().set("padding-left", "90px");
		buttonsLayout.add(displayActionButtons, getUserGuide, getTechnicalGuide, getMobileGuide,  excelLink);
		add(buttonsLayout);
		
	
	}
	

}
