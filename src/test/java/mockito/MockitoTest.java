package mockito;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by xhldtc on 9/13/17.
 */
public class MockitoTest {

    @Test
    public void test1() {
        //mock creation
        List mockedList = mock(List.class);

        //using mock object
        mockedList.add("one");
        mockedList.clear();

        //verification
        verify(mockedList).add("one");
        verify(mockedList).clear();
    }

    @Test
    public void test2() {
        //You can mock concrete classes, not just interfaces
        LinkedList mockedList = mock(LinkedList.class);

        //stubbing
        when(mockedList.get(0)).thenReturn("first");
        when(mockedList.get(1)).thenThrow(new RuntimeException());

        //following prints "first"
        Assert.assertEquals(mockedList.get(0), "first");
        //following throws runtime exception
        Assert.assertThrows(RuntimeException.class, () -> mockedList.get(1));
        //following prints "null" because get(999) was not stubbed
        Assert.assertNull(mockedList.get(999));

        //Although it is possible to verify a stubbed invocation, usually it's just redundant
        //If your code cares what get(0) returns, then something else breaks (often even before verify() gets executed).
        //If your code doesn't care what get(0) returns, then it should not be stubbed. Not convinced? See here.
        verify(mockedList).get(0);
    }

    @Test
    public void test3() {
        LinkedList<String> mockedList = mock(LinkedList.class);
//stubbing using built-in anyInt() argument matcher
        when(mockedList.get(anyInt())).thenReturn("element");
        when(mockedList.set(anyInt(), eq("hello"))).thenReturn("world");

        //stubbing using custom matcher (let's say isValid() returns your own matcher implementation):
        when(mockedList.contains(argThat(s -> s.equals("abc")))).thenReturn(true);
        when(mockedList.contains("hello")).thenReturn(false);

        //following prints "element"
        Assert.assertEquals(mockedList.get(1), "element");
        Assert.assertTrue(mockedList.contains("abc"));
        Assert.assertFalse(mockedList.contains("ab"));
        Assert.assertFalse(mockedList.contains("hello"));
        mockedList.add("123456");

        //you can also verify using an argument matcher
        verify(mockedList).get(anyInt());

        //argument matchers can also be written as Java 8 Lambdas
        verify(mockedList).add(argThat(someString -> someString.length() > 5));
    }
}
