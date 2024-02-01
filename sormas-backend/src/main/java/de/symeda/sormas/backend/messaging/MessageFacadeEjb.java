package de.symeda.sormas.backend.messaging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Value;
import com.google.gson.Gson;
import de.symeda.sormas.api.messaging.FCMDto;
import de.symeda.sormas.api.messaging.FCMResponseDto;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.messaging.MessageDto;
import de.symeda.sormas.api.messaging.MessageFacade;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.area.AreaFacadeEjb;
import de.symeda.sormas.backend.infrastructure.area.AreaService;
import de.symeda.sormas.backend.infrastructure.community.CommunityService;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.district.DistrictFacadeEjb;
import de.symeda.sormas.backend.infrastructure.district.DistrictService;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.infrastructure.region.RegionFacadeEjb;
import de.symeda.sormas.backend.infrastructure.region.RegionService;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "MessageFacade")
public class MessageFacadeEjb implements MessageFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private MessageService messageService;

	@EJB
	private AreaService areaService;

	@EJB
	private DistrictService districtService;

	@EJB
	private CommunityService communityService;

	@EJB
	private RegionService regionService;

	@EJB
	private UserService userService;

	@Value("${fcm.secret.key}")
	private String fcmSecretKey;

	public static MessageDto toDto(Message source) {
		if (source == null) {
			return null;
		}

		MessageDto target = new MessageDto();
		DtoHelper.fillDto(target, source);

		target.setMessageContent(source.getMessageContent());
		target.setUserTypes(source.getUsertype());
		target.setUserRoles(new HashSet<UserRole>(source.getUserRoles()));
		target.setArea(AreaFacadeEjb.toReferenceDto(source.getArea()));
		target.setRegion(RegionFacadeEjb.toReferenceDto(source.getRegion()));
		target.setDistrict(DistrictFacadeEjb.toReferenceDto(source.getDistrict()));
//		if (target.getDistricts() != null) {
//			target.setDistricts(DistrictFacadeEjb.toReferenceDto(new HashSet<District>(source.getDistricts())));	
//		}		

//		if (source.getCommunity() != null && !source.getCommunity().isEmpty()) {
//			Set<String> communitynos = new HashSet<>();
//			for (Community c : source.getCommunity()) {
//				if (c.getClusterNumber() != null && c != null) {
//					communitynos.add(c.getClusterNumber().toString());
//				}
//			}
//			target.setCommunitynos(communitynos);
//		}
		target.setCreatingUser(UserFacadeEjb.toReferenceDto(source.getCreatingUser()));

		return target;
	}

	public Message fromDto(@NotNull MessageDto source, boolean checkChangeDate) {
		Message target = (Message) DtoHelper.fillOrBuildEntity(source, messageService.getByUuid(source.getUuid()),
				Message::new, checkChangeDate);

		target.setMessageContent(source.getMessageContent());
		target.setUserRoles(new HashSet<UserRole>(source.getUserRoles()));
		target.setUsertype(source.getUserTypes());
		target.setArea(areaService.getByReferenceDto(source.getArea()));
		target.setRegion(regionService.getByReferenceDto(source.getRegion()));
		target.setDistrict(districtService.getByReferenceDto(source.getDistrict()));
//		if (target.getDistricts() != null) {
//			target.setDistricts(DistrictFacadeEjb.toReferenceDto(new HashSet<District>(source.getDistricts())));	
//		}		

//		target.setCommunity(communityService.getByReferenceDto(source.getCommunity()));
		target.setCreatingUser(userService.getByReferenceDto(source.getCreatingUser()));

		return target;
	}

	@Override
	public List<MessageDto> getIndexList(MessageCriteria messageCriteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Message> cq = cb.createQuery(Message.class);
		Root<Message> messages = cq.from(Message.class);

		Join<Message, Area> area = messages.join(Message.AREA, JoinType.LEFT);
		Join<Message, Region> region = messages.join(Message.REGION, JoinType.LEFT);
		Join<Message, District> district = messages.join(Message.DISTRICT, JoinType.LEFT);
//		Join<Message, Community> community = messages.join(Message.COMMUNITY, JoinType.LEFT);
		Join<Message, User> userJoin = messages.join(Message.CREATED_BY, JoinType.LEFT);

		cq.multiselect(messages.get(Message.UUID), messages.get(Message.MESSAGE_CONTENT),
//				criteria.getCampaignFormMeta() != null ? root.get(CampaignFormData.FORM_VALUES)
//						: cb.nullLiteral(String.class),
				messages.get(Message.USER_ROLES), area.get(Area.NAME), area.get(Area.EXTERNAL_ID),
				region.get(Region.NAME), region.get(Region.EXTERNAL_ID), district.get(District.NAME),
				district.get(District.EXTERNAL_ID),
//				community.get(Community.NAME),community.get(Community.EXTERNAL_ID), 
				userJoin.get(User.USER_NAME));

		Predicate filter = null;

		if (messageCriteria != null) {
			filter = messageService.buildCriteriaFilter(messageCriteria, cb, messages);
		}

		if (filter != null) {
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<Order>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case Message.UUID:
				case Message.MESSAGE_CONTENT:
					expression = messages.get(sortProperty.propertyName);
					break;
				case Message.USER_ROLES:
					expression = messages.get(Message.USER_ROLES);
					break;
				case Message.DISTRICT:
					expression = district.get(District.NAME);
					break;
				case Message.AREA:
					expression = area.get(Area.NAME);
					break;
				case Message.REGION:
					expression = region.get(Region.NAME);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(messages.get(Message.CHANGE_DATE)));
		}
		cq.select(messages);

		return QueryHelper.getResultList(em, cq, first, max, MessageFacadeEjb::toDto);
	}

	@Override
	public MessageDto saveMessage(@Valid MessageDto messageDto) {

		Message message = fromDto(messageDto, true);
		messageService.ensurePersisted(message);
		return toDto(message);
	}

	@Override
	public long count(MessageCriteria messageCriteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Message> from = cq.from(Message.class);

		Predicate filter = null;

		if (messageCriteria != null) {
			filter = messageService.buildCriteriaFilter(messageCriteria, cb, from);
		}

		if (filter != null) {
			cq.where(filter);
		}

		cq.select(cb.count(from));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public List<MessageDto> getMessageByUserRoles(MessageCriteria messageCriteria, UserType userType, Integer first,
			Integer max, Set<UserRole> userRoles) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Message> cq = cb.createQuery(Message.class);
		Root<Message> from = cq.from(Message.class);

		Predicate filter = null;

		if (messageCriteria != null) {
			filter = messageService.buildCriteriaFilterCustom(messageCriteria, cb, from, userRoles);
		}

		if (filter != null) {
			cq.where(filter);
		}

		cq.distinct(true).orderBy(cb.asc(from.get(Message.CHANGE_DATE)));

		return QueryHelper.getResultList(em, cq, first, max, MessageFacadeEjb::toDto);
	}

}
