package de.symeda.sormas.api.campaign.form;

import static de.symeda.sormas.api.utils.FieldConstraints.CHARACTER_LIMIT_BIG;
import static de.symeda.sormas.api.utils.FieldConstraints.CHARACTER_LIMIT_DEFAULT;
import static de.symeda.sormas.api.utils.FieldConstraints.CHARACTER_LIMIT_SMALL;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.symeda.sormas.api.MapperUtil;
import de.symeda.sormas.api.i18n.Validations;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CampaignFormElement implements Serializable {

	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String CAPTION = "caption";
	public static final String EXPRESSION = "expression";
	public static final String MAX = "max";
	public static final String MIN = "min";
	public static final String ERRORMESSAGE = "errormessage";
	public static final String COMMENT = "comment";
	public static final String DEFAULTVALUE = "defaultvalue";

	private static final long serialVersionUID = 5553496750859734167L;

	public static final String[] VALID_TYPES = { CampaignFormElementType.LABEL.toString(),
			CampaignFormElementType.SECTION.toString(), CampaignFormElementType.NUMBER.toString(),
			CampaignFormElementType.TEXT.toString(), CampaignFormElementType.YES_NO.toString(),
			CampaignFormElementType.RADIO.toString(), CampaignFormElementType.DROPDOWN.toString(),
			CampaignFormElementType.TEXTBOX.toString(), CampaignFormElementType.CHECKBOX.toString(),
			CampaignFormElementType.RADIOBASIC.toString(), CampaignFormElementType.DECIMAL.toString(),
			CampaignFormElementType.DATE.toString(), CampaignFormElementType.CHECKBOXBASIC.toString(),
			CampaignFormElementType.RANGE.toString(), CampaignFormElementType.ARRAY.toString() };

	public static final String[] VALID_STYLES = { CampaignFormElementStyle.INLINE.toString(),
			CampaignFormElementStyle.ROW.toString(), CampaignFormElementStyle.FIRST.toString(),
			CampaignFormElementStyle.COL_1.toString(), CampaignFormElementStyle.COL_2.toString(),
			CampaignFormElementStyle.COL_3.toString(), CampaignFormElementStyle.COL_4.toString(),
			CampaignFormElementStyle.COL_5.toString(), CampaignFormElementStyle.COL_6.toString(),
			CampaignFormElementStyle.COL_7.toString(), CampaignFormElementStyle.COL_8.toString(),
			CampaignFormElementStyle.COL_9.toString(), CampaignFormElementStyle.COL_10.toString(),
			CampaignFormElementStyle.COL_11.toString(), CampaignFormElementStyle.COL_12.toString() };

	public static final String[] ALLOWED_HTML_TAGS = { "br", "p", "b", "i", "u", "h1", "h2", "h3", "h4", "h5", "h6" };

	@Size(max = CHARACTER_LIMIT_SMALL, message = Validations.textTooLong)
	private String type;
	@Size(max = CHARACTER_LIMIT_SMALL, message = Validations.textTooLong)
	private String id;
	@Size(max = CHARACTER_LIMIT_DEFAULT, message = Validations.textTooLong)
	private String caption;
	@Size(max = CHARACTER_LIMIT_BIG, message = Validations.textTooLong)
	private String expression;
	private String max;
	private String min;
	private String[] styles;
	private List<MapperUtil> options;
	private String[] constraints;
	@Size(max = CHARACTER_LIMIT_SMALL, message = Validations.textTooLong)
	private String dependingOn;
	private String[] dependingOnValues;
	private boolean important;
	private boolean warnonerror;
	private boolean ignoredisable;
	private String errormessage;
	private String comment;
	private String defaultvalue;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String[] getStyles() {
		return styles;
	}

	public void setStyles(String[] styles) {
		this.styles = styles;
	}


	public List<MapperUtil> getOptions() {
		return options;
	}

	public void setOptions(List<MapperUtil> options) {
		this.options = options;
	}

	public String[] getConstraints() {
		return constraints;
	}

	public void setConstraints(String[] constraints) {
		this.constraints = constraints;
	}

	public String getDependingOn() {
		return dependingOn;
	}

	public void setDependingOn(String dependingOn) {
		this.dependingOn = dependingOn;
	}

	public String[] getDependingOnValues() {
		return dependingOnValues;
	}

	public void setDependingOnValues(String[] dependingOnValues) {
		this.dependingOnValues = dependingOnValues;
	}

	public boolean isImportant() {
		return important;
	}

	public void setImportant(boolean important) {
		this.important = important;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}
	
	

	public boolean isWarnonerror() {
		return warnonerror;
	}

	public void setWarnonerror(boolean warnonerror) {
		this.warnonerror = warnonerror;
	}
	
	
	public String getErrormessage() {
		return errormessage;
	}

	public void setErrormessage(String errormessage) {
		this.errormessage = errormessage;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isIgnoredisable() {
		return ignoredisable;
	}

	public void setIgnoredisable(boolean ignoredisable) {
		this.ignoredisable = ignoredisable;
	}
	

	public String getDefaultvalue() {
		return defaultvalue;
	}

	public void setDefaultvalue(String defaultvalue) {
		this.defaultvalue = defaultvalue;
	}

	/**
	 * Needed. Otherwise hibernate will persist whenever loading, because hibernate
	 * types creates new instances that aren't equal.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CampaignFormElement that = (CampaignFormElement) o;
		return important == that.important && ignoredisable == that.ignoredisable && Objects.equals(type, that.type) && Objects.equals(id, that.id)
				&& Objects.equals(caption, that.caption) && Objects.equals(expression, that.expression)
				&& Objects.equals(max, that.max) && Objects.equals(min, that.min)
				&& Arrays.equals(styles, that.styles)
				//&& Arrays.equals(options, that.options)
				&& Arrays.equals(constraints, that.constraints)
				&& Objects.equals(dependingOn, that.dependingOn)
				&& Arrays.equals(dependingOnValues, that.dependingOnValues)
				&& Objects.equals(warnonerror, that.warnonerror)
				&& Objects.equals(errormessage, that.errormessage)
				&& Objects.equals(comment, that.comment)
				&& Objects.equals(defaultvalue, that.defaultvalue);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(type, id, caption, expression, dependingOn, important, warnonerror, errormessage, comment, defaultvalue, ignoredisable);
		result = 31 * result + Arrays.hashCode(styles);
	//	result = 31 * result + Arrays.hashCode(options);
		result = 31 * result + Arrays.hashCode(constraints);
		result = 31 * result + Arrays.hashCode(dependingOnValues);
		return result;
	}
}
