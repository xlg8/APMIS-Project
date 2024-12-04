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

package de.symeda.sormas.api.campaign.data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;

public class CampaignFormDataIndexDto implements Serializable, Cloneable {

	public static final String I18N_PREFIX = "CampaignFormData";

	public static final String UUID = "uuid";
	public static final String CAMPAIGN = "campaign";
	public static final String FORM = "form";
	public static final String AREA = "area";
	public static final String RCODE = "rcode";
	public static final String REGION = "region";
	public static final String PCODE = "pcode";
	public static final String DISTRICT = "district";
	public static final String DCODE = "dcode";
	public static final String COMMUNITY = "community";
	public static final String COMMUNITYNUMBER = "clusternumber";
	public static final String COMMUNITYNUMBER_ = "clusternumber_";
	public static final String CCODE = "ccode";
	public static final String FORM_DATE = "formDate";
	public static final String FORM_VALUES = "formValues";
	public static final String FORM_TYPE = "formType";
	public static final String ANALYSIS_FIELD_A = "analysis_a";
	public static final String ANALYSIS_FIELD_B = "analysis_b";
	public static final String ANALYSIS_FIELD_C = "analysis_c";
	public static final String ANALYSIS_FIELD_D = "analysis_d";
	public static final String CAMPAIGN_ID = "campaign_id";
	public static final String ERROR_REPORT = "error_status";

	public static final String ANALYSIS_FIELD_A_ = "analysis_a_";
	public static final String ANALYSIS_FIELD_B_ = "analysis_b_";
	public static final String ANALYSIS_FIELD_C_ = "analysis_c_";
	public static final String ANALYSIS_FIELD_D_ = "analysis_d_";

	public static final String ISVERIFIED = "isverified";
	public static final String ISPUBLISHED = "ispublished";

	public static final String SOURCE = "source";
	public static final String CREATED_BY = "creatingUser";
	public static final String CREATINGUSER_USERTYPE = "creatingUserType";

	public static final String PERSON_TITLE = "personTitle";

	private static final long serialVersionUID = -6672198324526771162L;

	private String uuid;
	private String campaign;
	private String form;
	private List<CampaignFormDataEntry> formValues;
	private String area;
	private Long rcode;
	private String region;
	private Long pcode;
	private String district;
	private Long dcode;
	private String community;
	private Integer clusternumber;
	private Long clusternumber_;
	private Long ccode;
	private Date formDate;
	private String formType;
	private Long analysis_a;
	private Long analysis_b;
	private Long analysis_c;
	private Long analysis_d;
	private Long campaign_id;
	private String error_status;

	private Integer analysis_a_;
	private Integer analysis_b_;
	private Integer analysis_c_;
	private Integer analysis_d_;
	private Integer analysis_e_;
	private Integer analysis_f_;
	private Integer analysis_g_;


	private boolean isverified;
	private boolean ispublished;

	private String source;
	private String creatingUser;
	private String creatingUserType;

	private String personTitle;

	public CampaignFormDataIndexDto(String uuid, String campaign, String form, Object formValues, String area,
			Long rcode, String region, Long pcode, String district, Long dcode, String community, Integer clusternumber,
			Long ccode, Date formDate, String formType, String source, String creatingUser, boolean isverified,
			boolean ispublished) {
		this.uuid = uuid;
		this.campaign = campaign;
		this.form = form;
		this.formValues = (List<CampaignFormDataEntry>) formValues;
		this.area = area;
		this.rcode = rcode;
		this.region = region;
		this.pcode = pcode;
		this.district = district;
		this.dcode = dcode;
		this.community = community;
		this.clusternumber = clusternumber;
		this.ccode = ccode;
		this.formDate = formDate;
		this.formType = formType;
		this.source = source;
		this.creatingUser = creatingUser;
		this.isverified = isverified;
		this.ispublished = ispublished;
	}

	public CampaignFormDataIndexDto(String uuid, String campaign, String form, Object formValues, String area,
			Long rcode, String region, Long pcode, String district, Long dcode, String community, Integer clusternumber,
			Long ccode, Date formDate, String formType, String source, String creatingUser) {
		this.uuid = uuid;
		this.campaign = campaign;
		this.form = form;
		this.formValues = (List<CampaignFormDataEntry>) formValues;
		this.area = area;
		this.rcode = rcode;
		this.region = region;
		this.pcode = pcode;
		this.district = district;
		this.dcode = dcode;
		this.community = community;
		this.clusternumber = clusternumber;
		this.ccode = ccode;
		this.formDate = formDate;
		this.formType = formType;
		this.source = source;
		this.creatingUser = creatingUser;

	}

	public CampaignFormDataIndexDto(String form, String uuid, Long rcode, String campaign, String creatingUser,
			String area, String region, String district, Long analysis_a, Long analysis_b) {
		this.form = form;
		this.uuid = uuid;
		this.rcode = rcode;
		this.campaign = campaign;
		this.creatingUser = creatingUser;
		this.area = area;
		this.region = region;
		this.district = district;
		this.analysis_a = analysis_a;
		this.analysis_b = analysis_b;
	}

	public CampaignFormDataIndexDto(String creatingUserType) {

		this.creatingUserType = creatingUserType;

	}

	// FLW Contructor
	public CampaignFormDataIndexDto(String area, String region, String district, Integer clusternumber, Long ccode,
			String source, String creatingUser, String personTitle, String error_status) {
		this.area = area;
		this.region = region;
		this.district = district;
		this.clusternumber = clusternumber;
		this.ccode = ccode;
		this.source = source; // taskiaNumber
		this.creatingUser = creatingUser;
		this.personTitle = personTitle;
		this.error_status = "Error: Duplicate Tazkira number";
	}

	public CampaignFormDataIndexDto(String area, String region, String district, String community, Integer clusternumer,
			// Long clusternumber_,
			Long ccode, Long analysis_a, Long analysis_b, Long analysis_c, Long analysis_d

	) {
		this.area = area;
		this.region = region;
		this.district = district;
		this.community = community;
		this.clusternumber = clusternumer;
		// this.clusternumber_ = clusternumber_;
		this.ccode = ccode;
		this.analysis_a = analysis_a;
		this.analysis_b = analysis_b;
		this.analysis_c = analysis_c;
		this.analysis_d = analysis_d;

	}

	public CampaignFormDataIndexDto(String area, String region, String district, String community, Integer clusternumer,
			// Long clusternumber_,
			Long ccode, Long analysis_a, Long analysis_b, Long analysis_c, Long analysis_d,
//			Long campaign_id,
			String error_status) {
		this.area = area;
		this.region = region;
		this.district = district;
		this.community = community;
		this.clusternumber = clusternumer;
		// this.clusternumber_ = clusternumber_;
		this.ccode = ccode;
		this.analysis_a = analysis_a;
		this.analysis_b = analysis_b;
		this.analysis_c = analysis_c;
		this.analysis_d = analysis_d;
//			this.campaign_id = campaign_id;
		this.error_status = error_status;
	}

	public CampaignFormDataIndexDto(String area, String region, String district, String community, Integer clusternumer,
			// Long clusternumber_,
			Long ccode, Integer analysis_a_, Integer analysis_b_, Integer analysis_c_, Integer analysis_d_) {
		this.area = area;
		this.region = region;
		this.district = district;
		this.community = community;
		this.clusternumber = clusternumer;
		// this.clusternumber_ = clusternumber_;
		this.ccode = ccode;
		this.analysis_a_ = analysis_a_;
		this.analysis_b_ = analysis_b_;
		this.analysis_c_ = analysis_c_;
		this.analysis_d_ = analysis_d_;
	}
	
	public CampaignFormDataIndexDto(String area, String region, String district, String community, Integer clusternumer,
			// Long clusternumber_,
			Long ccode, Integer analysis_a_, Integer analysis_b_, Integer analysis_c_, Integer analysis_d_, String form) {
		this.area = area;
		this.region = region;
		this.district = district;
		this.community = community;
		this.clusternumber = clusternumer;
		// this.clusternumber_ = clusternumber_;
		this.ccode = ccode;
		this.analysis_a_ = analysis_a_;
		this.analysis_b_ = analysis_b_;
		this.analysis_c_ = analysis_c_;
		this.analysis_d_ = analysis_d_;
		this.form = form;
	}
	
	public CampaignFormDataIndexDto(
			String area, String region, String district, String community, 
			Integer clusternumer,
			// Long clusternumber_,
			Long ccode, Integer analysis_a_, Integer analysis_b_, Integer analysis_c_, Integer analysis_d_, Integer analysis_e_, Integer analysis_f_, Integer analysis_g_, String form) {
		this.area = area;
		this.region = region;
		this.district = district;
		this.community = community;
		this.clusternumber = clusternumer;
		// this.clusternumber_ = clusternumber_;
		this.ccode = ccode;
		this.analysis_a_ = analysis_a_;
		this.analysis_b_ = analysis_b_;
		this.analysis_c_ = analysis_c_;
		this.analysis_d_ = analysis_d_;
		this.analysis_e_ = analysis_e_;
		this.analysis_f_ = analysis_f_;
		this.analysis_g_ = analysis_g_;
		this.form = form;
	}

	public CampaignFormDataIndexDto(String uuid, String campaign, String form, Object formValues, String area,
			String region, String district, String community, Date formDate) {
		this.uuid = uuid;
		this.campaign = campaign;
		this.form = form;
		this.formValues = (List<CampaignFormDataEntry>) formValues;
		this.area = area;
		this.region = region;
		this.district = district;
		this.community = community;
		this.formDate = formDate;
	}
	
	public CampaignFormDataIndexDto(String uuid, String campaign) {
		this.uuid = uuid;
		this.campaign = campaign;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	public List<CampaignFormDataEntry> getFormValues() {
		return formValues;
	}

	public void setFormValues(List<CampaignFormDataEntry> formValues) {
		this.formValues = formValues;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public Integer getClusternumber() {
		return clusternumber;
	}

	public void setClusternumber(Integer clusternumber) {
		this.clusternumber = clusternumber;
	}

	public Long getClusternumber_() {
		return clusternumber_;
	}

	public void setClusternumber_(Long clusternumber_) {
		this.clusternumber_ = clusternumber_;
	}

	public Date getFormDate() {
		return formDate;
	}

	public void setFormDate(Date formDate) {
		this.formDate = formDate;
	}

	public String getFormType() {
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}

	public Long getRcode() {
		return rcode;
	}

	public void setRcode(Long rcode) {
		this.rcode = rcode;
	}

	public int getPcode() {
		return pcode.intValue();
	}

	public void setPcode(Long pcode) {
		this.pcode = pcode;
	}

	public int getDcode() {
		return dcode.intValue();
	}

	public void setDcode(Long dcode) {
		this.dcode = dcode;
	}

	public Long getCcode() {
		return ccode;// Integer.parseInt(ccode+"");
	}

	public void setCcode(Long ccode) {
		this.ccode = ccode;
	}

	public Long getAnalysis_a() {
		return analysis_a;
	}

	public void setAnalysis_a(Long analysis_a) {
		this.analysis_a = analysis_a;
	}

	public Long getAnalysis_b() {
		return analysis_b;
	}

	public void setAnalysis_b(Long analysis_b) {
		this.analysis_b = analysis_b;
	}

	public Long getAnalysis_c() {
		return analysis_c;
	}

	public void setAnalysis_c(Long analysis_c) {
		this.analysis_c = analysis_c;
	}

	public Long getAnalysis_d() {
		return analysis_d;
	}

	public void setAnalysis_d(Long analysis_d) {
		this.analysis_d = analysis_d;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getCreatingUser() {
		return creatingUser;
	}

	public void setCreatingUser(String creatingUser) {
		this.creatingUser = creatingUser;
	}

	public Integer getAnalysis_a_() {
		return analysis_a_;
	}

	public void setAnalysis_a_(Integer analysis_a_) {
		this.analysis_a_ = analysis_a_;
	}

	public Integer getAnalysis_b_() {
		return analysis_b_;
	}

	public void setAnalysis_b_(Integer analysis_b_) {
		this.analysis_b_ = analysis_b_;
	}

	public Integer getAnalysis_c_() {
		return analysis_c_;
	}

	public void setAnalysis_c_(Integer analysis_c_) {
		this.analysis_c_ = analysis_c_;
	}

	public Integer getAnalysis_d_() {
		return analysis_d_;
	}

	public void setAnalysis_d_(Integer analysis_d_) {
		this.analysis_d_ = analysis_d_;
	}
	
	

	public Integer getAnalysis_e_() {
		return analysis_e_;
	}

	public void setAnalysis_e_(Integer analysis_e_) {
		this.analysis_e_ = analysis_e_;
	}

	public Integer getAnalysis_f_() {
		return analysis_f_;
	}

	public void setAnalysis_f_(Integer analysis_f_) {
		this.analysis_f_ = analysis_f_;
	}

	public Integer getAnalysis_g_() {
		return analysis_g_;
	}

	public void setAnalysis_g_(Integer analysis_g_) {
		this.analysis_g_ = analysis_g_;
	}

	public String getError_status() {
		return error_status;
	}

	public void setError_status(String error_status) {
		this.error_status = error_status;
	}

	public Long getCampaign_id() {
		return campaign_id;
	}

	public void setCampaign_id(Long campaign_id) {
		this.campaign_id = campaign_id;
	}

	public String getPersonTitle() {
		return personTitle;
	}

	public void setPersonTitle(String personTitle) {
		this.personTitle = personTitle;
	}

	public String getCreatingUserType() {
		return creatingUserType;
	}

	public void setCreatingUserType(String creatingUserType) {
		this.creatingUserType = creatingUserType;
	}

	public boolean isIsverified() {
		return isverified;
	}

	public void setIsverified(boolean isverified) {
		this.isverified = isverified;
	}

	public boolean isIspublished() {
		return ispublished;
	}

	public String getPublishedStringValue() {
		if (isIspublished()) {

			return "Published";

		} else {
			return "Unpublished";
		}
	}
	
	public String getVerifiedStringValue() {
		if (isIsverified()) {

			return "Verified";

		} else {
			return "Unverified";
		}
	}

	public void setIspublished(boolean ispublished) {
		this.ispublished = ispublished;
	}

	@Override
	public int hashCode() {
		return Objects.hash(analysis_a, analysis_a_, analysis_b, analysis_b_, analysis_c, analysis_c_, analysis_d,
				analysis_d_, area, campaign, campaign_id, ccode, clusternumber, clusternumber_, community, creatingUser,
				creatingUserType, dcode, district, error_status, form, formDate, formType, formValues, ispublished,
				isverified, pcode, personTitle, rcode, region, source, uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampaignFormDataIndexDto other = (CampaignFormDataIndexDto) obj;
		return Objects.equals(analysis_a, other.analysis_a) && Objects.equals(analysis_a_, other.analysis_a_)
				&& Objects.equals(analysis_b, other.analysis_b) && Objects.equals(analysis_b_, other.analysis_b_)
				&& Objects.equals(analysis_c, other.analysis_c) && Objects.equals(analysis_c_, other.analysis_c_)
				&& Objects.equals(analysis_d, other.analysis_d) && Objects.equals(analysis_d_, other.analysis_d_)
				&& Objects.equals(area, other.area) && Objects.equals(campaign, other.campaign)
				&& Objects.equals(campaign_id, other.campaign_id) && Objects.equals(ccode, other.ccode)
				&& Objects.equals(clusternumber, other.clusternumber)
				&& Objects.equals(clusternumber_, other.clusternumber_) && Objects.equals(community, other.community)
				&& Objects.equals(creatingUser, other.creatingUser)
				&& Objects.equals(creatingUserType, other.creatingUserType) && Objects.equals(dcode, other.dcode)
				&& Objects.equals(district, other.district) && Objects.equals(error_status, other.error_status)
				&& Objects.equals(form, other.form) && Objects.equals(formDate, other.formDate)
				&& Objects.equals(formType, other.formType) && Objects.equals(formValues, other.formValues)
				&& ispublished == other.ispublished && isverified == other.isverified
				&& Objects.equals(pcode, other.pcode) && Objects.equals(personTitle, other.personTitle)
				&& Objects.equals(rcode, other.rcode) && Objects.equals(region, other.region)
				&& Objects.equals(source, other.source) && Objects.equals(uuid, other.uuid);
	}

}
