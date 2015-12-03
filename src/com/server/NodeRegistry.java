package com.server;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeRegistry {

	private NodeRegistry() {
		throw new UnsupportedOperationException(
				"Do not instantiate this class.");
	}

	private static final List<NodeEntry> entries = new ArrayList<NodeEntry>();

	public static synchronized void register(String nodeName,
			SftpDetails sftpDetails, PrintWriter printWriter, String nodeHttpHost, int nodeHttpPort, String nodeContextPath) {
		entries.add(new NodeEntry(nodeName, sftpDetails, printWriter, nodeHttpHost, nodeHttpPort, nodeContextPath));
	}

	public static synchronized void setDead(String nodeName) {
		for (NodeEntry entry : entries) {
			if (entry.nodeName.equals(nodeName)) {
				entry.isAlive = false;
			}
		}
	}
	
	public static boolean addTask(String nodeName) {
		List<NodeEntry> list = listEntries();
		
		for (NodeEntry entry : list) {
			if (entry.nodeName.equals(nodeName)) {
				entry.tasks.incrementAndGet();

				System.out.println(nodeName + " doing a task");
				return true;
			}
		}
		return false;
	}

	public static boolean confirmFinishTask(String nodeName) {
		List<NodeEntry> list = listEntries();
		
		for (NodeEntry entry : list) {
			if (entry.nodeName.equals(nodeName)) {
				entry.tasks.decrementAndGet();
				
				System.out.println(nodeName + " has finished a task");
				return true;
			}
		}
		System.out.println("Finish confirming");
		return false;
	}

	/*
	 * @desc Function to decide which node entry to 
	 * 		 deliver a file to another node.
	 * 		 Filters based on the filename (of course) and
	 * 		 who has the least "tasks".
	 */
	public static NodeRegistry.NodeEntry decide(String filename) {
		List<NodeRegistry.NodeEntry> list = NodeRegistry.getEntriesWithFile(filename);
		System.out.println("Entries with file: " + list);
		int numOfTasks = 0;
		boolean toContinue = true;
		
		while (!list.isEmpty() && toContinue) {
			for (NodeRegistry.NodeEntry nodeEntry: list) {
				if (nodeEntry.tasks.intValue() == numOfTasks) {
					toContinue = false;
					return nodeEntry;
				}
			}
			
			numOfTasks++;
		}
		
		return null;
	}
	
	/*
	 * @return List of Nodes that are alive.
	 */
	public static List<NodeEntry> listEntries() {
		List<NodeEntry> list = new ArrayList<NodeEntry>();

		for (NodeEntry nodeEntry : NodeRegistry.entries) {
			if (nodeEntry.isAlive) {
				list.add(nodeEntry);
			}
		}
		return list;
	}

	public static Map<String, Integer> getConsistentFiles() {
		Map<String, Integer> map = getFileStat();
		Map<String, Integer> newMap = new HashMap<>();
		int fileNumRequirement = calculateFileNumRequirement();
		
		for (String filename: map.keySet()) {
			if (map.get(filename) >= fileNumRequirement) {
				newMap.put(filename, map.get(filename));
			}
		}
		return newMap;
	}
	/*
	 * @desc Return a list of InconsistentEntry objects that includes nodes that
	 * are not two-thirds in nodes.
	 */
	public static List<InconsistentEntry> findAllInconsistentFiles() {
		List<InconsistentEntry> inconsistentEntries = new ArrayList<>();

		if (!listEntries().isEmpty()) {
			
			int fileNumRequirement = calculateFileNumRequirement();
			Map<String, Integer> fileStats = getFileStat();
			
			for (String filename : fileStats.keySet()) {
				if (fileStats.get(filename) < fileNumRequirement) {
					InconsistentEntry ie = new InconsistentEntry();
					ie.filename = filename;
					ie.entriesNotHaving = getEntriesWithOutFile(filename);
					inconsistentEntries.add(ie);
				}
			}
		}
		
		return inconsistentEntries;
	}

	/*
	 * @return required number of nodes that any file must be in
	 */
	private static int calculateFileNumRequirement() {
		return (int) Math.ceil((listEntries().size() * 2) / 3.0);
	}

	/*
	 * @return map containing the filename and how many nodes have them
	 */
	private static Map<String, Integer> getFileStat() {
		Map<String, Integer> map = new HashMap<String, Integer>();

		for (NodeEntry entry : listEntries()) {
			for (String filename : entry.filenames) {
				if (map.containsKey(filename)) {
					int count = map.get(filename);
					count++;
					map.put(filename, count);
				} else {
					map.put(filename, 1);
				}
			}
		}

		return map;
	}

	/*
	 * @return list of nodes that have a particular file
	 */
	public static List<NodeEntry> getEntriesWithFile(String filename) {
		List<NodeEntry> list = new ArrayList<NodeEntry>();
		List<NodeEntry> entriesList = listEntries();

		for (NodeEntry entry : entriesList) {
			if (entry.filenames.contains(filename) && entry.isAlive) {
				list.add(entry);
			}
		}

		return list;
	}

	/*
	 * @return list of nodes that DO NOT have a particular file
	 */
	private static List<NodeEntry> getEntriesWithOutFile(String filename) {
		List<NodeEntry> list = new ArrayList<NodeEntry>();
		List<NodeEntry> entriesList = listEntries();

		for (NodeEntry entry : entriesList) {
			if (!entry.filenames.contains(filename) && entry.isAlive) {
				list.add(entry);
			}
		}

		return list;
	}

	/*
	 * @return random node for upload of first
	 */
	public static NodeEntry getRandomNode() {

		return listEntries().get(new Random().nextInt(listEntries().size()));
	}


	public static List<String> getAllFiles() {
		List<NodeEntry> list = listEntries();
		Set<String> set = new HashSet<>();
		
		for (NodeEntry entry: list) {
			set.addAll(entry.filenames);
		}
		return new ArrayList<String>(set);
	}
	
	public static String printCurrentState() {
		StringBuilder sb = new StringBuilder();
		System.out.println("Printing Current state of NodeRegistry. ");
		if (listEntries().isEmpty()) {
			System.out.println("NodeRegistry is empty.");
		}
		for (NodeEntry entry : listEntries()) {
			sb.append(String.format(
					"%s: isAlive: %b, tasks: %d, filenames: %s\n",
					entry.nodeName, entry.isAlive, entry.tasks.intValue(),
					entry.filenames.toString()));
		}

		System.out.println(sb.toString());

		return sb.toString();
	}

	public static class NodeEntry {
		String nodeName;
		boolean isAlive = true;
		SftpDetails sftpDetails;
		List<String> filenames;
		AtomicInteger tasks = new AtomicInteger(0);
		PrintWriter printWriter;
		public String nodeHttpHost;
		public int nodeHttpPort;
		public String nodeContextPath;

		NodeEntry(String nodeName, SftpDetails sftpDetails,
				PrintWriter printWriter, String nodeHttpHost, int nodeHttpPort, String nodeContextPath) {
			this.nodeName = nodeName;
			this.filenames = new ArrayList<>();
			this.sftpDetails = sftpDetails;
			this.printWriter = printWriter;
			
			this.nodeHttpHost = nodeHttpHost;
			this.nodeHttpPort = nodeHttpPort;
			
			this.nodeContextPath = nodeContextPath;
		}

		@Override
		public String toString() {
			return "NodeEntry [nodeName=" + nodeName + ", isAlive=" + isAlive
					+ ", sftpDetails=" + sftpDetails + ", filenames="
					+ filenames + ", tasks=" + tasks + ", printWriter="
					+ printWriter + ", nodeHttpHost=" + nodeHttpHost
					+ ", nodeHttpPort=" + nodeHttpPort + "]";
		}
	}

	static class InconsistentEntry {
		List<NodeEntry> entriesNotHaving;
		String filename;

		@Override
		public String toString() {
			return "InconsistentEntry [entries=" + entriesNotHaving
					+ ", filename=" + filename + "]";
		}
	}
}