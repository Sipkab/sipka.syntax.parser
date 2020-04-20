/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package testing.saker.sipka.syntax.parser;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.classloader.ClassLoaderDataFinder;
import saker.build.thirdparty.saker.util.classloader.JarClassLoaderDataFinder;
import saker.build.thirdparty.saker.util.classloader.MultiDataClassLoader;
import saker.build.thirdparty.saker.util.io.ByteSource;
import saker.build.util.classloader.SakerPathClassLoaderDataFinder;
import testing.saker.SakerTest;
import testing.saker.nest.util.NestRepositoryCachingEnvironmentTestCase;

@SakerTest
public class JavaRuleGenCompileSakerTest extends NestRepositoryCachingEnvironmentTestCase {

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("sipka.syntax.parser.version", testParameters.get("SyntaxParserVersion"));
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		try (ClassLoaderDataFinder libcldf = new JarClassLoaderDataFinder(
				Paths.get(testParameters.get("SyntaxParserExportPath")))) {
			MultiDataClassLoader libcl = new MultiDataClassLoader(libcldf);
			Class<?> languageclass = Class.forName("sipka.syntax.parser.model.rule.Language", false, libcl);

			Object libparsedlang;
			try (InputStream testlanginput = ByteSource
					.toInputStream(files.openInput(PATH_WORKING_DIRECTORY.resolve("test.lang")))) {
				libparsedlang = ((Map<String, ?>) languageclass.getMethod("fromInputStream", InputStream.class)
						.invoke(null, testlanginput)).get("test");
			}
			assertLangObjParseable(libparsedlang);

			CombinedTargetTaskResult res;

			files.createDirectories(PATH_WORKING_DIRECTORY.resolve("src"));
			res = runScriptTask("build");

			try (SakerPathClassLoaderDataFinder compiledcldf = new SakerPathClassLoaderDataFinder(files,
					SakerPath.valueOf(res.getTargetTaskResult("classdir").toString()))) {
				MultiDataClassLoader compiledcl = new MultiDataClassLoader(libcl, compiledcldf);
				Class<?> langsclass = Class.forName("test.Languages", false, compiledcl);
				Object compiledlangobj = languageclass.cast(langsclass.getMethod("gettest").invoke(null));
				assertLangObjParseable(compiledlangobj);
			}

			runScriptTask("build");
			assertEmpty(getMetric().getRunTaskIdResults());
		}
	}

	private void assertLangObjParseable(Object langobj) throws Exception {
		try (InputStream in = ByteSource
				.toInputStream(files.openInput(PATH_WORKING_DIRECTORY.resolve("example.build")))) {
			langobj.getClass().getMethod("parseInputStream", InputStream.class).invoke(langobj, in);
		}
	}
}
