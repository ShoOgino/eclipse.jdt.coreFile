package test0189;
import java.util.*;
public class Test {
	public void foo() {
		for (int i= 0;/*[*/ i < 10/*]*/; i++)
			foo();
	}
}