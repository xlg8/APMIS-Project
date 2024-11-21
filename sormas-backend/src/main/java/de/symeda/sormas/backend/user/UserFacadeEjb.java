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
package de.symeda.sormas.backend.user;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;

import com.vladmihalcea.hibernate.type.util.SQLExtractor;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.campaign.CampaignLogDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogCriteria;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.report.UserReportModelDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserActivitySummaryDto;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserFacade;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserRole.UserRoleValidationException;
import de.symeda.sormas.api.user.UserSyncResult;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DefaultEntityHelper;
import de.symeda.sormas.api.utils.PasswordHelper;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.campaign.CampaignLog;
import de.symeda.sormas.backend.campaign.data.CampaignFormData;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.event.EventService;
import de.symeda.sormas.backend.infrastructure.ConfigurationChangeLog;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.area.AreaFacadeEjb;
import de.symeda.sormas.backend.infrastructure.area.AreaService;
import de.symeda.sormas.backend.infrastructure.community.Community;
import de.symeda.sormas.backend.infrastructure.community.CommunityFacadeEjb;
import de.symeda.sormas.backend.infrastructure.community.CommunityService;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.district.DistrictFacadeEjb;
import de.symeda.sormas.backend.infrastructure.district.DistrictService;
import de.symeda.sormas.backend.infrastructure.facility.Facility;
import de.symeda.sormas.backend.infrastructure.facility.FacilityFacadeEjb;
import de.symeda.sormas.backend.infrastructure.facility.FacilityService;
import de.symeda.sormas.backend.infrastructure.pointofentry.PointOfEntryFacadeEjb;
import de.symeda.sormas.backend.infrastructure.pointofentry.PointOfEntryService;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.infrastructure.region.RegionFacadeEjb;
import de.symeda.sormas.backend.infrastructure.region.RegionService;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.location.LocationFacadeEjb;
import de.symeda.sormas.backend.location.LocationFacadeEjb.LocationFacadeEjbLocal;
import de.symeda.sormas.backend.user.event.PasswordResetEvent;
import de.symeda.sormas.backend.user.event.UserCreateEvent;
import de.symeda.sormas.backend.user.event.UserUpdateEvent;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.JurisdictionHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "UserFacade")
public class UserFacadeEjb implements UserFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private UserService userService;

	@EJB
	private UserActivitySummaryService userActivitySummaryService;

	@EJB
	private LocationFacadeEjbLocal locationFacade;
	@EJB
	private RegionService regionService;
	@EJB
	private AreaService areaService;
	@EJB
	private DistrictService districtService;
	@EJB
	private CommunityService communityService;
	@EJB
	private FacilityService facilityService;
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	@EJB
	private CaseService caseService;
	@EJB
	private ContactService contactService;
	@EJB
	private EventService eventService;
	@EJB
	private PointOfEntryService pointOfEntryService;
	@Inject
	private Event<UserCreateEvent> userCreateEvent;
	@Inject
	private Event<UserUpdateEvent> userUpdateEvent;
	@Inject
	private Event<PasswordResetEvent> passwordResetEvent;

	@EJB
	private UserFacadeEjb.UserFacadeEjbLocal userServiceEBJ;

	public static String extractToken(User source) {

		if (source == null) {
			return null;
		}

		return source.getToken();
	}

	public static UserDto toDto(User source) {

		if (source == null) {
			return null;
		}

		UserDto target = new UserDto();
		DtoHelper.fillDto(target, source);

		target.setActive(source.isActive());
		target.setCommomUser(source.getUsertype() == UserType.COMMON_USER);
		target.setUserName(source.getUserName());
		target.setFirstName(source.getFirstName());
		target.setLastName(source.getLastName());
		target.setUserPosition(source.getUserPosition());
		target.setUserOrganisation(source.getUserOrganisation());
		target.setUserEmail(source.getUserEmail());
		target.setPhone(source.getPhone());
		target.setToken(source.getToken());
		target.setAddress(LocationFacadeEjb.toDto(source.getAddress()));
		target.setArea(AreaFacadeEjb.toReferenceDto(source.getArea()));
		target.setRegion(RegionFacadeEjb.toReferenceDto(source.getRegion()));
		target.setDistrict(DistrictFacadeEjb.toReferenceDto(source.getDistrict()));
		if (source.getDistricts() != null) {
			target.setDistricts(DistrictFacadeEjb.toReferenceDto(new HashSet<District>(source.getDistricts())));
		}
		if (source.getCommunity() != null) {
			target.setCommunity(CommunityFacadeEjb.toReferenceDto(new HashSet<Community>(source.getCommunity())));
		}

		target.setLanguage(source.getLanguage());

		target.setUserRoles(new HashSet<UserRole>(source.getUserRoles()));

		target.setFormAccess(new HashSet<FormAccess>(source.getFormAccess()));

		target.setUsertype(source.getUsertype());

		if (source.getArea() != null && source.getArea().getExternalId() != null) {
			target.setPcode(source.getArea().getExternalId().toString());
		}
		if (source.getDistrict() != null && source.getDistrict().getExternalId() != null) {
			target.setDcode(source.getDistrict().getExternalId().toString());
		}
		if (source.getRegion() != null && source.getRegion().getExternalId() != null) {
			target.setRcode(source.getRegion().getExternalId().toString());
		}
		if (source.getCommunity() != null && !source.getCommunity().isEmpty()) {
			Set<String> communitynos = new HashSet<>();
			for (Community c : source.getCommunity()) {
				if (c.getClusterNumber() != null && c != null) {
					communitynos.add(c.getClusterNumber().toString());
				}
			}
			target.setCommunitynos(communitynos);
		}
		return target;
	}

	public static UserReportModelDto ListtoDto(User source) {

		if (source == null) {
			return null;
		}
		CommunityService comd = new CommunityService();
		List<Community> comm = comd.getByAll();
		return ListtoDtoCommunityReporter(source, comm);
	}

	public static UserReportModelDto ListtoDtoCommunityReporter(User source, List<Community> comm) {
		UserReportModelDto target = new UserReportModelDto();
		DtoHelper.fillDto(target, source);

		target.setActive(source.isActive());
		target.setUserName(source.getUserName());
		target.setFirstName(source.getFirstName());
		target.setLastName(source.getLastName());
		target.setUserPosition(source.getUserPosition());
		target.setUserOrganisation(source.getUserOrganisation());
		target.setUserEmail(source.getUserEmail());
		target.setPhone(source.getPhone());
		target.setArea(AreaFacadeEjb.toReferenceDto(source.getArea()));
		target.setRegion(RegionFacadeEjb.toReferenceDto(source.getRegion()));
		target.setDistrict(DistrictFacadeEjb.toReferenceDto(source.getDistrict()));
		target.setDistricts(DistrictFacadeEjb.toReferenceDto(new HashSet<District>(source.getDistricts())));
		target.setCommunity(CommunityFacadeEjb.toReferenceDto(new HashSet<Community>(source.getCommunity())));
//		//System.out.println(source.getDistrict()+" :@@@@@@@@@@@@@@@@@@@@@@@@@@ area: "+ source.getArea() +" ##########@@@@@@@@@@@@@@@"+source.getCommunity());
//		target.setAssociatedOfficer(toReferenceDto(source.getAssociatedOfficer()));
//		target.setLaboratory(FacilityFacadeEjb.toReferenceDto(source.getLaboratory()));
//		target.setPointOfEntry(PointOfEntryFacadeEjb.toReferenceDto(source.getPointOfEntry()));
//		target.setLimitedDisease(source.getLimitedDisease());
//		target.setLanguage(source.getLanguage());
//		target.setHasConsentedToGdpr(source.isHasConsentedToGdpr());

		source.getUserRoles().size();
		target.setUserRoles(new HashSet<UserRole>(source.getUserRoles()));

		source.getFormAccess().size();
		target.setFormAccess(new HashSet<FormAccess>(source.getFormAccess()));

		target.setUsertype(source.getUsertype());
		return target;
	}

	public static UserReferenceDto toReferenceDto(User entity) {

		if (entity == null) {
			return null;
		}

		UserReferenceDto dto = new UserReferenceDto(entity.getUuid(), entity.getFirstName(), entity.getLastName(),
				entity.getUserRoles(), entity.getFormAccess(), entity.getUsertype());
		return dto;
	}

	public static UserReferenceDto toReferenceDto(UserReference entity) {

		if (entity == null) {
			return null;
		}

		UserReferenceDto dto = new UserReferenceDto(entity.getUuid(), entity.getFirstName(), entity.getLastName(),
				entity.getUserRoles(), entity.getFormAccess(), entity.getUserType());
		return dto;
	}

	private List<String> toUuidList(HasUuid hasUuid) {

		/*
		 * Supports conversion of a null object into a list with one "null" value in it.
		 * Uncertain if that use case exists, but wasn't suppose to be broken when
		 * replacing the Dto to Entity lookup.
		 */
		return Arrays.asList(hasUuid == null ? null : hasUuid.getUuid());
	}

	@Override
	public List<UserReferenceDto> getUsersByAreaAndRoles(AreaReferenceDto areaRef, UserRole... assignableRoles) {

		return userService.getReferenceList(toUuidList(areaRef), null, false, true, true, assignableRoles).stream()
				.map(f -> toReferenceDto(f)).collect(Collectors.toList());
	}

	@Override
	public List<UserReferenceDto> getUsersByAreasAndRoles(List<AreaReferenceDto> areaRefs,
			UserRole... assignableRoles) {

		return userService
				.getReferenceList(areaRefs.stream().map(AreaReferenceDto::getUuid).collect(Collectors.toList()), null,
						false, true, true, assignableRoles)
				.stream().map(UserFacadeEjb::toReferenceDto).collect(Collectors.toList());
	}

	@Override
	public List<UserReferenceDto> getUsersByRegionAndRoles(RegionReferenceDto regionRef, UserRole... assignableRoles) {

		return userService.getReferenceList(toUuidList(regionRef), null, false, true, true, assignableRoles).stream()
				.map(f -> toReferenceDto(f)).collect(Collectors.toList());
	}

	@Override
	public List<UserReferenceDto> getUsersByRegionsAndRoles(List<RegionReferenceDto> regionRefs,
			UserRole... assignableRoles) {

		return userService
				.getReferenceList(regionRefs.stream().map(RegionReferenceDto::getUuid).collect(Collectors.toList()),
						null, false, true, true, assignableRoles)
				.stream().map(UserFacadeEjb::toReferenceDto).collect(Collectors.toList());
	}

	@Override
	/*
	 * Get all users with the next higher jurisdiction, whose location contains the
	 * current users location For facility users, this includes district and
	 * community users, if their district/community is identical with that of the
	 * facility
	 */
	public List<UserReferenceDto> getUsersWithSuperiorJurisdiction(UserDto user) {
		JurisdictionLevel superordinateJurisdiction = JurisdictionHelper
				.getSuperordinateJurisdiction(UserRole.getJurisdictionLevel(user.getUserRoles()));

		List<UserReference> superiorUsersList = Collections.emptyList();
		switch (superordinateJurisdiction) {
		case NATION:
			superiorUsersList = userService.getReferenceList(null, null, null, false, false, true,
					UserRole.getWithJurisdictionLevels(superordinateJurisdiction));
			break;
		case AREA:
			superiorUsersList = userService.getReferenceList(Arrays.asList(user.getArea().getUuid()), null, null, false,
					false, true, UserRole.getWithJurisdictionLevels(superordinateJurisdiction));
			break;
		case REGION:
			superiorUsersList = userService.getReferenceList(Arrays.asList(user.getRegion().getUuid()), null, null,
					false, false, true, UserRole.getWithJurisdictionLevels(superordinateJurisdiction));
			break;
		case DISTRICT:
			// if user is assigned to a facility, but that facility is not assigned to a
			// district, show no superordinate users. Else, show users of the district (and
			// community) in which the facility is located

			District district = null;
			Community community = null;
			List<UserRole> superordinateRoles = UserRole.getWithJurisdictionLevels(superordinateJurisdiction);
			if (user.getDistrict() != null) {
				district = districtService.getByReferenceDto(user.getDistrict());
			} else if (user.getHealthFacility() != null) {
				Facility facility = facilityService.getByReferenceDto(user.getHealthFacility());
				district = facility.getDistrict();
				community = facility.getCommunity();
				superordinateRoles.addAll(UserRole.getWithJurisdictionLevels(JurisdictionLevel.COMMUNITY));
			}

			if (community == null) {
				superiorUsersList = userService.getReferenceList(null, Arrays.asList(district.getUuid()), null, false,
						false, true, superordinateRoles);
			} else if (district != null) {
				superiorUsersList = userService.getReferenceList(null, Arrays.asList(district.getUuid()),
						Arrays.asList(community.getUuid()), false, false, true, superordinateRoles);
			}

			break;
		}

		return superiorUsersList.stream().map(f -> toReferenceDto(f)).collect(Collectors.toList());
	}

	@Override
	public List<UserReferenceDto> getUserRefsByDistrict(DistrictReferenceDto districtRef, boolean includeSupervisors,
			UserRole... userRoles) {

		return userService.getReferenceList(null, toUuidList(districtRef), includeSupervisors, true, true, userRoles)
				.stream().map(f -> toReferenceDto(f)).collect(Collectors.toList());
	}

	@Override
	public List<UserReferenceDto> getUserRefsByDistricts(List<DistrictReferenceDto> districtRefs,
			boolean includeSupervisors, UserRole... userRoles) {

		return userService
				.getReferenceList(null,
						districtRefs.stream().map(DistrictReferenceDto::getUuid).collect(Collectors.toList()),
						includeSupervisors, true, true, userRoles)
				.stream().map(UserFacadeEjb::toReferenceDto).collect(Collectors.toList());
	}

	@Override
	public List<UserReferenceDto> getAllUserRefs(boolean includeInactive) {

		return userService.getReferenceList(null, null, false, true, !includeInactive).stream()
				.map(c -> toReferenceDto(c)).collect(Collectors.toList());
	}

	@Override
	public List<UserDto> getUsersByAssociatedOfficer(UserReferenceDto associatedOfficerRef, UserRole... userRoles) {

		User associatedOfficer = userService.getByReferenceDto(associatedOfficerRef);
		return userService.getAllByAssociatedOfficer(associatedOfficer, userRoles).stream().map(f -> toDto(f))
				.collect(Collectors.toList());
	}

	@Override
	public Page<UserDto> getIndexPage(UserCriteria userCriteria, int offset, int size,
			List<SortProperty> sortProperties) {
		List<UserDto> userIndexList = getIndexList(userCriteria, offset, size, sortProperties);
		long totalElementCount = count(userCriteria);
		return new Page<>(userIndexList, offset, size, totalElementCount);
	}

	@Override
	public List<UserDto> getAllAfter(Date date) {

		List<User> usr = new ArrayList<>();
		usr.add(userService.getCurrentUser());
		return usr.stream().filter(ft -> userService.getCurrentUser().getUuid().equals(ft.getUuid())).map(c -> toDto(c))
				.collect(Collectors.toList());
		/*
		 * return userService.getAllAfter(date, null).stream() .filter(ft ->
		 * userService.getCurrentUser().getUuid().equals(ft.getUuid())).map(c ->
		 * toDto(c)) .collect(Collectors.toList());
		 */
	}

	@Override
	public List<UserDto> getByUuids(List<String> uuids) {
		return userService.getByUuids(uuids).stream().map(c -> toDto(c)).collect(Collectors.toList());
	}

	@Override
	public List<String> getAllUuids() {

		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}

		return userService.getAllUuids().stream().filter(ftd -> userService.getCurrentUser().getUuid().equals(ftd))
				.collect(Collectors.toList());
	}

	@Override
	public UserDto getByUuid(String uuid) {
		return toDto(userService.getByUuid(uuid));
	}

	@Override
	public UserDto getByUserName(String userName) {
		return toDto(userService.getByUserName(userName));
	}

	@Override
	public UserDto getByEmail(String email) {
		return toDto(userService.getByEmail(email));
	}

	@Override
	public UserDto saveUser(@Valid UserDto dto) {

		User oldUser = null;
		if (dto.getCreationDate() != null) {
			try {
				oldUser = (User) BeanUtils.cloneBean(userService.getByUuid(dto.getUuid()));
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid bean access", e);
			}
		}

		User user = fromDto(dto, true);

		try {
			UserRole.validate(user.getUserRoles());
		} catch (UserRoleValidationException e) { // POST
			throw new ValidationException(e);
		}

		userService.ensurePersisted(user);

		if (oldUser == null) {
			userCreateEvent.fire(new UserCreateEvent(user));
		} else {
			userUpdateEvent.fire(new UserUpdateEvent(oldUser, user));
		}

		return toDto(user);
	}

	@Override
	public UserActivitySummaryDto saveUserActivitySummary(UserActivitySummaryDto userActivitySummaryDto) {
		userActivitySummaryDto.setCreatingUser(userServiceEBJ.getCurrentUser());
		UserActivitySummary userActivitySummary = fromDto(userActivitySummaryDto);
		userActivitySummary.setCreatingUser(userService.getCurrentUser());
		userActivitySummaryService.ensurePersisted(userActivitySummary);
		return toLogDto(userActivitySummary);
	}

	@Override
	public List<UserReportModelDto> getIndexListToDto(UserCriteria userCriteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> user = cq.from(User.class);
		Join<User, Area> area = user.join(User.AREA, JoinType.LEFT);
		Join<User, Region> region = user.join(User.REGION, JoinType.LEFT);
		Join<User, District> district = user.join(User.DISTRICT, JoinType.LEFT);
		Join<User, Location> address = user.join(User.ADDRESS, JoinType.LEFT);
		Join<User, Facility> facility = user.join(User.HEALTH_FACILITY, JoinType.LEFT);

		// TODO: We'll need a user filter for users at some point, to make sure that
		// users can edit their own details,
		// but not those of others

		Predicate filter = null;

		if (userCriteria != null) {
			// System.out.println("DEBUGGER: 45fffffffiiilibraryii = "+ userCriteria);
			filter = userService.buildCriteriaFilter(userCriteria, cb, user);
		}

		if (filter != null) {
			/*
			 * No preemptive distinct because this does collide with ORDER BY
			 * User.location.address (which is not part of the SELECT clause). UserType
			 */
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<Order>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case UserReportModelDto.UUID:
				case UserReportModelDto.ACTIVE:
				case UserReportModelDto.USER_NAME:
				case UserReportModelDto.USER_EMAIL:
					expression = user.get(sortProperty.propertyName);
					break;
				case UserReportModelDto.NAME:
					expression = user.get(User.FIRST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = user.get(User.LAST_NAME);
					break;
				case UserReportModelDto.DISTRICT:
					// System.out.println("DEBUGGER: 456ddddddt67ujhgtyuikjhu");
					expression = district.get(District.NAME);
					break;
				case UserReportModelDto.AREA:
					// System.out.println("DEBUGGER:
					// 4567uhgDdertgiiiiiiiiiilibraryiiiiiiiiiiifcwerfd9876543hgtyuikjhu");
					expression = area.get(Area.NAME);
					break;
				case UserReportModelDto.REGION:
					// System.out.println("DEBUGGER: 4567uhgfrt678456789ppppailed to load the
					// bootstrap javascrippppppppppppppp876543hgtyuikjhu");
					expression = region.get(Region.NAME);
					break;
				case UserReportModelDto.USER_ORGANISATION:
					expression = user.get(User.USER_ORGANISATION);
					// System.out.println("DEBUGGER:
					// 4567uhgfrt6oooooooooooooooooooooo78uijhgft67ujhgtyuikjhu");
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					// expression = user.get(User.USER_ORGANISATION);
					break;
				case UserReportModelDto.USER_POSITION:
					expression = user.get(User.USER_POSITION);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					// expression = user.get(User.USER_POSITION);

					// expression = facility.get(User.USER_POSITION);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(user.get(User.CHANGE_DATE)));
		}

		cq.select(user);

		// System.out.println("sdafasdeeeeeeeeeeeeeSQLeeeeeeeeeeeeeesdfhsdfg
		// "+SQLExtractor.from(em.createQuery(cq)));

		// System.out.println();

		return QueryHelper.getResultList(em, cq, first, max, UserFacadeEjb::ListtoDto);
	}

	@Override
	public List<UserDto> getIndexList(UserCriteria userCriteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {
		// System.out.println(max+" ------ "+first);

//		if(max > 500000) {
//			max = 50;
//			System.out.println(max+" --corrected---- "+first);
//		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> user = cq.from(User.class);
		Join<User, Area> area = user.join(User.AREA, JoinType.LEFT);
		Join<User, Region> region = user.join(User.REGION, JoinType.LEFT);
		Join<User, District> district = user.join(User.DISTRICT, JoinType.LEFT);
		Join<User, Location> address = user.join(User.ADDRESS, JoinType.LEFT);
		// Join<User, Facility> facility = user.join(User.HEALTH_FACILITY,
		// JoinType.LEFT);

		// TODO: We'll need a user filter for users at some point, to make sure that
		// users can edit their own details,
		// but not those of others

		Predicate filter = null;
//		Predicate predicateForRestUser = cb.isMember((UserRole.REST_USER), user.get(User.USER_ROLES));
//		Predicate predicateForCommunityUser = cb.isMember((UserRole.COMMUNITY_OFFICER), user.get(User.USER_ROLES));
//		Predicate predicateForMobileUser= cb.and(predicateForRestUser, predicateForCommunityUser);

		Set<UserRole> mobileuser = new HashSet<>();
		mobileuser.add(UserRole.REST_USER);
		mobileuser.add(UserRole.COMMUNITY_OFFICER);

		Predicate predicateForMobileUser = user.get(User.USER_ROLES).in(mobileuser);

		if (userCriteria != null) {
			// System.out.println("DEBUGGER: 45fffffffiiilibraryii = "+ userCriteria);
			filter = userService.buildCriteriaFilter(userCriteria, cb, user);
		}

		if (filter != null) {
			/*
			 * No preemptive distinct because this does collide with ORDER BY
			 * User.location.address (which is not part of the SELECT clause). UserType
			 */

//	        Path<Object> path = user.get(User.USER_ROLES);
//	        In<Object> in = cb.in(path);
//	        for (UserRole conditionColumnValue : mobileuser) {
//	        	System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ conditionColumnValue);
//	            in.value(conditionColumnValue);
//	        }
//			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ mobileuser.size());
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<Order>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case UserDto.UUID:
				case UserDto.ACTIVE:
				case UserDto.USER_NAME:
				case UserDto.USER_EMAIL:
					expression = user.get(sortProperty.propertyName);
					break;
				case UserDto.NAME:
					expression = user.get(User.FIRST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = user.get(User.LAST_NAME);
					break;
				case UserDto.DISTRICT:
					// System.out.println("DEBUGGER: 456ddddddt67ujhgtyuikjhu");
					expression = district.get(District.NAME);
					break;
				case UserDto.AREA:
					// System.out.println("DEBUGGER:
					// 4567uhgDdertgiiiiiiiiiilibraryiiiiiiiiiiifcwerfd9876543hgtyuikjhu");
					expression = area.get(Area.NAME);
					break;
				case UserDto.REGION:
					// System.out.println("DEBUGGER: 4567uhgfrt678456789ppppailed to load the
					// bootstrap javascrippppppppppppppp876543hgtyuikjhu");
					expression = region.get(Region.NAME);
					break;
				case UserDto.USER_ORGANISATION:
					expression = user.get(User.USER_ORGANISATION);
					// System.out.println("DEBUGGER:
					// 4567uhgfrt6oooooooooooooooooooooo78uijhgft67ujhgtyuikjhu");
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					// expression = user.get(User.USER_ORGANISATION);
					break;
				case UserDto.USER_POSITION:
					expression = user.get(User.USER_POSITION);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					// expression = user.get(User.USER_POSITION);

					// expression = facility.get(User.USER_POSITION);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(user.get(User.CHANGE_DATE)));
		}

		cq.select(user).distinct(true);

		TypedQuery<User> query = em.createQuery(cq);
		String sql = query.unwrap(org.hibernate.query.Query.class).getQueryString();

		return QueryHelper.getResultList(em, cq, first, max, UserFacadeEjb::toDto); 
	}

	@Override
	public long count(UserCriteria userCriteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<User> root = cq.from(User.class);

		Predicate filter = null;

		if (userCriteria != null) {
			filter = userService.buildCriteriaFilter(userCriteria, cb, root);
		}

		if (filter != null) {
			cq.where(filter);
		}

		 cq.select(cb.countDistinct(root));
		return em.createQuery(cq).getSingleResult();
	}

	private User fromDto(UserDto source, boolean checkChangeDate) {

		// System.out.println("77777");

		User target = DtoHelper.fillOrBuildEntity(source, userService.getByUuid(source.getUuid()),
				userService::createUser, checkChangeDate);

		target.setActive(source.isActive());
		target.setFirstName(source.getFirstName());
		target.setLastName(source.getLastName());
		target.setUserPosition(source.getUserPosition());
		target.setUserOrganisation(source.getUserOrganisation());
		target.setPhone(source.getPhone());
		target.setToken(source.getToken());
		target.setAddress(locationFacade.fromDto(source.getAddress(), checkChangeDate));

		target.setUserName(source.getUserName());
		target.setUserEmail(source.getUserEmail());
		target.setArea(areaService.getByReferenceDto(source.getArea()));
		target.setRegion(regionService.getByReferenceDto(source.getRegion()));
		target.setDistrict(districtService.getByReferenceDto(source.getDistrict()));
		if (source.getDistricts() != null) {
			target.setDistricts(districtService.getByReferenceDto(source.getDistricts()));
		}
		target.setCommunity(communityService.getByReferenceDto(source.getCommunity()));
		// //System.out.println(source.getDistrict()+"
		// :@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+source.getCommunity());
		target.setHealthFacility(facilityService.getByReferenceDto(source.getHealthFacility()));
		target.setAssociatedOfficer(userService.getByReferenceDto(source.getAssociatedOfficer()));
		target.setLaboratory(facilityService.getByReferenceDto(source.getLaboratory()));
		target.setPointOfEntry(pointOfEntryService.getByReferenceDto(source.getPointOfEntry()));
		target.setLimitedDisease(source.getLimitedDisease());
		target.setLanguage(source.getLanguage());
		target.setUsertype(source.getUsertype());
		target.setHasConsentedToGdpr(source.isHasConsentedToGdpr());

		target.setUserRoles(new HashSet<UserRole>(source.getUserRoles()));
		target.setFormAccess(new HashSet<FormAccess>(source.getFormAccess()));

		return target;
	}

	public UserActivitySummary fromDto(@NotNull UserActivitySummaryDto source) {

		UserActivitySummary target = new UserActivitySummary();

		target.setCreatingUser(userService.getByUuid(source.getCreatingUser().getUuid()));
		target.setAction(source.getAction());
		target.setActionModule(source.getActionModule());

		return target;
	}

	@Override
	public boolean isLoginUnique(String uuid, String userName) {
		return userService.isLoginUnique(uuid, userName);
	}

	@Override
	public String resetPassword(String uuid) {
		String resetPassword = userService.resetPassword(uuid);
		passwordResetEvent.fire(new PasswordResetEvent(userService.getByUuid(uuid)));
		return resetPassword;
	}
	
	@Override
	public String createMemorablePassword(String uuid) {
		String resetPassword = userService.createMemorablePassword(uuid);
		passwordResetEvent.fire(new PasswordResetEvent(userService.getByUuid(uuid)));
		return resetPassword;
	}
		

	@Override
	public boolean setCustomPassword(String uuid, String customPassword) {
		boolean resetPassword = userService.setCustomPassword(uuid, customPassword);
		passwordResetEvent.fire(new PasswordResetEvent(userService.getByUuid(uuid)));
		return resetPassword;
	}
	

	@Override
	public UserDto getCurrentUser() {
		return toDto(userService.getCurrentUser());
	}

	@Override
	public UserReferenceDto getCurrentUserAsReference() {
		return new UserReferenceDto(userService.getCurrentUser().getUuid());
	}

	@Override
	public Set<UserRole> getValidLoginRoles(String userName, String password) {
		System.out.println("sgetValidLoginRoles+ dfgasdfgasgas+++");
		User user = userService.getByUserName(userName);
		if (user != null && user.isActive()) {
			if (DataHelper.equal(user.getPassword(), PasswordHelper.encodePassword(password, user.getSeed()))) {
				return new HashSet<UserRole>(user.getUserRoles());
			}
		}
		return null;
	}

	@Override
	public void removeUserAsSurveillanceAndContactOfficer(String userUuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Case> caseQuery = cb.createQuery(Case.class);
		Root<Case> caseRoot = caseQuery.from(Case.class);
		Join<Case, User> surveillanceOfficerJoin = caseRoot.join(Case.SURVEILLANCE_OFFICER, JoinType.LEFT);

		caseQuery.where(cb.equal(surveillanceOfficerJoin.get(User.UUID), userUuid));
		List<Case> cases = em.createQuery(caseQuery).getResultList();
		cases.forEach(c -> {
			c.setSurveillanceOfficer(null);
			caseFacade.setResponsibleSurveillanceOfficer(c);
			caseService.ensurePersisted(c);
			caseFacade.reassignTasksOfCase(c, true);
		});

		CriteriaQuery<Contact> contactQuery = cb.createQuery(Contact.class);
		Root<Contact> contactRoot = contactQuery.from(Contact.class);
		Join<Contact, User> contactOfficerJoin = contactRoot.join(Contact.CONTACT_OFFICER, JoinType.LEFT);

		contactQuery.where(cb.equal(contactOfficerJoin.get(User.UUID), userUuid));
		List<Contact> contacts = em.createQuery(contactQuery).getResultList();
		contacts.forEach(c -> {
			c.setContactOfficer(null);
			contactService.ensurePersisted(c);
		});
	}

	@Override
	public UserSyncResult syncUser(String uuid) {
		User user = userService.getByUuid(uuid);

		UserSyncResult userSyncResult = new UserSyncResult();
		userSyncResult.setSuccess(true);

		UserUpdateEvent event = new UserUpdateEvent(user);
		event.setExceptionCallback(exceptionMessage -> {
			userSyncResult.setSuccess(false);
			userSyncResult.setErrorMessage(exceptionMessage);
		});

		this.userUpdateEvent.fire(event);

		return userSyncResult;
	}

	@Override
	public List<UserDto> getUsersWithDefaultPassword() {
		User currentUser = userService.getCurrentUser();
		if (currentUser.getUserRoles().stream().anyMatch(r -> r.hasDefaultRight(UserRight.USER_EDIT))) {
			// user is allowed to change all passwords
			// a list of all users with a default password is returned
			return userService
					.getAllDefaultUsers().stream().filter(user -> DefaultEntityHelper
							.usesDefaultPassword(user.getUserName(), user.getPassword(), user.getSeed()))
					.map(UserFacadeEjb::toDto).collect(Collectors.toList());

		} else {
			// user has only access to himself
			// the list will include him/her or will be empty
			if (DefaultEntityHelper.isDefaultUser(currentUser.getUserName()) && DefaultEntityHelper
					.usesDefaultPassword(currentUser.getUserName(), currentUser.getPassword(), currentUser.getSeed())) {
				return Collections.singletonList(UserFacadeEjb.toDto(currentUser));
			} else {
				return Collections.emptyList();
			}
		}
	}

	@Override
	public void enableUsers(List<String> userUuids) {
		updateActiveState(userUuids, true);
	}

	@Override
	public void disableUsers(List<String> userUuids) {
		updateActiveState(userUuids, false);
	}

	private void updateActiveState(List<String> userUuids, boolean active) {

		List<User> users = userService.getByUuids(userUuids);
		for (User user : users) {
			User oldUser;
			try {
				oldUser = (User) BeanUtils.cloneBean(user);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid bean access", e);
			}

			user.setActive(active);
			userService.ensurePersisted(user);

			userUpdateEvent.fire(new UserUpdateEvent(oldUser, user));
		}
	}

	@LocalBean
	@Stateless
	public static class UserFacadeEjbLocal extends UserFacadeEjb {

	}

	@Override
	public String changePassword(String uuid, String pass) {
		User user = userService.getByUserName(uuid);
		if (user == null) {
//			logger.warn("resetPassword() for unknown user '{}'", realmUserUuid);
			return "not changed";
		}
		user.setSeed(PasswordHelper.createPass(16));
		user.setPassword(PasswordHelper.encodePassword(pass, user.getSeed()));
		return "Changed";
	}

	public UserActivitySummaryDto toLogDto(UserActivitySummary source) {

		if (source == null) {
			return null;
		}
		UserActivitySummaryDto target = new UserActivitySummaryDto();

		target.setCreatingUser(userServiceEBJ.getByUuid(source.getCreatingUser().getUuid()));
		target.setAction(source.getAction());
		target.setActionModule(source.getActionModule());
		return target;
	}

	@Override
	public List<UserActivitySummaryDto> getUsersActivityByModule(String module) {
		// TODO Auto-generated method stub
		final String joinBuilder =

				"select u.action_logged, action_module, us.username, u.creationdate \n" + "from usersactivity u \n"
						+ "left outer join users us ON u.creatinguser_id = us.id \n" + "where u.action_module ilike '"
						+ module + "' ORDER BY u.creationdate DESC";

		System.out.println("=====seriesDataQuery======== " + joinBuilder);

		Query seriesDataQuery = em.createNativeQuery(joinBuilder);

		List<UserActivitySummaryDto> resultData = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		System.out.println("starting....");

		resultData.addAll(resultList.stream()
				.map((result) -> new UserActivitySummaryDto((String) result[0].toString(),
						(String) result[1].toString(), (String) result[2].toString(), (Date) result[3]))
				.collect(Collectors.toList()));

		return resultData;
	}

	@Override
	public void updateFormAccessUsers(List<String> userUuids, Set<FormAccess> accesses) {
		// TODO Auto-generated method stub
		List<User> users = userService.getByUuids(userUuids);
		for (User user : users) {

			user.setFormAccess(accesses);
			userService.ensurePersisted(user);

			userUpdateEvent.fire(new UserUpdateEvent(user));
		}

	}

//	
	public void bulkUpdateUserRoles(List<String> userUuids, UserDto userDto) {

		List<User> users = userService.getByUuids(userUuids);
		for (User user : users) {

			user.setUserRoles(new HashSet<UserRole>(userDto.getUserRoles()));

			user.setArea(areaService.getByReferenceDto(userDto.getArea()));
			user.setRegion(regionService.getByReferenceDto(userDto.getRegion()));
			user.setDistrict(districtService.getByReferenceDto(userDto.getDistrict()));
			user.setDistricts(districtService.getByReferenceDto(userDto.getDistricts()));
			System.out.println("User Roles from Userfaceade ejb when saving " + userDto.getCommunity());
//			if (userDto.getCommunity().size() > 0) {
			user.setCommunity(communityService.getByReferenceDto(userDto.getCommunity()));

//			}else {
//				Set<CommunityReferenceDto> emptyCommunity = new HashSet<>();
//				user.setCommunity(communityService.getByReferenceDto(emptyCommunity));
//			}
			userService.ensurePersisted(user);

			userUpdateEvent.fire(new UserUpdateEvent(user));
		}

	}
//	
//	
//	
//=======
//	

	@Override
	public boolean updateFcmToken(String username, String token) {

		boolean state = false;
		User user = userService.getByUserName(username);
		User oldUser;
		try {
			oldUser = (User) BeanUtils.cloneBean(user);
			user.setToken(token);
			userService.ensurePersisted(user);

			userUpdateEvent.fire(new UserUpdateEvent(oldUser, user));
			state = true;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid bean access", e);
		}
		return state;
	}

	@Override
	public List<String> getUserForFCM(Set<FormAccess> formAccesses, Set<AreaReferenceDto> areas,
			Set<RegionReferenceDto> regions, Set<DistrictReferenceDto> districts,
			Set<CommunityReferenceDto> communities) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> user = cq.from(User.class);
		Join<User, Area> area = user.join(User.AREA, JoinType.LEFT);
		Join<User, Region> region = user.join(User.REGION, JoinType.LEFT);
		Join<User, District> district = user.join(User.DISTRICT, JoinType.LEFT);
//		Join<User, Location> address = user.join(User.ADDRESS, JoinType.LEFT);

		Predicate filter = null;

		filter = userService.buildCriteriaFilterFCM(formAccesses, areas, regions, districts, communities, cb, user);

		if (filter != null) {
			cq.where(filter);
		}

		return QueryHelper.getResultList(em, cq, 0, Integer.MAX_VALUE, UserFacadeEjb::extractToken);
	}

	@Override
	public List<ConfigurationChangeLogDto> getUsersConfigurationChangeLog(ConfigurationChangeLogCriteria criteria,
			Integer first, Integer max, List<SortProperty> sortProperties) {
		// TODO Auto-generated method stub
//		final String unitName = criteria.getUnitName();
//		final String unitType = criteria.getUnitType();
//		final String action = criteria.getAction();
//		final Long unitCode = criteria.getUnitCode();

//		final String unitNameFilter = unitName != null ? "action_unit_name = "

		final String joinBuilder = " select creatinguser, action_unit_type, action_unit_name, unit_code, action_logged, creationdate, action_date "
				+ "from configurationchangelog; ";

		Query seriesDataQuery = em.createNativeQuery(joinBuilder);
		List<ConfigurationChangeLogDto> resultData = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		resultData.addAll(resultList.stream()
				.map((result) -> new ConfigurationChangeLogDto((String) result[0].toString(),
						(String) result[1].toString(), (String) result[2].toString(),
						((BigInteger) result[3]).longValue(), (String) result[4].toString(), (Date) result[5],
						(Date) result[6]))
				.collect(Collectors.toList()));

		return resultData;
	}

	@Override
	public UserDto saveUserFcmMobile(@Valid UserDto dto) {
		System.out.println("Backend hereeeeeeeeeeee token " + dto.getToken());
		User fromDto = fromDto(dto, false);
		userService.ensurePersisted(fromDto);
		return toDto(fromDto);
	}

	@Override
	public void updateLastLoginDate(Date lastUserLoginDate, String username) {
		// TODO Auto-generated method stub
		User user = userService.getByUserName(username);

//		System.out.println(user + " =============uservuseruseruser" +lastUserLoginDate );
		user.setLastLoginDate(lastUserLoginDate);
		userService.ensurePersisted(user);
		userUpdateEvent.fire(new UserUpdateEvent(user));

	}
	
//	@Override
//	public void updatePreviousLoginDate(Date previousUserLoginDate, String username) {
//		User user = userService.getByUserName(username);
//
//		user.setPreviouslogindate(previousUserLoginDate);
//		userService.ensurePersisted(user);
//		userUpdateEvent.fire(new UserUpdateEvent(user));
//	}

	@Schedule(second = "0", minute = "0", hour = "2", persistent = false)
	public void deactivateInactiveUsers() {
		try {
			runScheduledTask();
		} catch (Exception e) {
			System.err.println(
					e.toString() + " An Error Occured While Trying to Update Users Active Status After Inactivity.");
		}

	}

	public void runScheduledTask() {
		final String joinBuilder = "UPDATE users SET active = false WHERE lastlogindate <= CURRENT_DATE - INTERVAL "
				+ "'" + "270 days" + "';";

		System.out.println("Scheduled task executed at: " + new java.util.Date());

		em.createNativeQuery(joinBuilder).executeUpdate();

	}
//
//	@Override
//	public Date checkUsersActiveStatusByUsernameandActiveStatus(String username) {
//		// Use a parameterized query to prevent SQL injection
//		String getlastLoginDateQuery = "SELECT u.lastlogindate FROM users u WHERE u.username ilike " + "'" + username
//				+ "';";
//
//		// Use parameterized query with createNativeQuery
//		Query seriesDataQuery = em.createNativeQuery(getlastLoginDateQuery);
//		seriesDataQuery.setParameter("username", username);
//
//		// Execute query and cast result to Date
//		return seriesDataQuery.getSingleResult();
//
//	}
	
	@Override
    public Date checkUsersActiveStatusByUsernameandActiveStatus(String username) {
        String getLastLoginDateQuery = "SELECT u.lastlogindate FROM users u WHERE LOWER(u.username) = LOWER(:username)";
        
        try {
            Query query = em.createNativeQuery(getLastLoginDateQuery)
                            .setParameter("username", username);
            
            Object result = query.getSingleResult();
            
            if (result instanceof Date) {
                return (Date) result;
            } else if (result instanceof java.sql.Timestamp) {
                return new Date(((java.sql.Timestamp) result).getTime());
            } else {
             
                System.err.println("Unexpected result type: " + (result != null ? result.getClass().getName() : "null"));
                return null;
            }
        } catch (NoResultException e) {
            // No result found for the given username
            System.err.println("Unexpected result type: No result found for the given username.");

            return null;
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            return null;
        }
    }
	
//	@Override
//    public Date getPreviousLoginDateByUsername(String username) {
//        String getPreviousLoginDateQuery = "SELECT u.previouslogindate FROM users u WHERE LOWER(u.username) = LOWER(:username)";
//        
//        try {
//            Query query = em.createNativeQuery(getPreviousLoginDateQuery)
//                            .setParameter("username", username);
//            
//            Object result = query.getSingleResult();
//            
//            if (result instanceof Date) {
//                return (Date) result;
//            } else if (result instanceof java.sql.Timestamp) {
//                return new Date(((java.sql.Timestamp) result).getTime());
//            } else {
//             
//                System.err.println("Unexpected result type: " + (result != null ? result.getClass().getName() : "null"));
//                return null;
//            }
//        } catch (NoResultException e) {
//            // No result found for the given username
//            System.err.println("Unexpected result type: No result found for the given username.");
//
//            return null;
//        } catch (Exception e) {
//            // Log the exception
//            e.printStackTrace();
//            return null;
//        }
//    }
	
}
