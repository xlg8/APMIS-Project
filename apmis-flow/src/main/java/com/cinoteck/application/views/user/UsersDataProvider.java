package com.cinoteck.application.views.user;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cinoteck.application.views.configurations.RegionFilter;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.user.UserDto;

public class UsersDataProvider extends AbstractBackEndDataProvider<UserDto, UserFilter>{
	
	private static final long serialVersionUID = -1152945214310841479L;
	final List<UserDto> usersData = FacadeProvider.getUserFacade().getIndexList(null, null, null, null).stream()
			.collect(Collectors.toList());

	@Override
	protected Stream<UserDto> fetchFromBackEnd(Query<UserDto, UserFilter> query) {
		Stream<UserDto> stream = usersData.stream();
		
		// Filtering
        if (query.getFilter().isPresent()) {
            stream = stream.filter(e -> query.getFilter().get().test(e));
        }
        return stream.skip(query.getOffset()).limit(query.getLimit());
	}

	@Override
	protected int sizeInBackEnd(Query<UserDto, UserFilter> query) {
		   return (int) fetchFromBackEnd(query).count();
	}

}
