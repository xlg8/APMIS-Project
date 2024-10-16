package com.cinoteck.application.views.utils;

import com.opencsv.CSVWriter;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.utils.CSVUtils;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

public final class DownloadTransposedDaywiseDataUtility {

	CampaignFormDataCriteria criteria = new CampaignFormDataCriteria();

	public static StreamResource createTransposedDataFromIndexList(CampaignFormDataCriteria criteria, String formName,
			String campaignName) {
		CampaignFormDataCriteria criteriax = new CampaignFormDataCriteria();

		criteriax = criteria;

		String exportFileName = "APMIS_" + formName + "_" + campaignName + ".csv";// createFileNameWithCurrentDateandEntityNameString(formName+
																					// "_" + campaignName, ".csv");

		// Using the index list method to get the day-wise form data since it already
		// used the criteria on the grid to get the data
		List<CampaignFormDataIndexDto> formDatafromIndexList = FacadeProvider.getCampaignFormDataFacade()
				.getIndexList(criteriax, null, null, null);

		// Initialize the set to store unique variable parts without the day suffix
		Set<String> fieldIDWithoutDaySuffix = new HashSet<>();

		// Iterate through all elements in the formDatafromIndexList to build column
		// headers
		for (CampaignFormDataIndexDto formData : formDatafromIndexList) {
			if (formData.getFormValues() != null) {
				Set<String> fieldIDsFromFormValues = new HashSet<>();

				// Extract the field IDs from the form values and add them to the set to remove
				// duplicates
				for (CampaignFormDataEntry formValues : formData.getFormValues()) {
					fieldIDsFromFormValues.add(formValues.getId());
				}

				// Convert the set back to a list
				List<String> fieldIDsforColumnHeaders = new ArrayList<>(fieldIDsFromFormValues);

				// Extract unique variable parts and remove the day suffix
				for (String uniqueVariable : extractUniqueVariableParts(fieldIDsforColumnHeaders)) {
					String variableID = uniqueVariable.replaceAll("(_day\\d+|_days\\d+)$", "");
					fieldIDWithoutDaySuffix.add(variableID);
				}

			}
		}

		List<String> columnNames = new ArrayList<>();
//		
        Map<String, String> fieldIdToCaptionMap = new HashMap<>();

		List<String> fieldCaptions = new ArrayList<>();

		CampaignFormMetaDto formMetaReference = FacadeProvider.getCampaignFormMetaFacade()
				.getCampaignFormMetaByUuid(criteria.getCampaignFormMeta().getUuid());
		if (formMetaReference != null) {
			List<CampaignFormElement> campaignFormElements = formMetaReference.getCampaignFormElements();
			fieldIdToCaptionMap = matchFieldsWithCaptions(fieldIDWithoutDaySuffix, campaignFormElements);
		}

//	    columnNames.add(I18nProperties.getPrefixCaption(CampaignFormDataIndexDto.I18N_PREFIX, CampaignFormDataIndexDto.COMMUNITY));
		columnNames.add("Campaign");
		columnNames.add("Form");
		columnNames.add("Region");
		columnNames.add("RCode");
		columnNames.add("Province");
		columnNames.add("PCode");
		columnNames.add("District");
		columnNames.add("Dcode");
		columnNames.add("Cluster");
		columnNames.add("Cluster Number");
		columnNames.add("CCode");
		columnNames.add("Form Phase");
		columnNames.add("Source");
		columnNames.add("Creating User");
		columnNames.add("Day");
		
		


		fieldCaptions.add("Campaign");
		fieldCaptions.add("Form");
		fieldCaptions.add("Region");
		fieldCaptions.add("RCode");
		fieldCaptions.add("Province");
		fieldCaptions.add("PCode");
		fieldCaptions.add("District");
		fieldCaptions.add("Dcode");
		fieldCaptions.add("Cluster");
		fieldCaptions.add("Cluster Number");
		fieldCaptions.add("CCode");
		fieldCaptions.add("Form Phase");
		fieldCaptions.add("Source");
		fieldCaptions.add("Creating User");
		fieldCaptions.add("Day");
		
		 

		// Generate and write columns to CSV writer
		// index here has to be 15 because of the number of existing column
		// remeber to increment the values when new columns are added below the defined
		// columns
		Map<String, Integer> fieldIdPositions = new HashMap<>();
		int ageGroupIndex = 15;
		for (String fieldGroup : fieldIDWithoutDaySuffix) {
			columnNames.add(fieldGroup);
			fieldIdPositions.put(fieldGroup, ageGroupIndex);
			ageGroupIndex += 1;
		}
		
		 for (String fieldId : fieldIDWithoutDaySuffix) {
	            String caption = fieldIdToCaptionMap.getOrDefault(fieldId, fieldId);
	            fieldCaptions.add(caption);
	            fieldIdPositions.put(caption, ageGroupIndex);
				ageGroupIndex += 1;
	        }
		 

		Map<String, Map<String, String>> dayValueMap = new HashMap<>();

		for (CampaignFormDataIndexDto individualTransposedFormData : formDatafromIndexList) {
			if (individualTransposedFormData.getFormValues() != null) {
				Map<String, String> formDataMaxp = new HashMap<>();
				Set<String> fieldIDsFromFormValues = new HashSet<>();

				for (CampaignFormDataEntry formValues : individualTransposedFormData.getFormValues()) {
					formDataMaxp.put(formValues.getId(), formValues.getValue().toString());
					fieldIDsFromFormValues.add(formValues.getId());
				}

				List<String> fieldIDsforColumnHeaders = new ArrayList<>(fieldIDsFromFormValues);

				Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeader = new HashSet<>();

				for (String uniqueVariable : extractUniqueVariableParts(fieldIDsforColumnHeaders)) {
					String variableID = uniqueVariable.replaceAll("(_day\\d+|_days\\d+)$", "");
					uniqueVariablePartsWithoutDaySuffixForColumnHeader.add(variableID);
				}

				for (String day : extractUniqueDayValues(fieldIDsforColumnHeaders)) {
					for (String variable : uniqueVariablePartsWithoutDaySuffixForColumnHeader) {
						String key = variable + "_" + day;

						if (formDataMaxp.containsKey(key)) {
							String keyValue = formDataMaxp.get(key);
							dayValueMap.computeIfAbsent(day, k -> new HashMap<>()).put(variable, keyValue);
						}
					}
				}
			}
		}

//		return new StreamResource(exportFileName, () -> {
//			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
//				try (CSVWriter writer = CSVUtils.createCSVWriter(
//						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8.name()),
//						FacadeProvider.getConfigFacade().getCsvSeparator())) {
//
//					writer.writeNext(columnNames.toArray(new String[0]));
//					writer.writeNext(columnNames.toArray(new String[0]));
//
//					for (CampaignFormDataIndexDto individualTransposedFormData : formDatafromIndexList) {
//						if (individualTransposedFormData.getFormValues() != null) {
//							Map<String, String> formDataMaxp = new HashMap<>();
//							Set<String> fieldIDsFromFormValues = new HashSet<>();
//
//							for (CampaignFormDataEntry formValues : individualTransposedFormData.getFormValues()) {
//								
//								   String value = new String(formValues.getValue().toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
//		                            formDataMaxp.put(formValues.getId(), value);
//		                            fieldIDsFromFormValues.add(formValues.getId());
//		                            
////								formDataMaxp.put(formValues.getId(), formValues.getValue().toString());
////								fieldIDsFromFormValues.add(formValues.getId());
//							}
//
//							List<String> fieldIDsforColumnHeaders = new ArrayList<>(
//									fieldIDsFromFormValues);
//
//							Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeader = new HashSet<>();
//
//							for (String uniqueVariable : extractUniqueVariableParts(
//									fieldIDsforColumnHeaders)) {
//								String variableID = uniqueVariable.replaceAll("(_day\\d+|_days\\d+)$", "");
//								uniqueVariablePartsWithoutDaySuffixForColumnHeader.add(variableID);
//							}
//
//							
//							
//							for (String day : extractUniqueDayValues(fieldIDsforColumnHeaders)) {
//								List<String> row = new ArrayList<>(Collections.nCopies(columnNames.size(), ""));
//
//								row.set(0, individualTransposedFormData.getCampaign().toString());
//
//								row.set(1, individualTransposedFormData.getForm().toString());
//
//								row.set(2, individualTransposedFormData.getArea().toString());
//
//								row.set(3, individualTransposedFormData.getRcode().toString());
//
//								row.set(4, individualTransposedFormData.getRegion().toString());
//
//								row.set(5, individualTransposedFormData.getPcode() + "");
//
//								row.set(6, individualTransposedFormData.getDistrict().toString());
//
//								row.set(7, individualTransposedFormData.getDcode() + "");
//
//								if (individualTransposedFormData.getCommunity() == null) {
//									row.set(8, "");
//
//									row.set(9, "");
//
//									row.set(10, "");
//
//								} else {
//									
//									row.set(8, individualTransposedFormData.getCommunity().toString());
//
//									row.set(9, individualTransposedFormData.getClusternumber().toString());
//
//									row.set(10, individualTransposedFormData.getCcode().toString());
//									
//
//								}
//
//								row.set(11, individualTransposedFormData.getFormType().toString());
//
//								if (individualTransposedFormData.getSource() == null) {
//									row.set(12, "");
//
//								} else {
//									row.set(12, individualTransposedFormData.getSource().toString());
//
//								}
//
//								if (individualTransposedFormData.getCreatingUser() == null) {
//									row.set(13, "");
//
//								} else {
//									row.set(13, individualTransposedFormData.getCreatingUser().toString());
//
//
//								}
//
//								row.set(14, day);
//								
//								
//
//								for (String variable : uniqueVariablePartsWithoutDaySuffixForColumnHeader) {
//									
//									String key = variable + "_" + day;
//									
//									if (formDataMaxp.containsKey(key)) {
//										
//										String keyValue = formDataMaxp.get(key);
//										
//										int colIndex = columnNames.indexOf(variable);
//										
//										if (colIndex >= 0) {
//											row.set(colIndex, keyValue);
//										}
//									}
//								}
//
//								writer.writeNext(row.toArray(new String[0]));
//							}
//						}
//					}
//
//					writer.flush();
//
//				}
//				return new ByteArrayInputStream(byteStream.toByteArray());
//			} catch (IOException e) {
//				// Handle exceptions and show a notification if needed
//				return null;
//			}
//		});

		return new StreamResource(exportFileName, () -> {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				// Write UTF-8 BOM
				byteStream.write(0xEF);
				byteStream.write(0xBB);
				byteStream.write(0xBF);

				try (CSVWriter writer = CSVUtils.createCSVWriter(
						new OutputStreamWriter(byteStream, StandardCharsets.UTF_8),
						FacadeProvider.getConfigFacade().getCsvSeparator())) {

					// Write header twice
					writer.writeNext(fieldCaptions.toArray(new String[0]));

					writer.writeNext(columnNames.toArray(new String[0]));

					for (CampaignFormDataIndexDto individualTransposedFormData : formDatafromIndexList) {
						if (individualTransposedFormData.getFormValues() != null) {
							Map<String, String> formDataMaxp = new HashMap<>();
							Set<String> fieldIDsFromFormValues = new HashSet<>();

							for (CampaignFormDataEntry formValues : individualTransposedFormData.getFormValues()) {
								String value = new String(
										formValues.getValue().toString().getBytes(StandardCharsets.UTF_8),
										StandardCharsets.UTF_8);
								formDataMaxp.put(formValues.getId(), value);
								fieldIDsFromFormValues.add(formValues.getId());
							}

							List<String> fieldIDsforColumnHeaders = new ArrayList<>(fieldIDsFromFormValues);

							Set<String> uniqueVariablePartsWithoutDaySuffixForColumnHeader = new HashSet<>();

							for (String uniqueVariable : extractUniqueVariableParts(fieldIDsforColumnHeaders)) {
								String variableID = uniqueVariable.replaceAll("(_day\\d+|_days\\d+)$", "");
								uniqueVariablePartsWithoutDaySuffixForColumnHeader.add(variableID);
							}

							for (String day : extractUniqueDayValues(fieldIDsforColumnHeaders)) {
								List<String> row = new ArrayList<>(Collections.nCopies(columnNames.size(), ""));

								row.set(0, individualTransposedFormData.getCampaign().toString());
								row.set(1, individualTransposedFormData.getForm().toString());
								row.set(2, individualTransposedFormData.getArea().toString());
								row.set(3, individualTransposedFormData.getRcode().toString());
								row.set(4, individualTransposedFormData.getRegion().toString());
								row.set(5, individualTransposedFormData.getPcode() + "");
								row.set(6, individualTransposedFormData.getDistrict().toString());
								row.set(7, individualTransposedFormData.getDcode() + "");

								if (individualTransposedFormData.getCommunity() == null) {
									row.set(8, "");
									row.set(9, "");
									row.set(10, "");
								} else {
									row.set(8, individualTransposedFormData.getCommunity().toString());
									row.set(9, individualTransposedFormData.getClusternumber().toString());
									row.set(10, individualTransposedFormData.getCcode().toString());
								}

								row.set(11, individualTransposedFormData.getFormType().toString());
								row.set(12,
										individualTransposedFormData.getSource() != null
												? individualTransposedFormData.getSource().toString()
												: "");
								row.set(13,
										individualTransposedFormData.getCreatingUser() != null
												? individualTransposedFormData.getCreatingUser().toString()
												: "");
								row.set(14, day);

								for (String variable : uniqueVariablePartsWithoutDaySuffixForColumnHeader) {
									String key = variable + "_" + day;
									if (formDataMaxp.containsKey(key)) {
										String keyValue = formDataMaxp.get(key);
										int colIndex = columnNames.indexOf(variable);
										if (colIndex >= 0) {
											row.set(colIndex, keyValue);
										}
									}
								}

								// Ensure proper encoding for each cell in the row
								String[] rowArray = row.toArray(new String[0]);
								for (int i = 0; i < rowArray.length; i++) {
									if (rowArray[i] != null) {
										rowArray[i] = new String(rowArray[i].getBytes(StandardCharsets.UTF_8),
												StandardCharsets.UTF_8);
									}
								}

								writer.writeNext(rowArray);
							}
						}
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

	public static List<String> extractUniqueVariableParts(List<String> formEntriesID) {
		Set<String> variablePartsSet = new HashSet<>();
		Pattern pattern = Pattern.compile("(^(.+)day\\d+|^(.+)days\\d+)$");

		for (String id : formEntriesID) {
			Matcher matcher = pattern.matcher(id);
			if (matcher.find()) {
				variablePartsSet.add(matcher.group(1));
			}
		}
		// System.out.println("extractUniqueVariableParts-----" + variablePartsSet);
		return new ArrayList<>(variablePartsSet);
	}

	private static List<String> extractUniqueDayValues(List<String> formEntriesID) {
		Set<String> dayValuesSet = new HashSet<>();
		Pattern pattern = Pattern.compile("(day\\d+|days\\d+)$");

		for (String id : formEntriesID) {
			Matcher matcher1 = pattern.matcher(id);
			if (matcher1.find()) {
				dayValuesSet.add(matcher1.group());
			}

		}

		return new ArrayList<>(dayValuesSet);
	}

	public static String extractDay(String id) {
		Pattern pattern = Pattern.compile("_day\\d+$");
		Matcher matcher = pattern.matcher(id);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
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
	
	 private static Map<String, String> matchFieldsWithCaptions(
	            Set<String> fieldIDWithoutDaySuffix,
	            List<CampaignFormElement> campaignFormElements) {
	        
	        Map<String, String> fieldIdToCaptionMap = new HashMap<>();

	        for (String fieldId : fieldIDWithoutDaySuffix) {
	            for (CampaignFormElement element : campaignFormElements) {
	                String elementId = element.getId();
	                String elementIdWithoutDay = elementId.replaceAll("(_day\\d+|_days\\d+)$", "");
	                
	                if (elementIdWithoutDay.equals(fieldId)) {
	                    fieldIdToCaptionMap.put(fieldId, element.getCaption());
	                    break;
	                }
	            }
	        }

	        return fieldIdToCaptionMap;
	    }

}
