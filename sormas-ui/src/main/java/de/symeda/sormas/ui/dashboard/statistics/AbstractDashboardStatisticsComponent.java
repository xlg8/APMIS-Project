/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.dashboard.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.ui.dashboard.DashboardDataProvider;
import de.symeda.sormas.ui.dashboard.DashboardType;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.LayoutUtil;

/**
 * Remove when DashboardCnnstactStatisticsComponent is updated
 */
@Deprecated
@SuppressWarnings("serial")
public abstract class AbstractDashboardStatisticsComponent extends VerticalLayout {

	protected static final String FIRST_LOC = "firstLoc";
	protected static final String SECOND_LOC = "secondLoc";
	protected static final String THIRD_LOC = "thirdLoc";
	protected static final String FOURTH_LOC = "fourthLoc";

	protected final DashboardDataProvider dashboardDataProvider;

	protected CustomLayout subComponentsLayout;
	protected DashboardStatisticsSubComponent firstComponent;
	protected DashboardStatisticsSubComponent secondComponent;
	protected DashboardStatisticsSubComponent thirdComponent;
	protected DashboardStatisticsSubComponent fourthComponent;
	private Button showMoreButton;
	private Button showLessButton;



	public AbstractDashboardStatisticsComponent(DashboardDataProvider dashboardDataProvider) {
		this.dashboardDataProvider = dashboardDataProvider;
		this.setWidth(100, Unit.PERCENTAGE);
		this.setMargin(new MarginInfo(true, true, false, true));
		this.setSpacing(false);

		subComponentsLayout = new CustomLayout();
		subComponentsLayout.setTemplateContents(
			LayoutUtil.fluidRow(
				LayoutUtil.fluidColumnLoc(3, 0, 6, 0, FIRST_LOC),
				LayoutUtil.fluidColumnLoc(3, 0, 6, 0, SECOND_LOC),
				LayoutUtil.fluidColumnLoc(3, 0, 6, 0, THIRD_LOC),
				LayoutUtil.fluidColumnLoc(3, 0, 6, 0, FOURTH_LOC)));
		subComponentsLayout.setWidth(100, Unit.PERCENTAGE);

		addFirstComponent();
		addSecondComponent();
		addThirdComponent();
		addFourthComponent();

		addComponent(subComponentsLayout);

		
	}

	protected abstract void addFirstComponent();

	protected abstract void addSecondComponent();

	protected abstract void addThirdComponent();

	protected abstract void addFourthComponent();

	protected abstract void updateFirstComponent(int visibleDiseasesCount);

	protected abstract void updateSecondComponent(int visibleDiseasesCount);

	protected abstract void updateThirdComponent(int visibleDiseasesCount);

	protected abstract void updateFourthComponent(int visibleDiseasesCount);

	protected abstract int getNormalHeight();

	protected abstract int getFullHeight();

	protected abstract int getFilteredHeight();

	
	

	private boolean isFullMode() {
		return showLessButton != null && showLessButton.isVisible();
	}

	

	public int calculateGrowth(int currentCount, int previousCount) {
		return currentCount == 0
			? (previousCount > 0 ? -100 : 0)
			: previousCount == 0
				? (currentCount > 0 ? Integer.MIN_VALUE : 0)
				: Math.round(((currentCount - previousCount * 1.0f) / previousCount) * 100.0f);
	}
}
