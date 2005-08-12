package org.eclipse.jdt.apt.tests.annotations.readAnnotationType;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;

public class ReadAnnotationTypeProcessor extends BaseProcessor {

    private AnnotationTypeDeclaration _annotationType;
    
    public ReadAnnotationTypeProcessor(Set<AnnotationTypeDeclaration> declarationTypes, AnnotationProcessorEnvironment env) {
        super(env);
        assert declarationTypes.size() == 1;
        _annotationType = declarationTypes.iterator().next();
    }

    public void process() {
        PrintWriter writer = null;
        try
        {
            Collection<Declaration> declarations = _env.getDeclarationsAnnotatedWith(_annotationType);     
            assert declarations.size() == 1;
            new AnnotationReader().createClassFilesForAnnotatedDeclarations(declarations, _env);
        } catch (Throwable e)
        {
            e.printStackTrace();
            _env.getMessager().printError(e.getMessage());
        } finally
        {
            if (writer != null)
            {
                writer.close();
            }
        }
    }
    

}
