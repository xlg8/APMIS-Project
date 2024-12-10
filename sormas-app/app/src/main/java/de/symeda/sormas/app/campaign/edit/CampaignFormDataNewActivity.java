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

import static android.view.View.GONE;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.app.BaseEditActivity;
import de.symeda.sormas.app.BaseEditFragment;
import de.symeda.sormas.app.R;
import de.symeda.sormas.app.backend.campaign.Campaign;
import de.symeda.sormas.app.backend.campaign.data.CampaignFormData;
import de.symeda.sormas.app.backend.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.region.Community;
import de.symeda.sormas.app.backend.region.District;
import de.symeda.sormas.app.component.Item;
import de.symeda.sormas.app.component.menu.PageMenuItem;
import de.symeda.sormas.app.component.validation.FragmentValidator;
import de.symeda.sormas.app.core.async.AsyncTaskResult;
import de.symeda.sormas.app.core.async.SavingAsyncTask;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.core.notification.NotificationHelper;
import de.symeda.sormas.app.util.Bundler;

import static de.symeda.sormas.app.core.notification.NotificationType.ERROR;
import static de.symeda.sormas.app.core.notification.NotificationType.WARNING;
import static de.symeda.sormas.app.util.DataUtils.toItems;

public class CampaignFormDataNewActivity extends BaseEditActivity<CampaignFormData> {

    private AsyncTask saveTask;
    private Campaign campaign;
    private CampaignFormMeta campaignFormMeta;
    private CampaignFormDataCriteria criteria = new CampaignFormDataCriteria();

    public static void startActivity(Context context, String campaignUUID, String campaignFormMetaUUID) {
        BaseEditActivity.startActivity(context, CampaignFormDataNewActivity.class,
                BaseEditActivity.buildBundle(null).setCampaignUuid(campaignUUID).setCampaignFormMetaUuid(campaignFormMetaUUID));
    }

    @Override
    public void onCreateInner(@Nullable Bundle savedInstanceState) {
        super.onCreateInner(savedInstanceState);
        campaign = DatabaseHelper.getCampaignDao().queryUuid(new Bundler(savedInstanceState).getCampaignUuid());
        campaignFormMeta = DatabaseHelper.getCampaignFormMetaDao().queryUuid(new Bundler(savedInstanceState).getCampaignFormMetaUuid());
    }

    @Override
    protected CampaignFormData queryRootEntity(String recordUuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected CampaignFormData buildRootEntity() {
        CampaignFormData campaignFormData = DatabaseHelper.getCampaignFormDataDao().build();
        return campaignFormData;
    }

    @Override
    protected BaseEditFragment buildEditFragment(PageMenuItem menuItem, CampaignFormData activityRootData) {
        activityRootData.setCampaign(campaign);
        activityRootData.setCampaignFormMeta(campaignFormMeta);

        BaseEditFragment campaignFormDataNewFragment = CampaignFormDataNewFragment.newInstance(activityRootData);
        campaignFormDataNewFragment.setLiveValidationDisabled(true);
        return campaignFormDataNewFragment;
    }



    @Override
    public void saveData() {
        if (saveTask != null) {
            NotificationHelper.showNotification(this, WARNING, getString(R.string.message_already_saving));
            return; // don't save multiple times
        }

        final CampaignFormData campaignFormDataToSave = getStoredRootEntity();
        boolean saveChecker = true;
        criteria.setCampaign(campaign);
        criteria.setCampaignFormMeta(campaignFormMeta);
        criteria.setCommunity(campaignFormDataToSave.getCommunity());
        List<CampaignFormData> lotchecker = DatabaseHelper.getCampaignFormDataDao().queryByCriteria(criteria, 0, 100);

        campaignFormDataToSave.setFormCategory(campaignFormDataToSave.getCampaignFormMeta().getFormCategory());

        if(ConfigProvider.getUser().getUserRoles().contains(UserRole.SURVEILLANCE_OFFICER)){ // District Officer
            if(campaignFormDataToSave.getDistrict() !=  null){
                List<Community> newCommunities_ = DatabaseHelper.getCommunityDao().getByDistrict(campaignFormDataToSave.getDistrict());
                if (newCommunities_.size() > 0) {
                    campaignFormDataToSave.setCommunity(newCommunities_.get(0));
                }
            }
        }

        try {
            FragmentValidator.validate(getContext(), getActiveFragment().getContentBinding());
        } catch (ValidationException e) {
            NotificationHelper.showNotification(this, ERROR, e.getMessage());
            return;
        }

        final List<CampaignFormDataEntry> formValues = campaignFormDataToSave.getFormValues();
        final List<CampaignFormDataEntry> filledFormValues = new ArrayList<>();

        CampaignFormDataEntry lotNo = new CampaignFormDataEntry();
        CampaignFormDataEntry lotClusterNo = new CampaignFormDataEntry();

//        formValues.forEach(campaignFormDataEntry ->
        for(CampaignFormDataEntry campaignFormDataEntry : formValues) {
            if (campaignFormDataEntry.getId() != null && campaignFormDataEntry.getValue() != null) {
//                campaignFormDataEntry.getValue().toString().replaceAll(".0","");
                filledFormValues.add(campaignFormDataEntry);
                if (campaignFormDataEntry.getId().equalsIgnoreCase("LotNo")) {
                    lotNo = campaignFormDataEntry;
                }
                if (campaignFormDataEntry.getId().equalsIgnoreCase("LotClusterNo")) {
                    lotClusterNo = campaignFormDataEntry;
                }
            }
        };
//        );

        List<String> listLotNo = new ArrayList();
        List<String> listLotClusterNo = new ArrayList();

        if (lotchecker.size() > 0) {
            for (CampaignFormData campaignFormDataData : lotchecker) {
                List<CampaignFormDataEntry> lotOwnSec = campaignFormDataData.getFormValues();
                if (lotOwnSec.contains(lotNo)) {
                    listLotNo.add(lotOwnSec.get(lotOwnSec.indexOf(lotNo)).getValue().toString());
                }

                if (lotOwnSec.contains(lotClusterNo) && lotOwnSec.contains(lotNo)) {
                    listLotClusterNo.add(lotOwnSec.get(lotOwnSec.indexOf(lotClusterNo)).getValue().toString());
                }
            }
        }

        for (String string : listLotClusterNo) {
            if (listLotNo.size() > 0) {
            if ((Long.parseLong(string) - Long.parseLong(lotClusterNo.getValue().toString()) == 0)
                        && (Long.parseLong(listLotNo.get(0))
                        - Long.parseLong(lotNo.getValue().toString()) == 0)
            ) {
                saveChecker = false;
                break;
            }
            }
        }
        campaignFormDataToSave.setFormValues(filledFormValues);

        CampaignFormDataNewFragment activeFragment = (CampaignFormDataNewFragment) getActiveFragment();
        activeFragment.setLiveValidationDisabled(false);

        if (saveChecker) {
            saveTask = new SavingAsyncTask(getRootView(), campaignFormDataToSave) {

                @Override
                public void doInBackground(TaskResultHolder resultHolder) throws DaoException {

                    DatabaseHelper.getCampaignFormDataDao().saveAndSnapshot(campaignFormDataToSave);
                }

                @Override
                protected void onPostExecute(AsyncTaskResult<TaskResultHolder> taskResult) {
                    hidePreloader();
                    super.onPostExecute(taskResult);
                    if (taskResult.getResultStatus().isSuccess()) {
                        finish();
                        CampaignFormDataEditActivity.startActivity(getContext(), campaignFormDataToSave.getUuid());
                    }
                    saveTask = null;
                }
            }.executeOnThreadPool();
        } else {
            NotificationHelper.showNotification(this, WARNING, "Lot Cluster Number Already Exist for this Lot Number");
            return;
        }
    }

    @Override
    public Enum getPageStatus() {
        return null;
    }

    @Override
    protected int getActivityTitle() {
        return R.string.heading_campaign_form_data_new;
    }
}
