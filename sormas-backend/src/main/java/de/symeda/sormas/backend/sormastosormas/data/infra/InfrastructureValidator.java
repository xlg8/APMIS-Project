package de.symeda.sormas.backend.sormastosormas.data.infra;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.google.protobuf.ByteString;
import com.ibm.etcd.api.KeyValue;
import com.ibm.etcd.client.EtcdClient;
import com.ibm.etcd.client.KvStoreClient;
import com.ibm.etcd.client.kv.KvClient;
import de.symeda.sormas.api.InfrastructureDataReferenceDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
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
import de.symeda.sormas.api.sormastosormas.SormasServerDescriptor;
import de.symeda.sormas.api.sormastosormas.SormasToSormasConfig;
import de.symeda.sormas.api.sormastosormas.validation.ValidationErrorGroup;
import de.symeda.sormas.api.sormastosormas.validation.ValidationErrorMessage;
import de.symeda.sormas.api.sormastosormas.validation.ValidationErrors;
import de.symeda.sormas.backend.common.ConfigFacadeEjb;
import de.symeda.sormas.backend.common.InfrastructureAdo;
import de.symeda.sormas.backend.infrastructure.community.Community;
import de.symeda.sormas.backend.infrastructure.continent.Continent;
import de.symeda.sormas.backend.infrastructure.country.Country;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.facility.FacilityFacadeEjb;
import de.symeda.sormas.backend.infrastructure.pointofentry.PointOfEntryFacadeEjb;
import de.symeda.sormas.backend.infrastructure.community.CommunityFacadeEjb;
import de.symeda.sormas.backend.infrastructure.continent.ContinentFacadeEjb;
import de.symeda.sormas.backend.infrastructure.country.CountryFacadeEjb;
import de.symeda.sormas.backend.infrastructure.district.DistrictFacadeEjb;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.infrastructure.region.RegionFacadeEjb;
import de.symeda.sormas.backend.infrastructure.subcontinent.Subcontinent;
import de.symeda.sormas.backend.infrastructure.subcontinent.SubcontinentFacadeEjb;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.sample.SampleFacadeEjb;
import de.symeda.sormas.backend.sormastosormas.access.SormasToSormasDiscoveryService;
import de.symeda.sormas.backend.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Locale;
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
	@EJB
	private ConfigFacadeEjb.ConfigFacadeEjbLocal configFacadeEjb;

	private static final Logger LOGGER = LoggerFactory.getLogger(SormasToSormasDiscoveryService.class);

	public enum CentralInfra {
		CONTINENT,
		SUB_CONTINENT,
		COUNTRY,
		REGION,
		DISTRICT,
		COMMUNITY
	}

	private KvStoreClient createEtcdClient() throws IOException {
		String[] hostPort = configFacadeEjb.getCentralEtcdHost().split(":");
		SormasToSormasConfig sormasToSormasConfig = configFacadeEjb.getS2SConfig();

		URL truststorePath;
		try {
			truststorePath = Paths.get(configFacadeEjb.getCentralEtcdCaPath()).toUri().toURL();
		} catch (MalformedURLException e) {
			LOGGER.error("Etcd Url is malformed: %s", e);
			throw e;
		}

		KvStoreClient client;
		try {
			client = EtcdClient.forEndpoint(hostPort[0], Integer.parseInt(hostPort[1]))
				.withCredentials(sormasToSormasConfig.getEtcdClientName(), sormasToSormasConfig.getEtcdClientPassword())
				.withCaCert(Resources.asByteSource(truststorePath))
				.build();
		} catch (IOException e) {
			LOGGER.error("Could not load Etcd CA cert: %s", e);
			throw e;
		}

		LOGGER.info("Etcd client created successfully.");
		return client;
	}

	private <T> T loadFromEtcd(String uuid, Class<T> clazz) {
		// use resource auto-close
		try (KvStoreClient etcdClient = createEtcdClient()) {
			KvClient etcd = etcdClient.getKvClient();

			if (etcd == null) {
				//LOGGER.error((I18nProperties.getString(Strings.errorSormasToSormasServerAccess)));
				return null;
			}

			String key = String.format("/central/location/%s/%s", clazz.getSimpleName().toLowerCase(Locale.ROOT), uuid);
			KeyValue result = etcd.get(ByteString.copyFromUtf8(key)).sync().getKvsList().get(0);

			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
			mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
			try {
				return mapper.readValue(result.getValue().toStringUtf8(), clazz);
			} catch (JsonProcessingException e) {
				LOGGER.error("Could not serialize location object.");
				return null;
			}

		} catch (Exception e) {
			LOGGER.error((I18nProperties.getString(Strings.errorSormasToSormasServerAccess)));
			LOGGER.error("Unexpected error while reading SormasServerDescriptor data.", e);
			return null;
		}
	}

	public ValidationErrors processInfrastructure(CentralInfra type, InfrastructureDataReferenceDto referenceDto, String errorCaption) {
		ValidationErrors validationErrors = new ValidationErrors();
		InfrastructureAdo loadedInfra = null;
		String errorMessage = "";
		switch (type) {

		case CONTINENT:
			loadedInfra = loadFromEtcd(referenceDto.getUuid(), Continent.class);
			errorMessage = ((Continent) loadedInfra).getDefaultName();
			break;
		case SUB_CONTINENT:
			loadedInfra = loadFromEtcd(referenceDto.getUuid(), Subcontinent.class);
			errorMessage = ((Subcontinent) loadedInfra).getDefaultName();
			break;
		case COUNTRY:
			loadedInfra = loadFromEtcd(referenceDto.getUuid(), Country.class);
			errorMessage = ((Country) loadedInfra).getDefaultName();
			break;
		case REGION:
			loadedInfra = loadFromEtcd(referenceDto.getUuid(), Region.class);
			errorMessage = ((Region) loadedInfra).getName();
			break;
		case DISTRICT:
			loadedInfra = loadFromEtcd(referenceDto.getUuid(), District.class);
			errorMessage = ((District) loadedInfra).getName();
			break;
		case COMMUNITY:
			loadedInfra = loadFromEtcd(referenceDto.getUuid(), Community.class);
			errorMessage = ((Community) loadedInfra).getName();
			break;
		default:
			throw new IllegalStateException("Unexpected value: " + type);
		}

		// todo equality check missing
		if (referenceDto != null && loadedInfra == null) {
			validationErrors.add(new ValidationErrorGroup(errorCaption), new ValidationErrorMessage(Validations.sormasToSormasCountry, errorMessage));
		}
		return validationErrors;
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

	public ValidationErrors processLocation(LocationDto location, String groupNameTag) {
		ValidationErrors validationErrors = new ValidationErrors();
		validationErrors.addAll(processInfrastructure(CentralInfra.CONTINENT, location.getContinent(), groupNameTag));
		validationErrors.addAll(processInfrastructure(CentralInfra.SUB_CONTINENT, location.getSubcontinent(), groupNameTag));
		validationErrors.addAll(processInfrastructure(CentralInfra.COUNTRY, location.getCountry(), groupNameTag));
		validationErrors.addAll(processInfrastructure(CentralInfra.REGION, location.getRegion(), groupNameTag));
		validationErrors.addAll(processInfrastructure(CentralInfra.DISTRICT, location.getDistrict(), groupNameTag));
		validationErrors.addAll(processInfrastructure(CentralInfra.COMMUNITY, location.getCommunity(), groupNameTag));
		validationErrors.addAll(processFacility(location.getFacility(), location.getFacilityType(), location.getFacilityDetails(), groupNameTag));

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
