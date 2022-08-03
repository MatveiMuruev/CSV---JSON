import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

//        List<Employee> staff = new ArrayList<>();
//        staff.add(new Employee(1, "John", "Smith", "USA", 25));
//        staff.add(new Employee(2, "Ivan", "Petrov", "RU", 23));
//        writeCSV(staff, columnMapping);

        String fileName = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);

        String json = listToJson(list);

        writeString(json);

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

    public static void writeString(String json) {
        try {
            try (FileWriter file = new FileWriter("data.json")) {
                file.write(json);
                file.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void writeCSV(List<Employee> a, String[] columnMapping) {
//
//        ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
//        strategy.setType(Employee.class);
//        strategy.setColumnMapping(columnMapping);
//
//        try (Writer writer = new FileWriter("data.csv")) {
//            StatefulBeanToCsv<Employee> sbc = new StatefulBeanToCsvBuilder<Employee>(writer)
//                    .withMappingStrategy(strategy)
//                    .build();
//            sbc.write(a);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
//            e.printStackTrace();
//        }
//    }
}