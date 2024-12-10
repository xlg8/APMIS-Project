/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.app.campaign;

import static de.symeda.sormas.api.campaign.ExpressionProcessorUtils.refreshEvaluationContext;
import static de.symeda.sormas.api.utils.FieldConstraints.CHARACTER_LIMIT_DEFAULT;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;

import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.app.component.controls.ControlCheckBoxField;
import de.symeda.sormas.app.component.controls.ControlDateField;
import de.symeda.sormas.app.component.controls.ControlPropertyField;
import de.symeda.sormas.app.component.controls.ControlSpinnerField;
import de.symeda.sormas.app.component.controls.ControlSwitchField;
import de.symeda.sormas.app.component.controls.ControlTextEditField;
import de.symeda.sormas.app.component.controls.ControlTextReadField;
import de.symeda.sormas.app.util.YesNo;

public class CampaignFormDataFragmentUtils {
    public static final int DEFAULT_MIN_LENGTH = 1;

    private CampaignFormDataFragmentUtils() {
    }

    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static void handleExpressionSec(
            ExpressionParser expressionParser,
            List<CampaignFormDataEntry> formValues,
            CampaignFormElementType type,
            ControlPropertyField dynamicField,
            String expressionString,
            Boolean isDisIgnore,
            Object orginalValue) {
        try {
            if(!expressionString.isEmpty() && expressionString != null && !expressionString.equals("")) {
                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                String valuex = expressionValue + "";

             //   System.out.println(dynamicField.getCaption()+" =------))))))))))))))))))))))))))))))))))))))-----= " + valuex);
                if (!valuex.isEmpty() && !valuex.equals("") && expressionValue != null) {//&& !valuex.equals("0")

                    if (expressionValue != null) { //we need to see how to check and filter when its blank or empty

                        //     System.out.println(dynamicField.getCaption()+" : "+ expressionString+" =====)))))))))))))))==== "+expressionValue);
                        if (type == CampaignFormElementType.YES_NO) {
                            ControlSwitchField.setValue((ControlSwitchField) dynamicField, expressionValue, true, YesNo.class, null);
                        } else if (type == CampaignFormElementType.RANGE) {
                         //   System.out.println("+++++++++++111111+++++++++++++++++ " + valuex);
                            String valudex = valuex.equals("0") ? null : valuex.endsWith(".0") ? valuex.replace(".0", "") : valuex;
                         //   System.out.println(orginalValue+ "++++++++++++22222222++++++++++++++++ " + valudex);
                            if (orginalValue != null) {
                                if (!orginalValue.toString().equals(valudex)) {
                                    System.out.println(orginalValue + "from handlesec++++++++++++2222333++++++++++++++++ " + valudex);
                                    if (!(orginalValue.toString().isEmpty() && valudex == null)) {
                                        //   if (!valudex.isEmpty() && !orginalValue.toString().equals(valudex)) {
                                        System.out.println("from handlesec++++++++++++333333333++++++++++++++++ " + valudex);
                                        ControlTextEditField.setValue((ControlTextEditField) dynamicField, expressionValue.toString().equals("0") ? null : expressionValue.toString().endsWith(".0") ? expressionValue.toString().replace(".0", "") : expressionValue.toString());
                                        // }
                                    }
                                }
                            }else {
                                if (valudex != null) {
                                        //   if (!valudex.isEmpty() && !orginalValue.toString().equals(valudex)) {
                                //        System.out.println("++++++++++++333333333++++++++++++++++ " + valudex);
                                        ControlTextEditField.setValue((ControlTextEditField) dynamicField, expressionValue.toString().equals("0") ? null : expressionValue.toString().endsWith(".0") ? expressionValue.toString().replace(".0", "") : expressionValue.toString());
                                        // }
                                    }


                                }

                        } else if (type == CampaignFormElementType.NUMBER) {
                            ControlTextEditField.setValue((ControlTextEditField) dynamicField, expressionValue.toString().equals("0") ? "0" : (expressionValue.toString().endsWith(".0") ? expressionValue.toString().replace(".0", "") : expressionValue.toString()));

                        } else if (expressionValue.getClass().isAssignableFrom(Boolean.class)) {
                            ControlTextEditField.setValue((ControlTextEditField) dynamicField, (Double) (!Double.isFinite((double) expressionValue) ? 0 : expressionValue.toString().endsWith(".0") ? expressionValue.toString().replace(".0", "") : df.format((double) expressionValue)));
                        } else {
                            ControlTextEditField.setValue((ControlTextEditField) dynamicField, expressionValue == null ? null : expressionValue.toString());
                        }

                        //  if (type == CampaignFormElementType.RANGE) {

                        //  } else {
                        //     dynamicField.setEnabled(isDisIgnore);
                        // }
                    }

                    // Segun alleged Fix
//                    else{
//                        System.out.println(dynamicField.getCaption() + "EXPRESSION VALUE IS NOT NULLLLLLLLLLLLLLL+++++++++++111111+++++++++++++++++ " + valuex);
//                        if (type == CampaignFormElementType.RANGE) {
//                            System.out.println("+++++++++++111111+++++++++++++++++ " + valuex);
//                            String valudex = valuex.equals("0") ? null : valuex.endsWith(".0") ? valuex.replace(".0", "") : valuex;
//                            System.out.println("++++++++++++22222222++++++++++++++++ " + valudex);
//                            if (valudex != null) {
//                                if (!valudex.isEmpty()) {
//                                    System.out.println("++++++++++++333333333++++++++++++++++ " + valudex);
//                                    ControlTextEditField.setValue((ControlTextEditField) dynamicField, valudex);
//                                }
//                            }
//                        }
//                    }


                }


            }else{

            }
        } catch (SpelEvaluationException e) {
            Log.e("Error evaluating expression on field : " + dynamicField.getCaption(), e.getMessage());
        }
        if (type == CampaignFormElementType.RANGE) {
            dynamicField.setEnabled(true);
        }else if (isDisIgnore) {
            dynamicField.setEnabled(true);
        }else{
            dynamicField.setEnabled(false);
        }

    }




    public static void handleExpression(
            ExpressionParser expressionParser,
            List<CampaignFormDataEntry> formValues,
            CampaignFormElementType type,
            ControlPropertyField dynamicField,
            String expressionString,
            Boolean isDisIgnore) {
        try {
            if(!expressionString.isEmpty() && expressionString != null && !expressionString.equals("")) {
                final Object expressionValue = getExpressionValue(expressionParser, formValues, expressionString);
                String valuex = expressionValue + "";
                ;
                System.out.println("second method ___)))))))))))))))))))))))))))))))))))))))))))))-----= " + valuex);
                if (!valuex.isEmpty() && !valuex.equals("") && expressionValue != null) {//&& !valuex.equals("0")

                    if (expressionValue != null) { //we need to see how to check and filter when its blank or empty

                        //     System.out.println(dynamicField.getCaption()+" : "+ expressionString+" =====)))))))))))))))==== "+expressionValue);
                        if (type == CampaignFormElementType.YES_NO) {
                            ControlSwitchField.setValue((ControlSwitchField) dynamicField, expressionValue, true, YesNo.class, null);
                        } else if (type == CampaignFormElementType.RANGE) {
                            System.out.println("+++++++++++111111+++++++++++++++++ " + valuex);
                            String valudex = valuex.equals("0") ? null : valuex.endsWith(".0") ? valuex.replace(".0", "") : valuex;
                            System.out.println("++++++++++++22222222++++++++++++++++ " + valudex);
                            if (valudex != null) {
                                if (!valudex.isEmpty()) {
                                    System.out.println("++++++++++++333333333++++++++++++++++ " + valudex);
                                    ControlTextEditField.setValue((ControlTextEditField) dynamicField, valudex.replace(".0",""));
                                }
                            }
                        } else if (type == CampaignFormElementType.NUMBER) {
                            ControlTextEditField.setValue((ControlTextEditField) dynamicField, expressionValue.toString().equals("0") ? "0" : (expressionValue.toString().contains(".0") ? expressionValue.toString().replace(".0", "") : expressionValue.toString()));

                        } else if (expressionValue.getClass().isAssignableFrom(Boolean.class)) {
                            ControlTextEditField.setValue((ControlTextEditField) dynamicField, (Double) (!Double.isFinite((double) expressionValue) ? 0 : expressionValue.toString().endsWith(".0") ? expressionValue.toString().replace(".0", "") : df.format((double) expressionValue)));
                        } else {
                            ControlTextEditField.setValue((ControlTextEditField) dynamicField, expressionValue == null ? null : expressionValue.toString());
                        }

                        //  if (type == CampaignFormElementType.RANGE) {

                        //  } else {
                        //     dynamicField.setEnabled(isDisIgnore);
                        // }
                    }
                    // Segun alleged Fix
//                    else{
//                        System.out.println(dynamicField.getCaption() + "EXPRESSION VALUE IS NOT NULLLLLLLLLLLLLLL+++++++++++111111+++++++++++++++++ " + valuex);
//                        if (type == CampaignFormElementType.RANGE) {
//                            System.out.println("+++++++++++111111+++++++++++++++++ " + valuex);
//                            String valudex = valuex.equals("0") ? null : valuex.endsWith(".0") ? valuex.replace(".0", "") : valuex;
//                            System.out.println("++++++++++++22222222++++++++++++++++ " + valudex);
//                            if (valudex != null) {
//                                if (!valudex.isEmpty()) {
//                                    System.out.println("++++++++++++333333333++++++++++++++++ " + valudex);
//                                    ControlTextEditField.setValue((ControlTextEditField) dynamicField, valudex);
//                                }
//                            }
//                        }
//                    }


                }


            }
        } catch (SpelEvaluationException e) {
            Log.e("Error evaluating expression on field2 : " + dynamicField.getCaption(), e.getMessage());
        }
        if (type == CampaignFormElementType.RANGE) {
            dynamicField.setEnabled(true);
        }else if (isDisIgnore) {
            dynamicField.setEnabled(true);
        }else{
            dynamicField.setEnabled(false);
        }

    }

    public static Object getExpressionValue(ExpressionParser expressionParser, List<CampaignFormDataEntry> formValues, String expressionString)
            throws SpelEvaluationException, EvaluationException {




        System.out.println("111111111" +expressionString);
        final EvaluationContext context = refreshEvaluationContext(formValues);
        System.out.println("2222222222222");
        final Expression expression = expressionParser.parseExpression(expressionString);
        System.out.println("3333333333333333333");
        final Class<?> valueType = expression.getValueType(context);
        final Object valueFin = expression.getValue(context, valueType);
      //  System.out.println(valueType+" )))))))))---- "+valueFin+" ------------- "+valueFin.getClass());
        if(valueFin != null) {
            if (!valueFin.getClass().isAssignableFrom(valueType)) {
                //   if(valueFin instanceof valueType){}
                System.out.println("EmptyStackException: >>>>>>-");
                throw new EmptyStackException();
            }
        } else{

            throw new SpelEvaluationException(SpelMessage.NOT_A_REAL);
        }
        return expression.getValue(context, valueType);
    }


    public static void handleDependingOn(
            Map<String, ControlPropertyField> fieldMap,
            CampaignFormElement campaignFormElement,
            ControlPropertyField dynamicField) {
        final String dependingOn = campaignFormElement.getDependingOn();
        final String[] dependingOnValues = campaignFormElement.getDependingOnValues();

       List<String> constraints;
       String depenValuexd = null;
        if (dependingOnValues != null) {
            constraints = (List) Arrays.stream(dependingOnValues).collect(Collectors.toList());
            ListIterator<String> lstItemsx = constraints.listIterator();
            if (lstItemsx.hasNext()) {
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@1111>>>>>>>> "+lstItemsx);

                depenValuexd = lstItemsx.next().toString();
            }
        }

        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@22222>>>>>>>> "+depenValuexd);

        final String depenValuex = depenValuexd;

        if (dependingOn != null && depenValuex != null) {
            ControlPropertyField controlPropertyField = fieldMap.get(dependingOn);
            setVisibilityDependency(dynamicField, depenValuex, controlPropertyField.getValue());
            final ControlPropertyField finalDynamicField = dynamicField;
            controlPropertyField.addValueChangedListener(field -> {
                final String manVal = depenValuex;
                setVisibilityDependency(finalDynamicField, manVal, field.getValue());
            });
            };
        }


    public static void setVisibilityDependency(ControlPropertyField field, String dependingOnValues, Object dependingOnFieldValue) {
        System.out.println(dependingOnValues+ " = static   @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@22>>>>>>>> dynamic = "+dependingOnFieldValue);

        String parsedDependingOnFieldValue = dependingOnFieldValue == null
                ? ""
                : dependingOnFieldValue instanceof Boolean
                ? YesNoUnknown.valueOf(((Boolean) dependingOnFieldValue).booleanValue()).name()
                : dependingOnFieldValue.toString().equalsIgnoreCase("Yes") ? "true" : dependingOnFieldValue.toString().equalsIgnoreCase("No") ? "false" : dependingOnFieldValue.toString();

        System.out.println(" = dddddddddddddddddddddddddddddddd   @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@22>>>>>>>> dynamic = " + parsedDependingOnFieldValue);
        if (dependingOnValues.contains("!")) {


            dependingOnValues = dependingOnValues.replace("!", "");
            if (dependingOnValues.contains(parsedDependingOnFieldValue)) {
                field.setVisibility(View.GONE);
                if(field.getValue() != null){
                    field.setValue(null);
                }
            } else {

                field.setVisibility(View.VISIBLE);
            }
        } else {
             if (dependingOnValues.equalsIgnoreCase(parsedDependingOnFieldValue)) {
                System.out.println(parsedDependingOnFieldValue+ " VISIBLE   @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@22>>>>>>>> "+dependingOnFieldValue);

                 field.setVisibility(View.VISIBLE);

            } else {
                System.out.println(parsedDependingOnFieldValue+ " GONE   "+field.getCaption()+"   @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@22>>>>>>>> "+dependingOnFieldValue);

                 field.setVisibility(View.GONE);
                 if(field.getValue() != null){
                     field.setValue(null);
                 }
            }
        }
    }

    private static boolean containsIgnoreCase(List<String> list, String soughtFor) {
        for (String current : list) {
            if (current.equalsIgnoreCase(soughtFor)) {
                return true;
            }
        }
        return false;
    }

    public static CampaignFormDataEntry getOrCreateCampaignFormDataEntry(
            List<CampaignFormDataEntry> formValues,
            CampaignFormElement campaignFormElement) {
        for (CampaignFormDataEntry campaignFormDataEntry : formValues) {
            if (campaignFormDataEntry.getId().equals(campaignFormElement.getId())) {
                return campaignFormDataEntry;
            }
        }
        final CampaignFormDataEntry newCampaignFomDataEntry = new CampaignFormDataEntry(campaignFormElement.getId(), null);
        formValues.add(newCampaignFomDataEntry);
        return newCampaignFomDataEntry;
    }

    public static Map<String, String> getUserTranslations(CampaignFormMeta campaignFormMeta) {
        final Map<String, String> userTranslations = new HashMap<>();

        final Locale locale = I18nProperties.getUserLanguage().getLocale();
        if (locale != null) {
            final List<CampaignFormTranslations> campaignFormTranslations = campaignFormMeta.getCampaignFormTranslations();
            campaignFormTranslations.forEach(cft -> {
                if (cft.getLanguageCode().equalsIgnoreCase(locale.toString())) {
                    cft.getTranslations()
                            .forEach(translationElement -> userTranslations.put(translationElement.getElementId(), translationElement.getCaption()));
                }
            });
        }
        return userTranslations;
    }

    public static String getUserLanguageCaption(Map<String, String> userTranslations, CampaignFormElement campaignFormElement) {
        if (userTranslations != null && userTranslations.containsKey(campaignFormElement.getId())) {
            return userTranslations.get(campaignFormElement.getId());
        } else {
            return campaignFormElement.getCaption();
        }
    }

    public static ControlTextEditField createControlTextEditField(
            CampaignFormElement campaignFormElement,
            Context context,
            Map<String, String> userTranslations,
            Boolean isIntegerField,
            Boolean isRequired) {
        return new ControlTextEditField(context) {

            @Override
            protected String getPrefixDescription() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            protected String getPrefixCaption() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            public int getTextAlignment() {
                return View.TEXT_ALIGNMENT_VIEW_START;
            }

            @Override
            public int getGravity() {
                return Gravity.CENTER_VERTICAL;
            }

            @Override
            public int getMaxLines() {
                return 1;
            }

            @Override
            public int getMaxLength() {
                return CHARACTER_LIMIT_DEFAULT;
            }

            //	@Override
            //	public int getMinLength() {
            //		return DEFAULT_MIN_LENGTH;
            //	}
//
            @Override
            protected void inflateView(Context context, AttributeSet attrs, int defStyle) {
                super.inflateView(context, attrs, defStyle);
                initLabel();
                initLabelAndValidationListeners();
                setLiveValidationDisabled(true);
                initInput(isIntegerField, isRequired, false, null, null, false, false);
            }
        };
    }

    public static ControlTextEditField createControlTextEditFieldRangex(
            CampaignFormElement campaignFormElement,
            Context context,
            Map<String, String> userTranslations,
            Boolean isIntegerField,
            Boolean isRequired,
            String errorMsg) {
        return new ControlTextEditField(context) {

            @Override
            protected String getPrefixDescription() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            protected String getPrefixCaption() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            public int getTextAlignment() {
                return View.TEXT_ALIGNMENT_VIEW_START;
            }

            @Override
            public int getGravity() {
                return Gravity.CENTER_VERTICAL;
            }

            @Override
            public int getMaxLines() {
                return 1;
            }

            @Override
            public int getMaxLength() {
                return CHARACTER_LIMIT_DEFAULT;
            }

            //	@Override
            //	public int getMinLength() {
            //		return DEFAULT_MIN_LENGTH;
            //	}
//
            @Override
            protected void inflateView(Context context, AttributeSet attrs, int defStyle) {
                super.inflateView(context, attrs, defStyle);
                initLabel();
                initLabelAndValidationListenersErrorMsg(errorMsg);
                setLiveValidationDisabled(true);
                initInput(isIntegerField, isRequired, true, null, null, true, false);
            }
        };
    }

    public static ControlTextEditField createControlTextEditFieldRange(
            CampaignFormElement campaignFormElement,
            Context context,
            Map<String, String> userTranslations,
            Boolean isIntegerField,
            Boolean isRequired,
            Integer minVal,
            Integer maxVal,
            Boolean isExpression,
            Boolean warnOnError) {

        System.out.println(context+" --------------------- running range stage 1 : "+isExpression);
        final boolean isExpressionx = isExpression;
        return new ControlTextEditField(context) {



            @Override
            protected String getPrefixDescription() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            protected String getPrefixCaption() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            public int getTextAlignment() {
                return View.TEXT_ALIGNMENT_VIEW_START;
            }

            @Override
            public int getGravity() {
                return Gravity.CENTER_VERTICAL;
            }

            @Override
            public int getMaxLines() {
                return 1;
            }

            @Override
            public int getMaxLength() {
                return 8;
            }

            @Override
            public int getMinLength() {
                return 1;
            }

            //
            @Override
            protected void inflateView(Context context, AttributeSet attrs, int defStyle) {
                super.inflateView(context, attrs, defStyle);
                initLabel();
                initLabelAndValidationListeners();
                setLiveValidationDisabled(true);
                initInput(isIntegerField, isRequired, true, minVal, maxVal, isExpressionx, warnOnError);
//                setVisibility(GONE);
            }


        };
    }


    public static ControlSpinnerField createControlSpinnerFieldEditField(
            CampaignFormElement campaignFormElement,
            Context context,
            Map<String, String> userTranslations,
            Map<String, String> isIntegerField) {
        return new ControlSpinnerField(context) {

            @Override
            protected String getPrefixDescription() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            protected String getPrefixCaption() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            public int getTextAlignment() {
                return View.TEXT_ALIGNMENT_VIEW_START;
            }

            @Override
            public int getGravity() {
                return Gravity.CENTER_VERTICAL;
            }

            @Override
            protected void inflateView(Context context, AttributeSet attrs, int defStyle) {
                super.inflateView(context, attrs, defStyle);
                initLabel();
                initLabelAndValidationListeners();
                setLiveValidationDisabled(true);
                initInput(isIntegerField);
            }
        };
    }


    public static ControlDateField createControlDateEditField(
            CampaignFormElement campaignFormElement,
            Context context,
            Map<String, String> userTranslations,
            Boolean isIntegerField,
            FragmentManager fm, boolean isRequired) {
        return new ControlDateField(context) {

            @Override
            protected String getPrefixDescription() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            protected String getPrefixCaption() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            public int getTextAlignment() {
                return View.TEXT_ALIGNMENT_VIEW_START;
            }

            @Override
            public int getGravity() {
                return Gravity.CENTER_VERTICAL;
            }


            @Override
            protected void inflateView(Context context, AttributeSet attrs, int defStyle) {
                super.inflateView(context, attrs, defStyle);
                initLabel();
                initLabelAndValidationListeners();
                setLiveValidationDisabled(true);
                initializeDateField(fm);
                initInput(true, isRequired);
            }
        };
    }


    public static ControlCheckBoxField createControlCheckBoxField(
            CampaignFormElement campaignFormElement,
            Context context,
            Map<String, String> userTranslations) {
        return new ControlCheckBoxField(context) {

            @Override
            protected String getPrefixDescription() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            protected String getPrefixCaption() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            public int getTextAlignment() {
                return View.TEXT_ALIGNMENT_VIEW_START;
            }

            @Override
            public int getGravity() {
                return Gravity.CENTER_VERTICAL;
            }

            @Override
            protected void inflateView(Context context, AttributeSet attrs, int defStyle) {
                super.inflateView(context, attrs, defStyle);
                initLabel();
                initLabelAndValidationListeners();
                setLiveValidationDisabled(true);
                //required = true;

                initInput();
            }
        };
    }



    public static ControlSwitchField createControlYesNoUnknownField(
            CampaignFormElement campaignFormElement,
            Context context,
            Map<String, String> userTranslations) {
        return new ControlSwitchField(context) {

            @Override
            protected String getPrefixDescription() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            protected String getPrefixCaption() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            public int getTextAlignment() {
                return View.TEXT_ALIGNMENT_VIEW_START;
            }

            @Override
            public int getGravity() {
                return Gravity.CENTER_VERTICAL;
            }

            @Override
            protected void inflateView(Context context, AttributeSet attrs, int defStyle) {
                super.inflateView(context, attrs, defStyle);
                initLabel();
                initLabelAndValidationListeners();
                required = true;

               // initialize();
                setLiveValidationDisabled(true);
                initInputFirst();
            }
        };
    }




    public static ControlTextReadField createControlTextReadField(
            CampaignFormElement campaignFormElement,
            Context context,
            Map<String, String> userTranslations) {
        return new ControlTextReadField(context) {

            @Override
            protected String getPrefixDescription() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            protected String getPrefixCaption() {
                return getUserLanguageCaption(userTranslations, campaignFormElement);
            }

            @Override
            public int getTextAlignment() {
                return View.TEXT_ALIGNMENT_VIEW_START;
            }

            @Override
            public int getGravity() {
                return Gravity.CENTER_VERTICAL;
            }

            @Override
            public int getMaxLines() {
                return 1;
            }

            @Override
            protected void inflateView(Context context, AttributeSet attrs, int defStyle) {
                super.inflateView(context, attrs, defStyle);
                initLabel();
                initTextView();
            }
        };
    }

}
