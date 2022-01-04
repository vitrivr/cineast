package org.vitrivr.cineast.core.features.neuralnet.tf.models.deeplab;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.Collection;

public enum DeepLabLabel {

  Airplane(91, 7, -1, 508.8256432, -366.6095754),
  Animal(127, -1, -1, -381.7475892, -485.0995063),
  Apparel(93, -1, -1, -589.4855693, 358.6491053),
  Armchair(31, -1, -1, -466.3525102, 50.44752876),
  Ashcan(139, -1, -1, -346.0380358, 121.2383401),
  Awning(87, -1, -1, 50.1221531, -54.92999489),
  Bag(116, -1, -1, -357.006526, 300.7767016),
  Ball(120, -1, -1, -248.0286454, -201.4606493),
  Bannister(96, -1, -1, 251.2022496, -206.4706121),
  Bar(78, -1, -1, -254.441281, 392.0840915),
  Barrel(112, -1, -1, 383.3729222, 218.3984344),
  Base(41, -1, -1, -152.6107411, -221.9486038),
  Basket(113, -1, -1, -266.3981494, -184.0578917),
  Bathtub(38, -1, -1, -600.4143238, 130.5405869),
  Bed(8, -1, -1, -552.9418591, 108.6576251),
  Bench(70, -1, -1, -229.3820961, -270.5891866),
  Bicycle(128, 3, 12, 746.2792231, -299.8031184),
  Bird(-1, 14, -1, -416.8007105, -482.9562759),
  Blanket(132, -1, -1, -492.5192255, 154.0473616),
  Blind(64, -1, -1, 231.3189691, 278.2519254),
  Boat(77, -1, -1, 450.1470605, -328.8306746),
  Book(68, -1, -1, -281.5581572, 134.2972347),
  Bookcase(63, -1, -1, -325.2167563, 85.79959215),
  Booth(89, -1, -1, -252.3446974, 34.70832391),
  Bottle(99, 9, -1, 348.2497174, 200.0773774),
  Box(42, -1, -1, -382.485425, 315.7411277),
  Bridge(62, -1, 19, 384.4628385, -337.3772087),
  Buffet(100, -1, -1, -172.4118935, 228.7279754),
  Building(2, -1, 15, 101.417095, 568.7915318),
  Bus(81, 4, 9, 656.2353098, -302.7983221),
  Cabinet(11, -1, -1, -346.7653906, 54.81182029),
  Canopy(107, -1, -1, 73.2295209, -57.5463292),
  Car(21, 2, 7, 607.2434723, -281.2458038),
  Caravan(-1, -1, 13, 669.5000144, -235.3182096),
  Case(56, -1, -1, -326.1182628, -379.5798435),
  Cat(-1, 15, -1, -392.8645846, -452.4178867),
  Ceiling(6, 8, -1, 273.8403189, -96.35963986),
  Chair(20, -1, -1, -541.4100255, -26.27889514),
  Chandelier(86, -1, -1, 110.5820816, 170.3628298),
  Clock(149, -1, -1, 170.8266854, 546.5721549),
  Column(43, -1, -1, -273.4485455, 155.6816422),
  Computer(75, -1, -1, 188.8004946, 353.1592262),
  Counter(46, -1, -1, -306.5801345, 372.8955258),
  Countertop(71, -1, -1, -164.8094601, 336.8257422),
  Cow(-1, 16, -1, -363.9251371, -547.5060309),
  Cradle(118, -1, -1, -420.2261206, 155.0430906),
  Curtain(19, -1, -1, 131.0961824, -81.15440637),
  Cushion(40, -1, -1, -292.4862833, -164.3949626),
  dence(-1, -1, 17, 357.6441879, 376.295578),
  Desk(34, -1, -1, -287.2049761, 60.99762674),
  Dishwasher(130, -1, -1, -149.642726, 389.5671712),
  Dog(-1, 17, -1, -369.3873623, -453.9125812),
  Door(15, -1, -1, 6.14754699, 468.9503323),
  Earth(14, -1, -1, -118.4877608, -276.0911275),
  Escalator(97, -1, -1, 315.785901, -222.6654559),
  Fan(140, -1, -1, -228.2072191, -364.6224659),
  Fence(33, -1, -1, 396.3844386, -101.4598203),
  Field(30, -1, -1, -194.3062567, -274.8337146),
  Fireplace(50, -1, -1, -90.16722092, 304.6081204),
  Flag(150, -1, -1, -67.23271164, -22.95821314),
  Floor(4, -1, -1, 275.3603826, -135.4064348),
  Flower(67, -1, -1, 55.1929061, 131.8605885),
  Food(121, -1, -1, -159.4255884, 204.3837889),
  Fountain(105, -1, -1, 173.9920751, -369.4556666),
  Glass(148, -1, -1, 328.8438262, 185.662688),
  Grandstand(52, -1, -1, -198.3403643, -319.0947709),
  Grass(10, -1, -1, -46.17805441, -387.3895832),
  Ground(-1, -1, 28, -156.3095411, -263.4750108),
  Guardrail(-1, -1, 18, 438.3040546, -112.2850639),
  Hill(69, -1, -1, 155.8205922, -487.7898429),
  Hood(134, 18, -1, -154.9107304, 572.5769587),
  House(26, -1, -1, -2.327432508, 499.1735475),
  Hovel(80, -1, -1, -19.52629222, 528.4578024),
  Kitchen(74, -1, -1, -133.5934896, 349.7919671),
  Lake(129, -1, -1, 89.08866876, -347.2754503),
  Lamp(37, -1, -1, 159.5760649, 148.9227865),
  Land(95, -1, -1, 0.260955803, -423.998651),
  Light(83, -1, -1, 150.7027179, 109.4150073),
  Microwave(125, -1, -1, -49.89082543, 369.5952122),
  Minibike(117, -1, -1, 766.4205622, -277.1858826),
  Mirror(28, -1, -1, 170.4369215, 238.0533941),
  Monitor(144, -1, -1, 152.428595, 332.2791152),
  Motorbike(-1, 5, 11, 729.5502424, -279.3369109),
  Mountain(17, -1, -1, 134.4997037, -479.3810542),
  Ottoman(98, -1, -1, -519.2696818, 62.40281492),
  Oven(119, -1, -1, -80.06955937, 364.6709658),
  Painting(23, -1, -1, -54.61552, 59.71375806),
  Palm(73, -1, -1, -198.5038112, 308.9080375),
  Parking(-1, -1, 3, 271.8528289, 12.05761685),
  Path(53, -1, -1, 218.6482656, -526.6127399),
  Person(13, 1, 5, -340.9819959, -419.7250114),
  Pier(141, -1, -1, 420.4775128, -338.5039593),
  Pillow(58, -1, -1, -517.602535, 125.9543737),
  Plant(18, -1, -1, 46.54577323, 90.99854601),
  Plate(143, -1, -1, -268.4739703, 288.2437313),
  Plaything(109, -1, -1, -385.1315573, 158.4866433),
  Pole(94, -1, 21, 125.8846825, 14.42984747),
  Poster(101, -1, -1, -39.56044586, -11.66720227),
  Pot(126, -1, -1, 33.08629207, 197.5872441),
  PottedPlant(-1, 11, -1, -177.8396706, 577.519247),
  Radiator(147, -1, -1, 413.2752371, -73.15311377),
  Refrigerator(51, -1, -1, -109.9788463, 392.6175483),
  Rider(-1, -1, 6, 793.1418, -299.3461536),
  River(61, -1, -1, 102.4491485, -329.7429194),
  Road(7, -1, 1, 192.3746356, -511.6771834),
  Rock(35, -1, -1, -6.241039285, -301.8946406),
  Rug(29, -1, -1, -473.5257313, 102.2791981),
  Runway(55, -1, -1, 497.0085752, -391.8961968),
  Sand(47, -1, -1, 21.63736422, -319.4846053),
  Sconce(135, -1, -1, 135.3593, 176.8855181),
  Screen(131, -1, -1, 211.8742416, 319.4397511),
  Sculpture(133, -1, -1, -55.1868699, 81.69675618),
  Sea(27, -1, -1, 101.5603394, -291.8242638),
  Seat(32, -1, -1, -551.9460398, -50.270591),
  Sheep(-1, 19, -1, -385.9518445, -548.0183802),
  Shelf(25, -1, -1, -103.2996294, 433.5059196),
  Ship(104, -1, -1, 463.3936732, -309.2559529),
  Shower(146, -1, -1, -610.8547396, 161.8713154),
  Sidewalk(12, -1, 2, 248.0986739, 8.577918283),
  Signboard(44, -1, -1, -5.419116373, -25.65985696),
  Sink(48, -1, -1, -673.6936878, 157.8934666),
  Sky(3, -1, 27, 75.43505202, -134.6972014),
  Skyscraper(49, -1, -1, 132.5747982, 598.0550594),
  Sofa(24, 13, -1, -497.6837308, 71.76594379),
  Stage(102, -1, -1, 157.094064, -98.2008598),
  Stairs(54, -1, -1, 285.6064476, -205.047834),
  Stairway(60, -1, -1, 292.3507335, -184.7718135),
  Step(122, -1, -1, 246.1425593, -540.0191824),
  Stool(111, -1, -1, -516.4169569, 20.73935604),
  Stove(72, -1, -1, -92.05056858, 334.9867087),
  Streetlight(88, -1, -1, 191.6381062, 122.9738249),
  Table(16, -1, -1, -274.4149944, 358.1667783),
  Tank(123, -1, -1, -207.2231095, 591.7516845),
  Television(90, -1, -1, 350.3946396, 435.7167532),
  Tent(115, -1, -1, 27.66798318, -79.94751751),
  Terrain(-1, -1, 26, 104.274801, -488.9673086),
  Toilet(66, -1, -1, -637.1344582, 155.0037259),
  Towel(82, -1, -1, -528.5923576, 161.3434719),
  Tower(85, -1, -1, 134.3333143, 576.3715414),
  Trailer(-1, -1, 14, 608.7297384, -219.8851771),
  Train(-1, 6, -1, 659.2793942, -328.4868615),
  Tray(138, -1, -1, -259.1735476, 321.4226862),
  Tree(5, -1, -1, 92.26642609, -9.853472285),
  Truck(84, -1, 8, 615.0385714, -252.8079723),
  Tunnel(-1, -1, 20, 354.3510864, -328.5146067),
  TV(142, 12, -1, 352.8485024, 415.0896732),
  Van(103, -1, -1, 641.3509836, -258.3262512),
  Vase(136, -1, -1, 69.37037896, 159.4852389),
  Vegetation(-1, -1, 25, -28.78451674, -401.854388),
  Wall(1, -1, 16, 379.5709872, -85.79154264),
  Wardrobe(36, -1, -1, -579.2448006, 338.2871216),
  Washer(108, -1, -1, -166.405649, 411.1878803),
  Water(22, -1, -1, 59.38126714, -328.5219907),
  Waterfall(114, -1, -1, 150.1613062, -376.2859769),
  Windowpane(9, -1, -1, 301.956054, 160.9925036),

  NOTHING(-1, -1, -1, -1000, -1000);

  private static final TIntObjectHashMap<DeepLabLabel> ade20kMap = new TIntObjectHashMap<>();
  private static final TIntObjectHashMap<DeepLabLabel> pascalvocMap = new TIntObjectHashMap<>();
  private static final TIntObjectHashMap<DeepLabLabel> cityscapesMap = new TIntObjectHashMap<>();

  static {
    for (DeepLabLabel label : DeepLabLabel.values()) {
      if (label.ade20kId > 0) {
        ade20kMap.put(label.ade20kId, label);
      }
      if (label.pascalvocId > 0) {
        pascalvocMap.put(label.pascalvocId, label);
      }
      if (label.cityscapesId > 0) {
        cityscapesMap.put(label.cityscapesId, label);
      }
    }
  }

  private final int ade20kId, pascalvocId, cityscapesId;
  private final float embeddX, embeddY;

  DeepLabLabel(int ade20kId, int pascalvocId, int cityscapesId, double embeddX, double embeddY) {
    this.ade20kId = ade20kId;
    this.pascalvocId = pascalvocId;
    this.cityscapesId = cityscapesId;
    this.embeddX = (float) embeddX;
    this.embeddY = (float) embeddY;
  }

  public static DeepLabLabel fromAde20kId(int id) {
    if (ade20kMap.containsKey(id)) {
      return ade20kMap.get(id);
    }
    return NOTHING;
  }

  public static DeepLabLabel[][] fromAde20kId(int[][] ids) {
    DeepLabLabel[][] _return = new DeepLabLabel[ids.length][ids[0].length];
    for (int i = 0; i < ids.length; ++i) {
      for (int j = 0; j < ids[0].length; ++j) {
        _return[i][j] = fromAde20kId(ids[i][j]);
      }
    }
    return _return;
  }

  public static DeepLabLabel fromPascalVocId(int id) {
    if (pascalvocMap.containsKey(id)) {
      return pascalvocMap.get(id);
    }
    return NOTHING;
  }

  public static DeepLabLabel[][] fromPascalVocId(int[][] ids) {
    DeepLabLabel[][] _return = new DeepLabLabel[ids.length][ids[0].length];
    for (int i = 0; i < ids.length; ++i) {
      for (int j = 0; j < ids[0].length; ++j) {
        _return[i][j] = fromPascalVocId(ids[i][j]);
      }
    }
    return _return;
  }

  public static DeepLabLabel fromCityscapesId(int id) {
    if (cityscapesMap.containsKey(id)) {
      return cityscapesMap.get(id);
    }
    return NOTHING;
  }

  public static DeepLabLabel[][] fromCityscapesId(int[][] ids) {
    DeepLabLabel[][] _return = new DeepLabLabel[ids.length][ids[0].length];
    for (int i = 0; i < ids.length; ++i) {
      for (int j = 0; j < ids[0].length; ++j) {
        _return[i][j] = fromCityscapesId(ids[i][j]);
      }
    }
    return _return;
  }

  public static DeepLabLabel getClosest(float x, float y) {
    float dist = 1e6f;
    DeepLabLabel closest = NOTHING;
    for (DeepLabLabel label : DeepLabLabel.values()) {
      float d = (float) Math.sqrt(
          (x - label.embeddX) * (x - label.embeddX) + (y - label.embeddY) * (y - label.embeddY));
      if (d < dist) {
        closest = label;
        if (d < 0.1f) {
          break;
        }
      }
    }
    return closest;
  }

  public static DeepLabLabel getDominantLabel(Collection<DeepLabLabel> labels) {

    TObjectIntHashMap<DeepLabLabel> hist = new TObjectIntHashMap<>();

    for (DeepLabLabel label : labels) {
      hist.adjustOrPutValue(label, 1, 1);
    }

    int max = 0;
    DeepLabLabel dominant = NOTHING;
    for (DeepLabLabel label : hist.keySet()) {
      if (hist.get(label) > max) {
        max = hist.get(label);
        dominant = label;
      }
    }

    return dominant;

  }

  public float getEmbeddX() {
    return this.embeddX;
  }

  public float getEmbeddY() {
    return this.embeddY;
  }


}
