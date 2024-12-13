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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
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
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityHistoryExtractDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionHistoryExtractDto;
import de.symeda.sormas.api.user.UserDto;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@Path("/communities")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({ "USER", "REST_USER" })
public class CommunityResource {

	@GET
	@Path("/all/{since}")
	public List<CommunityDto> getAll(@PathParam("since") long since) {
		final Set<CommunityReferenceDto> rdto = FacadeProvider.getUserFacade().getCurrentUser().getCommunity();

		System.out.println((rdto != null) + "List<CommunityDto> getAll(zdsvxxxxxxxxxxxxxxxxxxx" + rdto.size());
		if (rdto != null && rdto.size() > 0) {
			System.out.println("rdtordtordto != null :zdsvxxxxxxxxxxxxxxxxxxx");
			List<CommunityDto> returnList = new ArrayList<>();

			for (CommunityReferenceDto lcs : rdto) {
				returnList.add(FacadeProvider.getCommunityFacade().getByUuid(lcs.getUuid()));
			}

			System.out.println("returnListvxxxxxxxxxxxxxxxxxxx" + returnList.size());

			return returnList;// FacadeProvider.getCommunityFacade().getAllAfter(new Date(since)).stream()
//				.filter(e -> rdto.stream().anyMatch(ee -> e.getUuid().equals(ee.getUuid()))).collect(Collectors.toList());
		} else {
			System.out.println("else :zdsvxxxxxxxxxxxxxxxxxxx");
			final Set<DistrictReferenceDto> rDistdto = FacadeProvider.getUserFacade().getCurrentUser().getDistricts();
			System.out
					.println(rDistdto + " :zdsvxxxxxxxxxxxxxxxxxxxx: " + rDistdto.size() + " :+_+_+_+_+_+_zzzzzzzzzzz");
			if (rDistdto != null & rDistdto.size() > 0) {
				return FacadeProvider.getCommunityFacade().getAllAfterWithDistrict(new Date(since), null);

			} else {
				System.out.println("eCOMMUNITY RETUNRING NULLL CHECK xxxxxxxxxxxxxxxx");
				return null;
			}
		}
//		
	}

	@GET
	@Path("/allClusters/{since}")
	public List<CommunityDto> getAllClusters(@PathParam("since") long since) {

		final Set<CommunityReferenceDto> rdto = FacadeProvider.getUserFacade().getCurrentUser().getCommunity();
		if (rdto.size() == 0) {
			return FacadeProvider.getCommunityFacade().getAllAfter(new Date(since));
		} else {
			return getAll(since);
		}
	}

	@POST
	@Path("/query")
	public List<CommunityDto> getByUuids(List<String> uuids) {
		List<CommunityDto> result = FacadeProvider.getCommunityFacade().getByUuids(uuids);
		return result;
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		final Set<CommunityReferenceDto> rdto = FacadeProvider.getUserFacade().getCurrentUser().getCommunity();

		System.out.println(" :zdsvxxxxxx++++ size-0: ");
		final Set<DistrictReferenceDto> rDistdto = FacadeProvider.getUserFacade().getCurrentUser().getDistricts();

		// todo: need to device smarter way of filtering out archived and deleted uuids
		// as we are collecting this from user table
		if (rdto != null && rdto.size() > 0) {
			System.out.println(" :zdsvxxxxxx++++ size-1: " + rdto.size());
			List<String> lstUuid = new ArrayList<>();

			for (CommunityReferenceDto com : rdto) {
				System.out.println(com.getUuid() + " :zdsvxxxxxx++++ size-2: " + com.getCaption());
				lstUuid.add(com.getUuid());
			}

			return lstUuid;
		} else if (rDistdto != null & rDistdto.size() > 0) {
			List<CommunityDto> comx = FacadeProvider.getCommunityFacade().getAllAfterWithDistrict(new Date(0), null);
			List<String> lstUuidx = new ArrayList<>();
			for (CommunityDto com : comx) {
				lstUuidx.add(com.getUuid());
			}

			System.out.println(rDistdto + " :zdsvxxxxxx++++ size = " + lstUuidx.size() + " xxxxxxxxxxxxxx: "
					+ rDistdto.size() + " :+_+_+_+_+_+_zzzzzzzzzzz");
			return lstUuidx;

		} else {
			return null;
		}
		// return FacadeProvider.getCommunityFacade().getAllUuids();
	}

	@POST
	@Path("/indexList")
	public Page<CommunityDto> getIndexList(@RequestBody CriteriaWithSorting<CommunityCriteriaNew> criteriaWithSorting,
			@QueryParam("offset") int offset, @QueryParam("size") int size) {
		return FacadeProvider.getCommunityFacade().getIndexPage(criteriaWithSorting.getCriteria(), offset, size,
				criteriaWithSorting.getSortProperties());
	}
	
	
	@GET
	@Path("/clustersHistory")
	public List<CommunityHistoryExtractDto> getClustersHistory(@QueryParam("getClusterHistory") String uuid) {
		System.out.println(uuid + "UUUID from province resource");
		return FacadeProvider.getCommunityFacade().getClustersHistory(uuid);
	}
}
