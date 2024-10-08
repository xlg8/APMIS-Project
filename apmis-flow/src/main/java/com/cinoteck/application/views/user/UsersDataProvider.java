package com.cinoteck.application.views.user;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

//	@Override
//	protected Stream<UserDto> fetchFromBackEnd(Query<UserDto, UserCriteria> query) {
////
////		Stream<UserDto> stream = DATABASE.stream();
////
////		if (query.getFilter().isPresent()) {
////			stream = stream.filter(person -> query.getFilter().get().equals(person));
////		}
////
////		if (query.getSortOrders().size() > 0) {
////			stream = stream.sorted(sortComparator(query.getSortOrders()));
////		}
//		
//		int offset = query.getOffset();
//	    int limit = query.getLimit();
//		 System.out.println("Offset: " + offset + ", Limit: " + limit);
//
//		 List<UserDto> userList = FacadeProvider.getUserFacade()
//		            .getIndexList(
//		                    query.getFilter().orElse(null),
//		                    query.getOffset(),
//		                    query.getLimit(),
//		                    null);
////		
//	    // Convert the list to a Set to ensure distinct values based on username
//	    Set<UserDto> distinctUsers = new HashSet<>();
//	    distinctUsers.addAll(userList);	
//	    		
//	    		
////	    		userList.stream()
////	            .collect(Collectors.toMap(UserDto::getUserName, user -> user, (user1, user2) -> user1))
////	            .values().stream().collect(Collectors.toSet());
//	    
//	    // Return a stream of distinct users
//	    return distinctUsers.stream();
//	}
//
//	@Override
//	protected Stream<UserDto> fetchFromBackEnd(Query<UserDto, UserCriteria> query) {
//	    int offset = query.getOffset();
//	    int limit = query.getLimit();
//	    System.out.println("Offset: " + offset + ", Limit: " + limit);
//
//	    List<UserDto> userList = FacadeProvider.getUserFacade()
//	            .getIndexList(query.getFilter().orElse(null), 0, Integer.MAX_VALUE, null); // Get all users
//
//	    if (userList.size() <= offset) {
//	        return Stream.empty(); // No users to return if offset is out of range
//	    }
//
//	    // Apply pagination
//	    List<UserDto> paginatedUsers = userList.stream()
//	            .skip(offset)
//	            .limit(limit)
//	            .collect(Collectors.toList());
//
//	    return paginatedUsers.stream(); // Return stream of paginated users
//	}
//		return FacadeProvider.getUserFacade() 
//                .getIndexList(
//                        query.getFilter().orElse(null),
//                        query.getOffset(),
//                        query.getLimit(),
//                        null).stream().distinct();
//
//	}

//	@Override
//	protected int sizeInBackEnd(Query<UserDto, UserCriteria> query) {
//		// TODO Auto-generated method stub
//		
//		//FacadeProvider.getUserFacade()
//		return (int) FacadeProvider.getUserFacade().count(query.getFilter().orElse(null));
////		return (int) fetchFromBackEnd(query).count();
//	}
	
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
	
	  @Override
	    protected Stream<UserDto> fetchFromBackEnd(Query<UserDto, UserCriteria> query) {
	        // Get the criteria from the query
	        UserCriteria criteria = query.getFilter().orElse(null);

	        // Fetch the data based on the criteria
	        Stream<UserDto> usersStream = fetchDataBasedOnCriteria(criteria, query);

	        // Ensure distinct results based on user properties (for example, user ID or other unique fields)
	        return usersStream.distinct();
	    }

	    private Stream<UserDto> fetchDataBasedOnCriteria(UserCriteria criteria, Query<UserDto, UserCriteria> query) {
	        // Your logic to fetch users based on criteria
	        // For example:
	        // return userRepository.findAll(criteria).stream();

	        // Example implementation:
	        List<UserDto> users =// ...; // Fetch users based on criteria
	        FacadeProvider.getUserFacade()
            .getIndexList(
                    query.getFilter().orElse(null),
                    query.getOffset(),
                    query.getLimit(),
                    null);
	        return users.stream();
	    }

	    @Override
	    protected int sizeInBackEnd(Query<UserDto, UserCriteria> query) {
	        // Implement logic to return the size of the filtered data
	        return (int) fetchFromBackEnd(query).count(); // Count distinct users
	    }
}