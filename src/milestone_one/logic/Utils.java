package milestone_one.logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import milestone_one.bean.Bug;
import milestone_one.bean.File;
import milestone_one.bean.Release;
import milestone_one.bean.Revision;

public class Utils {
	
	private static List<Release> releaseList = new ArrayList<>();
	private static final Logger log = Logger.getLogger(Utils.class.getName());
	private static String exception = "Exception in Utils.";
	private static String ioException = "IOException in Utils.";
	private static String fileNotFoundException = "FileNotFoundException in Utils.";
	
	private Utils() {
	   	  super();
	}
	
	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	      InputStream is = new URL(url).openStream();
	      try(BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
	         String jsonText = readAll(rd);
	         return new JSONObject(jsonText);
	       } finally {
	         is.close();
	       }
	}
	   
	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
	     InputStream is = new URL(url).openStream();
	     try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))){
	        String jsonText = readAll(rd);
	        return new JSONArray(jsonText);
	     }
	  }
	private static String readAll(Reader rd) throws IOException {
		
	      StringBuilder sb = new StringBuilder();
	      int cp;
	      while ((cp = rd.read()) != -1) {
	         sb.append((char) cp);
	      }
	      return sb.toString();
	   }
	
	public static Date dateFormatter(String str){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = formatter.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
		
	}
	
	public static List<Date> calculateMidDate(String folder, String projName) throws IOException {
		
		List<Date> dates = new ArrayList<>();
		ZoneId defaultZoneId = ZoneId.systemDefault();
		List<String> lifeProject = QueryGenerator.lifeProject(folder, projName);
		LocalDate dateBeginLoc = LocalDate.parse(lifeProject.get(0));
		LocalDate dateEndLoc = LocalDate.parse(lifeProject.get(1));
		long numDays = ChronoUnit.DAYS.between(dateBeginLoc, dateEndLoc);
		LocalDate midDateLoc = dateBeginLoc.plusDays(numDays/2L);
		//middle day of project
		Date dateBegin = Date.from(dateBeginLoc.atStartOfDay(defaultZoneId).toInstant());
		Date midDate = Date.from(midDateLoc.atStartOfDay(defaultZoneId).toInstant());
		dates.add(dateBegin);
		dates.add(midDate);
		
		return dates;
	}
	
	public static void compareFixVersion(List<Bug> bugList, int n) {
		for (int index = 0; index < n-1; index++) {
			//comparing the bugs'fixed versions in bugList
			if ((bugList.get(index)).getFixVersion().
					compareTo((bugList.get(index+1)).getFixVersion()) > 0) {
				//swap 
	            Collections.swap(bugList, index, (index+1));
	        }
	    }
        if (n - 1 > 1) {
        	compareFixVersion(bugList, n-1);
        }  
	}
	
	public static List<Release> fileReader (String projName) {
		int column = 0;
		String row = null;
		List<Release> list = new ArrayList<>();
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(projName + "VersionInfo.csv");
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, fileNotFoundException, e);
		}
		BufferedReader csvReader = new BufferedReader(fileReader);
		try {
			while ((row = csvReader.readLine()) != null) {
				String[] data = row.split(";");
				Release release = new Release();
				if (column > 0) {
					release.setIndex(Integer.parseInt(data[0]));
					release.setId(data[1]);
					release.setName(data[2]);
					release.setDate(Utils.dateFormatter(data[3].substring(0, 10)));
					list.add(release);
				}
			    column += 1;
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException, e);
		}
		try {
			csvReader.close();
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException, e);
		}
		return list;
	}
	
	/**public static void writeDatasetNoSum(List<File> buggyFiles, String projName, String folder) {
		setReleaseList(QueryGenerator.listFiles(projName, folder));
		List<Revision> revisionList = QueryGenerator.getRevisions(folder, projName);
		List<Revision> revisionsSupport = new ArrayList<>(); 
		List<Revision> revisions = new ArrayList<>();
		for (int r = 0; r < (releaseList.size())/2; r++) {
			int count = 0;
			for (int j=0; j< revisionList.size();j++) {
				count = j;
				if (revisionList.get(j).getRelease().getIndex()==(r+1)) {
					revisionsSupport.add(revisionList.get(j));
				}
			}
			if (!revisionsSupport.isEmpty()) {
				QueryGenerator.getRevisionsChanges(revisionsSupport, folder, projName, count);
				revisions.addAll(revisionsSupport);
				revisionsSupport.clear();
			}
		}
		setBugginess(buggyFiles, releaseList);
		String outname = projName + "Dataset_NoSum.csv";
		try(FileWriter fileWriter = new FileWriter(outname)) {
			//Name of CSV for output
            fileWriter.append("Version, File Name, NR, NAuth, Loc Added, Avg Loc Added, Max Loc Added, Churn, Avg Churn, Max Churn, "
            		+ "Chg Set Size, Avg Chg Set, Max Chg Set, Age, Buggy");
            fileWriter.append("\n");
            for (int i = 0; i < releaseList.size()/2; i++) {
            	System.out.println("I "+i);
            	for (int j = 0; j < releaseList.get(i).getList().size(); j++) {
            		File file = releaseList.get(i).getList().get(j);
            		fileWriter.append(String.valueOf(i+1));
            		fileWriter.append(",");
            		fileWriter.append(file.getName());
            		setMetrics(revisions, i, file);
            		if (file.getName().equals("bookkeeper-server/src/main/java/org/apache/bookkeeper/client/LedgerRecoveryOp.java")){
            			System.out.println("WEEKS IN WRITENOSUM "+file.getAge());
            		}
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getNum()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getnAuthors()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getSumLocAdded()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getAvgLocAdded()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getMaxLocAdded()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getChurn()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getAvgChurn()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getMaxChurn()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getChgSetSize()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getAvgChgSet()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getMaxChgSet()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getAge()));
            		fileWriter.append(",");
            		fileWriter.append(file.getIsBuggy());
            		fileWriter.append("\n");
	           }
	      }
		}catch (Exception e) {
			log.log(Level.SEVERE, exception, e);
		}
	}*/
	
	public static void writeDataset(List<File> buggyFiles, String projName, String folder) {
		setReleaseList(QueryGenerator.listFiles(projName, folder));
		List<Revision> revisionList = QueryGenerator.getRevisions(folder, projName);
		QueryGenerator.getRevisionsChanges(revisionList, folder, projName);
		setBugginess(buggyFiles, releaseList);
		String outname = projName + "Dataset.csv";
		try(FileWriter fileWriter = new FileWriter(outname)) {
			//Name of CSV for output
            fileWriter.append("Version, File Name, NR, NAuth, Loc Added, Avg Loc Added, Max Loc Added, Churn, Avg Churn, Max Churn, "
            		+ "Chg Set Size, Avg Chg Set, Max Chg Set, Buggy");
            fileWriter.append("\n");
            for (int i = 0; i < releaseList.size()/2; i++) {
            	for (int j = 0; j < releaseList.get(i).getList().size(); j++) {
            		File file = releaseList.get(i).getList().get(j);
            		fileWriter.append(String.valueOf(i+1));
            		fileWriter.append(",");
            		fileWriter.append(file.getName());
            		fillVoidFields(releaseList, i, file);
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getNum()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getnAuthors()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getSumLocAdded()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getAvgLocAdded()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getMaxLocAdded()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getChurn()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getAvgChurn()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getMaxChurn()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getChgSetSize()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getAvgChgSet()));
            		fileWriter.append(",");
            		fileWriter.append(String.valueOf(file.getMaxChgSet()));
            		fileWriter.append(",");
            		fileWriter.append(file.getIsBuggy());
            		fileWriter.append("\n");
	           }
	      }
		}catch (Exception e) {
			log.log(Level.SEVERE, exception, e);
		}
	}
	/**
	 * Function in which are considered all the files in the range of versions in which they're buggy. 
	 * If the file is in the proper release list, it gets marked as "buggy",
	 * if it's not, it means that the file has been deleted before the fix of the bug.
	 * In this case the file is considered as "buggy" until the date of deletion.
	 */
	public static void setBugginess(List<File> buggyFiles, List<Release> releaseList) {
	    // This block configure the logger with handler and formatter  
	    for (int b = 0; b < buggyFiles.size(); b++) {
			for (int i = buggyFiles.get(b).getBuggy()[0]; i < (buggyFiles.get(b).getBuggy()[1]+1); i++) {
				int index = releaseList.get(i-1).getList().indexOf(buggyFiles.get(b));
				if (index != -1) {
					releaseList.get(i-1).getList().get(index).setIsBuggy("YES");
				}
			}
		}
	}
	
	/**
	 * Function that set the metrics of the given file.
	 * It considers all the files for a certain revision in revisionList and checks 
	 * if there is the given file. If so, for the file are set as metrics those of
	 * the revisionList.
	 */
	public static void setMetrics(List<Revision> revisionList, int i, File file) {
		for (int z=0; z< revisionList.size(); z++) {
			Revision rev = revisionList.get(z);
			int ind = rev.getFiles().indexOf(file);
			if (revisionList.get(z).getRelease().getIndex()==i+1 && ind!=-1) {
				file.setSumLocAdded(rev.getFiles().get(ind).getSumLocAdded());
				file.setAvgLocAdded(rev.getFiles().get(ind).getAvgLocAdded());
				file.setMaxLocAdded(rev.getFiles().get(ind).getMaxLocAdded());
				file.setChurn(rev.getFiles().get(ind).getChurn());
				file.setAvgChurn(rev.getFiles().get(ind).getAvgChurn());
				file.setMaxChurn(rev.getFiles().get(ind).getMaxChurn());
				file.setChgSetSize(rev.getFiles().get(ind).getChgSetSize());
				file.setAvgChgSet(rev.getFiles().get(ind).getAvgChgSet());
				file.setMaxChgSet(rev.getFiles().get(ind).getMaxChgSet());
				file.setnAuthors(rev.getFiles().get(ind).getnAuthors());
				file.setNum(rev.getFiles().get(ind).getNum());
			}
		}
	}
	
	/**
	 * Function that fills the voids fields of the data-
	 * set, which are the files 
	 * for whom there hasn't been metrics' changes in the specific release.
	 * For this reason in these fields are inserted the values of the previous
	 * release for the file, that have to stay constant.
	 */
	public static void fillVoidFields(List<Release> releaseList, int i, File file) {
		if (i!=0) {
			int fileInd = releaseList.get(i-1).getList().indexOf(file);
			if (fileInd !=-1) {
				File prevFile = releaseList.get(i-1).getList().get(fileInd);
    			if (file.getSumLocAdded()==0 && file.getAvgLocAdded()==0 && file.getMaxLocAdded()==0 
    					&& file.getChurn()==0 && file.getAvgChurn()==0 && file.getMaxChurn()==0 &&
    					file.getChgSetSize()==0 && file.getAvgChgSet()==0 && file.getMaxChgSet()==0 &&
    					file.getnAuthors() == 0 && file.getNum() == 0 &&
    					(prevFile.getSumLocAdded()!=0 || prevFile.getAvgLocAdded()!=0 || prevFile.getMaxLocAdded()!=0 
    					|| prevFile.getChurn()!=0 || prevFile.getAvgChurn()!=0 || prevFile.getMaxChurn()!=0 ||
    					prevFile.getChgSetSize()!=0 || prevFile.getAvgChgSet()!=0 || prevFile.getMaxChgSet()!=0 || 
    					prevFile.getnAuthors()!=0 || prevFile.getNum()!=0)) {
    				file.setSumLocAdded(prevFile.getSumLocAdded());
    				file.setAvgLocAdded(prevFile.getAvgLocAdded());
					file.setMaxLocAdded(prevFile.getMaxLocAdded());
					file.setChurn(prevFile.getChurn());
					file.setAvgChurn(prevFile.getAvgChurn());
					file.setMaxChurn(prevFile.getMaxChurn());
					file.setChgSetSize(prevFile.getChgSetSize());
					file.setAvgChgSet(prevFile.getAvgChgSet());
					file.setMaxChgSet(prevFile.getMaxChgSet());
					file.setnAuthors(prevFile.getnAuthors());
					file.setNum(prevFile.getNum());
    			}
			}
		}
	}
	
	public static void consistence(Bug bug, List<Bug> bugList) {
		if ((bug.getFixVersion()!=null)&&(bug.getFixVersion().compareTo(bug.getInjVersion())>=0) &&
			(bug.getFixVersion().compareTo(bug.getOpenVersion())>=0) &&
			(bug.getOpenVersion().compareTo(bug.getInjVersion())>0)) {
			bugList.add(bug);
			
		}	
	}

	public List<Release> getReleaseList() {
		return releaseList;
	}

	public static void setReleaseList(List<Release> list) {
		releaseList.addAll(list);
	}
	
}
