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

package depends.extractor.ruby;

import static depends.deptypes.DependencyType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import depends.entity.repo.BuiltInType;
import depends.extractor.AbstractLangProcessor;
import depends.extractor.FileParser;
import depends.extractor.ParserCreator;
import depends.extractor.ruby.jruby.JRubyFileParser;
import depends.relations.ImportLookupStrategy;

public class RubyProcessor extends AbstractLangProcessor implements ParserCreator {
    private static final String LANG = "ruby";
    private static final String[] SUFFIX = new String[] {".rb"};
	private ExecutorService executor;
    public RubyProcessor() {
    	super(true);
    }

	@Override
	public String supportedLanguage() {
		return LANG;
	}

	@Override
	public String[] fileSuffixes() {
		return SUFFIX;
	}


	@Override
	public FileParser createFileParser(String fileFullPath) {
		executor = Executors.newSingleThreadExecutor();
		return new JRubyFileParser(fileFullPath, entityRepo, executor, new IncludedFileLocator(super.includePaths()), inferer, this);
	}


	@Override
	protected void finalize() throws Throwable {
		this.executor.shutdown();
		super.finalize();
	}

	@Override
	public ImportLookupStrategy getImportLookupStrategy() {
		return new RubyImportLookupStrategy();
	}


	@Override
	public BuiltInType getBuiltInType() {
		return new RubyBuiltInType();
	}
	
	@Override
	public List<String> supportedRelations() {
		ArrayList<String> dependencyTypes = new ArrayList<>();
		dependencyTypes.add(IMPORT);
		dependencyTypes.add(CONTAIN);
		dependencyTypes.add(INHERIT);
		dependencyTypes.add(CALL);
		dependencyTypes.add(PARAMETER);
		dependencyTypes.add(RETURN);
		dependencyTypes.add(SET);
		dependencyTypes.add(CREATE);
		dependencyTypes.add(USE);
		dependencyTypes.add(CAST);
		dependencyTypes.add(THROW);
		dependencyTypes.add(MIXIN);
		return dependencyTypes;
	}		
}
