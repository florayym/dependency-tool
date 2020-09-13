package depends.format.javascript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import depends.format.AbstractFormatDependencyDumper;
import depends.format.DBUtils;
import depends.matrix.core.DependencyMatrix;
import depends.matrix.core.DependencyPair;
import depends.matrix.core.DependencyValue;

enum Type {
    Import,
    Contain,
    Implement,
    Extend,
    Call,
    Parameter,
    Return,
    Use,
    Create,
    Cast,
    Throw,
    ImplLink,
    Annotation,
    MixIn,
    Set
}

public class JavaScriptFormatDependencyDumper extends AbstractFormatDependencyDumper {
    @Override
    public String getFormatName() {
        return "js";
    }

    public JavaScriptFormatDependencyDumper(DependencyMatrix dependencyMatrix, String inputDir, String outputFileName, String outputDir, String dbConfigDir) {
        super(dependencyMatrix, inputDir, outputFileName, outputDir, dbConfigDir);
    }

	@Override
	public boolean output() {
		ArrayList<String> nodes = matrix.getNodes();
		Collection<DependencyPair> dependencyPairs = matrix.getDependencyPairs();

        /* Dump in local js file */
        File file = new File(composeFilename() + ".js");
        try (BufferedWriter br = new BufferedWriter(new FileWriter(file))) {
            br.write("var dependencies = {links:[\n");
            for (DependencyPair dependencyPair : dependencyPairs) {
                String src = formatName(nodes.get(dependencyPair.getFrom()));
                String dest = formatName(nodes.get(dependencyPair.getTo()));
                String values = createValueString(buildValueObject(dependencyPair.getDependencies()));
                br.write("\t{\"source\": \"" + src + "\", \"dest\": \"" + dest + "\", \"values\": " + values + "},\n");
            }
            br.write("]};");
        } catch (FileNotFoundException e) {
            System.err.println("Cannot found " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Cannot write " + file.getAbsolutePath());
        }

        /* Persist in MySQL */
        if (db == null) {
            return true;
        }

        String projectName = inputDir.substring(inputDir.lastIndexOf('\\') + 1);
        Date date = new Date(new java.util.Date().getTime());
        // try-with-resources declares java.lang.AutoCloseable
        try (PreparedStatement stmtNode = db.getStatement(DBUtils.generateInsertSql("dependencies_id", new String[] {"project_name", "package_name", "lines_num", "collected_date", "item_id"}));
             PreparedStatement stmtDepLink = db.getStatement(DBUtils.generateInsertSql("dependencies", new String[] {"project_name", "source", "dest", "collected_date", "item_id"}));
             PreparedStatement stmtDepType = db.getStatement(DBUtils.generateInsertSql("dependency_type", new String[] {
                     "id", "Import_", "Contain_", "Implement_", "Extend_", "Call_", "Parameter_", "Return_", "Use_", "Create_", "Cast_", "Throw_", "ImplLink_", "Annotation_", "MixIn_", "Set_", "dep_id"}));
             PreparedStatement stmtQueryIndex = db.getStatement("SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + db.dbName + "' AND TABLE_NAME = 'dependencies'");
             ResultSet resultSet = stmtQueryIndex.executeQuery();
             PreparedStatement stmtCountLoc = db.getStatement(
                     "SELECT SUM(current_effective_lines) FROM report WHERE file_name REGEXP ? AND project_name = ? AND collected_date = ?")) {

            /* Insert Nodes */
            stmtNode.setString(1, projectName);
            stmtNode.setDate(4, date);
            stmtNode.setNull(5, Types.NULL);

            /* Count total number of lines in a package */
            stmtCountLoc.setString(2, projectName);
            stmtCountLoc.setDate(3, date);

            for (String node : nodes) {
                stmtNode.setString(2, node);
                stmtNode.setInt(3, countLOC(stmtCountLoc, node.replaceAll("\\.", "/"))); // FIXME same packageName in different directories
				db.executeStatement(stmtNode);
			}

            /* Insert Links and Link Types */
            stmtDepLink.setString(1, projectName);
            stmtDepLink.setDate(4, date);
            stmtDepLink.setNull(5, Types.NULL);

            stmtDepType.setNull(1, Types.NULL);

            int dep_id = resultSet.next() ? resultSet.getInt(1) : 0;

            for (DependencyPair dependencyPair : dependencyPairs) {
                stmtDepLink.setString(2, formatName(nodes.get(dependencyPair.getFrom())));
                stmtDepLink.setString(3, formatName(nodes.get(dependencyPair.getTo())));
                db.executeStatement(stmtDepLink);

                insertValueMapping(stmtDepType, buildValueObject(dependencyPair.getDependencies()));
                stmtDepType.setInt(17, dep_id++);
                db.executeStatement(stmtDepType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.closeConnection();
            }
        }
        return true;
    }

    private String formatName(String str) {
        if (!str.contains("\\")) {
            // return str.substring(str.indexOf('.', str.indexOf('.') + 1) + 1);
            return str;
        }
        str = str.replaceAll("\\\\", "/").substring(inputDir.length() + 1).split("\\.")[0];
        // return str.substring(str.indexOf('/', str.indexOf('/', str.indexOf('/') + 1) + 1) + 1);
        return str;
    }

    private Map<String, Float> buildValueObject(Collection<DependencyValue> dependencies) {
        Map<String, Float> valueObject = new HashMap<>();
        for (DependencyValue dependency : dependencies) {
            valueObject.put(dependency.getType(), (float) dependency.getWeight());
        }
        return valueObject;
    }

    private String createValueString(Map<String, Float> valueObjects) {
        StringBuilder values = new StringBuilder("{");
        for (Map.Entry<String, Float> entry : valueObjects.entrySet()) {
            values.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue()).append(", ");
        }
        return values.substring(0, values.lastIndexOf(",")) + "}";
    }

    private void insertValueMapping(PreparedStatement statement, Map<String, Float> valueObjects) throws SQLException {
        for (Type type: Type.values()) {
            String strType = type.toString();
            int parameterIndex = Type.valueOf(strType).ordinal() + 1;
            int weight = valueObjects.containsKey(strType) ? Math.round(valueObjects.get(strType)) : 0;
            statement.setInt(parameterIndex + 1, weight);
        }
	}

	private int countLOC(PreparedStatement statement, String packageName) throws SQLException {
       // try {
       //     Process process = Runtime.getRuntime().exec("cloc --quiet " + packageName); // https://github.com/AlDanial/cloc add cloc-1.86/ to path
       //     BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
       //     String s = null;
       //     while ((s = stdInput.readLine()) != null) {
       //         if (s.contains("Java")) { // only count java code line num
       //             return Integer.parseInt(s.split("\\s+")[4].trim());
       //         }
       //     }
       // } catch (IOException e) {
       //     e.printStackTrace();
       // }
       //  return 0;
        statement.setString(1, ".*" + packageName + "/[a-zA-Z0-9]*(.java)");
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next() ? resultSet.getInt(1) : 0;
    }
}
