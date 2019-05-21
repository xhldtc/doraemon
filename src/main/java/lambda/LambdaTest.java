package lambda;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.swing.text.DateFormatter;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by xhldtc on 8/26/17.
 */
public class LambdaTest {

    public static void main(String[] args) {
        ThreadLocal<DateFormatter> threadLocal
                = ThreadLocal.withInitial(() -> new DateFormatter(new SimpleDateFormat("dd-MMM-yyyy")));
        System.out.println(threadLocal.get().getFormat().format(new Date(0)));
    }

    @Test
    public void test() {
        Assert.assertEquals(countLowerCase("abcABC"), 3);
        Assert.assertEquals(findMaxLowerCaseString(Stream.of("abc", "ABC", "aaBB")).orElse(null), "abc");
        Assert.assertEquals(findMaxLowerCaseString(Stream.empty()).orElse(null), null);
    }

    public Optional<String> findMaxLowerCaseString(Stream<String> stream) {
        return stream.max(Comparator.comparing(this::countLowerCase));
    }

    public long countLowerCase(String s) {
        return s.chars().filter(Character::isLowerCase).count();
    }
}
