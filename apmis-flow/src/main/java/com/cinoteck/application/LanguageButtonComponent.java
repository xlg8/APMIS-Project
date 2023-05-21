//package com.cinoteck.application;
//
//import java.awt.MenuComponent;
//
//import com.vaadin.flow.component.ClickEvent;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.dialog.Dialog;
//import com.vaadin.flow.component.html.Div;
//import com.vaadin.flow.component.html.Image;
//import com.vaadin.flow.component.html.Paragraph;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//
//public class LanguageButtonComponent extends MenuComponent {
//	
//	public LanguageButtonComponent(){
//		 Button confirmButton;
//		 Button cancelButton;
//
//		Dialog dialog = new Dialog();
//		dialog.setCloseOnEsc(false);
//		dialog.setCloseOnOutsideClick(false);
//		
//		VerticalLayout dialogHolderLayout = new VerticalLayout();
//		
//		   
//		Div apmisImageContainer = new Div();
//		apmisImageContainer.getStyle().set("width", "100%");
//		apmisImageContainer.getStyle().set("display", "flex");
//		apmisImageContainer.getStyle().set("justify-content", "center");
//
//		Image img = new Image("images/logout.png", "APMIS-LOGO");
//		img.getStyle().set("max-height", "-webkit-fill-available");
//
//		apmisImageContainer.add(img);
//
//		Div aboutText = new Div();
//		
//		Paragraph text = new Paragraph("You are attempting to log out of APMIS");
//		Paragraph confirmationText = new Paragraph("Are you sure you want to logout?");
//		
//		
//		text.getStyle().set("color", "black");
//		text.getStyle().set("font-size", "24px");
//		confirmationText.getStyle().set("color", "green");
//		confirmationText.getStyle().set("font-size", "18px");
//		
//		
//		aboutText.getStyle().set("display", "flex");
//		aboutText.getStyle().set("flex-direction", "column");
//		aboutText.getStyle().set("align-items", "center");
//		aboutText.add(text, confirmationText);
//
//		Div logoutButtons = new Div();
//		logoutButtons.getStyle().set("display", "flex");
//		logoutButtons.getStyle().set("justify-content", "space-evenly");
//		logoutButtons.getStyle().set("width", "100%");
//	   
//		confirmButton = new Button("Confirm", event -> {
////			confirmButton.getUI().ifPresent(ui -> ui.navigate(""));
//		});
//		confirmButton.getStyle().set("width", "35%");
//		cancelButton = new Button("Cancel", event -> {
//			dialog.close();
////			cancelButton.getUI().ifPresent(ui -> ui.navigate("dashboard"));
//		});
//		cancelButton.getStyle().set("width", "35%");
//		cancelButton.getStyle().set("background", "white");
//		cancelButton.getStyle().set("color", "green");
//		logoutButtons.add(confirmButton, cancelButton);
//
//		dialogHolderLayout.add(apmisImageContainer, aboutText, logoutButtons);
//		dialog.add(dialogHolderLayout);
////		return cancelButton;
//		
//
////		add(dialog);
////		return dialog;
//
//	    }
//
//	
//}