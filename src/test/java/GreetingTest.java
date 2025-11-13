import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GreetingTest {

    @Test
    void testGetMessage() {
        assertEquals("Hello DevSecOps!", Greeting.getMessage());
    }
}
