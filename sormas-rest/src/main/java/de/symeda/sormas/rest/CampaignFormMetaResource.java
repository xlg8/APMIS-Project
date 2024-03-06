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

@Path("/campaignFormMeta")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({
	"USER",
	"REST_USER" })

public class CampaignFormMetaResource extends EntityDtoResource {

	@GET
	@Path("/all/{since}")
	public List<CampaignFormMetaDto> getAllCampaignFormMeta(@PathParam("since") long since) {
		return FacadeProvider.getCampaignFormMetaFacade().getAllAfter(new Date(since));
	}

	@POST
	@Path("/query")
	public List<CampaignFormMetaDto> getByUuids(List<String> uuids) {
		return FacadeProvider.getCampaignFormMetaFacade().getByUuids(uuids);
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		return FacadeProvider.getCampaignFormMetaFacade().getAllUuids();
	}
	
	@GET
	@Path("/formswithexp")
	public List<CampaignFormMetaExpiryDto> getAllFormsWithExpiry() {
		System.out.println("Request Hits Form Meta With Expiry "+ "=====================================11");
		return FacadeProvider.getCampaignFormMetaFacade().getFormsWithExpiry();
	}
}
