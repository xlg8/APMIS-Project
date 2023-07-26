package com.cinoteck.application.views.configurations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;

public class ClusterDataProvider  extends AbstractBackEndDataProvider<CommunityDto, CommunityCriteriaNew> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7345965237429493032L;
//	final List<CommunityDto> DATABASE = new ArrayList<>(FacadeProvider.getCommunityFacade().getAllCommunities());
//	DistrictCriteria crteria;

	@Override
	protected Stream<CommunityDto> fetchFromBackEnd(Query<CommunityDto, CommunityCriteriaNew> query) {

//		Stream<CommunityDto> stream = DATABASE.stream();
//
//		if (query.getFilter().isPresent()) {
//			stream = stream.filter(community -> query.getFilter().get().equals(community));
//		}
//
//		if (query.getSortOrders().size() > 0) {
//			stream = stream.sorted(sortComparator(query.getSortOrders()));
//		}


		return FacadeProvider.getCommunityFacade()
                .getIndexList(
                        query.getFilter().orElse(null),
                        query.getOffset(),
                        query.getLimit(),
                        null).stream();

	}

	@Override
	protected int sizeInBackEnd(Query<CommunityDto, CommunityCriteriaNew> query) {
		// TODO Auto-generated method stub
		
		return (int) FacadeProvider.getCommunityFacade().count(query.getFilter().orElse(null));
//		return (int) fetchFromBackEnd(query).count();
	}

	private static Comparator<CommunityDto> sortComparator(List<QuerySortOrder> sortOrders) {
		return sortOrders.stream().map(sortOrder -> {
			Comparator<CommunityDto> comparator = personFieldComparator(sortOrder.getSorted());

			if (sortOrder.getDirection() == SortDirection.ASCENDING) {
				comparator = comparator.reversed();
			}

			return comparator;
		}).reduce(Comparator::thenComparing).orElse((p1, p2) -> 0);
	}

	private static Comparator<CommunityDto> personFieldComparator(String sorted) {
	        if (sorted.equals("cluster")) {
	            return Comparator.comparing(person -> person.toString());
	        } 
	        return (p1, p2) -> 0;
	    }
			

}