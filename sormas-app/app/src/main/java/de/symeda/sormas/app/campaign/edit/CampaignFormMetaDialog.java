/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.app.campaign.edit;

import android.content.Context;

import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.api.utils.fieldaccess.UiFieldAccessCheckers;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.campaign.Campaign;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.component.dialog.FormDialog;
import de.symeda.sormas.app.component.validation.FragmentValidator;
import de.symeda.sormas.app.core.notification.NotificationHelper;
import de.symeda.sormas.app.databinding.DialogSelectCampaignFormMetaLayoutBinding;
import de.symeda.sormas.app.util.DataUtils;

import static de.symeda.sormas.app.core.notification.NotificationType.ERROR;

public class CampaignFormMetaDialog extends FormDialog {

    private DialogSelectCampaignFormMetaLayoutBinding contentBinding;
    private Campaign campaign;

    public CampaignFormMetaDialog(final FragmentActivity activity, Campaign campaign) {
        super(
                activity,
                R.layout.dialog_root_layout,
                R.layout.dialog_select_campaign_form_meta_layout,
                R.layout.dialog_root_two_button_panel_layout,
                R.string.heading_campaign_form_meta_select,
                -1,
                UiFieldAccessCheckers.forSensitiveData(campaign.isPseudonymized()));

        this.campaign = campaign;
    }

    @Override
    protected void setContentBinding(Context context, ViewDataBinding binding, String layoutName) {
        this.contentBinding = (DialogSelectCampaignFormMetaLayoutBinding) binding;
    }

    @Override
    protected void initializeContentView(ViewDataBinding rootBinding, ViewDataBinding buttonPanelBinding) {
        List<CampaignFormMeta> allFormsForCampaign = campaign.getCampaignFormMetas();
        List<CampaignFormMeta> allUnexpiredFormsForCampaign = new ArrayList<>();
        for (CampaignFormMeta campaignFormMeta : allFormsForCampaign) {
            Date expiryDate = DatabaseHelper.getCampaignFormMetaWithExpDao().getCampaignFormExpiryDateByCampaignIdAndFormId(campaign.getUuid(), campaignFormMeta.getUuid());
            LocalDate currentDate = LocalDate.now();
            if (expiryDate != null) {
            LocalDate expiryLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();


                if (currentDate.isBefore(expiryLocalDate) || expiryLocalDate.isEqual(currentDate)) {
                    System.out.println("Checking for form  " + campaignFormMeta.getFormName());
                    System.out.println("This form has Expired Dte is " + expiryLocalDate + " current date is " + currentDate);
                    allUnexpiredFormsForCampaign.add(campaignFormMeta);
//                // expiryDate is before currentDate or equals tob the current date itshold be added to my new list
            }  else {
                // expiryDate is after currentDate
                    System.out.println("This form has Expired Dte is " + expiryLocalDate + " current date is " + currentDate);
                    System.out.println("This form has Expired For Data Entry " + campaignFormMeta.getFormName());
            }
            } else {
                System.out.println("This form does not have an expiry date set  " + campaignFormMeta.getFormName());

            }
}
        Collections.sort(allUnexpiredFormsForCampaign, Comparator.comparing(CampaignFormMeta::getFormName));
        contentBinding.campaignFormMeta.initializeSpinner(DataUtils.toItems(allUnexpiredFormsForCampaign));
    }

    public CampaignFormMeta getCampaignFormMeta() {
        return (CampaignFormMeta) contentBinding.campaignFormMeta.getValue();
    }

    @Override
    protected void onPositiveClick() {
        setLiveValidationDisabled(false);
        try {
            FragmentValidator.validate(getContext(), contentBinding);
        } catch (ValidationException e) {
            NotificationHelper.showDialogNotification(CampaignFormMetaDialog.this, ERROR, e.getMessage());
            return;
        }

        super.setCloseOnPositiveButtonClick(true);
        super.onPositiveClick();
    }
}
