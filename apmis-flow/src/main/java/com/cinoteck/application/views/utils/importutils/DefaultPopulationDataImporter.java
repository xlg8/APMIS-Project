//package com.cinoteck.application.views.utils.importutils;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Optional;
//import java.util.function.Function;
//
//import de.symeda.sormas.api.AgeGroup;
//import de.symeda.sormas.api.FacadeProvider;
//import de.symeda.sormas.api.campaign.CampaignDto;
//import de.symeda.sormas.api.campaign.CampaignReferenceDto;
//import de.symeda.sormas.api.i18n.I18nProperties;
//import de.symeda.sormas.api.i18n.Validations;
//import de.symeda.sormas.api.importexport.InvalidColumnException;
//import de.symeda.sormas.api.importexport.ValueSeparator;
//import de.symeda.sormas.api.infrastructure.PopulationDataCriteria;
//import de.symeda.sormas.api.infrastructure.PopulationDataDto;
//import de.symeda.sormas.api.person.Sex;
//import de.symeda.sormas.api.infrastructure.community.CommunityDto;
//import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
//import de.symeda.sormas.api.infrastructure.district.DistrictDto;
//import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
//import de.symeda.sormas.api.infrastructure.region.RegionDto;
//import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
//import de.symeda.sormas.api.user.UserDto;
//import de.symeda.sormas.api.utils.DataHelper;
//import de.symeda.sormas.api.utils.ValidationRuntimeException;
//
///**
// * Data importer that is used to import population data.
// */
//public class DefaultPopulationDataImporter extends DataImporter {
//
//	/**
//	 * The pattern that entries in the header row must match in order for the importer to determine how to fill its entries.
//	 */
//	private static final String HEADER_PATTERN = "[A-Z]+_[A-Z]{3}_\\d+_(\\d+|PLUS)";
//
//	/**
//	 * The pattern that entries in the header row representing total counts must match in order for the importer to determine how to fill
//	 * its entries.
//	 */
//	private static final String TOTAL_HEADER_PATTERN = "[A-Z]+_TOTAL";
//
//	private final Date collectionDate;
//	private final CampaignReferenceDto campaignReferenceDto;
//
//	public DefaultPopulationDataImporter(File inputFile, UserDto currentUser, CampaignDto campaignDto, ValueSeparator csvSeparator) throws IOException {
//		
//		super(inputFile, false, currentUser, csvSeparator);
//		this.collectionDate = new Date();
//		
//		this.campaignReferenceDto = FacadeProvider.getCampaignFacade().getReferenceByUuid(campaignDto.getUuid());
//	}
//
//	@Override
//	protected ImportLineResult importDataFromCsvLine(
//		String[] values,
//		String[] entityClasses,
//		String[] entityProperties,
//		String[][] entityPropertyPaths,
//		boolean firstLine)
//		throws IOException, InvalidColumnException, InterruptedException {
//
//		// Check whether the new line has the same length as the header line
//		if (values.length > entityProperties.length) {
//			writeImportError(values, I18nProperties.getValidationError(Validations.importLineTooLong));
//			return ImportLineResult.ERROR;
//		}
//
//		// Reference population data that contains the region, district and community for this line
//		RegionReferenceDto region = null;
//		DistrictReferenceDto district = null;
//		CommunityReferenceDto community = null;
//		CampaignReferenceDto campaigns_ = null;
//		
//		System.out.println("++++++++++++++++++===============: "+entityProperties.length);
//
//		// Retrieve the region and district from the database or throw an error if more or less than one entry have been retrieved
//		for (int i = 0; i < entityProperties.length; i++) {
//			
//			System.out.println(entityProperties[i]+" :++++++++++++++++++===============: "+i);
//			if (PopulationDataDto.REGION.equalsIgnoreCase(entityProperties[i])) {
//				List<RegionReferenceDto> regions = FacadeProvider.getRegionFacade().getByExternalId(Long.parseLong(values[i]), false);
//				if (regions.size() != 1) {
//					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//					return ImportLineResult.ERROR;
//				}
//				region = regions.get(0);
//			}
//			if (PopulationDataDto.DISTRICT.equalsIgnoreCase(entityProperties[i])) {
//				if (DataHelper.isNullOrEmpty(values[i])) {
//					district = null;
//				} else {
//					List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getByExternalID(Long.parseLong(values[i]), region, false);
//					if (districts.size() != 1) {
//						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//						return ImportLineResult.ERROR;
//					}
//					district = districts.get(0);
//				}
//			}
//			/*
//			if (PopulationDataDto.COMMUNITY.equalsIgnoreCase(entityProperties[i])) {
//				
//				if (DataHelper.isNullOrEmpty(values[i])) {
//					community = null;
//				} else {
//					
//					System.out.println("++++++++++++++++++===============");
//					
//					List<CommunityReferenceDto> communities = FacadeProvider.getCommunityFacade().getByName(values[i], district, false);
//					if (communities.size() != 1) {
//						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//						return ImportLineResult.ERROR;
//					}
//					community = communities.get(0);
//				}
//			}*/
//			
//			//patch to use cluster No for data import
//			
//			if (PopulationDataDto.COMMUNITY_EXTID.equalsIgnoreCase(entityProperties[i])) { 
//				if (DataHelper.isNullOrEmpty(values[i])) {
//					community = null;
//				} else {
//					if(isLong(values[i])) {
//					List<CommunityReferenceDto> communities = FacadeProvider.getCommunityFacade().getByExternalId(Long.parseLong(values[i]), false);
//					if (communities.size() != 1) {
//						writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//						System.out.println(new ImportErrorException(values[i], entityProperties[i]).getMessage());
//						return ImportLineResult.ERROR;
//					}
//					community = communities.get(0);
//				} else {
//					writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
//					System.out.println(new ImportErrorException(values[i], entityProperties[i]).getMessage() +" ttttttttttttttttt 1111"+values[i]);
//					return ImportLineResult.ERROR;
//				}
//					}
//				}
//			
//			//fixing importing campaign population based patch
//			if (PopulationDataDto.CAMPAIGN.equalsIgnoreCase(entityProperties[i])) { 
//				if (DataHelper.isNullOrEmpty(values[i])) {
//					campaigns_ = FacadeProvider.getCampaignFacade().getReferenceByUuid(campaignReferenceDto.getUuid());
////					System.out.println("checking campagin on record "+campaigns_.getUuid().equals(campaignReferenceDto.getUuid()));
//					
////					campaigns_ = null;
//				} 
//				}
//			
//		
////		
////		//Enable campaign based population import
////		if (PopulationDataDto.CAMPAIGN.equalsIgnoreCase(entityProperties[i])) { 
////			if (DataHelper.isNullOrEmpty(values[i])) {
////				campaign = null;
////			} else {
////				if(values[i].toString().length() > 20 && values[i].toString().contains("-")) {
////				campaign = FacadeProvider.getCampaignFacade().getReferenceByUuid(values[i]);
////				
////			} else {
////				writeImportError(values, new ImportErrorException(values[i], entityProperties[i]).getMessage());
////				System.out.println(new ImportErrorException(values[i], entityProperties[i]).getMessage() +" campaginttttttttttttttttt 1111"+values[i]);
////				return ImportLineResult.ERROR;
////			}
////				}
////			}
//		}
////	
//
//		// The region and district that will be used to save the population data to the database
//		final RegionReferenceDto finalRegion = region;
//		final DistrictReferenceDto finalDistrict = district;
//		final CommunityReferenceDto finalCommunity = community;
//		
//		final CampaignReferenceDto finalCampaign = campaigns_;
//
//		// Retrieve the existing population data for the region and district
//		PopulationDataCriteria criteria = new PopulationDataCriteria().region(finalRegion);
//		//criteria.setCampaign(finalCampaign);
//		if (finalCampaign == null) {
//			criteria.campaignIsNull(true);
//		} else {
//			criteria.campaign(finalCampaign);
//		}
//		
//		
//		
//		
//		if (finalCommunity == null) {
//			criteria.communityIsNull(true);
//		} else {
//			criteria.community(finalCommunity);
//		}
//		
//		if (district == null) {
//			criteria.districtIsNull(true);
//		} else {
//			criteria.district(finalDistrict);
//		}
//		
//		List<PopulationDataDto> existingPopulationDataList = FacadeProvider.getPopulationDataFacade().getPopulationDataImportChecker(criteria);
//		List<PopulationDataDto> modifiedPopulationDataList = new ArrayList<PopulationDataDto>();
//		
//		
//		System.out.println("+++++++++:+ " + criteria.getAgeGroup());
//		System.out.println("+++++++++:+ " + criteria.getRegion());
//		System.out.println("+++++++++:+ " + criteria.getDistrict());
//		System.out.println("+++++++++:+ " + criteria.getCampaign());
//		System.out.println("+++++++++:+ " + criteria.getSex());
//		System.out.println("+++++++++:+ " + criteria.getDistrict());
//		System.out.println("+++++++++:+ " + existingPopulationDataList.size());
//		
//		
//
//		boolean populationDataHasImportError =
//			insertRowIntoData(values, entityClasses, entityPropertyPaths, false, new Function<ImportCellData, Exception>() {
//				
//				@Override
//				public Exception apply(ImportCellData cellData) {
//					System.out.println("++++++++++++++++111111111111111111111111++++++++++++++++ ");
//					
//					try {
//						if (PopulationDataDto.REGION.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])
//							|| PopulationDataDto.DISTRICT.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])
//							|| PopulationDataDto.CAMPAIGN.equalsIgnoreCase(cellData.getEntityPropertyPath()[0]) //Property type  not allowed
//							|| PopulationDataDto.COMMUNITY_EXTID.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
//							System.out.println("+++++++++++++ignoring......");
//							// Ignore the region, district and community columns
//						} else if (RegionDto.GROWTH_RATE.equalsIgnoreCase(cellData.getEntityPropertyPath()[0])) {
//							System.out.println("+++++++++++++----------");
//							// Update the growth rate of the region or district
////							if (!DataHelper.isNullOrEmpty(cellData.value)) {
////								Float growthRate = Float.parseFloat(cellData.value);
////								if (finalCommunity != null) {
////									CommunityDto communityDto = FacadeProvider.getCommunityFacade().getByUuid(finalCommunity.getUuid());
////									communityDto.setGrowthRate(growthRate);
////									FacadeProvider.getCommunityFacade().save(communityDto);
////								} else if (finalDistrict != null) {
////									DistrictDto districtDto = FacadeProvider.getDistrictFacade().getDistrictByUuid(finalDistrict.getUuid());
////									districtDto.setGrowthRate(growthRate);
////									FacadeProvider.getDistrictFacade().save(districtDto);
////								} else {
////									RegionDto regionDto = FacadeProvider.getRegionFacade().getByUuid(finalRegion.getUuid());
////									regionDto.setGrowthRate(growthRate);
////									FacadeProvider.getRegionFacade().save(regionDto);
////								}
////							}
//						} else {
//							
//							// Add the data from the currently processed cell to a new population data object
//							PopulationDataDto newPopulationData = PopulationDataDto.build(collectionDate);
//							newPopulationData.setCampaign(finalCampaign);
//							insertCellValueIntoData(newPopulationData, cellData.getValue(), cellData.getEntityPropertyPath());
//							System.out.println(newPopulationData.getAgeGroup() +"   :+++++++++++++: "+existingPopulationDataList.size());
//							
//							Optional<PopulationDataDto> existingPopulationData = existingPopulationDataList.stream()
//								.filter( 
//										populationData -> populationData.getAgeGroup().equals(newPopulationData.getAgeGroup())
//										&& populationData.getCampaign().equals(newPopulationData.getCampaign())
//										)
//								.findFirst();
////							boolean isExisted = false;
////							for(PopulationDataDto dc : existingPopulationDataList ) {
////								if(newPopulationData.getCampaign().equals(dc.getCampaign()) && newPopulationData.getAgeGroup().equals(dc.getAgeGroup())) {
////									isExisted = true;
////									break;
////								}
////							}
////							
////							System.out.println(isExisted+" :++++++++++++++++++: "+existingPopulationData.isPresent());
//							//TODO check if should overwrite
//							// Check whether this population data set already exists in the database; if yes, override it
//							if (existingPopulationData.isPresent()) {
//								System.out.println("++++++++++++++++existingPopulationData.isPresent()++++++++++++++++ ");
//								existingPopulationData.get().setPopulation(newPopulationData.getPopulation());
//								existingPopulationData.get().setCollectionDate(collectionDate);
//								modifiedPopulationDataList.add(existingPopulationData.get());
//							} else {
//								System.out.println("++++++++++++++++existingPopulationData.NOTisPresent()++++++++++++++++ ");
//								
//								newPopulationData.setRegion(finalRegion);
//								newPopulationData.setDistrict(finalDistrict);
//								newPopulationData.setCommunity(finalCommunity);
//								newPopulationData.setCampaign(finalCampaign);
//								modifiedPopulationDataList.add(newPopulationData);
//							}
//						}
//					} catch (ImportErrorException | InvalidColumnException | NumberFormatException e) {
//						System.out.println("++++++++++++++++Error found++++++++++++++++ ");
//						
//						return e;
//					}
//
//					return null;
//				}
//			});
//
//		// Validate and save the population data object into the database if the import has no errors
//		if (!populationDataHasImportError) {
//			System.out.println("++++++++++++++++0000000000000000NO Errror 000000000000000000000++++++++++++++++ " + modifiedPopulationDataList.size());
//			
//			try {
//				FacadeProvider.getPopulationDataFacade().savePopulationData(modifiedPopulationDataList);
//				return ImportLineResult.SUCCESS;
//			} catch (ValidationRuntimeException e) {
//				writeImportError(values, e.getMessage());
//				return ImportLineResult.ERROR;
//			}
//		} else {
//			return ImportLineResult.ERROR;
//		}
//	}
//	
//	
//	
//	private static boolean isLong(String str) {
//		
//	  	try {
//	      	@SuppressWarnings("unused")
//	    	int x = Integer.parseInt(str);
//	      	return true; //String is an Integer
//		} catch (NumberFormatException e) {
//	    	return false; //String is not an Integer
//		}
//	  	
//	}
//
//	/**
//	 * Inserts the entry of a single cell into the population data object. Checks whether the entity property accords to one of the patterns
//	 * defined in this class
//	 * and sets the according sex and age group to the population data object.
//	 */
//	private void insertCellValueIntoData(PopulationDataDto populationData, String value, String[] entityPropertyPaths)
//		throws InvalidColumnException, ImportErrorException {
//		String entityProperty = buildEntityProperty(entityPropertyPaths);
//
//		if (entityPropertyPaths.length != 1) {
//			throw new UnsupportedOperationException(
//				I18nProperties.getValidationError(Validations.importPropertyTypeNotAllowed, buildEntityProperty(entityPropertyPaths)));
//		}
//
//		String entityPropertyPath = entityPropertyPaths[0];
//
//		try {
//			if (entityPropertyPath.equalsIgnoreCase("TOTAL")) {
//				insertPopulationIntoPopulationData(populationData, value);
//			} else if (entityPropertyPath.matches(TOTAL_HEADER_PATTERN)) {
//				try {
//					populationData.setSex(Sex.valueOf(entityPropertyPaths[0].substring(0, entityPropertyPaths[0].indexOf("_"))));
//				} catch (IllegalArgumentException e) {
//					throw new InvalidColumnException(entityProperty);
//				}
//				insertPopulationIntoPopulationData(populationData, value);
//			} else if (entityPropertyPath.matches(HEADER_PATTERN)) {
//				// Sex
//				String sexString = entityPropertyPath.substring(0, entityPropertyPaths[0].indexOf("_"));
//				if (!sexString.equals("TOTAL")) {
//					try {
//						populationData.setSex(Sex.valueOf(sexString));
//					} catch (IllegalArgumentException e) {
//						throw new InvalidColumnException(entityProperty);
//					}
//				}
//
//				// Age group
//				String ageGroupString = entityPropertyPath.substring(entityPropertyPath.indexOf("_") + 1, entityPropertyPaths[0].length());
//				try {
//					populationData.setAgeGroup(AgeGroup.valueOf(ageGroupString));
//				} catch (IllegalArgumentException e) {
//					throw new InvalidColumnException(entityProperty);
//				}
//
//				insertPopulationIntoPopulationData(populationData, value);
//			} else {
//				throw new ImportErrorException(I18nProperties.getValidationError(Validations.importPropertyTypeNotAllowed, entityPropertyPath));
//			}
//		} catch (IllegalArgumentException e) {
//			throw new ImportErrorException(value, entityProperty);
//		} catch (ImportErrorException e) {
//			throw e;
//		} catch (Exception e) {
//			logger.error("Unexpected error when trying to import population data: " + e.getMessage());
//			throw new ImportErrorException(I18nProperties.getValidationError(Validations.importUnexpectedError));
//		}
//	}
//
//	private void insertPopulationIntoPopulationData(PopulationDataDto populationData, String entry) throws ImportErrorException {
//		try {
//			populationData.setPopulation(Integer.parseInt(entry));
//		} catch (NumberFormatException e) {
//			throw new ImportErrorException(e.getMessage());
//		}
//	}
//}