package de.symeda.sormas.backend.sormastosormas.data.infra;

import de.symeda.sormas.api.infrastructure.continent.ContinentDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityReferenceDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityType;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.pointofentry.PointOfEntryDto;
import de.symeda.sormas.api.infrastructure.pointofentry.PointOfEntryReferenceDto;
import de.symeda.sormas.api.infrastructure.subcontinent.SubcontinentReferenceDto;
import de.symeda.sormas.api.location.LocationDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.continent.ContinentReferenceDto;
import de.symeda.sormas.api.infrastructure.country.CountryReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.sormastosormas.validation.ValidationErrorGroup;
import de.symeda.sormas.api.sormastosormas.validation.ValidationErrorMessage;
import de.symeda.sormas.api.sormastosormas.validation.ValidationErrors;
import de.symeda.sormas.backend.infrastructure.facility.FacilityFacadeEjb;
import de.symeda.sormas.backend.infrastructure.pointofentry.PointOfEntryFacadeEjb;
import de.symeda.sormas.backend.infrastructure.community.CommunityFacadeEjb;
import de.symeda.sormas.backend.infrastructure.continent.ContinentFacadeEjb;
import de.symeda.sormas.backend.infrastructure.country.CountryFacadeEjb;
import de.symeda.sormas.backend.infrastructure.district.DistrictFacadeEjb;
import de.symeda.sormas.backend.infrastructure.region.RegionFacadeEjb;
import de.symeda.sormas.backend.infrastructure.subcontinent.SubcontinentFacadeEjb;
import de.symeda.sormas.backend.sample.SampleFacadeEjb;
import de.symeda.sormas.backend.user.UserService;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.Optional;

@Stateless
@LocalBean
public class InfrastructureValidator {

	@EJB
	private UserService userService;
	@EJB
	private ContinentFacadeEjb.ContinentFacadeEjbLocal continentFacade;
	@EJB
	private SubcontinentFacadeEjb.SubcontinentFacadeEjbLocal subcontinentFacade;
	@EJB
	private RegionFacadeEjb.RegionFacadeEjbLocal regionFacade;
	@EJB
	private DistrictFacadeEjb.DistrictFacadeEjbLocal districtFacade;
	@EJB
	private CommunityFacadeEjb.CommunityFacadeEjbLocal communityFacade;
	@EJB
	private FacilityFacadeEjb.FacilityFacadeEjbLocal facilityFacade;
	@EJB
	private PointOfEntryFacadeEjb.PointOfEntryFacadeEjbLocal pointOfEntryFacade;
	@EJB
	private CountryFacadeEjb.CountryFacadeEjbLocal countryFacade;
	@EJB
	private SampleFacadeEjb.SampleFacadeEjbLocal sampleFacade;

// todo this appears to be leaky as it is used in event processor etc as well

	private enum CentralInfra {
		CONTINENT,
		SUB_CONTINENT,
		COUNTRY,
		REGION,
		DISTRICT,
		COMMUNITY
	}

	private <T> T loadFromEtcd(String uuid, CentralInfra type, T clazz) {
		// todo access etcd load JSON and deserialize into object
		return null;
	}

	private ValidationErrors processContinent(ContinentReferenceDto continent, String errorCaption) {
		ValidationErrors validationErrors = new ValidationErrors();
		ContinentReferenceDto localContinent = continentFacade.toReferenceDto(loadFromEtcd(continent.getUuid(), CentralInfra.CONTINENT, ContinentDto.class));
		if (continent != null && localContinent == null) {
			validationErrors
				.add(new ValidationErrorGroup(errorCaption), new ValidationErrorMessage(Validations.sormasToSormasCountry, continent.getCaption()));
		}
		return validationErrors;
	}

	private ValidationErrors processSubcontinent(SubcontinentReferenceDto subcontinent, String groupNameTag) {
		return null;
	}

	public ValidationErrors processCountry(CountryReferenceDto country, String errorCaption) {
		return null;
	}

	public ValidationErrors processRegion(RegionReferenceDto region, String errorCaption) {
		return null;
	}

	public ValidationErrors processDistrict(DistrictReferenceDto district, String errorCaption) {
		return null;
	}

	public ValidationErrors processCommunity(CommunityReferenceDto community, String errorCaption) {
		return null;
	}

	private InfrastructureValidator.WithDetails<FacilityReferenceDto> loadFacility(
		FacilityReferenceDto facility,
		FacilityType facilityType,
		String facilityDetails) {
		String facilityUuid = facility.getUuid();

		if (FacilityDto.CONSTANT_FACILITY_UUIDS.contains(facilityUuid)) {

			FacilityReferenceDto localFacility = facilityDetails != null
				? facilityFacade.getByNameAndType(facilityDetails.trim(), null, null, facilityType, false).stream().findFirst().orElse(null)
				: null;
			if (localFacility == null) {
				localFacility = facilityFacade.getByUuid(facilityUuid).toReference();
			} else {
				facilityDetails = null;
			}

			return WithDetails.of(localFacility, facilityDetails);
		} else {
			Optional<FacilityReferenceDto> localFacility = facility.getExternalId() != null
				? facilityFacade.getByExternalIdAndType(facility.getExternalId(), facilityType, false).stream().findFirst()
				: Optional.empty();

			if (!localFacility.isPresent()) {
				localFacility = facilityFacade.getByNameAndType(facility.getCaption(), null, null, facilityType, false).stream().findFirst();
			}

			final String details;
			if (!localFacility.isPresent()) {
				details = facility.getCaption();
				localFacility = Optional.of(facilityFacade.getByUuid(FacilityDto.OTHER_FACILITY_UUID).toReference());
			} else {
				details = facilityDetails;
			}

			return localFacility.map((f) -> WithDetails.of(f, details)).orElse(null);
		}
	}

	public ValidationErrors processFacility(FacilityReferenceDto facility, FacilityType facilityType, String facilityDetails, String errorCaption) {
		// todo set details correctly
		// call all setters correctly
		ValidationErrors validationErrors = new ValidationErrors();
		WithDetails<FacilityReferenceDto> tmp = loadFacility(facility, facilityType, facilityDetails);
		FacilityReferenceDto localFacility = tmp.entity;
		if (facility != null && localFacility == null) {
			validationErrors
				.add(new ValidationErrorGroup(errorCaption), new ValidationErrorMessage(Validations.sormasToSormasFacility, facility.getCaption()));
		}
		return validationErrors;
	}

	private WithDetails<PointOfEntryReferenceDto> loadPointOfEntry(PointOfEntryReferenceDto pointOfEntry, String pointOfEntryDetails) {
		// todo set details correctly
		// call all setters correctly
		String pointOfEntryUuid = pointOfEntry.getUuid();

		if (PointOfEntryDto.CONSTANT_POE_UUIDS.contains(pointOfEntryUuid)) {
			PointOfEntryReferenceDto localPointOfEntry = pointOfEntryDetails != null
				? pointOfEntryFacade.getByName(pointOfEntryDetails.trim(), null, false).stream().findFirst().orElse(null)
				: null;
			if (localPointOfEntry == null) {
				localPointOfEntry = pointOfEntryFacade.getByUuid(pointOfEntryUuid).toReference();
			} else {
				pointOfEntryDetails = null;
			}

			return WithDetails.of(localPointOfEntry, pointOfEntryDetails);
		} else {

			Optional<PointOfEntryReferenceDto> localPointOfEntry = pointOfEntry.getExternalId() != null
				? pointOfEntryFacade.getByExternalId(pointOfEntry.getExternalId(), false).stream().findFirst()
				: Optional.empty();

			if (!localPointOfEntry.isPresent()) {
				localPointOfEntry = pointOfEntryFacade.getByName(pointOfEntry.getCaption(), null, false).stream().findFirst();
			}

			final String details;
			if (!localPointOfEntry.isPresent()) {
				details = pointOfEntry.getCaption();
				localPointOfEntry = Optional
					.of(pointOfEntryFacade.getByUuid(PointOfEntryDto.getOtherPointOfEntryUuid(pointOfEntry.getPointOfEntryType())).toReference());
			} else {
				details = pointOfEntryDetails;
			}

			return localPointOfEntry.map(p -> WithDetails.of(p, details)).orElse(null);
		}
	}

	public ValidationErrors processPointOfEntry(PointOfEntryReferenceDto pointOfEntry, String pointOfEntryDetails, String errorCaption) {
		ValidationErrors validationErrors = new ValidationErrors();
		WithDetails<PointOfEntryReferenceDto> tmp = loadPointOfEntry(pointOfEntry, pointOfEntryDetails);
		PointOfEntryReferenceDto localPointOfEntry = tmp.entity;
		if (pointOfEntry != null && localPointOfEntry == null) {
			validationErrors.add(
				new ValidationErrorGroup(errorCaption),
				new ValidationErrorMessage(Validations.sormasToSormasPointOfEntry, pointOfEntry.getCaption()));
		}
		return validationErrors;
	}

	public ValidationErrors processLocation(LocationDto address, String groupNameTag) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.addAll(processContinent(address.getContinent(), groupNameTag));
		validationErrors.addAll(processSubcontinent(address.getSubcontinent(), groupNameTag));
		/*
		 * validationErrors.addAll(processCountry());
		 * DataHelper.Pair<InfrastructureData, List<ValidationErrorMessage>> infrastructureAndErrors = processInfrastructure(
		 * address.getContinent(),
		 * address.getSubcontinent(),
		 * address.getCountry(),
		 * address.getRegion(),
		 * address.getDistrict(),
		 * address.getCommunity(),
		 * address.getFacilityType(),
		 * address.getFacility(),
		 * address.getFacilityDetails(),
		 * null,
		 * null);
		 */
		return validationErrors;
	}

	private static final class WithDetails<T> {

		private T entity;
		private String details;

		public static <T> WithDetails<T> of(T facility, String facilityDetails) {
			WithDetails<T> localFacility = new WithDetails<>();

			localFacility.entity = facility;
			localFacility.details = facilityDetails;

			return localFacility;
		}
	}

}
