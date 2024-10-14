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
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.PlatformEnum;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.ValidationException;
import de.symeda.sormas.app.BaseActivity;
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
import de.symeda.sormas.app.component.menu.PageMenuItem;
import de.symeda.sormas.app.component.validation.FragmentValidator;
import de.symeda.sormas.app.core.async.AsyncTaskResult;
import de.symeda.sormas.app.core.async.SavingAsyncTask;
import de.symeda.sormas.app.core.async.TaskResultHolder;
import de.symeda.sormas.app.core.notification.NotificationHelper;
import de.symeda.sormas.app.util.Bundler;

import static de.symeda.sormas.app.core.notification.NotificationType.ERROR;
import static de.symeda.sormas.app.core.notification.NotificationType.WARNING;

import androidx.annotation.Nullable;

import org.springframework.core.env.SystemEnvironmentPropertySource;

public class CampaignFormDataEditActivity extends BaseEditActivity<CampaignFormData> {

    private AsyncTask saveTask;
    private Campaign campaign;
    private CampaignFormMeta campaignFormMeta;
    private CampaignFormDataCriteria criteria = new CampaignFormDataCriteria();

    public static void startActivity(Context context, String rootUuid) {
        BaseActivity.startActivity(context, CampaignFormDataEditActivity.class, buildBundle(rootUuid));
    }

    @Override
    protected CampaignFormData queryRootEntity(String recordUuid) {
        return DatabaseHelper.getCampaignFormDataDao().queryUuidWithEmbedded(recordUuid);
    }

    @Override
    protected CampaignFormData buildRootEntity() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected BaseEditFragment buildEditFragment(PageMenuItem menuItem, CampaignFormData activityRootData) {
        return CampaignFormDataEditFragment.newInstance(activityRootData);
    }

    @Override
    public void saveData() {

        if (saveTask != null) {
            NotificationHelper.showNotification(this, WARNING, getString(R.string.message_already_saving));
            return; // don't save multiple times
        }

        final CampaignFormData campaignFormDataToSave = getStoredRootEntity();
        campaign = DatabaseHelper.getCampaignDao().queryUuid(campaignFormDataToSave.getCampaign().getUuid());
        campaignFormMeta = DatabaseHelper.getCampaignFormMetaDao().queryUuid(campaignFormDataToSave.getCampaignFormMeta().getUuid());
        boolean saveChecker = true;
        criteria.setCampaign(campaign);
        criteria.setCampaignFormMeta(campaignFormMeta);
        criteria.setCommunity(campaignFormDataToSave.getCommunity());
        List<CampaignFormData> lotchecker = DatabaseHelper.getCampaignFormDataDao().queryByCriteria(criteria, 0, 100);

        System.out.println(campaignFormDataToSave.getCampaignFormMeta().getFormCategory()+">>>>>edit>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>__");
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
                filledFormValues.add(campaignFormDataEntry);
                if (campaignFormDataEntry.getId().equalsIgnoreCase("LotNo")) {
                    lotNo = campaignFormDataEntry;
                }
                if (campaignFormDataEntry.getId().equalsIgnoreCase("LotClusterNo")) {
                    lotClusterNo = campaignFormDataEntry;
                }
            }
        }
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
        campaignFormDataToSave.setSoruce(PlatformEnum.MOBILE);

        if (saveChecker) {
        saveTask = new SavingAsyncTask(getRootView(), campaignFormDataToSave) {

            @Override
            public void doInBackground(TaskResultHolder resultHolder) throws DaoException {
                DatabaseHelper.getCampaignFormDataDao().saveAndSnapshot(campaignFormDataToSave);
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<TaskResultHolder> taskResult) {
                super.onPostExecute(taskResult);

                if (taskResult.getResultStatus().isSuccess()) {
                    finish();
                } else {
                 //   onResume(); // reload data
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
        return R.string.heading_campaign_form_data_edit;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (saveTask != null && !saveTask.isCancelled())
            saveTask.cancel(true);
    }
}
