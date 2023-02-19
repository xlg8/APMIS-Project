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
package de.symeda.sormas.ui;

import de.symeda.sormas.ui.campaign.CampaignController;
import de.symeda.sormas.ui.configuration.infrastructure.InfrastructureController;
import de.symeda.sormas.ui.dashboard.DashboardController;
import de.symeda.sormas.ui.user.UserController;
import de.symeda.sormas.ui.utils.BaseControllerProvider;

public class ControllerProvider extends BaseControllerProvider {

	private final InfrastructureController infrastructureController;
	private final UserController userController;
	private final DashboardController dashboardController;
	private final CampaignController campaignController;

	public ControllerProvider() {
		super();
		infrastructureController = new InfrastructureController();
		userController = new UserController();
		dashboardController = new DashboardController();
		campaignController = new CampaignController();

	}

	protected static ControllerProvider get() {
		return (ControllerProvider) BaseControllerProvider.get();
	}

	public static InfrastructureController getInfrastructureController() {
		return get().infrastructureController;
	}

	public static UserController getUserController() {
		return get().userController;
	}

	public static DashboardController getDashboardController() {
		return get().dashboardController;
	}

	public static CampaignController getCampaignController() {
		return get().campaignController;
	}


}
