package org.vitrivr.cineast.core.iiif.presentationapi.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ManifestV3Test {

  private static final String JSON_RESPONSE = """
      {
        "@context": "http://iiif.io/api/presentation/3/context.json",
        "id": "https://ub-iiifpresentation.ub.unibas.ch/portraets/9948535910105504/manifest/",
        "type": "Manifest",
        "label": {
          "de": [
            "1 Reproduktion eines Bildes auf Karton aufgezogen. schwarz/weiss -- [Portr\\u00e4t von Johann Rudolf Schnell]"
          ]
        },
        "items": [
          {
            "id": "https://ub-iiifpresentation.ub.unibas.ch/id/canvas/{'width': 3779, 'url': 'IBB_1_004853591.jpx', 'height': 2929}",
            "type": "Canvas",
            "label": {
              "de": [
                null
              ]
            },
            "metadata": [
              {
                "label": {
                  "de": [
                    "Signatur"
                  ],
                  "en": [
                    "Signatur"
                  ]
                },
                "value": {
                  "de": [
                    "UBH Portr BS Schnell JR 1767, 1e. UBH",
                    "UBH Portr BS Schnell JR 1767, 1a. UBH",
                    "UBH Portr BS Schnell JR 1767, 1c. UBH",
                    "UBH Portr BS Schnell JR 1767, 1b. UBH",
                    "UBH Portr BS Schnell JR 1767, 1d. UBH"
                  ]
                }
              },
              {
                "label": {
                  "de": [
                    "Dargestellte Person/en"
                  ],
                  "en": [
                    "Dargestellte Person/en"
                  ]
                },
                "value": {
                  "de": [
                    "Schnell, Johann Rudolf (1767-1829)",
                    "Schnell, Johann Rudolf (1767-1829)"
                  ]
                }
              },
              {
                "label": {
                  "de": [
                    "Datierung"
                  ],
                  "en": [
                    "Datierung"
                  ]
                },
                "value": {
                  "de": [
                    "[17--?]"
                  ]
                }
              },
              {
                "label": {
                  "de": [
                    "Technik"
                  ],
                  "en": [
                    "Technik"
                  ]
                },
                "value": {
                  "de": [
                    "1 Reproduktion eines Bildes auf Karton aufgezogen. schwarz/weiss"
                  ]
                }
              },
              {
                "label": {
                  "de": [
                    "Masse"
                  ],
                  "en": [
                    "Masse"
                  ]
                },
                "value": {
                  "de": [
                    "oval 6,8 x 5,4 cm, Blatt 11 x 8,1 cm, Karton 11,3 x 8,4 cm"
                  ]
                }
              },
              {
                "label": {
                  "de": [
                    "Allgemeine Anmerkung"
                  ],
                  "en": [
                    "Allgemeine Anmerkung"
                  ]
                },
                "value": {
                  "de": [
                    "Lebensdaten: geb. 7. Oktober 1767, gest. 21. M\\u00e4rz 1829",
                    "Professor der Geschichte und Rechte an der Universit\\u00e4t Basel"
                  ]
                }
              },
              {
                "label": {
                  "de": [
                    "swisscovery Link"
                  ],
                  "en": [
                    "swisscovery Link"
                  ]
                },
                "value": {
                  "de": [
                    "<a href=\\"https://basel.swisscovery.org/discovery/fulldisplay?docid=alma9948535910105504&context=L&vid=41SLSP_UBS:live\\">swisscovery</a>"
                  ]
                }
              },
              {
                "label": {
                  "de": [
                    "Swisscollections"
                  ]
                },
                "value": {
                  "de": [
                    "<a href=\\"https://swisscollections.ch/Record/9948535910105504\\">vollst\\u00e4ndige Metadaten</a>"
                  ]
                }
              }
            ],
            "width": 3779,
            "height": 2929,
            "thumbnail": [
              {
                "id": "https://ub-sipi.ub.unibas.ch/portraets/IBB_1_004853591.jpx/full/200,/0/default.jpg",
                "type": "Image",
                "service": [
                  {
                    "type": "ImageService2",
                    "profile": "http://iiif.io/api/image/2/level2.json",
                    "id": "https://ub-sipi.ub.unibas.ch/portraets/IBB_1_004853591.jpx"
                  }
                ]
              }
            ],
            "items": [
              {
                "id": "https://ub-iiifpresentation.ub.unibas.ch/missing_id_definition",
                "type": "AnnotationPage",
                "items": [
                  {
                    "id": "https://ub-iiifpresentation.ub.unibas.ch//id/body/{'width': 3779, 'url': 'IBB_1_004853591.jpx', 'height': 2929}",
                    "type": "Annotation",
                    "motivation": "painting",
                    "target": "https://ub-iiifpresentation.ub.unibas.ch/id/canvas/{'width': 3779, 'url': 'IBB_1_004853591.jpx', 'height': 2929}",
                    "body": {
                      "id": "https://ub-sipi.ub.unibas.ch/portraets/IBB_1_004853591.jpx/full/200,/0/default.jpg",
                      "type": "Image",
                      "format": "image/jpeg",
                      "service": [
                        {
                          "type": "ImageService2",
                          "profile": "http://iiif.io/api/image/2/level2.json",
                          "id": "https://ub-sipi.ub.unibas.ch/portraets/IBB_1_004853591.jpx"
                        }
                      ]
                    }
                  }
                ]
              }
            ]
          }
        ],
        "metadata": [],
        "requiredStatement": {
          "label": {
            "de": [
              "Attribution"
            ]
          },
          "value": {
            "de": [
              "Portr\\u00e4tsammlung Universit\\u00e4tsbibliothek Basel"
            ]
          }
        },
        "rights": "https://creativecommons.org/publicdomain/mark/1.0/deed",
        "behavior": [
          "unordered"
        ],
        "logo": [
          {
            "id": "https://www.unibas.ch/dam/jcr:93abfa0e-58d6-45ed-930a-de414ca40b13/uni-basel-logo.svg",
            "type": "Image",
            "format": "image/png",
            "service": [
              {
                "type": "ImageService2",
                "profile": "level2",
                "id": "https://www.unibas.ch/dam/jcr:93abfa0e-58d6-45ed-930a-de414ca40b13/uni-basel-logo.svg"
              }
            ]
          }
        ],
        "partOf": [
          null
        ],
        "structures": [
          {
            "id": "https://ub-iiifpresentation.ub.unibas.ch//id/range/9948535910105504",
            "type": "Range",
            "label": {
              "de": [
                "9948535910105504"
              ]
            },
            "items": [
              {
                "id": "https://ub-iiifpresentation.ub.unibas.ch/id/canvas/{'width': 3779, 'url': 'IBB_1_004853591.jpx', 'height': 2929}",
                "type": "Canvas"
              }
            ]
          }
        ]
      }
      """;

  @Test
  public void parsingTest() throws JsonProcessingException {
    Manifest_v3 manifest = new ObjectMapper().readValue(JSON_RESPONSE, Manifest_v3.class);
    assertNotNull(manifest);
    assertEquals(1, manifest.getImageUrls().size());
  }

}