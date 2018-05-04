package jaicore.processes;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.FileUtil;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;

/**
 * This class outsources the call to an arbitrary method into a separate process. This is specifically relevant if you work with libraries that do no respect the interrupt-functionality.
 * 
 * Using this wrapper should allow you to conveniently interrupt any method you like. There are of course some drawbacks one must consider: - Execution is MUCH slower compared to execution in a local
 * thread (up to a factor of 10) - all items (target object, inputs, and the expected output) must be serializable - communication with the executed logic is highly limited - no call by reference
 * possible. If the routine to be outsourced needs to update the object on which it is invoked, encapsulate the logic first into a method that returns the object. - there may be strange side effects,
 * e.g., if the subprocess loads libraries manually, this may happen very often and flood temp directories in the file system
 * 
 * @author Felix Mohr
 *
 */
public class SubProcessWrapper {
	private static final Random random = new Random(System.currentTimeMillis());
	private static final Logger logger = LoggerFactory.getLogger(SubProcessWrapper.class);
	
	private static final List<SubProcessWrapper> wrappers = new ArrayList<>();
	
	private String memory = "256M";
	private File tmpDir = new File("tmp");
	private int pidOfSubProcess;
	
	public static List<SubProcessWrapper> getWrappers() {
		return wrappers;
	}

	public SubProcessWrapper() {
		super();
		wrappers.add(this);
	}

	static public String getAbsoluteClasspath() {
		try {
			return System.getProperty("java.class.path") + java.io.File.pathSeparatorChar
					+ URLDecoder.decode(SubProcessWrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Object run(String clazz, String method, Object target, Object... inputs) throws IOException, InterruptedException, InvocationTargetException {
		return run(clazz, method, target, Arrays.asList(inputs));
	}

	/**
	 * 
	 * @param clazz
	 * @param method
	 * @param target
	 * @param timeout
	 * @param inputs
	 * @return
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 *             This is only thrown if the executing thread is interrupted from *outside* but not when it is canceled due to the timeout
	 */
	public Optional<Object> runWithTimeout(String clazz, String method, Object target, int timeout, Object... inputs)
			throws IOException, InvocationTargetException, InterruptedException {
		TimeoutSubmitter submitter = TimeoutTimer.getInstance().getSubmitter();
		int timerId = submitter.interruptMeAfterMS(timeout);
		Object c;
		try {
			c = run(clazz, method, target, inputs);
		} catch (Exception e) {
			submitter.close();
			throw e;
		}
		submitter.cancelTimeout(timerId);
		if (Thread.interrupted()) {
			throw new IllegalStateException("We got interrupted but no InterruptedException was thrown!");
		}
		return (c != null) ? Optional.of(c) : Optional.empty();
	}

	public Object run(String clazz, String method, Object target, List<Object> inputs) throws IOException, InterruptedException, InvocationTargetException {

		/* create new id for the invocation */
		String id = String.valueOf(random.nextLong());
		File dir = new File(tmpDir.getAbsolutePath() + File.separator + id);
		dir.mkdirs();
		logger.info("Created tmp dir \"{}\" for invocation {}.{}({}).", dir.getAbsolutePath(), target, method, inputs);

		/* create command list */
		final List<String> commands = new ArrayList<>();
		commands.add("java");
		commands.add("-cp");
		commands.add(getAbsoluteClasspath());
		commands.add("-Xmx" + memory);
		commands.add(this.getClass().getName());
		commands.add(dir.getAbsolutePath());
		commands.add(clazz);
		commands.add(method);

		logger.info("Serializing object ...");
		
		/* serialize target */
		String targetVal = "null";
		if (target != null) {
			targetVal = target.getClass().getName() + ".ser";
			FileUtil.serializeObject(target, dir.getAbsolutePath() + File.separator + targetVal);
		}
		commands.add(targetVal);

		/* serialize inputs */
		for (int i = 0; i < inputs.size(); i++) {
			Object input = inputs.get(i);
			String argval;
			if (inputs.get(i) != null) {
				argval = input.getClass().getName() + ".ser";
				FileUtil.serializeObject(inputs.get(i), dir.getAbsolutePath() + File.separator + argval);
			} else
				argval = "null";
			commands.add(argval);
		}

		/* run process in a new thread */
		logger.info("ProcessBuilder started");
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);
		Process process = pb.start();
		pidOfSubProcess = ProcessUtil.getPID(process);
		logger.info("Spawned process {}. Currently active java processes:", pidOfSubProcess);
		ProcessUtil.getRunningJavaProcesses().stream().forEach(p -> logger.info("\t{}", p));
		
		/* this hook kills the java process we just created. This is done on the system level, because process.destroy() is not reliable
		 * The hook is invoked either on an interrupt of the executing thread or if the whole application is terminated (not abrubtly) */
		Thread killerHook = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("Destroying subprocess {}", pidOfSubProcess);
				if (process.isAlive()) {
					try {
						process.destroy();
						ProcessUtil.killProcess(pidOfSubProcess);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				logger.info("Subprocess {} destroyed.", pidOfSubProcess);
//				try {
//					System.out.println("Running java processes: ");
//					ProcessUtil.getRunningJavaProcesses().stream().forEach(p -> System.out.println("\t" + p));
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			}
		});
		Runtime.getRuntime().addShutdownHook(killerHook);

		/* now wait for the process to terminate */
		try {
			logger.info("Awaiting termination.");
			process.waitFor();
		}

		/* if the process is supposed to be interrupted in the meantime, destroy it */
		catch (InterruptedException e) {
			logger.info("Received interrupt");
			killerHook.run();
			FileUtil.deleteFolderRecursively(dir);
			throw e;
		} finally {
			try {
				Runtime.getRuntime().removeShutdownHook(killerHook);
			} catch (IllegalStateException e) {
				/* this can be ignored safely */
			}
		}

		/* now read in result and return it */
		logger.info("Processing results ...");
		File serializedResult = new File(dir + File.separator + "result.ser");
		File nullResult = new File(dir + File.separator + "result.null");
		File exceptionResult = new File(dir + File.separator + "result.exception");
		if (serializedResult.exists()) {
			try {
				Object out = FileUtil.unserializeObject(serializedResult.getAbsolutePath());
				FileUtil.deleteFolderRecursively(dir);
				return out;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (nullResult.exists()) {
			FileUtil.deleteFolderRecursively(dir);
			return null;
		}
		if (exceptionResult.exists()) {
			Exception exception = null;
			try {
				exception = (Exception) FileUtil.unserializeObject(exceptionResult.getAbsolutePath());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			FileUtil.deleteFolderRecursively(dir);
			throw new InvocationTargetException(exception);
		}
		logger.warn("Subprocess execution terminated but no result was observed for " + id + "!");
		return null;
	}

	private static Object executeCommand(File folder, String clazz, String method_name, String target, LinkedList<String> argsArray) throws Exception {

		logger.info("Invoking in folder {} method {} on class {}", folder, clazz, method_name);
		Object targetObject = null;
		if (!target.equals("null"))
			targetObject = FileUtil.unserializeObject(folder.getAbsolutePath() + File.separator + target);

		Class<?>[] params = new Class[argsArray.size()];
		Object[] objcts = new Object[argsArray.size()];
		int counter = 0;
		while (!argsArray.isEmpty()) {
			String descriptor = argsArray.poll();
			boolean isNull = descriptor.equals("");
			objcts[counter] = isNull ? null : FileUtil.unserializeObject(folder.getAbsolutePath() + File.separator + descriptor);
			params[counter] = isNull ? null : Class.forName(descriptor.substring(0, descriptor.lastIndexOf(".")));
			counter++;
		}

		/* retrieve method and call it */
		Method method = MethodUtils.getMatchingAccessibleMethod(Class.forName(clazz), method_name, params);
		return method.invoke(targetObject, objcts);
	}

	public static void main(String[] args) {
		LinkedList<String> argsArray = new LinkedList<String>(Arrays.asList(args));
		File folder = new File(argsArray.poll());
		if (!folder.exists())
			throw new IllegalArgumentException("The invocation call folder " + folder + " does not exist!");
		String clazz = argsArray.poll();
		String method_name = argsArray.poll();
		String target = argsArray.poll();
		Object result;
		try {
			result = executeCommand(folder, clazz, method_name, target, argsArray);
			FileUtil.serializeObject(result, folder.getAbsolutePath() + File.separator + "result." + (result != null ? "ser" : "null"));
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				FileUtil.serializeObject(e, folder.getAbsolutePath() + File.separator + "result.exception");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		logger.info("Finishing subprocess");
	}

	public void setMemory(String pMemory) {
		memory = pMemory;
	}

	public File getTmpDir() {
		return tmpDir;
	}

	public void setTmpDir(File tmpDir) {
		this.tmpDir = tmpDir;
	}
}
