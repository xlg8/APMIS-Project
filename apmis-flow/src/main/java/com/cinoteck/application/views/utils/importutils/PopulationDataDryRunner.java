package com.cinoteck.application.views.utils.importutils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import com.google.firebase.database.core.operation.Overwrite;

import de.symeda.sormas.api.AgeGroup;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.importexport.InvalidColumnException;
import de.symeda.sormas.api.importexport.ValueSeparator;
import de.symeda.sormas.api.infrastructure.PopulationDataCriteria;
import de.symeda.sormas.api.infrastructure.PopulationDataDryRunCriteria;
import de.symeda.sormas.api.infrastructure.PopulationDataDryRunDto;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.ValidationRuntimeException;

/**
 * Data importer that is used to import population data.
 */
public class PopulationDataDryRunner extends DataImporter {

	/**
	 * The pattern that entries in the header row must match in order for the
	 * importer to determine how to fill its entries.
	 */
	private static final String HEADER_PATTERN = "[A-Z]+_[A-Z]{3}_\\d+_(\\d+|PLUS)";

	/**
	 * The pattern that entries in the header row representing total counts must
	 * match in order for the importer to determine how to fill its entries.
	 */
	private static final String TOTAL_HEADER_PATTERN = "[A-Z]+_TOTAL";

	private boolean isOverWrite;
	private boolean isOverWriteEnabledCode;
	private final Date collectionDate;
	private final CampaignReferenceDto campaignReferenceDto;
	private final String dtoIdentifier;

	private static final String PROVINCE = "province";
	private static final String TOTAL_0_4 = "TOTAL_AGE_0_4";
	private static final String TOTAL_5_10 = "TOTAL_AGE_5_10";


	public PopulationDataDryRunner(File inputFile, UserDto currentUser, CampaignDto campaignDto,
			ValueSeparator csvSeparator, boolean overwrite) throws IOException {

		super(inputFile, false, currentUser, csvSeparator);
		this.collectionDate = new Date();
		this.isOverWrite = overwrite;
		this.campaignReferenceDto = FacadeProvider.getCampaignFacade().getReferenceByUuid(campaignDto.getUuid());
		this.dtoIdentifier = campaignDto.getUuid();
	}

	@Override
	protected ImportLineResult importDataFromCsvLine(String[] values, String[] entityClasses, String[] entityProperties,
			String[][] entityPropertyPaths, boolean firstLine)
			throws IOException, InvalidColumnException, InterruptedException, ConstraintViolationException ,PersistenceException  ,TransactionRolledbackLocalException, EJBTransactionRolledbackException {

		// Check whether the new line has the same length as the header line
		if (values.length > entityProperties.length) {
			writeImportError(values, I18nProperties.getValidationError(Validations.importLineTooLong));
			return ImportLineResult.ERROR;
		}

		// Reference population data that contains the region, district and community
		// for this line
		RegionReferenceDto region = null;
		DistrictReferenceDto district = null;
		CommunityReferenceDto community = null;
		CampaignReferenceDto campaigns_ = null;
		String modality_ = null;
		String districtStatus_ =  null;

//		System.out.println("++++++++++++++++++===============: "+entityProperties.length);

		// Retrieve the region and district from the database or throw an error if more
		// or less than one entry have been retrieved
		for (int i = 0; i < entityProperties.length; i++) {

//			if (isOverWrite) {
				if ( PROVINCE.equalsIgnoreCase(entityProperties[i])) {
					List<RegionReferenceDto> regions = FacadeProvider.getRegionFacade()
							.getByExternalId(Long.parseLong(values[i]), false);
					if (regions.size() != 1) {
						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
						return ImportLineResult.ERROR;
					}
					region = regions.get(0);
				}
				if (PopulationDataDto.DISTRICT.equalsIgnoreCase(entityProperties[i])) {
					if (DataHelper.isNullOrEmpty(values[i])) {
						district = null;
					} else {
						List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade()
								.getByExternalID(Long.parseLong(values[i]), region, false);
						if (districts.size() != 1) {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage());
							return ImportLineResult.ERROR;
						}
						district = districts.get(0);
					}
				}
				
				// patch to use cluster No for data import

				if (PopulationDataDto.COMMUNITY_EXTID.equalsIgnoreCase(entityProperties[i])) {
					if (DataHelper.isNullOrEmpty(values[i])) {
						community = null;
					} else {
						if (isLong(values[i])) {
							List<CommunityReferenceDto> communities = FacadeProvider.getCommunityFacade()
									.getByExternalId(Long.parseLong(values[i]), false);
							if (communities.size() != 1) {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage());
//							System.out.println(new ImportErrorException(values[i], entityProperties[i]).getMessage());
								return ImportLineResult.ERROR;
							}
							community = communities.get(0);
						} else {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage());
//						System.out.println(new ImportErrorException(values[i], entityProperties[i]).getMessage() +" ttttttttttttttttt 1111"+values[i]);
							return ImportLineResult.ERROR;
						}
					}
				}

				// fixing importing campaign population based patch
				if (PopulationDataDto.CAMPAIGN.equalsIgnoreCase(entityProperties[i])) {
					if (DataHelper.isNullOrEmpty(values[i])) {
						campaigns_ = FacadeProvider.getCampaignFacade().getReferenceByUuid(dtoIdentifier);
					} else {

						if (values[i].toString().length() > 28 && values[i].toString().contains("-")) {
							campaigns_ = FacadeProvider.getCampaignFacade().getReferenceByUuid(values[i]);

							if (campaigns_ == null) {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage());
								// System.out.println("~~~~~~~~~~~~~~~~~~~````"+new
								// ImportErrorException(values[i], entityProperties[i]).getMessage());
								return ImportLineResult.ERROR;
							}

							// check if data matched campaign
							if (!campaigns_.getUuid().equals(campaignReferenceDto.getUuid())) {
								writeImportError(values, campaigns_ + " Campaign mismatched");
								// writeImportError(values, new ImportErrorException(values[i], "Capaign not
								// matched");
								return ImportLineResult.ERROR;
							}

						} else {
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage());
							// System.out.println("~!~!~!~!~!~!@!@!~ "+new ImportErrorException(values[i],
							// entityProperties[i]).getMessage() +" ttttttttttttttttt 1111"+values[i]);
							return ImportLineResult.ERROR;
						}
					}
				}
				
				
				
				
				
				
				if(isOverWrite) {
					if (PopulationDataDto.MODALITY.equalsIgnoreCase(entityProperties[i])) {
						if (DataHelper.isNullOrEmpty(values[i])) {
							
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage() + " Cannot be empty");
							// System.out.println("~~~~~~~~~~~~~~~~~~~````"+new
							// ImportErrorException(values[i], entityProperties[i]).getMessage());
							return ImportLineResult.ERROR;
//							modality_ = "H2H";
						} else {

							if (values[i].toString() != "" || values[i].toString() != null) {
								modality_ = values[i];

								if (modality_ == null) {
									
									
									writeImportError(values,
											new ImportErrorException(values[i], entityProperties[i]).getMessage());
									// System.out.println("~~~~~~~~~~~~~~~~~~~````"+new
									// ImportErrorException(values[i], entityProperties[i]).getMessage());
									return ImportLineResult.ERROR;
								}

							} else {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage());
								return ImportLineResult.ERROR;
							}
						}
					}
					
					
					
					if (PopulationDataDto.DISTRICT_STATUS.equalsIgnoreCase(entityProperties[i])) {
						
						if (DataHelper.isNullOrEmpty(values[i])) {
							
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage() + " Cannot be empty");
							// System.out.println("~~~~~~~~~~~~~~~~~~~````"+new
							// ImportErrorException(values[i], entityProperties[i]).getMessage());
							return ImportLineResult.ERROR;
//							districtStatus_ = "Full District";
						} else {

							if (values[i].toString() != "" || values[i].toString() != null) {
								districtStatus_ = values[i];

								if (districtStatus_ == null) {
									writeImportError(values,
											new ImportErrorException(values[i], entityProperties[i]).getMessage());
									// System.out.println("~~~~~~~~~~~~~~~~~~~````"+new
									// ImportErrorException(values[i], entityProperties[i]).getMessage());
									return ImportLineResult.ERROR;
								}

							} else {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage());
								return ImportLineResult.ERROR;
							}
						}
					}
					
					if (TOTAL_0_4.equalsIgnoreCase(entityProperties[i])) {
						if (DataHelper.isNullOrEmpty(values[i])) {
//							districtStatus_ = "Full District";
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage() + " POPULATION DATA CANNOT BE LEFT EMPTY");
							return ImportLineResult.ERROR;
						} else {

				
						}
					}
					
					if (TOTAL_5_10.equalsIgnoreCase(entityProperties[i])) {
						if (DataHelper.isNullOrEmpty(values[i])) {
//							districtStatus_ = "Full District";
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage() + " POPULATION DATA CANNOT BE LEFT EMPTY");
							return ImportLineResult.ERROR;
						} else {

				
						}
					}
					
				}else {
					if (PopulationDataDto.MODALITY.equalsIgnoreCase(entityProperties[i])) {
						if (DataHelper.isNullOrEmpty(values[i])) {
//							modality_ = "H2H";
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage());
							return ImportLineResult.ERROR;
						} else {

							if (values[i].toString() != "" || values[i].toString() != null) {
								modality_ = values[i];

								if (modality_ == null) {
									
									
									writeImportError(values,
											new ImportErrorException(values[i], entityProperties[i]).getMessage());
									// System.out.println("~~~~~~~~~~~~~~~~~~~````"+new
									// ImportErrorException(values[i], entityProperties[i]).getMessage());
									return ImportLineResult.ERROR;
								}

							} else {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage());
								return ImportLineResult.ERROR;
							}
						}
					}
					
					
					if (PopulationDataDto.DISTRICT_STATUS.equalsIgnoreCase(entityProperties[i])) {
						
						if (DataHelper.isNullOrEmpty(values[i])) {
//							districtStatus_ = "Full District";
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage());
							return ImportLineResult.ERROR;
						} else {

							if (values[i].toString() != "" || values[i].toString() != null) {
								districtStatus_ = values[i];

								if (districtStatus_ == null) {
									writeImportError(values,
											new ImportErrorException(values[i], entityProperties[i]).getMessage());
									// System.out.println("~~~~~~~~~~~~~~~~~~~````"+new
									// ImportErrorException(values[i], entityProperties[i]).getMessage());
									return ImportLineResult.ERROR;
								}

							} else {
								writeImportError(values,
										new ImportErrorException(values[i], entityProperties[i]).getMessage());
								return ImportLineResult.ERROR;
							}
						}
					}
					
					if (TOTAL_0_4.equalsIgnoreCase(entityProperties[i])) {
						if (DataHelper.isNullOrEmpty(values[i])) {
//							districtStatus_ = "Full District";
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage() + " POPULATION DATA CANNOT BE LEFT EMPTY");
							return ImportLineResult.ERROR;
						} else {

				
						}
					}
					
					if (TOTAL_5_10.equalsIgnoreCase(entityProperties[i])) {
						if (DataHelper.isNullOrEmpty(values[i])) {
//							districtStatus_ = "Full District";
							writeImportError(values,
									new ImportErrorException(values[i], entityProperties[i]).getMessage() + " POPULATION DATA CANNOT BE LEFT EMPTY");
							return ImportLineResult.ERROR;
						} else {

				
						}
					}
				}
				
				
				
				
		
				
				
//			} 	

//			else {
//				if (PROVINCE.equalsIgnoreCase(entityProperties[i])) {
//					List<RegionReferenceDto> regions = FacadeProvider.getRegionFacade()
//							.getByExternalId(Long.parseLong(values[i]), false);
//					if (regions.size() != 1) {
//						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//						return ImportLineResult.ERROR;
//					}
//					region = regions.get(0);
//				}
//				if (PopulationDataDto.DISTRICT.equalsIgnoreCase(entityProperties[i])) {
//					if (DataHelper.isNullOrEmpty(values[i])) {
//						district = null;
//					} else {
//						List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade()
//								.getByExternalID(Long.parseLong(values[i]), region, false);
//						if (districts.size() != 1) {
//							writeImportError(values,
//									new ImportErrorException(values[i], entityProperties[i]).getMessage());
//							return ImportLineResult.ERROR;
//						}
//						district = districts.get(0);
//					}
//				}
//
//				// patch to use cluster No for data import
//
//				if (PopulationDataDto.COMMUNITY_EXTID.equalsIgnoreCase(entityProperties[i])) {
//					if (DataHelper.isNullOrEmpty(values[i])) {
//						community = null;
//					} else {
//						if (isLong(values[i])) {
//							List<CommunityReferenceDto> communities = FacadeProvider.getCommunityFacade()
//									.getByExternalId(Long.parseLong(values[i]), false);
//							if (communities.size() != 1) {
//								writeImportError(values,
//										new ImportErrorException(values[i], entityProperties[i]).getMessage());
////							System.out.println(new ImportErrorException(values[i], entityProperties[i]).getMessage());
//								return ImportLineResult.ERROR;
//							}
//							community = communities.get(0);
//						} else {
//							writeImportError(values,
//									new ImportErrorException(values[i], entityProperties[i]).getMessage());
////						System.out.println(new ImportErrorException(values[i], entityProperties[i]).getMessage() +" ttttttttttttttttt 1111"+values[i]);
//							return ImportLineResult.ERROR;
//						}
//					}
//				}
//
//				// fixing importing campaign population based patch
//				if (PopulationDataDto.CAMPAIGN.equalsIgnoreCase(entityProperties[i])) {
//					if (DataHelper.isNullOrEmpty(values[i])) {
//						campaigns_ = FacadeProvider.getCampaignFacade().getReferenceByUuid(dtoIdentifier);
//					} else {
//
//						if (values[i].toString().length() > 28 && values[i].toString().contains("-")) {
//							campaigns_ = FacadeProvider.getCampaignFacade().getReferenceByUuid(values[i]);
//
//							if (campaigns_ == null) {
//								writeImportError(values,
//										new ImportErrorException(values[i], entityProperties[i]).getMessage());
//								// System.out.println("~~~~~~~~~~~~~~~~~~~````"+new
//								// ImportErrorException(values[i], entityProperties[i]).getMessage());
//								return ImportLineResult.ERROR;
//							}
//
//							// check if data matched campaign
//							if (!campaigns_.getUuid().equals(campaignReferenceDto.getUuid())) {
//								writeImportError(values, campaigns_ + " Campaign mismatched");
//								// writeImportError(values, new ImportErrorException(values[i], "Capaign not
//								// matched");
//								return ImportLineResult.ERROR;
//							}
//							
//
//			                if (district != null && campaigns_.getUuid() != null) {
//			                    // Check for duplicate key
////			                    boolean duplicateExists = FacadeProvider.getPopulationDataDryRunFacade()
////			                            .checkDuplicatePopulationData(district.getUuid(), "AGE_GROUP", campaigns_.getUuid());
////			                    if (duplicateExists) {
////			                        writeImportError(values, "Duplicate key value violates unique constraint for district_id, agegroup, campaign_id.");
////			                        return ImportLineResult.ERROR;
////			                    }
//
//			                    Integer districtPopulation =
//			                            FacadeProvider.getPopulationDataDryRunFacade().getDistrictPopulationByUuidAndAgeGroup(
//			                                    district.getUuid(), campaigns_.getUuid(), values[i]);
//
//			                    if (districtPopulation == null) {
//			                        writeImportError(values, campaigns_ + " Campaign mismatched with District");
//			                        return ImportLineResult.ERROR;
//			                    }
//			                }
//
//						} else {
//							writeImportError(values,
//									new ImportErrorException(values[i], entityProperties[i]).getMessage());
//							// System.out.println("~!~!~!~!~!~!@!@!~ "+new ImportErrorException(values[i],
//							// entityProperties[i]).getMessage() +" ttttttttttttttttt 1111"+values[i]);
//							return ImportLineResult.ERROR;
//						}
//					}
//				}
//
//				if (PopulationDataDto.MODALITY.equalsIgnoreCase(entityProperties[i])) {
//					if (DataHelper.isNullOrEmpty(values[i])) {
//						modality_ = "H2H";
//					} else {
//
//						if (values[i].toString() != "" || values[i].toString() != null) {
//							modality_ = values[i];
//
//							if (modality_ == null) {
//								
//								
//								writeImportError(values,
//										new ImportErrorException(values[i], entityProperties[i]).getMessage());
//								// System.out.println("~~~~~~~~~~~~~~~~~~~````"+new
//								// ImportErrorException(values[i], entityProperties[i]).getMessage());
//								return ImportLineResult.ERROR;
//							}
//
//						} else {
//							writeImportError(values,
//									new ImportErrorException(values[i], entityProperties[i]).getMessage());
//							return ImportLineResult.ERROR;
//						}
//					}
//				}
//
//				if (PopulationDataDto.DISTRICT_STATUS.equalsIgnoreCase(entityProperties[i])) {
//					if (DataHelper.isNullOrEmpty(values[i])) {
//						districtStatus_ = "Full District";
//					} else {
//
//						if (values[i].toString() != "" || values[i].toString() != null) {
//							districtStatus_ = values[i];
//
//							if (districtStatus_ == null) {
//								writeImportError(values,
//										new ImportErrorException(values[i], entityProperties[i]).getMessage());
//								// System.out.println("~~~~~~~~~~~~~~~~~~~````"+new
//								// ImportErrorException(values[i], entityProperties[i]).getMessage());
//								return ImportLineResult.ERROR;
//							}
//
//						} else {
//							writeImportError(values,
//									new ImportErrorException(values[i], entityProperties[i]).getMessage());
//							return ImportLineResult.ERROR;
//						}
//					}
//				}
//				
//
//				
//
//			}


		}
//	

		// The region and district that will be used to save the population data to the
		// database
		final RegionReferenceDto finalRegion = region;
		final DistrictReferenceDto finalDistrict = district;
		final CommunityReferenceDto finalCommunity = community;
		final CampaignReferenceDto finalCampaign = campaigns_;

		final String modality = modality_;
		final String districtStatus = districtStatus_;

		// Retrieve the existing population data for the region and district
		PopulationDataDryRunCriteria criteria = new PopulationDataDryRunCriteria().region(finalRegion);
		// criteria.setCampaign(finalCampaign);
		if (finalCampaign == null) {
			criteria.campaignIsNull(true);
		} else {
			criteria.campaign(finalCampaign);
		}

		if (finalDistrict == null) {
			criteria.districtIsNull(true);
		} else {
			criteria.district(finalDistrict);
		}

		if (finalCommunity == null) {
			criteria.communityIsNull(true);
		} else {
			criteria.community(finalCommunity);
		}

		if (modality == null) {
			criteria.modalityIsNull(true);
		} else {
			criteria.setModality(modality);
		}

		if (districtStatus == null) {
			criteria.districtStatusIsNull(true);
		} else {
			criteria.setDistrictStatus(districtStatus);
		}

		List<PopulationDataDryRunDto> existingPopulationDataList = FacadeProvider.getPopulationDataDryRunFacade()
				.getPopulationDataImportChecker(criteria);

		List<PopulationDataDryRunDto> modifiedPopulationDataList = new ArrayList<PopulationDataDryRunDto>();

		boolean populationDataHasImportError = false;
		if (isOverWrite) {
			populationDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
					new Function<ImportCellData, Exception>() {

						@Override
						public Exception apply(ImportCellData cellData) {
//							System.out.println("++++++++++++++++111111111111111111111111++++++++++++++++ ");

							try {
								if (PROVINCE.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])
										|| PopulationDataDto.DISTRICT
												.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])
										|| PopulationDataDto.CAMPAIGN
												.equalsIgnoreCase(cellData.getEntityPropertyPath()[0]) // Property
																										// type//
																										// allowed
										|| PopulationDataDto.COMMUNITY_EXTID
												.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])
										|| PopulationDataDto.MODALITY
												.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])
										|| PopulationDataDto.DISTRICT_STATUS
												.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {

								} else {

									// Add the data from the currently processed cell to a new population data
									// object
									PopulationDataDryRunDto newPopulationData = PopulationDataDryRunDto.build(collectionDate);

									newPopulationData.setCampaign(finalCampaign);
									newPopulationData.setDistrict(finalDistrict);

									insertCellValueIntoData(newPopulationData, cellData.getValue(),
											cellData.getEntityPropertyPath());


									Optional<PopulationDataDryRunDto> existingPopulationData = existingPopulationDataList
											.stream()
											.filter(populationData -> populationData.getAgeGroup()
													.equals(newPopulationData.getAgeGroup())
													&& populationData.getCampaign()
															.equals(newPopulationData.getCampaign())
													&& populationData.getDistrict()
															.equals(newPopulationData.getDistrict()))
											.findFirst();

									Optional<PopulationDataDryRunDto> existingPopulationDatax = existingPopulationDataList
											.stream().filter(populationData -> populationData.getAgeGroup()
													.equals(newPopulationData.getAgeGroup()))
											.findFirst();

									System.out.println(" ---======+++++++++++++:X " + existingPopulationData + "hhhhh"
											+ existingPopulationDataList);

									if (existingPopulationData.isPresent()) {
										System.out.println(
												"++++++++++++++++existingPopulationData.isPresent()++++++++++++++++ ");
										existingPopulationData.get().setPopulation(newPopulationData.getPopulation());
										existingPopulationData.get().setCollectionDate(collectionDate);
//										existingPopulationData.setModality(moda);

										modifiedPopulationDataList.add(existingPopulationData.get());
									} else {
										System.out.println(
												"++++++++++++++++existingPopulationData.NOTisPresent()++++++++++++++++ ");

										newPopulationData.setRegion(finalRegion);
										newPopulationData.setDistrict(finalDistrict);
										newPopulationData.setCommunity(finalCommunity);
										newPopulationData.setCampaign(finalCampaign);
										newPopulationData.setModality(modality);
										newPopulationData.setDistrictStatus(districtStatus);

										modifiedPopulationDataList.add(newPopulationData);
									}
								}
							} catch (ImportErrorException | InvalidColumnException | NumberFormatException e) {

								return e;
							}

							return null;
						}
					});
			
		} else {
			populationDataHasImportError = insertRowIntoData(values, entityClasses, entityPropertyPaths, false,
					new Function<ImportCellData, Exception>() {

						@Override
						public Exception apply(ImportCellData cellData) {
//							System.out.println("++++++++++++++++111111111111111111111111++++++++++++++++ ");

							try {
								if (PROVINCE.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])
										|| PopulationDataDto.DISTRICT
												.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])
										|| PopulationDataDto.CAMPAIGN
												.equalsIgnoreCase(cellData.getEntityPropertyPath()[0]) // Property//
																										// allowed
										|| PopulationDataDto.COMMUNITY_EXTID
												.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])
										|| PopulationDataDto.MODALITY
												.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])
										|| PopulationDataDto.DISTRICT_STATUS
												.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
								} else {

									// Add the data from the currently processed cell to a new population data
									// object
									PopulationDataDryRunDto newPopulationData = PopulationDataDryRunDto.build(collectionDate);
									newPopulationData.setCampaign(finalCampaign);
									insertCellValueIntoData(newPopulationData, cellData.getValue(),
											cellData.getEntityPropertyPath());
									
									System.out.println(newPopulationData.getAgeGroup() + "   :++++++++gggg+++++:Y "
											+ cellData.getValue() + "  kkkkkkkkkkk  " + cellData.getEntityPropertyPath());
									
									System.out.println(newPopulationData.getAgeGroup() + "   :+++++++++++++:Y "
											+ existingPopulationDataList.size());


									Optional<PopulationDataDryRunDto> existingPopulationData = existingPopulationDataList
										    .stream()
										    .filter(populationData -> {
										        boolean ageGroupMatches = newPopulationData.getAgeGroup() == null || 
										                                  populationData.getAgeGroup().equals(newPopulationData.getAgeGroup());
										        boolean campaignMatches = populationData.getCampaign().equals(newPopulationData.getCampaign());
										        boolean districtMatches = populationData.getDistrict().equals(newPopulationData.getDistrict());
										        return ageGroupMatches && campaignMatches && districtMatches;
										    })
										    .findFirst();


									if (existingPopulationData.isPresent()) {
										System.out.println(
												"++++++++++++++++existingPopulationData.isPresent()++++++++++++++++ ");
										existingPopulationData.get().setPopulation(newPopulationData.getPopulation());
										existingPopulationData.get().setCollectionDate(collectionDate);
//										existingPopulationData.setModality();

										modifiedPopulationDataList.add(existingPopulationData.get());
									} else {
										System.out.println(
												"++++++++++++++++existingPopulationData.NOTisPresent()++++++++++++++++ ");

										newPopulationData.setRegion(finalRegion);
										newPopulationData.setDistrict(finalDistrict);
										newPopulationData.setCommunity(finalCommunity);
										newPopulationData.setCampaign(finalCampaign);
										newPopulationData.setModality(modality);
										newPopulationData.setDistrictStatus(districtStatus);

										modifiedPopulationDataList.add(newPopulationData);
									}
								}
							} catch (ImportErrorException | InvalidColumnException | NumberFormatException e) {

								return e;
							}

							return null;
						}
					});
		}

		// Validate and save the population data object into the database if the import
		// has no errors
		if (!populationDataHasImportError) {
			System.out.println("++++++++++++++++0000000000000000NO Errror 000000000000000000000++++++++++++++++ "
					+ modifiedPopulationDataList.size());

			try {
				FacadeProvider.getPopulationDataDryRunFacade().savePopulationData(modifiedPopulationDataList);
				return ImportLineResult.SUCCESS;
			} catch (ValidationRuntimeException e) {
//				writeImportError(values, e.getMessage());
				return ImportLineResult.ERROR;
			}
			catch (ConstraintViolationException exx) {
				writeImportError(values,
						 "Check to ensure PCode and DCode Match and Campaign UUID is correct" +exx.getMessage());
//				writeImportError(values, e.getMessage());
				return ImportLineResult.ERROR;
			}
			catch (Exception exx) {
				writeImportError(values,
						 "Data Cannot Be Saved Please Check The Fields to be sure they're correct" +exx.getMessage());
//				writeImportError(values, e.getMessage());
				return ImportLineResult.ERROR;
			}
		} else {
			return ImportLineResult.ERROR;
		}
	}

	private static boolean isLong(String str) {

		try {
			@SuppressWarnings("unused")
			int x = Integer.parseInt(str);
			return true; // String is an Integer
		} catch (NumberFormatException e) {
			return false; // String is not an Integer
		}

	}

	/**
	 * Inserts the entry of a single cell into the population data object. Checks
	 * whether the entity property accords to one of the patterns defined in this
	 * class and sets the according sex and age group to the population data object.
	 */
	private void insertCellValueIntoData(PopulationDataDryRunDto populationData, String value, String[] entityPropertyPaths)
			throws InvalidColumnException, ImportErrorException {
		String entityProperty = buildEntityProperty(entityPropertyPaths);

		if (entityPropertyPaths.length != 1) {
			throw new UnsupportedOperationException(I18nProperties.getValidationError(
					Validations.importPropertyTypeNotAllowed, buildEntityProperty(entityPropertyPaths)));
		}

		String entityPropertyPath = entityPropertyPaths[0];

		try {
			if (entityPropertyPath.equalsIgnoreCase("TOTAL")) {
				insertPopulationIntoPopulationData(populationData, value);

				System.out.println(populationData + "header string " + value);
			} else if (entityPropertyPath.matches(TOTAL_HEADER_PATTERN)) {
				try {
					populationData.setSex(
							Sex.valueOf(entityPropertyPaths[0].substring(0, entityPropertyPaths[0].indexOf("_"))));
				} catch (IllegalArgumentException e) {
					throw new InvalidColumnException(entityProperty);
				}
				insertPopulationIntoPopulationData(populationData, value);
			} else if (entityPropertyPath.matches(HEADER_PATTERN)) {
				// Sex
				String sexString = entityPropertyPath.substring(0, entityPropertyPaths[0].indexOf("_"));
				if (!sexString.equals("TOTAL")) {
					try {
						populationData.setSex(Sex.valueOf(sexString));

						System.out.println(sexString + "header niot totsl string ");
					} catch (IllegalArgumentException e) {
						throw new InvalidColumnException(entityProperty);
					}
				}

				// Age group
				String ageGroupString = entityPropertyPath.substring(entityPropertyPath.indexOf("_") + 1,
						entityPropertyPaths[0].length());

				//
				try {

					populationData.setAgeGroup(AgeGroup.valueOf(ageGroupString));

					System.out.println(ageGroupString + "Agegroup string ");
				} catch (IllegalArgumentException e) {
					throw new InvalidColumnException(entityProperty);
				}

				insertPopulationIntoPopulationData(populationData, value);
			} else {
				throw new ImportErrorException(I18nProperties
						.getValidationError(Validations.importPropertyTypeNotAllowed, entityPropertyPath));
			}
		} catch (IllegalArgumentException e) {
			throw new ImportErrorException(value, entityProperty);
		} catch (ImportErrorException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unexpected error when trying to import population data: " + e.getMessage());
			throw new ImportErrorException(I18nProperties.getValidationError(Validations.importUnexpectedError));
		}
	}

	private void insertPopulationIntoPopulationData(PopulationDataDryRunDto populationData, String entry)
			throws ImportErrorException {
		try {
			populationData.setPopulation(Integer.parseInt(entry));
		} catch (NumberFormatException e) {
			throw new ImportErrorException(e.getMessage());
		}
	}
}