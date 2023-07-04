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
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;

public class DistrictDataProvider extends AbstractBackEndDataProvider<DistrictIndexDto, DistrictCriteria> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7345965237429493032L;
	final List<DistrictIndexDto> DATABASE = new ArrayList<>(FacadeProvider.getDistrictFacade().getAllDistricts());
	DistrictCriteria crteria;

	@Override
	protected Stream<DistrictIndexDto> fetchFromBackEnd(Query<DistrictIndexDto, DistrictCriteria> query) {

		Stream<DistrictIndexDto> stream = DATABASE.stream();

		if (query.getFilter().isPresent()) {
			stream = stream.filter(person -> query.getFilter().get().equals(person));
		}

		if (query.getSortOrders().size() > 0) {
			stream = stream.sorted(sortComparator(query.getSortOrders()));
		}


		return FacadeProvider.getDistrictFacade()
                .getIndexList(
                        query.getFilter().orElse(null),
                        query.getOffset(),
                        query.getLimit(),
                        null).stream();

	}

	@Override
	protected int sizeInBackEnd(Query<DistrictIndexDto, DistrictCriteria> query) {
		// TODO Auto-generated method stub
		return (int) fetchFromBackEnd(query).count();
	}

	private static Comparator<DistrictIndexDto> sortComparator(List<QuerySortOrder> sortOrders) {
		return sortOrders.stream().map(sortOrder -> {
			Comparator<DistrictIndexDto> comparator = personFieldComparator(sortOrder.getSorted());

			if (sortOrder.getDirection() == SortDirection.ASCENDING) {
				comparator = comparator.reversed();
			}

			return comparator;
		}).reduce(Comparator::thenComparing).orElse((p1, p2) -> 0);
	}

	private static Comparator<DistrictIndexDto> personFieldComparator(String sorted) {
	        if (sorted.equals("district")) {
	            return Comparator.comparing(person -> person.toString());
	        } 
	        return (p1, p2) -> 0;
	    }
			

}

