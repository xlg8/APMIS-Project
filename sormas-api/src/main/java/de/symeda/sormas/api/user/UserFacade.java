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
package de.symeda.sormas.api.user;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.validation.Valid;

import org.springframework.scheduling.annotation.Scheduled;

import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogCriteria;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.report.UserReportModelDto;
import de.symeda.sormas.api.utils.SortProperty;

@Remote
public interface UserFacade {

	UserDto getByUuid(String uuid);

	UserDto saveUserFcmMobile(@Valid UserDto dto);

	UserDto saveUser(@Valid UserDto dto);

	boolean isLoginUnique(String uuid, String userName);

	String resetPassword(String uuid);
	
	String createMemorablePassword(String uuid);
	
	boolean setCustomPassword(String uuid, String customPassword);
	
	String changePassword(String uuid, String pass);

	List<UserDto> getAllAfter(Date date);

	UserDto getByUserName(String userName);

	UserDto getByEmail(String email);

	List<UserReferenceDto> getUsersByAreaAndRoles(AreaReferenceDto areaRef, UserRole... assignableRoles);

	List<UserReferenceDto> getUsersByAreasAndRoles(List<AreaReferenceDto> areaRefs, UserRole... assignableRoles);

	List<UserReferenceDto> getUsersByRegionAndRoles(RegionReferenceDto regionRef, UserRole... assignableRoles);

	List<UserReferenceDto> getUsersByRegionsAndRoles(List<RegionReferenceDto> regionRefs, UserRole... assignableRoles);

	List<UserReferenceDto> getUsersWithSuperiorJurisdiction(UserDto user);

	List<UserDto> getIndexList(UserCriteria userCriteria, Integer first, Integer max,
			List<SortProperty> sortProperties);

	List<UserReportModelDto> getIndexListToDto(UserCriteria userCriteria, Integer first, Integer max,
			List<SortProperty> sortProperties);

	Page<UserDto> getIndexPage(UserCriteria userCriteria, int offset, int size, List<SortProperty> sortProperties);

	long count(UserCriteria userCriteria);

	/**
	 * 
	 * @param district
	 * @param includeSupervisors independent from the district
	 * @param userRoles          roles of the users by district
	 * @return
	 */
	List<UserReferenceDto> getUserRefsByDistrict(DistrictReferenceDto district, boolean includeSupervisors,
			UserRole... userRoles);

	List<UserReferenceDto> getUserRefsByDistricts(List<DistrictReferenceDto> districts, boolean includeSupervisors,
			UserRole... userRoles);

	List<UserReferenceDto> getAllUserRefs(boolean includeInactive);

	List<UserDto> getUsersByAssociatedOfficer(UserReferenceDto associatedOfficer, UserRole... userRoles);

	List<String> getAllUuids();

	List<UserDto> getByUuids(List<String> uuids);

	UserDto getCurrentUser();

	UserReferenceDto getCurrentUserAsReference();

	Set<UserRole> getValidLoginRoles(String userName, String password);

	void removeUserAsSurveillanceAndContactOfficer(String userUuid);

	UserSyncResult syncUser(String userUuid);

	List<UserDto> getUsersWithDefaultPassword();

	void enableUsers(List<String> userUuids);

	void disableUsers(List<String> userUuids);

	UserActivitySummaryDto saveUserActivitySummary(UserActivitySummaryDto campaignLogDto);

	List<UserActivitySummaryDto> getUsersActivityByModule(String module);

	List<ConfigurationChangeLogDto> getUsersConfigurationChangeLog(ConfigurationChangeLogCriteria criteria,
			Integer first, Integer max, List<SortProperty> sortProperties);

	public void updateFormAccessUsers(List<String> userUuids, Set<FormAccess> accesses);

	public void bulkUpdateUserRoles(List<String> userUuids, UserDto userDto);

	boolean updateFcmToken(String username, String token);

	List<String> getUserForFCM(Set<FormAccess> formAccesses, Set<AreaReferenceDto> areas,
			Set<RegionReferenceDto> regions, Set<DistrictReferenceDto> districts,
			Set<CommunityReferenceDto> communities);

	public void updateLastLoginDate(Date lastUserLoginDate, String userName);
	
//	public void updatePreviousLoginDate(Date previousUserLoginDate, String userName);

	public void deactivateInactiveUsers();
	
	public Date checkUsersActiveStatusByUsernameandActiveStatus(String username);
	
//	public Date getPreviousLoginDateByUsername(String username);
}
