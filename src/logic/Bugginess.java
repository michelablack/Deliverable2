package logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bean.Bug;
import bean.File;

public class Bugginess {
	
	private Bugginess() {
		super();
	}
	
	static List<Bug> buggyList = new ArrayList<>();
	static List<File> buggyFiles = new ArrayList<>(); 
	static Date midDate;
	static int[] buggy;
	public static void isBuggy(List<Bug> buggyList, String projName, String folder) {
		for (int i = 0; i < buggyList.size(); i++) {
			Integer iv = buggyList.get(i).getRelease().getIv();
			Integer fv = buggyList.get(i).getRelease().getFv();
			List<String> commitList = QueryGenerator.getCommits(buggyList.get(i).getKey(), projName, folder);
			for (int j = 0; j < commitList.size(); j++) {
				List<File> filesList = QueryGenerator.getFiles(commitList.get(j), projName, folder);
				for (int f = 0; f < filesList.size(); f++) {
					File file = filesList.get(f);
					buggy = new int[2];
					buggy[0] = iv;
					buggy[1] = fv;
					file.setBuggy(buggy);
					buggyFiles.add(file);
				}
			}
		}
		Utils.writeDataset(buggyFiles, projName, folder);
	}
}
