package ai.libs.jaicore.basic.sets;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class SetUtilTestIntersection {

    private ArrayList<String> a;
    private ArrayList<String> b;
    private Collection intersection;

    public SetUtilTestIntersection(ArrayList<String> a, ArrayList<String> b, Collection intersection) {
        this.a = a;
        this.b = b;
        this.intersection = intersection;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> setData(){

        Collection<String> a = new ArrayList<>();
        a.add("i1");
        a.add("i2");
        a.add("i3");
        Collection<String> b = new ArrayList<>();
        b.add("i2");
        b.add("i3");
        b.add("i6");
        Collection<String> intersectionOfTwoSets = new ArrayList<>();
        intersectionOfTwoSets.add("i2");
        intersectionOfTwoSets.add("i3");

        return Arrays.asList(new Object[][] {
                {a, b, intersectionOfTwoSets}
        });

    }

    @Test
    public void testIntersectionOfTwoSets(){

        Collection ointersection = new ArrayList();
        ointersection = SetUtil.intersection(a,b);
        assertEquals(ointersection, intersection);

    }
}
