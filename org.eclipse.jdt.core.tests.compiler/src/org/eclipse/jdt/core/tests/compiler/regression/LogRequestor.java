package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

public class LogRequestor extends Requestor {
	String problemLog = "";
public LogRequestor(org.eclipse.jdt.internal.compiler.IProblemFactory problemFactory, String outputPath) {
	super(problemFactory, outputPath, false);
}
public void acceptResult(CompilationResult compilationResult) {
	StringBuffer buffer = new StringBuffer(100);
	hasErrors |= compilationResult.hasErrors();
	if (compilationResult.hasProblems()) {
		IProblem[] problems = compilationResult.getProblems();
		int count = problems.length;
		int problemCount = 0;
		for (int i = 0; i < count; i++) { 
			if (problems[i] != null) {
				if (problemCount == 0)
					buffer.append("----------\n");
				problemCount++;
				buffer.append(problemCount + (problems[i].isError() ? ". ERROR" : ". WARNING"));
				buffer.append(" in " + new String(problems[i].getOriginatingFileName()));
				try {
					buffer.append(((DefaultProblem)problems[i]).errorReportSource(compilationResult.compilationUnit));
					buffer.append("\n");
					buffer.append(problems[i].getMessage());
					buffer.append("\n");
				} catch (Exception e) {
				}
				buffer.append("----------\n");
			}
		}
		problemLog += buffer.toString();
	}
	outputClassFiles(compilationResult);	
}
}
