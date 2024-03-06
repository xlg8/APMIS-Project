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
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CriteriaWithSorting;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

/**
 * @see <a href="https://jersey.java.net/documentation/latest/">Jersey documentation</a>
 * @see <a href="https://jersey.java.net/documentation/latest/jaxrs-resources.html#d0e2051">Jersey documentation HTTP Methods</a>
 *
 */
@Path("/districts")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({
	"USER",
	"REST_USER" })
public class DistrictResource {
	@GET
	@Path("/all/{since}")
	public List<DistrictDto> getAll(@PathParam("since") long since) {
		final Set<DistrictReferenceDto> rdto = FacadeProvider.getUserFacade().getCurrentUser().getDistricts();
		final DistrictReferenceDto rdtox = FacadeProvider.getUserFacade().getCurrentUser().getDistrict();
		
//		System.out.println(rdto.size()+" : List<DistrictDto> getAll(@PathParam(\"since\") long since) +++++++++++++++++++++++++++++++++++++");
		
		if(rdto != null && rdto.size() > 0) {
			
//			System.out.println(FacadeProvider.getDistrictFacade().getAllAfter(new Date(since)).stream()
//					.filter(e -> rdto.stream().anyMatch(ee -> e.getUuid().equals(ee.getUuid()))).collect(Collectors.toList()).size()+"hhhhhhhhhhhh+++ ");
			return FacadeProvider.getDistrictFacade().getAllAfter(new Date(since)).stream()
					.filter(e -> rdto.stream().anyMatch(ee -> e.getUuid().equals(ee.getUuid()))).collect(Collectors.toList());
		} else {
			List<DistrictDto> retList = new ArrayList<>();
			
			retList.add(FacadeProvider.getDistrictFacade().getByUuid(rdtox.getUuid()));
			
			return retList;
		}
		
	}
	

	@POST
	@Path("/query")
	public List<DistrictDto> getByUuids(List<String> uuids) {

		List<DistrictDto> result = FacadeProvider.getDistrictFacade().getByUuids(uuids);
		return result;
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		final Set<DistrictReferenceDto> rdto = FacadeProvider.getUserFacade().getCurrentUser().getDistricts();
		final DistrictReferenceDto rdtox = FacadeProvider.getUserFacade().getCurrentUser().getDistrict();
		
//		System.out.println("public List<String> getAllUuids() : +++++++++++++++++++++++++++++++++++++");
		if(rdto != null && rdto.size() > 0) {
//			System.out.println(FacadeProvider.getDistrictFacade().getAllUuids().stream()
//					.filter(e -> rdto.stream().anyMatch(ee -> e.equals(ee.getUuid()))).collect(Collectors.toList()).size()+" :public List<Strifffffffuids() : +++++++++++++++++++++++++++++++++++++");
			return FacadeProvider.getDistrictFacade().getAllUuids().stream()
					.filter(e -> rdto.stream().anyMatch(ee -> e.equals(ee.getUuid()))).collect(Collectors.toList());
		} else {
			List<String> retListx = new ArrayList<>();
			
			retListx.add(rdtox.getUuid());
			
			return retListx;
		
		}
		
//		
//		System.out.println("public List<String> getAllUuids() : +++++++++++++++++++++++++++++++++++++");
//		return FacadeProvider.getDistrictFacade().getAllUuids();
	}

	@POST
	@Path("/indexList")
	public Page<DistrictIndexDto> getIndexList(
		@RequestBody CriteriaWithSorting<DistrictCriteria> criteriaWithSorting,
		@QueryParam("offset") int offset,
		@QueryParam("size") int size) {
		return FacadeProvider.getDistrictFacade()
			.getIndexPage(criteriaWithSorting.getCriteria(), offset, size, criteriaWithSorting.getSortProperties());
	}
}
