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

package de.symeda.sormas.app.backend.campaign.form;

import java.util.List;

import de.symeda.sormas.api.PushResult;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaExpiryDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaWithExpReferenceDto;
import de.symeda.sormas.api.clinicalcourse.ClinicalCourseReferenceDto;
import de.symeda.sormas.app.backend.clinicalcourse.ClinicalCourse;
import de.symeda.sormas.app.backend.common.AdoDtoHelper;
import de.symeda.sormas.app.rest.NoConnectionException;
import de.symeda.sormas.app.rest.RetroProvider;
import retrofit2.Call;

public class CampaignFormMetaWithExpDtoHelper extends AdoDtoHelper<CampaignFormMetaWithExp, CampaignFormMetaExpiryDto > {

    public static CampaignFormMetaWithExpReferenceDto toReferenceDto(CampaignFormMetaWithExp ado) {
        if (ado == null) {
            return null;
        }
        CampaignFormMetaWithExpReferenceDto dto = new CampaignFormMetaWithExpReferenceDto(ado.getUuid());
        return dto;
    }

    @Override
    protected Class<CampaignFormMetaWithExp> getAdoClass() {
        return CampaignFormMetaWithExp.class;
    }

    @Override
    protected Class<CampaignFormMetaExpiryDto> getDtoClass() {
        return CampaignFormMetaExpiryDto.class;
    }

    @Override
    protected Call<List<CampaignFormMetaExpiryDto>> pullAllSince(long since) throws NoConnectionException {
        return RetroProvider.getCampaignFormMetaWithExpFacade().getAllFormsWithExpiry();

    }



    @Override
    protected Call<List<CampaignFormMetaExpiryDto>> pullByUuids(List<String> uuids) throws NoConnectionException {

        return RetroProvider.getCampaignFormMetaWithExpFacade().getAllFormsWithExpiry();
    }

    @Override
    protected Call<List<PushResult>> pushAll(List<CampaignFormMetaExpiryDto> campaignFormMetaExpiryDtos) throws NoConnectionException {
        return null;
    }

    @Override
    protected void fillInnerFromDto(CampaignFormMetaWithExp ado  , CampaignFormMetaExpiryDto dto) {
        ado.setCampaignId(dto.getCampaignId());
        ado.setExpiryDay(dto.getExpiryDay());
        ado.setFormId(dto.getFormId());
        ado.setExpiryDate(dto.getEnddate());

    }

    @Override
    protected void fillInnerFromAdo(CampaignFormMetaExpiryDto target, CampaignFormMetaWithExp source) {
        target.setExpiryDay(source.getExpiryDay());
        target.setCampaignId(source.getCampaignId());
        target.setFormId(source.getFormId());
        target.setEnddate(source.getExpiryDate());

    }

}
