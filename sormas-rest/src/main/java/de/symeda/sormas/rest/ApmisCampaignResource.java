package de.symeda.sormas.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.opencsv.CSVWriter;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignAggregateDataDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.report.CampaignDataExtractDto;

@Path("/apmisrestserver")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
//@Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
@RolesAllowed({ "USER", "REST_USER", })
public class ApmisCampaignResource {// extends EntityDtoResource {

	@GET
	@Path("/campaigns")
	public List<CampaignReferenceDto> getAllCampaigns() {
		List<CampaignReferenceDto> cdto = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
		return cdto;
	}

	@GET
	@Path("/uuidsx")
	public List<String> getAllUuids() {
		List<CampaignReferenceDto> cdto = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();// .getAllActive();

		return FacadeProvider.getAreaFacade().getAllUuids();
	}

	@GET
	@Path("/campaigns/{uuid}") // return a campaign by its UUID
	public CampaignDto getByUuid(@PathParam("uuid") String uuid) {
		return FacadeProvider.getCampaignFacade().getByUuid(uuid);
	}

	@GET
	@Path("/campaigns/{uuid}/forms") // return a campaign's forms //next: use the uuid of the form to get out all the
										// data associated with that form and the campaign
	public Set<CampaignFormMetaReferenceDto> getCampaignForms(@PathParam("uuid") String uuid) {
		return FacadeProvider.getCampaignFacade().getByUuid(uuid).getCampaignFormMetas();
	}

	@GET
	@Path("/{campaigns_uuid}/{forms_uuid}/json")
	public List<CampaignDataExtractDto> getCampaignFormData(@PathParam("campaigns_uuid") String campaign_uuid,
			@PathParam("forms_uuid") String form_uuid) {
		List<CampaignDataExtractDto> lstdto = FacadeProvider.getCampaignFormDataFacade()
				.getCampaignFormDataExtractApi(campaign_uuid, form_uuid);
		return lstdto;
	}

	@GET
	@Path("/{campaigns_uuid}/{forms_uuid}/csv")
	@Produces("text/csv")
	public Response getCampaignFormDataCSV(@PathParam("campaigns_uuid") String campaign_uuid,
			@PathParam("forms_uuid") String form_uuid) {
		List<CampaignDataExtractDto> lstdto = FacadeProvider.getCampaignFormDataFacade()
				.getCampaignFormDataExtractApi(campaign_uuid, form_uuid);
		String csv = toExtractCsv(lstdto);

		return Response.ok(csv).header("Content-Disposition", "attachment; filename=Extractdata.csv").build();
	}

	private String toExtractCsv(List<CampaignDataExtractDto> data) {

		StringWriter stringWriter = new StringWriter();

		try (CSVWriter writer = new CSVWriter(stringWriter)) {

			// Define the header row
			// String formUuid, String formId, String formField, String formCaption, String
			// area,String region, String district, Long sumValue

			String[] header = { "Year", "Campaign", "Region", "Province", "District", "Cluster", "ClusterNumber", "Form", "FieldId", "Value" };
			writer.writeNext(header);

			// Add the data rows
			for (CampaignDataExtractDto formData : data) {
				String[] row = {formData.getCampaignyear(), formData.getCampaign(), formData.getArea(), formData.getRegion(), formData.getDistrict(), formData.getCummunity(), 
						formData.getClusternumber()+"", formData.getFormname(), formData.getKey(), formData.getValue()
						};
				writer.writeNext(row);
			}

			// Flush the writer and return the CSV data
			writer.flush();
			return stringWriter.toString();

		} catch (IOException e) {
			throw new RuntimeException("Error converting Extractdata to CSV", e);
		}
	}

	@GET
	@Path("/aggregate/{campaigns_uuid}")
	@Produces("text/csv")
	public Response getDataAsCsv(@PathParam("campaigns_uuid") String campaign_uuid) {
		System.err.println("connecting to db");
		List<CampaignAggregateDataDto> resultsCSV = FacadeProvider.getCampaignFormDataFacade()
				.getCampaignFormDataAggregatetoCSV(campaign_uuid);
		System.err.println("runing result to CSV");
		String csv = toCsv(resultsCSV);

		return Response.ok(csv).header("Content-Disposition", "attachment; filename=data.csv").build();
	}

	private String toCsv(List<CampaignAggregateDataDto> data) {

		StringWriter stringWriter = new StringWriter();

		try (CSVWriter writer = new CSVWriter(stringWriter)) {

			// Define the header row
			// String formUuid, String formId, String formField, String formCaption, String
			// area,String region, String district, Long sumValue

			String[] header = { "FormUUID", "FormID", "FieldName", "Province", "Region", "District", "Aggregate" };
			writer.writeNext(header);

			// Add the data rows
			for (CampaignAggregateDataDto formData : data) {
				String[] row = { formData.getFormUuid(), formData.getFormId(), formData.getFormCaption(),
						formData.getArea(), formData.getRegion(), formData.getDistrict(),
						formData.getSumValue().toString() };
				writer.writeNext(row);
			}

			// Flush the writer and return the CSV data
			writer.flush();
			return stringWriter.toString();

		} catch (IOException e) {
			throw new RuntimeException("Error converting data to CSV", e);
		}
	}

}
