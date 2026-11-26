-- Reference data for the body composition measurement types (BODY_IMPEDANCE ... BODY_WATER_MASS).
--
-- Every value of MeasurementTypeEnum except UNKNOWN_MEASUREMENT_TYPE needs a measurement_type row,
-- otherwise the type cannot be stored, scheduled or returned by GetMeasurementTypes.
--
-- The ids are fixed so that all installations share the same identifiers for the same type.
-- The migration is idempotent: a row is skipped when its id exists or when the same
-- (measurement_type, measurement_sub_type) pair is already present.
--
-- The new types are intentionally not linked to any device (device_measurement_type): clients that
-- do not know a type must not see it in device responses.

INSERT INTO measurement_type (id, measurement_type, measurement_sub_type, min_value, max_value, unit, ehr_observation_id)
SELECT v.id, v.measurement_type, v.measurement_sub_type, v.min_value, v.max_value, v.unit, v.ehr_observation_id
FROM (VALUES
    ('d54dbf39-7158-4031-adfe-0f23ee093d9b'::uuid, 'BODY_IMPEDANCE',                 'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 5000::double precision,  'Ohm',    'openEHR-EHR-OBSERVATION.eearly_body_composition.v0'),
    ('8bb65a42-4bf4-466e-a512-b209dd768b3a'::uuid, 'BODY_MASS_INDEX',                'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 1000::double precision,  'kg/m2',  'openEHR-EHR-OBSERVATION.body_mass_index.v2'),
    ('e0eaaed2-c725-4105-8999-8ea2bddebdac'::uuid, 'FAT_FREE_MASS',                  'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 1000::double precision,  'kg',     'openEHR-EHR-OBSERVATION.eearly_body_composition.v0'),
    ('5566a04f-c95b-4204-ad41-13d95b9f4398'::uuid, 'FAT_MASS',                       'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 1000::double precision,  'kg',     'openEHR-EHR-OBSERVATION.eearly_body_composition.v0'),
    ('96538a2b-52cd-424d-ab04-dc2ab29caf83'::uuid, 'BODY_FAT_PERCENTAGE',            'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 100::double precision,   '%',      'openEHR-EHR-OBSERVATION.eearly_body_composition.v0'),
    ('7dbc5297-75da-48dc-afda-508dd583e5ba'::uuid, 'BODY_WATER_PERCENTAGE',          'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 100::double precision,   '%',      'openEHR-EHR-OBSERVATION.eearly_body_composition.v0'),
    ('c9287255-9d85-4bb0-9e27-11f9dfc7b76d'::uuid, 'SKELETAL_MUSCLE_MASS',           'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 1000::double precision,  'kg',     'openEHR-EHR-OBSERVATION.eearly_body_composition.v0'),
    ('062d839c-64e7-4f09-ac35-1ad1143d4627'::uuid, 'BASAL_METABOLIC_RATE',           'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 20000::double precision, 'kcal/d', 'openEHR-EHR-OBSERVATION.eearly_body_composition.v0'),
    ('827664ef-d46e-4ee9-bfed-8fbc5c01dd7d'::uuid, 'FAT_FREE_MASS_PERCENTAGE',       'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 100::double precision,   '%',      'openEHR-EHR-OBSERVATION.eearly_body_composition.v0'),
    ('239c6bf6-8f1d-4e21-afd0-3fd35822d35b'::uuid, 'SKELETAL_MUSCLE_MASS_PERCENTAGE', 'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 100::double precision,   '%',      'openEHR-EHR-OBSERVATION.eearly_body_composition.v0'),
    ('bd9b1917-3dd4-4f3a-8a03-28705b980627'::uuid, 'BODY_WATER_MASS',                'UNKNOWN_MEASUREMENT_SUB_TYPE', 0::double precision, 1000::double precision,  'kg',     'openEHR-EHR-OBSERVATION.eearly_body_composition.v0')
) AS v (id, measurement_type, measurement_sub_type, min_value, max_value, unit, ehr_observation_id)
WHERE NOT EXISTS (
    SELECT 1
    FROM measurement_type t
    WHERE t.id = v.id
       OR (t.measurement_type = v.measurement_type AND t.measurement_sub_type = v.measurement_sub_type)
);
