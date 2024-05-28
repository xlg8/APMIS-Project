package com.cinoteck.application.views.utils;

import com.cinoteck.application.views.utils.ExportEntityName;
import com.opencsv.CSVWriter;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.AgeGroup;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.utils.CSVUtils;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	                
	                columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.REGION));
	                columnNames.add(I18nProperties.getCaption("PCode"));

	                columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.DISTRICT));
	                columnNames.add(I18nProperties.getCaption("DCode"));

	                columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.COMMUNITY));
	                columnNames.add(I18nProperties.getCaption(Captions.Campaign));
	              
	                Map<AgeGroup, Integer> ageGroupPositions = new HashMap<>();
	                int ageGroupIndex = columnNames.size();
	                for (AgeGroup ageGroup : AgeGroup.values()) {
	                	if(ageGroup.equals(AgeGroup.AGE_0_4) || ageGroup.equals(AgeGroup.AGE_5_10)) {
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
	                
	                columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.MODALITY));
	                columnNames.add(I18nProperties.getPrefixCaption(PopulationDataDto.I18N_PREFIX, PopulationDataDto.DISTRICT_STATUS));


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
		                String dataAreaCode =  ((BigInteger) populationExportData[1]).toString();
	                    String dataRegionName = (String) populationExportData[2];
	                    String dataRegionCode =  ((BigInteger) populationExportData[3]).toString();
	                    String dataDistrictName = populationExportData[4] == null ? "" : (String) populationExportData[4];
	                    String dataDistrictCode =  ((BigInteger) populationExportData[5]).toString();;
	                    String dataCommunityName = populationExportData[6] == null ? "" : (String) populationExportData[6];
	                    String dataCampaignName = populationExportData[7] == null ? "" : (String) populationExportData[7];
	                    String dataModality = populationExportData[10] == null ? "" : (String) populationExportData[10];
	                    String dataDistrictStatus = populationExportData[11] == null ? "" : (String) populationExportData[11];

	                    if (exportLine != null && (!dataRegionName.equals(regionName)
	                            || !dataDistrictName.equals(districtName)
	                            || !dataCampaignName.equals(campaignName)
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
//	                    provinceCode = dataRegionCode;
//	                    districtCode = dataDistrictCode;
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

	public static String createFileNameWithCurrentDate(ExportEntityName entityName, String fileExtension) {
		String instanceName = FacadeProvider.getConfigFacade().getSormasInstanceName().toLowerCase(); // The export is
																										// being
																										// prepared
		String processedInstanceName = DataHelper.cleanStringForFileName(instanceName);
		String processedEntityName = DataHelper.cleanStringForFileName(entityName.getLocalizedNameInSystemLanguage());
		String exportDate = DateHelper.formatDateForExport(new Date());
		return String.join("_", processedInstanceName, processedEntityName, exportDate, fileExtension);
	}
}
