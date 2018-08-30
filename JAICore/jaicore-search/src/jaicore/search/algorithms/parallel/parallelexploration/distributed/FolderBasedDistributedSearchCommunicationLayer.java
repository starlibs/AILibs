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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.FileUtil;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;

public class FolderBasedDistributedSearchCommunicationLayer<T, A, V extends Comparable<V>> implements DistributedSearchCommunicationLayer<T, A, V> {

	private static final Logger logger = LoggerFactory.getLogger(FolderBasedDistributedSearchCommunicationLayer.class);
	private final Set<String> knownCoworkers = new HashSet<>();
	private final Path communicationFolder;
	private Map<String, Semaphore> registerTickets = new HashMap<>();
	private Map<String, BlockingQueue<Collection<Node<T, V>>>> jobQueues = new HashMap<>();

	private final Thread masterFolderObserver = new Thread() {
		public void run() {
//			try {
//				while (!Thread.interrupted()) {
//
//					Thread.sleep(500);
//				}
//			} catch (InterruptedException e) {
//				logger.info("Shutting down folder listener.");
//			}
		}
	};
	
	private final Thread coworkerFolderObserver = new Thread() {
		public void run() {
			try {
				while (!Thread.interrupted()) {
					
					/* detect whether a coworker has a new job request */
					for (String coworker : knownCoworkers) {
						readCoworkersJob(coworker);
					}
					
					/* check whether registering coworkers have been attached */
					for (String registeringCoworker : registerTickets.keySet()) {
						if (isAttached(registeringCoworker)) {
							registerTickets.get(registeringCoworker).release();
							registerTickets.remove(registeringCoworker);
						}
					}
					
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {
				logger.info("Shutting down folder listener.");
			}
		}
	};
	
	public FolderBasedDistributedSearchCommunicationLayer(Path communicationFolder, boolean isMaster) {
		super();
		this.communicationFolder = communicationFolder;
		if (isMaster)
			masterFolderObserver.start();
		else
			coworkerFolderObserver.start();
	}

	public void init() {

		/* clean directory */
		try (Stream<Path> paths = Files.walk(communicationFolder)) {
			paths.forEach(filePath -> {
				try {
					if (Files.isRegularFile(filePath) && !filePath.getFileName().toString().contains("register")) {
						logger.info("Deleting {}", filePath.getFileName());
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
					logger.info("Recognized coworker {}", coworkerId);
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

		/* add coworkers */
		for (String newCoworker : newCoworkers) {
			knownCoworkers.add(newCoworker);
			if (!jobQueues.containsKey(newCoworker))
				jobQueues.put(newCoworker, new LinkedBlockingQueue<>());
		}

		return newCoworkers;
	}

	@Override
	public void detachCoworker(String coworker) {
		File f = new File(communicationFolder.toAbsolutePath() + "/attach-" + coworker);
		try {
			Files.delete(f.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createNewJobForCoworker(String coworkerId, Collection<Node<T, V>> nodesToBeSolved) {
		File target = new File(communicationFolder.toFile().getAbsolutePath() + "/job-" + coworkerId);
		File tmp = new File(target.getAbsolutePath() + ".tmp");
		logger.info("Writing job for {}: {}", coworkerId, nodesToBeSolved);
		try (ObjectOutputStream bw = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)))) {
			bw.writeObject(nodesToBeSolved);
			bw.close();
			Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
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
				result = (DistributedComputationResult<T, V>) in.readObject();
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
			logger.error("Failed to read and/or delete file " + file.getName());
		return result;
	}

	/** Coworker Stuff **/
	public void register(String coworker) throws InterruptedException {
		try {

			/* detach coworker forst if it is attached */
			if (isAttached(coworker))
				detachCoworker(coworker);

			/* create register semaphore */
			Semaphore s = new Semaphore(0);
			registerTickets.put(coworker, s);

			/* now register */
			File f = new File(communicationFolder.toAbsolutePath() + "/register-" + coworker);
			f.createNewFile();

			/* now wait for acceptance */
			s.acquire(1);
			
			/* if the coworker has been registered, add him to the pool of candidates */
			knownCoworkers.add(coworker);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void unregister(String coworker) {
		try {
			File f = new File(communicationFolder.toAbsolutePath() + "/register-" + coworker);
			if (f.exists()) {
				logger.info("Deleting {}", f.getAbsolutePath());
				Files.delete(f.toPath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Collection<Node<T, V>> nextJob(String coworker) throws InterruptedException {
		if (!jobQueues.containsKey(coworker))
			jobQueues.put(coworker, new LinkedBlockingQueue<>());
		return jobQueues.get(coworker).take();
	}

	@Override
	public void reportResult(String coworker, DistributedComputationResult<T, V> result) {
		File target = new File(communicationFolder.toFile().getAbsolutePath() + "/results-" + coworker);
		File tmp = new File(target.getAbsolutePath() + ".tmp");
		try (ObjectOutputStream bw = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)))) {
			bw.writeObject(result);
			bw.close();
			Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void attachCoworker(String coworker) {
		try {
			File f = new File(communicationFolder.toAbsolutePath() + "/attach-" + coworker);
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isAttached(String coworker) {
		File f = new File(communicationFolder.toAbsolutePath() + "/attach-" + coworker);
		return f.exists();
	}

	@Override
	public void setGraphGenerator(SerializableGraphGenerator<T, A> generator) throws Exception {
		FileUtil.serializeObject(generator, communicationFolder.toAbsolutePath() + "/graphgen.ser");
	}

	@Override
	public void setNodeEvaluator(SerializableNodeEvaluator<T, V> evaluator) throws Exception {
		FileUtil.serializeObject(evaluator, communicationFolder.toAbsolutePath() + "/nodeeval.ser");
	}

	@SuppressWarnings("unchecked")
	@Override
	public SerializableGraphGenerator<T, A> getGraphGenerator() throws Exception {
		return (SerializableGraphGenerator<T, A>) FileUtil.unserializeObject(communicationFolder.toAbsolutePath() + "/graphgen.ser");
	}

	@SuppressWarnings("unchecked")
	@Override
	public INodeEvaluator<T, V> getNodeEvaluator() throws Exception {
		return (INodeEvaluator<T, V>) FileUtil.unserializeObject(communicationFolder.toAbsolutePath() + "/nodeeval.ser");
	}

	@Override
	public void close() {
		masterFolderObserver.interrupt();
		coworkerFolderObserver.interrupt();
	}

	private void readCoworkersJob(String coworker) {
		File f = new File(communicationFolder.toAbsolutePath() + "/job-" + coworker);
		if (!f.exists())
			return;
		int tries = 0;
		while (tries < 10) {
			try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)))) {
				@SuppressWarnings("unchecked")
				Collection<Node<T, V>> nodes = (Collection<Node<T, V>>) in.readObject();
				in.close();
				Files.delete(f.toPath());
				jobQueues.get(coworker).add(nodes);
				return;
			} catch (IOException e) {
				try {
					logger.error("Error reading file " + f.toString() + ", waiting 500ms and retrying.");
					e.printStackTrace();
					tries++;
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		logger.info("Giving up reading the results of " + coworker);
	}
}
