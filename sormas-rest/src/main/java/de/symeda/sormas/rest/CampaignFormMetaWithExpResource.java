package de.symeda.sormas.rest;

import java.util.Date;
import java.util.List;

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
import de.symeda.sormas.api.campaign.form.CampaignFormMetaExpiryIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaWithExpReferenceDto;

@Path("/campaignFormMetaexp")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({
	"USER",
	"REST_USER" })

public class CampaignFormMetaWithExpResource extends EntityDtoResource {

	@GET
	@Path("/all/{since}")
	public List<CampaignFormMetaExpiryDto> getAllCampaignFormMeta(@PathParam("since") long since) {
		
		System.out.println("Request Hits Form Meta With Expiry  getAllCampaignFormMeta"+ "=====================================00000");

		return FacadeProvider.getCampaignFormMetaFacade().getFormsWithExpiry();
	}

	@POST
	@Path("/query")
	public List<CampaignFormMetaDto> getByUuids(List<String> uuids) {
		

		return FacadeProvider.getCampaignFormMetaFacade().getByUuids(uuids);
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		System.out.println("Request Hits Form Meta With Expiry  getAllUuids"+ "=====================================112222");

		return FacadeProvider.getCampaignFormMetaFacade().getAllUuids();
	}
	
	@GET
	@Path("/formswithexp")
	public List<CampaignFormMetaExpiryDto> getAllFormsWithExpiry() {
		System.out.println("Request Hits Form Meta With Expiry  getAllFormsWithExpiry ==========================111111111"+ FacadeProvider.getCampaignFormMetaFacade().getFormsWithExpiry().size());
		return FacadeProvider.getCampaignFormMetaFacade().getFormsWithExpiry();
	}
}
