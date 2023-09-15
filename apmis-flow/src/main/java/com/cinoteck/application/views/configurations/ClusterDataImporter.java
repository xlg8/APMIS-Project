package com.cinoteck.application.views.configurations;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cinoteck.application.views.utils.importutils.DataImporter;
import com.cinoteck.application.views.utils.importutils.ImportCellData;
import com.cinoteck.application.views.utils.importutils.ImportErrorException;
import com.cinoteck.application.views.utils.importutils.ImportLineResult;
import com.opencsv.exceptions.CsvValidationException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.importexport.ValueSeparator;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityFacade;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictFacade;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionFacade;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserFacade;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.ValidationRuntimeException;

/**
 * Data importer that is used to import population data.
 */
public class ClusterDataImporter extends DataImporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProvinceDataImporter.class);

	private final CommunityFacade clusterFacade;

	private UI currentUI;
	private boolean isOverWrite;

	// file_, true, userDto, campaignForm.getUuid(), campaignReferenceDto,
	// ValueSeparator.COMMA
	public ClusterDataImporter(File inputFile, boolean hasEntityClassRow, CommunityDto currentUser,
			ValueSeparator csvSeparator, boolean overwrite) throws IOException {

		super(inputFile, hasEntityClassRow, currentUser, csvSeparator);
		this.isOverWrite = overwrite;

		this.clusterFacade = FacadeProvider.getCommunityFacade();
	}

	@Override
	public void startImport(Consumer<StreamResource> addErrorReportToLayoutCallback,
			Consumer<StreamResource> addCredentialReportToLayoutCallback, boolean isUserCreation, UI currentUI,
			boolean duplicatesPossible) throws IOException, CsvValidationException {

		this.currentUI = currentUI;
		super.startImport(addErrorReportToLayoutCallback, null, false, currentUI, false);
	}

	@Override
	protected ImportLineResult importDataFromCsvLine(String[] values, String[] entityClasses, String[] entityProperties,
			String[][] entityPropertyPaths, boolean firstLine) throws IOException, InterruptedException {

		if (values.length > entityProperties.length) {
			writeImportError(values, I18nProperties.getValidationError(Validations.importLineTooLong));
			return ImportLineResult.ERROR;
		}

		// Lets run some validations

		AreaReferenceDto area = null;
		RegionReferenceDto region = null;
		DistrictReferenceDto district = null;
		CommunityReferenceDto cluster = null;
		Long aext_id = null;
		Long areax = null;
		Long districtExtId = null;
		Long clusterExtId = null;
		Integer clusterNumber = null;
		String clusterName = "";

		System.out.println("++++++++++++++++++===============: " + entityProperties.length);

		// Retrieve the region and district from the database or throw an error if more
		// or less than one entry have been retrieved
		for (int i = 0; i < entityProperties.length; i++) {

			System.out.println(entityProperties[i] + " :++++++++++++++++++===============: " + i);

//			
			if (AreaDto.EXTERNAL_ID.equalsIgnoreCase(entityProperties[i])) {
				try {
					Long externalIdValue = Long.parseLong(values[i]);

					areax = externalIdValue;
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (RegionDto.EXTERNAL_ID.equalsIgnoreCase(entityProperties[i])) {
				try {
//					List<RegionReferenceDto> regions = FacadeProvider.getRegionFacade()
//							.getByExternalId(Long.parseLong(values[i]), false);
//					if (regions.size() != 1) {
//						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//						return ImportLineResult.ERROR;
//					}
//					region = regions.get(0);
//					aext_id = Long.parseLong(regions.get(0).toString());
					Long externalIdValue = Long.parseLong(values[i]);
					aext_id = externalIdValue;
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (DistrictDto.EXTERNAL_ID.equalsIgnoreCase(entityProperties[i])) {
				try {
//					List<RegionReferenceDto> regions = FacadeProvider.getRegionFacade()
//							.getByExternalId(Long.parseLong(values[i]), false);
//					if (regions.size() != 1) {
//						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//						return ImportLineResult.ERROR;
//					}
//					region = regions.get(0);
//					aext_id = Long.parseLong(regions.get(0).toString());
					Long externalIdValue = Long.parseLong(values[i]);
					districtExtId = externalIdValue;
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (CommunityDto.EXTERNAL_ID.equalsIgnoreCase(entityProperties[i])) {
				try {
//					List<RegionReferenceDto> regions = FacadeProvider.getRegionFacade()
//							.getByExternalId(Long.parseLong(values[i]), false);
//					if (regions.size() != 1) {
//						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//						return ImportLineResult.ERROR;
//					}
//					region = regions.get(0);
//					aext_id = Long.parseLong(regions.get(0).toString());
					Long externalIdValue = Long.parseLong(values[i]);
					clusterExtId = externalIdValue;
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (CommunityDto.CLUSTER_NUMBER.equalsIgnoreCase(entityProperties[i])) {
				try {
//					List<RegionReferenceDto> regions = FacadeProvider.getRegionFacade()
//							.getByExternalId(Long.parseLong(values[i]), false);
//					if (regions.size() != 1) {
//						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//						return ImportLineResult.ERROR;
//					}
//					region = regions.get(0);
//					aext_id = Long.parseLong(regions.get(0).toString());
					Integer externalIdValue = Integer.parseInt(values[i]);
					clusterNumber = externalIdValue;
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (CommunityDto.NAME.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {
					clusterName = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | This cannot be empty");
					return ImportLineResult.ERROR;

				} else {

					String clusterName_ = values[i];

					String regex = "\\S+";
					boolean isNoSpaceMatch = clusterName_.matches(regex);

					if (!isNoSpaceMatch) {
						writeImportError(values,
								new ImportErrorException(values[i], entityProperties[i]).getMessage()
										+ " | This cannot be empty or have white space and might be "
										+ Validations.textTooLong);
						return ImportLineResult.ERROR;
					} else {
						DistrictReferenceDto regrefDto = new DistrictReferenceDto();
						List<CommunityReferenceDto> clusterNameList = FacadeProvider.getCommunityFacade()
								.getByName(clusterName_, regrefDto, true);

						UserDto usrCheck = FacadeProvider.getUserFacade().getByUserName(clusterName_);

						if (clusterNameList.size() < 1) {
							clusterName = clusterName_;

						} else {
							if (isOverWrite) {
								clusterName = clusterName_;
								return ImportLineResult.SUCCESS;

							} else {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage()
												+ " | Cluster Name exist ");
								return ImportLineResult.ERROR;

							}

						}

					}
				}
			}

			final AreaReferenceDto finalArea = area;
			final RegionReferenceDto finalRegion = region;
			final DistrictReferenceDto finalDistrict = district;
			final String finalClustername = clusterName;
			final Long extd = aext_id;
			final Long areaid = areax;
			final Long districtid = districtExtId;
			final Long clusterid = clusterExtId;
			final Integer clusterNo = clusterNumber;

			List<CommunityDto> newUserLinetoSave = new ArrayList<>();

			CommunityDto newUserLine = CommunityDto.build();

			System.out.println("++++++++++++++++existingPopulationData.NOTisPresent()++++++++++++++++ ");
			newUserLine.setName(finalClustername);
			newUserLine.setRegion(finalRegion);
			newUserLine.setDistrict(finalDistrict);
			newUserLine.setClusterNumber(clusterNo);
			newUserLine.setExternalId(clusterid);
//			newUserLine.setName(finalRProvincename);

			boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
					new Function<ImportCellData, Exception>() {

						@Override
						public Exception apply(ImportCellData cellData) {
							System.out.println("++++++++++++++++111111111: " + cellData.getEntityPropertyPath()[0]);

							try {

								if (CommunityDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setName(cellData.getValue());
								}
								if (CommunityDto.CLUSTER_NUMBER.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setClusterNumber(Integer.parseInt(cellData.getValue()));
								}
								if (CommunityDto.EXTERNAL_ID.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setExternalId(Long.parseLong(cellData.getValue()));
								}
								if (CommunityDto.REGION.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									Long externalId = Long.parseLong(cellData.getValue());
									List<RegionReferenceDto> areasz = FacadeProvider.getRegionFacade()
											.getByExternalId(externalId, false);
									RegionReferenceDto areaReferenceDto = areasz.get(0);
									newUserLine.setRegion(areaReferenceDto);
//									newUserLine.setArea(cellData.getValue());
								}

								if (CommunityDto.DISTRICT.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									Long externalId = Long.parseLong(cellData.getValue());
									List<DistrictReferenceDto> areasz = FacadeProvider.getDistrictFacade()
											.getByExternalId(externalId, false);
									DistrictReferenceDto districtReferenceDto = areasz.get(0);
									newUserLine.setDistrict(districtReferenceDto);
//									newUserLine.setArea(cellData.getValue());
								}

								newUserLinetoSave.add(newUserLine);

							} catch (NumberFormatException e) {
								System.out.println("++++++++++++++++Error found++++++++++++++++ ");

								return e;
							}

							return null;
						}
					});

			if (!usersDataHasImportError) {

				try {
					FacadeProvider.getCommunityFacade().save(newUserLinetoSave.get(0), true);

					return ImportLineResult.SUCCESS;
				} catch (ValidationRuntimeException e) {
					writeImportError(values, e.getMessage());
					return ImportLineResult.ERROR;
				}
			} else {
				return ImportLineResult.ERROR;
			}
		}
		return ImportLineResult.SUCCESS;

	}

	@Override
	protected boolean executeDefaultInvoke(PropertyDescriptor pd, Object element, String entry,
			String[] entryHeaderPath) throws InvocationTargetException, IllegalAccessException, ImportErrorException {

		final boolean invokingSuccessful = super.executeDefaultInvoke(pd, element, entry, entryHeaderPath);
		final Class<?> propertyType = pd.getPropertyType();
//		if (propertyType.isAssignableFrom(RegionReferenceDto.class)) {
//			private final UserFacade userFacade;
//			final UserDto currentUserDto = userFacade.getByUuid(currentUser.getUuid());
//			final JurisdictionLevel jurisdictionLevel = UserRole.getJurisdictionLevel(currentUserDto.getUserRoles());
//			if (jurisdictionLevel == JurisdictionLevel.REGION
//					&& !currentUserDto.getRegion().getCaption().equals(entry)) {
//				throw new ImportErrorException(
//						I18nProperties.getValidationError(Validations.importEntryRegionNotInUsersJurisdiction, entry,
//								buildEntityProperty(entryHeaderPath)));
//			}
//		}
		return invokingSuccessful;
	}

	@Override
	protected String getErrorReportFileName() {
		return "province_Import_error_report.csv";
	}
}