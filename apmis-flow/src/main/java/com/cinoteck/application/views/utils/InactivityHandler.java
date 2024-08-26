////package com.cinoteck.application.views.utils;
////
////import com.vaadin.flow.component.dialog.Dialog;
////import com.vaadin.flow.component.button.Button;
////import com.vaadin.flow.component.html.Span;
////import com.vaadin.flow.component.notification.Notification;
////import com.vaadin.flow.component.ClientCallable;
////import com.vaadin.flow.component.Text;
////import com.vaadin.flow.component.UI;
////import com.vaadin.flow.component.page.Page;
////import com.vaadin.flow.server.VaadinService;
////import com.vaadin.flow.server.VaadinSession;
////import com.vaadin.flow.server.auth.AnonymousAllowed;
////import com.vaadin.flow.component.orderedlayout.VerticalLayout;
////
////public class InactivityHandler extends VerticalLayout {
////
////    public InactivityHandler() {
////        // Start the inactivity timer when the component is attached
//////        UI.getCurrent().getPage().executeJs(
//////            "startInactivityTimer($0, 120000)", 
//////            this.getElement().callJsFunction("showInactivityDialog")
//////        );
////    	
////    	UI.getCurrent().getPage().executeJs(
////    		    "startInactivityTimer(showInactivityDialog, $0)", 
////    		    60000 // 2 minutes in milliseconds
////    		);
////    	
////    	UI.getCurrent().getPage().executeJs("function startInactivityTimer(dialogFunction, timeout) {\r\n"
////    			+ "    setTimeout(dialogFunction, timeout);\r\n"
////    			+ "}");
////    			
////    		    
////    	
////    }
////
////    @ClientCallable
////    public void showInactivityDialog() {
////        Dialog inactivityDialog = new Dialog();
////        inactivityDialog.add(new Text("You have been inactive for a while."));
////        Button extendSessionButton = new Button("Continue", e -> inactivityDialog.close());
////        Button logoutButton = new Button("Logout", e -> {
////            inactivityDialog.close();
////            // Implement logout logic here
////        });
////        inactivityDialog.add(extendSessionButton, logoutButton);
////        inactivityDialog.open();
////    }
////}
//
//package com.cinoteck.application.views.utils;
//
//import com.vaadin.flow.component.dialog.Dialog;
//import com.vaadin.flow.component.html.Span;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.Text;
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.server.VaadinSession;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.component.ClientCallable;
//
////public class InactivityHandler extends VerticalLayout {
////	Span countdownSpan = new Span();
////    public InactivityHandler() {
////    	
////        
////        add(countdownSpan); // Add the countdown display to the UI
////
////        // Define the necessary JavaScript functions and start the timer
////        UI.getCurrent().getPage().executeJs(
////            "function startInactivityTimer(timeout) {" +
////            "    var remainingTime = timeout / 1000;" + // Time in seconds" +
////            "    var countdownInterval = setInterval(function() {" +
////            "        $0.$server.updateCountdown(remainingTime);" + // Update the countdown on the server side
////            "        remainingTime--;" +
////            " console.log(remainingTime); " + 
////            "        if (remainingTime <= 0) {" +
////            "            clearInterval(countdownInterval);" + // Stop the interval
////            "            $0.$server.showInactivityDialog();" + // Show the inactivity dialog
////            "        }" +
////            "    }, 1000);" + // Update every second
////            "}" +
////            "startInactivityTimer($1);", 
////            getElement(), // Binds the client callable to the Vaadin component
////            60000 // 2 minutes in milliseconds
////        );
////
////    }
////    
////    @ClientCallable
////    public void updateCountdown(int remainingTime) {
////        countdownSpan.setText("Time remaining: " + remainingTime + " seconds");
////    }
////
////
////    @ClientCallable
////    public void showInactivityDialog() {
////        Dialog inactivityDialog = new Dialog();
////        inactivityDialog.add(new Text("You have been inactive for a while."));
////        
////        Button extendSessionButton = new Button("Continue", e -> {
////        	
////        	inactivityDialog.close();
////        	
////            UI.getCurrent().getPage().executeJs(
////                    "function startInactivityTimer(timeout) {" +
////                    "    var remainingTime = timeout / 1000;" + // Time in seconds" +
////                    "    var countdownInterval = setInterval(function() {" +
////                    "        $0.$server.updateCountdown(remainingTime);" + // Update the countdown on the server side
////                    "        remainingTime--;" +
////                    " console.log(remainingTime); " + 
////                    "        if (remainingTime <= 0) {" +
////                    "            clearInterval(countdownInterval);" + // Stop the interval
////                    "            $0.$server.showInactivityDialog();" + // Show the inactivity dialog
////                    "        }" +
////                    "    }, 1000);" + // Update every second
////                    "}" +
////                    "startInactivityTimer($1);", 
////                    getElement(), // Binds the client callable to the Vaadin component
////                    60000 // 1 minutes in milliseconds
////                );
////        	
////        	
////
////        });
////        Button logoutButton = new Button("Logout", e -> {
////            inactivityDialog.close();
////            // Implement logout logic here
////            VaadinSession.getCurrent().getSession().invalidate();
////            UI.getCurrent().getPage().setLocation("/apmis-flow");
////        });
////        
////        inactivityDialog.add(extendSessionButton, logoutButton);
////        inactivityDialog.open();
////    }
////}
////
////package com.cinoteck.application.views.utils;
//
////
////public class InactivityHandler extends VerticalLayout {
////
////    private Span countdownSpan;
////
////    public InactivityHandler() {
////        countdownSpan = new Span();
////        add(countdownSpan); // Add the countdown display to the UI
////
////        // Define the necessary JavaScript functions and start the timer
////        UI.getCurrent().getPage().executeJs(
////            "function startInactivityTimer(timeout) {" +
////            "    var remainingTime = timeout / 1000;" + // Time in seconds
////            "    var countdownInterval;" + 
////            "    function resetTimer() {" +
////            "        clearInterval(countdownInterval);" + // Stop the existing interval
////            "        remainingTime = timeout / 1000;" + // Reset the time
////            "        countdownInterval = setInterval(function() {" +
////            "            $0.$server.updateCountdown(remainingTime);" + // Update the countdown on the server side
////            "            remainingTime--;" +
////            "            console.log(remainingTime);" +
////            "            if (remainingTime <= 0) {" +
////            "                clearInterval(countdownInterval);" + // Stop the interval
////            "                $0.$server.showInactivityDialog();" + // Show the inactivity dialog
////            "            }" +
////            "        }, 1000);" + // Update every second
////            "    }" +
////            "    document.addEventListener('mousemove', resetTimer);" + // Reset on mouse move
////            "    document.addEventListener('keydown', resetTimer);" + // Reset on key press
////            "    resetTimer(); " + // Start the timer immediately
////            "}" +
////            "startInactivityTimer($1);", 
////            getElement(), // Binds the client callable to the Vaadin component
////            60000 // 1 minute in milliseconds
////        );
////    }
////
////    @ClientCallable
////    public void updateCountdown(int remainingTime) {
////        countdownSpan.setText("Time remaining: " + remainingTime + " seconds");
////        System.out.println("Countdown timer " + remainingTime + "--------------------------------------" );
////        
////    }
////
////    @ClientCallable
////    public void showInactivityDialog() {
////        Dialog inactivityDialog = new Dialog();
////        inactivityDialog.add(new Text("You have been inactive for a while."));
////        
////        Button extendSessionButton = new Button("Continue", e -> {
////            inactivityDialog.close();
////            // Restart the timer when the session is continued
////            UI.getCurrent().getPage().executeJs(
////                "startInactivityTimer($0);",
////                getElement(), 
////                60000 // 1 minute in milliseconds
////            );
////        });
////        
////        Button logoutButton = new Button("Logout", e -> {
////            inactivityDialog.close();
////            // Implement logout logic here
////            VaadinSession.getCurrent().getSession().invalidate();
////            UI.getCurrent().getPage().setLocation("/apmis-flow");
////        });
////
////        inactivityDialog.add(extendSessionButton, logoutButton);
////        inactivityDialog.open();
////    }
////}
//
//public class InactivityHandler extends VerticalLayout {
//
//	private Span countdownSpan;
//
//	public InactivityHandler() {
//		countdownSpan = new Span();
//		add(countdownSpan); // Add the countdown display to the UI
//
//		startTimer();
//	}
//
//	private void startTimer() {
//		// Define the necessary JavaScript functions and start the timer
////        UI.getCurrent().getPage().executeJs(
////            "function startInactivityTimer(timeout) {" +
////            "    var remainingTime = timeout / 1000;" + // Time in seconds
////            "    var countdownInterval;" + 
////            "    function resetTimer() {" +
////            "        clearInterval(countdownInterval);" + // Stop the existing interval
////            "        remainingTime = timeout / 1000;" + // Reset the time
////            "        countdownInterval = setInterval(function() {" +
////            "            $0.$server.updateCountdown(remainingTime);" + // Update the countdown on the server side
////            "            remainingTime--;" +
////            "            console.log(remainingTime);" +
////            "            if (remainingTime <= 0) {" +
////            "                clearInterval(countdownInterval);" + // Stop the interval
////            "                $0.$server.showInactivityDialog();" + // Show the inactivity dialog
////            "            }" +
////            "        }, 1000);" + // Update every second
////            "    }" +
////            "    document.addEventListener('mousemove', resetTimer);" + // Reset on mouse move
////            "    document.addEventListener('keydown', resetTimer);" + // Reset on key press
////            "    resetTimer(); " + // Start the timer immediately
////            "}" +
////            "startInactivityTimer($1);", 
////            getElement(), 
////            60000 // 1 minute in milliseconds
////        );
//
//		UI.getCurrent().getPage().executeJs(
//				"window.inactivityHandler = {" + 
//				"    countdownInterval: null," + 
//				"    startInactivityTimer: function(timeout) {" + 
//				"        var remainingTime = timeout / 1000;" + // Time
//																													// in
//																													// seconds
//				"        this.stopTimer();" + // Stop any existing timer before starting a new one
//				"        this.countdownInterval = setInterval(function() {" + 
//				"            $0.$server.updateCountdown(remainingTime);" + // Update the countdown on the server side
//				"            remainingTime--;" +
//				"            console.log(remainingTime);" +
//				"            if (remainingTime == 10) {" + 
//				"                clearInterval(this.countdownInterval);" + // Stop the interval
//				"                $0.$server.showInactivityDialog();" + // Show the inactivity dialog
//				"            }"+
////				+ "else if(remainingTime < 2){clearInterval(this.countdownInterval); this.countdownInterval = null; }" +
//				"        }, 1000);" + // Update every second
//				"    }," + 
//				"    stopTimer: function() {" + 
//				"        if (this.countdownInterval) {" + 
//				"            clearInterval(this.countdownInterval);" + // Stop the interval
//				"            this.countdownInterval = null;" + // Clear the interval reference
//				"        }" + 
//				"    }," + 
//				"    resetTimer: function(timeout) {" + 
//				"        this.startInactivityTimer(timeout);" + // Restart the timer
//				"    }" + 
//				"};" + 
//				
//				
//				"window.inactivityHandler.startInactivityTimer($1);", getElement(), 60000 // 1 minute
//																											// in
//																											// milliseconds
//		);
//
//		// Add event listeners for mouse move and key press to reset the timer
//		UI.getCurrent().getPage().executeJs(
//				"document.addEventListener('mousemove', function() { window.inactivityHandler.resetTimer($0); });",
//				60000);
//		UI.getCurrent().getPage().executeJs(
//				"document.addEventListener('keydown', function() { window.inactivityHandler.resetTimer($0); });",
//				60000);
//	}
//
//	  // Method to reset the timer from other views
//    public void resetTimer() {
//        UI.getCurrent().getPage().executeJs("window.inactivityHandler.resetTimer($0);", 60000);
//    }
//
//	// Method to stop the timer completely
//	public void stopTimer() {
//		UI.getCurrent().getPage().executeJs("window.inactivityHandler.stopTimer();");
//	}
//
//	@ClientCallable
//	public void updateCountdown(int remainingTime) {
//		countdownSpan.setText("Time remaining: " + remainingTime + " seconds");
//		 System.out.println("Countdown timer " + remainingTime + "--------------------------------------" );
//	}
//
//	@ClientCallable
//	public void showInactivityDialog() {
//		Dialog inactivityDialog = new Dialog();
//		inactivityDialog.add(new Text("You have been inactive for a while."));
//
//		Button extendSessionButton = new Button("Continue", e -> {
//			stopTimer();
//			
//			inactivityDialog.close();
//			
//			startTimer();
//		});
//
//		Button logoutButton = new Button("Logout", e -> {
//			inactivityDialog.close();
//			VaadinSession.getCurrent().getSession().invalidate();
//			UI.getCurrent().getPage().setLocation("/apmis-flow");
//		});
//
//		inactivityDialog.add(extendSessionButton, logoutButton);
//		inactivityDialog.open();
//		inactivityDialog.setModal(true); // Ensure the dialog opens above any other dialogs
//		inactivityDialog.setDraggable(true);
//	}
//	
////	public InactivityHandler getInactivityHandler() {
////        return inactivityHandler;
////    }
//}
