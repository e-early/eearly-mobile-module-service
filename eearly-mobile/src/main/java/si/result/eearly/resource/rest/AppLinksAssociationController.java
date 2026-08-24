package si.result.eearly.resource.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/.well-known")
public class AppLinksAssociationController {

  @GetMapping(value = "/apple-app-site-association", produces = "application/pkcs7-mime")
  public ResponseEntity<String> getAppleAppSiteAssociation() {

    return ResponseEntity.ok("""
        {
          "applinks": {
            "apps": [],
            "details": [
              {
                "appIDs": [
                 "42M99WHQ4D.si.result.eearly"
                ],
                "paths": [
                    "*"
                ],
                "components": [
                    {
                        "/": "/*"
                    }
                ]
              }
            ]
          },
          "webcredentials": {
            "apps": [
                "42M99WHQ4D.si.result.eearly"
            ]
          }
        }
        """);
  }

  @GetMapping(value = "/assetlinks.json", produces = "application/json")
  public ResponseEntity<String> getAndroidAssetLinks() {
    return ResponseEntity.ok("""
        [
          {
            "relation": [
              "delegate_permission/common.handle_all_urls"
            ],
            "target": {
              "namespace": "android_app",
              "package_name": "si.result.eearly_mobile_app",
              "sha256_cert_fingerprints": [
                "2C:B8:E5:D7:68:A0:AA:C9:8C:5A:C9:11:6F:C8:CF:B6:18:B2:DB:AA:5C:F9:81:AE:B9:C4:88:94:AF:05:0D:32"
              ]
            }
          },
          {
            "relation": [
              "delegate_permission/common.handle_all_urls"
            ],
            "target": {
              "namespace": "android_app",
              "package_name": "si.result.eearly_mobile_app.debug",
              "sha256_cert_fingerprints": [
                "2C:B8:E5:D7:68:A0:AA:C9:8C:5A:C9:11:6F:C8:CF:B6:18:B2:DB:AA:5C:F9:81:AE:B9:C4:88:94:AF:05:0D:32",
                "BC:81:F0:5C:62:F0:6B:5A:E8:12:71:EF:9E:D6:24:E9:BB:81:03:84:A4:38:FD:FC:C9:D5:2C:5D:F4:8E:12:7A"
              ]
            }
          }
        ]
        """);
  }
}
