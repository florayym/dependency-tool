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

package depends.format.javascript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import depends.format.AbstractFormatDependencyDumper;
import depends.matrix.core.DependencyMatrix;
import depends.matrix.core.DependencyPair;
import depends.matrix.core.DependencyValue;

public class JavaScriptFormatDependencyDumper extends AbstractFormatDependencyDumper {
	@Override
	public String getFormatName() {
		return "js";
	}

	public JavaScriptFormatDependencyDumper(DependencyMatrix dependencyMatrix, String inputDir, String projectName, String outputDir) {
		super(dependencyMatrix, inputDir, projectName, outputDir);
	}

	@Override
	public boolean output() {
		ArrayList<String> nodes = matrix.getNodes();
		Collection<DependencyPair> dependencyPairs = matrix.getDependencyPairs();

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

		return true;
	}

	private String formatName(String str) {
		if (!str.contains("\\")) {
			return str.substring(str.indexOf('.', str.indexOf('.') + 1) + 1);
		}
		str = str.replaceAll("\\\\", "/").substring(inputDir.length() + 1).split("\\.")[0];
		//		return str.substring(str.indexOf('/', str.indexOf('/', str.indexOf('/') + 1) + 1) + 1);
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
}
