package com.server;

import java.util.List;

/*
 * @desc Thread to make sure the cluster
 * 		 is consistent with the 2/3 rule.
 */
public class Replicator implements Runnable {

	@Override
	public void run() {
		System.out.println("Check 2/3");

		// Check if file is in two-thirds of the nodes. Deferred feature

		List<NodeRegistry.InconsistentEntry> inconsistentFiles = NodeRegistry.findAllInconsistentFiles();
		if (!inconsistentFiles.isEmpty()) {
			for (NodeRegistry.InconsistentEntry inconsistentEntry : inconsistentFiles) {
				
				NodeRegistry.NodeEntry nodeEntry = inconsistentEntry.entries.get(0);
				NodeRegistry.NodeEntry nodeToSendFile = decide(inconsistentEntry.filename);
				
				// TODO Add this to NodeEntry class
				String filePath = "/home/samuel/";
				
				nodeEntry.printWriter.println(String.format("SENDFILE,%s,%d,%s,%s,%s",
													nodeToSendFile.sftpDetails.host,
													nodeToSendFile.sftpDetails.port,
													nodeToSendFile.sftpDetails.username,
													nodeToSendFile.sftpDetails.password, 
													filePath + inconsistentEntry.filename));
				
				System.out.println("Finished Sending Replicate request");
			}
		}
	}

	// TODO
	// use task and filename variables to decide/filter 
	private static NodeRegistry.NodeEntry decide(String filename) {
		return NodeRegistry.listEntries().get(0);
	}
}
