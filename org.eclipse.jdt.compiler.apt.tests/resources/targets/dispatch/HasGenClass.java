/*
 * Target for annotation processing test.  Processing this
 * should result in generation of a class gen.HgcGen with
 * method public String foo().
 * @see org.eclipse.jdt.compiler.apt.tests.processors.genclass.GenClassProc.
 */
package targets.dispatch;

import org.eclipse.jdt.compiler.apt.tests.annotations.GenClass;

// This class will be generated by the annotation processor
import gen.HgcGen;

@GenClass(clazz="gen.HgcGen", method="foo")
public class HasGenClass {
	String get() {
		return (new HgcGen()).foo();
	}
}			