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
import javax.ws.rs.core.MediaType;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaExpiryDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;

@Path("/campaignFormMeta")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({
	"USER",
	"REST_USER" })

public class CampaignFormMetaResource extends EntityDtoResource {

	@GET
	@Path("/all/{since}")
	public List<CampaignFormMetaDto> getAllCampaignFormMeta(@PathParam("since") long since) {
		final Set<DistrictReferenceDto> rdto = FacadeProvider.getUserFacade().getCurrentUser().getDistricts();
		
			
		if(rdto != null && rdto.size() > 0) {
//			System.out.println("+++++++++++++++++++++++form size"+FacadeProvider.getCampaignFormMetaFacade().getAllAfter(new Date(since)).stream()
//					.filter(e -> e.isDistrictentry() == true).collect(Collectors.toList()).size());
			return FacadeProvider.getCampaignFormMetaFacade().getAllAfter(new Date(since)).stream()
					.filter(e -> e.isDistrictentry() == true).collect(Collectors.toList());
			
		} else {
//			System.out.println("+++++++++++++++++++++++form size"+FacadeProvider.getCampaignFormMetaFacade().getAllAfter(new Date(since)).stream()
//					.filter(e -> e.isDistrictentry() != true).collect(Collectors.toList()).size());
			return FacadeProvider.getCampaignFormMetaFacade().getAllAfter(new Date(since)).stream()
					.filter(e -> e.isDistrictentry() != true).collect(Collectors.toList());
		}
	}

	@POST
	@Path("/query")
	public List<CampaignFormMetaDto> getByUuids(List<String> uuids) {
		return FacadeProvider.getCampaignFormMetaFacade().getByUuids(uuids);
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		final Set<DistrictReferenceDto> rdto = FacadeProvider.getUserFacade().getCurrentUser().getDistricts();
		
		
		if(rdto != null && rdto.size() > 0) {
			List<CampaignFormMetaDto> listArra = FacadeProvider.getCampaignFormMetaFacade().getAllAfter(new Date(0)).stream()
					.filter(e -> e.isDistrictentry() == true).collect(Collectors.toList());
			List<String> finalList = new ArrayList<>();
//			System.out.println("+++++++++++++++++++++++form size"+listArra.size());
			for(CampaignFormMetaDto lsc : listArra ) {
				finalList.add(lsc.getUuid());
			}
			return finalList;
			
		} else {
			List<CampaignFormMetaDto> listArra = FacadeProvider.getCampaignFormMetaFacade().getAllAfter(new Date(0)).stream()
					.filter(e -> e.isDistrictentry() != true).collect(Collectors.toList());
			List<String> finalList = new ArrayList<>();
			for(CampaignFormMetaDto lsc : listArra ) {
				finalList.add(lsc.getUuid());
			}
			return finalList;
		}
		
//		return FacadeProvider.getCampaignFormMetaFacade().getAllUuids();
	}
	
	@GET
	@Path("/formswithexp")
	public List<CampaignFormMetaExpiryDto> getAllFormsWithExpiry() {
		return FacadeProvider.getCampaignFormMetaFacade().getFormsWithExpiry();
	}
}
