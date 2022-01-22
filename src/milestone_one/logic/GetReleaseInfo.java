package milestone_one.logic;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.json.JSONException;
import org.json.JSONObject;

import milestone_one.bean.Bug;
import milestone_one.bean.File;
import milestone_one.bean.Property;
import milestone_one.bean.Release;
import milestone_one.bean.Revision;

import org.json.JSONArray;


public class GetReleaseInfo {
	
	   private static Map<LocalDateTime, String> releaseNames;
	   private static Map<LocalDateTime, String> releaseID;
	   private static ArrayList<LocalDateTime> releases;
	   private static List<Release> list;
	   static String projName = Property.getInstance().getProperty("PROJECT");
	   static String link = Property.getInstance().getProperty("LINK_JIRA");
	   
	public static void main(String[] args) throws IOException, JSONException {
	    //Fills the arraylist with releases dates and orders them
	    //Ignores releases with missing dates
	    releases = new ArrayList<>();
        Integer i;
        String url = link + projName;
        JSONObject json = Utils.readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");
        releaseNames = new HashMap<>();
        releaseID = new HashMap<> ();
        for (i = 0; i < versions.length(); i++ ) {
           String name = "";
           String id = "";
           if(versions.getJSONObject(i).has("releaseDate")) {
              if (versions.getJSONObject(i).has("name"))
                 name = versions.getJSONObject(i).get("name").toString();
              if (versions.getJSONObject(i).has("id"))
                 id = versions.getJSONObject(i).get("id").toString();
              addRelease(versions.getJSONObject(i).get("releaseDate").toString(),
                         name,id);
           }
        }
        Collections.sort(releases, (o1, o2) -> o1.compareTo(o2));
        
        if (releases.size() < 6) return;
        String outname = projName + "VersionInfo.csv";
	    //Name of CSV for output
		try (FileWriter fileWriter = new FileWriter(outname)){
            fileWriter.append("Index;Version ID;Version Name;Date");
            fileWriter.append("\n");
            for ( i = 0; i < releases.size(); i++) {
               Integer index = i + 1;
               fileWriter.append(index.toString());
               fileWriter.append(";");
               fileWriter.append(releaseID.get(releases.get(i)));
               fileWriter.append(";");
               fileWriter.append(releaseNames.get(releases.get(i)));
               fileWriter.append(";");
               fileWriter.append(releases.get(i).toString());
               fileWriter.append("\n");
            }
         } catch (Exception e) {
            e.printStackTrace();
         } 
	   }
 
	
	   public static void addRelease(String strDate, String name, String id) {
		      LocalDate date = LocalDate.parse(strDate);
		      LocalDateTime dateTime = date.atStartOfDay();
		      if (!releases.contains(dateTime))
		         releases.add(dateTime);
		      releaseNames.put(dateTime, name);
		      releaseID.put(dateTime, id);
		   }

		/**
		 * Function that, given one of the available dates, derives fix version.
		 * It takes the informations needed by the csv file previously fill in. 
		 */
		static void getFixVersionIndex(Bug bug, Date fixVersion) {
			list = Utils.fileReader(projName);
			for (int i = 0; i < list.size(); i++) {
				if ((fixVersion!=null) && fixVersion.compareTo(list.get(i).getDate()) == 0){
					bug.getRelease().setFv(list.get(i).getIndex());
					break;
				}
			}
			for (int i = 0; i < list.size()-1; i++) {
				if ((fixVersion!=null) && fixVersion.compareTo(list.get(i).getDate()) > 0){
					bug.getRelease().setFv(list.get(i+1).getIndex());
				}
			}
		}
		/**
		 * Function that, given one of the available dates, derives open version.
		 * It takes the informations needed by the csv file previously fill in. 
		 */
		static void getOpenVersionIndex(Bug bug, Date openVersion) {
			list = Utils.fileReader(projName);
			for (int i = 0; i < list.size(); i++) {
				if (openVersion.compareTo(list.get(i).getDate()) >= 0){
					bug.getRelease().setOv(list.get(i).getIndex());
				}
			}
		}
		
		/**
		 * Function that, given one of the available dates, derives injected version.
		 * It takes the informations needed by the csv file previously fill in. 
		 */
		static void getInjVersionIndex(Bug bug, Date injVersion) {
			list = Utils.fileReader(projName);
			for (int i = 0; i < list.size(); i++) {
				if ((injVersion != null) && (injVersion.compareTo(list.get(i).getDate()) == 0) ){
					bug.getRelease().setIv(list.get(i).getIndex());
					break;
				}
			}
		}
		/**
		 * Function to obtain the list of releases, with the updated release's 
		 * files list. It is put the given file in the right versions lists,
		 * considering its addition and deletion dates.
		 */
		public static List<Release> getFileRelease(File file) {
			List<File> files = null;
			int initialVersion = 0;
			int finalVersion = 0;
	
			initialVersion = setInitialVersion(file,initialVersion);
			finalVersion = setFinalVersion(file,finalVersion);
			
			/* It is considered every version in which there is the given file.
			It is taken the list of files for each version and then the file is added to this list. 
			The file is added only if it is the first occurrence for that list */
			for (int i = initialVersion; i < finalVersion+1; i++) {
				files = list.get(i).getList();
				if (!files.contains(file)) {
					
					File fileVer = new File();
					fileVer.setName(file.getName());
					fileVer.setFileAdded(file.getFileAdded());
					fileVer.setFileDeleted(file.getFileDeleted());
					files.add(fileVer);
				}
				list.get(i).setList(files);
			}	
			return list;
		}


		private static int setInitialVersion(File file, int initVer) {
			int initialVersion = initVer;
			/* If the file addition date is grater then the last version date, it is taken
			this one as the initial version for the file. */
			if ((file.getFileAdded().compareTo(list.get(list.size()-1).getDate())) >= 0){
				initialVersion =  list.size()-1;
			}
			/* If the file addition date is grater then the i-th version date and lower then the 
			next one (i+1-th), it is taken this one as the initial version for the file. */
			if (initialVersion == 0) {
				for (int i = 0; i < list.size()-1; i++) {
					if ((file.getFileAdded().compareTo(list.get(i).getDate()) >= 0)
							&& (file.getFileAdded().compareTo(list.get(i+1).getDate()) < 0) ) {
						initialVersion = i;
					}
				}
			}
			return initialVersion;
		}
		
		private static int setFinalVersion(File file, int finVer) {
			int finalVersion = finVer;
			/* If the file deletion date is grater then the last version date and it's
			not null, it is taken this one as the final version for the file. */
			if ((file.getFileDeleted() != null) &&
				(file.getFileDeleted().compareTo(list.get(list.size()-1).getDate())) >= 0) {
				finalVersion = list.size()-1;
			}
			/* If the file deletion date is grater then the i-th version date and lower then the 
			next one (i+1-th), it is taken this one as the final version for the file. */
			if ((finalVersion == 0) && (file.getFileDeleted() != null)) {
				for (int i = 0; i < list.size()-1; i++) {
					if  ((file.getFileDeleted().compareTo(list.get(i).getDate()) >= 0) &&
						(file.getFileDeleted().compareTo(list.get(i+1).getDate()) < 0)) {
						finalVersion = i;
					}
				}
			}
			/* If there isn't a deletion date for the file, it is considered the last one 
			as the final version for the file*/
			if (file.getFileDeleted() == null) {
				finalVersion = list.size()-1;
			}
			return finalVersion;
		}

		
		public static void getRevisionRelease(Revision revision) {
			Release rel = new Release();
			if (revision.getDate().compareTo(list.get(0).getDate()) <= 0){
				rel.setIndex(list.get(0).getIndex());
				revision.setRelease(rel);
				return;
			}
			for (int i = 0; i < list.size()-1; i++) {
				if ((revision.getDate().compareTo(list.get(i).getDate()) > 0) &&
				(revision.getDate().compareTo(list.get(i+1).getDate()) <= 0)){
					rel.setIndex(list.get(i).getIndex());
					revision.setRelease(rel);
					break;
				}
			}
			if (revision.getDate().compareTo(list.get(list.size()-1).getDate()) > 0){
				rel.setIndex(list.get(list.size()-1).getIndex());
				revision.setRelease(rel);
			}
		}


	
}