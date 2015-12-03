package com.server;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.util.Constants;

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
			NodeRegistry.NodeEntry fromNode = NodeRegistry.decide(inconsistentEntry.filename);
			// readjust files one at a time per iteration
			NodeRegistry.NodeEntry toNode = inconsistentEntry.entriesNotHaving.get(0);
			
			System.out.println("FROMNODE: " + fromNode);
			System.out.println("TONODE: " + toNode);
			
			fromNode.printWriter.println(String.format("SENDFILE,%s,%d,%s,%s,%s,%s,%s",
												toNode.sftpDetails.host,
												toNode.sftpDetails.port,
												toNode.sftpDetails.username,
												toNode.sftpDetails.password, 
												toNode.nodeName,
												inconsistentEntry.filename, 
												toNode.nodeContextPath));
			NodeRegistry.addTask(fromNode.nodeName);
			toNode.filenames.add(inconsistentEntry.filename);
			System.out.println("Finished Sending Replicate request");
			
			// Delete files if meets two-thirds rule.
			Map<String, Integer> consistentFiles = NodeRegistry.getConsistentFiles();
			
	        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(Constants.GATEWAY_DIRECTORY))) {
	            for (Path path : directoryStream) {
	                if (consistentFiles.containsKey(path.getFileName().toString())) {
	                	Files.delete(path);
	                	System.out.println("Replicator: Deleting file:" + path + " in GATEWAY for satisfying the two-third rule.");
	                }
	            }
	        } catch (IOException ex) {}
		}
	}


}
