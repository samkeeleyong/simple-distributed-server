package com.server;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NodeRegistry {

	private NodeRegistry() {
		throw new UnsupportedOperationException(
				"Do not instantiate this class.");
	}

	private static final List<NodeEntry> entries = new ArrayList<NodeEntry>();

	public static synchronized void register(String nodeName, SftpDetails sftpDetails, PrintWriter printWriter) {
		entries.add(new NodeEntry(nodeName, sftpDetails, printWriter));
	}

	public static synchronized void setDead(String nodeName) {
		for (NodeEntry entry : entries) {
			if (entry.nodeName.equals(nodeName)) {
				entry.isAlive = false;
			}
		}
	}

	// TODO
	private static void findEntryWithTask(int numOfTasks) {
		
	}


	public static synchronized List<NodeEntry> listEntries() {
		// TODO make a defensive copy
		return NodeRegistry.entries;
	}

	// TODO
	public static synchronized List<InconsistentEntry> findAllInconsistentFiles() {
		List<InconsistentEntry> inconsistentEntries = new ArrayList<>();
		
		if (!listEntries().isEmpty()) {
			InconsistentEntry ie = new InconsistentEntry();
			ie.entries = listEntries();
			ie.filename = "filename.txt";
			inconsistentEntries.add(ie);
		}
		
		return inconsistentEntries;
	}
	
	@Override
	public String toString() {
		return "Here are the nodes:" + entries.toString();
	}
	
	static class NodeEntry {
		String nodeName;
		boolean isAlive = true;
		SftpDetails sftpDetails;
		List<String> filenames;
		int tasks = 0;
		PrintWriter printWriter;

		NodeEntry(String nodeName, SftpDetails sftpDetails, PrintWriter printWriter) {
			this.nodeName = nodeName;
			this.filenames = new ArrayList<>();
			this.sftpDetails = sftpDetails;
			this.printWriter = printWriter;
		}

		@Override
		public String toString() {
			return "RegistryEntry [nodeName=" + nodeName + ", isAlive="
					+ isAlive + ", sftpDetails=" + sftpDetails + ", filenames="
					+ filenames + ", tasks=" + tasks + ", printWriter="
					+ printWriter + "]";
		}
	}

	static class InconsistentEntry {
		List<NodeRegistry.NodeEntry> entries;
		String filename;
		
		@Override
		public String toString() {
			return "InconsistentEntry [entries=" + entries + ", filename="
					+ filename + "]";
		}
	}
}