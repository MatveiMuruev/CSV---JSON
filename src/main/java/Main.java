import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.opencsv.CSVReader;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.juneau.json.annotation.Json;
import org.apache.juneau.serializer.SerializeException;
import org.apache.juneau.xml.XmlSerializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        List<Employee> staff = new ArrayList<>();
        staff.add(new Employee(1, "John", "Smith", "USA", 25));
        staff.add(new Employee(2, "Ivan", "Petrov", "RU", 23));
        writeCSV(staff, columnMapping);

        String fileName = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);

        String json = listToJson(list);


        writeString(json, "data.json");

        writeXML(toXML(staff));

        list = parseXML("data.xml");
        //System.out.println(list.toString());
        writeString(listToJson(list), "data2.json");

        json = readString("data2.json");

        list = jsonToList(json);
        for (Employee item : list) {
            System.out.println(item.toString());
        }
    }

    private static List<Employee> parseXML(String fileName) {

        List<Employee> employee = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(fileName);

            Node staff = document.getFirstChild();

            NodeList nodeList = staff.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                NodeList nodeEmployee = nodeList.item(i).getChildNodes();

                long id = 0;
                String firstName = "";
                String lastName = "";
                String country = "";
                int age = 0;

                for (int j = 0; j < nodeEmployee.getLength(); j++) {
                    if (nodeEmployee.item(i).getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    switch (nodeEmployee.item(j).getNodeName()) {
                        case "id": {
                            id = Integer.valueOf(nodeEmployee.item(j).getTextContent());
                            break;
                        }
                        case "firstName": {
                            firstName = nodeEmployee.item(j).getTextContent();
                            break;
                        }
                        case "lastName": {
                            lastName = nodeEmployee.item(j).getTextContent();
                            break;
                        }
                        case "country": {
                            country = nodeEmployee.item(j).getTextContent();
                            break;
                        }
                        case "age": {
                            age = Integer.valueOf(nodeEmployee.item(j).getTextContent());
                            break;
                        }
                    }

                }
                employee.add(new Employee(id, firstName, lastName, country, age));
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return employee;
    }

    public static List parseCSV(String[] columnMapping, String fileName) {
        List<Employee> list = new ArrayList();
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            list = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String listToJson(List<Employee> list) {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<ArrayList<Employee>>() {
        }.getType();
        return gson.toJson(list, listType);
    }

    public static void writeString(String json, String fileName) {
        try {
            try (FileWriter file = new FileWriter(fileName)) {
                file.write(json);
                file.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeCSV(List<Employee> a, String[] columnMapping) {

        ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(Employee.class);
        strategy.setColumnMapping(columnMapping);

        try (Writer writer = new FileWriter("data.csv")) {
            StatefulBeanToCsv<Employee> sbc = new StatefulBeanToCsvBuilder<Employee>(writer)
                    .withMappingStrategy(strategy)
                    .build();
            sbc.write(a);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            e.printStackTrace();
        }
    }

    public static void writeXML(String xml) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document = builder.parse(new InputSource(new StringReader(xml)));

            document = renameDocumentTag(document, "array", "staff");
            document = renameDocumentTag(document, "object", "employee");

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("data.xml"));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(domSource, streamResult);

        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public static String toXML(List<Employee> a) {
        String xml = "";
        try {
            XmlSerializer xmlSerializer = XmlSerializer.DEFAULT_NS_SQ_READABLE;
            xml = xmlSerializer.serialize(a);
        } catch (SerializeException e) {
            e.printStackTrace();
        }

        return xml;
    }

    public static Document renameDocumentTag(Document document, String fromTag, String toTag) {
        NodeList nodeList = document.getElementsByTagName(fromTag);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            document.renameNode(element, element.getNamespaceURI(), toTag);
        }
        return document;
    }

    private static String readString(String fileName) {
        String s = "";
        String line = "";

        try (BufferedReader reader = new BufferedReader((new FileReader(fileName)))) {
            while ((line = reader.readLine()) != null) {
                s += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    private static List<Employee> jsonToList(String json) {

        List<Employee> employeeList = new ArrayList<>();
        long id = 0;
        String firstName = "";
        String lastName = "";
        String country = "";
        long age = 0;

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(json);
            // JSONObject jsonObject = (JSONObject) obj;
            JSONArray employeers = (JSONArray) obj; // jsonObject.get("employee");
            for (Object item : employeers) {
                JSONObject employee = (JSONObject) item;
                id = (long) employee.get("id");
                firstName = (String) employee.get("firstName");
                lastName = (String) employee.get("lastName");
                country = (String) employee.get("country");
                age = (long) employee.get("age");
                employeeList.add(new Employee(id, firstName, lastName, country, (int) age));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return employeeList;
    }

}