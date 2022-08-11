import org.hamcrest.collection.IsEmptyCollection;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class MainTest {

    @Test
    public void testFileCsv() {
        String location = "data.csv";
        Path path = Paths.get(location);
        File file = new File(location);
        Assert.assertTrue(Files.exists(path));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv")
    void testParseCSV(long id, String firstName, String lastName, String country, int age) {
        Employee employee = new Employee(id, firstName, lastName, country, age);

        String expected = employee.toString();

        String employeeActual = "Employee{id=" + employee.getId() + ", firstName='" + employee.getFirstName() + "', lastName='" + employee.getLastName() + "', country='" + employee.getCountry() + "', age=" + employee.getAge() + "}";

        assertEquals(expected, employeeActual);
    }

    @Test
    public void testFileJson() {
        String location = "data.json";
        Path path = Paths.get(location);
        File file = new File(location);
        Assert.assertTrue(Files.exists(path));
    }

    @Test
    public void testJson() {
        String json = Main.readString("data.json");
        Assert.assertTrue(isValid(json));
    }


    public boolean isValid(String json) {
        try {
            JSONParser parser = new JSONParser();
            parser.parse(json);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

//    ________HAMCRECT___________

    @Test
    public void hamcrestTestJson() {
        String containsJson = "\"firstName\": \"John\"";
        assertThat(Main.readString("data.json"), anyOf(containsString(containsJson)));
    }

    String[] files = {"data.csv", "data.json", "data.xml", "data2.json"};

    @Test
    public void hamcrestTestAllFile() {
        String otpaph = "CSV-Parse\\";
        Path path = Path.of(otpaph).toAbsolutePath();
        List<Path> listFiles = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(path.getParent(), 1)) {
            listFiles = walk.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String item : files) {
            String jsonFile = path.getParent() + "\\" + item;
            Path expected = Paths.get(jsonFile);
            assertThat(listFiles, hasItems(expected));
        }
    }

    @Test
    public void hamcrestTestListEmpty() {
        List<Employee> list = Main.parseXML("data.xml");
        assertThat(list, is(not(empty())));
    }

    @Test
    public void hamcrestTestListSyze() {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        List<Employee> list = Main.parseCSV(columnMapping, "data.csv");
        assertThat(list, hasSize(2));
    }

    @Test
    public void hamcrestTest_ValidClass() {
        assertThat(Main.parseXML("data.xml"), instanceOf(List.class));
    }

    @Test
    public void hamcrestTest_Json_Contains_EmployeeObject() {
        List<Employee> list = Main.jsonToList(Main.readString("data2.json"));
        for (Employee item : list) {
            assertThat(item, instanceOf(Employee.class));
        }
    }
}