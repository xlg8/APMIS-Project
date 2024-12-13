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
package de.symeda.sormas.api.infrastructure.community;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;

import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.infrastructure.GeoLocationFacade;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.report.CommunityUserReportModelDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.SortProperty;

@Remote
public interface CommunityFacade extends GeoLocationFacade<CommunityDto, CommunityDto, CommunityReferenceDto, CommunityCriteriaNew> {

	List<CommunityReferenceDto> getAllActiveByDistrict(String districtUuid);

	Page<CommunityDto> getIndexPage(CommunityCriteriaNew communityCriteria, Integer offset, Integer size, List<SortProperty> sortProperties);

	CommunityReferenceDto getCommunityReferenceByUuid(String uuid);

	CommunityReferenceDto getCommunityReferenceById(long id);
	
	List<CommunityUserReportModelDto> getAllActiveCommunitytoRerence(CommunityCriteriaNew criteria, Integer first, Integer max, List<SortProperty> sortProperties, FormAccess formacc);
	
	Integer getAllActiveCommunitytoRerenceCount(CommunityCriteriaNew criteria, Integer first, Integer max, List<SortProperty> sortProperties, FormAccess formacc);

	List<CommunityUserReportModelDto> getAllActiveCommunitytoRerencexx(CommunityCriteriaNew criteria, Integer first, Integer max, List<SortProperty> sortProperties,  FormAccess formacc);


	// todo handle parent infra generically
	List<CommunityReferenceDto> getByName(String name, DistrictReferenceDto districtRef, boolean includeArchivedEntities);
	
	List<CommunityReferenceDto> getByExternalID(Long ext_id, DistrictReferenceDto districtRef, boolean includeArchivedEntities);

	boolean isUsedInOtherInfrastructureData(Collection<String> communityUuids);

	boolean hasArchivedParentInfrastructure(Collection<String> communityUuids);

	Map<String, String> getDistrictUuidsForCommunities(List<CommunityReferenceDto> communities);

	long countReportGrid(CommunityCriteriaNew criteria, FormAccess formacc);
	
	List<CommunityUserReportModelDto> getAllActiveCommunitytoRerenceNew(Integer first, Integer max, List<SortProperty> sortProperties, FormAccess formacc);
	
	List<CommunityDto> getAllCommunities();

	List<CommunityUserReportModelDto> getAllActiveCommunitytoRerenceFlow(CommunityCriteriaNew criteria, Integer first, Integer max, List<SortProperty> sortProperties, FormAccess formacc);

	Integer getAllActiveCommunitytoRerenceFLowCount(CommunityCriteriaNew criteria, Integer first, Integer max, List<SortProperty> sortProperties, FormAccess formacc);

	List<CommunityDto> getAllAfterWithDistrict(Date date, Set<DistrictReferenceDto> rDistdto);
	
	List<CommunityHistoryExtractDto> getClustersHistory(String uuid);
	
}
