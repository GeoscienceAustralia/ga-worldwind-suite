package geonames;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FeatureCodes
{
	private final static Map<String, FeatureClass> featureClasses = new HashMap<String, FeatureClass>();
	private final static Map<String, FeatureCode> featureCodes = new HashMap<String, FeatureCode>();

	public static FeatureClass getClass(String featureClass)
	{
		FeatureClass fc = featureClasses.get(featureClass);
		if(fc == null)
		{
			fc = new FeatureClass(featureClass, "Unknown", Font.decode("Arial-PLAIN-10"), Color.white);
		}
		return fc;
	}

	public static FeatureCode getCode(String featureCode)
	{
		FeatureCode fc = featureCodes.get(featureCode);
		if(fc == null)
		{
			fc = new FeatureCode(featureCode, "Unknown");
		}
		return fc;
	}

	static
	{
		init();
		fillCodes();
	}

	private static void fillCodes()
	{
		for (Entry<String, FeatureClass> entry : featureClasses.entrySet())
		{
			Collection<FeatureCode> codes = entry.getValue().getCodes();
			for (FeatureCode code : codes)
			{
				featureCodes.put(code.code, code);
			}
		}
	}

	private static void add(FeatureClass featureClass)
	{
		featureClasses.put(featureClass.code, featureClass);
	}

	private static void init()
	{
		FeatureClass fc;

		fc = new FeatureClass("A", "country, state, region", Font.decode("Arial-BOLD-11"), Color.lightGray);
		fc.add(new FeatureCode("ADM1", "first-order administrative division", "a primary administrative division of a country, such as a state in the United States"));
		fc.add(new FeatureCode("ADM2", "second-order administrative division", "a subdivision of a first-order administrative division"));
		fc.add(new FeatureCode("ADM3", "third-order administrative division", "a subdivision of a second-order administrative division"));
		fc.add(new FeatureCode("ADM4", "fourth-order administrative division", "a subdivision of a third-order administrative division"));
		fc.add(new FeatureCode("ADMD", "administrative division", "an administrative division of a country, undifferentiated as to administrative level"));
		fc.add(new FeatureCode("LTER", "leased area", "a tract of land leased by the United Kingdom from the People's Republic of China to form part of Hong Kong"));
		fc.add(new FeatureCode("PCL", "political entity"));
		fc.add(new FeatureCode("PCLD", "dependent political entity"));
		fc.add(new FeatureCode("PCLF", "freely associated state"));
		fc.add(new FeatureCode("PCLI", "independent political entity"));
		fc.add(new FeatureCode("PCLIX", "section of independent political entity"));
		fc.add(new FeatureCode("PCLS", "semi-independent political entity"));
		fc.add(new FeatureCode("PRSH", "parish", "an ecclesiastical district"));
		fc.add(new FeatureCode("TERR", "territory"));
		fc.add(new FeatureCode("ZN", "zone"));
		fc.add(new FeatureCode("ZNB", "buffer zone", "a zone recognized as a buffer between two nations in which military presence is minimal or absent"));
		add(fc);

		fc = new FeatureClass("H", "stream, lake", Font.decode("Arial-PLAIN-10"), Color.cyan);
		fc.add(new FeatureCode("AIRS", "seaplane landing area", "a place on a waterbody where floatplanes land and take off"));
		fc.add(new FeatureCode("ANCH", "anchorage", "an area where vessels may anchor"));
		fc.add(new FeatureCode("BAY", "bay", "a coastal indentation between two capes or headlands, larger than a cove but smaller than a gulf"));
		fc.add(new FeatureCode("BAYS", "bays", "coastal indentations between two capes or headlands, larger than a cove but smaller than a gulf"));
		fc.add(new FeatureCode("BGHT", "bight(s)", "an open body of water forming a slight recession in a coastline"));
		fc.add(new FeatureCode("BNK", "bank(s)", "an elevation, typically located on a shelf, over which the depth of water is relatively shallow but sufficient for most surface navigation"));
		fc.add(new FeatureCode("BNKR", "stream bank", "a sloping margin of a stream channel which normally confines the stream to its channel on land"));
		fc.add(new FeatureCode("BNKX", "section of bank", ""));
		fc.add(new FeatureCode("BOG", "bog(s)", "a wetland characterized by peat forming sphagnum moss, sedge, and other acid-water plants"));
		fc.add(new FeatureCode("CAPG", "icecap", "a dome-shaped mass of glacial ice covering an area of mountain summits or other high lands; smaller than an ice sheet"));
		fc.add(new FeatureCode("CHN", "channel", "the deepest part of a stream, bay, lagoon, or strait, through which the main current flows"));
		fc.add(new FeatureCode("CHNL", "lake channel(s)", "that part of a lake having water deep enough for navigation between islands, shoals, etc."));
		fc.add(new FeatureCode("CHNM", "marine channel", "that part of a body of water deep enough for navigation through an area otherwise not suitable"));
		fc.add(new FeatureCode("CHNN", "navigation channel", "a buoyed channel of sufficient depth for the safe navigation of vessels"));
		fc.add(new FeatureCode("CNFL", "confluence", "a place where two or more streams or intermittent streams flow together"));
		fc.add(new FeatureCode("CNL", "canal", "an artificial watercourse"));
		fc.add(new FeatureCode("CNLA", "aqueduct", "a conduit used to carry water"));
		fc.add(new FeatureCode("CNLB", "canal bend", "a conspicuously curved or bent section of a canal"));
		fc.add(new FeatureCode("CNLD", "drainage canal", "an artificial waterway carrying water away from a wetland or from drainage ditches"));
		fc.add(new FeatureCode("CNLI", "irrigation canal", "a canal which serves as a main conduit for irrigation water"));
		fc.add(new FeatureCode("CNLN", "navigation canal(s)", "a watercourse constructed for navigation of vessels"));
		fc.add(new FeatureCode("CNLQ", "abandoned canal", ""));
		fc.add(new FeatureCode("CNLSB", "underground irrigation canal(s)", "a gently inclined underground tunnel bringing water for irrigation from aquifers"));
		fc.add(new FeatureCode("CNLX", "section of canal", ""));
		fc.add(new FeatureCode("COVE", "cove(s)", "a small coastal indentation, smaller than a bay"));
		fc.add(new FeatureCode("CRKT", "tidal creek(s)", "a meandering channel in a coastal wetland subject to bi-directional tidal currents"));
		fc.add(new FeatureCode("CRNT", "current", "a horizontal flow of water in a given direction with uniform velocity"));
		fc.add(new FeatureCode("CUTF", "cutoff", "a channel formed as a result of a stream cutting through a meander neck"));
		fc.add(new FeatureCode("DCK", "dock(s)", "a waterway between two piers, or cut into the land for the berthing of ships"));
		fc.add(new FeatureCode("DCKB", "docking basin", "a part of a harbor where ships dock"));
		fc.add(new FeatureCode("DOMG", "icecap dome", "a comparatively elevated area on an icecap"));
		fc.add(new FeatureCode("DPRG", "icecap depression", "a comparatively depressed area on an icecap"));
		fc.add(new FeatureCode("DTCH", "ditch", "a small artificial watercourse dug for draining or irrigating the land"));
		fc.add(new FeatureCode("DTCHD", "drainage ditch", "a ditch which serves to drain the land"));
		fc.add(new FeatureCode("DTCHI", "irrigation ditch", "a ditch which serves to distribute irrigation water"));
		fc.add(new FeatureCode("DTCHM", "ditch mouth(s)", "an area where a drainage ditch enters a lagoon, lake or bay"));
		fc.add(new FeatureCode("ESTY", "estuary", "a funnel-shaped stream mouth or embayment where fresh water mixes with sea water under tidal influences"));
		fc.add(new FeatureCode("FISH", "fishing area", "a fishing ground, bank or area where fishermen go to catch fish"));
		fc.add(new FeatureCode("FJD", "fjord", "a long, narrow, steep-walled, deep-water arm of the sea at high latitudes, usually along mountainous coasts"));
		fc.add(new FeatureCode("FJDS", "fjords", "long, narrow, steep-walled, deep-water arms of the sea at high latitudes, usually along mountainous coasts"));
		fc.add(new FeatureCode("FLLS", "waterfall(s)", "a perpendicular or very steep descent of the water of a stream"));
		fc.add(new FeatureCode("FLLSX", "section of waterfall(s)", ""));
		fc.add(new FeatureCode("FLTM", "mud flat(s)", "a relatively level area of mud either between high and low tide lines, or subject to flooding"));
		fc.add(new FeatureCode("FLTT", "tidal flat(s)", "a large flat area of mud or sand attached to the shore and alternately covered and uncovered by the tide"));
		fc.add(new FeatureCode("GLCR", "glacier(s)",
				"a mass of ice, usually at high latitudes or high elevations, with sufficient thickness to flow away from the source area in lobes, tongues, or masses"));
		fc.add(new FeatureCode("GULF", "gulf", "a large recess in the coastline, larger than a bay"));
		fc.add(new FeatureCode("GYSR", "geyser", "a type of hot spring with intermittent eruptions of jets of hot water and steam"));
		fc.add(new FeatureCode("HBR", "harbor(s)", "a haven or space of deep water so sheltered by the adjacent land as to afford a safe anchorage for ships"));
		fc.add(new FeatureCode("HBRX", "section of harbor", ""));
		fc.add(new FeatureCode("INLT", "inlet", "a narrow waterway extending into the land, or connecting a bay or lagoon with a larger body of water"));
		fc.add(new FeatureCode("INLTQ", "former inlet", "an inlet which has been filled in, or blocked by deposits"));
		fc.add(new FeatureCode("LBED", "lake bed(s)", "a dried up or drained area of a former lake"));
		fc
				.add(new FeatureCode("LGN", "lagoon",
						"a shallow coastal waterbody, completely or partly separated from a larger body of water by a barrier island, coral reef or other depositional feature"));
		fc.add(new FeatureCode("LGNS", "lagoons",
				"shallow coastal waterbodies, completely or partly separated from a larger body of water by a barrier island, coral reef or other depositional feature"));
		fc.add(new FeatureCode("LGNX", "section of lagoon", ""));
		fc.add(new FeatureCode("LK", "lake", "a large inland body of standing water"));
		fc.add(new FeatureCode("LKC", "crater lake", "a lake in a crater or caldera"));
		fc.add(new FeatureCode("LKI", "intermittent lake", ""));
		fc.add(new FeatureCode("LKN", "salt lake", "an inland body of salt water with no outlet"));
		fc.add(new FeatureCode("LKNI", "intermittent salt lake", ""));
		fc.add(new FeatureCode("LKO", "oxbow lake", "a crescent-shaped lake commonly found adjacent to meandering streams"));
		fc.add(new FeatureCode("LKOI", "intermittent oxbow lake", ""));
		fc.add(new FeatureCode("LKS", "lakes", "large inland bodies of standing water"));
		fc.add(new FeatureCode("LKSB", "underground lake", "a standing body of water in a cave"));
		fc.add(new FeatureCode("LKSC", "crater lakes", "lakes in a crater or caldera"));
		fc.add(new FeatureCode("LKSI", "intermittent lakes", ""));
		fc.add(new FeatureCode("LKSN", "salt lakes", "inland bodies of salt water with no outlet"));
		fc.add(new FeatureCode("LKSNI", "intermittent salt lakes", ""));
		fc.add(new FeatureCode("LKX", "section of lake", ""));
		fc.add(new FeatureCode("MFGN", "salt evaporation ponds", "diked salt ponds used in the production of solar evaporated salt"));
		fc.add(new FeatureCode("MGV", "mangrove swamp", "a tropical tidal mud flat characterized by mangrove vegetation"));
		fc.add(new FeatureCode("MOOR", "moor(s)", "an area of open ground overlaid with wet peaty soils"));
		fc.add(new FeatureCode("MRSH", "marsh(es)", "a wetland dominated by grass-like vegetation"));
		fc.add(new FeatureCode("MRSHN", "salt marsh", "a flat area, subject to periodic salt water inundation, dominated by grassy salt-tolerant plants"));
		fc.add(new FeatureCode("NRWS", "narrows", "a navigable narrow part of a bay, strait, river, etc."));
		fc.add(new FeatureCode("OCN", "ocean", "one of the major divisions of the vast expanse of salt water covering part of the earth"));
		fc.add(new FeatureCode("OVF", "overfalls", "an area of breaking waves caused by the meeting of currents or by waves moving against the current"));
		fc.add(new FeatureCode("PND", "pond", "a small standing waterbody"));
		fc.add(new FeatureCode("PNDI", "intermittent pond", ""));
		fc.add(new FeatureCode("PNDN", "salt pond", "a small standing body of salt water often in a marsh or swamp, usually along a seacoast"));
		fc.add(new FeatureCode("PNDNI", "intermittent salt pond(s)", ""));
		fc.add(new FeatureCode("PNDS", "ponds", "small standing waterbodies"));
		fc.add(new FeatureCode("PNDSF", "fishponds", "ponds or enclosures in which fish are kept or raised"));
		fc.add(new FeatureCode("PNDSI", "intermittent ponds", ""));
		fc.add(new FeatureCode("PNDSN", "salt ponds", "small standing bodies of salt water often in a marsh or swamp, usually along a seacoast"));
		fc.add(new FeatureCode("POOL", "pool(s)", "a small and comparatively still, deep part of a larger body of water such as a stream or harbor; or a small body of standing water"));
		fc.add(new FeatureCode("POOLI", "intermittent pool", ""));
		fc.add(new FeatureCode("RCH", "reach", "a straight section of a navigable stream or channel between two bends"));
		fc.add(new FeatureCode("RDGG", "icecap ridge", "a linear elevation on an icecap"));
		fc.add(new FeatureCode("RDST", "roadstead", "an open anchorage affording less protection than a harbor"));
		fc.add(new FeatureCode("RF", "reef(s)", "a surface-navigation hazard composed of consolidated material"));
		fc.add(new FeatureCode("RFC", "coral reef(s)", "a surface-navigation hazard composed of coral"));
		fc.add(new FeatureCode("RFX", "section of reef", ""));
		fc.add(new FeatureCode("RPDS", "rapids", "a turbulent section of a stream associated with a steep, irregular stream bed"));
		fc.add(new FeatureCode("RSV", "reservoir(s)", "an artificial pond or lake"));
		fc.add(new FeatureCode("RSVI", "intermittent reservoir", ""));
		fc.add(new FeatureCode("RSVT", "water tank", "a contained pool or tank of water at, below, or above ground level"));
		fc.add(new FeatureCode("RVN", "ravine(s)", "a small, narrow, deep, steep-sided stream channel, smaller than a gorge"));
		fc.add(new FeatureCode("SBKH", "sabkha(s)", "a salt flat or salt encrusted plain subject to periodic inundation from flooding or high tides"));
		fc.add(new FeatureCode("SD", "sound", "a long arm of the sea forming a channel between the mainland and an island or islands; or connecting two larger bodies of water"));
		fc.add(new FeatureCode("SEA", "sea", "a large body of salt water more or less confined by continuous land or chains of islands forming a subdivision of an ocean"));
		fc.add(new FeatureCode("SHOL", "shoal(s)", "a surface-navigation hazard composed of unconsolidated material"));
		fc.add(new FeatureCode("SILL", "sill", "the low part of an underwater gap or saddle separating basins, including a similar feature at the mouth of a fjord"));
		fc.add(new FeatureCode("SPNG", "spring(s)", "a place where ground water flows naturally out of the ground"));
		fc.add(new FeatureCode("SPNS", "sulphur spring(s)", "a place where sulphur ground water flows naturally out of the ground"));
		fc.add(new FeatureCode("SPNT", "hot spring(s)", "a place where hot ground water flows naturally out of the ground"));
		fc.add(new FeatureCode("STM", "stream", "a body of running water moving to a lower level in a channel on land"));
		fc.add(new FeatureCode("STMA", "anabranch", "a diverging branch flowing out of a main stream and rejoining it downstream"));
		fc.add(new FeatureCode("STMB", "stream bend", "a conspicuously curved or bent segment of a stream"));
		fc.add(new FeatureCode("STMC", "canalized stream", "a stream that has been substantially ditched, diked, or straightened"));
		fc.add(new FeatureCode("STMD", "distributary(-ies)", "a branch which flows away from the main stream, as in a delta or irrigation canal"));
		fc.add(new FeatureCode("STMH", "headwaters", "the source and upper part of a stream, including the upper drainage basin"));
		fc.add(new FeatureCode("STMI", "intermittent stream", ""));
		fc.add(new FeatureCode("STMIX", "section of intermittent stream", ""));
		fc.add(new FeatureCode("STMM", "stream mouth(s)", "a place where a stream discharges into a lagoon, lake, or the sea"));
		fc.add(new FeatureCode("STMQ", "abandoned watercourse",
				"a former stream or distributary no longer carrying flowing water, but still evident due to lakes, wetland, topographic or vegetation patterns"));
		fc.add(new FeatureCode("STMS", "streams", "bodies of running water moving to a lower level in a channel on land"));
		fc.add(new FeatureCode("STMSB", "lost river", "a surface stream that disappears into an underground channel, or dries up in an arid area"));
		fc.add(new FeatureCode("STMX", "section of stream", ""));
		fc.add(new FeatureCode("STRT", "strait", "a relatively narrow waterway, usually narrower and less extensive than a sound, connecting two larger bodies of water"));
		fc.add(new FeatureCode("SWMP", "swamp", "a wetland dominated by tree vegetation"));
		fc.add(new FeatureCode("SYSI", "irrigation system", "a network of ditches and one or more of the following elements: water supply, reservoir, canal, pump, well, drain, etc."));
		fc.add(new FeatureCode("TNLC", "canal tunnel", "a tunnel through which a canal passes"));
		fc.add(new FeatureCode("WAD", "wadi",
				"a valley or ravine, bounded by relatively steep banks, which in the rainy season becomes a watercourse; found primarily in North Africa and the Middle East"));
		fc.add(new FeatureCode("WADB", "wadi bend", "a conspicuously curved or bent segment of a wadi"));
		fc.add(new FeatureCode("WADJ", "wadi junction", "a place where two or more wadies join"));
		fc.add(new FeatureCode("WADM", "wadi mouth", "the lower terminus of a wadi where it widens into an adjoining floodplain, depression, or waterbody"));
		fc.add(new FeatureCode("WADS", "wadies",
				"valleys or ravines, bounded by relatively steep banks, which in the rainy season become watercourses; found primarily in North Africa and the Middle East"));
		fc.add(new FeatureCode("WADX", "section of wadi", ""));
		fc.add(new FeatureCode("WHRL", "whirlpool", "a turbulent, rotating movement of water in a stream"));
		fc.add(new FeatureCode("WLL", "well", "a cylindrical hole, pit, or tunnel drilled or dug down to a depth from which water, oil, or gas can be pumped or brought to the surface"));
		fc.add(new FeatureCode("WLLQ", "abandoned well", ""));
		fc.add(new FeatureCode("WLLS", "wells", "cylindrical holes, pits, or tunnels drilled or dug down to a depth from which water, oil, or gas can be pumped or brought to the surface"));
		fc.add(new FeatureCode("WTLD", "wetland", "an area subject to inundation, usually characterized by bog, marsh, or swamp vegetation"));
		fc.add(new FeatureCode("WTLDI", "intermittent wetland", ""));
		fc.add(new FeatureCode("WTRC", "watercourse", "a natural, well-defined channel produced by flowing water, or an artificial channel designed to carry flowing water"));
		fc.add(new FeatureCode("WTRH", "waterhole(s)", "a natural hole, hollow, or small depression that contains water, used by man and animals, especially in arid areas"));
		add(fc);

		fc = new FeatureClass("L", "parks, area", Font.decode("Arial-PLAIN-10"), Color.green);
		fc.add(new FeatureCode("AGRC", "agricultural colony", "a tract of land set aside for agricultural settlement"));
		fc.add(new FeatureCode("AMUS", "amusement park", "Amusement Park are theme parks, adventure parks offering entertainment, similar to funfairs but with a fix location"));
		fc.add(new FeatureCode("AREA", "area", "a tract of land without homogeneous character or boundaries"));
		fc.add(new FeatureCode("BSND", "drainage basin", "an area drained by a stream"));
		fc.add(new FeatureCode("BSNP", "petroleum basin", "an area underlain by an oil-rich structural basin"));
		fc.add(new FeatureCode("BTL", "battlefield", "a site of a land battle of historical importance"));
		fc.add(new FeatureCode("CLG", "clearing", "an area in a forest with trees removed"));
		fc.add(new FeatureCode("CMN", "common", "a park or pasture for community use"));
		fc.add(new FeatureCode("CNS", "concession area", "a lease of land by a government for economic development, e.g., mining, forestry"));
		fc.add(new FeatureCode("COLF", "coalfield", "a region in which coal deposits of possible economic value occur"));
		fc.add(new FeatureCode("CONT", "continent", "continent : Europe, Africa, Asia, North America, South America, Oceania,Antarctica"));
		fc.add(new FeatureCode("CST", "coast", "a zone of variable width straddling the shoreline"));
		fc.add(new FeatureCode("CTRB", "business center", "a place where a number of businesses are located"));
		fc.add(new FeatureCode("DEVH", "housing development", "a tract of land on which many houses of similar design are built according to a development plan"));
		fc.add(new FeatureCode("FLD", "field(s)", "an open as opposed to wooded area"));
		fc.add(new FeatureCode("FLDI", "irrigated field(s)", "a tract of level or terraced land which is irrigated"));
		fc.add(new FeatureCode("GASF", "gasfield", "an area containing a subterranean store of natural gas of economic value"));
		fc.add(new FeatureCode("GRAZ", "grazing area", "an area of grasses and shrubs used for grazing"));
		fc.add(new FeatureCode("GVL", "gravel area", "an area covered with gravel"));
		fc.add(new FeatureCode("INDS", "industrial area", "an area characterized by industrial activity"));
		fc.add(new FeatureCode("LAND", "arctic land", "a tract of land in the Arctic"));
		fc.add(new FeatureCode("LCTY", "locality", "a minor area or place of unspecified or mixed character and indefinite boundaries"));
		fc.add(new FeatureCode("MILB", "military base",
				"a place used by an army or other armed service for storing arms and supplies, and for accommodating and training troops, a base from which operations can be initiated"));
		fc.add(new FeatureCode("MNA", "mining area", "an area of mine sites where minerals and ores are extracted"));
		fc.add(new FeatureCode("MVA", "maneuver area", "a tract of land where military field exercises are carried out"));
		fc.add(new FeatureCode("NVB", "naval base",
				"an area used to store supplies, provide barracks for troops and naval personnel, a port for naval vessels, and from which operations are initiated"));
		fc.add(new FeatureCode("OAS", "oasis(-es)", "an area in a desert made productive by the availability of water"));
		fc.add(new FeatureCode("OILF", "oilfield", "an area containing a subterranean store of petroleum of economic value"));
		fc.add(new FeatureCode("PEAT", "peat cutting area", "an area where peat is harvested"));
		fc.add(new FeatureCode("PRK", "park", "an area, often of forested land, maintained as a place of beauty, or for recreation"));
		fc.add(new FeatureCode("PRT", "port", "a place provided with terminal and transfer facilities for loading and discharging waterborne cargo or passengers, usually located in a harbor"));
		fc.add(new FeatureCode("QCKS", "quicksand", "an area where loose sand with water moving through it may become unstable when heavy objects are placed at the surface, causing them to sink"));
		fc.add(new FeatureCode("REP", "republic", ""));
		fc.add(new FeatureCode("RES", "reserve", "a tract of public land reserved for future use or restricted as to use"));
		fc.add(new FeatureCode("RESA", "agricultural reserve", "a tract of land reserved for agricultural reclamation and/or development"));
		fc.add(new FeatureCode("RESF", "forest reserve", "a forested area set aside for preservation or controlled use"));
		fc.add(new FeatureCode("RESH", "hunting reserve", "a tract of land used primarily for hunting"));
		fc.add(new FeatureCode("RESN", "nature reserve", "an area reserved for the maintenance of a natural habitat"));
		fc.add(new FeatureCode("RESP", "palm tree reserve", "an area of palm trees where use is controlled"));
		fc.add(new FeatureCode("RESV", "reservation", "a tract of land set aside for aboriginal, tribal, or native populations"));
		fc.add(new FeatureCode("RESW", "wildlife reserve", "a tract of public land reserved for the preservation of wildlife"));
		fc.add(new FeatureCode("RGN", "region", "an area distinguished by one or more observable physical or cultural characteristics"));
		fc.add(new FeatureCode("RGNE", "economic region", "a region of a country established for economic development or for statistical purposes"));
		fc.add(new FeatureCode("RGNL", "lake region", "a tract of land distinguished by numerous lakes"));
		fc.add(new FeatureCode("RNGA", "artillery range", "a tract of land used for artillery firing practice"));
		fc.add(new FeatureCode("SALT", "salt area", "a shallow basin or flat where salt accumulates after periodic inundation"));
		fc.add(new FeatureCode("SNOW", "snowfield", "an area of permanent snow and ice forming the accumulation area of a glacier"));
		fc.add(new FeatureCode("TRB", "tribal area", "a tract of land used by nomadic or other tribes"));
		fc.add(new FeatureCode("ZZZZZ", "master source holdings list", ""));
		add(fc);

		fc = new FeatureClass("P", "city, village", Font.decode("Arial-BOLD-11"), Color.yellow);
		fc.add(new FeatureCode("PPL", "populated place", "a city, town, village, or other agglomeration of buildings where people live and work"));
		fc.add(new FeatureCode("PPLA", "seat of a first-order administrative division", "seat of a first-order administrative division (PPLC takes precedence over PPLA)"));
		fc.add(new FeatureCode("PPLC", "capital of a political entity", ""));
		fc.add(new FeatureCode("PPLG", "seat of government of a political entity", ""));
		fc.add(new FeatureCode("PPLL", "populated locality", "an area similar to a locality but with a small group of dwellings or other buildings"));
		fc.add(new FeatureCode("PPLQ", "abandoned populated place", ""));
		fc.add(new FeatureCode("PPLR", "religious populated place", "a populated place whose population is largely engaged in religious occupations"));
		fc.add(new FeatureCode("PPLS", "populated places", "cities, towns, villages, or other agglomerations of buildings where people live and work"));
		fc.add(new FeatureCode("PPLW", "destroyed populated place", "a village, town or city destroyed by a natural disaster, or by war"));
		fc.add(new FeatureCode("PPLX", "section of populated place", ""));
		fc.add(new FeatureCode("STLMT", "israeli settlement", ""));
		add(fc);

		fc = new FeatureClass("R", "road, railroad", Font.decode("Arial-PLAIN-10"), Color.red);
		fc.add(new FeatureCode("CSWY", "causeway", "a raised roadway across wet ground or shallow water"));
		fc.add(new FeatureCode("CSWYQ", "former causeway", "a causeway no longer used for transportation"));
		fc.add(new FeatureCode("OILP", "oil pipeline", "a pipeline used for transporting oil"));
		fc.add(new FeatureCode("PRMN", "promenade", "a place for public walking, usually along a beach front"));
		fc.add(new FeatureCode("PTGE", "portage", "a place where boats, goods, etc., are carried overland between navigable waters"));
		fc.add(new FeatureCode("RD", "road", "an open way with improved surface for transportation of animals, people and vehicles"));
		fc.add(new FeatureCode("RDA", "ancient road", "the remains of a road used by ancient cultures"));
		fc.add(new FeatureCode("RDB", "road bend", "a conspicuously curved or bent section of a road"));
		fc.add(new FeatureCode("RDCUT", "road cut", "an excavation cut through a hill or ridge for a road"));
		fc.add(new FeatureCode("RDJCT", "road junction", "a place where two or more roads join"));
		fc.add(new FeatureCode("RJCT", "railroad junction", "a place where two or more railroad tracks join"));
		fc.add(new FeatureCode("RR", "railroad", "a permanent twin steel-rail track on which freight and passenger cars move long distances"));
		fc.add(new FeatureCode("RRQ", "abandoned railroad", ""));
		fc.add(new FeatureCode("RTE", "caravan route", "the route taken by caravans"));
		fc.add(new FeatureCode("RYD", "railroad yard", "a system of tracks used for the making up of trains, and switching and storing freight cars"));
		fc.add(new FeatureCode("ST", "street", "a paved urban thoroughfare"));
		fc.add(new FeatureCode("STKR", "stock route", "a route taken by livestock herds"));
		fc.add(new FeatureCode("TNL", "tunnel", "a subterranean passageway for transportation"));
		fc.add(new FeatureCode("TNLN", "natural tunnel", "a cave that is open at both ends"));
		fc.add(new FeatureCode("TNLRD", "road tunnel", "a tunnel through which a road passes"));
		fc.add(new FeatureCode("TNLRR", "railroad tunnel", "a tunnel through which a railroad passes"));
		fc.add(new FeatureCode("TNLS", "tunnels", "subterranean passageways for transportation"));
		fc.add(new FeatureCode("TRL", "trail", "a path, track, or route used by pedestrians, animals, or off-road vehicles"));
		add(fc);

		fc = new FeatureClass("S", "spot, building, farm", Font.decode("Arial-PLAIN-10"), Color.pink);
		fc.add(new FeatureCode("ADMF", "administrative facility", "a government building"));
		fc.add(new FeatureCode("AGRF", "agricultural facility", "a building and/or tract of land used for improving agriculture"));
		fc
				.add(new FeatureCode("AIRB", "airbase",
						"an area used to store supplies, provide barracks for air force personnel, hangars and runways for aircraft, and from which operations are initiated"));
		fc.add(new FeatureCode("AIRF", "airfield", "a place on land where aircraft land and take off; no facilities provided for the commercial handling of passengers and cargo"));
		fc.add(new FeatureCode("AIRH", "heliport", "a place where helicopters land and take off"));
		fc.add(new FeatureCode("AIRP", "airport",
				"a place where aircraft regularly land and take off, with runways, navigational aids, and major facilities for the commercial handling of passengers and cargo"));
		fc.add(new FeatureCode("AIRQ", "abandoned airfield", ""));
		fc.add(new FeatureCode("AMTH", "amphitheater", "an oval or circular structure with rising tiers of seats about a stage or open space"));
		fc.add(new FeatureCode("ANS", "ancient site", "a place where archeological remains, old structures, or cultural artifacts are located"));
		fc.add(new FeatureCode("ARCH", "arch", "a natural or man-made structure in the form of an arch"));
		fc.add(new FeatureCode("ASTR", "astronomical station", "a point on the earth whose position has been determined by observations of celestial bodies"));
		fc.add(new FeatureCode("ASYL", "asylum", "a facility where the insane are cared for and protected"));
		fc.add(new FeatureCode("ATHF", "athletic field", "a tract of land used for playing team sports, and athletic track and field events"));
		fc
				.add(new FeatureCode("ATM", "autmatic teller machine",
						"An unattended electronic machine in a public place, connected to a data system and related equipment and activated by a bank customer to obtain cash withdrawals and other banking services."));
		fc.add(new FeatureCode("BANK", "bank", "A business establishment in which money is kept for saving or commercial purposes or is invested, supplied for loans, or exchanged."));
		fc.add(new FeatureCode("BCN", "beacon", "a fixed artificial navigation mark"));
		fc.add(new FeatureCode("BDG", "bridge", "a structure erected across an obstacle such as a stream, road, etc., in order to carry roads, railroads, and pedestrians across"));
		fc.add(new FeatureCode("BDGQ", "ruined bridge", "a destroyed or decayed bridge which is no longer functional"));
		fc.add(new FeatureCode("BLDG", "building(s)", "a structure built for permanent use, as a house, factory, etc."));
		fc.add(new FeatureCode("BP", "boundary marker", "a fixture marking a point along a boundary"));
		fc.add(new FeatureCode("BRKS", "barracks", "a building for lodging military personnel"));
		fc.add(new FeatureCode("BRKW", "breakwater", "a structure erected to break the force of waves at the entrance to a harbor or port"));
		fc.add(new FeatureCode("BSTN", "baling station", "a facility for baling agricultural products"));
		fc.add(new FeatureCode("BTYD", "boatyard", "a waterside facility for servicing, repairing, and building small vessels"));
		fc.add(new FeatureCode("BUR", "burial cave(s)", "a cave used for human burials"));
		fc.add(new FeatureCode("BUSTN", "Bus Station", "a facility comprising ticket office, platforms, etc. for loading and unloading passengers"));
		fc.add(new FeatureCode("BUSTP", "Bus Stop", "a place lacking station facilities"));
		fc.add(new FeatureCode("CARN", "cairn", "a heap of stones erected as a landmark or for other purposes"));
		fc.add(new FeatureCode("CAVE", "cave(s)", "an underground passageway or chamber, or cavity on the side of a cliff"));
		fc.add(new FeatureCode("CCL", "Centre Continuous Learning", "Centres for Continuous Learning"));
		fc.add(new FeatureCode("CH", "church", "a building for public Christian worship"));
		fc.add(new FeatureCode("CMP", "camp(s)", "a site occupied by tents, huts, or other shelters for temporary use"));
		fc.add(new FeatureCode("CMPL", "logging camp", "a camp used by loggers"));
		fc.add(new FeatureCode("CMPLA", "labor camp", "a camp used by migrant or temporary laborers"));
		fc.add(new FeatureCode("CMPMN", "mining camp", "a camp used by miners"));
		fc.add(new FeatureCode("CMPO", "oil camp", "a camp used by oilfield workers"));
		fc.add(new FeatureCode("CMPQ", "abandoned camp", ""));
		fc.add(new FeatureCode("CMPRF", "refugee camp", "a camp used by refugees"));
		fc.add(new FeatureCode("CMTY", "cemetery", "a burial place or ground"));
		fc.add(new FeatureCode("COMC", "communication center", "a facility, including buildings, antennae, towers and electronic equipment for receiving and transmitting information"));
		fc.add(new FeatureCode("CRRL", "corral(s)", "a pen or enclosure for confining or capturing animals"));
		fc.add(new FeatureCode("CSNO", "casino", "a building used for entertainment, especially gambling"));
		fc.add(new FeatureCode("CSTL", "castle", "a large fortified building or set of buildings"));
		fc.add(new FeatureCode("CSTM", "customs house", "a building in a port where customs and duties are paid, and where vessels are entered and cleared"));
		fc.add(new FeatureCode("CTHSE", "courthouse", "a building in which courts of law are held"));
		fc.add(new FeatureCode("CTRA", "atomic center", "a facility where atomic research is carried out"));
		fc.add(new FeatureCode("CTRCM", "community center", "a facility for community recreation and other activities"));
		fc.add(new FeatureCode("CTRF", "facility center", "a place where more than one facility is situated"));
		fc.add(new FeatureCode("CTRM", "medical center",
				"a complex of health care buildings including two or more of the following: hospital, medical school, clinic, pharmacy, doctor's offices, etc."));
		fc.add(new FeatureCode("CTRR", "religious center", "a facility where more than one religious activity is carried out, e.g., retreat, school, monastery, worship"));
		fc.add(new FeatureCode("CTRS", "space center", "a facility for launching, tracking, or controlling satellites and space vehicles"));
		fc.add(new FeatureCode("CVNT", "convent", "a building where a community of nuns lives in seclusion"));
		fc.add(new FeatureCode("DAM", "dam", "a barrier constructed across a stream to impound water"));
		fc.add(new FeatureCode("DAMQ", "ruined dam", "a destroyed or decayed dam which is no longer functional"));
		fc.add(new FeatureCode("DAMSB", "sub-surface dam", "a dam put down to bedrock in a sand river"));
		fc.add(new FeatureCode("DARY", "dairy", "a facility for the processing, sale and distribution of milk or milk products"));
		fc.add(new FeatureCode("DCKD", "dry dock", "a dock providing support for a vessel, and means for removing the water so that the bottom of the vessel can be exposed"));
		fc.add(new FeatureCode("DCKY", "dockyard", "a facility for servicing, building, or repairing ships"));
		fc.add(new FeatureCode("DIKE", "dike", "an earth or stone embankment usually constructed for flood or stream control"));
		fc.add(new FeatureCode("DPOF", "fuel depot", "an area where fuel is stored"));
		fc.add(new FeatureCode("EST", "estate(s)", "a large commercialized agricultural landholding with associated buildings and other facilities"));
		fc.add(new FeatureCode("ESTB", "banana plantation", "an estate that specializes in the growing of bananas"));
		fc.add(new FeatureCode("ESTC", "cotton plantation", "an estate specializing in the cultivation of cotton"));
		fc.add(new FeatureCode("ESTO", "oil palm plantation", "an estate specializing in the cultivation of oil palm trees"));
		fc.add(new FeatureCode("ESTR", "rubber plantation", "an estate which specializes in growing and tapping rubber trees"));
		fc.add(new FeatureCode("ESTSG", "sugar plantation", "an estate that specializes in growing sugar cane"));
		fc.add(new FeatureCode("ESTSL", "sisal plantation", "an estate that specializes in growing sisal"));
		fc.add(new FeatureCode("ESTT", "tea plantation", "an estate which specializes in growing tea bushes"));
		fc.add(new FeatureCode("ESTX", "section of estate", ""));
		fc.add(new FeatureCode("FCL", "facility", "a building or buildings housing a center, institute, foundation, hospital, prison, mission, courthouse, etc."));
		fc.add(new FeatureCode("FNDY", "foundry", "a building or works where metal casting is carried out"));
		fc.add(new FeatureCode("FRM", "farm", "a tract of land with associated buildings devoted to agriculture"));
		fc.add(new FeatureCode("FRMQ", "abandoned farm", ""));
		fc.add(new FeatureCode("FRMS", "farms", "tracts of land with associated buildings devoted to agriculture"));
		fc.add(new FeatureCode("FRMT", "farmstead", "the buildings and adjacent service areas of a farm"));
		fc.add(new FeatureCode("FT", "fort", "a defensive structure or earthworks"));
		fc.add(new FeatureCode("FY", "ferry", "a boat or other floating conveyance and terminal facilities regularly used to transport people and vehicles across a waterbody"));
		fc.add(new FeatureCode("GATE", "gate", "a controlled access entrance or exit"));
		fc.add(new FeatureCode("GDN", "garden(s)", "an enclosure for displaying selected plant or animal life"));
		fc.add(new FeatureCode("GHSE", "guest house", "a house used to provide lodging for paying guests"));
		fc.add(new FeatureCode("GOSP", "gas-oil separator plant", "a facility for separating gas from oil"));
		fc.add(new FeatureCode("GRVE", "grave", "a burial site"));
		fc.add(new FeatureCode("HERM", "hermitage", "a secluded residence, usually for religious sects"));
		fc.add(new FeatureCode("HLT", "halting place", "a place where caravans stop for rest"));
		fc.add(new FeatureCode("HSE", "house(s)", "a building used as a human habitation"));
		fc.add(new FeatureCode("HSEC", "country house", "a large house, mansion, or chateau, on a large estate"));
		fc.add(new FeatureCode("HSP", "hospital", "a building in which sick or injured, especially those confined to bed, are medically treated"));
		fc.add(new FeatureCode("HSPC", "clinic", "a medical facility associated with a hospital for outpatients"));
		fc.add(new FeatureCode("HSPD", "dispensary", "a building where medical or dental aid is dispensed"));
		fc.add(new FeatureCode("HSPL", "leprosarium", "an asylum or hospital for lepers"));
		fc.add(new FeatureCode("HSTS", "historical site", "a place of historical importance"));
		fc.add(new FeatureCode("HTL", "hotel", "a building providing lodging and/or meals for the public"));
		fc.add(new FeatureCode("HUT", "hut", "a small primitive house"));
		fc.add(new FeatureCode("HUTS", "huts", "small primitive houses"));
		fc.add(new FeatureCode("INSM", "military installation", "a facility for use of and control by armed forces"));
		fc.add(new FeatureCode("ITTR", "research institute", "a facility where research is carried out"));
		fc.add(new FeatureCode("JTY", "jetty", "a structure built out into the water at a river mouth or harbor entrance to regulate currents and silting"));
		fc.add(new FeatureCode("LDNG", "landing", "a place where boats receive or discharge passengers and freight, but lacking most port facilities"));
		fc.add(new FeatureCode("LEPC", "leper colony", "a settled area inhabited by lepers in relative isolation"));
		fc.add(new FeatureCode("LIBR", "library", "A place in which information resources such as books are kept for reading, reference, or lending."));
		fc.add(new FeatureCode("LOCK", "lock(s)", "a basin in a waterway with gates at each end by means of which vessels are passed from one water level to another"));
		fc.add(new FeatureCode("LTHSE", "lighthouse", "a distinctive structure exhibiting a major navigation light"));
		fc.add(new FeatureCode("MALL", "mall", "A large, often enclosed shopping complex containing various stores, businesses, and restaurants usually accessible by common passageways."));
		fc.add(new FeatureCode("MAR", "marina", "a harbor facility for small boats, yachts, etc."));
		fc.add(new FeatureCode("MFG", "factory", "one or more buildings where goods are manufactured, processed or fabricated"));
		fc.add(new FeatureCode("MFGB", "brewery", "one or more buildings where beer is brewed"));
		fc.add(new FeatureCode("MFGC", "cannery", "a building where food items are canned"));
		fc.add(new FeatureCode("MFGCU", "copper works", "a facility for processing copper ore"));
		fc.add(new FeatureCode("MFGLM", "limekiln", "a furnace in which limestone is reduced to lime"));
		fc.add(new FeatureCode("MFGM", "munitions plant", "a factory where ammunition is made"));
		fc.add(new FeatureCode("MFGPH", "phosphate works", "a facility for producing fertilizer"));
		fc.add(new FeatureCode("MFGQ", "abandoned factory", ""));
		fc.add(new FeatureCode("MFGSG", "sugar refinery", "a facility for converting raw sugar into refined sugar"));
		fc.add(new FeatureCode("MKT", "market", "a place where goods are bought and sold at regular intervals"));
		fc.add(new FeatureCode("ML", "mill(s)", "a building housing machines for transforming, shaping, finishing, grinding, or extracting products"));
		fc.add(new FeatureCode("MLM", "ore treatment plant", "a facility for improving the metal content of ore by concentration"));
		fc.add(new FeatureCode("MLO", "olive oil mill", "a mill where oil is extracted from olives"));
		fc.add(new FeatureCode("MLSG", "sugar mill", "a facility where sugar cane is processed into raw sugar"));
		fc.add(new FeatureCode("MLSGQ", "former sugar mill", "a sugar mill no longer used as a sugar mill"));
		fc.add(new FeatureCode("MLSW", "sawmill", "a mill where logs or lumber are sawn to specified shapes and sizes"));
		fc.add(new FeatureCode("MLWND", "windmill", "a mill or water pump powered by wind"));
		fc.add(new FeatureCode("MLWTR", "water mill", "a mill powered by running water"));
		fc.add(new FeatureCode("MN", "mine(s)", "a site where mineral ores are extracted from the ground by excavating surface pits and subterranean passages"));
		fc.add(new FeatureCode("MNAU", "gold mine(s)", "a mine where gold ore, or alluvial gold is extracted"));
		fc.add(new FeatureCode("MNC", "coal mine(s)", "a mine where coal is extracted"));
		fc.add(new FeatureCode("MNCR", "chrome mine(s)", "a mine where chrome ore is extracted"));
		fc.add(new FeatureCode("MNCU", "copper mine(s)", "a mine where copper ore is extracted"));
		fc.add(new FeatureCode("MNDT", "diatomite mine(s)", "a place where diatomaceous earth is extracted"));
		fc.add(new FeatureCode("MNFE", "iron mine(s)", "a mine where iron ore is extracted"));
		fc.add(new FeatureCode("MNMT", "monument", "a commemorative structure or statue"));
		fc.add(new FeatureCode("MNN", "salt mine(s)", "a mine from which salt is extracted"));
		fc.add(new FeatureCode("MNNI", "nickel mine(s)", "a mine where nickel ore is extracted"));
		fc.add(new FeatureCode("MNPB", "lead mine(s)", "a mine where lead ore is extracted"));
		fc.add(new FeatureCode("MNPL", "placer mine(s)", "a place where heavy metals are concentrated and running water is used to extract them from unconsolidated sediments"));
		fc.add(new FeatureCode("MNQ", "abandoned mine", ""));
		fc.add(new FeatureCode("MNQR", "quarry(-ies)", "a surface mine where building stone or gravel and sand, etc. are extracted"));
		fc.add(new FeatureCode("MNSN", "tin mine(s)", "a mine where tin ore is extracted"));
		fc.add(new FeatureCode("MOLE", "mole", "a massive structure of masonry or large stones serving as a pier or breakwater"));
		fc.add(new FeatureCode("MSQE", "mosque", "a building for public Islamic worship"));
		fc
				.add(new FeatureCode("MSSN", "mission",
						"a place characterized by dwellings, school, church, hospital and other facilities operated by a religious group for the purpose of providing charitable services and to propagate religion"));
		fc.add(new FeatureCode("MSSNQ", "abandoned mission", ""));
		fc.add(new FeatureCode("MSTY", "monastery", "a building and grounds where a community of monks lives in seclusion"));
		fc.add(new FeatureCode("MTRO", "metro station", "metro station (Underground, Tube, or Méo)"));
		fc.add(new FeatureCode("MUS", "museum", "a building where objects of permanent interest in one or more of the arts and sciences are preserved and exhibited"));
		fc.add(new FeatureCode("NOV", "novitiate", "a religious house or school where novices are trained"));
		fc.add(new FeatureCode("NSY", "nursery(-ies)", "a place where plants are propagated for transplanting or grafting"));
		fc.add(new FeatureCode("OBPT", "observation point", "a wildlife or scenic observation point"));
		fc.add(new FeatureCode("OBS", "observatory", "a facility equipped for observation of atmospheric or space phenomena"));
		fc.add(new FeatureCode("OBSR", "radio observatory", "a facility equipped with an array of antennae for receiving radio waves from space"));
		fc.add(new FeatureCode("OILJ", "oil pipeline junction", "a section of an oil pipeline where two or more pipes join together"));
		fc.add(new FeatureCode("OILQ", "abandoned oil well", ""));
		fc.add(new FeatureCode("OILR", "oil refinery", "a facility for converting crude oil into refined petroleum products"));
		fc.add(new FeatureCode("OILT", "tank farm", "a tract of land occupied by large, cylindrical, metal tanks in which oil or liquid petrochemicals are stored"));
		fc.add(new FeatureCode("OILW", "oil well", "a well from which oil may be pumped"));
		fc.add(new FeatureCode("OPRA", "opera house", "A theater designed chiefly for the performance of operas."));
		fc.add(new FeatureCode("PAL", "palace", "a large stately house, often a royal or presidential residence"));
		fc.add(new FeatureCode("PGDA", "pagoda", "a tower-like storied structure, usually a Buddhist shrine"));
		fc.add(new FeatureCode("PIER", "pier", "a structure built out into navigable water on piles providing berthing for ships and recreation"));
		fc.add(new FeatureCode("PKLT", "parking lot", "an area used for parking vehicles"));
		fc.add(new FeatureCode("PMPO", "oil pumping station", "a facility for pumping oil through a pipeline"));
		fc.add(new FeatureCode("PMPW", "water pumping station", "a facility for pumping water from a major well or through a pipeline"));
		fc.add(new FeatureCode("PO", "post office", "a public building in which mail is received, sorted and distributed"));
		fc.add(new FeatureCode("PP", "police post", "a building in which police are stationed"));
		fc.add(new FeatureCode("PPQ", "abandoned police post", ""));
		fc.add(new FeatureCode("PRKGT", "park gate", "a controlled access to a park"));
		fc.add(new FeatureCode("PRKHQ", "park headquarters", "a park administrative facility"));
		fc.add(new FeatureCode("PRN", "prison", "a facility for confining prisoners"));
		fc.add(new FeatureCode("PRNJ", "reformatory", "a facility for confining, training, and reforming young law offenders"));
		fc.add(new FeatureCode("PRNQ", "abandoned prison", ""));
		fc.add(new FeatureCode("PS", "power station", "a facility for generating electric power"));
		fc.add(new FeatureCode("PSH", "hydroelectric power station", "a building where electricity is generated from water power"));
		fc.add(new FeatureCode("PSTB", "border post", "a post or station at an international boundary for the regulation of movement of people and goods"));
		fc.add(new FeatureCode("PSTC", "customs post", "a building at an international boundary where customs and duties are paid on goods"));
		fc.add(new FeatureCode("PSTP", "patrol post", "a post from which patrols are sent out"));
		fc.add(new FeatureCode("PYR", "pyramid", "an ancient massive structure of square ground plan with four triangular faces meeting at a point and used for enclosing tombs"));
		fc.add(new FeatureCode("PYRS", "pyramids", "ancient massive structures of square ground plan with four triangular faces meeting at a point and used for enclosing tombs"));
		fc.add(new FeatureCode("QUAY", "quay", "a structure of solid construction along a shore or bank which provides berthing for ships and which generally provides cargo handling facilities"));
		fc.add(new FeatureCode("RECG", "golf course", "a recreation field where golf is played"));
		fc.add(new FeatureCode("RECR", "racetrack", "a track where races are held"));
		fc.add(new FeatureCode("REST", "restaurant", "A place where meals are served to the public"));
		fc.add(new FeatureCode("RHSE", "resthouse", "a structure maintained for the rest and shelter of travelers"));
		fc.add(new FeatureCode("RKRY", "rookery", "a breeding place of a colony of birds or seals"));
		fc.add(new FeatureCode("RLG", "religious site", "an ancient site of significant religious importance"));
		fc.add(new FeatureCode("RLGR", "retreat", "a place of temporary seclusion, especially for religious groups"));
		fc.add(new FeatureCode("RNCH", "ranch(es)", "a large farm specializing in extensive grazing of livestock"));
		fc.add(new FeatureCode("RSD", "railroad siding", "a short track parallel to and joining the main track"));
		fc.add(new FeatureCode("RSGNL", "railroad signal", "a signal at the entrance of a particular section of track governing the movement of trains"));
		fc.add(new FeatureCode("RSRT", "resort", "a specialized facility for vacation, health, or participation sports activities"));
		fc.add(new FeatureCode("RSTN", "railroad station", "a facility comprising ticket office, platforms, etc. for loading and unloading train passengers and freight"));
		fc.add(new FeatureCode("RSTNQ", "abandoned railroad station", ""));
		fc.add(new FeatureCode("RSTP", "railroad stop", "a place lacking station facilities where trains stop to pick up and unload passengers and freight"));
		fc.add(new FeatureCode("RSTPQ", "abandoned railroad stop", ""));
		fc.add(new FeatureCode("RUIN", "ruin(s)", "a destroyed or decayed structure which is no longer functional"));
		fc.add(new FeatureCode("SCH", "school", "building(s) where instruction in one or more branches of knowledge takes place"));
		fc.add(new FeatureCode("SCHA", "agricultural school", "a school with a curriculum focused on agriculture"));
		fc.add(new FeatureCode("SCHC", "college", "the grounds and buildings of an institution of higher learning"));
		fc.add(new FeatureCode("SCHD", "Driving School", "Driving School"));
		fc.add(new FeatureCode("SCHL", "Language School", "Language Schools & Institutions"));
		fc.add(new FeatureCode("SCHM", "military school", "a school at which military science forms the core of the curriculum"));
		fc.add(new FeatureCode("SCHN", "maritime school", "a school at which maritime sciences form the core of the curriculum"));
		fc.add(new FeatureCode("SECP", "State Exam Prep Centre", "State Exam Preparation Centres"));
		fc.add(new FeatureCode("SHPF", "sheepfold", "a fence or wall enclosure for sheep and other small herd animals"));
		fc.add(new FeatureCode("SHRN", "shrine", "a structure or place memorializing a person or religious concept"));
		fc.add(new FeatureCode("SHSE", "storehouse", "a building for storing goods, especially provisions"));
		fc.add(new FeatureCode("SLCE", "sluice", "a conduit or passage for carrying off surplus water from a waterbody, usually regulated by means of a sluice gate"));
		fc.add(new FeatureCode("SNTR", "sanatorium", "a facility where victims of physical or mental disorders are treated"));
		fc.add(new FeatureCode("SPA", "spa", "a resort area usually developed around a medicinal spring"));
		fc.add(new FeatureCode("SPLY", "spillway", "a passage or outlet through which surplus water flows over, around or through a dam"));
		fc.add(new FeatureCode("SQR", "square", "a broad, open, public area near the center of a town or city"));
		fc.add(new FeatureCode("STBL", "stable", "a building for the shelter and feeding of farm animals, especially horses"));
		fc.add(new FeatureCode("STDM", "stadium", "a structure with an enclosure for athletic games with tiers of seats for spectators"));
		fc.add(new FeatureCode("STNB", "scientific research base", "a scientific facility used as a base from which research is carried out or monitored"));
		fc.add(new FeatureCode("STNC", "coast guard station", "a facility from which the coast is guarded by armed vessels"));
		fc.add(new FeatureCode("STNE", "experiment station", "a facility for carrying out experiments"));
		fc.add(new FeatureCode("STNF", "forest station", "a collection of buildings and facilities for carrying out forest management"));
		fc.add(new FeatureCode("STNI", "inspection station", "a station at which vehicles, goods, and people are inspected"));
		fc.add(new FeatureCode("STNM", "meteorological station", "a station at which weather elements are recorded"));
		fc.add(new FeatureCode("STNR", "radio station", "a facility for producing and transmitting information by radio waves"));
		fc.add(new FeatureCode("STNS", "satellite station", "a facility for tracking and communicating with orbiting satellites"));
		fc.add(new FeatureCode("STNW", "whaling station", "a facility for butchering whales and processing train oil"));
		fc.add(new FeatureCode("STPS", "steps", "stones or slabs placed for ease in ascending or descending a steep slope"));
		fc.add(new FeatureCode("THTR", "theater", "A building, room, or outdoor structure for the presentation of plays, films, or other dramatic performances"));
		fc.add(new FeatureCode("TMB", "tomb(s)", "a structure for interring bodies"));
		fc.add(new FeatureCode("TMPL", "temple(s)", "an edifice dedicated to religious worship"));
		fc.add(new FeatureCode("TNKD", "cattle dipping tank", "a small artificial pond used for immersing cattle in chemically treated water for disease control"));
		fc.add(new FeatureCode("TOWR", "tower", "a high conspicuous structure, typically much higher than its diameter"));
		fc.add(new FeatureCode("TRIG", "triangulation station", "a point on the earth whose position has been determined by triangulation"));
		fc.add(new FeatureCode("TRMO", "oil pipeline terminal", "a tank farm or loading facility at the end of an oil pipeline"));
		fc.add(new FeatureCode("TWO", "Temp Work Office", "Temporary Work Offices"));
		fc.add(new FeatureCode("UNIO", "postgrad & MBA", "Post Universitary Education Institutes (post graduate studies and highly specialised master programs) & MBA"));
		fc.add(new FeatureCode("UNIP", "University Prep School", "University Preparation Schools & Institutions"));
		fc
				.add(new FeatureCode(
						"UNIV",
						"university",
						"An institution for higher learning with teaching and research facilities constituting a graduate school and professional schools that award master's degrees and doctorates and an undergraduate division that awards bachelor's degrees."));
		fc.add(new FeatureCode("USGE", "united states government establishment", "a facility operated by the United States Government in Panama"));
		fc.add(new FeatureCode("VETF", "veterinary facility", "a building or camp at which veterinary services are available"));
		fc.add(new FeatureCode("WALL", "wall", "a thick masonry structure, usually enclosing a field or building, or forming the side of a structure"));
		fc.add(new FeatureCode("WALLA", "ancient wall", "the remains of a linear defensive stone structure"));
		fc.add(new FeatureCode("WEIR", "weir(s)", "a small dam in a stream, designed to raise the water level or to divert stream flow through a desired channel"));
		fc.add(new FeatureCode("WHRF", "wharf(-ves)", "a structure of open rather than solid construction along a shore or a bank which provides berthing for ships and cargo-handling facilities"));
		fc.add(new FeatureCode("WRCK", "wreck", "the site of the remains of a wrecked vessel"));
		fc.add(new FeatureCode("WTRW", "waterworks", "a facility for supplying potable water through a water source and a system of pumps and filtration beds"));
		fc.add(new FeatureCode("ZNF", "free trade zone", "an area, usually a section of a port, where goods may be received and shipped free of customs duty and of most customs regulations"));
		fc.add(new FeatureCode("ZOO", "zoo", "a zoological garden or park where wild animals are kept for exhibition"));
		add(fc);

		fc = new FeatureClass("T", "mountain, hill, rock", Font.decode("Arial-PLAIN-10"), Color.orange);
		fc.add(new FeatureCode("ASPH", "asphalt lake", "a small basin containing naturally occurring asphalt"));
		fc.add(new FeatureCode("ATOL", "atoll(s)", "a ring-shaped coral reef which has closely spaced islands on it encircling a lagoon"));
		fc.add(new FeatureCode("BAR", "bar",
				"a shallow ridge or mound of coarse unconsolidated material in a stream channel, at the mouth of a stream, estuary, or lagoon and in the wave-break zone along coasts"));
		fc.add(new FeatureCode("BCH", "beach", "a shore zone of coarse unconsolidated sediment that extends from the low-water line to the highest reach of storm waves"));
		fc.add(new FeatureCode("BCHS", "beaches", "a shore zone of coarse unconsolidated sediment that extends from the low-water line to the highest reach of storm waves"));
		fc.add(new FeatureCode("BDLD", "badlands", "an area characterized by a maze of very closely spaced, deep, narrow, steep-sided ravines, and sharp crests and pinnacles"));
		fc.add(new FeatureCode("BLDR", "boulder field", "a high altitude or high latitude bare, flat area covered with large angular rocks"));
		fc.add(new FeatureCode("BLHL", "blowhole(s)", "a hole in coastal rock through which sea water is forced by a rising tide or waves and spurted through an outlet into the air"));
		fc.add(new FeatureCode("BLOW", "blowout(s)", "a small depression in sandy terrain, caused by wind erosion"));
		fc.add(new FeatureCode("BNCH", "bench", "a long, narrow bedrock platform bounded by steeper slopes above and below, usually overlooking a waterbody"));
		fc.add(new FeatureCode("BUTE", "butte(s)", "a small, isolated, usually flat-topped hill with steep sides"));
		fc.add(new FeatureCode("CAPE", "cape", "a land area, more prominent than a point, projecting into the sea and marking a notable change in coastal direction"));
		fc.add(new FeatureCode("CFT", "cleft(s)", "a deep narrow slot, notch, or groove in a coastal cliff"));
		fc.add(new FeatureCode("CLDA", "caldera", "a depression measuring kilometers across formed by the collapse of a volcanic mountain"));
		fc.add(new FeatureCode("CLF", "cliff(s)", "a high, steep to perpendicular slope overlooking a waterbody or lower area"));
		fc.add(new FeatureCode("CNYN", "canyon", "a deep, narrow valley with steep sides cutting into a plateau or mountainous area"));
		fc.add(new FeatureCode("CONE", "cone(s)", "a conical landform composed of mud or volcanic material"));
		fc.add(new FeatureCode("CRDR", "corridor", "a strip or area of land having significance as an access way"));
		fc.add(new FeatureCode("CRQ", "cirque", "a bowl-like hollow partially surrounded by cliffs or steep slopes at the head of a glaciated valley"));
		fc.add(new FeatureCode("CRQS", "cirques", "bowl-like hollows partially surrounded by cliffs or steep slopes at the head of a glaciated valley"));
		fc.add(new FeatureCode("CRTR", "crater(s)", "a generally circular saucer or bowl-shaped depression caused by volcanic or meteorite explosive action"));
		fc.add(new FeatureCode("CUET", "cuesta(s)", "an asymmetric ridge formed on tilted strata"));
		fc.add(new FeatureCode("DLTA", "delta", "a flat plain formed by alluvial deposits at the mouth of a stream"));
		fc.add(new FeatureCode("DPR", "depression(s)", "a low area surrounded by higher land and usually characterized by interior drainage"));
		fc.add(new FeatureCode("DSRT", "desert", "a large area with little or no vegetation due to extreme environmental conditions"));
		fc.add(new FeatureCode("DUNE", "dune(s)", "a wave form, ridge or star shape feature composed of sand"));
		fc.add(new FeatureCode("DVD", "divide", "a line separating adjacent drainage basins"));
		fc.add(new FeatureCode("ERG", "sandy desert", "an extensive tract of shifting sand and sand dunes"));
		fc.add(new FeatureCode("FAN", "fan(s)",
				"a fan-shaped wedge of coarse alluvium with apex merging with a mountain stream bed and the fan spreading out at a low angle slope onto an adjacent plain"));
		fc.add(new FeatureCode("FORD", "ford", "a shallow part of a stream which can be crossed on foot or by land vehicle"));
		fc.add(new FeatureCode("FSR", "fissure", "a crack associated with volcanism"));
		fc.add(new FeatureCode("GAP", "gap", "a low place in a ridge, not used for transportation"));
		fc.add(new FeatureCode("GRGE", "gorge(s)", "a short, narrow, steep-sided section of a stream valley"));
		fc.add(new FeatureCode("HDLD", "headland", "a high projection of land extending into a large body of water beyond the line of the coast"));
		fc.add(new FeatureCode("HLL", "hill", "a rounded elevation of limited extent rising above the surrounding land with local relief of less than 300m"));
		fc.add(new FeatureCode("HLLS", "hills", "rounded elevations of limited extent rising above the surrounding land with local relief of less than 300m"));
		fc.add(new FeatureCode("HMCK", "hammock(s)", "a patch of ground, distinct from and slightly above the surrounding plain or wetland. Often occurs in groups"));
		fc.add(new FeatureCode("HMDA", "rock desert", "a relatively sand-free, high bedrock plateau in a hot desert, with or without a gravel veneer"));
		fc.add(new FeatureCode("INTF", "interfluve", "a relatively undissected upland between adjacent stream valleys"));
		fc.add(new FeatureCode("ISL", "island", "a tract of land, smaller than a continent, surrounded by water at high water"));
		fc.add(new FeatureCode("ISLF", "artificial island", "an island created by landfill or diking and filling in a wetland, bay, or lagoon"));
		fc.add(new FeatureCode("ISLM", "mangrove island", "a mangrove swamp surrounded by a waterbody"));
		fc.add(new FeatureCode("ISLS", "islands", "tracts of land, smaller than a continent, surrounded by water at high water"));
		fc.add(new FeatureCode("ISLT", "land-tied island", "a coastal island connected to the mainland by barrier beaches, levees or dikes"));
		fc.add(new FeatureCode("ISLX", "section of island", ""));
		fc.add(new FeatureCode("ISTH", "isthmus", "a narrow strip of land connecting two larger land masses and bordered by water"));
		fc.add(new FeatureCode("KRST", "karst area",
				"a distinctive landscape developed on soluble rock such as limestone characterized by sinkholes, caves, disappearing streams, and underground drainage"));
		fc.add(new FeatureCode("LAVA", "lava area", "an area of solidified lava"));
		fc.add(new FeatureCode("LEV", "levee", "a natural low embankment bordering a distributary or meandering stream; often built up artificially to control floods"));
		fc.add(new FeatureCode("MESA", "mesa(s)", "a flat-topped, isolated elevation with steep slopes on all sides, less extensive than a plateau"));
		fc.add(new FeatureCode("MND", "mound(s)", "a low, isolated, rounded hill"));
		fc.add(new FeatureCode("MRN", "moraine", "a mound, ridge, or other accumulation of glacial till"));
		fc.add(new FeatureCode("MT", "mountain", "an elevation standing high above the surrounding area with small summit area, steep slopes and local relief of 300m or more"));
		fc.add(new FeatureCode("MTS", "mountains", "a mountain range or a group of mountains or high ridges"));
		fc.add(new FeatureCode("NKM", "meander neck", "a narrow strip of land between the two limbs of a meander loop at its narrowest point"));
		fc.add(new FeatureCode("NTK", "nunatak", "a rock or mountain peak protruding through glacial ice"));
		fc.add(new FeatureCode("NTKS", "nunataks", "rocks or mountain peaks protruding through glacial ice"));
		fc.add(new FeatureCode("PAN", "pan", "a near-level shallow, natural depression or basin, usually containing an intermittent lake, pond, or pool"));
		fc.add(new FeatureCode("PANS", "pans", "a near-level shallow, natural depression or basin, usually containing an intermittent lake, pond, or pool"));
		fc.add(new FeatureCode("PASS", "pass", "a break in a mountain range or other high obstruction, used for transportation from one side to the other [See also gap]"));
		fc.add(new FeatureCode("PEN", "peninsula", "an elongate area of land projecting into a body of water and nearly surrounded by water"));
		fc.add(new FeatureCode("PENX", "section of peninsula", ""));
		fc.add(new FeatureCode("PK", "peak", "a pointed elevation atop a mountain, ridge, or other hypsographic feature"));
		fc.add(new FeatureCode("PKS", "peaks", "pointed elevations atop a mountain, ridge, or other hypsographic features"));
		fc.add(new FeatureCode("PLAT", "plateau", "an elevated plain with steep slopes on one or more sides, and often with incised streams"));
		fc.add(new FeatureCode("PLATX", "section of plateau", ""));
		fc.add(new FeatureCode("PLDR", "polder", "an area reclaimed from the sea by diking and draining"));
		fc.add(new FeatureCode("PLN", "plain(s)", "an extensive area of comparatively level to gently undulating land, lacking surface irregularities, and usually adjacent to a higher area"));
		fc.add(new FeatureCode("PLNX", "section of plain", ""));
		fc.add(new FeatureCode("PROM", "promontory(-ies)", "a bluff or prominent hill overlooking or projecting into a lowland"));
		fc.add(new FeatureCode("PT", "point", "a tapering piece of land projecting into a body of water, less prominent than a cape"));
		fc.add(new FeatureCode("PTS", "points", "tapering pieces of land projecting into a body of water, less prominent than a cape"));
		fc.add(new FeatureCode("RDGB", "beach ridge", "a ridge of sand just inland and parallel to the beach, usually in series"));
		fc.add(new FeatureCode("RDGE", "ridge(s)", "a long narrow elevation with steep sides, and a more or less continuous crest"));
		fc.add(new FeatureCode("REG", "stony desert", "a desert plain characterized by a surface veneer of gravel and stones"));
		fc.add(new FeatureCode("RK", "rock", "a conspicuous, isolated rocky mass"));
		fc.add(new FeatureCode("RKFL", "rockfall", "an irregular mass of fallen rock at the base of a cliff or steep slope"));
		fc.add(new FeatureCode("RKS", "rocks", "conspicuous, isolated rocky masses"));
		fc.add(new FeatureCode("SAND", "sand area", "a tract of land covered with sand"));
		fc.add(new FeatureCode("SBED", "dry stream bed", "a channel formerly containing the water of a stream"));
		fc.add(new FeatureCode("SCRP", "escarpment", "a long line of cliffs or steep slopes separating level surfaces above and below"));
		fc.add(new FeatureCode("SDL", "saddle", "a broad, open pass crossing a ridge or between hills or mountains"));
		fc.add(new FeatureCode("SHOR", "shore", "a narrow zone bordering a waterbody which covers and uncovers at high and low water, respectively"));
		fc.add(new FeatureCode("SINK", "sinkhole", "a small crater-shape depression in a karst area"));
		fc.add(new FeatureCode("SLID", "slide", "a mound of earth material, at the base of a slope and the associated scoured area"));
		fc.add(new FeatureCode("SLP", "slope(s)", "a surface with a relatively uniform slope angle"));
		fc.add(new FeatureCode("SPIT", "spit", "a narrow, straight or curved continuation of a beach into a waterbody"));
		fc.add(new FeatureCode("SPUR", "spur(s)", "a subordinate ridge projecting outward from a hill, mountain or other elevation"));
		fc.add(new FeatureCode("TAL", "talus slope", "a steep concave slope formed by an accumulation of loose rock fragments at the base of a cliff or steep slope"));
		fc.add(new FeatureCode("TRGD", "interdune trough(s)", "a long wind-swept trough between parallel longitudinal dunes"));
		fc.add(new FeatureCode("TRR", "terrace", "a long, narrow alluvial platform bounded by steeper slopes above and below, usually overlooking a waterbody"));
		fc.add(new FeatureCode("UPLD", "upland", "an extensive interior region of high land with low to moderate surface relief"));
		fc.add(new FeatureCode("VAL", "valley", "an elongated depression usually traversed by a stream"));
		fc.add(new FeatureCode("VALG", "hanging valley", "a valley the floor of which is notably higher than the valley or shore to which it leads; most common in areas that have been glaciated"));
		fc.add(new FeatureCode("VALS", "valleys", "elongated depressions usually traversed by a stream"));
		fc.add(new FeatureCode("VALX", "section of valley", ""));
		fc.add(new FeatureCode("VLC", "volcano", "a conical elevation composed of volcanic materials with a crater at the top"));
		add(fc);

		fc = new FeatureClass("U", "undersea", Font.decode("Arial-PLAIN-10"), Color.blue);
		fc.add(new FeatureCode("APNU", "apron", "a gentle slope, with a generally smooth surface, particularly found around groups of islands and seamounts"));
		fc.add(new FeatureCode("ARCU", "arch", "a low bulge around the southeastern end of the island of Hawaii"));
		fc.add(new FeatureCode("ARRU", "arrugado", "an area of subdued corrugations off Baja California"));
		fc.add(new FeatureCode("BDLU", "borderland",
				"a region adjacent to a continent, normally occupied by or bordering a shelf, that is highly irregular with depths well in excess of those typical of a shelf"));
		fc.add(new FeatureCode("BKSU", "banks", "elevations, typically located on a shelf, over which the depth of water is relatively shallow but sufficient for safe surface navigation"));
		fc.add(new FeatureCode("BNCU", "bench", "a small terrace"));
		fc.add(new FeatureCode("BNKU", "bank", "an elevation, typically located on a shelf, over which the depth of water is relatively shallow but sufficient for safe surface navigation"));
		fc.add(new FeatureCode("BSNU", "basin", "a depression more or less equidimensional in plan and of variable extent"));
		fc.add(new FeatureCode("CDAU", "cordillera", "an entire mountain system including the subordinate ranges, interior plateaus, and basins"));
		fc.add(new FeatureCode("CNSU", "canyons", "relatively narrow, deep depressions with steep sides, the bottom of which generally has a continuous slope"));
		fc.add(new FeatureCode("CNYU", "canyon", "a relatively narrow, deep depression with steep sides, the bottom of which generally has a continuous slope"));
		fc.add(new FeatureCode("CRSU", "continental rise", "a gentle slope rising from oceanic depths towards the foot of a continental slope"));
		fc.add(new FeatureCode("DEPU", "deep", "a localized deep area within the confines of a larger feature, such as a trough, basin or trench"));
		fc.add(new FeatureCode("EDGU", "shelf edge", "a line along which there is a marked increase of slope at the outer margin of a continental shelf or island shelf"));
		fc.add(new FeatureCode("ESCU", "escarpment (or scarp)", "an elongated and comparatively steep slope separating flat or gently sloping areas"));
		fc.add(new FeatureCode("FANU", "fan", "a relatively smooth feature normally sloping away from the lower termination of a canyon or canyon system"));
		fc.add(new FeatureCode("FLTU", "flat", "a small level or nearly level area"));
		fc.add(new FeatureCode("FRKU", "fork", "a branch of a canyon or valley"));
		fc.add(new FeatureCode("FRSU", "forks", "a branch of a canyon or valley"));
		fc.add(new FeatureCode("FRZU", "fracture zone",
				"an extensive linear zone of irregular topography of the sea floor, characterized by steep-sided or asymmetrical ridges, troughs, or escarpments"));
		fc.add(new FeatureCode("FURU", "furrow", "a closed, linear, narrow, shallow depression"));
		fc.add(new FeatureCode("GAPU", "gap", "a narrow break in a ridge or rise"));
		fc.add(new FeatureCode("GLYU", "gully", "a small valley-like feature"));
		fc.add(new FeatureCode("HLLU", "hill", "an elevation rising generally less than 500 meters"));
		fc.add(new FeatureCode("HLSU", "hills", "elevations rising generally less than 500 meters"));
		fc.add(new FeatureCode("HOLU", "hole", "a small depression of the sea floor"));
		fc.add(new FeatureCode("KNLU", "knoll", "an elevation rising generally more than 500 meters and less than 1,000 meters and of limited extent across the summit"));
		fc.add(new FeatureCode("KNSU", "knolls", "elevations rising generally more than 500 meters and less than 1,000 meters and of limited extent across the summits"));
		fc.add(new FeatureCode("LDGU", "ledge", "a rocky projection or outcrop, commonly linear and near shore"));
		fc.add(new FeatureCode("LEVU", "levee", "an embankment bordering a canyon, valley, or seachannel"));
		fc.add(new FeatureCode("MDVU", "median valley", "the axial depression of the mid-oceanic ridge system"));
		fc.add(new FeatureCode("MESU", "mesa", "an isolated, extensive, flat-topped elevation on the shelf, with relatively steep sides"));
		fc.add(new FeatureCode("MNDU", "mound", "a low, isolated, rounded hill"));
		fc.add(new FeatureCode("MOTU", "moat", "an annular depression that may not be continuous, located at the base of many seamounts, islands, and other isolated elevations"));
		fc.add(new FeatureCode("MTSU", "mountains", "well-delineated subdivisions of a large and complex positive feature"));
		fc.add(new FeatureCode("MTU", "mountain", "a well-delineated subdivision of a large and complex positive feature"));
		fc.add(new FeatureCode("PKSU", "peaks", "prominent elevations, part of a larger feature, either pointed or of very limited extent across the summit"));
		fc.add(new FeatureCode("PKU", "peak", "a prominent elevation, part of a larger feature, either pointed or of very limited extent across the summit"));
		fc.add(new FeatureCode("PLFU", "platform", "a flat or gently sloping underwater surface extending seaward from the shore"));
		fc.add(new FeatureCode("PLNU", "plain", "a flat, gently sloping or nearly level region"));
		fc.add(new FeatureCode("PLTU", "plateau", "a comparatively flat-topped feature of considerable extent, dropping off abruptly on one or more sides"));
		fc.add(new FeatureCode("PNLU", "pinnacle", "a high tower or spire-shaped pillar of rock or coral, alone or cresting a summit"));
		fc.add(new FeatureCode("PRVU", "province", "a region identifiable by a group of similar physiographic features whose characteristics are markedly in contrast with surrounding areas"));
		fc.add(new FeatureCode("RAVU", "ravine", "a small canyon"));
		fc.add(new FeatureCode("RDGU", "ridge", "a long narrow elevation with steep sides"));
		fc.add(new FeatureCode("RDSU", "ridges", "long narrow elevations with steep sides"));
		fc.add(new FeatureCode("RFSU", "reefs", "surface-navigation hazards composed of consolidated material"));
		fc.add(new FeatureCode("RFU", "reef", "a surface-navigation hazard composed of consolidated material"));
		fc.add(new FeatureCode("RISU", "rise", "a broad elevation that rises gently, and generally smoothly, from the sea floor"));
		fc.add(new FeatureCode("RMPU", "ramp", "a gentle slope connecting areas of different elevations"));
		fc.add(new FeatureCode("RNGU", "range", "a series of associated ridges or seamounts"));
		fc.add(new FeatureCode("SCNU", "seachannel", "a continuously sloping, elongated depression commonly found in fans or plains and customarily bordered by levees on one or two sides"));
		fc.add(new FeatureCode("SCSU", "seachannels", "continuously sloping, elongated depressions commonly found in fans or plains and customarily bordered by levees on one or two sides"));
		fc.add(new FeatureCode("SDLU", "saddle", "a low part, resembling in shape a saddle, in a ridge or between contiguous seamounts"));
		fc.add(new FeatureCode("SHFU", "shelf",
				"a zone adjacent to a continent (or around an island) that extends from the low water line to a depth at which there is usually a marked increase of slope towards oceanic depths"));
		fc.add(new FeatureCode("SHLU", "shoal", "a surface-navigation hazard composed of unconsolidated material"));
		fc.add(new FeatureCode("SHSU", "shoals", "hazards to surface navigation composed of unconsolidated material"));
		fc.add(new FeatureCode("SHVU", "shelf valley", "a valley on the shelf, generally the shoreward extension of a canyon"));
		fc.add(new FeatureCode("SILU", "sill", "the low part of a gap or saddle separating basins"));
		fc.add(new FeatureCode("SLPU", "slope", "the slope seaward from the shelf edge to the beginning of a continental rise or the point where there is a general reduction in slope"));
		fc.add(new FeatureCode("SMSU", "seamounts", "elevations rising generally more than 1,000 meters and of limited extent across the summit"));
		fc.add(new FeatureCode("SMU", "seamount", "an elevation rising generally more than 1,000 meters and of limited extent across the summit"));
		fc.add(new FeatureCode("SPRU", "spur", "a subordinate elevation, ridge, or rise projecting outward from a larger feature"));
		fc
				.add(new FeatureCode("TERU", "terrace",
						"a relatively flat horizontal or gently inclined surface, sometimes long and narrow, which is bounded by a steeper ascending slope on one side and by a steep descending slope on the opposite side"));
		fc.add(new FeatureCode("TMSU", "tablemounts (or guyots)", "seamounts having a comparatively smooth, flat top"));
		fc.add(new FeatureCode("TMTU", "tablemount (or guyot)", "a seamount having a comparatively smooth, flat top"));
		fc.add(new FeatureCode("TNGU", "tongue", "an elongate (tongue-like) extension of a flat sea floor into an adjacent higher feature"));
		fc.add(new FeatureCode("TRGU", "trough", "a long depression of the sea floor characteristically flat bottomed and steep sided, and normally shallower than a trench"));
		fc.add(new FeatureCode("TRNU", "trench", "a long, narrow, characteristically very deep and asymmetrical depression of the sea floor, with relatively steep sides"));
		fc.add(new FeatureCode("VALU", "valley", "a relatively shallow, wide depression, the bottom of which usually has a continuous gradient"));
		fc.add(new FeatureCode("VLSU", "valleys", "a relatively shallow, wide depression, the bottom of which usually has a continuous gradient"));
		add(fc);

		fc = new FeatureClass("V", "forest, heath", Font.decode("Arial-PLAIN-10"), new Color(0, 128, 0));
		fc.add(new FeatureCode("BUSH", "bush(es)", "a small clump of conspicuous bushes in an otherwise bare area"));
		fc.add(new FeatureCode("CULT", "cultivated area", "an area under cultivation"));
		fc.add(new FeatureCode("FRST", "forest(s)", "an area dominated by tree vegetation"));
		fc.add(new FeatureCode("FRSTF", "fossilized forest", "a forest fossilized by geologic processes and now exposed at the earth's surface"));
		fc.add(new FeatureCode("GRSLD", "grassland", "an area dominated by grass vegetation"));
		fc.add(new FeatureCode("GRVC", "coconut grove", "a planting of coconut trees"));
		fc.add(new FeatureCode("GRVO", "olive grove", "a planting of olive trees"));
		fc.add(new FeatureCode("GRVP", "palm grove", "a planting of palm trees"));
		fc.add(new FeatureCode("GRVPN", "pine grove", "a planting of pine trees"));
		fc.add(new FeatureCode("HTH", "heath", "an upland moor or sandy area dominated by low shrubby vegetation including heather"));
		fc.add(new FeatureCode("MDW", "meadow", "a small, poorly drained area dominated by grassy vegetation"));
		fc.add(new FeatureCode("OCH", "orchard(s)", "a planting of fruit or nut trees"));
		fc.add(new FeatureCode("SCRB", "scrubland", "an area of low trees, bushes, and shrubs stunted by some environmental limitation"));
		fc.add(new FeatureCode("TREE", "tree(s)", "a conspicuous tree used as a landmark"));
		fc.add(new FeatureCode("TUND", "tundra", "a marshy, treeless, high latitude plain, dominated by mosses, lichens, and low shrub vegetation under permafrost conditions"));
		fc.add(new FeatureCode("VIN", "vineyard", "a planting of grapevines"));
		fc.add(new FeatureCode("VINS", "vineyards", "plantings of grapevines"));
		fc.add(new FeatureCode("ll", "not available"));
		add(fc);
	}
}
