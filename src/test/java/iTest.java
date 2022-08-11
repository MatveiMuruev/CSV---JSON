import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

public class iTest {
    @ParameterizedTest
    @DisplayName("той тест")
    @CsvFileSource(resources = "/src/test/resources/data.csv")
    public void csvTest(String a){
            }
}
