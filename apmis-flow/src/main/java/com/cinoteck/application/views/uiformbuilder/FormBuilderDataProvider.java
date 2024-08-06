package com.cinoteck.application.views.uiformbuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.form.CampaignFormCriteria;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.utils.SortProperty;

public class FormBuilderDataProvider extends AbstractBackEndDataProvider<CampaignFormMetaDto, CampaignFormCriteria> {

	private static final long serialVersionUID = 1L;
	private CampaignFormCriteria filterCriteria;

	@SuppressWarnings("unchecked")
	@Override
	protected Stream<CampaignFormMetaDto> fetchFromBackEnd(Query<CampaignFormMetaDto, CampaignFormCriteria> query) {
		
		int offset = query.getOffset();
		int limit = query.getLimit();
		CampaignFormCriteria criteria = query.getFilter().orElse(filterCriteria);

		List<SortProperty> sortProperties = query.getSortOrders().stream()
				.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
						sortOrder.getDirection() == SortDirection.ASCENDING))
				.collect(Collectors.toList());

		List<CampaignFormMetaDto> listData = FacadeProvider.getCampaignFormMetaFacade().getIndexList(criteria, offset,
				limit, sortProperties);
		return listData.stream();
	}

	@Override
	protected int sizeInBackEnd(Query<CampaignFormMetaDto, CampaignFormCriteria> query) {
		CampaignFormCriteria criteria = query.getFilter().orElse(filterCriteria);
		return (int) FacadeProvider.getCampaignFormMetaFacade().count(criteria);
	}
}
