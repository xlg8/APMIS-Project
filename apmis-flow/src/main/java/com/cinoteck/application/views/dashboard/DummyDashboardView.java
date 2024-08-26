package com.cinoteck.application.views.dashboard;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.uiformbuilder.FormGridComponent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

@PageTitle("APMIS-Analytics Dashboard")
@Route(value = "analytics", layout = MainLayout.class)

public class DummyDashboardView extends VerticalLayout implements RouterLayout, BeforeEnterObserver {

	private static final long serialVersionUID = 1851726752523985165L;

	protected static final Logger logger = LogManager.getLogger(DummyDashboardView.class);
	protected CampaignDashboardDataProvider dataProvider;
	private boolean callbackRunning = false;
	private Timer timer;

	public DummyDashboardView() {		
		Html html = new Html(
				"<iframe src='https://nalytic.afghanistan-apmis.com/' style='width:100%; height:100%;'></iframe>");
	
		startIntervalCallback();		
		UI.getCurrent().addPollListener(event -> {
			if (callbackRunning) {
				UI.getCurrent().access(this::pokeFlow);
			} else {
				stopPullers();
			}
		});
		

		UI.getCurrent().getPage().executeJs("$('#togglecollapse').click();");
		add(html);
		setSizeFull();
	}

	private void pokeFlow() {
		logger.debug("runingImport...");
	}
	
	
	private void startIntervalCallback() {
		UI.getCurrent().setPollInterval(5000);
		if (!callbackRunning) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
//					stopIntervalCallback();
				}
			}, 15000); // 10 minutes

			callbackRunning = true;
		}
	}

	private void stopIntervalCallback() {
		if (callbackRunning) {
			callbackRunning = false;
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}

		}
		
	}
	
	private void stopPullers() {
		UI.getCurrent().setPollInterval(-1);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {}

}
