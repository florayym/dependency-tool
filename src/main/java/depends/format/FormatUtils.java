package depends.format;

import depends.matrix.core.DependencyValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FormatUtils {

    public static String formatClassName(String str, String inputDir) {
        str = str.replaceAll("\\\\", "/"); // Win -> Unix
        // whether granularity is file (class) or file
        return str.endsWith(".java") ? str.substring(inputDir.length() + 1, str.length() - 5) : str;
        // return str.substring(str.indexOf('/', str.indexOf('/', str.indexOf('/') + 1) + 1) + 1); // shorten to certain level
    }

    public static Map<String, Float> buildValueObject(Collection<DependencyValue> dependencies) {
        Map<String, Float> valueObject = new HashMap<>();
        for (DependencyValue dependency : dependencies) {
            valueObject.put(dependency.getType(), (float) dependency.getWeight());
        }
        return valueObject;
    }

    public static String getProjectName(String inputDir) {
        return inputDir.substring(inputDir.replaceAll("\\\\", "/").lastIndexOf('/') + 1);
    }

    // public static int countLOC(PreparedStatement statement, String packageName) throws SQLException {
    //     // try {
    //     //     Process process = Runtime.getRuntime().exec("cloc --quiet " + packageName); // https://github.com/AlDanial/cloc add cloc-1.86/ to path
    //     //     BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
    //     //     String s = null;
    //     //     while ((s = stdInput.readLine()) != null) {
    //     //         if (s.contains("Java")) { // only count java code line num
    //     //             return Integer.parseInt(s.split("\\s+")[4].trim());
    //     //         }
    //     //     }
    //     // } catch (IOException e) {
    //     //     e.printStackTrace();
    //     // }
    //     //  return 0;
    // }
}
