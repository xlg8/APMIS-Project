package com.cinoteck.application.views;

import com.cinoteck.application.views.configurations.ConfigView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.Theme;

@Route("trythis")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
@Theme("apmis-theme")
public class MainLayout extends AppLayout implements AppShellConfigurator, HasDynamicTitle, HasUrlParameter<Long> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	  private String pagetitle = "";
	 private H1 title = new H1("Default Title");

	
	 
	public MainLayout() {
		
		this.getStyle().set("--vaadin-app-layout-drawer-width", "10px");

		
		DrawerToggle toggle = new DrawerToggle();
		title = new H1(pagetitle);
		Tabs tabs = getTabs();
		
		addToDrawer(tabs);
		addToNavbar(toggle, title);

		setPrimarySection(Section.DRAWER); 
	}

	private Tabs getTabs() {
	    Tabs tabs = new Tabs();
	    tabs.add(createTab(VaadinIcon.DASHBOARD, "Dashboard", ConfigView.class));
	    tabs.setOrientation(Tabs.Orientation.VERTICAL);
	    tabs.addClassName("tabs");
	    return tabs;
	}

	
	//TODO: Move the styles into CSS classes for a cleaner code 
	private Tab createTab(VaadinIcon viewIcon, String viewName, Class<? extends Component> viewClass) {
	    Icon icon = viewIcon.create();
	    icon.getStyle().set("box-sizing", "border-box")
	        .set("margin-inline-end", "var(--lumo-space-m)")
	        .set("padding", "var(--lumo-space-xs)");

	    RouterLink link = new RouterLink();
	    link.setRoute(viewClass);
	    

	    // Create a VerticalLayout to stack the icon and the Span vertically
	    VerticalLayout verticalLayout = new VerticalLayout(icon, new Span(viewName));
	    verticalLayout.setSpacing(false);
	    verticalLayout.setPadding(false);

	    // Center the elements vertically and horizontally within the VerticalLayout
	    verticalLayout.getStyle().set("display", "flex")
	        .set("flex-direction", "column")
	        .set("align-items", "center")
	        .set("justify-content", "center");

	    link.add(verticalLayout);

	    return new Tab(link);
	}

	  public void setTitle(String newTitle) {
	        title.setText(newTitle);
	    }

	@Override
	public String getPageTitle() {
		return title.toString();
	}

	@Override
    public void setParameter(BeforeEvent event,
            @OptionalParameter Long parameter) {
        if (parameter != null) {
        	pagetitle = "Blog Post #" + parameter;
        } else {
        	pagetitle = "Blog Home";
        }
    }
	




}