package com.cinoteck.application.views.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vaadin.addons.taefi.component.ToggleButtonGroup;

import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import java.util.stream.Collectors;

@PageTitle("APMIS-Analytics Dashboard")
@Route(value = "analytics", layout = MainLayout.class)

public class AnalyticsDashboardView extends VerticalLayout implements RouterLayout , BeforeEnterObserver {

	private static final long serialVersionUID = 1851726752523985165L;

	protected CampaignDashboardDataProvider dataProvider;

	public AnalyticsDashboardView() {
//		 Html html = new Html("<iframe src='https://staging.afghanistan-apmis.com/analytics' style='width:100%; height:100%;'></iframe>");
		 
		 
	        ToggleButtonGroup<String> group60 = new ToggleButtonGroup<>("Disabled group:",
	                List.of("All", "Items", "Are", "Disabled", "Selected"));
	        group60.setId("group60");
	        group60.setValue("Selected");
	        group60.setEnabled(false);

	        HashMap<String, String> map = new HashMap<>();
	        
	        // Adding elements to the Map
	        // using standard put() method
	        map.put("vishal", "aaaa");
	        map.put("sachin", "bbbb");
	        map.put("vaibhav", "cccc");
	        
	        
	        ToggleButtonGroup<String> group70 = new ToggleButtonGroup<>("Choose desert: [unavailable items are disabled]");
	        group70.setId("group70");
	      
	        group70.setItems(map.values().stream().collect(Collectors.toList()));
//	        group70.setItemLabelGenerator(item -> String.format("%s (%d)", item.name, item.availableCount));
	        group70.addValueChangeListener(event -> {
	            if (event.getOldValue() != null) {
	               Notification.show(event.getOldValue()+" -old");
	            }
	            if (event.getValue() != null) {
	            	Notification.show(event.getValue() +" -new");
	            }
	           // group70.setValue(event.getValue());
	        });
	        
	        
	        
		add(group70);
		setSizeFull();
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
	}


}
