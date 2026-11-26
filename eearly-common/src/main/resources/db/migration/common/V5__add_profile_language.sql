-- Language of the patient's app and push notifications, as an ISO 639-1 code. Owned by the admin
-- service (synced via UpsertPatientDemographics); the patient can change it in the app
-- (UpdateLanguage), which is sent on to the admin service. Defaults to Slovenian ('sl').
ALTER TABLE profile ADD COLUMN language VARCHAR(3) NOT NULL DEFAULT 'sl';
