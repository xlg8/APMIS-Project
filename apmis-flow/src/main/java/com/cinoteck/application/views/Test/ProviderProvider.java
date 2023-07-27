package com.cinoteck.application.views.Test;

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
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.report.CommunityUserReportModelDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.SortProperty;

public class ProviderProvider extends AbstractBackEndDataProvider<CommunityUserReportModelDto, CommunityCriteriaNew>{
	FormAccess formAccess;
	final List<CommunityUserReportModelDto> DATABASE = new ArrayList<>( FacadeProvider.getCommunityFacade()
			.getAllActiveCommunitytoRerenceFlow(null, null, null,
					null,
					formAccess));	
	CommunityCriteriaNew criteria;
	
					@Override
					protected Stream<CommunityUserReportModelDto> fetchFromBackEnd(
							Query<CommunityUserReportModelDto, CommunityCriteriaNew> query) {
						// TODO Auto-generated method stub
						Stream<CommunityUserReportModelDto> stream = DATABASE.stream();
						
						if (query.getFilter().isPresent()) {
							stream = stream.filter(community -> query.getFilter().get().equals(community));
						}

						if (query.getSortOrders().size() > 0) {
							stream = stream.sorted();
						}
						return FacadeProvider.getCommunityFacade()
								.getAllActiveCommunitytoRerenceFlow(query.getFilter().orElse(null), query.getOffset(), query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()),
										formAccess).stream();
					}

					@Override
					protected int sizeInBackEnd(Query<CommunityUserReportModelDto, CommunityCriteriaNew> query) {
						// TODO Auto-generated method stub
						return (int) fetchFromBackEnd(query).count();//0;
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
