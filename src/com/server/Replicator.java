package com.server;

import java.util.List;

/*
 * @desc Thread to make sure the cluster
 * 		 is consistent with the 2/3 rule.
 */
public class Replicator implements Runnable {
	@Override
	public void run() {
		System.out.println("Replicator:");
		NodeRegistry.printCurrentState();

		// Check if file is in two-thirds of the nodes. Deferred feature

		List<NodeRegistry.InconsistentEntry> inconsistentFiles = NodeRegistry.findAllInconsistentFiles();
		for (NodeRegistry.InconsistentEntry inconsistentEntry : inconsistentFiles) {
			
			System.out.println("Replicator:Attempting to replicate " + inconsistentEntry.filename);
			NodeRegistry.NodeEntry fromNode = decide(inconsistentEntry.filename);
			// readjust files one at a time per iteration
			NodeRegistry.NodeEntry toNode = inconsistentEntry.entriesNotHaving.get(0);
			
			System.out.println("FROMNODE: " + fromNode);
			System.out.println("TONODE: " + toNode);
			
			fromNode.printWriter.println(String.format("SENDFILE,%s,%d,%s,%s,%s,%s",
												toNode.sftpDetails.host,
												toNode.sftpDetails.port,
												toNode.sftpDetails.username,
												toNode.sftpDetails.password, 
												toNode.nodeName,
												inconsistentEntry.filename));
			NodeRegistry.addTask(fromNode.nodeName);
			toNode.filenames.add(inconsistentEntry.filename);
			System.out.println("Finished Sending Replicate request");
		}
	}

	// use task and filename variables to decide/filter
	/*
	 * @desc Function to decide which node entry to 
	 * 		 deliver a file to another node.
	 * 		 Filters based on the filename (of course) and
	 * 		 who has the least "tasks".
	 */
	private static NodeRegistry.NodeEntry decide(String filename) {
		List<NodeRegistry.NodeEntry> list = NodeRegistry.getEntriesWithFile(filename);
		
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
}
