package com.cinoteck.application.views.user;

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
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;

public class UsersDataProvider extends AbstractBackEndDataProvider<UserDto, UserCriteria>{
	
	private static final long serialVersionUID = 7345965237429493032L;
//	UserCriteria crteria;
//	final List<UserDto> DATABASE = new ArrayList<>(FacadeProvider.getUserFacade()
//			.getIndexList(crteria, null, null, null));

	@Override
	protected Stream<UserDto> fetchFromBackEnd(Query<UserDto, UserCriteria> query) {
//
//		Stream<UserDto> stream = DATABASE.stream();
//
//		if (query.getFilter().isPresent()) {
//			stream = stream.filter(person -> query.getFilter().get().equals(person));
//		}
//
//		if (query.getSortOrders().size() > 0) {
//			stream = stream.sorted(sortComparator(query.getSortOrders()));
//		}


		return FacadeProvider.getUserFacade() 
                .getIndexList(
                        query.getFilter().orElse(null),
                        query.getOffset(),
                        query.getLimit(),
                        null).stream();

	}

	@Override
	protected int sizeInBackEnd(Query<UserDto, UserCriteria> query) {
		// TODO Auto-generated method stub
		
		//FacadeProvider.getUserFacade()
//		return (int) FacadeProvider.getUserFacade().count(query.getFilter().orElse(null));
		return (int) fetchFromBackEnd(query).count();
	}
	
	private static Comparator<UserDto> sortComparator(List<QuerySortOrder> sortOrders) {
		return sortOrders.stream().map(sortOrder -> {
			Comparator<UserDto> comparator = personFieldComparator(sortOrder.getSorted());

			if (sortOrder.getDirection() == SortDirection.ASCENDING) {
				comparator = comparator.reversed();
			}

			return comparator;
		}).reduce(Comparator::thenComparing).orElse((p1, p2) -> 0);
	}

	private static Comparator<UserDto> personFieldComparator(String sorted) {
	        if (sorted.equals("user")) {
	            return Comparator.comparing(person -> person.toString());
	        } 
	        return (p1, p2) -> 0;
	 }
}