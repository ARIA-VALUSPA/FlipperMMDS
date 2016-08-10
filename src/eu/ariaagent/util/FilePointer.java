package eu.ariaagent.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adg on 04/08/2016.
 *
 */
public class FilePointer {

    private static final String EMPTY_FML_START =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<!DOCTYPE fml-apml SYSTEM \"..\\..\\ARIA-ValuspaFMLTranslator\\fml-apml.dtd\">\n" +
            "<fml-apml>\n" +
            "\t<bml>\n" +
            "\t\t<speech id=\"s1\" language=\"english\" start=\"0.0\" text=\"\" voice=\"cereproc\">\n" +
            "\t\t\n";
    private static final String EMPTY_FML_END =
            "\n\t\t</speech>\n" +
            "\t</bml>\n" +
            "\t<fml>\n" +
            "\t</fml>\n" +
            "</fml-apml>";

    private String filePath;
    private String xmlContent;
    private String speechContent;

    private String curXmlContent;
    private String curSpeechContent;

    private String curHedge = null;

    public FilePointer(String filePath) {
        this.filePath = filePath;
        try {
            xmlContent = ReadFile(filePath, Charset.defaultCharset());
            readFile(true);
            prepareCurrent();
        } catch (Exception e) {
            xmlContent = "";
            speechContent = "";
            System.err.println("Error while reading file " + filePath);
            e.printStackTrace();
        }
    }

    public FilePointer(String filePath, String xmlContent, String speechContent) {
        this.filePath = filePath;
        this.xmlContent = xmlContent;
        this.speechContent = speechContent;
        prepareCurrent();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getSpeechContent() {
        return speechContent;
    }

    public String getXmlContent() {
        return xmlContent;
    }

    public String getCurXmlContent() {
        return curXmlContent;
    }

    public String getCurSpeechContent() {
        return curSpeechContent;
    }

    public void prepareCurrent() {
        curXmlContent = xmlContent;
        curSpeechContent = speechContent;
    }

    public void replaceAll(Pattern pattern, String replaceWith) {
        Matcher matcher1 = pattern.matcher(curXmlContent);
        curXmlContent = matcher1.replaceAll(replaceWith);
        Matcher matcher2 = pattern.matcher(curSpeechContent);
        curSpeechContent = matcher2.replaceAll(replaceWith);
    }

    public void setHedge(String hedge) {
        if (hedge == null || hedge.trim().isEmpty()) {
            return;
        }
        if (curHedge != null) {
            curSpeechContent = curSpeechContent.replace(curHedge, hedge);
            curXmlContent = curXmlContent.replace(curHedge, hedge);
            curHedge = hedge;
        } else {
            curSpeechContent = hedge + curSpeechContent;
            curHedge = hedge;

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                Document doc = builder.parse(new InputSource(new StringReader(curXmlContent)));
                doc.normalizeDocument();

                NodeList nodeList = doc.getElementsByTagName("speech");
                if (nodeList.getLength() > 0) {
                    Node speechNode = nodeList.item(0);
                    for (Node node = speechNode.getFirstChild(); node != null; node = node.getNextSibling()) {
                        if (node.getNodeName().equals("#text") && !node.getNodeValue().trim().isEmpty()) {
                            node.setNodeValue(hedge + node.getNodeValue());
                            break;
                        }
                    }
                    curXmlContent = NodeToString(doc);
                } else {
                    System.err.println("Could find speech node in modified file '" + filePath + "' \n " + curXmlContent);
                }
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }
        }
    }

    private void readFile(boolean retry) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        doc.normalizeDocument();

        NodeList nodeList = doc.getElementsByTagName("speech");
        if (nodeList.getLength() > 0) {
            speechContent = ExtractText(nodeList.item(0));
        } else {
            System.err.println("No speech tag detected in file '" + filePath + "'");
            speechContent = " oh ";
            xmlContent = EMPTY_FML_START + speechContent + EMPTY_FML_END;
            if (retry) {
                readFile(false);
            }
        }
    }

    @Override
    public String toString() {
        return "{ " + filePath + " ; " + speechContent + " ; " + xmlContent + " ; " + curSpeechContent + " ; " + curXmlContent + " }";
    }

    public static FilePointer CreateFromSpeech(String speech) {
        String xml = EMPTY_FML_START + speech + EMPTY_FML_END;
        return new FilePointer(speech, xml, speech);
    }

    public static String ReadFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String content = new String(encoded, encoding);
        int start = 0;
        while (content.charAt(start) != '<') {
            start++;
        }
        return content.substring(start);
    }

    public static String ExtractText(Node node) {
        StringBuilder sb = new StringBuilder();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child.getNodeName().equals("#text")) {
                String value = child.getNodeValue();
                String[] splits = value.split("\\s+");
                System.out.println(Arrays.toString(splits));
                for (String split : splits) {
                    split = split.trim();
                    if (!split.isEmpty()) {
                        sb.append(split).append(" ");
                    }
                }
            }
        }
        return sb.toString().trim();
    }

    public static String NodeToString(Node node) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
            te.printStackTrace();
        }
        return "";
    }
}
