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

	private final RegionFacade regionFacade;

	private UI currentUI;

	private boolean isOverWrite;

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

		AreaReferenceDto area = null;
		RegionReferenceDto region = null;
		RegionDto regionDto = null;
		Long provinceExternalId = null;
		Long areax = null;
		String provinceName = "";

		System.out.println("++++++++++++++++++===============: " + entityProperties.length);

		// Retrieve the region and district from the database or throw an error if more
		// or less than one entry have been retrieved
		for (int i = 0; i < entityProperties.length; i++) {

			System.out.println(entityProperties[i] + " :++++++++++++++++++===============: " + i);
			
			if (RegionDto.NAME.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {
					provinceName = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | This cannot be empty");
					return ImportLineResult.ERROR;

				} else {

					String provinceName_ = values[i];

					String regex = "\\S+";
					boolean isNoSpaceMatch = provinceName_.matches(regex);

					if ( !isNoSpaceMatch) {
						writeImportError(values,
								new ImportErrorException(values[i], entityProperties[i]).getMessage()
										+ " | This cannot be empty or have white space.");
						return ImportLineResult.ERROR;
					} else {
						List<RegionDto> provinceNameList = FacadeProvider.getRegionFacade().getByName(provinceName_, true);

						if (provinceNameList.size() < 1) {
							provinceName = provinceName_;

						} else {
							if(isOverWrite){
								
								provinceName = provinceName_;
								return ImportLineResult.SUCCESS;

							}else {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage()
												+ " | Region Name Exists ");
								return ImportLineResult.ERROR;
							}
						}

					}

				}
			}
			
			
			if (RegionDto.EXTERNAL_ID.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {

					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | PCode Cannot Be Left Empty");
					return ImportLineResult.ERROR;

				} else {
					List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade()
							.getByExternalId(Long.parseLong(values[i]), false);
					
					if (provinces.size() > 0) {
						if (isOverWrite) {
							try {
								Long externalIdValue = Long.parseLong(values[i]);
								provinceExternalId = externalIdValue;
							} catch (NumberFormatException e) {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage());
								return ImportLineResult.ERROR;
							}
						}else {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage());
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


			
			if (RegionDto.AREA.equalsIgnoreCase(entityProperties[i])) {
				List<AreaReferenceDto> existingAreas = FacadeProvider.getAreaFacade()
						.getByExternalId(Long.parseLong(values[i]), false);
				System.out.println(existingAreas + " :++++++++++++++++++===============: " +existingAreas.size());
				if (existingAreas.size() != 1) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | Region Doesn't Exist");
					return ImportLineResult.ERROR;
				} else {
					try {
						Long externalIdValue = Long.parseLong(values[i]);
						areax = externalIdValue;
					} catch (NumberFormatException e) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
				}

			}

			

			

			final AreaReferenceDto finalArea = area;
			final String finalRProvincename = provinceName;
			final Long extd = provinceExternalId;

			List<RegionDto> newUserLinetoSave = new ArrayList<>();

			RegionDto newUserLine = RegionDto.build();

			System.out.println("++++++++++++++++existingPopulationData.NOTisPresent()++++++++++++++++ ");
			newUserLine.setArea(finalArea);
			newUserLine.setExternalId(extd);
			newUserLine.setName(finalRProvincename);

			boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
					new Function<ImportCellData, Exception>() {

						@Override
						public Exception apply(ImportCellData cellData) {
							System.out.println("++++++++++++++++111111111: " + cellData.getEntityPropertyPath()[0]);

							try {

								if (RegionDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setName(cellData.getValue());
								}
								if (RegionDto.EXTERNAL_ID.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									
									List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade()
											.getByExternalId(Long.parseLong(cellData.getValue()), false);
									if(provinces.size() > 0) {
										String [] ccc = {cellData.getValue()};
										
										try {
											writeImportError(ccc, new ImportErrorException( entityProperties[i]).getMessage() + "| PCode Exists");
										} catch (IOException e) {
											// TODO Auto-generated catch block
											
											e.printStackTrace();
										}
									}else {
										newUserLine.setExternalId(Long.parseLong(cellData.getValue()));
									}
								}
								if (RegionDto.AREA.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									Long externalId = Long.parseLong(cellData.getValue());
									List<AreaReferenceDto> areasz = FacadeProvider.getAreaFacade()
											.getByExternalId(externalId, false);
									if(areasz.size() == 0) {
										
										String [] ccc = {cellData.getValue()};
										
										try {
											writeImportError(ccc, new ImportErrorException( entityProperties[i]).getMessage() + "| Region does not exist");
										} catch (IOException e) {
											// TODO Auto-generated catch block
											
											e.printStackTrace();
										}
									}else {
										AreaReferenceDto areaReferenceDto = areasz.get(0);
										newUserLine.setArea(areaReferenceDto);
									}
									
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
					FacadeProvider.getRegionFacade().save(newUserLinetoSave.get(0), true);

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