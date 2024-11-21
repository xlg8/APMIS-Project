/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.PushResult;
import de.symeda.sormas.api.caze.CriteriaWithSorting;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

/**
 * @see <a href="https://jersey.java.net/documentation/latest/">Jersey documentation</a>
 * @see <a href="https://jersey.java.net/documentation/latest/jaxrs-resources.html#d0e2051">Jersey documentation HTTP Methods</a>
 *
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({
	"USER",
	"REST_USER"})
public class UserResource {

	
	final UserDto userDto = FacadeProvider.getUserFacade().getCurrentUser();
	
	
	@GET
	@Path("/all/{since}")
	public List<UserDto> getAll(@PathParam("since") long since) {
		
		
		List<UserDto> userDtoList = new ArrayList<>();
		userDtoList.add(userDto);
		return userDtoList; //FacadeProvider.getUserFacade().getAllAfter(new Date(since));
	}

	@POST
	@Path("/query")
	public List<UserDto> getByUuids(List<String> uuids) {
		List<UserDto> result = FacadeProvider.getUserFacade().getByUuids(uuids);
		return result;
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		List<String> userDtoList = new ArrayList<>();
		userDtoList.add(userDto.getUuid());
		
		return userDtoList;//FacadeProvider.getUserFacade().getAllUuids();
	}

	@POST
	@Path("/indexList")
	public Page<UserDto> getIndexList(
		@RequestBody CriteriaWithSorting<UserCriteria> criteriaWithSorting,
		@QueryParam("offset") int offset,
		@QueryParam("size") int size) {
		return FacadeProvider.getUserFacade().getIndexPage(criteriaWithSorting.getCriteria(), offset, size, criteriaWithSorting.getSortProperties());
	}
	
//	@POST
//	@Path("/fcm/token/{username}/{token}")
//	public boolean updateFcmToken(@PathParam("username") String username, @PathParam("token") String token) {
//		return FacadeProvider.getUserFacade().updateFcmToken(username, token);
//	}
	
	@POST
	@Path("/push")
	public List<PushResult> postUserFcm(@Valid List<UserDto> dtos) {
		System.out.println("Before e enter passed userdto from mobile to resttttttttttttttttt " + dtos.get(0).getName());
		List<PushResult> resultlist = new ArrayList();
		for (UserDto userDto : dtos) {
			System.out.println("after e enter passed userdto from mobile to resttttttttttttttttt " + userDto.getName());
			FacadeProvider.getUserFacade().saveUserFcmMobile(userDto);
			resultlist.add(PushResult.OK);
		} 			
		return resultlist;
	}
}
