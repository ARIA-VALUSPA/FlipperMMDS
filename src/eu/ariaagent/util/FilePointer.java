package eu.ariaagent.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adg on 04/08/2016.
 *
 */
public class FilePointer {

    private String filePath;
    private String xmlContent;
    private String speechContent;

    private String curXmlContent;
    private String curSpeechContent;

    public FilePointer(String filePath) {
        this.filePath = filePath;
        try {
            xmlContent = ReadFile(filePath, Charset.defaultCharset());
            ReadFile();
        } catch (Exception e) {
            xmlContent = "";
            speechContent = "";
            e.printStackTrace();
        }
    }

    public FilePointer(String filePath, String xmlContent, String speechContent) {
        this.filePath = filePath;
        this.xmlContent = xmlContent;
        this.speechContent = speechContent;
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

    public void addHedge(String hedge) {
        curSpeechContent = hedge + curSpeechContent;
        // TODO: add hedge to xml!!!
    }

    private void ReadFile() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
        doc.normalizeDocument();

        NodeList nodeList = doc.getElementsByTagName("speech");
        if (nodeList.getLength() > 0) {
            speechContent = ExtractText(nodeList.item(0));
        } else {
            speechContent = "";
        }
    }

    public static FilePointer CreateFromSpeech(String speech) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<!DOCTYPE fml-apml SYSTEM \"..\\..\\ARIA-ValuspaFMLTranslator\\fml-apml.dtd\">\n" +
                "<fml-apml>\n" +
                "\t<bml>\n" +
                "\t\t<speech id=\"s1\" language=\"english\" start=\"0.0\" text=\"\" voice=\"cereproc\">\n" +
                "\t\t\n" + speech +
                "\t\t</speech>\n" +
                "\t</bml>\n" +
                "\t<fml>\n" +
                "\t</fml>\n" +
                "</fml-apml>";
        return new FilePointer("", xml, speech);
    }

    public static String ReadFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String ExtractText(Node node) {
        StringBuilder sb = new StringBuilder();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child.getNodeName().equals("#text")) {
                sb.append(child.getNodeValue());
            }
        }
        return sb.toString().trim();
    }

}
