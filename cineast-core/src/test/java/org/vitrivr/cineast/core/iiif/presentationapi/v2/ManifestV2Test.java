package org.vitrivr.cineast.core.iiif.presentationapi.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class ManifestV2Test {

  private static final String JSON_RESPONSE = """
      {
        "@context": "http://iiif.io/api/presentation/2/context.json",
        "@id": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/manifest.json",
        "@type": "sc:Manifest",
        "label": "Cambridge, Corpus Christi College, MS 200: Baldwin of Ford OCist, De sacramento altaris.",
        "description": "The De sacramento altaris by Baldwin of Ford OCist, archbishop of Canterbury (d. 1190), contained in CCCC MS 200, was written in the period 1170-80. This copy was produced after he became archbishop in 1184 because an historiated initial at the beginning of the text shows him writing and wearing a mitre. The text discusses the biblical sources of the eucharist, foreshadowed in the paschal lamb and manna of the Old Testament, and defined in the New Testament writings of Saints Matthew, John and Paul. The book belonged to Christ Church cathedral priory, Canterbury.",
        "attribution": "Images courtesy of The Parker Library, Corpus Christi College, Cambridge. Licensed under a Creative Commons Attribution-NonCommercial 4.0 International License. For higher resolution images suitable for scholarly or commercial publication, either in print or in an electronic format, please contact the Parker Library directly at parker-library@corpus.cam.ac.uk",
        "logo": {
          "@id": "https://stacks.stanford.edu/image/iiif/wy534zh7137%2FSULAIR_rosette/full/400,/0/default.jpg",
          "service": {
            "@context": "http://iiif.io/api/image/2/context.json",
            "@id": "https://stacks.stanford.edu/image/iiif/wy534zh7137%2FSULAIR_rosette",
            "profile": "http://iiif.io/api/image/2/level1.json"
          }
        },
        "seeAlso": {
          "@id": "https://purl.stanford.edu/bg021sq9590.mods",
          "format": "application/mods+xml"
        },
        "viewingHint": "paged",
        "metadata": [
          {
            "label": "Title",
            "value": "Baldwin of Ford OCist, De sacramento altaris"
          },
          {
            "label": "Title",
            "value": "Baldewinus de sacramento altaris"
          },
          {
            "label": "Contributor",
            "value": "Christ Church, Canterbury (originator)"
          },
          {
            "label": "Type",
            "value": "Text"
          },
          {
            "label": "Language",
            "value": "Latin."
          },
          {
            "label": "Format",
            "value": "electronic"
          },
          {
            "label": "Format",
            "value": "image/tif"
          },
          {
            "label": "Subject",
            "value": "Manuscripts"
          },
          {
            "label": "Coverage",
            "value": "Great Britain"
          },
          {
            "label": "Subject",
            "value": "Manuscripts--Great Britain"
          },
          {
            "label": "Identifier",
            "value": "http://parkerweb.stanford.edu/"
          },
          {
            "label": "Identifier",
            "value": "CCC200"
          },
          {
            "label": "Identifier",
            "value": "Stanley_V. 3"
          },
          {
            "label": "Identifier",
            "value": "TJames_302"
          },
          {
            "label": "Relation",
            "value": "Parker Manuscripts"
          },
          {
            "label": "PublishDate",
            "value": "2017-03-28T17:00:21Z"
          }
        ],
        "thumbnail": {
          "@id": "https://stacks.stanford.edu/image/iiif/bg021sq9590%2F200_1_TC_46/full/!400,400/0/default.jpg",
          "@type": "dctypes:Image",
          "format": "image/jpeg",
          "service": {
            "@context": "http://iiif.io/api/image/2/context.json",
            "@id": "https://stacks.stanford.edu/image/iiif/bg021sq9590%2F200_1_TC_46",
            "profile": "http://iiif.io/api/image/2/level1.json"
          }
        },
        "sequences": [
          {
            "@id": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/normal",
            "@type": "sc:Sequence",
            "label": "Current page order",
            "canvases": [
              {
                "@id": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/canvas/canvas-1",
                "@type": "sc:Canvas",
                "label": "f. ar",
                "height": 9198,
                "width": 6226,
                "images": [
                  {
                    "@id": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/imageanno/anno-1",
                    "@type": "oa:Annotation",
                    "motivation": "sc:painting",
                    "resource": {
                      "@id": "https://stacks.stanford.edu/image/iiif/bg021sq9590%2F200_a_R_TC_46/full/full/0/default.jpg",
                      "@type": "dctypes:Image",
                      "format": "image/jpeg",
                      "height": 9198,
                      "width": 6226,
                      "service": {
                        "@id": "https://stacks.stanford.edu/image/iiif/bg021sq9590%2F200_a_R_TC_46",
                        "@context": "http://iiif.io/api/image/2/context.json",
                        "profile": "http://iiif.io/api/image/2/level1.json"
                      }
                    },
                    "on": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/canvas/canvas-1"
                  }
                ]
              },
              {
                "@id": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/canvas/canvas-222",
                "@type": "sc:Canvas",
                "label": "f. ev",
                "height": 9150,
                "width": 6262,
                "images": [
                  {
                    "@id": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/imageanno/anno-222",
                    "@type": "oa:Annotation",
                    "motivation": "sc:painting",
                    "resource": {
                      "@id": "https://stacks.stanford.edu/image/iiif/bg021sq9590%2F200_e_V_TC_46/full/full/0/default.jpg",
                      "@type": "dctypes:Image",
                      "format": "image/jpeg",
                      "height": 9150,
                      "width": 6262,
                      "service": {
                        "@id": "https://stacks.stanford.edu/image/iiif/bg021sq9590%2F200_e_V_TC_46",
                        "@context": "http://iiif.io/api/image/2/context.json",
                        "profile": "http://iiif.io/api/image/2/level1.json"
                      }
                    },
                    "on": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/canvas/canvas-222"
                  }
                ]
              }
            ]
          }
        ],
        "structures": [
          {
            "@type": "sc:Range",
            "label": "Table of Contents",
            "viewingHint": "top",
            "@id": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/range/r0",
            "ranges": [
              "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/range/r1"
            ]
          },
          {
            "@id": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/range/r1",
            "@type": "sc:Range",
            "label": "Baldwin of Ford OCist, De sacramento altaris",
            "within": "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/range/r0",
            "canvases": [
              "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/canvas/canvas-9",
              "https://dms-data.stanford.edu/data/manifests/Parker/bg021sq9590/canvas/canvas-213"
            ]
          }
        ]
      }
      """;

  @Test
  public void parsingTest() throws JsonProcessingException {
    Manifest_v2 manifest = new ObjectMapper().readValue(JSON_RESPONSE, Manifest_v2.class);
    assertNotNull(manifest);
    assertEquals(2, manifest.getImageUrls().size());
  }

}
