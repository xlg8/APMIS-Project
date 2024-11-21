package com.cinoteck.application.views.configurations;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cinoteck.application.UserProvider;
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
import de.symeda.sormas.api.importexport.InvalidColumnException;
import de.symeda.sormas.api.importexport.ValueSeparator;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDryRunDto;
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
public class DistrictDataDryRunner extends DataImporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProvinceDataImporter.class);
	private static final String DISTRICT_NAME = "District_Name";
	private static final String P_CODE = "PCode";
	private static final String D_CODE = "DCode";

	private final DistrictFacade districtFacade;

	private UI currentUI;

	private boolean isOverWrite;
	private boolean isOverWriteEnabledCode;
	List<DistrictReferenceDto> districtNameList = new ArrayList<>();
	List<DistrictReferenceDto> districts = new ArrayList<>();
	UserProvider userProvider = new UserProvider();
	
	LocalDate localDate = LocalDate.now();
	Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

File file;

	// file_, true, userDto, campaignForm.getUuid(), campaignReferenceDto,
	// ValueSeparator.COMMA
	public DistrictDataDryRunner(File inputFile, boolean hasEntityClassRow, DistrictDto currentUser,
			ValueSeparator csvSeparator, boolean overwrite) throws IOException {

		super(inputFile, hasEntityClassRow, currentUser, csvSeparator);
		this.isOverWrite = overwrite;
		this.districtFacade = FacadeProvider.getDistrictFacade();
	}

	@Override
	public void startImport(File file, Consumer<StreamResource> addErrorReportToLayoutCallback,
			Consumer<StreamResource> addCredentialReportToLayoutCallback, boolean isUserCreation, UI currentUI,
			boolean duplicatesPossible) throws IOException, CsvValidationException {
this.file=file;
		this.currentUI = currentUI;
		super.startImport(file, addErrorReportToLayoutCallback, null, false, currentUI, false);
	}

	@Override
	protected ImportLineResult importDataFromCsvLine(String[] values, String[] entityClasses, String[] entityProperties,
			String[][] entityPropertyPaths, boolean firstLine) throws IOException, InterruptedException ,  InvalidColumnException, InterruptedException, ConstraintViolationException ,PersistenceException  ,TransactionRolledbackLocalException, EJBTransactionRolledbackException , EntityExistsException{

		if (values.length > entityProperties.length) {
			writeImportError(values, I18nProperties.getValidationError(Validations.importLineTooLong));
			return ImportLineResult.ERROR;
		}

		// Lets run some validations
		RegionReferenceDto province = null;
		Long province_xt_id = null;

		Long districtExtId = null;
		String districtName = "";
		String risk = "";

		// Retrieve the region and district from the database or throw an error if more
		// or less than one entry have been retrieved
		for (int i = 0; i < entityProperties.length; i++) {

			System.out.println(entityProperties[i] + " :++++++++++++++++++===============: " + i + " = " + values[i]);
			if (DISTRICT_NAME.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {
					districtName = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | This cannot be empty");
					return ImportLineResult.ERROR;

				} else {

					String districtName_ = values[i];

					String regex = "\\S+";
					boolean isNoSpaceMatch = districtName_.matches(regex);

					if (!isNoSpaceMatch) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | This cannot be empty or have white space.");
						return ImportLineResult.ERROR;
					} else {
						RegionReferenceDto regrefDto = new RegionReferenceDto();
						districtNameList = FacadeProvider.getDistrictFacade().getByName(districtName_, regrefDto, true);

						if (districtNameList.size() < 1) {
							districtName = districtName_;

						} else {
//							if (isOverWrite) {
//
//								districtName = districtName_;
							// return ImportLineResult.SUCCESS;

//							} else {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage()
											+ " | District Name Exists ");
							return ImportLineResult.ERROR;
//							}
						}

					}

				}
			}
//			
//			if (R_CODE.equalsIgnoreCase(entityProperties[i])) {
//				try {
//					region_xt_id = Long.parseLong(values[i]);
//
//				} catch (NumberFormatException e) {
//					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//					return ImportLineResult.ERROR;
//				}
//			}

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

			if (DistrictDto.RISK.equalsIgnoreCase(entityProperties[i])) {
				risk = values[i];

			}

			if (D_CODE.equalsIgnoreCase(entityProperties[i])) {
				if (DataHelper.isNullOrEmpty(values[i])) {

					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | DCode cannot be left empty");
					return ImportLineResult.ERROR;

				} else {

					try {
						Long externalIdValue = Long.parseLong(values[i]);

						districts = FacadeProvider.getDistrictFacade().getByExternalId(externalIdValue, false);

						if (districts.size() > 0) {
							if (isOverWrite && districts.size() == 1) {
								try {
//											Long externalIdValue = Long.parseLong(values[i]);
									districtExtId = externalIdValue;
									isOverWriteEnabledCode = true;
								} catch (NumberFormatException e) {
									writeImportError(values,
											new ImportErrorException(values[i], entityProperties[i]).getMessage()
													+ " | " + e.getMessage());
									return ImportLineResult.ERROR;
								}

							} else if (districts.size() > 1) {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage()
												+ " | Possible duplicate already on this system");
								return ImportLineResult.ERROR;
							} else {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage());
								return ImportLineResult.ERROR;
							}

						} else if (districts.size() == 0) {

							districtExtId = externalIdValue;

						}

					} catch (NumberFormatException e) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}

				}
			}
		}
//		if(isOverWrite) {
//			
//		}else {
//			
//		}
		if (province_xt_id != null && province != null && districtName != "" && districtExtId != null) {
		} else {
			writeImportError(values, " | Somthing went wrong: Required data not supplied");
			return ImportLineResult.ERROR;
		}

		final RegionReferenceDto finalRegion = province;
		final String finalDistrictname = districtName;
		final Long districtid = districtExtId;
		String rixk = risk;

		List<DistrictDryRunDto> newUserLinetoSave = new ArrayList<>();

		DistrictDryRunDto newUserLine = DistrictDryRunDto.build();

		newUserLine.setName(finalDistrictname);
		newUserLine.setRegion(finalRegion);
		newUserLine.setExternalId(districtid);
		newUserLine.setRisk(rixk);

		if (isOverWrite && isOverWriteEnabledCode) {
			DistrictDto newUserLine_ = FacadeProvider.getDistrictFacade().getByUuid(districts.get(0).getUuid());
			;
			newUserLine_.setName(finalDistrictname);
			newUserLine_.setRegion(finalRegion);
			newUserLine_.setExternalId(districtid);
			newUserLine_.setRisk(rixk);

			boolean isSameData = checkSameInstances(districts, districtNameList);
			if (isSameData) {
				return ImportLineResult.SUCCESS;
			}

			boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
					new Function<ImportCellData, Exception>() {

						@Override
						public Exception apply(ImportCellData cellData) {
							System.out.println("++++++++++++++++111111111: " + cellData.getEntityPropertyPath()[0]);

							try {

								if (DistrictDryRunDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine_.setName(cellData.getValue());
								}
								if (DistrictDryRunDto.EXTERNAL_ID.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine_.setExternalId(Long.parseLong(cellData.getValue()));
								}

								if (DistrictDryRunDto.REGION.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									Long externalId = Long.parseLong(cellData.getValue());
									List<RegionReferenceDto> areasz = FacadeProvider.getRegionFacade()
											.getByExternalId(externalId, false);
									RegionReferenceDto areaReferenceDto = areasz.get(0);
									newUserLine_.setRegion(areaReferenceDto);
//											newUserLine.setArea(cellData.getValue());
								}
								if (DistrictDto.RISK.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine_.setRisk(cellData.getValue());
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
				boolean checkExeption = false;
				try {
					FacadeProvider.getDistrictDryRunFacade().save(newUserLinetoSave.get(0), true);

					return ImportLineResult.SUCCESS;
				} catch (ValidationRuntimeException e) {
					writeImportError(values, e.getMessage());
					return ImportLineResult.ERROR;
				} catch (TransactionRolledbackLocalException ex) {
					checkExeption = true;
					writeImportError(values, ex.getMessage());
					return ImportLineResult.ERROR;
				} catch (Exception e) {
					checkExeption = true;
					writeImportError(values, e.getMessage());
					return ImportLineResult.ERROR;
				}
			} else {
				return ImportLineResult.ERROR;
			}

		} else {

			boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
					new Function<ImportCellData, Exception>() {

						@Override
						public Exception apply(ImportCellData cellData) {
							System.out.println("++++++++++++++++111111111: " + cellData.getEntityPropertyPath()[0]);

							try {

								if (DistrictDryRunDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setName(cellData.getValue());
								}
								if (DistrictDryRunDto.EXTERNAL_ID.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setExternalId(Long.parseLong(cellData.getValue()));
								}
								if (DistrictDryRunDto.REGION.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									Long externalId = Long.parseLong(cellData.getValue());
									List<RegionReferenceDto> areasz = FacadeProvider.getRegionFacade()
											.getByExternalId(externalId, false);
									RegionReferenceDto areaReferenceDto = areasz.get(0);
									newUserLine.setRegion(areaReferenceDto);
//									newUserLine.setArea(cellData.getValue());
								}
								if (DistrictDryRunDto.RISK.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setRisk(cellData.getValue());
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
				boolean checkExeption = false;
				try {
					FacadeProvider.getDistrictDryRunFacade().save(newUserLinetoSave.get(0), true);

					return ImportLineResult.SUCCESS;
				} catch (ValidationRuntimeException e) {
					checkExeption = true;
					writeImportError(values, e.getMessage());
					return ImportLineResult.ERROR;
				} finally {
					if (!checkExeption) {

						ConfigurationChangeLogDto configurationChangeLogDto = new ConfigurationChangeLogDto();

						for (DistrictDryRunDto districtData : newUserLinetoSave) {

							configurationChangeLogDto.setCreatinguser(userProvider.getUser().getUserName());
							configurationChangeLogDto.setAction_unit_type("District");
							configurationChangeLogDto.setAction_unit_name(districtData.getName());
							configurationChangeLogDto.setUnit_code(districtData.getExternalId());
							configurationChangeLogDto.setAction_date(date);

							if (isOverWrite) {
								configurationChangeLogDto.setAction_logged("Import : Overwrite");

							} else {
								configurationChangeLogDto.setAction_logged("Import");

							}
						}

						FacadeProvider.getAreaFacade().saveAreaChangeLog(configurationChangeLogDto);
						checkExeption = false;
					}
				}
			} else {
				return ImportLineResult.ERROR;
			}
		}

	}

	public static boolean checkSameInstances(List<DistrictReferenceDto> list1, List<DistrictReferenceDto> list2) {
		if (list1.size() != list2.size()) {
			return false;
		}

		for (DistrictReferenceDto model : list1) {
			if (!list2.contains(model)) {
				return false;
			}
		}
		return true;
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
		return "district_dryrun_Import_error_report.csv";
	}
}