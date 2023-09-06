//package com.cinoteck.application.views.utils.controllers;
//
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.html.Anchor;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.router.Route;
//import com.vaadin.flow.server.StreamResource;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//@Route("download-errors")
//public class ErrorView extends VerticalLayout {
//
//    private final CSVImporter csvImporter;
//
//    @Autowired
//    public ErrorView(CSVImporter csvImporter) {
//        this.csvImporter = csvImporter;
//
//        Button downloadButton = new Button("Download Errors");
//        downloadButton.addClickListener(event -> {
//            StreamResource errorResource = csvImporter.importCSV("your_csv_file.csv");
//            Anchor anchor = new Anchor(errorResource, "errors.txt");
//            anchor.getElement().setAttribute("download", true);
//
//            UI.getCurrent().getPage().executeJavaScript("document.body.appendChild(arguments[0]);", anchor.getElement());
//            anchor.getElement().callFunction("click");
//            UI.getCurrent().getPage().executeJavaScript("document.body.removeChild(arguments[0]);", anchor.getElement());
//        });
//
//        add(downloadButton);
//    }
//}