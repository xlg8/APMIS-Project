package com.cinoteck.application.views.uiformbuilder;

import java.util.stream.Stream;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.form.CampaignFormCriteria;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;

public class FormBuilderDataProvider extends AbstractBackEndDataProvider<CampaignFormMetaDto, CampaignFormCriteria> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	@Override
	protected Stream<CampaignFormMetaDto> fetchFromBackEnd(Query<CampaignFormMetaDto, CampaignFormCriteria> query) {	
		return FacadeProvider.getCampaignFormMetaFacade().getAllFormElement().stream();
	}

	@Override
	protected int sizeInBackEnd(Query<CampaignFormMetaDto, CampaignFormCriteria> query) {		
		return 55;
	}

}
