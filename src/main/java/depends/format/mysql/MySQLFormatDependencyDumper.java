package depends.format.mysql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import depends.format.AbstractFormatDependencyDumper;
import depends.format.DBUtils;
import depends.format.FormatUtils;
import depends.matrix.core.DependencyMatrix;
import depends.matrix.core.DependencyPair;

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

public class MySQLFormatDependencyDumper extends AbstractFormatDependencyDumper {
    private DBUtils db;

    @Override
    public String getFormatName() {
        return "mysql";
    }

    public MySQLFormatDependencyDumper(DependencyMatrix dependencyMatrix, String inputDir, String dbConfigDir) {
        super(dependencyMatrix, inputDir, "", "");
        this.db = dbConfigDir == null ? null : new DBUtils(dbConfigDir);
    }

	@Override
	public boolean output() {
        if (db == null) {
            System.err.println("Please specify mysql configurations.");
            return false;
        }

		ArrayList<String> nodes = matrix.getNodes();
		Collection<DependencyPair> dependencyPairs = matrix.getDependencyPairs();

        String projectName = FormatUtils.getProjectName(inputDir);
        Date date = DBUtils.getDate();
        // try-with-resources declares java.lang.AutoCloseable
        try (PreparedStatement stmtNode = db.getStatement(DBUtils.generateInsertSql("dependencies_id", new String[] {"project_name", "package_name", "lines_num", "heat", "collected_date", "item_id"}));
             PreparedStatement stmtDepLink = db.getStatement(DBUtils.generateInsertSql("dependencies", new String[] {"project_name", "source", "dest", "collected_date", "item_id"}));
             PreparedStatement stmtDepType = db.getStatement(DBUtils.generateInsertSql("dependency_type", new String[] {
                     "id", "Import_", "Contain_", "Implement_", "Extend_", "Call_", "Parameter_", "Return_", "Use_", "Create_", "Cast_", "Throw_", "ImplLink_", "Annotation_", "MixIn_", "Set_", "dep_id"}));
             PreparedStatement stmtQueryIndex = db.getStatement("SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + db.dbName + "' AND TABLE_NAME = 'dependencies'");
             ResultSet resultSet = stmtQueryIndex.executeQuery();
             PreparedStatement stmtPkgStat = db.getStatement("SELECT SUM(current_effective_lines), AVG(heat) FROM report WHERE project_name = ? AND collected_date = ? AND file_name REGEXP ?")) {

            /* Insert Nodes */
            stmtNode.setString(1, projectName);
            stmtNode.setDate(5, date);
            stmtNode.setNull(6, Types.NULL);

            /* Count total number of lines in a package */
            stmtPkgStat.setString(1, projectName);
            stmtPkgStat.setDate(2, date);

            for (String node : nodes) {
                String nodeName = FormatUtils.formatClassName(node, inputDir);
                stmtNode.setString(2, nodeName);
                stmtPkgStat.setString(3, ".*" + nodeName.replaceAll("\\.", "/") + "/?[a-zA-Z0-9]*(.java)"); // FIXME REGEXP same packageName in different directories
                Object[] resultArray = DBUtils.execResult(stmtPkgStat, 2);
                String loc = resultArray == null || resultArray[0] == null ? "0" : resultArray[0].toString();
                String heat = resultArray == null || resultArray[1] == null ? "0.0" : resultArray[1].toString();
                stmtNode.setInt(3, Integer.parseInt(loc));
                stmtNode.setFloat(4, Float.parseFloat(heat));
				db.executeStatement(stmtNode);
			}

            /* Insert Links and Link Types */
            stmtDepLink.setString(1, projectName);
            stmtDepLink.setDate(4, date);
            stmtDepLink.setNull(5, Types.NULL);

            stmtDepType.setNull(1, Types.NULL);

            int dep_id = resultSet.next() ? resultSet.getInt(1) : 0;

            for (DependencyPair dependencyPair : dependencyPairs) {
                stmtDepLink.setString(2, FormatUtils.formatClassName(nodes.get(dependencyPair.getFrom()), inputDir));
                stmtDepLink.setString(3, FormatUtils.formatClassName(nodes.get(dependencyPair.getTo()), inputDir));
                db.executeStatement(stmtDepLink);

                insertValueMapping(stmtDepType, FormatUtils.buildValueObject(dependencyPair.getDependencies()));
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

    private void insertValueMapping(PreparedStatement statement, Map<String, Float> valueObjects) throws SQLException {
        for (Type type: Type.values()) {
            String strType = type.toString();
            int parameterIndex = Type.valueOf(strType).ordinal() + 1;
            int weight = valueObjects.containsKey(strType) ? Math.round(valueObjects.get(strType)) : 0;
            statement.setInt(parameterIndex + 1, weight);
        }
	}
}
