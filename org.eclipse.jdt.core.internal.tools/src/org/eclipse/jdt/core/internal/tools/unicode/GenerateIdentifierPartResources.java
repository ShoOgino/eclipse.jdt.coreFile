package org.eclipse.jdt.core.internal.tools.unicode;

import java.io.IOException;

public class GenerateIdentifierPartResources {

	public static void main(String[] args) throws IOException {
		UnicodeResourceGenerator generator = new UnicodeResourceGenerator(args);
		generator.generate(new PartEnvironment());
	}
}
