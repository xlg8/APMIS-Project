package de.symeda.sormas.rest;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaHistoryExtractDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;

@Path("/areas")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({
	"USER",
	"REST_USER" })
public class AreaResource {

	@GET
	@Path("/all/{since}")
	public List<AreaDto> getAll(@PathParam("since") long since) {
		return FacadeProvider.getAreaFacade().getAllAfter(new Date(since));
	}

	@POST
	@Path("/query")
	public List<AreaDto> getByUuids(List<String> uuids) {
		List<AreaDto> result = FacadeProvider.getAreaFacade().getByUuids(uuids);
		return result;
	}

	@GET
	@Path("/uuids")
	public List<String> getAllUuids() {
		return FacadeProvider.getAreaFacade().getAllUuids();
	}
	
	@GET
	@Path("/areaHistory")
	public List<AreaHistoryExtractDto> getAreasHistory(@QueryParam("getAreaHistory") String uuid) {
		System.out.println(uuid + "UUUID from area resource");
		return FacadeProvider.getAreaFacade().getAreasHistory(uuid);
	}
}
