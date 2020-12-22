package depends.format.javascript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import depends.format.AbstractFormatDependencyDumper;
import depends.format.DBUtils;
import depends.format.FormatUtils;
import depends.matrix.core.DependencyMatrix;
import depends.matrix.core.DependencyPair;

public class JavaScriptFormatDependencyDumper extends AbstractFormatDependencyDumper {
    private DBUtils db;

    @Override
    public String getFormatName() {
        return "js";
    }

    public JavaScriptFormatDependencyDumper(DependencyMatrix dependencyMatrix, String inputDir, String outputFileName, String outputDir, String dbConfigDir) {
        super(dependencyMatrix, inputDir, outputFileName, outputDir);
        this.db = dbConfigDir == null || dbConfigDir.isEmpty() ? null : new DBUtils(dbConfigDir);
    }

	@Override
	public boolean output() {
		ArrayList<String> nodes = matrix.getNodes();
		Collection<DependencyPair> dependencyPairs = matrix.getDependencyPairs();

        /* Dump in local js file */
        File file = new File(composeFilename() + ".js");
        try (BufferedWriter br = new BufferedWriter(new FileWriter(file));
             PreparedStatement stmtPkgStat = db == null ? null
                     : db.getStatement("SELECT SUM(end_total_lines), SUM(heat) FROM project_report " +
                                        "WHERE collected_date = ? AND project_name = ? AND file_path REGEXP ?")) {
            if (db != null) {
                br.write("var dependencies = {\n\tnodes: [\n");
                stmtPkgStat.setDate(1, DBUtils.getDate(null));
                stmtPkgStat.setString(2, FormatUtils.getProjectName(inputDir));

                for (String node: nodes) {
                    String nodeName = FormatUtils.formatClassName(node, inputDir);
                    stmtPkgStat.setString(3, ".*" + nodeName.replaceAll("\\.", "/") + "/?[a-zA-Z0-9]*(.java)");
                    Object[] resultArray = DBUtils.execResult(stmtPkgStat, 2);
                    String loc = resultArray == null || resultArray[0] == null ? "0" : resultArray[0].toString();
                    String heat = resultArray == null || resultArray[1] == null ? "0.0" : resultArray[1].toString();
                    br.write("\t\t{\"name\": \"" + nodeName + "\", \"loc\": " + loc + ", \"heat\": " + heat + "},\n");
                }
                br.write("\t],\n");
                br.write("\tlinks: [\n");
            } else {
                br.write("var dependencies = {\n\tlinks: [\n");
            }
            for (DependencyPair dependencyPair : dependencyPairs) {
                String src = FormatUtils.formatClassName(nodes.get(dependencyPair.getFrom()), inputDir);
                String dest = FormatUtils.formatClassName(nodes.get(dependencyPair.getTo()), inputDir);
                String values = createValueString(FormatUtils.buildValueObject(dependencyPair.getDependencies()));
                br.write("\t\t{\"source\": \"" + src + "\", \"dest\": \"" + dest + "\", \"values\": " + values + "},\n");
            }
            br.write("\t]\n};");
        } catch (FileNotFoundException e) {
            System.err.println("Cannot found " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Cannot write " + file.getAbsolutePath());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String createValueString(Map<String, Float> valueObjects) {
        StringBuilder values = new StringBuilder("{");
        for (Map.Entry<String, Float> entry : valueObjects.entrySet()) {
            values.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue()).append(", ");
        }
        return values.substring(0, values.lastIndexOf(",")) + "}";
    }
}
