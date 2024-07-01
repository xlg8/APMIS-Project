package com.cinoteck.application.views.utils;

import com.cinoteck.application.views.utils.ExportEntityName;
import com.opencsv.CSVWriter;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.AgeGroup;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.utils.CSVUtils;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.SortProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DownloadFlowUtilityView {

	public static StreamResource createPopulationDataExportResource(String campaignUuid) {
		String exportFileName = createFileNameWithCurrentDate(ExportEntityName.POPULATION_DATA, ".csv");

		return new StreamResource(exportFileName, () -> {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				try (CSVWriter writer = CSVUtils.createCSVWriter(
						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
						FacadeProvider.getConfigFacade().getCsvSeparator())) {

					// Generate and write columns to CSV writer
					List<String> columnNames = new ArrayList<>();

					columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, "Region"));
					columnNames.add(I18nProperties.getCaption("RCode"));

					columnNames.add(
							I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.REGION));
					columnNames.add(I18nProperties.getCaption("PCode"));

					columnNames.add(
							I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.DISTRICT));
					columnNames.add(I18nProperties.getCaption("DCode"));

					columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX,
							PopulationDataDto.COMMUNITY));
					columnNames.add(I18nProperties.getCaption(Captions.Campaign));

					Map<AgeGroup, Integer> ageGroupPositions = new HashMap<>();
					int ageGroupIndex = columnNames.size();
					for (AgeGroup ageGroup : AgeGroup.values()) {
						if (ageGroup.equals(AgeGroup.AGE_0_4) || ageGroup.equals(AgeGroup.AGE_5_10)) {
							columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, null));
							ageGroupPositions.put(ageGroup, ageGroupIndex);
							ageGroupIndex += 1;
						}
//	                    columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, null));
//	                    columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, Sex.MALE));
//	                    columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, Sex.FEMALE));
//	                    columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, Sex.OTHER));
//	                    ageGroupPositions.put(ageGroup, ageGroupIndex);
//	                    ageGroupIndex += 4; // Increment by 4 for each age group
					}

					columnNames.add(
							I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.MODALITY));
					columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX,
							PopulationDataDto.DISTRICT_STATUS));

					writer.writeNext(columnNames.toArray(new String[0]));

					List<Object[]> populationExportDataList = FacadeProvider.getPopulationDataFacade()
							.getPopulationDataForExport(campaignUuid);

					String[] exportLine = null;
					String areaName = "";
					Long rCode;
					String regionName = "";
					Long provinceCode;
					String districtName = "";
					Long districtCode;
					String communityName = "";
					String campaignName = "";

					for (Object[] populationExportData : populationExportDataList) {
						String dataAreaName = (String) populationExportData[0];
						String dataAreaCode = ((BigInteger) populationExportData[1]).toString();
						String dataRegionName = (String) populationExportData[2];
						String dataRegionCode = ((BigInteger) populationExportData[3]).toString();
						String dataDistrictName = populationExportData[4] == null ? ""
								: (String) populationExportData[4];
						String dataDistrictCode = ((BigInteger) populationExportData[5]).toString();
						;
						String dataCommunityName = populationExportData[6] == null ? ""
								: (String) populationExportData[6];
						String dataCampaignName = populationExportData[7] == null ? ""
								: (String) populationExportData[7];
						String dataModality = populationExportData[10] == null ? "" : (String) populationExportData[10];
						String dataDistrictStatus = populationExportData[11] == null ? ""
								: (String) populationExportData[11];

						if (exportLine != null && (!dataRegionName.equals(regionName)
								|| !dataDistrictName.equals(districtName) || !dataCampaignName.equals(campaignName)
								|| !dataCommunityName.equals(communityName))) {
							// New region or district reached; write line to CSV
							writer.writeNext(exportLine);
							exportLine = null;
						}

						if (exportLine == null) {
							exportLine = new String[columnNames.size()];
							exportLine[0] = dataAreaName;
							exportLine[1] = dataAreaCode;
							exportLine[2] = dataRegionName;
							exportLine[3] = dataRegionCode;
							exportLine[4] = dataDistrictName;
							exportLine[5] = dataDistrictCode;
							exportLine[6] = dataCommunityName;
							exportLine[7] = dataCampaignName;
							exportLine[10] = dataModality;
							exportLine[11] = dataDistrictStatus;
						}

						AgeGroup ageGroup = AgeGroup.valueOf((String) populationExportData[8]);
						if(ageGroup != null) {
							String sexString = (String) populationExportData[9];
							Integer ageGroupPosition = ageGroupPositions.get(ageGroup);

							if (Sex.MALE.getName().equals(sexString)) {
								exportLine[ageGroupPosition + 1] = String.valueOf(populationExportData[12]);
							} else if (Sex.FEMALE.getName().equals(sexString)) {
								exportLine[ageGroupPosition + 2] = String.valueOf(populationExportData[12]);
							} else if (Sex.OTHER.getName().equals(sexString)) {
								exportLine[ageGroupPosition + 3] = String.valueOf(populationExportData[12]);
							} else {
								exportLine[ageGroupPosition] = String.valueOf(populationExportData[12]);
							}

							areaName = dataAreaName;
							regionName = dataRegionName;
							districtName = dataDistrictName;
							communityName = dataCommunityName;
							campaignName = dataCampaignName;
//		                    provinceCode = dataRegionCode;
//		                    districtCode = dataDistrictCode;
						}
						
					}

					// Write the last line to CSV
					if (exportLine != null) {
						writer.writeNext(exportLine);
					}

					writer.flush();
				}
				return new ByteArrayInputStream(byteStream.toByteArray());
			} catch (IOException e) {
				// Handle exceptions and show a notification if needed
				return null;
			}
		});
	}

	public static StreamResource createPopulationDataExportResourcex(String campaignUuid) {
		String exportFileName = createFileNameWithCurrentDate(ExportEntityName.POPULATION_DATA, ".csv");

		return new StreamResource(exportFileName, () -> {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				try (CSVWriter writer = CSVUtils.createCSVWriter(
						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
						FacadeProvider.getConfigFacade().getCsvSeparator())) {
					// Generate and write columns to CSV writer
					List<String> columnNames = new ArrayList<>();
					columnNames.add(
							I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.REGION));
					columnNames.add(
							I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.DISTRICT));
					columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX,
							PopulationDataDto.COMMUNITY));
					// add campaign uuid to the last row
					columnNames.add(I18nProperties.getCaption(Captions.Campaign));
					columnNames.add(
							I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.MODALITY));
					columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX,
							PopulationDataDto.DISTRICT_STATUS));

//						columnNames.add(I18nProperties.getString(Strings.total + "0_4"));
//						columnNames.add(I18nProperties.getString(Strings.total + "5_10"));

//						columnNames.add(I18nProperties.getCaption(Captions.populationDataMaleTotal));
//						columnNames.add(I18nProperties.getCaption(Captions.populationDataFemaleTotal));

					Map<AgeGroup, Integer> ageGroupPositions = new HashMap<>();
					int ageGroupIndex = 8;
					for (AgeGroup ageGroup : AgeGroup.values()) {

//							if (ageGroup.equals(AgeGroup.AGE_0_4 )|| ageGroup.equals(AgeGroup.AGE_5_10 )) {
						columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, null));
//								columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, Sex.MALE));
//								columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, Sex.FEMALE));
//								columnNames.add(DataHelper.getSexAndAgeGroupString(ageGroup, Sex.OTHER));
						ageGroupPositions.put(ageGroup, ageGroupIndex);
						ageGroupIndex += 1;
//							}

					}

					writer.writeNext(columnNames.toArray(new String[columnNames.size()]));

					List<Object[]> populationExportDataList = FacadeProvider.getPopulationDataFacade()
							.getPopulationDataForExport(campaignUuid);

					String[] exportLine = new String[columnNames.size()];
					String regionName = "";
					String districtName = "";
					String communityName = "";
					String campaignName = "";
					String modality = "";
					String district_status = "";

					for (Object[] populationExportData : populationExportDataList) {
						String dataRegionName = (String) populationExportData[0];
						String dataDistrictName = populationExportData[1] == null ? ""
								: (String) populationExportData[1];
						String dataCommunityName = populationExportData[2] == null ? ""
								: (String) populationExportData[2];
						String dataCampaignName = populationExportData[3] == null ? ""
								: (String) populationExportData[3];

						String dataModality = populationExportData[4] == null ? "" : (String) populationExportData[3];

						String dataDistrictStatus = populationExportData[4] == null ? ""
								: (String) populationExportData[3];

						if (exportLine[0] != null && (!dataRegionName.equals(regionName)
								|| !dataDistrictName.equals(districtName) || !dataCampaignName.equals(campaignName)
								|| !dataCommunityName.equals(communityName))) {
							// New region or district reached; write line to CSV
							writer.writeNext(exportLine);
							exportLine = new String[columnNames.size()];
						}
						regionName = dataRegionName;
						districtName = dataDistrictName;
						communityName = dataCommunityName;
						campaignName = dataCampaignName;
						modality = dataModality;
						district_status = dataDistrictStatus;
						// Region
						if (exportLine[0] == null) {
							exportLine[0] = (String) populationExportData[0];
						}
						// District
						if (exportLine[1] == null) {
							exportLine[1] = (String) populationExportData[1];
						}
						// Community
						if (exportLine[2] == null) {
							exportLine[2] = (String) populationExportData[2];
						}

						// campaign
						if (exportLine[3] == null) {
							exportLine[3] = (String) populationExportData[3];
						}

						// modality
						if (exportLine[4] == null) {
							exportLine[4] = (String) populationExportData[4];
						}

						// district status
						if (exportLine[5] == null) {
							exportLine[5] = (String) populationExportData[5];
						}

						if (populationExportData[6] == null) {
							// Total population
							String sexString = (String) populationExportData[7];
							if (Sex.MALE.getName().equals(sexString)) {
								exportLine[7] = String.valueOf((int) populationExportData[8]);
							} else if (Sex.FEMALE.getName().equals(sexString)) {
								exportLine[8] = String.valueOf((int) populationExportData[8]);
							} else if (Sex.OTHER.getName().equals(sexString)) {
								exportLine[9] = String.valueOf((int) populationExportData[8]);
							} else {
								exportLine[6] = String.valueOf((int) populationExportData[6]);
							}
						} else {

							// Population based on age group position and sex
							Integer ageGroupPosition = ageGroupPositions
									.get(AgeGroup.valueOf((String) populationExportData[6]));
							String sexString = (String) populationExportData[7];
							if (Sex.MALE.getName().equals(sexString)) {
								ageGroupPosition += 1;
							} else if (Sex.FEMALE.getName().equals(sexString)) {
								ageGroupPosition += 2;
							} else if (Sex.OTHER.getName().equals(sexString)) {
								ageGroupPosition += 3;
							}

							else {
								exportLine[ageGroupPosition] = String.valueOf((int) populationExportData[8]);
							}

							System.out.println(ageGroupPosition + "Age groups pos ");

							System.out.println(ageGroupPosition + "Age groups pos " + populationExportData[0]
									+ populationExportData[1] + populationExportData[2] + populationExportData[3]
									+ populationExportData[4] + populationExportData[5] + populationExportData[6]
									+ populationExportData[7]);
//								Integer ageGroupPosition = ageGroupPositions.get(AgeGroup.valueOf((String) populationExportData[4]));
//								String sexString = (String) populationExportData[5];
//								if (Sex.MALE.getName().equals(sexString)) {
//									ageGroupPosition += 1;
//								} else if (Sex.FEMALE.getName().equals(sexString)) {
//									ageGroupPosition += 2;
//								} else if (Sex.OTHER.getName().equals(sexString)) {
//									ageGroupPosition += 3;
//								}
//								exportLine[ageGroupPosition] = String.valueOf((int) populationExportData[6]);
						}

						columnNames.add(I18nProperties.getCaption(Captions.Campaign));
					}

					// Write last line to CSV
					writer.writeNext(exportLine);
					writer.flush();

				}
				return new ByteArrayInputStream(byteStream.toByteArray());
			} catch (IOException e) {
				// Handle exceptions and show a notification if needed
				return null;
			}
		});
	}
	
	
	public static void treat(CampaignFormDataCriteria criteria) {
		 List<CampaignFormDataIndexDto> formValueIds = FacadeProvider.getCampaignFormDataFacade()
	               .getIndexList(criteria, null, 1, null);

	       if (formValueIds == null || formValueIds.isEmpty()) {
	           System.out.println("No data found.");
	           return;
	       }

	       // Collect all form entries
	       List<CampaignFormDataEntry> formEntries = new ArrayList<>();
	       for (CampaignFormDataIndexDto vv : formValueIds) {
	           formEntries.addAll(vv.getFormValues());
	       }

	       if (formEntries.isEmpty()) {
	           System.out.println("No form entries found.");
	           return;
	       }

	       // Collect all form entry IDs
	       List<String> formEntriesID = new ArrayList<>();
	       for (CampaignFormDataEntry vv : formEntries) {
	           formEntriesID.add(vv.getId());
	       }

	       System.out.println(formEntriesID + " IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIids in form entry ");

	       // Extract and combine _day values
	       List<String> uniqueDayValues = extractUniqueDayValues(formEntriesID);
	       List<String> uniqueVariableParts = extractUniqueVariableParts(formEntriesID);

	       
	       System.out.println(formEntriesID + " IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIids in form entry " + uniqueVariableParts);

	       // Write the table to a CSV file
	       writeTableToCSV(uniqueDayValues, uniqueVariableParts, formEntries, "output.csv");
	}
	  
  

	public static StreamResource createTransposedDayWiseDataExportResource(CampaignFormDataCriteria criteria, Query<CampaignFormDataIndexDto, CampaignFormDataCriteria> query) {
//		return null;
		String exportFileName = createFileNameWithCurrentDateandEntityNameString("DaywiseData", ".csv");
//
		return new StreamResource(exportFileName, () -> {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				try (CSVWriter writer = CSVUtils.createCSVWriter(
						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
						FacadeProvider.getConfigFacade().getCsvSeparator())) {
//					// Generate and write columns to CSV writer
					List<String> columnNames = new ArrayList<>();

					List<CampaignFormDataIndexDto> formValueIds = FacadeProvider.getCampaignFormDataFacade()
							.getIndexList(criteria, null, 1, null);
					
					CampaignFormDataIndexDto formValusFirst = formValueIds.get(0); 
					List<CampaignFormDataEntry> formEntries = new ArrayList<>();
					for (CampaignFormDataIndexDto vv : formValueIds) {
						formEntries = vv.getFormValues();
					}
					
					
					List<String> formEntriesID = new ArrayList<>();
					for (CampaignFormDataEntry vv : formEntries) {
						formEntriesID.add(vv.getId());
					}
					
					System.out.println(formEntriesID +  " IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIids in form entry ");
					
					
					 List<String> uniqueDayValues = extractUniqueDayValues(formEntriesID);

				        List<String> uniqueVariableParts = extractUniqueVariableParts(formEntriesID);

				        // Print the unique day values
				        for (String dayValue : uniqueDayValues) {
				            System.out.println(dayValue +  " DAAAAAAAAAAAAAAAAAAAAYYYYYYYYYYYYYYYYYYYYYYYYYYYYY VALUE  in form entry ");
				        }
				        
				        writeTableToCSV(uniqueDayValues, uniqueVariableParts, formEntries, "output.csv");
				        
				        

				}
//				return new ByteArrayInputStream(byteStream.toByteArray());
			} catch (IOException e) {
				// Handle exceptions and show a notification if needed
				return null;
			}
			return null;
		});

	}
	
	 private static void writeTableToCSV(List<String> uniqueDayValues, List<String> uniqueVariableParts, List<CampaignFormDataEntry> formEntries, String fileName) {
	        // Sort day values and variable parts for proper order in the table
	        Collections.sort(uniqueDayValues);
	        Collections.sort(uniqueVariableParts);

	        try (FileWriter writer = new FileWriter(fileName)) {
	            // Write CSV header
	            writer.append("Day");
	            for (String variable : uniqueVariableParts) {
	                writer.append(",").append(variable);
	            }
	            writer.append("\n");

	            // Write CSV rows
	            for (String day : uniqueDayValues) {
	                writer.append(day);
	                for (String variable : uniqueVariableParts) {
	                    String value = getValueForVariableAndDay(formEntries, variable, day);
	                    writer.append(",").append(value != null ? value : "");
	                }
	                writer.append("\n");
	            }

	            System.out.println("CSV file created: " + fileName);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	
	private static List<String> extractUniqueVariableParts(List<String> formEntriesID) {
        Set<String> variablePartsSet = new HashSet<>();
        Pattern pattern = Pattern.compile("^(.+)_day\\d+$");

        for (String id : formEntriesID) {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                variablePartsSet.add(matcher.group(1));
            }
        }

        return new ArrayList<>(variablePartsSet);
    }
	
	private static String getValueForVariableAndDay(List<CampaignFormDataEntry> formEntries, String variable, String day) {
        String idPattern = variable + day;
        for (CampaignFormDataEntry entry : formEntries) {
            if (entry.getId().equals(idPattern)) {
                return entry.getValue().toString();
            }
        }
        return null;
    }
	
	private static List<String> extractUniqueDayValues(List<String> formEntriesID) {
        Set<String> dayValuesSet = new HashSet<>();
        Pattern pattern = Pattern.compile("day\\d+$");

        for (String id : formEntriesID) {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                dayValuesSet.add(matcher.group());
            }
        }

        return new ArrayList<>(dayValuesSet);
    }

	public static String createFileNameWithCurrentDate(ExportEntityName entityName, String fileExtension) {
		String instanceName = FacadeProvider.getConfigFacade().getSormasInstanceName().toLowerCase(); // The export is
																										// being
																										// prepared
		String processedInstanceName = DataHelper.cleanStringForFileName(instanceName);
		String processedEntityName = DataHelper.cleanStringForFileName(entityName.getLocalizedNameInSystemLanguage());
		String exportDate = DateHelper.formatDateForExport(new Date());
		return String.join("_", processedInstanceName, processedEntityName, exportDate, fileExtension);
	}

	public static String createFileNameWithCurrentDateandEntityNameString(String entityName, String fileExtension) {
		String instanceName = FacadeProvider.getConfigFacade().getSormasInstanceName().toLowerCase(); // The export is
																										// being
																										// prepared
		String processedInstanceName = DataHelper.cleanStringForFileName(instanceName);
		String processedEntityName = DataHelper.cleanStringForFileName(entityName);
		String exportDate = DateHelper.formatDateForExport(new Date());
		return String.join("_", processedInstanceName, processedEntityName, exportDate, fileExtension);
	}
}
