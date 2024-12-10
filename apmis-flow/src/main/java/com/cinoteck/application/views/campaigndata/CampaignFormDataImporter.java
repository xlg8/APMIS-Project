package com.cinoteck.application.views.campaigndata;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cinoteck.application.views.utils.importutils.DataImporter;
import com.cinoteck.application.views.utils.importutils.ImportErrorException;
import com.cinoteck.application.views.utils.importutils.ImportLineResult;
import com.opencsv.exceptions.CsvValidationException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dialog.Dialog;
//import com.vaadin.server.Sizeable;
//import com.vaadin.server.StreamResource;
//import com.vaadin.ui.UI;
//import com.vaadin.ui.Window;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.importexport.ImportLineResultDto;
import de.symeda.sormas.api.importexport.InvalidColumnException;
import de.symeda.sormas.api.importexport.ValueSeparator;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserFacade;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.ValidationRuntimeException;

/**
 * Data importer that is used to import population data.
 */
public class CampaignFormDataImporter extends DataImporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CampaignFormDataImporter.class);

	private final String campaignFormMetaUuid;
	private final CampaignReferenceDto campaignReferenceDto;
	CampaignDto campaignDto;
	AreaDto dto = new AreaDto();
	private final UserFacade userFacade;
	private UI currentUI;
	List<Long> externalIdList = new ArrayList<>();
	List<Long> regionexternalIdList = new ArrayList<>();
	List<Long> districtexternalIdList = new ArrayList<>();

	private static final String R_CODE = "RCode";
	private static final String P_CODE = "PCode";
	private static final String D_CODE = "DCode";
	private static final String C_CODE = "CCode";
	String selectedAreaUUid = "";
	String selectedRegionUUid = "";
	String selectedDistrictUUid = "";
	boolean areaExists = false;
	boolean regionExists = false;
	boolean districtExists = false;
	
	File file;

	// file_, true, userDto, campaignForm.getUuid(), campaignReferenceDto,
	// ValueSeparator.COMMA
	public CampaignFormDataImporter(File inputFile, boolean hasEntityClassRow, UserDto currentUser,
			String campaignFormMetaUuid, CampaignReferenceDto campaignReferenceDto, CampaignDto campaignDto,
			ValueSeparator csvSeparator) throws IOException {
		super(inputFile, hasEntityClassRow, currentUser, csvSeparator);
		this.campaignFormMetaUuid = campaignFormMetaUuid;
		this.campaignReferenceDto = campaignReferenceDto;
		this.campaignDto = campaignDto;
		this.userFacade = FacadeProvider.getUserFacade();
	}
	

	@Override
	public void startImport(File file, Consumer<StreamResource> addErrorReportToLayoutCallback, Consumer<StreamResource> notused,
			boolean notUsed, UI currentUI, boolean duplicatesPossible) throws IOException, CsvValidationException {
		this.file=file;
		this.currentUI = currentUI;
		super.startImport(file, addErrorReportToLayoutCallback, notused, false, currentUI, duplicatesPossible);
	}

	@Override
	protected ImportLineResult importDataFromCsvLine(String[] values, String[] entityClasses, String[] entityProperties,
			String[][] entityPropertyPaths, boolean firstLine) throws IOException, InterruptedException {

		if (values.length > entityProperties.length) {
			writeImportError(values, I18nProperties.getValidationError(Validations.importLineTooLong));
			return ImportLineResult.ERROR;
		}
		CampaignFormDataDto campaignFormData = CampaignFormDataDto.build();
		logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX -------------------------------------");
		campaignFormData.setCreatingUser(userFacade.getCurrentUserAsReference());
		campaignFormData.setSource("IMPORT");

		Long area_xt_id = null;
		Long region_xt_id = null;
		Long district_xt_id = null;

		for (int i = 0; i < entityProperties.length; i++) {
			logger.debug(
					entityProperties + "entity propertiesssss" + entityProperties[1] + entityProperties[i].toString());
			if (R_CODE.equalsIgnoreCase(entityProperties[i])) {
				areaExists = false;

				List<AreaReferenceDto> selectedAreaInCampaignx = FacadeProvider.getAreaFacade()
						.getAllActiveAndSelectedAsReference(campaignDto.getUuid());
//				logger.debug("selectedAreaInCampaignx -------------------------------------------" +selectedAreaInCampaignx);
				area_xt_id = Long.parseLong(values[i]);
				for (AreaReferenceDto areaExId : selectedAreaInCampaignx) {
					AreaDto selectedAreasFromDto = FacadeProvider.getAreaFacade().getByUuid(areaExId.getUuid());
					Long selectedAreaId = selectedAreasFromDto.getExternalId();
					externalIdList.add(selectedAreaId);
					if (selectedAreaId.equals(area_xt_id)) {
						selectedAreaUUid = areaExId.getUuid();
						campaignFormData.setArea(areaExId);
					}
				}

				for (Long item : externalIdList) {
					if (item.equals(area_xt_id)) {
						areaExists = true;
						logger.debug("selectedAreaInCampaignx ----------------------------");
						break;
					}
				}

				if (!areaExists) {
					logger.debug(area_xt_id + " does not exist in the list.");
					writeImportError(values, I18nProperties.getCaption(Captions.areaNotExistInCampaignImportError));
					return ImportLineResult.ERROR;
				}

			}

			logger.debug(" cheking area " + areaExists);
			if (P_CODE.equalsIgnoreCase(entityProperties[i]) && areaExists) {
				logger.debug("there is rcode -------------------------------------------");

				List<RegionReferenceDto> selectedAreaInCampaignxy = FacadeProvider.getRegionFacade()
						.getAllActiveByAreaAndSelectedInCampaign(selectedAreaUUid, campaignDto.getUuid());
				region_xt_id = Long.parseLong(values[i]);
				for (RegionReferenceDto regExId : selectedAreaInCampaignxy) {
					RegionDto selectedRegionsFromDto = FacadeProvider.getRegionFacade().getByUuid(regExId.getUuid());
					Long selectedRegionId = selectedRegionsFromDto.getExternalId();
					regionexternalIdList.add(selectedRegionId);
					if (selectedRegionId.equals(region_xt_id)) {
						selectedRegionUUid = regExId.getUuid();
						campaignFormData.setRegion(regExId);
					}

				}

				for (Long item : regionexternalIdList) {
					if (item.equals(region_xt_id)) {
						regionExists = true;
						break;
					}
				}

				logger.debug(region_xt_id + "selected provinces  -------------------------------------------"
						+ selectedAreaInCampaignxy);
				if (!regionExists) {
					logger.debug(
							regionExists + "  does the region existtttttttttttttttttttttttttttttttttttttttttttttt");
					logger.debug(area_xt_id + " does not exist in the list.");
					writeImportError(values, I18nProperties.getCaption(Captions.regionNotExistInCampaignImportError));
					return ImportLineResult.ERROR;
				}
			}
			
			if (D_CODE.equalsIgnoreCase(entityProperties[i]) && regionExists) {
				logger.debug("there is dcode -------------------------------------------" + regionExists);

				List<DistrictReferenceDto> selectedDistrictInCampaignxy = FacadeProvider.getDistrictFacade()
						.getAllActiveByRegionAndSelectedInCampaign(selectedRegionUUid, campaignDto.getUuid());
				
				logger.debug( "selected district list  -------------------------------------------" + selectedDistrictInCampaignxy);
				district_xt_id = Long.parseLong(values[i]);
				
				for (DistrictReferenceDto districtExId : selectedDistrictInCampaignxy) {
					DistrictDto selectedDistrictsFromDto = FacadeProvider.getDistrictFacade().getByUuid(districtExId.getUuid());
					Long selectedDistrictId = selectedDistrictsFromDto.getExternalId();
					districtexternalIdList.add(selectedDistrictId);
					
					if (selectedDistrictId.equals(district_xt_id)) {
						selectedDistrictUUid = districtExId.getUuid();
						campaignFormData.setDistrict(districtExId);
					}

				}

				for (Long item : districtexternalIdList) {
					if (item.equals(district_xt_id)) {
						districtExists = true;
						break;
					}
				}

				logger.debug(region_xt_id + "selected districts  -------------------------------------------"
						+ selectedDistrictInCampaignxy);
				if (!districtExists) {
					logger.debug(
							districtExists + "  does the district existtttttttttttttttttttttttttttttttttttttttttttttt");
					logger.debug(district_xt_id + " does not exist in the list.");
					writeImportError(values, I18nProperties.getCaption("District is not selected for this campaign"));
					return ImportLineResult.ERROR;
				} 
			}
			
			if (C_CODE.equalsIgnoreCase(entityProperties[i]) && districtExists) {
			try {
				List<CommunityReferenceDto> community = FacadeProvider.getCommunityFacade()
						.getByExternalID(Long.parseLong(values[i]), campaignFormData.getDistrict(), true);
				if (community.isEmpty()) {
					throw new ImportErrorException(I18nProperties.getValidationError(
							Validations.importEntryDoesNotExistDbOrDistrict, values[i], entityProperties[i]));
				} else if (community.size() > 1) {
					throw new ImportErrorException(I18nProperties.getValidationError(
							Validations.importCommunityNotUnique, values[i], entityProperties[i]));
				} else {
					campaignFormData.setCommunity(community.get(0));


					try {
						campaignFormData = insertImportRowIntoData(campaignFormData, values, entityProperties);
						
						campaignFormData.setCampaign(campaignReferenceDto);

//						CampaignFormDataDto existingData = FacadeProvider.getCampaignFormDataFacade()
//								.getExistingData(new CampaignFormDataCriteria().campaign(campaignFormData.getCampaign())
//										.campaignFormMeta(campaignFormData.getCampaignFormMeta())
//										.community((CommunityReferenceDto) campaignFormData.getCommunity())
//										.formDate(campaignFormData.getFormDate()));
						logger.debug("1111111"+campaignFormData.getDistrict());
						logger.debug("1111112"+campaignFormData.getRegion());
						logger.debug("1111113"+campaignFormData.getCommunity());
						logger.debug("1111114"+campaignFormData.getFormDate());
						logger.debug("1111115"+campaignFormData.getArea());
						logger.debug("1111116"+campaignFormData.getCampaignFormMeta());
						logger.debug("1111116"+campaignFormData.getCampaign());
						
						FacadeProvider.getCampaignFormDataFacade().saveCampaignFormData(campaignFormData);
					} catch (ImportErrorException | InvalidColumnException | ValidationRuntimeException e) {
						logger.debug(e.getLocalizedMessage()+ "ddddddddddddddddddddddd: "+e.getMessage());
						writeImportError(values, e.getLocalizedMessage());
						return ImportLineResult.ERROR;
					}
					return ImportLineResult.SUCCESS;
				
				
				
				}
			} catch(ImportErrorException e) {
				logger.debug(e.getLocalizedMessage()+ "ddddddddddddddddddddddddddd: "+e.getMessage());
				writeImportError(values, e.getLocalizedMessage());
				return ImportLineResult.ERROR;
				}
			}
		}
		return ImportLineResult.SUCCESS;
	}

	private boolean isEntryValid(CampaignFormElement definition, CampaignFormDataEntry entry) {
		if (definition.getType().equalsIgnoreCase(CampaignFormElementType.NUMBER.toString())) {
			return NumberUtils.isParsable(entry.getValue().toString());
		} else if (definition.getType().equalsIgnoreCase(CampaignFormElementType.DECIMAL.toString())
				|| definition.getType().equalsIgnoreCase(CampaignFormElementType.RANGE.toString())) {
			return NumberUtils.isParsable(entry.getValue().toString());
		} else if (definition.getType().equalsIgnoreCase(CampaignFormElementType.TEXT.toString())) {
			return !entry.getValue().toString().matches("[0-9]+");
		} else if (definition.getType().equalsIgnoreCase(CampaignFormElementType.YES_NO.toString())) {
			return Arrays.stream(CampaignFormElementType.YES_NO.getAllowedValues()).map(String::toLowerCase)
					.anyMatch(v -> v.equals(entry.getValue().toString().toLowerCase()));
		}
		return true;
	}

	private CampaignFormDataDto insertImportRowIntoData(CampaignFormDataDto campaignFormData, String[] entry, String[] entryHeaderPath)
			throws InvalidColumnException, ImportErrorException {

		logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

		CampaignFormMetaDto campaignMetaDto = FacadeProvider.getCampaignFormMetaFacade()
				.getCampaignFormMetaByUuid(campaignFormMetaUuid);
		campaignFormData.setCampaignFormMeta(
				new CampaignFormMetaReferenceDto(campaignFormMetaUuid, campaignMetaDto.getFormName()));
		List<String> formDataDtoFields = Stream.of(campaignFormData.getClass().getDeclaredFields()).map(Field::getName)
				.collect(Collectors.toList());
		for (int i = 0; i < entry.length; i++) {
			final String propertyPath = entryHeaderPath[i];
			if (formDataDtoFields.contains(propertyPath)) {
				logger.debug("---------" + propertyPath + "@@@@@@@@@@@@@");

				try {
					PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyPath,
							campaignFormData.getClass());
					Class<?> propertyType = propertyDescriptor.getPropertyType();
					if (!executeDefaultInvoke(propertyDescriptor, campaignFormData, entry[i],
							new String[] { propertyPath })) {
						logger.debug("---ccccc-----" + propertyType + "@@@@@@@@@@@@@");

						final UserDto currentUserDto = userFacade.getByUuid(currentUser.getUuid());
						final JurisdictionLevel jurisdictionLevel = UserRole
								.getJurisdictionLevel(currentUserDto.getUserRoles());

						if (propertyType.isAssignableFrom(DistrictReferenceDto.class)) {
							if (jurisdictionLevel == JurisdictionLevel.DISTRICT
									&& !currentUserDto.getDistrict().getCaption().equals(entry[i])) {
								throw new ImportErrorException(I18nProperties.getValidationError(
										Validations.importEntryDistrictNotInUsersJurisdiction, entry[i], propertyPath));
							}
							List<DistrictReferenceDto> district = FacadeProvider.getDistrictFacade()
									.getByExternalID(Long.parseLong(entry[i]), campaignFormData.getRegion(), true);
							if (district.isEmpty()) {
								throw new ImportErrorException(I18nProperties.getValidationError(
										Validations.importEntryDoesNotExistDbOrRegion, entry[i], propertyPath));
							} else if (district.size() > 1) {
								throw new ImportErrorException(I18nProperties.getValidationError(
										Validations.importDistrictNotUnique, entry[i], propertyPath));
							} else {
								propertyDescriptor.getWriteMethod().invoke(campaignFormData, district.get(0));
							}
						} else if (propertyType.isAssignableFrom(CommunityReferenceDto.class)) {
							if (jurisdictionLevel == JurisdictionLevel.COMMUNITY
									&& !currentUserDto.getCommunity().stream().map(CommunityReferenceDto::getCaption)
											.collect(Collectors.toList()).contains(entry[i])) {
								throw new ImportErrorException(I18nProperties.getValidationError(
										Validations.importEntryCommunityNotInUsersJurisdiction, entry[i],
										propertyPath));
							}
							List<CommunityReferenceDto> community = FacadeProvider.getCommunityFacade()
									.getByExternalID(Long.parseLong(entry[i]), campaignFormData.getDistrict(), true);
							if (community.isEmpty()) {
								throw new ImportErrorException(I18nProperties.getValidationError(
										Validations.importEntryDoesNotExistDbOrDistrict, entry[i], propertyPath));
							} else if (community.size() > 1) {
								throw new ImportErrorException(I18nProperties.getValidationError(
										Validations.importCommunityNotUnique, entry[i], propertyPath));
							} else {
								campaignFormData.setCommunity(community.get(0));
								propertyDescriptor.getWriteMethod().invoke(campaignFormData, community.get(0));
							}
						}
					}
				} catch (InvocationTargetException | IllegalAccessException e) {
					logger.debug("---ccccc---aaaa--"+e.getMessage());
					throw new ImportErrorException(
							
							I18nProperties.getValidationError(Validations.importErrorInColumn, propertyPath));
				} catch (IntrospectionException e) {
					logger.debug("---ccccc-----bbbb"+e.getMessage());
					// skip unknown fields
				} catch (ImportErrorException e) {
					logger.debug("---ccccc-----cccc"+e.getMessage());
					throw e;
				} catch (Exception e) {
					logger.debug("---ccccc-----dddddd"+e.getMessage());
					LOGGER.error("Unexpected error when trying to import campaign form data: " + e.getMessage(), e);
					throw new ImportErrorException(
							I18nProperties.getValidationError(Validations.importUnexpectedError));
				}
			} else {
				CampaignFormDataEntry formEntry = new CampaignFormDataEntry(propertyPath, entry[i]);

				Optional<CampaignFormElement> existingFormElement = campaignMetaDto.getCampaignFormElements().stream()
						.filter(e -> e.getId().equals(formEntry.getId())).findFirst();
				if (!existingFormElement.isPresent()) {
					// skip unknown fields
					continue;
				} else if (Objects.nonNull(formEntry.getValue())
						&& StringUtils.isNotBlank(formEntry.getValue().toString())
						&& !isEntryValid(existingFormElement.get(), formEntry)) {
					throw new ImportErrorException(I18nProperties
							.getValidationError(Validations.importWrongDataTypeError, entry[i], propertyPath));
				}

				if (formEntry.getValue() == null || StringUtils.isBlank(formEntry.getValue().toString())) {
					continue;
				}

				// Convert yes/no values to true/false
				if (CampaignFormElementType.YES_NO.toString().equals(existingFormElement.get().getType())) {
					String value = formEntry.getValue().toString();
					if ("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
						formEntry.setValue(true);
					} else if ("no".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
						formEntry.setValue(false);
					}
				}
				if (CampaignFormElementType.RADIO.toString().equals(existingFormElement.get().getType())) {
					String value = formEntry.getValue().toString();
					if ("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
						formEntry.setValue(true);
					} else if ("no".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
						formEntry.setValue(false);
					}
				}

				if (Objects.nonNull(campaignFormData.getFormValues())) {
					List<CampaignFormDataEntry> currentElementFormValues = campaignFormData.getFormValues();
					currentElementFormValues.add(formEntry);
					campaignFormData.setFormValues(currentElementFormValues);
				} else {
					List<CampaignFormDataEntry> formValues = new LinkedList<>();
					formValues.add(formEntry);
					campaignFormData.setFormValues(formValues);
				}
			}
		}

		ImportLineResultDto<CampaignFormDataDto> constraintErrors = validateConstraints(campaignFormData);
		if (constraintErrors.isError()) {
			throw new ImportErrorException(constraintErrors.getMessage());
		}
		
		return campaignFormData;
	}

	@Override
	protected boolean executeDefaultInvoke(PropertyDescriptor pd, Object element, String entry,
			String[] entryHeaderPath) throws InvocationTargetException, IllegalAccessException, ImportErrorException{//, ParseException {

		final boolean invokingSuccessful = super.executeDefaultInvoke(pd, element, entry, entryHeaderPath);
		final Class<?> propertyType = pd.getPropertyType();
		if (propertyType.isAssignableFrom(RegionReferenceDto.class)) {
			final UserDto currentUserDto = userFacade.getByUuid(currentUser.getUuid());
			final JurisdictionLevel jurisdictionLevel = UserRole.getJurisdictionLevel(currentUserDto.getUserRoles());
			if (jurisdictionLevel == JurisdictionLevel.REGION
					&& !currentUserDto.getRegion().getCaption().equals(entry)) {
				throw new ImportErrorException(
						I18nProperties.getValidationError(Validations.importEntryRegionNotInUsersJurisdiction, entry,
								buildEntityProperty(entryHeaderPath)));
			}
		}
		logger.debug("xxxxxxxxxxxxxxxinvokingSuccessful "+invokingSuccessful);
		return invokingSuccessful;
	}

	@Override
	protected String getErrorReportFileName() {
		return "campaign_data_import_error_report.csv";
	}

	private void handleDetectedDuplicate(CampaignFormDataDto newData, CampaignFormDataDto existingData,
			CampaignFormDataImportLock lock) {

		Dialog popupWindow = new Dialog();

		currentUI.accessSynchronously(() -> {
			Runnable cancelCallback = () -> {
				synchronized (lock) {
					lock.setSimilarityChoice(CampaignFormDataSimilarityChoice.CANCEL);
					lock.notify();
					lock.wasNotified = true;
					popupWindow.close();
				}
			};
			Runnable skipCallback = () -> {
				synchronized (lock) {
					lock.setSimilarityChoice(CampaignFormDataSimilarityChoice.SKIP);
					lock.notify();
					lock.wasNotified = true;
					popupWindow.close();
				}
			};
			Runnable overwriteCallback = () -> {
				synchronized (lock) {
					lock.setSimilarityChoice(CampaignFormDataSimilarityChoice.OVERWRITE);
					lock.notify();
					lock.wasNotified = true;
					popupWindow.close();
				}
			};

			CampaignFormDataSelectionField selectionField = new CampaignFormDataSelectionField(newData, existingData,
					String.format(I18nProperties.getString(Strings.infoSkipOrOverrideDuplicateCampaignFormDataImport),
							newData.getCampaign().toString(), newData.getCampaignFormMeta().toString()),
					cancelCallback, skipCallback, overwriteCallback);

			popupWindow.add(selectionField);
			popupWindow.setHeaderTitle(I18nProperties.getString(Strings.headingCampaignFormDataAlreadyExisting));
			popupWindow.setWidth(960, Unit.PIXELS);

			currentUI.add(popupWindow);
			popupWindow.open();
		});
	}

	private enum CampaignFormDataSimilarityChoice {

		CANCEL, SKIP, OVERWRITE;
	}

	private static class CampaignFormDataImportLock {

		protected boolean wasNotified = false;
		protected CampaignFormDataSimilarityChoice similarityChoice;

		public void setSimilarityChoice(CampaignFormDataSimilarityChoice similarityChoice) {
			this.similarityChoice = similarityChoice;
		}
	}
}

