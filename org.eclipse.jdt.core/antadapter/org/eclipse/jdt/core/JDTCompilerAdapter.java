/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.eclipse.jdt.internal.antadapter.AntAdapterMessages;

/**
 * Ant 1.5 compiler adapter for the Eclipse Java compiler. This adapter permits the
 * Eclipse Java compiler to be used with the <code>javac</code> task in Ant scripts. In order
 * to use it, just set the property <code>build.compiler</code> as follows:
 * <p>
 * <code>&lt;property name="build.compiler" value="org.eclipse.jdt.core.JDTCompilerAdapter"/&gt;</code>
 * </p>
 * <p>
 * For more information on Ant check out the website at http://jakarta.apache.org/ant/ .
 * </p>
 * 
 * @since 2.0
 */
public class JDTCompilerAdapter extends DefaultCompilerAdapter {
	private static String compilerClass = "org.eclipse.jdt.internal.compiler.batch.Main"; //$NON-NLS-1$
	String logFileName;
	
	/**
	 * Performs a compile using the JDT batch compiler
	 * @throws BuildException if anything wrong happen during the compilation
	 * @return boolean true if the compilation is ok, false otherwise
	 */
	public boolean execute() throws BuildException {
		attributes.log(AntAdapterMessages.getString("ant.jdtadapter.info.usingJDTCompiler"), Project.MSG_VERBOSE); //$NON-NLS-1$
		Commandline cmd = setupJavacCommand();

		try {
			Class c = Class.forName(compilerClass);
			Constructor batchCompilerConstructor = c.getConstructor(new Class[] { PrintWriter.class, PrintWriter.class, Boolean.TYPE});
			Object batchCompilerInstance = batchCompilerConstructor.newInstance(new Object[] {new PrintWriter(System.out), new PrintWriter(System.err), new Boolean(true)});
			Method compile = c.getMethod("compile", new Class[] {String[].class}); //$NON-NLS-1$
			Object result = compile.invoke(batchCompilerInstance, new Object[] { cmd.getArguments()});
			final boolean resultValue = ((Boolean) result).booleanValue();
			if (!resultValue && verbose) {
				System.out.println(AntAdapterMessages.getString("ant.jdtadapter.error.compilationFailed", this.logFileName)); //$NON-NLS-1$
			}
			return resultValue;
		} catch (ClassNotFoundException cnfe) {
			throw new BuildException(AntAdapterMessages.getString("ant.jdtadapter.error.cannotFindJDTCompiler")); //$NON-NLS-1$
		} catch (Exception ex) {
			throw new BuildException(ex);
		}
	}
	
	
	protected Commandline setupJavacCommand() throws BuildException {
		Commandline cmd = new Commandline();
		
		/*
		 * This option is used to never exit at the end of the ant task. 
		 */
		cmd.createArgument().setValue("-noExit"); //$NON-NLS-1$

        if (bootclasspath != null && bootclasspath.size() != 0) {
			/*
			 * Set the bootclasspath for the Eclipse compiler.
			 */
			cmd.createArgument().setValue("-bootclasspath"); //$NON-NLS-1$
			cmd.createArgument().setPath(bootclasspath);        	
        } else {
            includeJavaRuntime = true;
        }

        Path classpath = new Path(project);

       /*
         * Eclipse compiler doesn't support -extdirs.
         * It is emulated using the classpath. We add extdirs entries after the 
         * bootclasspath.
         */
        addExtdirs(extdirs, classpath);

		/*
		 * The java runtime is already handled, so we simply want to retrieve the
		 * ant runtime and the compile classpath.
		 */
        classpath.append(getCompileClasspath());

        // For -sourcepath, use the "sourcepath" value if present.
        // Otherwise default to the "srcdir" value.
        Path sourcepath = null;
        
        // retrieve the method getSourcepath() using reflect
        // This is done to improve the compatibility to ant 1.5
        Class javacClass = Javac.class;
        Method getSourcepathMethod = null;
        try {
	        getSourcepathMethod = javacClass.getMethod("getSourcepath", null); //$NON-NLS-1$
        } catch(NoSuchMethodException e) {
        	// if not found, then we cannot use this method (ant 1.5)
        }
        Path compileSourcePath = null;
        if (getSourcepathMethod != null) {
	 		try {
				compileSourcePath = (Path) getSourcepathMethod.invoke(attributes, null);
			} catch (IllegalAccessException e) {
				// should never happen
			} catch (InvocationTargetException e) {
				// should never happen
			}
        }
        if (compileSourcePath != null) {
            sourcepath = compileSourcePath;
        } else {
            sourcepath = src;
        }
		classpath.append(sourcepath);
		/*
		 * Set the classpath for the Eclipse compiler.
		 */
		cmd.createArgument().setValue("-classpath"); //$NON-NLS-1$
		cmd.createArgument().setPath(classpath);

        String memoryParameterPrefix = JavaEnvUtils.getJavaVersion().equals(JavaEnvUtils.JAVA_1_1) ? "-J-" : "-J-X";//$NON-NLS-1$//$NON-NLS-2$
        if (memoryInitialSize != null) {
            if (!attributes.isForkedJavac()) {
                attributes.log(AntAdapterMessages.getString("ant.jdtadapter.info.ignoringMemoryInitialSize"), Project.MSG_WARN); //$NON-NLS-1$
            } else {
                cmd.createArgument().setValue(memoryParameterPrefix
                                              + "ms" + memoryInitialSize); //$NON-NLS-1$
            }
        }

        if (memoryMaximumSize != null) {
            if (!attributes.isForkedJavac()) {
                attributes.log(AntAdapterMessages.getString("ant.jdtadapter.info.ignoringMemoryMaximumSize"), Project.MSG_WARN); //$NON-NLS-1$
            } else {
                cmd.createArgument().setValue(memoryParameterPrefix
                                              + "mx" + memoryMaximumSize); //$NON-NLS-1$
            }
        }

        if (debug) {
	       // retrieve the method getSourcepath() using reflect
	        // This is done to improve the compatibility to ant 1.5
	        Method getDebugLevelMethod = null;
	        try {
		        getDebugLevelMethod = javacClass.getMethod("getDebugLevel", null); //$NON-NLS-1$
	        } catch(NoSuchMethodException e) {
	        	// if not found, then we cannot use this method (ant 1.5)
	        	// debug level is only available with ant 1.5.x
	        }
     	    String debugLevel = null;
	        if (getDebugLevelMethod != null) {
				try {
					debugLevel = (String) getDebugLevelMethod.invoke(attributes, null);
				} catch (IllegalAccessException e) {
					// should never happen
				} catch (InvocationTargetException e) {
					// should never happen
				}
        	}
			if (debugLevel != null) {
				if (debugLevel.length() == 0) {
					cmd.createArgument().setValue("-g:none"); //$NON-NLS-1$
				} else {
					cmd.createArgument().setValue("-g:" + debugLevel); //$NON-NLS-1$
				}
			} else {
				cmd.createArgument().setValue("-g"); //$NON-NLS-1$
            }
        } else {
            cmd.createArgument().setValue("-g:none"); //$NON-NLS-1$
        }
        
       // retrieve the method getCurrentCompilerArgs() using reflect
        // This is done to improve the compatibility to ant 1.5
        Method getCurrentCompilerArgsMethod = null;
        try {
	        getCurrentCompilerArgsMethod = javacClass.getMethod("getCurrentCompilerArgs", null); //$NON-NLS-1$
        } catch(NoSuchMethodException e) {
        	// if not found, then we cannot use this method (ant 1.5)
        	// debug level is only available with ant 1.5.x
        }
 	    String[] compilerArgs = null;
        if (getCurrentCompilerArgsMethod != null) {
			try {
				compilerArgs = (String[]) getCurrentCompilerArgsMethod.invoke(attributes, null);
			} catch (IllegalAccessException e) {
				// should never happen
			} catch (InvocationTargetException e) {
				// should never happen
			}
    	}
    	
	   	if (compilerArgs == null) {
			/*
			 * Handle the nowarn option. If none, then we generate all warnings.
			 */
			if (attributes.getNowarn()) {
				if (deprecation) {
					cmd.createArgument().setValue("-warn:allDeprecation"); //$NON-NLS-1$
				} else {
					cmd.createArgument().setValue("-nowarn"); //$NON-NLS-1$
				}
			} else if (deprecation) {
				cmd.createArgument().setValue("-warn:allDeprecation,constructorName,packageDefaultMethod,maskedCatchBlocks,unusedImports,staticReceiver"); //$NON-NLS-1$
			} else {
				cmd.createArgument().setValue("-warn:constructorName,packageDefaultMethod,maskedCatchBlocks,unusedImports,staticReceiver"); //$NON-NLS-1$
			}
    	} else {
			/*
			 * Handle the nowarn option. If none, then we generate all warnings.
			 */
			if (attributes.getNowarn()) {
				if (deprecation) {
					cmd.createArgument().setValue("-warn:allDeprecation"); //$NON-NLS-1$
				} else {
					cmd.createArgument().setValue("-nowarn"); //$NON-NLS-1$
				}
			} else if (deprecation) {
				cmd.createArgument().setValue("-warn:allDeprecation"); //$NON-NLS-1$
			} else if (compilerArgs.length == 0) {
				cmd.createArgument().setValue("-warn:constructorName,packageDefaultMethod,maskedCatchBlocks,unusedImports,staticReceiver"); //$NON-NLS-1$
			}
	        /*
			 * Add extra argument on the command line
			 */
			if (compilerArgs.length != 0) {
		        cmd.addArguments(compilerArgs);
			}
	   	}

	   	/*
		 * destDir option.
		 */		
		if (destDir != null) {
			cmd.createArgument().setValue("-d"); //$NON-NLS-1$
			cmd.createArgument().setFile(destDir.getAbsoluteFile());
		}

		/*
		 * target option.
		 */		
		if (target != null) {
			cmd.createArgument().setValue("-target"); //$NON-NLS-1$
			cmd.createArgument().setValue(target);
		}

		/*
		 * verbose option
		 */
		if (verbose) {
			cmd.createArgument().setValue("-verbose"); //$NON-NLS-1$
			/*
			 * extra option allowed by the Eclipse compiler
			 */
			cmd.createArgument().setValue("-log"); //$NON-NLS-1$
			logFileName = destDir.getAbsolutePath() + ".log"; //$NON-NLS-1$
			cmd.createArgument().setValue(logFileName);
		}

		/*
		 * failnoerror option
		 */
		if (!attributes.getFailonerror()) {
			cmd.createArgument().setValue("-proceedOnError"); //$NON-NLS-1$
		}

		/*
		 * source option
		 */
		String source = attributes.getSource();
        if (source != null) {
            cmd.createArgument().setValue("-source"); //$NON-NLS-1$
            cmd.createArgument().setValue(source);
        }
        
		if (JavaEnvUtils.getJavaVersion().equals(JavaEnvUtils.JAVA_1_4)) {
			if (target != null && target.equals("1.1")) {			   //$NON-NLS-1$	
				cmd.createArgument().setValue("-1.3"); //$NON-NLS-1$
			} else {
				cmd.createArgument().setValue("-1.4"); //$NON-NLS-1$
			}
		} else {
			cmd.createArgument().setValue("-1.3"); //$NON-NLS-1$
		}
		
		/*
		 * encoding option
		 */
        if (encoding != null) {
            cmd.createArgument().setValue("-encoding"); //$NON-NLS-1$
            cmd.createArgument().setValue(encoding);
        }

     	/*
		 * Eclipse compiler doesn't have a -sourcepath option. This is
		 * handled through the javac task that collects all source files in
		 * srcdir option.
		 */        
        logAndAddFilesToCompile(cmd);
		return cmd;
	}
	
    /**
     * Emulation of extdirs feature in java >= 1.2.
     * This method adds all files in the given
     * directories (but not in sub-directories!) to the classpath,
     * so that you don't have to specify them all one by one.
     * @param extdirs - Path to append files to
     */
    private void addExtdirs(Path extDirs, Path classpath) {
        if (extDirs == null) {
            String extProp = System.getProperty("java.ext.dirs"); //$NON-NLS-1$
            if (extProp != null) {
                extDirs = new Path(classpath.getProject(), extProp);
            } else {
                return;
            }
        }

        String[] dirs = extDirs.list();
        for (int i = 0; i < dirs.length; i++) {
            File dir = classpath.getProject().resolveFile(dirs[i]);
            if (dir.exists() && dir.isDirectory()) {
                FileSet fs = new FileSet();
                fs.setDir(dir);
                fs.setIncludes("*"); //$NON-NLS-1$
                classpath.addFileset(fs);
            }
        }
    }
}
