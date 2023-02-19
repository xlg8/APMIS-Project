package de.symeda.sormas.ui.dashboard.map;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;

@JavaScript({ "myscript.js", "https://maps.googleapis.com/maps/api/js?v=3.exp" })
@StyleSheet({ "mystyle.css" })
@SuppressWarnings("serial")
public class CampaignDashboardMapComponent extends AbstractJavaScriptComponent{

	float param1 = 123;
	float param2 = 456;

	public CampaignDashboardMapComponent() {
	       callFunction("myfunction",param1,param2); //Pass parameters to your function
	    }

}
