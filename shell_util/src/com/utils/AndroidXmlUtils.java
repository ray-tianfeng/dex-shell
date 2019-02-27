package com.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;

/**
 * Created by zoulong on 2019/2/26 0026.
 */
public class AndroidXmlUtils {
    private static final String NS = "http://schemas.android.com/apk/res/android";
    public static String readApplicationName(String manifestPath) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(manifestPath);
        Element element = document.getDocumentElement();
        Element applicationElement = (Element)element.getElementsByTagName("application").item(0);
        if(applicationElement.hasAttributeNS(NS,"name")){
            return applicationElement.getAttributeNodeNS(NS,"name").getValue();
        }else{
            return null;
        }
    }

    public static void changeApplicationName(String manifestPath, String newName) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(manifestPath);
        Element element = document.getDocumentElement();
        Element applicationElement = (Element)element.getElementsByTagName("application").item(0);
        if(applicationElement.hasAttribute("name")){
            applicationElement.getAttributeNodeNS(NS,"name").setValue(newName);
        }else{
            applicationElement.setAttribute("android:name",newName);
        }

        writeXmlToLocal(document,manifestPath);
    }


    public static void addMateData(String manifestPath,String name,String value) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(manifestPath);
        Element element = document.getDocumentElement();
        Element applicationElement = (Element)element.getElementsByTagName("application").item(0);
        Element mateDataElement = document.createElement("meta-data");
        mateDataElement.setAttribute("android:name",name);
        mateDataElement.setAttribute("android:value",value);
        applicationElement.appendChild(mateDataElement);
        writeXmlToLocal(document,manifestPath);
    }

    private static void writeXmlToLocal(Document document, String outXmlPath) throws TransformerException {
        TransformerFactory tff = TransformerFactory.newInstance();
        Transformer tf = tff.newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT,"yes");
        tf.transform(new DOMSource(document),new StreamResult(outXmlPath));
    }
}
