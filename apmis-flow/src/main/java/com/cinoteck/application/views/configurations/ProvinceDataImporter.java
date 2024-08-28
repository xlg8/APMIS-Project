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
import de.symeda.sormas.api.importexport.ValueSeparator;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
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
public class ProvinceDataImporter extends DataImporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProvinceDataImporter.class);
	
	private static final String PROVINCE_NAME = "Province_Name";
	private static final String R_CODE = "RCode";
	private static final String P_CODE = "PCode";
			
	private final RegionFacade regionFacade;

	private UI currentUI;

	private boolean isOverWrite;
	private boolean isOverWriteEnabledCode;
	List<RegionDto> provinceNameList = new ArrayList<>();
	List<RegionReferenceDto> provinces = new ArrayList<>();
	UserProvider userProvider = new UserProvider();
	LocalDate localDate = LocalDate.now();
	Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());



	private boolean stopProcessNow;

	// file_, true, userDto, campaignForm.getUuid(), campaignReferenceDto,
	// ValueSeparator.COMMA
	public ProvinceDataImporter(File inputFile, boolean hasEntityClassRow, RegionDto currentUser,
			ValueSeparator csvSeparator, boolean overwrite) throws IOException {

		super(inputFile, hasEntityClassRow, currentUser, csvSeparator);
		this.isOverWrite = overwrite;
		this.regionFacade = FacadeProvider.getRegionFacade();
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

		AreaReferenceDto region = null;
		Long provinceExternalId = null;
		String provinceName = "";
		
		// Retrieve the region and district from the database or throw an error if more
		// or less than one entry have been retrieved
		for (int i = 0; i < entityProperties.length; i++) {
			
			if (PROVINCE_NAME.equalsIgnoreCase(entityProperties[i])) {
				String provinceName_ = values[i];
				if (DataHelper.isNullOrEmpty(values[i])) {
					provinceName = "";
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | This cannot be empty");
					return ImportLineResult.ERROR;

				} else {
					

					String regex = "\\S+";
					boolean isNoSpaceMatch = provinceName_.matches(regex);
					boolean isNotTooShort = provinceName_.length() > 2;

					if ( !isNoSpaceMatch && isNotTooShort) {
						writeImportError(values,
								new ImportErrorException(values[i], entityProperties[i]).getMessage()
										+ " | This cannot be empty or have white space or too short (less than 2 letters).");
						return ImportLineResult.ERROR;
					} else {
						provinceNameList = FacadeProvider.getRegionFacade().getByName(provinceName_, true);

						if (provinceNameList.size() < 1) {
							provinceName = provinceName_;

						} else {
//							if(isOverWrite){
//								
//								provinceName = provinceName_;
//								return ImportLineResult.SUCCESS;
//
//							}else {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage()
												+ " | Province Name Exists ");
								return ImportLineResult.ERROR;
//							}
						}

					}

				}
			}
			
			if (P_CODE.equalsIgnoreCase(entityProperties[i])) {
				try {
					Long.parseLong(values[i]);
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | PCode accepts number only");
					return ImportLineResult.ERROR;
				}
				if (DataHelper.isNullOrEmpty(values[i])) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | PCode Cannot Be Left Empty");
					return ImportLineResult.ERROR;

				} else {
					
					provinces = FacadeProvider.getRegionFacade()
							.getByExternalId(Long.parseLong(values[i]), true);
					
					if (provinces.size() > 0) {
						if (isOverWrite) {
							System.out.println(isOverWrite + "overrrrrrideeeeeeeeeeeeeeeee");
							try {
								Long externalIdValue = Long.parseLong(values[i]);
								provinceExternalId = externalIdValue;
								isOverWriteEnabledCode = true;
							} catch (NumberFormatException e) {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage() + " | " +e.getMessage());
								return ImportLineResult.ERROR;
							}
						} else {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage()+" | PCode already on the system");
							return ImportLineResult.ERROR;
						}

					} else if (provinces.size() == 0) {
						try {
							Long externalIdValue = Long.parseLong(values[i]);
							provinceExternalId = externalIdValue;
						} catch (NumberFormatException e) {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage() + " : "
											+ e.getLocalizedMessage());
							return ImportLineResult.ERROR;
						}
					}
				}

			}


			
			if (R_CODE.equalsIgnoreCase(entityProperties[i])) {
				try {
					Long.parseLong(values[i]);
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | RCode accepts number only");
					return ImportLineResult.ERROR;
				}
				List<AreaReferenceDto> existingAreas = FacadeProvider.getAreaFacade()
						.getByExternalId(Long.parseLong(values[i]), false);
				if (existingAreas.size() != 1) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | Region Doesn't Exist");
					return ImportLineResult.ERROR;
				} else if (existingAreas.size() == 1){
					region = existingAreas.get(0);
					try {
						region = existingAreas.get(0);
					} catch (NumberFormatException e) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
				} else {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | More thank one Region found for this RCode");
					return ImportLineResult.ERROR;
				}

			}
			
			System.out.println(provinceName+" | "+entityProperties[i] + " == "+ P_CODE +"? = "+P_CODE.equalsIgnoreCase(entityProperties[i]) + "__:_" +values[i]);
			
			
		}
			
		if (region != null && provinceExternalId != null && provinceName != "") {}else {
			writeImportError(values, " | Somthing went wrong");
			return ImportLineResult.ERROR;
		}
		
		System.out.println(provinceName+" | "+provinceExternalId);
		
			final AreaReferenceDto finalArea = region;
			final String finalRProvincename = provinceName;
			final Long extd = provinceExternalId;

			List<RegionDto> newUserLinetoSave = new ArrayList<>();

			RegionDto newUserLine = RegionDto.build();

			newUserLine.setArea(finalArea);
			newUserLine.setExternalId(extd);
			newUserLine.setName(finalRProvincename);

			
			if(isOverWrite && isOverWriteEnabledCode) {
				
				System.out.println("isOverWrite && isOverWriteEnabledCode" +  "cell data value ");
				RegionDto newUserLine_ = FacadeProvider.getRegionFacade().getByUuid(provinces.get(0).getUuid());
				newUserLine_.setArea(finalArea);
				newUserLine_.setExternalId(extd);
				newUserLine_.setName(finalRProvincename);
				 boolean isSameData = checkSameInstances(provinces, provinceNameList);
				 if(isSameData) {
					 return ImportLineResult.SUCCESS;
				 }
				 boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
							new Function<ImportCellData, Exception>() {

								@Override
								public Exception apply(ImportCellData cellData) {
									try {
										if (RegionDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
											newUserLine_.setName(cellData.getValue());
											System.out.println(cellData.getValue() +  "cell data value ");
										}
										if (RegionDto.EXTERNAL_ID.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
											newUserLine_.setExternalId(Long.parseLong(cellData.getValue()));
											System.out.println(cellData.getValue() +  "cell data value ");
										}
										newUserLinetoSave.add(newUserLine_);

									} catch (NumberFormatException e) {
										return e;
//									
								}
									return null;
								}
							});
				 if (!usersDataHasImportError && !stopProcessNow) {

						try {
							FacadeProvider.getRegionFacade().save(newUserLinetoSave.get(0), true);

							return ImportLineResult.SUCCESS;
						} catch (ValidationRuntimeException e) {
							writeImportError(values, e.getMessage());
							return ImportLineResult.ERROR;
						}
					} else {
						return ImportLineResult.ERROR;
					}
				 
				 
			}else {
				boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
						new Function<ImportCellData, Exception>() {

							@Override
							public Exception apply(ImportCellData cellData) {
								try {

									newUserLinetoSave.add(newUserLine);

								} catch (NumberFormatException e) {
									return e;
//								
							}
								return null;
							}
						});

				if (!usersDataHasImportError && !stopProcessNow) {
					boolean checkExeption = false;
					try {
						FacadeProvider.getRegionFacade().save(newUserLinetoSave.get(0), true);

						return ImportLineResult.SUCCESS;
					} catch (ValidationRuntimeException e) {
						checkExeption = true;
						writeImportError(values, e.getMessage());
						return ImportLineResult.ERROR;
					}finally {
						if (!checkExeption) {
							
							ConfigurationChangeLogDto configurationChangeLogDto = new ConfigurationChangeLogDto();

							for (RegionDto  regionData : newUserLinetoSave){
								
								configurationChangeLogDto.setCreatinguser(userProvider.getUser().getUserName());
								configurationChangeLogDto.setAction_unit_type("Province");
								configurationChangeLogDto.setAction_unit_name(regionData.getName());
								configurationChangeLogDto.setUnit_code(regionData.getExternalId());
								configurationChangeLogDto.setAction_date(date);

								if(isOverWrite) {
									configurationChangeLogDto.setAction_logged("Import : Overwrite");

								}else {
									configurationChangeLogDto.setAction_logged("Import");

								}
							}
							
															
							FacadeProvider.getAreaFacade().saveAreaChangeLog(configurationChangeLogDto);
						}
					}
				} else {
					return ImportLineResult.ERROR;
				}
			}
//			return ImportLineResult.SUCCESS;
		
		
	}
	
	private void stopProcessing(boolean stop) {
		stopProcessNow = stop;
	}
	
//	@Override
	 public static boolean checkSameInstances(List<RegionReferenceDto> list1, List<RegionDto> list2) {
	        if (list1.size() != list2.size()) {
	            return false;
	        }

	        for (RegionReferenceDto model : list1) {
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
		return "province_Import_error_report.csv";
	}
}