package org.bds.test;

import org.bds.lang.InterpolateVars;
import org.bds.util.Gpr;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Quick test cases when creating a new feature...
 *
 * @author pcingola
 *
 */
public class TestCasesInterpolate extends TestCasesBase {

	void checkInterpolate(String str, String strings[], String vars[]) {
		InterpolateVars iv = new InterpolateVars(null, null);
		iv.parse(str);
		if (verbose) {
			System.out.println("String: " + str);
			System.out.println("\tInterpolation result: |" + iv + "|");
		}

		// Special case: No variables to interpolate
		if (strings.length == 1 && vars[0].isEmpty()) {
			Assert.assertTrue(iv.isEmpty());
			return;
		}

		// Check strings
		for (int i = 0; i < strings.length; i++) {
			if (verbose) {
				System.out.print("\tIndex: " + i);
				System.out.print("\tstring.expected: " + strings[i] + "\tstring.actual: " + iv.getLiterals()[i]);
				System.out.println("\tvar.expected: " + vars[i] + "\tvar.actual: " + iv.getExpressions()[i]);
			}

			Assert.assertEquals(strings[i], iv.getLiterals()[i]);
			if (vars[i] != null && !vars[i].isEmpty()) Assert.assertEquals(vars[i], iv.getExpressions()[i].toString());
		}
	}

	@Test
	public void test00() {
		Gpr.debug("Test");

		String strings[] = { "Hello $i" };
		String vars[] = { "" };

		checkInterpolate("Hello \\$i", strings, vars);
	}

	@Test
	public void test01() {
		Gpr.debug("Test");
		String strings[] = { "Hello " };
		String vars[] = { "i" };

		checkInterpolate("Hello $i", strings, vars);
	}

	@Test
	public void test02() {
		Gpr.debug("Test");
		String strings[] = { "Hello ", " " };
		String vars[] = { "i", "j" };

		checkInterpolate("Hello $i $j", strings, vars);
	}

	@Test
	public void test03() {
		Gpr.debug("Test");
		String strings[] = { "Hello ", "" };
		String vars[] = { "i", "j" };

		checkInterpolate("Hello $i$j", strings, vars);
	}

	@Test
	public void test04() {
		Gpr.debug("Test");
		String strings[] = { "l[1] : " };
		String vars[] = { "l[1]" };

		checkInterpolate("l[1] : $l[1]", strings, vars);
	}

	@Test
	public void test05() {
		Gpr.debug("Test");
		String strings[] = { "m{'Helo'} : " };
		String vars[] = { "m{\"Helo\"}" };

		checkInterpolate("m{'Helo'} : $m{'Helo'}", strings, vars);
	}

	@Test
	public void test06() {
		Gpr.debug("Test");
		String strings[] = { "m{'Helo'} : " };
		String vars[] = { "m{l[i]}" };

		checkInterpolate("m{'Helo'} : $m{$l[$i]}", strings, vars);
	}

	@Test
	public void test07() {
		Gpr.debug("Test");
		String strings[] = { "Hello $" };
		String vars[] = { "" };

		checkInterpolate("Hello $", strings, vars);
	}

	@Test
	public void test08() {
		Gpr.debug("Test");
		String strings[] = { "Hello $\n" };
		String vars[] = { "" };

		checkInterpolate("Hello $\n", strings, vars);
	}

	@Test
	public void test09() {
		Gpr.debug("Test");
		String strings[] = { "m{'Helo'} : " };
		String vars[] = { "m{s}" };

		checkInterpolate("m{'Helo'} : $m{$s}", strings, vars);
	}

	@Test
	public void test10() {
		Gpr.debug("Test");
		String strings[] = { "l[1] : '", "'\n" };
		String vars[] = { "l[1]", "" };

		checkInterpolate("l[1] : '$l[1]'\n", strings, vars);
	}

	@Test
	public void test11() {
		Gpr.debug("Test");
		String strings[] = { "List with variable index: '", "'\n" };
		String vars[] = { "l[s]", "" };

		checkInterpolate("List with variable index: '$l[$s]'\n", strings, vars);
	}

	@Test
	public void test12() {
		Gpr.debug("Test");
		String strings[] = { "List with variable index: {", "}\n" };
		String vars[] = { "l[s]", "" };

		checkInterpolate("List with variable index: {$l[$s]}\n", strings, vars);
	}

	@Test
	public void test13() {
		Gpr.debug("Test");
		String strings[] = { "List with variable index: [", "]\n" };
		String vars[] = { "l[s]", "" };

		checkInterpolate("List with variable index: [$l[$s]]\n", strings, vars);
	}

	@Test
	public void test14() {
		Gpr.debug("Test");
		String strings[] = { "Map with list and variable index: ", };
		String vars[] = { "m{l[s]}", "" };

		checkInterpolate("Map with list and variable index: $m{$l[$s]}", strings, vars);
	}

	@Test
	public void test15() {
		Gpr.debug("Test");
		String strings[] = { "Map with list and variable index: {", "}\n" };
		String vars[] = { "m{l[s]}", "" };

		checkInterpolate("Map with list and variable index: {$m{$l[$s]}}\n", strings, vars);
	}

	@Test
	public void test16() {
		Gpr.debug("Test");
		String str = "something ending in backslash \\";
		String strAfter = InterpolateVars.unEscapeDollar(str);

		if (verbose) {
			Gpr.debug("str (before): " + str);
			Gpr.debug("str (after) : " + strAfter);
		}

		Assert.assertEquals("Expected string should end in backslash", str, strAfter);
	}

	@Test
	public void test17() {
		Gpr.debug("Test");
		String str = "ending in dollar $";
		String strAfter = InterpolateVars.unEscapeDollar(str);

		if (verbose) {
			Gpr.debug("str (before): " + str);
			Gpr.debug("str (after) : " + strAfter);
		}

		Assert.assertEquals("Expected string should end in backslash", str, strAfter);
	}

	@Test
	public void test18() {
		Gpr.debug("Test");
		String str = "this dolalr sign '\\$VAR' should be escaped";
		String strExpected = "this dolalr sign '$VAR' should be escaped";

		String strAfter = InterpolateVars.unEscapeDollar(str);
		if (verbose) {
			Gpr.debug("str (before): " + str);
			Gpr.debug("str (after) : " + strAfter);
		}

		Assert.assertEquals("Expected string should end in backslash", strExpected, strAfter);
	}

	@Test
	public void test19() {
		Gpr.debug("Test");
		String str = "hello \\t world \\n";
		String strAfter = InterpolateVars.unEscapeDollar(str);

		if (verbose) {
			Gpr.debug("str (before): " + str);
			Gpr.debug("str (after) : " + strAfter);
		}

		Assert.assertEquals("Expected string should end in backslash", str, strAfter);
	}

	@Test
	public void test20() {
		Gpr.debug("Test");

		// We want to execute an inline perl script within a task
		// E.g.:
		//     task perl -e 'use English; print "PID: \$PID\n";'
		// 
		// Here $PID is a perl variable and should not be interpreted 
		// by bds. We need a way to escape such variables.

		String cmd = "PID: \\$PID";
		InterpolateVars intVars = new InterpolateVars(null, null);
		boolean parsed = intVars.parse(cmd);

		if (verbose) Gpr.debug("cmd: '" + cmd + "'");
		Assert.assertFalse("This string should not be parsed by interpolation", parsed);
	}

}
