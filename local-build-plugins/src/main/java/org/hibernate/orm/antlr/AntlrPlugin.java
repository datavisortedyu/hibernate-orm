/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.antlr;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;

/**
 * Custom Antlr v4 Plugin
 *
 * The Gradle-supplied Antlr plugin attempts to simultaneously support multiple
 * versions of Antlr which leads to many difficulties. This custom plugin provides
 * dedicated and simplified support for Antlr v4
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("unused")
public class AntlrPlugin implements Plugin<Project> {
	public static final String ANTLR = "antlr";

	public static final String HQL_PKG = "org.hibernate.grammars.hql";
	public static final String SQL_PKG = "org.hibernate.grammars.importsql";
	public static final String GRAPH_PKG = "org.hibernate.grammars.graph";
	public static final String ORDER_PKG = "org.hibernate.grammars.ordering";

	@Override
	public void apply(Project project) {
		final Task groupingTask = project.getTasks().create( "generateParsers" );
		groupingTask.setDescription( "Performs all defined Antlr grammar generations" );
		groupingTask.setGroup( ANTLR );

		final AntlrSpec antlrSpec = project.getExtensions().create(
				AntlrSpec.REGISTRATION_NAME,
				AntlrSpec.class,
				project,
				groupingTask
		);

		final Configuration antlrDependencies = project.getConfigurations().maybeCreate( ANTLR );

		final SourceSet mainSourceSet = project.getExtensions()
				.getByType( JavaPluginExtension.class )
				.getSourceSets()
				.getByName( SourceSet.MAIN_SOURCE_SET_NAME );
		mainSourceSet.setCompileClasspath( mainSourceSet.getCompileClasspath().plus( antlrDependencies ) );
		mainSourceSet.getJava().srcDir( antlrSpec.getOutputBaseDirectory() );

		final Task compileTask = project.getTasks().getByName( mainSourceSet.getCompileJavaTaskName() );
		compileTask.dependsOn( groupingTask );

		populateGrammars( antlrSpec );
	}

	private void populateGrammars(AntlrSpec antlrSpec) {
		antlrSpec.getGrammarDescriptors().create(
				"hql",
				(grammarDescriptor) -> {
					grammarDescriptor.getPackageName().set( HQL_PKG );
					grammarDescriptor.getLexerFileName().set( "HqlLexer.g4" );
					grammarDescriptor.getParserFileName().set( "HqlParser.g4" );
				}
		);

		antlrSpec.getGrammarDescriptors().create(
				"graph",
				(grammarDescriptor) -> {
					grammarDescriptor.getPackageName().set( GRAPH_PKG );
					grammarDescriptor.getLexerFileName().set( "GraphLanguageLexer.g4" );
					grammarDescriptor.getParserFileName().set( "GraphLanguageParser.g4" );
				}
		);

		antlrSpec.getGrammarDescriptors().create(
				"sqlScript",
				(grammarDescriptor) -> {
					grammarDescriptor.getPackageName().set( SQL_PKG );
					grammarDescriptor.getLexerFileName().set( "SqlScriptLexer.g4" );
					grammarDescriptor.getParserFileName().set( "SqlScriptParser.g4" );
				}
		);

		antlrSpec.getGrammarDescriptors().create(
				"ordering",
				(grammarDescriptor) -> {
					grammarDescriptor.getPackageName().set( ORDER_PKG );
					grammarDescriptor.getLexerFileName().set( "OrderingLexer.g4" );
					grammarDescriptor.getParserFileName().set( "OrderingParser.g4" );
				}
		);
	}
}
