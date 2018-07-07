package defaultpackage;

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;

public class Main {

    public static String targetElementId = "make-everything-ok-button";

    public static String determinePath(Node node, int removeLevels){
        //below parameter determines how many levels will be removed from the top of the path

        for (int i = removeLevels; i > 0; i--) {
            node = node.getParentNode();
        }

        String XMLPath = node.getNodeName();
        node = node.getParentNode();

        while (node != null) {

            int position = 1;
            Node previousSibling = node.getPreviousSibling();
            while (previousSibling != null) {
                if (previousSibling.getNodeName().equals(node.getNodeName())) {
                    position++;
                }
                previousSibling = previousSibling.getPreviousSibling();
            }

            XMLPath = node.getNodeName() + "[" + position + "]" + "/" + XMLPath;
            node = node.getParentNode();

            if (node.getNodeName().equals("html")) {
                XMLPath = "html/" + XMLPath;
                break;
            }
        }

        return XMLPath;
    }

    public static void main(String[] args) {

        try {
            //application args
            String originFilePath = args[0];
            String diffFilePath = args[1];

            File originFile = new File(originFilePath);
            File diffFile = new File(diffFilePath);

            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document originDocument = documentBuilder.parse(originFile);
            Document diffDocument = documentBuilder.parse(diffFile);
            originDocument.getDocumentElement().normalize();
            diffDocument.getDocumentElement().normalize();

            XPath xPath = XPathFactory.newInstance().newXPath();

            //find origin file button

            String originFileExpression = "//a[@id = '" + targetElementId + "']";
            Node originNode = (Node) xPath.compile(originFileExpression).evaluate(originDocument, XPathConstants.NODE);

            //determine the XML path from the origin file to look for the button in the diff file
            //below parameter determines how many levels will be removed from the top of the path
            int removeLevels = 2;

            String originXMLPath = determinePath(originNode, removeLevels);

            String originNodeName = originNode.getNodeName();

            System.out.println("The path to start search. " + removeLevels +
                    " levels has been removed from the end: " + originXMLPath);
            System.out.println("Origin node type is <" + originNodeName + ">");

            NodeList diffNodeSet = (NodeList) xPath.compile(originXMLPath + "//"
                    + originNodeName).evaluate(diffDocument, XPathConstants.NODESET);

            //analyze the text to determine if its what we looking fo
            System.out.println("Analyzing text containing in selected <" + originNodeName + "> elements in diff file");
            ArrayList<Node> selectedDiffNodes = new ArrayList<>();

            for (int i = 0; i < diffNodeSet.getLength(); i++) {
                String buttonText = diffNodeSet.item(i).getTextContent().toLowerCase();
                if (buttonText.contains("fine")
                        | buttonText.contains("ok")
                        | buttonText.contains("good")
                        | buttonText.contains("excellent")
                        | buttonText.contains("great")
                        | buttonText.contains("super")
                        | buttonText.contains("perfect")) {
                    if (! buttonText.contains("break")
                            | buttonText.contains("bad")
                            | buttonText.contains("not")
                            | buttonText.contains("worse")
                            | buttonText.contains("dont")
                            | buttonText.contains("don't")) {
                        selectedDiffNodes.add(diffNodeSet.item(i));
                    }
                }
            }

            if (selectedDiffNodes.size() == 1){
                //determine the XML path from the diff file

                String diffXMLPath = determinePath(selectedDiffNodes.get(0), 0);
                System.out.println("Path to element in diff-case HTML: \n" +
                diffXMLPath);

            } else {
                System.out.println("Error. Application found zero or more that one possible buttons");
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }
}
