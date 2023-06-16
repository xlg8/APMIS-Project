/*******************************************************************************
// * SORMAS® - Surveillance Outbreak Response Management & Analysis System
// * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <https://www.gnu.org/licenses/>.
// *******************************************************************************/
//package com.cinoteck.application;
//
//import com.cinoteck.application.views.utils.BaseControllerProvider;
//
//public class ControllerProvider extends BaseControllerProvider {
//
//
//	private final InfrastructureController infrastructureController;
//
//	private final UserController userController;
//
//	private final DashboardController dashboardController;
//
//	private final AggregateReportController aggregateReportController;
//	private final CampaignController campaignController;
//	private final SormasToSormasController sormasToSormasController;
//	private final CustomExportController customExportController;
//
//	public ControllerProvider() {
//		super();
//
//		infrastructureController = new InfrastructureController();
//		dashboardController = new DashboardController();
//		aggregateReportController = new AggregateReportController();
//		campaignController = new CampaignController();
//		sormasToSormasController = new SormasToSormasController();
//		customExportController = new CustomExportController();
//	}
//
//	protected static ControllerProvider get() {
//		return (ControllerProvider) BaseControllerProvider.get();
//	}
//	public static InfrastructureController getInfrastructureController() {
//		return get().infrastructureController;
//	}
//
//	public static UserController getUserController() {
//		return get().userController;
//	}
//
//	public static DashboardController getDashboardController() {
//		return get().dashboardController;
//	}
//
//	public static AggregateReportController getAggregateReportController() {
//		return get().aggregateReportController;
//	}
//
//	public static CampaignController getCampaignController() {
//		return get().campaignController;
//	}
//
//	public static SormasToSormasController getSormasToSormasController() {
//		return get().sormasToSormasController;
//	}
//
//	public static CustomExportController getCustomExportController() {
//		return get().customExportController;
//	}
//
//}
