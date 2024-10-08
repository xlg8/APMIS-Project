package com.cinoteck.application.views.user;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;


@PageTitle("APMIS-User Management")
@Route(value = "disabledusersmanegement", layout = UsersViewParent.class)
public class disabledUsersView extends VerticalLayout implements RouterLayout, BeforeEnterObserver {

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// TODO Auto-generated method stub
		
	}


}
