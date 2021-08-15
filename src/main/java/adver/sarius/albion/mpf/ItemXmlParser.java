package adver.sarius.albion.mpf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ItemXmlParser {
	private Document xmlDoc;

	//
	public ItemXmlParser(String filePath) {
		// xml parsing from:
		// https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// optional, but recommended
			// process XML securely, avoid attacks like XML External Entities (XXE)
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			// parse XML file
			DocumentBuilder db = dbf.newDocumentBuilder();

			this.xmlDoc = db.parse(new File(filePath));

			// optional, but recommended
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			xmlDoc.getDocumentElement().normalize();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeInvalidItemQualities(List<Item> items) {
		for (int i = 0; i < xmlDoc.getChildNodes().item(0).getChildNodes().getLength(); i++) {
			Node node = xmlDoc.getChildNodes().item(0).getChildNodes().item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Node uniquenameAttribute = node.getAttributes().getNamedItem("uniquename");
				if (uniquenameAttribute != null) {
					String itemName = uniquenameAttribute.getNodeValue();
					Node maxqualitylevelAttribute = node.getAttributes().getNamedItem("maxqualitylevel");
					items.removeIf(item -> (item.getItemTypeId().equals(itemName)
							|| item.getItemTypeId().startsWith(itemName + "@")
							|| (itemName.contains("_JOURNAL_") && item.getItemTypeId().startsWith(itemName)))
							&& item.getQuality() > (maxqualitylevelAttribute == null ? 1
									: Integer.parseInt(maxqualitylevelAttribute.getNodeValue())));

					if (!items.stream()
							.anyMatch(item -> item.getItemTypeId().equals(itemName)
									|| item.getItemTypeId().startsWith(itemName + "@")
									|| (itemName.contains("_JOURNAL_") && item.getItemTypeId().startsWith(itemName)))) {
						Main.log("Found unknown entry in xml: " + uniquenameAttribute);
					}
				}
			}
		}
	}

	private double silverReturnFactor = 0.75;
	private double resourceReturnFactor = 0.25;

	public List<ProcessingItems> readArtifactSalvage(List<Item> itemPrices) {
		List<ProcessingItems> results = new ArrayList<>();

		NodeList nodes = xmlDoc.getElementsByTagName("simpleitem");

		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			// TODO: is shopcategory safe enough to identify all and only artifacts?
			Node shopcategory = node.getAttributes().getNamedItem("shopcategory");
			if (shopcategory != null && "artefacts".equals(shopcategory.getNodeValue())) {
				// TODO: null checks?
				String uniquename = node.getAttributes().getNamedItem("uniquename").getNodeValue();
				int itemvalue = Integer.parseInt(node.getAttributes().getNamedItem("itemvalue").getNodeValue());
				boolean salvageable = "true".equals(node.getAttributes().getNamedItem("salvageable").getNodeValue());
				// TODO: use two lists instead of one map, so I can add the same item multiple
				// times without manually checking and adding in map?
				Map<String, Double> itemsIn = new HashMap<>();
				itemsIn.put(uniquename, 1d);
				double silverIn = 0;
				Map<String, Double> itemsOut = new HashMap<>();
				double silverOut = itemvalue * silverReturnFactor;

				for (int j = 0; j < node.getChildNodes().getLength(); j++) {
					if ("craftingrequirements".equals(node.getChildNodes().item(j).getNodeName())) {
						NodeList children = node.getChildNodes().item(j).getChildNodes();
						for (int k = 0; k < children.getLength(); k++) {
							if ("craftresource".equals(children.item(k).getNodeName())) {
								// TODO: null checks?
								Node craftingItem = children.item(k).getAttributes().getNamedItem("uniquename");
								Node craftingCount = children.item(k).getAttributes().getNamedItem("count");
								itemsOut.put(craftingItem.getNodeValue(),
										Double.parseDouble(craftingCount.getNodeValue()) * resourceReturnFactor);
							}
						}
						results.addAll(getPriceCombinations(itemsIn, silverIn, itemsOut, silverOut, itemPrices));

					}
				}
			}
		}

		return results;
	}

	/**
	 * Returns a list with all combinations of given qualities and cities matching
	 * the item names from itemPrices.
	 * 
	 * @param itemsIn
	 * @param silverIn
	 * @param itemsOut
	 * @param silverOut
	 * @param itemPrices
	 * @return
	 */
	private List<ProcessingItems> getPriceCombinations(Map<String, Double> itemsIn, double silverIn,
			Map<String, Double> itemsOut, double silverOut, List<Item> itemPrices) {
		List<ProcessingItems> collected = new ArrayList<>();

		Map<String, List<Item>> itemVariations = new HashMap<>();
		List<String> itemNameList = new ArrayList<>();
		// schleife über input map
		for (String itemName : itemsIn.keySet()) {
			// pro entry alle items raussuchen
			itemVariations.put(itemName,
					itemPrices.stream().filter(i -> i.getItemTypeId().equals(itemName)).collect(Collectors.toList()));
			itemNameList.add(itemName);
		}
		// List von Map so wie in ProcessingItem zurückgeben
		// will eine Map<Item, Count> als Objekt haben. Also eine List davon als
		// rekursions ergebnis
		// rekursiv alle slots durchgehen, und darin über alle elemente iterieren
		List<Map<Item, Double>> inputCombinations = new ArrayList<>();
		recursivelyGetCombinations(inputCombinations, new HashMap<>(itemsIn.size()), itemNameList, itemVariations,
				itemsIn);
		// dann habe ich also alle input items durchkombiniert
		// gleiche für output machen.
		// TODO: use own method?
		itemVariations.clear();
		itemNameList.clear();
		for (String itemName : itemsOut.keySet()) {
			itemVariations.put(itemName,
					itemPrices.stream().filter(i -> i.getItemTypeId().equals(itemName)).collect(Collectors.toList()));
			itemNameList.add(itemName);
		}
		List<Map<Item, Double>> outputCombinations = new ArrayList<>();
		recursivelyGetCombinations(outputCombinations, new HashMap<>(itemsOut.size()), itemNameList, itemVariations,
				itemsOut);

		// dann mit double loop die beiden durchkombinieren
		for (Map<Item, Double> inputMap : inputCombinations) {
			for (Map<Item, Double> outputMap : outputCombinations) {
				collected.add(new ProcessingItems(inputMap, silverIn, outputMap, silverOut,
						ProcessingItems.OPERATION_SALVAGE_ARTIFACT));
			}
		}
		return collected;
	}

	/**
	 * One item can have different variations, like city and quality. If there are 3
	 * items, each with 3 variations, this method will produce a list with 27
	 * different entries. Each entry consists of a map, each map with 3 elements: 1
	 * variation of each of the 3 items.
	 * 
	 * 
	 * @param computedResultRef  List instance that will be filled by this method
	 *                           with all the computed results. Each item will have
	 *                           an amount assigned.
	 * @param currentMap         Map with already picked items and their count for
	 *                           the current combination. Empty map for first call
	 *                           would make sense.
	 * @param remainingItemSlots All the item names that still need to be picked.
	 * @param itemSlots          Map containing the relevant item names, paired with
	 *                           a list of all variations of that item.
	 * @param itemCounts         Mapping of item name to their count. Which will be
	 *                           used to fill the computedResultRef maps.
	 */
	private void recursivelyGetCombinations(List<Map<Item, Double>> computedResultRef, Map<Item, Double> currentMap,
			List<String> remainingItemSlots, Map<String, List<Item>> itemSlots, Map<String, Double> itemCounts) {
		if (remainingItemSlots.isEmpty()) {
			computedResultRef.add(currentMap);
			return;
		} else {
			String currentItemSlot = remainingItemSlots.remove(0);
			for (Item i : itemSlots.get(currentItemSlot)) {
				Map<Item, Double> clonedMap = new HashMap<>(currentMap);
				clonedMap.put(i, itemCounts.get(i.getItemTypeId()));
				recursivelyGetCombinations(computedResultRef, clonedMap, remainingItemSlots, itemSlots, itemCounts);
			}
			remainingItemSlots.add(0, currentItemSlot);
		}
		return;
	}

	public void readArtifactEquipmentSalvage(List<Item> itemPrices) {

	}

	public void readMaterialTransmute(List<Item> itemPrices) {

	}

	public void readEquipmentEnchanting(List<Item> itemPrices) {

	}
}