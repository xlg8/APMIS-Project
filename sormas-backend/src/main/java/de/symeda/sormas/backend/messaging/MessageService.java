package de.symeda.sormas.backend.messaging;

import java.util.Arrays;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.AbstractCoreAdoService;
import de.symeda.sormas.backend.common.AdoServiceWithUserFilter;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.area.AreaService;
import de.symeda.sormas.backend.infrastructure.community.Community;
import de.symeda.sormas.backend.infrastructure.community.CommunityService;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.district.DistrictService;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.infrastructure.region.RegionService;
import de.symeda.sormas.backend.user.User;

@Stateless
@LocalBean
public class MessageService extends AdoServiceWithUserFilter<Message> {

	@EJB
	private AreaService areaService;

	@EJB
	private RegionService regionService;

	@EJB
	private DistrictService districtService;

	@EJB
	private CommunityService communityService;
	
	public MessageService() {
		super(Message.class);
	}

	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, Message> from) {
		// TODO Auto-generated method stub
		return null;
	}

	public Predicate buildCriteriaFilter(MessageCriteria messageCriteria, CriteriaBuilder cb, Root<Message> from) {

		Predicate filter = null;
//		if (messageCriteria.getUserRole() != null) {
//			filter = CriteriaBuilderHelper.and(cb, filter,
//					cb.equal(from.get(Message.USER_ROLES), messageCriteria.getUserRole()));
//		}

		if (messageCriteria.getUserRole() != null) {
			Join<User, UserRole> joinRoles = from.join(Message.USER_ROLES, JoinType.LEFT);
			filter = CriteriaBuilderHelper.and(cb, filter, joinRoles.in(Arrays.asList(messageCriteria.getUserRole())));
		}

		if (messageCriteria.getFormAccess() != null) {
			Join<User, FormAccess> joinFormAccess = from.join(Message.MESSAGE_FORM_ACCESS, JoinType.LEFT);
			filter = CriteriaBuilderHelper.and(cb, filter, joinFormAccess.in(Arrays.asList(messageCriteria.getFormAccess())));
		}
		
//		if (messageCriteria.getArea() != null) {
//			filter = from.get(Message.AREA).in(areaService.getByUuid(messageCriteria.getArea().getUuid()));
//		}
//
//		if (messageCriteria.getRegion() != null) {
//			filter = from.get(Message.REGION).in(regionService.getByUuid(messageCriteria.getRegion().getUuid()));
//		}
//
//		if (messageCriteria.getDistrict() != null) {
//			filter = from.get(Message.DISTRICT).in(districtService.getByUuid(messageCriteria.getDistrict().getUuid()));
//		}
		
		if (messageCriteria.getArea() != null) {
			Join<Message, Area> joinAreas = from.join(Message.AREA, JoinType.LEFT);
			Predicate areaFilter = joinAreas.in(areaService.getByUuid(messageCriteria.getArea().getUuid()));
			filter = CriteriaBuilderHelper.and(cb, filter, areaFilter);
		}
		
		if (messageCriteria.getRegion() != null) {
			Join<Message, Region> joinRegion = from.join(Message.REGION, JoinType.LEFT);
			Predicate regionFilter = joinRegion.in(regionService.getByUuid(messageCriteria.getRegion().getUuid()));
			filter = CriteriaBuilderHelper.and(cb, filter, regionFilter);
		}
		
		if (messageCriteria.getDistrict() != null) {
			Join<Message, District> joinDistrict = from.join(Message.DISTRICT, JoinType.LEFT);
			Predicate districtFilter = joinDistrict.in(districtService.getByUuid(messageCriteria.getDistrict().getUuid()));
			filter = CriteriaBuilderHelper.and(cb, filter, districtFilter);
		}

		if (messageCriteria.getFreeText() != null) {
			String[] textFilters = (messageCriteria.getFreeText().split("\\s+"));
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
						CriteriaBuilderHelper.unaccentedIlikeCustom(cb, from.get(Message.MESSAGE_CONTENT), textFilter),
						CriteriaBuilderHelper.ilike(cb, from.get(Message.UUID), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		return filter;
	}

	public Predicate buildCriteriaFilterCustom(MessageCriteria messageCriteria, CriteriaBuilder cb, Root<Message> from,
			Set<UserRole> userRoles, Set<FormAccess> formAccess) {

		Predicate filter = null;
		if (userRoles.size() > 0) {
			Join<Message, UserRole> joinRoles = from.join(Message.USER_ROLES, JoinType.LEFT);
			Predicate rolesFilter = joinRoles.in(userRoles);
			filter = CriteriaBuilderHelper.and(cb, filter, rolesFilter);
		}
		
		if (formAccess.size() > 0) {
			Join<Message, FormAccess> joinFormAccess = from.join(Message.MESSAGE_FORM_ACCESS, JoinType.LEFT);
			Predicate formAccessFilter = joinFormAccess.in(formAccess);
			filter = CriteriaBuilderHelper.and(cb, filter, formAccessFilter);
		}
		
		return filter;
	}
		
	public Predicate buildCriteriaFilterArea(MessageCriteria messageCriteria, CriteriaBuilder cb, Root<Message> from) {

		Predicate filter = null;		
		if (messageCriteria.getArea() != null) {
			Join<Message, Area> joinAreas = from.join(Message.AREA, JoinType.LEFT);
			Predicate areaFilter = joinAreas.in(areaService.getByUuid(messageCriteria.getArea().getUuid()));
			filter = CriteriaBuilderHelper.and(cb, filter, areaFilter);	
		}
		return filter;
	}
		
	public Predicate buildCriteriaFilterRegion(MessageCriteria messageCriteria, CriteriaBuilder cb, Root<Message> from) {

		Predicate filter = null;		
		if (messageCriteria.getRegion() != null) {
			Join<Message, Region> joinRegion = from.join(Message.REGION, JoinType.LEFT);
			Predicate regionFilter = joinRegion.in(regionService.getByUuid(messageCriteria.getRegion().getUuid()));
			filter = CriteriaBuilderHelper.and(cb, filter, regionFilter);
		}
		return filter;
	}
	
	public Predicate buildCriteriaFilterDistrict(MessageCriteria messageCriteria, CriteriaBuilder cb, Root<Message> from) {

		Predicate filter = null;		
		if (messageCriteria.getDistrict() != null) {
			Join<Message, District> joinDistrict = from.join(Message.DISTRICT, JoinType.LEFT);
			Predicate districtFilter = joinDistrict.in(districtService.getByUuid(messageCriteria.getDistrict().getUuid()));
			filter = CriteriaBuilderHelper.and(cb, filter, districtFilter);
		}
		return filter;
	}	
	
	public Predicate buildCriteriaFilterCommunity(MessageCriteria messageCriteria, CriteriaBuilder cb, Root<Message> from) {

		Predicate filter = null;		
		if (messageCriteria.getCommunity() != null) {
			Join<Message, Community> joinCommunity = from.join(Message.COMMUNITY, JoinType.LEFT);
			Predicate communityFilter = joinCommunity.in(communityService.getByUuid(messageCriteria.getCommunity().getUuid()));
			filter = CriteriaBuilderHelper.and(cb, filter, communityFilter);
		}
		return filter;
	}
}
