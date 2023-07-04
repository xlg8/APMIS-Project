package com.cinoteck.application.views.pivot;

import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


@PageTitle("Pivot")
@Route(value = "pivot", layout = MainLayout.class)
public class PivotView extends VerticalLayout {
	
	public  PivotView() {
		setSizeFull();
//		Button hideShow = new Button();
//		hideShow.addClickListener(e->{
//			UI.getCurrent().getElement().executeJs("function hidePivotCells() {\r\n"
//					+ "  var elements = document.getElementsByClassName(\"pvtUiCell\");\r\n"
//					+ "  \r\n"
//					+ "  for (var i = 0; i < elements.length; i++) {\r\n"
//					+ "    if (elements[i].style.display === \"none\") {\r\n"
//					+ "      elements[i].style.removeProperty(\"display\");\r\n"
//					+"console.log(\"display:hidden\");\r\n"
//					+ "    } else {\r\n"
//					+ "      elements[i].style.display = \"none\";\r\n"
//					+"console.log(\"display:none\");\r\n"
//					+ "    }\r\n"
//					+ "  }\r\n"
//					+ "}");
//		});
		 Html html = new Html("<iframe src='pivottablejs.html' style='width:100%; height:100%; border: 0px;'></iframe>");
	        add(html);
	        
	      
	}

}
