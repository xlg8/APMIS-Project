/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package de.symeda.sormas.api;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import de.symeda.sormas.api.campaign.CampaignFacade;
import de.symeda.sormas.api.campaign.data.CampaignFormDataFacade;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDefinitionFacade;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaFacade;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsFacade;
import de.symeda.sormas.api.dashboard.DashboardFacade;
import de.symeda.sormas.api.docgeneneration.DocumentTemplateFacade;
import de.symeda.sormas.api.document.DocumentFacade;
import de.symeda.sormas.api.feature.FeatureConfigurationFacade;
import de.symeda.sormas.api.geo.GeoShapeProvider;
import de.symeda.sormas.api.geocoding.GeocodingFacade;
import de.symeda.sormas.api.i18n.I18nFacade;
import de.symeda.sormas.api.importexport.ExportFacade;
import de.symeda.sormas.api.importexport.ImportFacade;
import de.symeda.sormas.api.info.InfoFacade;
import de.symeda.sormas.api.infrastructure.InfrastructureSyncFacade;
import de.symeda.sormas.api.infrastructure.PopulationDataFacade;
import de.symeda.sormas.api.infrastructure.area.AreaFacade;
import de.symeda.sormas.api.infrastructure.community.CommunityFacade;
import de.symeda.sormas.api.infrastructure.continent.ContinentFacade;
import de.symeda.sormas.api.infrastructure.country.CountryFacade;
import de.symeda.sormas.api.infrastructure.district.DistrictFacade;
import de.symeda.sormas.api.infrastructure.facility.FacilityFacade;
import de.symeda.sormas.api.infrastructure.pointofentry.PointOfEntryFacade;
import de.symeda.sormas.api.infrastructure.region.RegionFacade;
import de.symeda.sormas.api.infrastructure.subcontinent.SubcontinentFacade;
import de.symeda.sormas.api.systemevents.SystemEventFacade;
import de.symeda.sormas.api.user.FormAccessConfigFacade;
import de.symeda.sormas.api.user.UserFacade;
import de.symeda.sormas.api.user.UserRightsFacade;
import de.symeda.sormas.api.user.UserRoleConfigFacade;

public class FacadeProvider {

	private static final String JNDI_PREFIX = "java:global/sormas-ear/sormas-backend/";

	private final InitialContext ic;

	private static FacadeProvider instance;

	protected FacadeProvider() {

		try {
			ic = new InitialContext();
		} catch (NamingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static FacadeProvider get() {
		if (instance == null) {
			instance = new FacadeProvider();
		}
		return instance;
	}

	

	

	public static FacilityFacade getFacilityFacade() {
		return get().lookupEjbRemote(FacilityFacade.class);
	}

	public static ContinentFacade getContinentFacade() {
		return get().lookupEjbRemote(ContinentFacade.class);
	}

	public static SubcontinentFacade getSubcontinentFacade() {
		return get().lookupEjbRemote(SubcontinentFacade.class);
	}

	public static CountryFacade getCountryFacade() {
		return get().lookupEjbRemote(CountryFacade.class);
	}

	public static RegionFacade getRegionFacade() {
		return get().lookupEjbRemote(RegionFacade.class);
	}
	
	public static AreaFacade getAreatFacade() {
		return get().lookupEjbRemote(AreaFacade.class);
	}

	public static DistrictFacade getDistrictFacade() {
		return get().lookupEjbRemote(DistrictFacade.class);
	}

	public static CommunityFacade getCommunityFacade() {
		return get().lookupEjbRemote(CommunityFacade.class);
	}

	public static UserFacade getUserFacade() {
		return get().lookupEjbRemote(UserFacade.class);
	}

	public static UserRoleConfigFacade getUserRoleConfigFacade() {
		return get().lookupEjbRemote(UserRoleConfigFacade.class);
	}
	
	public static FormAccessConfigFacade getFormAccessConfigFacade() {
		return get().lookupEjbRemote(FormAccessConfigFacade.class);
	}

	public static GeoShapeProvider getGeoShapeProvider() {
		return get().lookupEjbRemote(GeoShapeProvider.class);
	}

	public static ConfigFacade getConfigFacade() {
		return get().lookupEjbRemote(ConfigFacade.class);
	}

	public static ExportFacade getExportFacade() {
		return get().lookupEjbRemote(ExportFacade.class);
	}

	public static ImportFacade getImportFacade() {
		return get().lookupEjbRemote(ImportFacade.class);
	}

	public static DashboardFacade getDashboardFacade() {
		return get().lookupEjbRemote(DashboardFacade.class);
	}

	public static PointOfEntryFacade getPointOfEntryFacade() {
		return get().lookupEjbRemote(PointOfEntryFacade.class);
	}

	public static PopulationDataFacade getPopulationDataFacade() {
		return get().lookupEjbRemote(PopulationDataFacade.class);
	}

	public static InfrastructureSyncFacade getInfrastructureSyncFacade() {
		return get().lookupEjbRemote(InfrastructureSyncFacade.class);
	}

	public static FeatureConfigurationFacade getFeatureConfigurationFacade() {
		return get().lookupEjbRemote(FeatureConfigurationFacade.class);
	}

	public static GeocodingFacade getGeocodingFacade() {
		return get().lookupEjbRemote(GeocodingFacade.class);
	}

	public static CampaignFacade getCampaignFacade() {
		return get().lookupEjbRemote(CampaignFacade.class);
	}

	public static CampaignDiagramDefinitionFacade getCampaignDiagramDefinitionFacade() {
		return get().lookupEjbRemote(CampaignDiagramDefinitionFacade.class);
	}

	public static CampaignFormMetaFacade getCampaignFormMetaFacade() {
		return get().lookupEjbRemote(CampaignFormMetaFacade.class);
	}

	public static CampaignFormDataFacade getCampaignFormDataFacade() {
		return get().lookupEjbRemote(CampaignFormDataFacade.class);
	}

	public static CampaignStatisticsFacade getCampaignStatisticsFacade() {
		return get().lookupEjbRemote(CampaignStatisticsFacade.class);
	}

	public static AreaFacade getAreaFacade() {
		return get().lookupEjbRemote(AreaFacade.class);
	}

	public static DocumentTemplateFacade getDocumentTemplateFacade() {
		return get().lookupEjbRemote(DocumentTemplateFacade.class);
	}

	public static DocumentFacade getDocumentFacade() {
		return get().lookupEjbRemote(DocumentFacade.class);
	}

	public static SystemEventFacade getSystemEventFacade() {

		return get().lookupEjbRemote(SystemEventFacade.class);
	}

	public static I18nFacade getI18nFacade() {
		return get().lookupEjbRemote(I18nFacade.class);
	}

	public static UserRightsFacade getUserRightsFacade() {
		return get().lookupEjbRemote(UserRightsFacade.class);
	}

	public static InfoFacade getInfoFacade() {
		return get().lookupEjbRemote(InfoFacade.class);
	}

	@SuppressWarnings("unchecked")
	public <P> P lookupEjbRemote(Class<P> clazz) {
		try {
			return (P) get().ic.lookup(buildJndiLookupName(clazz));
		} catch (NamingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static String buildJndiLookupName(Class<?> clazz) {
		return JNDI_PREFIX + clazz.getSimpleName();
	}
}
