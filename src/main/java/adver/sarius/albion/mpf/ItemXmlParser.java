package adver.sarius.albion.mpf;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ItemXmlParser {

	// load file in constructor
	// have methods to load and return salvaging, enchanting, etc
	// if its too slow try to do it all in one run

	private Document xmlDoc;

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

			Document xmlDoc = db.parse(new File(filePath));

			// optional, but recommended
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			xmlDoc.getDocumentElement().normalize();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readArtifactSalvage(List<Item> itemPrices) {

//		System.out.println("root node: " + doc.getDocumentElement().getNodeName());
//		for (int i = 0; i < doc.getDocumentElement().getChildNodes().getLength(); i++) {
//			System.out.println(doc.getDocumentElement().getChildNodes().item(i).getNodeName());
//		}
	}

	public void readArtifactEquipmentSalvage(List<Item> itemPrices) {

	}

	public void readMaterialTransmute(List<Item> itemPrices) {

	}

	public void readEquipmentEnchanting(List<Item> itemPrices) {

	}
}