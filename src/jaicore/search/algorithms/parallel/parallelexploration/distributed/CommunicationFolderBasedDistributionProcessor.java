package jaicore.search.algorithms.parallel.parallelexploration.distributed;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.structure.core.Node;

public class CommunicationFolderBasedDistributionProcessor<T,V extends Comparable<V>> implements DistributedSearchMaintainer<T,V> {

	private static final Logger logger = LoggerFactory.getLogger(CommunicationFolderBasedDistributionProcessor.class);
	private final Path communicationFolder;

	/** Master Stuff **/
	public CommunicationFolderBasedDistributionProcessor(Path communicationFolder) {
		super();
		this.communicationFolder = communicationFolder;

		/* clean directory */
		try (Stream<Path> paths = Files.walk(communicationFolder)) {
			paths.forEach(filePath -> {
				try {
					if (Files.isRegularFile(filePath) && !filePath.getFileName().toString().contains("register")) {
						System.out.println("Deleting " + filePath.getFileName());
						Files.delete(filePath);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Collection<String> detectNewCoworkers() {

		/* check whether there are requests */
		Collection<String> newCoworkers = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(communicationFolder)) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath) && filePath.toFile().getName().startsWith("register-")) {

					/* register coworker and remove it */
					String coworkerId = filePath.toFile().getName().substring("register-".length());
					System.out.println("Recognized coworker " + coworkerId);
					try {
						Files.delete(filePath);
					} catch (Exception e) {
						e.printStackTrace();
					}
					newCoworkers.add(coworkerId);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newCoworkers;
	}
	
	@Override
	public void detachCoworker(String coworker) {
		File f = new File(communicationFolder.toAbsolutePath() + "/attach-" + coworker);
		try{
			Files.delete(f.toPath());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void createNewJobForCoworker(String coworkerId, Collection<Node<T,V>> nodesToBeSolved) {
		File target = new File(communicationFolder.toFile().getAbsolutePath() + "/job-" + coworkerId);
		File tmp = new File(target.getAbsolutePath() + ".tmp");
		System.out.println("Writing job: " + nodesToBeSolved);
		try (ObjectOutputStream bw = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(tmp)))) {
			bw.writeObject(nodesToBeSolved);
			bw.close();
			Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public DistributedComputationResult<T, V> readResult(String coworker) {
		File file = new File(communicationFolder.toFile().getAbsolutePath() + "/results-" + coworker);
		if (!file.exists())
			return null;
		logger.info("Found results from coworker " + coworker);
		boolean success = false;
		int tries = 0;
		DistributedComputationResult<T, V> result = null;
		while (!success && tries < 4) {
			try {
				tries++;

				/* read results object */
				logger.info("Reading file " + file.getAbsolutePath() + " ...");
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
				result = (DistributedComputationResult<T, V>)in.readObject();
				in.close();
				logger.info("done");
				Files.delete(file.toPath());
				success = true;
			} catch (IOException | ClassNotFoundException e) {
				try {
					Thread.sleep(100);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		if (!success)
			System.err.println("Failed to read and/or delete file " + file.getName());
		return result;
	}

	/** Coworker Stuff **/
	public void register(String coworker) {
		try {
			File f = new File(communicationFolder.toAbsolutePath() + "/register-" + coworker);
			f.createNewFile();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void unregister(String coworker) {
		try {
			File f = new File(communicationFolder.toAbsolutePath() + "/unregister-" + coworker);
			if (f.exists())
				Files.delete(f.toPath());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasNewJob(String coworker) {
		File f = new File(communicationFolder.toAbsolutePath() + "/job-" + coworker);
		return f.exists();
	}
	
	public Collection<Node<T,V>> getJobDescription(String coworker) {
		File f = new File(communicationFolder.toAbsolutePath() + "/job-" + coworker);
		if (!f.exists())
			throw new NoSuchElementException("No job available for " + coworker);
		int tries = 0;
		while (tries < 10) {
			try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)))) {
				@SuppressWarnings("unchecked")
				Collection<Node<T,V>> nodes = (Collection<Node<T,V>>)in.readObject();
				in.close();
				Files.delete(f.toPath());
				return nodes;
			}
			catch (IOException e) {
				try {
					System.err.println("Error reading file " + f.toString() + ", waiting 500ms and retrying.");
					e.printStackTrace();
					tries ++;
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		System.err.println("Giving up reading the results of " + coworker);
		return null;
	}

	@Override
	public void reportResult(String coworker, DistributedComputationResult<T, V> result) {
		File target = new File(communicationFolder.toFile().getAbsolutePath() + "/results-" + coworker);
		File tmp = new File(target.getAbsolutePath() + ".tmp");
		try (ObjectOutputStream bw = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)))) {
			bw.writeObject(result);
			bw.close();
			Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void attachCoworker(String coworker) {
		try {
			File f = new File(communicationFolder.toAbsolutePath() + "/attach-" + coworker);
			f.createNewFile();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isAttached(String coworker) {
		File f = new File(communicationFolder.toAbsolutePath() + "/attach-" + coworker);
		return f.exists();
	}
}
