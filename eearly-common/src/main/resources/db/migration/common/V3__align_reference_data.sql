-- Aligns reference data with the values used by the reference deployment.
-- Every statement is safe to run on databases that already contain the target values.

-- 1. Body height is measured in centimetres (range 50-300) instead of metres (range 0-3).
UPDATE measurement_type
SET unit = 'cm', min_value = 50, max_value = 300
WHERE id = '27d77908-3302-4012-a371-1941fe019d29'
  AND (unit IS DISTINCT FROM 'cm' OR min_value IS DISTINCT FROM 50 OR max_value IS DISTINCT FROM 300);

-- 2. The EEARLY observation archetypes carry the "eearly_" prefix and are on version v0.
--    The glucose variability row also had a stray trailing quote.
UPDATE measurement_type
SET ehr_observation_id = 'openEHR-EHR-OBSERVATION.eearly_heart_rate_variability.v0'
WHERE id = '4a992791-93cb-4367-af53-00613632bc54'
  AND ehr_observation_id IS DISTINCT FROM 'openEHR-EHR-OBSERVATION.eearly_heart_rate_variability.v0';

UPDATE measurement_type
SET ehr_observation_id = 'openEHR-EHR-OBSERVATION.eearly_stress_level.v0'
WHERE id = 'd2f0d81a-57b0-4594-a6d4-e7704a705e0b'
  AND ehr_observation_id IS DISTINCT FROM 'openEHR-EHR-OBSERVATION.eearly_stress_level.v0';

UPDATE measurement_type
SET ehr_observation_id = 'openEHR-EHR-OBSERVATION.eearly_glucose_variability.v0'
WHERE id = '997417d6-f394-40a6-b919-4626ee04f985'
  AND ehr_observation_id IS DISTINCT FROM 'openEHR-EHR-OBSERVATION.eearly_glucose_variability.v0';

-- 3. Newer Renpho ES-26M units advertise as "Renpho-Scale" in addition to "QN-Scale".
UPDATE device
SET bluetooth_device_names = array_append(COALESCE(bluetooth_device_names, '{}'), 'Renpho-Scale')
WHERE id = '852e2c00-b08f-4747-8239-ddd21c0c131a'
  AND NOT ('Renpho-Scale' = ANY (COALESCE(bluetooth_device_names, '{}')));
