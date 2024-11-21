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
import org.springframework.beans.factory.annotation.Autowired;

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
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.importexport.ValueSeparator;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaFacade;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
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
public class AreaDataImporter extends DataImporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AreaDataImporter.class);

	private final AreaFacade areaFacade;
	private UI currentUI;
	private static final String HEADER_PATTERN = "[A-Z]+_[A-Z]{3}_\\d+_(\\d+|PLUS)";
	private final AreaReferenceDto areaReferenceDto;
	private boolean isOverWrite;
	private boolean isOverWriteEnabledCode;
	List<AreaReferenceDto> areas = new ArrayList<>();
	List<AreaReferenceDto> araeNameList = new ArrayList<>();
	UserProvider userProvider = new UserProvider();
	LocalDate localDate = LocalDate.now();
	Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());


	// file_, true, userDto, campaignForm.getUuid(), campaignReferenceDto,
	// ValueSeparator.COMMA
	public AreaDataImporter(File inputFile, boolean hasEntityRowClass, UserDto userDto, AreaDto currentUser,
			ValueSeparator csvSeparator, boolean overwrite) throws IOException {
		super(inputFile, false, userDto, csvSeparator);
		this.areaFacade = FacadeProvider.getAreaFacade();
		this.isOverWrite = overwrite;
		this.areaReferenceDto = FacadeProvider.getAreaFacade().getAreaReferenceByUuid(currentUser.getUuid());

	}

	@Autowired
	public ImportAreaDataDialog importDataDialog = new ImportAreaDataDialog();

	@Override
	public void startImport(File file_ ,Consumer<StreamResource> addErrorReportToLayoutCallback,
			Consumer<StreamResource> addCredentialReportToLayoutCallback, boolean isUserCreation, UI currentUI,
			boolean duplicatesPossible) throws IOException, CsvValidationException {

		this.currentUI = currentUI;
		super.startImport(file_, addErrorReportToLayoutCallback, addCredentialReportToLayoutCallback, false, currentUI,
				duplicatesPossible);
	}

	@Override
	protected ImportLineResult importDataFromCsvLine(String[] values, String[] entityClasses, String[] entityProperties,
			String[][] entityPropertyPaths, boolean firstLine) throws IOException, InterruptedException {

		if (values.length > entityProperties.length) {
			writeImportError(values, I18nProperties.getValidationError(Validations.importLineTooLong));
			return ImportLineResult.ERROR;
		}

		// Lets run some validations
		Long areaExternalId = null;
		String regionName = "";
		
		List<AreaDto> combinedList = new ArrayList();

		for (int i = 0; i < entityProperties.length; i++) {
			if (AreaDto.NAME.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {

					regionName = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | This cannot be empty");
					return ImportLineResult.ERROR;

				} else {

					String regionName_ = values[i];

					String regex = "\\S+";
					boolean isNoSpaceMatch = regionName_.matches(regex);

					if (!isNoSpaceMatch) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " | This cannot be empty or have white space. ");
						return ImportLineResult.ERROR;
					} else {
						araeNameList = FacadeProvider.getAreaFacade().getByName(regionName_, true);

						if (araeNameList.size() < 1) {// (usrCheck == null) {
							regionName = regionName_;

						} else {
//							if(isOverWrite){
//								regionName = regionName_;
//								isOverWriteEnabledName = true;
							// return ImportLineResult.SUCCESS;

//							} else {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage()
											+ " | Region Name Exists ");
							return ImportLineResult.ERROR;
//							}

						}

					}

				}
			}

			if (AreaDto.EXTERNAL_ID.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {

					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | RCode Cannot Be Left Empty");
					return ImportLineResult.ERROR;

				} else {
					areas = FacadeProvider.getAreaFacade().getByExternalId(Long.parseLong(values[i]), false);
					if (areas.size() > 0) {
						if (isOverWrite) {
							try {

								Long externalIdValue = Long.parseLong(values[i]);
								areaExternalId = externalIdValue;
								isOverWriteEnabledCode = true;
							} catch (NumberFormatException e) {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage());
								return ImportLineResult.ERROR;
							}
						} else {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage()
											+ " | This RCode Exists");
							return ImportLineResult.ERROR;
						}

					} else if (areas.size() == 0) {
						try {
							Long externalIdValue = Long.parseLong(values[i]);
							areaExternalId = externalIdValue;
						} catch (NumberFormatException e) {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage());
							return ImportLineResult.ERROR;
						}
					}
				}

			}

		}

		final long finalRegionExternalId = areaExternalId;
		final String finalRegionName = regionName;
		List<AreaDto> newUserLinetoSave = new ArrayList<>();

		AreaDto newUserLine = AreaDto.build();

		newUserLine.setExternalId(finalRegionExternalId);
		newUserLine.setName(finalRegionName);

		if (isOverWrite && isOverWriteEnabledCode) {

			AreaDto newUserLine_ = FacadeProvider.getAreaFacade().getByUuid(areas.get(0).getUuid());

			boolean isSameData = checkSameInstances(areas, araeNameList);
			if (isSameData) {
				return ImportLineResult.SUCCESS;
			}

			boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
					new Function<ImportCellData, Exception>() {

						@Override
						public Exception apply(ImportCellData cellData) {

							try {

								if (AreaDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine_.setName(cellData.getValue());
								}
//								if (AreaDto.EXTERNAL_ID.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
//									newUserLine.setExternalId(Long.parseLong(cellData.getValue()));// LastName(cellData.getValue());
//								}

								newUserLinetoSave.add(newUserLine_);

							} catch (NumberFormatException e) {
								return e;
							}

							return null;
						}
					});

			if (!usersDataHasImportError) {

				try {
					FacadeProvider.getAreaFacade().save(newUserLinetoSave.get(0));

					return ImportLineResult.SUCCESS;
				} catch (ValidationRuntimeException e) {
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

							try {

								if (AreaDto.NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setName(cellData.getValue());
								}
								if (AreaDto.EXTERNAL_ID.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
									newUserLine.setExternalId(Long.parseLong(cellData.getValue()));// LastName(cellData.getValue());
								}

								newUserLinetoSave.add(newUserLine);

							} catch (NumberFormatException e) {
								return e;
							}

							return null;
						}
					});

			if (!usersDataHasImportError) {
				boolean checkExeption = false;
				combinedList.add(newUserLinetoSave.get(0));
				
				initiateImportConfirmationDialog();
				
				try {
					FacadeProvider.getAreaFacade().save(newUserLinetoSave.get(0));

					return ImportLineResult.SUCCESS;
				} catch (ValidationRuntimeException e) {
					checkExeption = true;
					writeImportError(values, e.getMessage());
					return ImportLineResult.ERROR;
				} finally {
					if (!checkExeption) {
						
						ConfigurationChangeLogDto configurationChangeLogDto = new ConfigurationChangeLogDto();

						for (AreaDto  areaData : newUserLinetoSave){
							
							configurationChangeLogDto.setCreatinguser(userProvider.getUser().getUserName());
							configurationChangeLogDto.setAction_unit_type("Region");
							configurationChangeLogDto.setAction_unit_name(areaData.getName());
							configurationChangeLogDto.setUnit_code(areaData.getExternalId());
							configurationChangeLogDto.setAction_date(date);

							if(isOverWrite) {
								configurationChangeLogDto.setAction_logged("Import : Overwrite");

							}else {
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
	
	public void initiateImportConfirmationDialog() {
		
	}

	public static boolean checkSameInstances(List<AreaReferenceDto> list1, List<AreaReferenceDto> list2) {
		if (list1.size() != list2.size()) {
			return false;
		}

		for (AreaReferenceDto model : list1) {
			if (!list2.contains(model)) {
				return false;
			}
		}
		return true;
	}

	public void makeInitialPassword(String userUuid, String userEmail, String[] lineWorkingOn) throws IOException {
		if (StringUtils.isBlank(userEmail)
				|| AuthProvider.getProvider(FacadeProvider.getConfigFacade()).isDefaultProvider()) {
			String newPassword = FacadeProvider.getUserFacade().resetPassword(userUuid);

			writeUserCredentialLog(lineWorkingOn, newPassword);

		}
	}

	@Override
	protected String getErrorReportFileName() {
		return "region_Import_error_report.csv";
	}
}