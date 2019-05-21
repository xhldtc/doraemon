package lambda;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by xhldtc on 8/26/17.
 */
public class Artist {

    private String name;
    private List<Artist> members;
    private String origin;

    public Stream<Artist> getMembers() {
        return members.stream();
    }
}
