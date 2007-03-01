/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.performance;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;

import junit.framework.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.builder.TestingEnvironment;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.performance.util.JdtCorePerformanceMeter;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;



public abstract class FullSourceWorkspaceTests extends TestCase {

	// Debug variables
	final static boolean DEBUG = "true".equals(System.getProperty("debug"));
	final static boolean PRINT = "true".equals(System.getProperty("print"));
	/**
	 * Flag to validate test run environnement.
	 * <p>
	 * This property has been added to speed-up check-up of local shells to run the
	 * entire performance tests.
	 * <p>
	 * WARNING: if this property is set, *nothing at all* will be run, neither measure nor warm-up.
	 */
	final static boolean TOUCH = "true".equals(System.getProperty("touch"));

	// Options
	final static Hashtable INITIAL_OPTIONS = JavaCore.getOptions();
	final static String COMPLIANCE = System.getProperty("compliance");
	static {
		Hashtable options = INITIAL_OPTIONS;
		String compliance = compliance();
		if (compliance == null) {
			System.out.println("Used default compliance: "+options.get(JavaCore.COMPILER_COMPLIANCE));
		} else {
			System.out.println("Used compliance: "+compliance);
			options.put(CompilerOptions.OPTION_Compliance, compliance);
			options.put(CompilerOptions.OPTION_Source, compliance);	
			options.put(CompilerOptions.OPTION_TargetPlatform, compliance);
			JavaCore.setOptions(options);
		}
	}
	protected static String compliance() {
		String compliance = null;
		if ("1.3".equals(COMPLIANCE)) {
			compliance = CompilerOptions.VERSION_1_3;
		} else if ("1.4".equals(COMPLIANCE)) {
			compliance = CompilerOptions.VERSION_1_4;
		} else if ("1.5".equals(COMPLIANCE) || "5.0".equals(COMPLIANCE)) {
			compliance = CompilerOptions.VERSION_1_5;
		} else if ("1.6".equals(COMPLIANCE) || "6.0".equals(COMPLIANCE)) {
			compliance = CompilerOptions.VERSION_1_6;
		}
		return compliance;
	}
	
	// Garbage collect constants
	final static int MAX_GC = 10; // Max gc iterations
	final static int TIME_GC = 500; // Sleep to wait gc to run (in ms)
	final static int DELTA_GC = 1000; // Threshold to remaining free memory

	// Workspace variables
	protected static TestingEnvironment ENV = null;
	protected static IJavaProject[] ALL_PROJECTS;
	protected static IJavaProject JDT_CORE_PROJECT;
	protected static ICompilationUnit PARSER_WORKING_COPY;
	protected final static String BIG_PROJECT_NAME = "BigProject";
	protected static JavaProject BIG_PROJECT;
//	protected final static String JUNIT_PROJECT_NAME = "junit";
//	protected static IJavaProject JUNIT_PROJECT;
	
	// Compilaiton variable
	public static final String COMPILER_OUTPUT_DIR;
	static {
		String outputDir = null;
		String container = System.getProperty("user.home");
		if (container == null){
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot workspaceRoot = workspace.getRoot();
			File pluginDir = workspaceRoot.getProject(JavaCore.PLUGIN_ID).getLocation().toFile();
			try {
				outputDir = pluginDir.getCanonicalPath() + File.separator + "bin";
			} catch (IOException e) {
				// skip
			}
		} else {
			outputDir = Util.toNativePath(container) + File.separator + "bin";
		}
		if (outputDir == null) {
			COMPILER_OUTPUT_DIR = "none";
		} else {
			COMPILER_OUTPUT_DIR = "\""+outputDir+"\"";
		}
	}


	// Index variables
	protected static IndexManager INDEX_MANAGER = JavaModelManager.getJavaModelManager().getIndexManager();
	
	// Tests infos
	protected static int ALL_TESTS_COUNT = 0;
	protected static int TEST_POSITION = 0;
	protected static List TESTS_NAME_LIST;

	/**
	 * Count of measures done for all tests.
	 * <b>
	 * Default value is 10 but can be modified using system property "measures".
	 * <b>
	 * For example, "-Dmeasures=1" will make all performance test suites to run
	 * only 1 iteration for each test.
	 */
	protected final static int MEASURES_COUNT;
	static {
		String measures = System.getProperty("measures", "10");
		int count = 10;
		try {
			count = Integer.parseInt(measures);
			if (count < 0 || count > 20) {
				System.out.println("INFO: Measures parameter ("+count+") is ignored as it is an invalid value! (should be between 0 and 20)");
				count = 10;
			} else if (count != 10) {
				System.out.println("WARNING: Measures count has been changed while running this test = "+count+" instead of 10 normally!");
			}
		}
		catch (NumberFormatException nfe) {
			// use default value
			System.out.println("INFO: Specified 'measures' VM argument (="+measures+") is ignored as it is not an integer (0-20)!");
		}
		MEASURES_COUNT = count;
	}

	// Scenario information
	String scenarioReadableName, scenarioShortName;
	StringBuffer scenarioComment;
	static Map SCENARII_COMMENT = new HashMap();

	// Time measuring
	long startMeasuring, testDuration;

	// Standard deviation threshold. Statistic should not be take into account when it's reached
	protected final static double STDDEV_THRESHOLD = 0.02; // default is 2%

	/**
	 * Variable used for log files.
	 * Log files are used in conjonction with {@link JdtCorePerformanceMeter} class.
	 * These are file where CPU times of each test of subclasses are stored.
	 * This specific way to run performance tests is activated by specifying
	 * following options:
	 *		-DPerformanceMeterFactory=org.eclipse.jdt.core.tests.performance:org.eclipse.jdt.core.tests.performance.util.JdtCorePerformanceMeterFactory
	 *		-DlogDir=directory where you want to write log files (for example d:/usr/OTI/tests/perfs/stats)
	 * 
	 */
	// Store directory where to put files
	private final static File INVALID_DIR = new File("Invalid");
	protected static File LOG_DIR;
	// Types of statistic which can be stored.
	protected final static String[] LOG_TYPES = { "cpu", "elapsed" };
	// Main version which is logged
	protected final static String LOG_VERSION;
	static {
		String version = new Main(null, null, false).bind("compiler.version");
		version = version.substring(0, version.indexOf(','));
		if (version.startsWith("0.")) {
			version = "v_"+version.substring(2);
		}
		LOG_VERSION = version;
	}
	// Patch version currently applied: may be null!
	protected final static String PATCH_ID = System.getProperty("patch");
	protected static String RUN_ID;

	// Filter to get only the 3.0 plugins
	class FullSourceProjectsFilter implements FileFilter {
		public boolean accept(File project) {
			if (project.isDirectory()) {
				StringTokenizer tokenizer = new StringTokenizer(project.getName(), ".");
				String token = tokenizer.nextToken();
				if (token.equals("org") && tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					if (token.equals("junit") && !tokenizer.hasMoreTokens()) {
						return true;
					}
					if (token.equals("apache")) {
						token = tokenizer.nextToken();
						if (token.equals("ant") || token.equals("lucene")) {
							return true;
						}
						return false;
					}
					if (token.equals("eclipse") && tokenizer.hasMoreTokens()) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * Initialize log directory.
	 * 
	 * Directory where log files must be put is specified by System property <code>logDir</code>.
	 * For example, if user want to store log files in d:/usr/OTI/tests/perfs/stats,
	 * then he has to specify: -DlogDir=d:/usr/OTI/tests/perfs/stats in VM Arguments of his
	 * performance test launch configuration.
	 * 
	 * CAUTION: Parent directory at least <b>must</b> exist before running test otherwise
	 * it won't be created and times won't be logged.
	 * This was intentional to avoid unexpected log files creation (especially during nightly/integration builds).
	 */
	protected static void initLogDir() {
		String logDir = System.getProperty("logDir");
		File dir = null;
		if (logDir != null) {
			// Verify that parent log dir is valid if exist
			dir = new File(logDir);
			if (dir.exists()) {
				if (!dir.isDirectory()) {
					System.err.println(logDir+" is not a valid directory, log files will NOT be written!");
					dir = INVALID_DIR;
				}
			} else {
				// Create parent dir if necessary
				int n=0;
				boolean created = false;
				while (!created && n<3) {
					created = dir.mkdir();
					if (!created) {
						dir = dir.getParentFile();
					}
					n++;
				}
				if (!created) {
					System.err.println("Cannot create "+logDir+", log files will NOT be written!");
					dir = INVALID_DIR;
				}
			}
			
			// Create Log dir
			String[] subdirs = (PATCH_ID == null) 
				? new String[] {LOG_VERSION, RUN_ID }
				: new String[] {LOG_VERSION, PATCH_ID, RUN_ID };
			for (int i=0; i<subdirs.length; i++) {
				dir = new File(dir, subdirs[i]);
				if (dir.exists()) {
					if (!dir.isDirectory()) {
						System.err.println(dir.getPath()+" is not a valid directory, log files will NOT be written!");
						dir= INVALID_DIR;
						break;
					}
				} else if (!dir.mkdirs()) {
					System.err.println("Cannot create "+dir.getPath()+", log files will NOT be written!");
					dir = INVALID_DIR;
					break;
				}
			}
		}
		LOG_DIR = dir;
	}

	/**
	 * @param name
	 */
	public FullSourceWorkspaceTests(String name) {
		super(name);
	}

	/**
	 * Create test suite for a given TestCase class.
	 * 
	 * Use this method for all JDT/Core performance test using full source workspace.
	 * All test count is computed to know when tests are about to be finished.
	 *
	 * @param testClass TestCase test class
	 * @return test suite
	 */
	static Test buildSuite(Class testClass) {

		// Create tests
		String className = testClass.getName();
		TestSuite suite = new TestSuite(className);
		List tests = buildTestsList(testClass);
		int size = tests.size();
		TESTS_NAME_LIST = new ArrayList(size);
		for (int i=0; i<size; i++) {
			FullSourceWorkspaceTests test = (FullSourceWorkspaceTests)tests.get(i);
			suite.addTest(test);
			TESTS_NAME_LIST.add(test.getName());
		}
		ALL_TESTS_COUNT += suite.testCount();
		
		// Init log dir if necessary
		if (LOG_DIR == null) {
			if (RUN_ID == null) {
				RUN_ID = suiteTypeShortName(testClass);
			}
			initLogDir();
		}
		
		// Return created tests
		return suite;
	}

	/**
	 * Create print streams (one for each type of statistic).
	 * Log file names have all same prefix based on test class name,
	 * include type of statistic stored in it and always have extension ".log".
	 * 
	 * If log file does not exist, then add column headers at the beginning of the file.
	 * 
	 * This method does nothing if log files directory has not been initialized
	 * (which should be the case most of times and especially while running nightly/integration build performance tests).
	 */
	static void createPrintStream(Class testClass, PrintStream[] logStreams, int count, String prefix) {
		if (LOG_DIR != null) {
			for (int i=0, ln=LOG_TYPES.length; i<ln; i++) {
				String suiteTypeName = suiteTypeShortName(testClass);
				File logFile = new File(LOG_DIR, suiteTypeName+'_'+LOG_TYPES[i]+".log");
				try {
					boolean fileExist = logFile.exists();
					logStreams[i] = new PrintStream(new FileOutputStream(logFile, true));
					if (logStreams[i] != null) {
						if (!fileExist) {
							logStreams[i].print("Date  \tTime  \t");
							for (int j=0; j<count; j++) {
								String testName = ((String) TESTS_NAME_LIST.get(j)).substring(4+(prefix==null?0:prefix.length())); // 4="test".length()
								logStreams[i].print(testName+'\t');
							}
							logStreams[i].println("Comment");
							
						}
						// Log date and time
						Date date = new Date(System.currentTimeMillis());
						logStreams[i].print(DateFormat.getDateInstance(3).format(date)+'\t');
						logStreams[i].print(DateFormat.getTimeInstance(3).format(date)+'\t');
						System.out.println("Log file "+logFile.getPath()+" opened.");
					} else {
						System.err.println("Cannot open "+logFile.getPath()+"!!!");
					}
				} catch (FileNotFoundException e) {
					System.err.println("Cannot find file "+logFile.getPath()+"!!!");
				}
			}
		}
	}

	/*
	 * Returns the OS path to the directory that contains this plugin.
	 */
	static String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.eclipse.jdt.core.tests.performance").getEntry("/");
			return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Return a short name for a given suite test.
	 * Typically remove prefix "FullSourceWorkspace" and suffix "Test"
	 */
	protected static String suiteTypeShortName(Class testClass) {
		String className = testClass.getName();
		int startIndex = className.indexOf("FullSourceWorkspace");
		int endIndex = className.lastIndexOf("Test");
		if (startIndex < 0) return null;
		startIndex += "FullSourceWorkspace".length();
		return className.substring(startIndex, endIndex);
	}

	/**
	 * Log test performance result and close stream if it was last one.
	 */
	protected void logPerfResult(PrintStream[] logStreams, int count) {

		// Perfs comment buffers
		String[] comments = new String[2];

		// Log perf result
		boolean haveTimes  = JdtCorePerformanceMeter.CPU_TIMES != null && JdtCorePerformanceMeter.ELAPSED_TIMES != null;
		if (haveTimes) {
			NumberFormat pFormat = NumberFormat.getPercentInstance();
			pFormat.setMaximumFractionDigits(1);
			NumberFormat dFormat = NumberFormat.getNumberInstance();
			dFormat.setMaximumFractionDigits(0);
			String stddevThresholdStr = dFormat.format(STDDEV_THRESHOLD*100);
			NumberFormat dFormat2 = NumberFormat.getNumberInstance();
			dFormat2.setMaximumFractionDigits(2);
			try {
				// Store CPU Time
				JdtCorePerformanceMeter.Statistics cpuStats = (JdtCorePerformanceMeter.Statistics) JdtCorePerformanceMeter.CPU_TIMES.get(this.scenarioReadableName);
				if (cpuStats != null) {
					double percent = cpuStats.stddev/cpuStats.average;
					if (percent > STDDEV_THRESHOLD) {
						//if (logStreams[0] != null) logStreams[0].print("'"); // disable over threshold result for xls table
						System.out.println("	WARNING: CPU time standard deviation is over "+stddevThresholdStr+"%: "+dFormat2.format(cpuStats.stddev)+"/"+cpuStats.average+"="+ pFormat.format(percent));
						comments[0] = "stddev=" + pFormat.format(percent);
					}
					if (logStreams[0] != null) {
						logStreams[0].print(""+cpuStats.sum+"\t");
					}
				} else {
					Thread.sleep(1000);
					System.err.println(this.scenarioShortName+": we should have stored CPU time!");
					Thread.sleep(1000);
				}
				// Store Elapsed time
				JdtCorePerformanceMeter.Statistics elapsedStats = (JdtCorePerformanceMeter.Statistics) JdtCorePerformanceMeter.ELAPSED_TIMES.get(this.scenarioReadableName);
				if (elapsedStats != null) {
					double percent = elapsedStats.stddev/elapsedStats.average;
					if (percent > STDDEV_THRESHOLD) {
						//if (logStreams[1] != null) logStreams[1].print("'"); // disable over threshold result for xls table
						System.out.println("	WARNING: Elapsed time standard deviation is over "+stddevThresholdStr+"%: "+dFormat.format(elapsedStats.stddev)+"/"+elapsedStats.average+"="+ pFormat.format(percent));
						comments[1] = "stddev=" + pFormat.format(percent);
					}
					if (logStreams[1] != null) {
						logStreams[1].print(""+elapsedStats.sum+"\t");
					}
				} else {
					Thread.sleep(1000);
					System.err.println(this.scenarioShortName+": we should have stored Elapsed time");
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				// do nothing
			}
		}

		// Update comment buffers
		StringBuffer[] scenarioComments = (StringBuffer[]) SCENARII_COMMENT.get(getClass());
		if (scenarioComments == null) {
			scenarioComments = new StringBuffer[LOG_TYPES.length];
			SCENARII_COMMENT.put(getClass(), scenarioComments);
		}
		for (int i=0, ln=LOG_TYPES.length; i<ln; i++) {
			if (this.scenarioComment != null || comments[i] != null) {
				if (scenarioComments[i] == null) {
					scenarioComments[i] = new StringBuffer();
				} else {
					scenarioComments[i].append(' ');
				}
				if (this.scenarioComment == null) {
					scenarioComments[i].append("["+TEST_POSITION+"]");
				} else {
					scenarioComments[i].append(this.scenarioComment);
				}
				if (comments[i] != null) {
					if (this.scenarioComment != null) scenarioComments[i].append(',');
					scenarioComments[i].append(comments[i]);
				}
			}
		}

		// Close log
		if (count == 0) {
			for (int i=0, ln=logStreams.length; i<ln; i++) {
				if (logStreams[i] != null) {
					if (haveTimes) {
						if (scenarioComments[i] != null) {
							logStreams[i].print(scenarioComments[i].toString());
						}	
						logStreams[i].println();
					}
					logStreams[i].close();
				}
			}
			TEST_POSITION = 0;
		}
	}

	/**
	 * Perform gc several times to be sure that it won't take time while executing current test.
	 */
	protected void runGc() {
		int iterations = 0;
		long delta=0, free=0;
		for (int i=0; i<MAX_GC; i++) {
			free = Runtime.getRuntime().freeMemory();
			System.gc();
			delta = Runtime.getRuntime().freeMemory() - free;
//			if (DEBUG) System.out.println("Loop gc "+ ++iterations + " (free="+free+", delta="+delta+")");
			try {
				Thread.sleep(TIME_GC);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		if (iterations == MAX_GC && delta > DELTA_GC) {
			// perhaps gc was not well executed
			System.out.println("WARNING: "+this.scenarioShortName+" still get "+delta+" unfreeable memory (free="+free+",total="+Runtime.getRuntime().totalMemory()+") after "+MAX_GC+" gc...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	/**
	 * Override super implementation to:
	 * <ul>
	 *		<li>store scenario names and comment (one scenario per test)</li>
	 *		<li>init workspace if first test run</li>
	 *		<li>increment test position</li>
	 *	</ul>
	 * @see org.eclipse.test.performance.PerformanceTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		// Store scenario readable name
		String scenario = Performance.getDefault().getDefaultScenarioId(this);
		this.scenarioReadableName = scenario.substring(scenario.lastIndexOf('.')+1, scenario.length()-2);
		this.scenarioShortName = this.scenarioReadableName.substring(this.scenarioReadableName.lastIndexOf('#')+5/*1+"test".length()*/, this.scenarioReadableName.length());
		this.scenarioComment = null;

		// Set testing environment if null
		if (ENV == null) {
			ENV = new TestingEnvironment();
			ENV.openEmptyWorkspace();
			setUpFullSourceWorkspace();
//			if (JUNIT_PROJECT == null) {
//				setUpJunitProject();
//			}
		}

		// Verify that all used projects were found in wksp
		assertNotNull("We should have found "+JavaCore.PLUGIN_ID+" project in workspace!!!", JDT_CORE_PROJECT);

		// Increment test position
		TEST_POSITION++;

		// Abort if only touch
		if (TOUCH) {
			String testPrintName = "'"+scenarioShortName+"' test"; 
			System.out.println("Touch "+testPrintName+" to verify that it will run correctly.");
			throw new Error(testPrintName+" execution has been aborted!");
		}

		// Print test name
		System.out.println("================================================================================");
		System.out.println("Running "+this.scenarioReadableName+"...");

		// Time measuring
		this.testDuration = 0;

		// Wait 2 seconds
		Thread.sleep(2000);
	}

	/*
	 * Set up full source workpsace from zip file.
	 */
	private void setUpFullSourceWorkspace() throws IOException, CoreException {
		
		// Get wksp info
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		String targetWorkspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();

		// Get projects directories
		File wkspDir = new File(targetWorkspacePath);
		FullSourceProjectsFilter filter = new FullSourceProjectsFilter();
		File[] directories = wkspDir.listFiles(filter);
		long start = System.currentTimeMillis();
		int dirLength = directories.length;
		if (dirLength != 62) {
			String fullSourceZipPath = getPluginDirectoryPath() + File.separator + "full-source-R3_0.zip";
			System.out.println("Unzipping "+fullSourceZipPath);
			System.out.print("	in "+targetWorkspacePath+"...");
			Util.unzip(fullSourceZipPath, targetWorkspacePath);
			System.out.println(" done in "+(System.currentTimeMillis()-start)+"ms.");
		}

		// Init environment with existing porjects
		System.out.print("Create and open projects in environment...");
		start = System.currentTimeMillis();
		for (int i = 0; i < dirLength; i++) {
			String dirName = directories[i].getName();
			IProject project = workspaceRoot.getProject(dirName);
			if (project.exists()) {
				ENV.addProject(project);
			} else {
				ENV.addProject(dirName);
			}
		}
		System.out.println("("+(System.currentTimeMillis()-start)+"ms)");

		// Init JRE_LIB variable
		String jdkLib = Util.getJavaClassLibs()[0];
		JavaCore.setClasspathVariable("JRE_LIB", new Path(jdkLib), null);
		
		// Set classpaths (workaround bug 73253 Project references not set on project open)
		System.out.print("Set projects classpaths...");
		ALL_PROJECTS = JavaCore.create(workspaceRoot).getJavaProjects();
		int length = ALL_PROJECTS.length;
		for (int i = 0; i < length; i++) {
			String projectName = ALL_PROJECTS[i].getElementName();
			if (JavaCore.PLUGIN_ID.equals(projectName)) {
				JDT_CORE_PROJECT = ALL_PROJECTS[i];
			} else if (BIG_PROJECT_NAME.equals(projectName)) {
				BIG_PROJECT = (JavaProject) ALL_PROJECTS[i];
//			} else if (JUNIT_PROJECT_NAME.equals(projectName)) {
//				JUNIT_PROJECT = ALL_PROJECTS[i];
			}
			ALL_PROJECTS[i].setRawClasspath(ALL_PROJECTS[i].getRawClasspath(), null);
			// Make Big project dependent from jdt.core one
//			IClasspathEntry[] bigProjectEntries = BIG_PROJECT.getRawClasspath();
//			int bpeLength = bigProjectEntries.length;
//			System.arraycopy(bigProjectEntries, 0, bigProjectEntries = new IClasspathEntry[bpeLength+1], 0, bpeLength);
//			bigProjectEntries[bpeLength] = JavaCore.newProjectEntry(JDT_CORE_PROJECT.getPath());
		}
		System.out.println("done");

		// Initialize Parser wokring copy
		IJavaElement element = JDT_CORE_PROJECT.findType("org.eclipse.jdt.internal.compiler.parser.Parser");
		assertTrue("Parser should exist in org.eclipse.jdt.core project!", element != null && element.exists());
		PARSER_WORKING_COPY = (ICompilationUnit) element.getParent();
	}

	/*
	 * Create JUnit project and add it to the workspace
	 *
	private void setUpJunitProject() throws CoreException, IOException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();
	
		// Print for log in case of project creation troubles...
		System.out.println("Create '"+JUNIT_PROJECT_NAME+"' project in "+workspaceRoot.getLocation()+":");
		long start = System.currentTimeMillis();
	
		// Print for log in case of project creation troubles...
		String genericsZipPath = getPluginDirectoryPath() + File.separator + JUNIT_PROJECT_NAME + "src.zip";
		start = System.currentTimeMillis();
		System.out.println("Unzipping "+genericsZipPath);
		System.out.print("	in "+targetWorkspacePath+"...");
	
		// Unzip file
		Util.unzip(genericsZipPath, targetWorkspacePath);
		System.out.println(" "+(System.currentTimeMillis()-start)+"ms.");
	
		// Add project to workspace
		System.out.print("	- add project to full source workspace...");
		start = System.currentTimeMillis();
		ENV.addProject(JUNIT_PROJECT_NAME);
		JUNIT_PROJECT = createJavaProject(JUNIT_PROJECT_NAME, new String[]{ "src" }, "bin", "1.5");
		JUNIT_PROJECT.setRawClasspath(JUNIT_PROJECT.getResolvedClasspath(true), null);
	
		// Print for log in case of project creation troubles...
		System.out.println(" "+(System.currentTimeMillis()-start)+"ms.");
	}
	*/

	/**
	 * @deprecated Use {@link #tagAsGlobalSummary(String,Dimension,boolean)} instead
	 */
	public void tagAsGlobalSummary(String shortName, Dimension dimension) {
		tagAsGlobalSummary(shortName, dimension, false); // do NOT put in fingerprint
	}
	protected void tagAsGlobalSummary(String shortName, boolean fingerprint) {
		tagAsGlobalSummary(shortName, Dimension.ELAPSED_PROCESS, fingerprint);
	}
	protected void tagAsGlobalSummary(String shortName, Dimension dimension, boolean fingerprint) {
		if (DEBUG) System.out.println(shortName);
		if (fingerprint) super.tagAsGlobalSummary(shortName, dimension);
	}
	/**
	 * @deprecated We do not use this method...
	 */
	public void tagAsGlobalSummary(String shortName, Dimension[] dimensions) {
		System.out.println("ERROR: tagAsGlobalSummary(String, Dimension[]) is not implemented!!!");
	}
	/**
	 * @deprecated Use {@link #tagAsSummary(String,Dimension,boolean)} instead
	 */
	public void tagAsSummary(String shortName, Dimension dimension) {
		tagAsSummary(shortName, dimension, false); // do NOT put in fingerprint
	}
	protected void tagAsSummary(String shortName, boolean fingerprint) {
		tagAsSummary(shortName, Dimension.ELAPSED_PROCESS, fingerprint);
	}
	public void tagAsSummary(String shortName, Dimension dimension, boolean fingerprint) {
		if (DEBUG) System.out.println(shortName);
		if (fingerprint) super.tagAsSummary(shortName, dimension);
	}
	/**
	 * @deprecated We do not use this method...
	 */
	public void tagAsSummary(String shortName, Dimension[] dimensions) {
		System.out.println("ERROR: tagAsGlobalSummary(String, Dimension[]) is not implemented!!!");
	}
	public void tagAsSummary(String shortName, Dimension[] dimensions, boolean fingerprint) {
		if (DEBUG) System.out.println(shortName);
		if (fingerprint) super.tagAsSummary(shortName, dimensions);
	}
	public void startMeasuring() {
		super.startMeasuring();
		this.startMeasuring = System.currentTimeMillis();
	}
	public void stopMeasuring() {
		super.stopMeasuring();
		this.testDuration += System.currentTimeMillis() - this.startMeasuring;
	}
	public void commitMeasurements() {
		if (PRINT) System.out.println("	Test duration = "+this.testDuration+"ms");
		super.commitMeasurements();
	}
	/**
	 * Override super implementation to:
	 *	<ul>
	 *		<li>decrement all test count</li>
	 *		<li>reset workspace and back to initial options if last test run</li>
	 *</ul>
	 * @see org.eclipse.test.performance.PerformanceTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		ALL_TESTS_COUNT--;
		if (ALL_TESTS_COUNT == 0) {
//			ENV.resetWorkspace();
			JavaCore.setOptions(INITIAL_OPTIONS);
		}
		super.tearDown();
	}

	/**
	 * Start a build on given projkect or workspace using given options.
	 * 
	 * @param javaProject Project which must be (full) build or null if all workspace has to be built.
	 * @param options Options used while building
	 */
	protected void build(final IJavaProject javaProject, Hashtable options, boolean noWarning) throws IOException, CoreException {
		if (DEBUG) System.out.print("\tstart build...");
		JavaCore.setOptions(options);
		if (PRINT) System.out.println("JavaCore options: "+options);

		// Build workspace if no project
		if (javaProject == null) {
			// single measure
			runGc();
			startMeasuring();
			ENV.fullBuild();
			stopMeasuring();
		} else {
			if (PRINT) System.out.println("Project options: "+javaProject.getOptions(false));
			// warm-up
			ENV.fullBuild(javaProject.getProject().getName());
			
			// measures
			int max = MEASURES_COUNT / 2;
			for (int i=0; i<max; i++) {
				runGc();
				startMeasuring();
				IWorkspaceRunnable compilation = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						ENV.fullBuild(javaProject.getPath());
					}
				};
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				if (workspace.isTreeLocked()) {
					compilation.run(null/*no progress available*/);
				} else {
					workspace.run(
						compilation,
						null/*don't take any lock*/,
						IWorkspace.AVOID_UPDATE,
						null/*no progress available here*/);
				}
				stopMeasuring();
			}
		}
		
		// Verify markers
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IMarker[] markers = workspaceRoot.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		List resources = new ArrayList();
		List messages = new ArrayList();
		int warnings = 0;
		for (int i = 0, length = markers.length; i < length; i++) {
			IMarker marker = markers[i];
			switch (((Integer) marker.getAttribute(IMarker.SEVERITY)).intValue()) {
				case IMarker.SEVERITY_ERROR:
					resources.add(marker.getResource().getName());
					messages.add(marker.getAttribute(IMarker.MESSAGE));
					break;
				case IMarker.SEVERITY_WARNING:
					warnings++;
					if (noWarning) {
						resources.add(marker.getResource().getName());
						messages.add(marker.getAttribute(IMarker.MESSAGE));
					}
					break;
			}
		}
		workspaceRoot.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

		// Assert result
		int size = messages.size();
		if (size > 0) {
			StringBuffer debugBuffer = new StringBuffer();
			for (int i=0; i<size; i++) {
				debugBuffer.append(resources.get(i));
				debugBuffer.append(":\n\t");
				debugBuffer.append(messages.get(i));
				debugBuffer.append('\n');
			}
			System.out.println(this.scenarioShortName+": Unexpected ERROR marker(s):\n" + debugBuffer.toString());
			System.out.println("--------------------");
		}
		if (DEBUG) System.out.println("done");
		
		// Commit measure
		commitMeasurements();
		assertPerformance();
	
		// Store warning
		if (warnings>0) {
			System.out.println("\t- "+warnings+" warnings found while performing build.");
		}
		if (this.scenarioComment == null) {
			this.scenarioComment = new StringBuffer("["+TEST_POSITION+"]");
		} else {
			this.scenarioComment.append(' ');
		}
		this.scenarioComment.append("warn=");
		this.scenarioComment.append(warnings);
	}

	/*
	 * Delete a directory from file system.
	 * @param directory
	 *
	protected void cleanupDirectory(File directory) {
		if (!directory.isDirectory() || !directory.exists()) {
			return;
		}
		String[] fileNames = directory.list();
		for (int i = 0; i < fileNames.length; i++) {
			File file = new File(directory, fileNames[i]);
			if (file.isDirectory()) {
				cleanupDirectory(file);
			} else {
				if (!file.delete())
					System.out.println("Could not delete file " + file.getPath()); //$NON-NLS-1$
			}
		}
		if (!directory.delete())
			System.out.println("Could not delete directory " + directory.getPath()); //$NON-NLS-1$
	}
	*/

	/*
	 * Clear given options
	 */
	Map clearOptions(Map options) {
		// turn all errors and warnings into ignore. The customizable set of compiler
		// options only contains additional Eclipse options. The standard JDK compiler
		// options can't be changed anyway.
		for (Iterator iter= options.keySet().iterator(); iter.hasNext();) {
			String key= (String)iter.next();
			String value= (String)options.get(key);
			if ("error".equals(value) || "warning".equals(value)) {  //$NON-NLS-1$//$NON-NLS-2$
				// System.out.println("Ignoring - " + key);
				options.put(key, "ignore"); //$NON-NLS-1$
			} else if ("enabled".equals(value)) {
				// System.out.println("	- disabling " + key);
				options.put(key, "disabled");
			}
		}
		options.put(JavaCore.COMPILER_TASK_TAGS, "");
		return options;
	}

	/*
	 * Full Build using batch compiler
	 */
	protected void compile(String pluginID, String options, boolean log, String[] srcPaths) throws IOException, CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getProject(pluginID).getLocation().toFile().getCanonicalPath();
		String logFileName = targetWorkspacePath + File.separator + getName()+".log";
		String workspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath()+File.separator;
		String binPath = File.separator+"bin"+File.pathSeparator;
		String classpath = " -cp " +
			workspacePath+"org.eclipse.osgi" + binPath +
			workspacePath+"org.eclipse.jface" + binPath +
			workspacePath+"org.eclipse.core.runtime" + binPath +
			workspacePath+"org.eclipse.core.resources"+binPath +
			workspacePath+"org.eclipse.text"+binPath;
		String sources = srcPaths == null ? " "+targetWorkspacePath : "";
		if (srcPaths != null) {
			for (int i=0, l=srcPaths.length; i<l; i++) {
				String path = workspacePath + pluginID + File.separator + srcPaths[i];
				if (path.indexOf(" ") > 0) {
					path = "\"" + path + "\"";
				}
				sources += " " + path;
			}
		}

		// Warm up
		String compliance = " -" + (COMPLIANCE==null ? "1.4" : COMPLIANCE);
		final String cmdLine = classpath + compliance + " -g -preserveAllLocals "+(options==null?"":options)+" -d " + COMPILER_OUTPUT_DIR + (log?" -log "+logFileName:"") + sources;
		if (PRINT) System.out.println("	Compiler command line = "+cmdLine);
		int warnings = 0;
		StringWriter errStrWriter = new StringWriter();
		PrintWriter err = new PrintWriter(errStrWriter);
		PrintWriter out = new PrintWriter(new StringWriter());
		Main warmup = new Main(out, err, false);
		warmup.compile(Main.tokenize(cmdLine));
		if (warmup.globalErrorsCount > 0) {
			System.out.println(this.scenarioShortName+": "+warmup.globalErrorsCount+" Unexpected compile ERROR!");
			if (DEBUG) {
				System.out.println(errStrWriter.toString());
				System.out.println("--------------------");
			}
		}
		if (!"none".equals(COMPILER_OUTPUT_DIR)) {
			Util.delete(COMPILER_OUTPUT_DIR);
		}
		warnings = warmup.globalWarningsCount;
		if (!log) Util.writeToFile(errStrWriter.toString(), logFileName);

		// Clean writer
		err = null;
		out = null;
		errStrWriter = null;

		// Measures
		for (int i = 0; i < MEASURES_COUNT; i++) {
			runGc();
			NullPrintWriter nullPrint= new NullPrintWriter();
			startMeasuring();
			final Main main = new Main(nullPrint, nullPrint, false);
			IWorkspaceRunnable compilation = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					main.compile(Main.tokenize(cmdLine));
				}
			};
			if (workspace.isTreeLocked()) {
				compilation.run(null/*no progress available*/);
			} else {
				workspace.run(
					compilation,
					null/*don't take any lock*/,
					IWorkspace.AVOID_UPDATE,
					null/*no progress available here*/);
			}
			stopMeasuring();
			if (!"none".equals(COMPILER_OUTPUT_DIR)) {
				Util.delete(COMPILER_OUTPUT_DIR);
			}
		}
		
		// Commit measures
		commitMeasurements();
		assertPerformance();

		// Store warning
		if (warnings>0) {
			System.out.println("\t- "+warnings+" warnings found while performing batch compilation.");
		}
		if (this.scenarioComment == null) {
			this.scenarioComment = new StringBuffer("["+TEST_POSITION+"]");
		} else {
			this.scenarioComment.append(' ');
		}
		this.scenarioComment.append("warn=");
		this.scenarioComment.append(warnings);
	}

	/**
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#createJavaProject(String, String[], String[], String[][], String[][], String[], String[][], String[][], boolean[], String, String[], String[][], String[][], String)
	 */
	protected IJavaProject createJavaProject(final String projectName, final String[] sourceFolders, final String projectOutput, final String compliance) throws CoreException {
		final IJavaProject[] result = new IJavaProject[1];
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				
				// create classpath entries 
				IProject project = ENV.getProject(projectName);
				IPath projectPath = project.getFullPath();
				int sourceLength = sourceFolders == null ? 0 : sourceFolders.length;
				IClasspathEntry[] entries = new IClasspathEntry[sourceLength+1];
				for (int i= 0; i < sourceLength; i++) {
					IPath sourcePath = new Path(sourceFolders[i]);
					int segmentCount = sourcePath.segmentCount();
					if (segmentCount > 0) {
						// create folder and its parents
						IContainer container = project;
						for (int j = 0; j < segmentCount; j++) {
							IFolder folder = container.getFolder(new Path(sourcePath.segment(j)));
							if (!folder.exists()) {
								folder.create(true, true, null);
							}
							container = folder;
						}
					}
					// create source entry
					entries[i] = 
						JavaCore.newSourceEntry(
							projectPath.append(sourcePath), 
							new IPath[0],
							new IPath[0], 
							null
						);
				}
				
				// Add JRE_LIB entry
				entries[sourceLength] = JavaCore.newVariableEntry(
								new Path("JRE_LIB"),
								null,
								null);

				// create project's output folder
				IPath outputPath = new Path(projectOutput);
				if (outputPath.segmentCount() > 0) {
					IFolder output = project.getFolder(outputPath);
					if (!output.exists()) {
						output.create(true, true, null);
					}
				}
				
				// set classpath and output location
				IJavaProject javaProject = ENV.getJavaProject(projectName);
				javaProject.setRawClasspath(entries, projectPath.append(outputPath), null);
				
				// set compliance level options
				if ("1.5".equals(compliance)) {
					Map options = new HashMap();
					options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
					options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
					options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
					javaProject.setOptions(options);
				}
				
				result[0] = javaProject;
			}
		};
		ResourcesPlugin.getWorkspace().run(create, null);	
		return result[0];
	}

	private void collectAllFiles(File root, ArrayList collector, FileFilter fileFilter) {
		File[] files = root.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			final File currentFile = files[i];
			if (currentFile.isDirectory()) {
				collectAllFiles(currentFile, collector, fileFilter);
			} else {
				collector.add(currentFile);
			}
		}
	}

	protected File[] getAllFiles(File root, FileFilter fileFilter) {
		ArrayList files = new ArrayList();
		if (root.isDirectory()) {
			collectAllFiles(root, files, fileFilter);
			File[] result = new File[files.size()];
			files.toArray(result);
			return result;
		} else {
			return null;
		}
	}

	/**
	 * Returns the specified compilation unit in the given project, root, and
	 * package fragment or <code>null</code> if it does not exist.
	 */
	protected IClassFile getClassFile(IJavaProject project, String rootPath, String packageName, String className) throws JavaModelException {
		IPackageFragment pkg= getPackageFragment(project, rootPath, packageName);
		if (pkg == null) {
			return null;
		}
		return pkg.getClassFile(className);
	}

	/**
	 * Returns compilation unit with given name in given project and package.
	 * @param projectName
	 * @param packageName
	 * @param unitName
	 * @return org.eclipse.jdt.core.ICompilationUnit
	 */
	protected ICompilationUnit getCompilationUnit(String projectName, String packageName, String unitName) throws JavaModelException {
		IJavaProject javaProject = getProject(projectName);
		if (javaProject == null) return null;
		IType type = javaProject.findType(packageName, unitName);
		if (type != null) {
			return type.getCompilationUnit();
		}
		return null;
	}

	/**
	 * Returns the specified package fragment in the given project and root, or
	 * <code>null</code> if it does not exist.
	 * The rootPath must be specified as a project relative path. The empty
	 * path refers to the default package fragment.
	 */
	protected IPackageFragment getPackageFragment(IJavaProject project, String rootPath, String packageName) throws JavaModelException {
		IPackageFragmentRoot root= getPackageFragmentRoot(project, rootPath);
		if (root == null) {
			return null;
		}
		return root.getPackageFragment(packageName);
	}

	/**
	 * Returns the specified package fragment root in the given project, or
	 * <code>null</code> if it does not exist.
	 * If relative, the rootPath must be specified as a project relative path. 
	 * The empty path refers to the package fragment root that is the project
	 * folder iteslf.
	 * If absolute, the rootPath refers to either an external jar, or a resource 
	 * internal to the workspace
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(
		IJavaProject project, 
		String rootPath)
		throws JavaModelException {

		if (project == null) {
			return null;
		}
		IPath path = new Path(rootPath);
		if (path.isAbsolute()) {
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IResource resource = workspaceRoot.findMember(path);
			IPackageFragmentRoot root;
			if (resource == null) {
				// external jar
				root = project.getPackageFragmentRoot(rootPath);
			} else {
				// resource in the workspace
				root = project.getPackageFragmentRoot(resource);
			}
			return root;
		} else {
			IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
			if (roots == null || roots.length == 0) {
				return null;
			}
			for (int i = 0; i < roots.length; i++) {
				IPackageFragmentRoot root = roots[i];
				if (!root.isExternal()
					&& root.getUnderlyingResource().getProjectRelativePath().equals(path)) {
					return root;
				}
			}
		}
		return getExternalJarFile(project, rootPath);
	}

	/**
	 * Returns project corresponding to given name or null if none is found.
	 * @param projectName
	 * @return IJavaProject
	 */
	protected IJavaProject getProject(String projectName) {
		for (int i=0, length = ALL_PROJECTS.length; i<length; i++) {
			if (ALL_PROJECTS[i].getElementName().equals(projectName))
				return ALL_PROJECTS[i];
		}
		return null;
	}

	/**
	 * Returns all compilation units of a given project.
	 * @param javaProject Project to collect units
	 * @return List of org.eclipse.jdt.core.ICompilationUnit
	 */
	protected List getProjectCompilationUnits(IJavaProject javaProject) throws JavaModelException {
		IPackageFragmentRoot[] fragmentRoots = javaProject.getPackageFragmentRoots();
		int length = fragmentRoots.length;
		List allUnits = new ArrayList();
		for (int i=0; i<length; i++) {
			if (fragmentRoots[i] instanceof JarPackageFragmentRoot) continue;
			IJavaElement[] packages= fragmentRoots[i].getChildren();
			for (int k= 0; k < packages.length; k++) {
				IPackageFragment pack = (IPackageFragment) packages[k];
				ICompilationUnit[] units = pack.getCompilationUnits();
				for (int u=0; u<units.length; u++) {
					allUnits.add(units[u]);
				}
			}
		}
		return allUnits;
	}

	protected IPackageFragmentRoot getExternalJarFile(IJavaProject project, String jarSimpleName) throws JavaModelException {
		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
		if (roots == null || roots.length == 0) {
			return null;
		}
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			if (root.isExternal() && root.getElementName().equals(jarSimpleName)) {
				return root;
			}
		}		
		return null;
	}

	/**
	 * Split a list of compilation units in several arrays.
	 * @param units List of org.eclipse.jdt.core.ICompilationUnit
	 * @param splitSize Size of the arrays
	 * @return List of ICompilationUnit[]
	 */
	protected List splitListInSmallArrays(List units, int splitSize) throws JavaModelException {
		int size = units.size();
		if (size == 0) return Collections.EMPTY_LIST;
		int length = size / splitSize;
		int remind = size%splitSize;
		List splitted = new ArrayList(remind==0?length:length+1);
		if (length == 0) {
			ICompilationUnit[] sublist = new ICompilationUnit[size];
			units.toArray(sublist);
			splitted.add(sublist);
			return splitted;
		}
		int ptr = 0;
		for (int i= 0; i<length; i++){
			ICompilationUnit[] sublist = new ICompilationUnit[splitSize];
			units.subList(ptr, ptr+splitSize).toArray(sublist);
			splitted.add(sublist);
			ptr += splitSize;
		}
		if (remind > 0) {
			if (remind< 10) {
				ICompilationUnit[] lastList = (ICompilationUnit[]) splitted.remove(length-1);
				System.arraycopy(lastList, 0, lastList = new ICompilationUnit[splitSize+remind], 0, splitSize);
				for (int i=ptr, j=splitSize; i<size; i++, j++) {
					lastList[j] = (ICompilationUnit) units.get(i);
				}
				splitted.add(lastList);
			} else {
				ICompilationUnit[] sublist = new ICompilationUnit[remind];
				units.subList(ptr, size).toArray(sublist);
				splitted.add(sublist);
			}
		}
		return splitted;
	}

	/*
	 * Create hashtable of none or all warning options.
	 * Possible kind:
	 * 	-1: no options
	 *  0: default options
	 *  1: all options
	 */
	protected Hashtable warningOptions(int kind) {

		// Values
		Hashtable optionsMap = JavaCore.getDefaultOptions();
		if (kind == 0) {
			// Default set since 3.1
			optionsMap.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.IGNORE); 
		} else {
			clearOptions(optionsMap);
			boolean all = kind == 1;
			String generate = all ? CompilerOptions.GENERATE : CompilerOptions.DO_NOT_GENERATE;
			String warning = all ? CompilerOptions.WARNING : CompilerOptions.IGNORE;
			String enabled = all ? CompilerOptions.ENABLED : CompilerOptions.DISABLED;
			String preserve = all ? CompilerOptions.OPTIMIZE_OUT : CompilerOptions.PRESERVE;
			
			// Set options values
			optionsMap.put(CompilerOptions.OPTION_LocalVariableAttribute, generate); 
			optionsMap.put(CompilerOptions.OPTION_LineNumberAttribute, generate);
			optionsMap.put(CompilerOptions.OPTION_SourceFileAttribute, generate);
			optionsMap.put(CompilerOptions.OPTION_PreserveUnusedLocal, preserve);
			optionsMap.put(CompilerOptions.OPTION_DocCommentSupport, enabled); 
			optionsMap.put(CompilerOptions.OPTION_ReportMethodWithConstructorName, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportDeprecation, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, enabled); 
			optionsMap.put(CompilerOptions.OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, enabled); 
			optionsMap.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportUnusedLocal, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameter, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportUnusedImport, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportNoEffectAssignment, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportNoImplicitStringConversion, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportIncompatibleNonInheritedInterfaceMethod, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportLocalVariableHiding, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportFieldHiding, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportEmptyStatement, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportAssertIdentifier, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportUnnecessaryElse, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadoc, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, enabled); 
			optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocTags, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocComments, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportFinallyBlockNotCompletingNormally, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, warning); 
			optionsMap.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, warning); 
			optionsMap.put(CompilerOptions.OPTION_TaskTags, all ? JavaCore.DEFAULT_TASK_TAGS : "");
			optionsMap.put(CompilerOptions.OPTION_TaskPriorities, all ? JavaCore.DEFAULT_TASK_PRIORITIES : "");
			optionsMap.put(CompilerOptions.OPTION_TaskCaseSensitive, enabled); 
			optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, enabled); 
			optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, enabled); 
			optionsMap.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, enabled); 
			optionsMap.put(CompilerOptions.OPTION_InlineJsr, enabled);
		}

		// Ignore 3.1 options
		optionsMap.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE); 
		optionsMap.put(CompilerOptions.OPTION_ReportEnumIdentifier, CompilerOptions.IGNORE); 

		// Ignore 3.2 options
		optionsMap.put(CompilerOptions.OPTION_ReportUnusedLabel, CompilerOptions.IGNORE); 

		// Set compliance
		String compliance= compliance();
		if (compliance != null) {
			optionsMap.put(CompilerOptions.OPTION_Compliance, compliance);
			optionsMap.put(CompilerOptions.OPTION_Source, compliance);
			optionsMap.put(CompilerOptions.OPTION_TargetPlatform, compliance);
		}

		// Return created options map
		return optionsMap;
	}
}
