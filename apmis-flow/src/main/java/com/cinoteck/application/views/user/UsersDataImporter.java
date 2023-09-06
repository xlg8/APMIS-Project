package com.cinoteck.application.views.user;

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
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.importexport.ValueSeparator;
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
public class UsersDataImporter extends DataImporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(UsersDataImporter.class);

	private final UserFacade userFacade;
	private UI currentUI;
	private Set<UserRole> userRole = new HashSet<>();

	// file_, true, userDto, campaignForm.getUuid(), campaignReferenceDto,
	// ValueSeparator.COMMA
	public UsersDataImporter(File inputFile, boolean hasEntityClassRow, UserDto currentUser,
			ValueSeparator csvSeparator) throws IOException {
		super(inputFile, hasEntityClassRow, currentUser, csvSeparator);
		this.userFacade = FacadeProvider.getUserFacade();
	}

	@Override
	public void startImport(Consumer<StreamResource> addErrorReportToLayoutCallback, Consumer<StreamResource> addCredentialReportToLayoutCallback, boolean isUserCreation, UI currentUI,
			boolean duplicatesPossible) throws IOException, CsvValidationException {

		this.currentUI = currentUI;
		super.startImport(addErrorReportToLayoutCallback, addCredentialReportToLayoutCallback, true, currentUI, duplicatesPossible);
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
		Set<CommunityReferenceDto> community = new HashSet<>();
		Set<FormAccess> formAccess= new HashSet<>();
		String username = "";

		System.out.println("++++++++++++++++++===============: " + entityProperties.length);

		// Retrieve the region and district from the database or throw an error if more
		// or less than one entry have been retrieved
		for (int i = 0; i < entityProperties.length; i++) {

			System.out.println(entityProperties[i] + " :++++++++++++++++++===============: " + i);
			if (UserDto.AREA.equalsIgnoreCase(entityProperties[i])) {
				try {
					List<AreaReferenceDto> areas = FacadeProvider.getAreaFacade()
							.getByExternalId(Long.parseLong(values[i]), false);
					if (areas.size() != 1) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
					area = areas.get(0);
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}
			if (UserDto.REGION.equalsIgnoreCase(entityProperties[i])) {
				try {
					List<RegionReferenceDto> regions = FacadeProvider.getRegionFacade()
							.getByExternalId(Long.parseLong(values[i]), false);
					if (regions.size() != 1) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
					region = regions.get(0);
				} catch (NumberFormatException e) {
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " : " + e.getLocalizedMessage());
					return ImportLineResult.ERROR;
				}
			}
			if (UserDto.DISTRICT.equalsIgnoreCase(entityProperties[i])) {
				if (DataHelper.isNullOrEmpty(values[i])) {
					district = null;
				} else {
					try {
						List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade()
								.getByExternalID(Long.parseLong(values[i]), region, false);
						if (districts.size() != 1) {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage());
							return ImportLineResult.ERROR;
						}
						district = districts.get(0);

					} catch (NumberFormatException e) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
								+ " : " + e.getLocalizedMessage());
						return ImportLineResult.ERROR;
					}
				}
			}

			if (UserDto.COMMUNITY.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {
					community = null;
				} else {
					String inputString = values[i];

					String[] cCodes = inputString.substring(1, inputString.length() - 1).split(",");

					for (int ix = 0; ix < cCodes.length; ix++) {
						
						try {
							List<CommunityReferenceDto> communities = FacadeProvider.getCommunityFacade().getByExternalID(Long.parseLong(cCodes[ix].trim()), district, false);
							
							if (communities.size() != 1) {
								writeImportError(values, new ImportErrorException(cCodes[ix].trim(), entityProperties[i]).getMessage());
								return ImportLineResult.ERROR;
							}
							for(CommunityReferenceDto ds : communities) {
							community.add(ds);
							}
							

						} catch (NumberFormatException e) {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage() + " : "
											+ e.getLocalizedMessage());
							return ImportLineResult.ERROR;
						}

					}

				}
			}

			if (UserDto.FORM_ACCESS.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {
					formAccess = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | This cannot be empty");
					return ImportLineResult.ERROR;
				} else {
					String inputString = values[i];

					String[] formAccess_ = inputString.substring(1, inputString.length() - 1).split(",");

					for (int ix = 0; ix < formAccess_.length; ix++) {
						String formAccessx = formAccess_[ix].trim();
						try {

							int iv = 0;

							for (FormAccess myEnum : FormAccess.values()) {
								if (myEnum.name().equalsIgnoreCase(formAccessx)) {
									iv++;
									formAccess.add(myEnum);
								}
							}

							if (iv < 1) {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage()
												+ " | This cannot be empty");
								return ImportLineResult.ERROR;
							}

						} catch (NumberFormatException e) {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage() + " : "
											+ e.getLocalizedMessage());
							return ImportLineResult.ERROR;
						}
					}

				}
			}

			if (UserDto.USER_NAME.equalsIgnoreCase(entityProperties[i])) {

				if (DataHelper.isNullOrEmpty(values[i])) {
					username = null;
					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage()
							+ " | This cannot be empty");
					return ImportLineResult.ERROR;

				} else {

					String username_ = values[i];

					String regex = "\\S+";
					boolean isNoSpaceMatch = username_.matches(regex);

					if (!((username_.length() > 4) && (username_.length() < 25)) && !isNoSpaceMatch) {
						writeImportError(values,
								new ImportErrorException(values[i], entityProperties[i]).getMessage()
										+ " | This cannot be empty or have white space and might be "
										+ Validations.textTooLong);
						return ImportLineResult.ERROR;
					} else {

						UserDto usrCheck = FacadeProvider.getUserFacade().getByUserName(username_);

						if (usrCheck == null) {
							username = username_;
							
						} else {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage()
											+ " | User exist ");
							return ImportLineResult.ERROR;
						}

					}

				}
			}
		}

			// The region and district that will be used to save the population data to the
			// database
			final AreaReferenceDto finalArea = area;
			final RegionReferenceDto finalRegion = region;
			final DistrictReferenceDto finalDistrict = district;
			final Set<CommunityReferenceDto> finalCommunity = community;
			final Set<FormAccess> finalFormAccess = formAccess;
			final String finalUsername = username;
			
			List<UserDto> newUserLinetoSave = new ArrayList<>();
			
			
			UserDto newUserLine = UserDto.build();
			
			System.out.println("++++++++++++++++existingPopulationData.NOTisPresent()++++++++++++++++ ");
			newUserLine.setArea(finalArea);
			newUserLine.setRegion(finalRegion);
			newUserLine.setDistrict(finalDistrict);
			newUserLine.setCommunity(finalCommunity);
			newUserLine.setUserName(finalUsername);
			//define logic to accept other userroles except for, mobile user
			userRole.add(UserRole.REST_USER);
			
			newUserLine.setUserRoles(userRole);
			newUserLine.setFormAccess(finalFormAccess);
			

			boolean usersDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
					new Function<ImportCellData, Exception>() {

						@Override
						public Exception apply(ImportCellData cellData) {
							System.out.println("++++++++++++++++111111111: "+cellData.getEntityPropertyPath()[0]);

							try {
								

									// Add the data from the currently processed cell to a new user object
							
										//pulling others that might not need major validation
										
										if (UserDto.FIRST_NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine.setFirstName(cellData.getValue());
										}
										if (UserDto.LAST_NAME.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine.setLastName(cellData.getValue());
										}
										if (UserDto.USER_ORGANISATION.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine.setUserOrganisation(cellData.getValue());
										}
										if (UserDto.USER_POSITION.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine.setUserPosition(cellData.getValue());
										}
										if (UserDto.USER_EMAIL.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine.setUserEmail(cellData.getValue());
										}
										if (UserDto.PHONE.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine.setPhone(cellData.getValue());
										}
										if (UserDto.COMMON_USER.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
										newUserLine.setCommomUser(false);
										}
										if (UserDto.LANGUAGE.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
											if(cellData.getValue() != null) {
												if(cellData.getValue().equalsIgnoreCase("pashto")) {
													newUserLine.setLanguage(Language.PS);
												} else if(cellData.getValue().equalsIgnoreCase("farsi")) {
													newUserLine.setLanguage(Language.FA);
												} else {
													newUserLine.setLanguage(Language.EN);
												}										
											}
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
				FacadeProvider.getUserFacade().saveUser(newUserLinetoSave.get(0));
				
				makeInitialPassword(newUserLinetoSave.get(0).getUuid(), newUserLinetoSave.get(0).getUserEmail(), values);
				
				return ImportLineResult.SUCCESS;
			} catch (ValidationRuntimeException | IOException e ) {
				writeImportError(values, e.getMessage());
				return ImportLineResult.ERROR;
			}
		} else {
			return ImportLineResult.ERROR;
		}
	}
	
	
	public void makeInitialPassword(String userUuid, String userEmail, String[] lineWorkingOn) throws IOException {
		if (StringUtils.isBlank(userEmail)
				|| AuthProvider.getProvider(FacadeProvider.getConfigFacade()).isDefaultProvider()) {
			String newPassword = FacadeProvider.getUserFacade().resetPassword(userUuid);
			
			writeUserCredentialLog(lineWorkingOn, newPassword);
			
		}
	}
	
	

	@Override
	protected boolean executeDefaultInvoke(PropertyDescriptor pd, Object element, String entry,
			String[] entryHeaderPath) throws InvocationTargetException, IllegalAccessException, ImportErrorException {

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
		return invokingSuccessful;
	}

	@Override
	protected String getErrorReportFileName() {
		return "user_Import_error_report.csv";
	}
}