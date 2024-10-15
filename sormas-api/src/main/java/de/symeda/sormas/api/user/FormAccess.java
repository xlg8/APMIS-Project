package de.symeda.sormas.api.user;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public enum FormAccess {

//	ICM,
//	PCA,
//	ARCHIVE,
//	FLW,
//	ADMIN,
//	FMS,
//	LQAS,
//	TRAINING,
//	EAG,
//	EAG_ADMIN,
//	EAG_ICM,
//	EAG_PCA,
//	EAG_FMS,
//	EAG_LQAS;
	
	ICM("ICM"),
    PCA("PCA"),
    ARCHIVE("ARCHIVE"),
    FLW("FLW"),
    ADMIN("ADMIN"),
    FMS("FMS"),
    LQAS("LQAS"),
    TRAINING("TRAINING"),
    EAG("EAG"),
    EAG_ADMIN("EAG-ADMIN"),
    EAG_ICM("EAG-ICM"),
    EAG_PCA("EAG-PCA"),
    EAG_FMS("EAG-FMS"),
    EAG_LQAS("EAG-LQAS"),
    MODALITY_PRE("MODALITY_PRE"),
    MODALITY_POST("MODALITY_POST"),
	VALIDATION("VALIDATION");

    private String displayName;

    FormAccess(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

	public void addAssignableForms(Collection<FormAccess> collection) {

		for (FormAccess form : FormAccess.values()) {
			collection.add(form);
		}
	}

	public static Set<FormAccess> getAssignableForms() {
		Set<FormAccess> result = EnumSet.allOf(FormAccess.class);

		return result;
	}

}
