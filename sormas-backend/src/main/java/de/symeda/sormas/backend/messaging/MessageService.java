package de.symeda.sormas.backend.messaging;

import java.util.Arrays;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.AbstractCoreAdoService;
import de.symeda.sormas.backend.common.AdoServiceWithUserFilter;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.user.User;

@Stateless
@LocalBean
public class MessageService extends AdoServiceWithUserFilter<Message> {

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
		if (messageCriteria.getUserRole() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(Message.USER_ROLES), messageCriteria.getUserRole()));
		}
		
		if (messageCriteria.getUserType() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(Message.USER_TYPE), messageCriteria.getUserType()));
		}

		if (messageCriteria.getArea() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(Message.AREA), messageCriteria.getArea()));
		}
		
		if (messageCriteria.getRegion() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(Message.REGION), messageCriteria.getRegion()));
		}
		
		if (messageCriteria.getDistrict() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(Message.DISTRICT), messageCriteria.getDistrict()));
		}

		if (messageCriteria.getFreeText() != null) {
			String[] textFilters = (messageCriteria.getFreeText().split("\\s+"));
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb
						.or(CriteriaBuilderHelper.unaccentedIlikeCustom(cb, from.get(Message.MESSAGE_CONTENT), textFilter),
								CriteriaBuilderHelper.ilike(cb, from.get(Message.UUID), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		return filter;
	}

	public Predicate buildCriteriaFilterCustom(MessageCriteria messageCriteria, CriteriaBuilder cb,
			Root<Message> from, Set<UserRole> userRoles) {
			
		Predicate filter = null;
		if (userRoles != null) {
//			String[] textFilters = userRoles.split("\\s+");
			for (UserRole textFilter : userRoles) {
				if (DataHelper.isNullOrEmpty(textFilter.toString())) {
					continue;
				}
//				Predicate likeFilters = cb.and(cb, from.get(Message.USER_ROLES), textFilter);
				filter = CriteriaBuilderHelper.and(cb, filter);
			}
		}
		
//		if (userRoles != null) {
//			Join<Message, UserRole> joinRoles = from.join(Message.USER_ROLES, JoinType.LEFT);
//			Predicate rolesFilter = joinRoles.in(userRoles);
//			filter = CriteriaBuilderHelper.and(cb, filter, rolesFilter);
//		}
		
//		if (messageCriteria.getUserType() != null) {
//			Join<Message, UserType> joinType = from.join(Message.USER_TYPE, JoinType.LEFT);
//			Predicate rolesFilter = joinType.in(messageCriteria.getUserType());
//			filter = CriteriaBuilderHelper.and(cb, filter, rolesFilter);
//		}
		
//		if (messageCriteria.getArea() != null) {
//			Join<Message, Area> joinType = from.join(Message.AREA, JoinType.LEFT);
//			Predicate areaFilter = joinType.in(messageCriteria.getArea());
//			filter = CriteriaBuilderHelper.and(cb, filter, areaFilter
//					cb.equal(from.get(Message.AREA), messageCriteria.getArea())
//					);
//		}

		return filter;
	}
	
}
