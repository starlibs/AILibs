package ai.libs.softwareconfiguration.serialization;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.softwareconfiguration.model.Interface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("SimplifiableJUnitAssertion")
public class RequiredInterfaceLoaderTest {

    JsonNode rootNode;

    public RequiredInterfaceLoaderTest() throws IOException {
        this.rootNode = getRootNodeFromJson(new File("testrsc/required_interface_syntax.json"));
    }

    @Test
    public void test1() throws IOException {
        Interface reqInterface = createRequiredInterface(rootNode.path("requiredInterface").get(0));
        assertTrue("Expected min = 0", reqInterface.getMin() == 0);
        assertTrue("Expected max = 1", reqInterface.getMax() == 1);
    }

    @Test
    public void test2() throws IOException {
        Interface reqInterface = createRequiredInterface(rootNode.path("requiredInterface").get(1));
        assertTrue("Expected min = 1", reqInterface.getMin() == 1);
        assertTrue("Expected max = 1", reqInterface.getMax() == 1);
    }

    @Test
    public void test3() throws IOException {
        Interface reqInterface = createRequiredInterface(rootNode.path("requiredInterface").get(2));
        assertTrue("Expected min = 1", reqInterface.getMin() == 1);
        assertTrue("Expected max = 1", reqInterface.getMax() == 1);
    }

    @Test
    public void test4() throws IOException {
        Interface reqInterface = createRequiredInterface(rootNode.path("requiredInterface").get(3));
        assertTrue("Expected min = 0", reqInterface.getMin() == 0);
        assertTrue("Expected max = 1", reqInterface.getMax() == 1);
    }

    @Test(expected = IOException.class)
    public void test5() throws IOException {
        createRequiredInterface(rootNode.path("requiredInterface").get(4));
    }

    @Test
    public void test6() throws IOException {
        Interface reqInterface = createRequiredInterface(rootNode.path("requiredInterface").get(5));
        assertTrue("Expected min = 2", reqInterface.getMin() == 2);
        assertTrue("Expected max = 6", reqInterface.getMax() == 6);
    }

    @Test(expected = IOException.class)
    public void test7() throws IOException {
        createRequiredInterface(rootNode.path("requiredInterface").get(6));
    }

    @Test(expected = IOException.class)
    public void test8() throws IOException {
        createRequiredInterface(rootNode.path("requiredInterface").get(7));
    }

    @Test(expected = IOException.class)
    public void test9() throws IOException {
        createRequiredInterface(rootNode.path("requiredInterface").get(8));
    }

    @Test(expected = IOException.class)
    public void test10() throws IOException {
        createRequiredInterface(rootNode.path("requiredInterface").get(9));
    }

    private JsonNode getRootNodeFromJson(final File jsonFile) throws IOException {
        String jsonDescription;
        ObjectMapper objectMapper = new ObjectMapper();
        if (jsonFile instanceof ResourceFile) {
            jsonDescription = ResourceUtil.readResourceFileToString(((ResourceFile) jsonFile).getPathName());
        } else {
            jsonDescription = FileUtil.readFileAsString(jsonFile);
        }
        jsonDescription = jsonDescription.replaceAll("/\\*(.*)\\*/", "");

        return objectMapper.readTree(jsonDescription);
    }

    private Interface createRequiredInterface(JsonNode requiredInterface) throws IOException {
        if (!requiredInterface.has("id")) {
            throw new IOException("No id has been specified");
        }
        if (!requiredInterface.has("name")) {
            throw new IOException("No name has been specified");
        }
        if (requiredInterface.has("optional")) {
            if (!requiredInterface.has("min") && !requiredInterface.has("max")) {
                if (requiredInterface.get("optional").asBoolean()) {
                    return new Interface(requiredInterface.get("id").asText(),
                            requiredInterface.get("name").asText(),
                            0,
                            1);
                } else {
                    return new Interface(requiredInterface.get("id").asText(),
                            requiredInterface.get("name").asText(),
                            1,
                            1);
                }
            } else {
                throw new IOException("When specifying \"optional\" for a required interface, both \"min\" and \"max\" must be omitted");
            }
        } else { // optional is missing
            if (!requiredInterface.has("min") && !requiredInterface.has("max")) {
                return new Interface(requiredInterface.get("id").asText(),
                        requiredInterface.get("name").asText(),
                        1,
                        1);// optional is missing
            } else if (requiredInterface.has("min") && requiredInterface.has("max")) {
                int min = requiredInterface.get("min").asInt();
                int max = requiredInterface.get("max").asInt();
                if (min <= max) {
                    return new Interface(requiredInterface.get("id").asText(),
                            requiredInterface.get("name").asText(),
                            requiredInterface.get("min").asInt(),
                            requiredInterface.get("max").asInt());
                } else {
                    throw new IOException("When declaring a required interface, \"min\" should be lesser than \"max\"");
                }
            } else {
                throw new IOException("If not specifying \"optional\" for a required interface, either both \"min\" and \"max\" must be specified or none at all");
            }
        }
    }
}
