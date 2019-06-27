package ai.libs.jaicore.basic.sets;

import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class SetUtilTest1 {

    private HashSet<String> a;
    private HashSet<String> b;
    private HashSet<String> c;
    private Collection setunionOfThreeSets;


    //constructor with three sets
    public SetUtilTest1(HashSet<String> a, HashSet<String> b, HashSet<String> c, Collection setunionOfThreeSets){
        this.a = a;
        this.b = b;
        this.c = c;
        this.setunionOfThreeSets = setunionOfThreeSets;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> setData(){

        Collection<String> a = new HashSet<>();
        a.add("i1");
        a.add("i2");
        a.add("i3");
        Collection<String> b = new HashSet<>();
        b.add("i3");
        b.add("i5");
        b.add("i6");
        Collection<String> c = new HashSet<>();
        c.add("i1");
        c.add("i5");
        c.add("i7");
        Collection<String> unionOfThreeSets = new HashSet<>();
        unionOfThreeSets.add("i1");
        unionOfThreeSets.add("i2");
        unionOfThreeSets.add("i3");
        unionOfThreeSets.add("i5");
        unionOfThreeSets.add("i6");
        unionOfThreeSets.add("i7");

            return Arrays.asList(new Object[][] {
                    {b, c, a, unionOfThreeSets }
            });
    }


    @Test
    public void testUnionOfThreeSets(){

         Collection actualUnionForThreeSets = new ArrayList();
         actualUnionForThreeSets = SetUtil.union(b,c,a);
         assertEquals(actualUnionForThreeSets, setunionOfThreeSets);

    }


}
