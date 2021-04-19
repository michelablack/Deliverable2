package logic;

import java.util.List;

import bean.File;

public class Metrics {
	
	private Metrics() {
		super();
	}
	
	/**
	 * Function to calculate the metrics:
	 * - loc_added, avg_loc_added, max_loc_added;
	 * - churn, avg_churn, max_churn
	 * @param changes, array of string of form: <loc added, loc deleted, file name>;
	 * @param files, list of files for a certain revision;
	 * @param allFiles, list of all the files in the revisions.
	 */
	public static void locAddedAndChurn(String[] changes, List<File> files, List<File> allFiles) {
		File file = new File();
		float locAdd = Float.parseFloat(changes[0]);
		float locDel = Float.parseFloat(changes[1]); 
		String name = changes[2];
		file.setName(name);
		/*considering the last file in the list allFiles which
		has the same name as the one considered.*/
		int index = allFiles.lastIndexOf(file);
		File newFile = new File(file);
		newFile.setLocAdded(locAdd);
		newFile.setLocDeleted(locDel);
		newFile.setLastIndex(index);
		if (index != -1) {
			File prevFile = allFiles.get(index);
			/*considering the last pairwise files in the list
			*in order to obtain the updated data for the 
			considered values.*/
			Float sumLocAdded = locAdd+prevFile.getSumLocAdded();
			Float sumLocDeleted = locDel+prevFile.getSumLocDeleted();
			Float churn = sumLocAdded - sumLocDeleted;
			/*---------METRIC LOC_ADDED----------
			*calculated as the sum of LOC added in a 
			*revision of the considered file.
			*/
			newFile.setSumLocAdded(sumLocAdded);
			newFile.setSumLocDeleted(sumLocDeleted);
			/*---------METRIC CHURN----------
			*calculated as the sum of added-deleted LOC
			*in a revision of the considered file.
			*/
			newFile.setChurn(churn);
			/*calculating the actual number of revisions of 
			*the given file. 
			*/
			newFile.setNum(prevFile.getNum()+1);
			/*---------METRIC AVG_LOC_ADDED----------
			*calculated as the average of LOC added until 
			*the considered revision of the file.
			*/
			newFile.setAvgLocAdded(sumLocAdded/newFile.getNum());
			/*---------METRIC AVG_CHURN----------
			*calculated as the average of added-deleted LOC  
			*until the considered revision of the file.
			*/
			newFile.setAvgChurn(churn/newFile.getNum());
			float prevMaxLoc = prevFile.getMaxLocAdded();
			float prevMaxChurn = prevFile.getMaxChurn();
			/*---------METRIC MAX_LOC_ADDED----------
			 *calculated checking what is the maximum LOC added 
			 *between this one and the last revision of the file.
			 */
			if (locAdd>prevMaxLoc) {
				newFile.setMaxLocAdded(locAdd);
			}
			else newFile.setMaxLocAdded(prevMaxLoc);
			/*---------METRIC MAX_CHURN----------
			 *calculated checking what is the maximum added-deleted LOC  
			 *between this one and the last revision of the file.
			 */
			if ((locAdd-locDel) > prevMaxChurn) {
				newFile.setMaxChurn(locAdd-locDel);
			}
			else newFile.setMaxChurn(prevMaxChurn);
			files.add(newFile);
			allFiles.add(newFile);
		}
		else {
			/*
			 *If it's the first occurrence for the file in the list,
			 *the values are set with the actual revion's ones.
			 */
			newFile.setSumLocAdded(locAdd);
			newFile.setSumLocDeleted(locDel);
			newFile.setMaxLocAdded(locAdd);
			newFile.setMaxChurn(locAdd-locDel);
			newFile.setNum(1);
			newFile.setAvgLocAdded(locAdd);
			newFile.setAvgLocAdded(locAdd-locDel);
			files.add(newFile);
			allFiles.add(newFile);
		}
	}
	/**
	 * * Function to calculate the metrics:
	 * - change_set, avg_change_set, max_change_set;
	 * @param files, list of files for a certain revision;
	 * @param allFiles, list of all the files in the revisions.
	 */
	public static void changeSet(List<File> files, List<File> allFiles) {
		//---------METRIC CHANGE-SET-SIZE----------
		float chgSet = (float)files.size()-1;
		for (int i=0; i< files.size(); i++) {
			File file = files.get(i);
			file.setChgSetSize(chgSet);
			int index = file.getLastIndex();
			if (index != -1) {
				File prevFile = allFiles.get(index);
				float actualSum= chgSet+file.getSumChgSetSize();
				/*---------METRIC AVG_CHANGE_SET----------
				*calculated as the sum of all the change set
				*sizes until this revision, divided by the number
				*of the actual revisions.
				*/
				file.setAvgChgSet(actualSum/file.getNum());
				file.setSumChgSetSize(actualSum);
				float prevMaxChgSet = prevFile.getMaxChgSet();
				/*---------METRIC MAX_CHANGE_SET----------
				 *calculated checking what is the maximum change set size  
				 *between this one and the last revision of the file.
				 */
				if (chgSet>prevMaxChgSet) {
					file.setMaxChgSet(chgSet);
				}
				else file.setMaxChgSet(prevMaxChgSet);
				
			}
			else {
				file.setSumChgSetSize(chgSet);
				file.setMaxChgSet(chgSet);
				file.setAvgChgSet(chgSet);
			}
		}
		
	}
}
