package org.bds.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.bds.Bds;
import org.bds.compile.CompilerMessages;
import org.bds.osCmd.TeeOutputStream;
import org.bds.run.RunState;
import org.bds.scope.ScopeSymbol;
import org.bds.util.Gpr;

import junit.framework.Assert;

/**
 * BDS test cases: Compile or run a bds program and store exitCode, STDOUT, STDERR, etc.
 *
 * @author pcingola
 */
public class BdsTest {

	public boolean debug;
	public boolean verbose;

	public Boolean compileOk;
	public String args[]; // Command line arguments (before program name)
	public String argsAfter[]; // Command line arguments (after program name)
	public String fileName;
	public CompilerMessages compilerMessages;
	public Bds bds;
	public Integer exitCode;
	public ByteArrayOutputStream captureStdout, captureStderr; // Capture STDOUT & STDERR
	public TeeOutputStream teeStdout, teeStderr;
	PrintStream stdout, stderr; // Store original STDOUT & STDERR
	public RunState runState;

	public BdsTest(String fileName, boolean verbose, boolean debug) {
		this(fileName, null, null, verbose, debug);
	}

	public BdsTest(String fileName, String args[], boolean verbose, boolean debug) {
		this(fileName, args, null, verbose, debug);
	}

	public BdsTest(String fileName, String args[], String argsAfter[], boolean verbose, boolean debug) {
		this.fileName = fileName;
		this.args = args;
		this.argsAfter = argsAfter;
		this.verbose = verbose;
		this.debug = debug;
	}

	/**
	 * Create 'command'
	 */
	void bds() {
		ArrayList<String> l = new ArrayList<String>();

		// Add command line options
		if (verbose) l.add("-v");
		if (debug) l.add("-d");

		if (args != null) {
			for (String arg : args)
				l.add(arg);
		}

		l.add(fileName); // Add file

		if (argsAfter != null) {
			for (String arg : argsAfter)
				l.add(arg);
		}

		args = l.toArray(new String[0]);

		// Create command
		bds = new Bds(args);
		bds.setStackCheck(true);
	}

	/**
	 * Show captured STDOUT & STDERR
	 */
	void captureShow() {
		if (!(verbose || debug)) {
			stdout.print("STDOUT ('" + fileName + "'):\n" + Gpr.prependEachLine("\t", captureStdout.toString()));
			stderr.print("STDERR ('" + fileName + "'):\n" + Gpr.prependEachLine("\t", captureStderr.toString()));
		}
	}

	void captureStart() {
		// Capture STDOUT
		stdout = System.out; // Store original stdout
		captureStdout = new ByteArrayOutputStream();
		teeStdout = new TeeOutputStream(stdout, captureStdout);

		// Capture STDERR
		stderr = System.err;
		captureStderr = new ByteArrayOutputStream();
		teeStderr = new TeeOutputStream(stderr, captureStderr);

		boolean show = verbose || debug;

		// Set STDOUT & STDERR
		System.setOut(new PrintStream(show ? teeStdout : captureStdout));
		System.setErr(new PrintStream(show ? teeStderr : captureStderr));
	}

	/**
	 * Stop capturing STDOUT & STDERR (restore original)
	 */
	void captureStop() {
		// Restore STDOUT & STDERR
		System.setOut(stdout);
		System.setErr(stderr);

		try {
			if (teeStdout != null) teeStdout.close();
			if (teeStderr != null) teeStderr.close();
			teeStdout = teeStderr = null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Check that compiler errors are found during compiling process
	 */
	public void checkCompileError(String expectedErrors) {
		Assert.assertFalse(errMsg("Expecting compilation errors, none found (program compiled OK)"), compileOk);
		Assert.assertEquals(errMsg("Expecting compilation errors not found"), expectedErrors.trim(), compilerMessages.toString().trim());
	}

	/**
	 * Check that the file was compiled OK
	 */
	public void checkCompileOk() {
		if (!compilerMessages.isEmpty()) Assert.fail("Compile errors in file '" + fileName + "':\n" + compilerMessages);
		if (compileOk != null) Assert.assertTrue(errMsg("There was an error while compiling"), compileOk);
	}

	/**
	 * Check exit code
	 */
	public void checkExitCode(int expectedExitCode) {
		Assert.assertTrue(errMsg("No exit value (program was not run)"), exitCode != null);
		Assert.assertEquals(errMsg("Expecting exit code '" + expectedExitCode + "', but it was '" + exitCode + "'"), expectedExitCode, (int) exitCode);
	}

	/**
	 * Check that the program run and finished OK
	 */
	void checkRunOk() {
		checkCompileOk();
		checkRunState(RunState.FINISHED);
		checkExitCode(0);
	}

	/**
	 * Check that RunState matches ou expectations
	 */
	void checkRunState(RunState expectedRunState) {
		Assert.assertEquals(errMsg("Expecting rRunState '" + expectedRunState + "', but it was '" + runState + "'") //
		, expectedRunState //
				, runState//
		);
	}

	public void checkStderr(String expectedStderr) {
		int index = captureStderr.toString().indexOf(expectedStderr);
		Assert.assertTrue(errMsg("Error: Expeted string '" + expectedStderr + "' in STDERR not found"), index >= 0);
	}

	public void checkStdout(String expectedStdout) {
		checkStdout(expectedStdout, false);
	}

	public void checkStdout(String expectedStdout, boolean negate) {
		int index = captureStdout.toString().indexOf(expectedStdout);

		if (negate) Assert.assertFalse(errMsg("Error: NOT expeted string '" + expectedStdout + "' in STDOUT not found"), index >= 0);
		else Assert.assertTrue(errMsg("Error: Expeted string '" + expectedStdout + "' in STDOUT not found"), index >= 0);
	}

	/**
	 * Check a variable's value
	 */
	public void checkVariable(String varname, Object expectedValue) {
		ScopeSymbol ssym = getSymbol(varname);
		Assert.assertTrue(errMsg("Variable '" + varname + "' not found "), ssym != null);
		Assert.assertEquals( //
				errMsg("Variable '" + varname + "' has different value than expeced:\n" //
						+ "\tExpected value : " + expectedValue //
						+ "\tReal value     : " + ssym.getValue()) //
						,
				expectedValue.toString() //
				, ssym.getValue().toString() //
		);
	}

	/**
	 * Check all variables in the hash
	 */
	void checkVariables(HashMap<String, Object> expectedValues) {
		// Check all values
		for (String varName : expectedValues.keySet()) {
			Object expectedValue = expectedValues.get(varName);

			ScopeSymbol ssym = getSymbol(varName);
			Assert.assertTrue(errMsg("Missing variable '" + varName + "'"), ssym != null);

			if (!expectedValue.toString().equals(ssym.getValue().toString())) {
				Assert.assertEquals(errMsg("Variable '" + varName + "' does not match:\n"//
						+ "\tExpected : '" + expectedValue.toString() + "'" //
						+ "\tActual   : '" + ssym.getValue().toString() + "'" //
				) //
				, expectedValue.toString() //
				, ssym.getValue().toString() //
				);
			}
		}
	}

	/**
	 * Compile code
	 */
	public boolean compile() {
		if (bds == null) bds(); // Create command

		compileOk = false;
		try {
			captureStart(); // Capture STDOUT & STDERR
			compileOk = bds.compile(); // Run
			compilerMessages = bds.getCompilerMessages();
		} catch (Throwable t) {
			captureShow(); // Make sure STDOUT & STDERR have been shown
			captureStop();
			throw new RuntimeException(t);
		} finally {
			captureStop();
		}

		return compileOk;
	}

	/**
	 * Create an error message
	 */
	String errMsg(String msg) {
		StringBuilder sb = new StringBuilder();

		sb.append("ERROR: " + msg + "\n");
		sb.append("\t" + toString());

		return sb.toString();
	}

	/**
	 * Get a symbol
	 */
	public ScopeSymbol getSymbol(String name) {
		return bds.getProgramUnit().getRunScope().getSymbol(name);
	}

	/**
	 * Run command
	 */
	public void run() {
		if (bds == null) bds(); // Create command

		try {
			captureStart(); // Capture STDOUT & STDERR
			exitCode = bds.run(); // Run
			compilerMessages = bds.getCompilerMessages(); // Any compile errors?
			if (bds.getBigDataScriptThread() != null) runState = bds.getBigDataScriptThread().getRunState(); // Get final RunState
		} catch (Throwable t) {
			captureShow(); // Make sure STDOUT & STDERR have been shown
			captureStop();
			t.printStackTrace();
			throw new RuntimeException(t);
		} finally {
			captureStop();
		}
	}

	/**
	 * Run a file and check that the expected variable matches a result
	 */
	public void runAndCheck(String varname, Object expectedValue) {
		run();
		checkRunOk();
		checkVariable(varname, expectedValue);
	}

	/**
	 * Check that a file recovers from a checkpoint and runs without errors
	 */
	public Bds runAndCheckpoint(String checkpointFileName, String varName, Object expectedValue, Runnable runBeforeRecover) {
		// Run
		run();
		checkCompileOk();

		// Run something before checkpoint recovery?
		if (runBeforeRecover != null) runBeforeRecover.run();
		else if (varName != null) checkVariable(varName, expectedValue);

		//---
		// Recover from checkpoint
		//---
		String chpFileName = checkpointFileName;
		if (checkpointFileName == null) chpFileName = fileName + ".chp";
		if (verbose) System.err.println("\n\n\nRecovering from checkpoint file '" + chpFileName + "'\n\n\n");
		if (debug) Gpr.debug("CheckPoint file name : " + chpFileName);
		String args2[] = { "-r", chpFileName };
		String args2v[] = { "-v", "-r", chpFileName };
		Bds bigDataScript2 = new Bds(verbose ? args2v : args2);
		bigDataScript2.setStackCheck(true);
		bigDataScript2.run();

		// Check variable's value on the recovered (checkpoint run) program
		if (varName != null) {
			ScopeSymbol ssym = bigDataScript2.getProgramUnit().getRunScope().getSymbol(varName);
			Assert.assertTrue(errMsg("Variable '" + varName + "' not found "), ssym != null);
			Assert.assertEquals( //
					errMsg("Variable '" + varName + "' has different value than expeced:\n" //
							+ "\tExpected value : " + expectedValue //
							+ "\tReal value     : " + ssym.getValue()) //
							,
					expectedValue.toString() //
					, ssym.getValue().toString() //
			);
		}

		return bigDataScript2;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Program: " + fileName + "\n");

		sb.append("\tArguments:\n");
		for (int i = 0; i < args.length; i++)
			sb.append("\t\targs[" + i + "]: " + args[i] + "\n");

		// Compilation errors / messages?
		if (compileOk != null) sb.append("\tCompiled OK: " + compileOk + "\n");
		if (!compilerMessages.isEmpty()) sb.append("\tCompile messages: " + compilerMessages.toString() + "\n");

		if (exitCode != null) sb.append("\tExit code: " + exitCode + "\n");

		// STDOUT and STDERR not already shown? Add them
		if (!(verbose || debug)) {
			sb.append("\tSTDOUT:\n" + Gpr.prependEachLine("\t\t|", captureStdout.toString()) + "\n");
			sb.append("\tSTDERR:\n" + Gpr.prependEachLine("\t\t|", captureStderr.toString()) + "\n");
		}

		return sb.toString();

	}
}
