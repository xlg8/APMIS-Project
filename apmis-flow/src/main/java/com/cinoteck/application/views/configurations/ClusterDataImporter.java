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
	private static final String CLUSTER_NAME = "Cluster_Name";
	private static final String CLUSTER_NO = "ClusterNo";
	private static final String P_CODE = "PCode";
	private static final String D_CODE = "DCode";
	private static final String C_CODE = "CCode";

	private final CommunityFacade clusterFacade;

	private UI currentUI;
	private boolean isOverWrite;
	private boolean isOverWriteEnabledCode;
	List<CommunityReferenceDto> clusters = new ArrayList<>();
	List<CommunityReferenceDto> clusterNameList = new ArrayList<>();

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

		RegionReferenceDto province = null;
		DistrictReferenceDto district = null;
		Long province_xt_id = null;
		Long district_xt_id = null;

		Long clusterExtId = null;
		Integer clusterNumber = null;
		String clusterName = "";

		// Retrieve the region and district from the database or throw an error if more
		// or less than one entry have been retrieved
		for (int i = 0; i < entityProperties.length; i++) {

			if (P_CODE.equalsIgnoreCase(entityProperties[i])) {
				try {
					province_xt_id = Long.parseLong(values[i]);
					List<RegionReferenceDto> existingProvinces = FacadeProvider.getRegionFacade()
							.getByExternalId(province_xt_id, false);

					if (existingProvinces.size() < 1) {
						province_xt_id = null;
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Province does not exist or > 1");
						return ImportLineResult.ERROR;
					} else if (existingProvinces.size() == 1) {

						province = existingProvinces.get(0);

					} else {
						province_xt_id = null;
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Posible Duplicate PCode Found");
						return ImportLineResult.ERROR;
					}
				} catch (NumberFormatException e) {
					province_xt_id = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (D_CODE.equalsIgnoreCase(entityProperties[i])) {
				try {
					district_xt_id = Long.parseLong(values[i]);

					List<DistrictReferenceDto> existingDistricts = FacadeProvider.getDistrictFacade()
							.getByExternalId(district_xt_id, false);

					if (existingDistricts.size() < 1) {
						district_xt_id = null;
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | District does not exist or > 1");
						return ImportLineResult.ERROR;
					} else if (existingDistricts.size() == 1) {

						district = existingDistricts.get(0);

					} else {
						district_xt_id = null;
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | Posible Duplicate DCode Found");
						return ImportLineResult.ERROR;
					}
				} catch (NumberFormatException e) {
					district_xt_id = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (C_CODE.equalsIgnoreCase(entityProperties[i])) {
				if (DataHelper.isNullOrEmpty(values[i])) {

					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | CCode cannot be left empty");
					return ImportLineResult.ERROR;

				} else {

					try {
						Long externalIdValue = Long.parseLong(values[i]);

						clusters = FacadeProvider.getCommunityFacade()
								.getByExternalId(externalIdValue, false);

						if (clusters.size() > 0) {
							if (isOverWrite && clusters.size() == 1) {
								try {
//									Long externalIdValue = Long.parseLong(values[i]);
									clusterExtId = externalIdValue;
									isOverWriteEnabledCode = true;
								} catch (NumberFormatException e) {
									writeImportError(values,
											new ImportErrorException(values[i], entityProperties[i]).getMessage() + " | " +e.getMessage());
									return ImportLineResult.ERROR;
								}
						
								
							} else if (clusters.size() > 1) {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage()
												+ " | Possible duplicate already on this system");
								return ImportLineResult.ERROR;
							} else {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage() + " | CCode exists on the system");
								
								return ImportLineResult.ERROR;
							}

						} else if (clusters.size() == 0) {

							clusterExtId = externalIdValue;

						}

					} catch (NumberFormatException e) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
				}
			}

			if (CLUSTER_NO.equalsIgnoreCase(entityProperties[i])) {
				try {

					Integer externalIdValue = Integer.parseInt(values[i]);
					clusterNumber = externalIdValue;
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}

			if (CLUSTER_NAME.equalsIgnoreCase(entityProperties[i])) {
				
//				System.out.println("1111111111111111111111111111111111111111111111111111111111111111111111");

				if (DataHelper.isNullOrEmpty(values[i])) {
					clusterName = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | This cannot be empty");
					return ImportLineResult.ERROR;

				} else {
//					System.out.println("22222222222222222222222222222222222222222222222222222222222222222222222222222");

					String clusterName_ = values[i];

//					String regex = "\\S+";
					boolean isNoSpaceMatch = clusterName_.matches(clusterName_);

					if (!isNoSpaceMatch) {
						writeImportError(values,
								new ImportErrorException(values[i], entityProperties[i]).getMessage()
										+ " | This cannot be empty or have white space and might be "
										+ Validations.textTooLong);
						return ImportLineResult.ERROR;
					} else {
						


						DistrictReferenceDto regrefDto = new DistrictReferenceDto();
						clusterNameList = FacadeProvider.getCommunityFacade()
								.getByName(clusterName_, regrefDto, true);
						
						if (clusterNameList.size() < 1) {
							clusterName = clusterName_;

						} else {

//							if (isOverWrite) {
//								clusterName = clusterName_;
//								return ImportLineResult.SUCCESS;
//
//							} else {

								if (clusterNameList.size() >= 0) {
								//	System.out.println("6666666666666666666666666666666666666666666666666666");

									List<DistrictReferenceDto> existingDistricts = FacadeProvider.getDistrictFacade()
											.getByExternalId(district_xt_id, false);

									String finalDistrictUUid = "";

									for (DistrictReferenceDto isolatedDistrict : existingDistricts) {
										finalDistrictUUid = isolatedDistrict.getUuid();
									}

									List<CommunityReferenceDto> checkClusterInDistrictList = FacadeProvider
											.getCommunityFacade().getAllActiveByDistrict(finalDistrictUUid);

									List<String> clusterNames = new ArrayList<String>();
									
									
									System.out.println(checkClusterInDistrictList.size() + "List of Clusters in District");
									for (CommunityReferenceDto ffff : checkClusterInDistrictList) {
										
										String caption  = ffff.getCaption();
										
										clusterNames.add(caption);
										
//										System.out.println(clusterNames + " Cluster Name   Liat"  + caption );
//										System.out/.println(clusterNames.size() + "Size  of Clusters Name List");
									
									}	
									if (clusterNames.contains(values[i])) {
										

										writeImportError(values,
												new ImportErrorException(values[i], entityProperties[i]).getMessage()
														+ " | Cluster Name exist ");
										
										return ImportLineResult.ERROR;
									} else {
										
//										System.out.println("Successssssfulllll   " + clusterName_ + "yyy"+ clusterName + values[i]);
										clusterName = clusterName_;
										
//										return ImportLineResult.SUCCESS;
									}
								}

//							}

						}

					}
				}
			}

		}

		if (province_xt_id != null && province != null && clusterName != "" && clusterExtId != null
				&& clusterNumber != null) {
		} else {
			writeImportError(values, " | Somthing went wrong: Required data not supplied");
			return ImportLineResult.ERROR;
		}

		// check if dcode is a subchild of pcode
		List<DistrictReferenceDto> toCheck = FacadeProvider.getDistrictFacade()
				.getAllActiveByRegion(province.getUuid());
		if (!toCheck.contains(district)) {
			writeImportError(values, " | PCode does not match DCode");
			return ImportLineResult.ERROR;
		}

		final RegionReferenceDto finalRegion = province;
		final DistrictReferenceDto finalDistrict = district;

		final String finalClustername = clusterName;
		final Long clusterid = clusterExtId;
		final Integer clusterNo = clusterNumber;

		List<CommunityDto> newUserLinetoSave = new ArrayList<>();
		
		if(isOverWrite && isOverWriteEnabledCode) {
			CommunityDto newUserLine_ = FacadeProvider.getCommunityFacade().getByUuid(clusters.get(0).getUuid());
			newUserLine_.setName(finalClustername);
			newUserLine_.setRegion(finalRegion);
			newUserLine_.setDistrict(finalDistrict);
			newUserLine_.setClusterNumber(clusterNo);
			newUserLine_.setExternalId(clusterid);
			
			boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
					new Function<ImportCellData, Exception>() {

						@Override
						public Exception apply(ImportCellData cellData) {
							System.out.println("++++++++++++++++111111111: " + cellData.getEntityPropertyPath()[0]);

							try {

								if (CommunityDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine_.setName(cellData.getValue());
								}
								if (CommunityDto.CLUSTER_NUMBER.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine_.setClusterNumber(Integer.parseInt(cellData.getValue()));
								}
								if (CommunityDto.EXTERNAL_ID.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine_.setExternalId(Long.parseLong(cellData.getValue()));
								}
								if (CommunityDto.REGION.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									Long externalId = Long.parseLong(cellData.getValue());
									List<RegionReferenceDto> areasz = FacadeProvider.getRegionFacade()
											.getByExternalId(externalId, false);
									RegionReferenceDto areaReferenceDto = areasz.get(0);
									newUserLine_.setRegion(areaReferenceDto);
//										newUserLine_.setArea(cellData.getValue());
								}

								if (CommunityDto.DISTRICT.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									Long externalId = Long.parseLong(cellData.getValue());
									List<DistrictReferenceDto> areasz = FacadeProvider.getDistrictFacade()
											.getByExternalId(externalId, false);
									DistrictReferenceDto districtReferenceDto = areasz.get(0);
									newUserLine_.setDistrict(districtReferenceDto);
//										newUserLine_.setArea(cellData.getValue());
								}

								newUserLinetoSave.add(newUserLine_);

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
					writeImportError(values, values + " already exists.");
					return ImportLineResult.ERROR;
				}
			} else {
				return ImportLineResult.ERROR;
			}
			
		}else {
			CommunityDto newUserLine = CommunityDto.build();

			System.out.println("++++++++++++++++existingPopulationData.NOTisPresent()++++++++++++++++ ");
			newUserLine.setName(finalClustername);
			newUserLine.setRegion(finalRegion);
			newUserLine.setDistrict(finalDistrict);
			newUserLine.setClusterNumber(clusterNo);
			newUserLine.setExternalId(clusterid);
//				newUserLine.setName(finalRProvincename);

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
//										newUserLine.setArea(cellData.getValue());
								}

								if (CommunityDto.DISTRICT.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									Long externalId = Long.parseLong(cellData.getValue());
									List<DistrictReferenceDto> areasz = FacadeProvider.getDistrictFacade()
											.getByExternalId(externalId, false);
									DistrictReferenceDto districtReferenceDto = areasz.get(0);
									newUserLine.setDistrict(districtReferenceDto);
//										newUserLine.setArea(cellData.getValue());
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
					writeImportError(values, values + " already exists.");
					return ImportLineResult.ERROR;
				}
			} else {
				return ImportLineResult.ERROR;
			}
		}

		

	}

	@Override
	protected boolean executeDefaultInvoke(PropertyDescriptor pd, Object element, String entry,
			String[] entryHeaderPath) throws InvocationTargetException, IllegalAccessException, ImportErrorException {

		final boolean invokingSuccessful = super.executeDefaultInvoke(pd, element, entry, entryHeaderPath);
		final Class<?> propertyType = pd.getPropertyType();
		return invokingSuccessful;
	}

	@Override
	protected String getErrorReportFileName() {
		return "province_Import_error_report.csv";
	}
}