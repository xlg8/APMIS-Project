package de.symeda.sormas.api.messaging;

import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.validation.Valid;

import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.SortProperty;

@Remote
public interface MessageFacade {

	List<MessageDto> getIndexList(MessageCriteria messageCriteria, Integer first, Integer max,
			List<SortProperty> sortProperties);

	MessageDto saveMessage(@Valid MessageDto messageDto);

	long count(MessageCriteria messageCriteria);

	List<MessageDto> getMessageByUserRoles(MessageCriteria messageCriteria, UserType userType, Integer first,
			Integer max, Set<UserRole> userRoles, Set<FormAccess> formAccess);

	long getNewMessage(MessageCriteria messageCriteria, UserType userType, Integer first,
			Integer max, Set<UserRole> userRoles, Set<FormAccess> formAccess);
}
