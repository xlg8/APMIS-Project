package com.cinoteck.application.views.configurations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.area.AreaCriteria;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.region.RegionCriteria;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;

public class RegionFilterDataProvider  extends AbstractBackEndDataProvider<AreaDto, AreaCriteria> {
	
	AreaCriteria areaCriteria;
	
	private  List<AreaDto> DATABASE;
	
	@Override
	protected Stream<AreaDto> fetchFromBackEnd(Query<AreaDto, AreaCriteria> query) {
//		areaCriteria.relevanceStatus(EntityRelevanceStatus.ACTIVE);
		DATABASE = new ArrayList<>(FacadeProvider.getAreaFacade().getAllActiveAsReferenceAndPopulation());
		
		Stream<AreaDto> stream = DATABASE.stream();

		// TODO Auto-generated method stub
		if (query.getFilter().isPresent()) {
			stream = stream.filter(person -> query.getFilter().get().equals(person));
		}

		if (query.getSortOrders().size() > 0) {
			stream = stream.sorted(sortComparator(query.getSortOrders()));
		}

		return FacadeProvider.getAreaFacade().getIndexList(
				query.getFilter().orElse(null),
                query.getOffset(),
                query.getLimit(),
                null).stream();
	}
		private static Comparator<AreaDto> sortComparator(List<QuerySortOrder> sortOrders) {
			return sortOrders.stream().map(sortOrder -> {
				Comparator<AreaDto> comparator = personFieldComparator(sortOrder.getSorted());

				if (sortOrder.getDirection() == SortDirection.ASCENDING) {
					comparator = comparator.reversed();
				}

				return comparator;
			}).reduce(Comparator::thenComparing).orElse((p1, p2) -> 0);
	}
		private static Comparator<AreaDto> personFieldComparator(String sorted) {
	        if (sorted.equals("area")) {
	            return Comparator.comparing(person -> person.toString());
	        } 
	        return (p1, p2) -> 0;
	    }
		@Override
		protected int sizeInBackEnd(Query<AreaDto, AreaCriteria> query) {
			// TODO Auto-generated method stub
			return 1;
		}

		

}
