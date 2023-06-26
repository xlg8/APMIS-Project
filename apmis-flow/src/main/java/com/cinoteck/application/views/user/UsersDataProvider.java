package com.cinoteck.application.views.user;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.user.UserDto;

public class UsersDataProvider extends AbstractBackEndDataProvider<UserDto, UsersFilter>{
	
	private static final long serialVersionUID = -1152945214310841479L;
	final List<UserDto> usersData = FacadeProvider.getUserFacade().getIndexList(null, null, null, null).stream()
			.collect(Collectors.toList());

	@Override
	protected Stream<UserDto> fetchFromBackEnd(Query<UserDto, UsersFilter> query) {
		Stream<UserDto> stream = usersData.stream();
		
		// Filtering
        if (query.getFilter().isPresent()) {
            stream = stream.filter(e -> query.getFilter().get().test(e));
        }
        return stream.skip(query.getOffset()).limit(query.getLimit());
	}

	@Override
	protected int sizeInBackEnd(Query<UserDto, UsersFilter> query) {
		   return (int) fetchFromBackEnd(query).count();
	}

}
