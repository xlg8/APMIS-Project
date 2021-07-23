package de.symeda.sormas.backend.feature;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Collectors;

import org.junit.Test;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.backend.AbstractBeanTest;
import de.symeda.sormas.backend.TestDataCreator.RDCF;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;

public class FeatureConfigurationServiceTest extends AbstractBeanTest {

	@Test
	public void testCreateMissingFeatureConfigurations() {

		createConfigurations();
		FeatureConfigurationService featureConfigurationService = getBean(FeatureConfigurationService.class);
		featureConfigurationService.createMissingFeatureConfigurations();
		assertTrue(featureConfigurationService.getAll().stream().map(e -> e.getFeatureType()).collect(Collectors.toList()).containsAll(FeatureType.getAllServerFeatures()));
	}

	// FIXME Test fails: Do I get the feature wrong or is there a bug?
	@Test
	public void testUpdateFeatureConfigurations() {

		createConfigurations();
		FeatureConfigurationService cut = getBean(FeatureConfigurationService.class);

		FeatureConfiguration taskManagement = build(FeatureType.TASK_MANAGEMENT, false);
		FeatureConfiguration taskNotify = build(FeatureType.ASSIGN_TASKS_TO_HIGHER_LEVEL);

		assertFalse(cut.getByUuid(taskManagement.getUuid()).isEnabled());
		assertTrue(cut.getByUuid(taskNotify.getUuid()).isEnabled());

		assertFalse(getFeatureConfigurationFacade().isFeatureEnabled(FeatureType.TASK_MANAGEMENT));

		/*
		 * update relies on that all serverFeature configurations are already present,
		 * that's why the createMissing needs to be run before.
		 */
		cut.createMissingFeatureConfigurations();
		cut.updateFeatureConfigurations();
		assertFalse(getFeatureConfigurationFacade().isFeatureEnabled(FeatureType.TASK_MANAGEMENT));
		assertFalse(getFeatureConfigurationFacade().isFeatureEnabled(FeatureType.ASSIGN_TASKS_TO_HIGHER_LEVEL));
		assertFalse(cut.getByUuid(taskManagement.getUuid()).isEnabled());
		assertFalse(cut.getByUuid(taskNotify.getUuid()).isEnabled());
	}

	private void createConfigurations() {

		// Some serverFeatures
		build(FeatureType.EVENT_SURVEILLANCE);
		build(FeatureType.TASK_MANAGEMENT);

		// Some features configured on district level
		RDCF rdcf = creator.createRDCF();
		Region region = getRegionService().getByUuid(rdcf.region.getUuid());
		build(FeatureType.LINE_LISTING, null, region, getDistrictService().getByUuid(rdcf.district.getUuid()));
		build(FeatureType.LINE_LISTING, null, region, creator.createDistrict("d2", region));
	}

	private FeatureConfiguration build(FeatureType type) {

		return build(type, null, null, null);
	}

	private FeatureConfiguration build(FeatureType type, boolean enabled) {

		return build(type, null, null, null, enabled);
	}

	private FeatureConfiguration build(FeatureType type, Disease disease, Region region, District district) {

		return build(type, disease, region, district, type.isEnabledDefault());
	}

	private FeatureConfiguration build(FeatureType type, Disease disease, Region region, District district, boolean enabled) {

		FeatureConfigurationService featureConfigurationService = getBean(FeatureConfigurationService.class);

		FeatureConfiguration entity = new FeatureConfiguration();
		entity.setFeatureType(type);
		entity.setEnabled(enabled);
		entity.setDisease(disease);
		entity.setRegion(region);
		entity.setDistrict(district);
		featureConfigurationService.ensurePersisted(entity);

		return entity;
	}
}
