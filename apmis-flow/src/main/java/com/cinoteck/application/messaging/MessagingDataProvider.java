package com.cinoteck.application.messaging;

import java.util.stream.Stream;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.form.CampaignFormCriteria;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.messaging.MessageDto;

public class MessagingDataProvider extends AbstractBackEndDataProvider<MessageDto, MessageCriteria> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	protected Stream<MessageDto> fetchFromBackEnd(Query<MessageDto, MessageCriteria> query) {	
		return FacadeProvider.getMessageFacade().getIndexList(
				 query.getFilter().orElse(null),
                 query.getOffset(),
                 query.getLimit(),
                 null).stream();
	}

	@Override
	protected int sizeInBackEnd(Query<MessageDto, MessageCriteria> query) {		
		return (int) FacadeProvider.getMessageFacade().count(query.getFilter().orElse(null));
	}

}
