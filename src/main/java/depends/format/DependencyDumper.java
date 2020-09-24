/*
MIT License

Copyright (c) 2018-2019 Gang ZHANG

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package depends.format;

import java.util.List;

import depends.format.detail.DetailTextFormatDependencyDumper;
import depends.format.dot.DotFormatDependencyDumper;
import depends.format.dot.DotFullnameDependencyDumper;
import depends.format.excel.ExcelXlsFormatDependencyDumper;
import depends.format.excel.ExcelXlsxFormatDependencyDumper;
import depends.format.javascript.JavaScriptFormatDependencyDumper;
import depends.format.json.JsonFormatDependencyDumper;
import depends.format.mysql.MySQLFormatDependencyDumper;
import depends.format.plantuml.BriefPlantUmlFormatDependencyDumper;
import depends.format.plantuml.PlantUmlFormatDependencyDumper;
import depends.format.xml.XmlFormatDependencyDumper;
import depends.matrix.core.DependencyMatrix;
import edu.emory.mathcs.backport.java.util.Arrays;

public class DependencyDumper {

	private DependencyMatrix dependencyMatrix;

	public DependencyDumper(DependencyMatrix dependencies) {
		this.dependencyMatrix = dependencies;
	}

	public void outputResult(String inputDir, String outputFileName, String outputDir, String[] outputFormat, String dbConfigDir) {
        outputDeps(inputDir, outputFileName, outputDir, outputFormat, dbConfigDir);
	}

	private final void outputDeps(String inputDir, String outputFileName, String outputDir, String[] outputFormat, String dbConfigDir) {
		@SuppressWarnings("unchecked")
		List<String> formatList = Arrays.asList(outputFormat);
		AbstractFormatDependencyDumper[] builders = new AbstractFormatDependencyDumper[] {
		 	new DetailTextFormatDependencyDumper(dependencyMatrix, inputDir, outputFileName, outputDir),
		 	new XmlFormatDependencyDumper(dependencyMatrix, inputDir, outputFileName, outputDir),
		 	new JsonFormatDependencyDumper(dependencyMatrix, inputDir, outputFileName, outputDir),
		 	new JavaScriptFormatDependencyDumper(dependencyMatrix, inputDir, outputFileName, outputDir, dbConfigDir),
			new MySQLFormatDependencyDumper(dependencyMatrix, inputDir, dbConfigDir),
		 	new ExcelXlsFormatDependencyDumper(dependencyMatrix, inputDir, outputFileName, outputDir),
		 	new ExcelXlsxFormatDependencyDumper(dependencyMatrix, inputDir, outputFileName, outputDir),
		 	new DotFormatDependencyDumper(dependencyMatrix, inputDir, outputFileName, outputDir),
		 	new DotFullnameDependencyDumper(dependencyMatrix, inputDir, outputFileName, outputDir),
		 	new PlantUmlFormatDependencyDumper(dependencyMatrix, inputDir, outputFileName, outputDir),
		 	new BriefPlantUmlFormatDependencyDumper(dependencyMatrix, inputDir, outputFileName, outputDir)
		};
		for (AbstractFormatDependencyDumper builder : builders) {
			if (formatList.contains(builder.getFormatName())){
				if (!builder.output()) {
					System.err.println("Output to " + builder.getFormatName() + " failed!");
				}
			}
		}
    }
}
