package de.symeda.sormas.app.backend.campaign.form;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.symeda.sormas.app.backend.campaign.Campaign;
import de.symeda.sormas.app.backend.common.AbstractAdoDao;

public class CampaignFormMetaWithExpDao extends AbstractAdoDao<CampaignFormMetaWithExp> {



    public CampaignFormMetaWithExpDao(Dao<CampaignFormMetaWithExp, Long> innerDao) {
        super(innerDao);
    }

    @Override
    protected Class<CampaignFormMetaWithExp> getAdoClass() {
        return CampaignFormMetaWithExp.class;
    }

    @Override
    public String getTableName() {
        return CampaignFormMetaWithExp.TABLE_NAME;
    }


//    public List<String> getCampaignFormsUuidsByExpiryDate(String campaignUuid) {
//        try{
//            QueryBuilder<String, Long> queryBuilder = queryBuilder();
//
//        }catch {
//
//        }
//    }
public List<String> getCampaignFormsUuidsByExpiryDate(String campaignUuid) {
    List<String> uuids = new ArrayList<>();
    try {


        // Create a query builder
        QueryBuilder<CampaignFormMetaWithExp, Long> queryBuilder = queryBuilder();

        // Set the query criteria
        List<Where<CampaignFormMetaWithExp, Long>> whereStatements = new ArrayList<>();

        Where<CampaignFormMetaWithExp, Long> where = queryBuilder.where();
        whereStatements.add(where.eq("campaignId", campaignUuid));

        // Prepare the query
        PreparedQuery<CampaignFormMetaWithExp> preparedQuery = queryBuilder.prepare();

        // Execute the query and retrieve the results
//        List<CampaignFormMetaWithExp> results = query(preparedQuery);
        List<CampaignFormMetaWithExp> results = queryBuilder.query(); // Here is the change

        // Extract UUIDs from the results
        for (CampaignFormMetaWithExp result : results) {
            uuids.add(result.getUuid());
        }
    } catch (SQLException e) {
        // Handle any exceptions
        e.printStackTrace();
    }
    return uuids;
}

    public Date getCampaignFormExpiryDateByCampaignIdAndFormId(String campaignUuid, String formUuid) {
        Date expiryDate = null;
        try {
            // Create a query builder
            QueryBuilder<CampaignFormMetaWithExp, Long> queryBuilder = queryBuilder();

            // Set the query criteria
            Where<CampaignFormMetaWithExp, Long> where = queryBuilder.where();
            where.eq("campaignId", campaignUuid).and().eq("formId", formUuid);

            // Prepare the query
            PreparedQuery<CampaignFormMetaWithExp> preparedQuery = queryBuilder.prepare();

            // Execute the query and retrieve the result
            List<CampaignFormMetaWithExp> results = queryBuilder.query(); // Here is the change

//            CampaignFormMetaWithExp result = query(preparedQuery);


            // Extract the expiry date from the result
            for(CampaignFormMetaWithExp result : results){
                if (result != null) {
                    expiryDate = result.getExpiryDate();
                }
            }

        } catch (SQLException e) {
            // Handle any exceptions
            e.printStackTrace();
        }
        return expiryDate;
    }


}

