package milestone_one.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import milestone_one.bean.File;
import milestone_one.bean.Release;
import milestone_one.bean.Revision;

public class QueryGenerator {
	
	static String git = "git -C ";
    static String nullProcess = "process is null";
    private static final Logger log = Logger.getLogger(QueryGenerator.class.getName());
    private static String ioException = "IOException in QueryGenerator.";
    private static String interruptedException = "InterruptedException in QueryGenerator.";
    
	private QueryGenerator() {
	   	  super();
	}
	
	/**
	 * Function to obtain, through rest API, all the tickets that have been fixed,
	 * ordered by ascending created date.
	 * @return the String from which the Json is derived. 
	 */
	public static String getOrderedFixedTickets(String projName, Integer i, Integer j) {
		
		return  "https://issues.apache.org/jira/rest/api/2/search?jql="
				+ "project=%22"+ projName +"%22AND%22issueType%22=%22Bug%22AND"
				+ "(%22status%22=%22closed%22OR%22status%22=%22resolved%22)AND%22"
				+ "resolution%22=%22fixed%22%20ORDER%20BY%20created%20ASC&fields=key,"
				+ "resolutiondate,versions,fixVersions,created&startAt="+ i.toString() +"&maxResults="+ j.toString();
	}
	
	/**
	 * Function to retrieve the all the commits from a given ticket.
	 */
	public static List<String> getCommits(String key, String projName, String folder) {
		String s = null;
		String command = git+folder+"\\"+projName+" log --pretty=format:\"%H\" --grep=";
		Process pDot = process(command+"\""+key+". \"");
		Process pColon = process(command+key+":");
		exception(pDot);
		exception(pColon);
		BufferedReader stdInputDot = new BufferedReader(new 
                InputStreamReader(pDot.getInputStream()));
		BufferedReader stdInputColon = new BufferedReader(new 
                InputStreamReader(pColon.getInputStream()));
		List<String> list = new ArrayList<>();
        try {
			while ((s = stdInputDot.readLine()) != null) {
			list.add(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			while ((s = stdInputColon.readLine()) != null) {
			list.add(s);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException , e);
		}
        return list;
		
	}
	
	/**
	 * Function to list all the files .java in the project.
	 */
	public static List<Release> listFiles(String projName, String folder){
		String line = null;
		String prevLine = null;
		Process p = processNoWait(git +folder+"\\"+projName+" --no-pager log --pretty=format:\"\" --name-only *.java");
		exception(p);
		BufferedReader stdInput = new BufferedReader(new 
                InputStreamReader(p.getInputStream()));
		List<Release> releaseList = null;
        try {
        	while ((line = stdInput.readLine()) != null || prevLine != null) {
				if (line != null && !line.equals("")) {
					File file = new File();
					file.setName(line);
					if (getFilesDates(file, projName, folder)) {
						releaseList = GetReleaseInfo.getFileRelease(file);
					}
				}
				prevLine = line;
        	}	
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException , e);
		}
      
        return releaseList;
	}
	
	/**
	 * Function to retrieve the all the files from a given commit,
	 * considering only the .java ones.
	 */
	public static List<File> getFiles(String commit, String projName, String folder) {
		String s = null;
		Process p  = processNoWait(git +folder+"\\"+projName+" diff-tree --no-commit-id --name-only -r "
		+commit+" *.java");
		exception(p);
		BufferedReader stdInput = new BufferedReader(new 
                InputStreamReader(p.getInputStream()));
		List<File> list = new ArrayList<>();
        try {
			while ((s = stdInput.readLine()) != null) {
				File file = new File();
				file.setName(s);
				list.add(file);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException , e);
		}
        return list;
		
		
	}
	
	/**
	 * Function to retrieve the addition's and deletion's dates for the given file.
	 */
	public static boolean getFilesDates(File file, String projName, String folder) {
		String fileName = file.getName();
		String s = null;
		Process p  = process(git +folder+"\\"+projName+" log --diff-filter=A --diff-filter=D "
		+"--pretty=format:\"%cs\" -- "+fileName);
		exception(p);
		List<Date> list = new ArrayList<>();
		BufferedReader stdInput = new BufferedReader(new 
                InputStreamReader(p.getInputStream()));
        try {
			while ((s = stdInput.readLine()) != null) {
				list.add(Utils.dateFormatter(s));
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException , e);
		}
        if (list.isEmpty()) {
        	return false;
        }
        if (list.size() == 1) {
        	file.setFileAdded(list.get(0));
        }
        else {
        	file.setFileDeleted(list.get(0));
           	file.setFileAdded(list.get(1));
        }
        return true;
	}
	
	/**
	 * Function to get the fixed version of a ticket from the GitHub's repository.
	 */
	public static Date getFixedVersion(String folder, String key, String projName) {
		String sDot = null;
		String sColon = null;
		String command = git +folder+"\\"+projName+" --pretty=format:'%ad' --date=iso -1 log --grep=";
		Process pDot = process(command+"\""+key+". \"");
		Process pColon = process(command+key+":");
		exception(pDot);
		exception(pColon);
		BufferedReader stdInputDot = new BufferedReader(new 
                InputStreamReader(pDot.getInputStream()));
		BufferedReader stdInputColon = new BufferedReader(new 
                InputStreamReader(pColon.getInputStream()));
        try {
			sDot = stdInputDot.readLine();
			sColon = stdInputColon.readLine();
			if ((sDot!=null) &&(!sDot.equals(sColon))) {
	        	return Utils.dateFormatter(sDot.substring(1, 10));
	        }
			else if (sColon!=null) {
				return Utils.dateFormatter(sColon.substring(1, 10));
			}
			
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException , e);
		}
		return null;
        
	}
	
	/**
	 * Function to get all the commits, commit's dates and authors of the available
	 * revisions for the project, putting them inside the object Revision.
	 * @return a list of Revision objects
	 */
	public static List<Revision> getRevisions(String folder, String projName){
		String s = null;
		Process p = QueryGenerator.processNoWait(git +folder+"\\"+projName+" --no-pager "
				+ "log --pretty=format:\"%cs,%H,%an\" --reverse");
		QueryGenerator.exception(p);
		BufferedReader stdInput = new BufferedReader(new 
                InputStreamReader(p.getInputStream()));
		List<Revision> revisionList =  new ArrayList<>();
        try { 
			while ((s = stdInput.readLine()) != null) {
				String [] commit = s.split(",");
				Revision revision = new Revision();
				revision.setDate(Utils.dateFormatter(commit[0]));
				revision.setCommit(commit[1]);
				//In ZookKeeper there are commits without authors
				if (commit.length>2) {
					revision.setAuthor(commit[2]);
				}
				GetReleaseInfo.getRevisionRelease(revision);
				revisionList.add(revision);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException , e);
		}
        return revisionList;
	}
	
	/**
	 * Function to get all the changes between two following revisions.
	 * In particular, the git command returns an information like:
	 * -Number of lines added -Number of lines deleted -File name touched.
	 * A file list, with metrics added for each file, is added to every
	 * revision.
	 */
	public static void getRevisionsChanges(List<Revision> revisionList, String folder, String projName) {
		String s = null;
		List<File> files = new ArrayList<>();
		List<File> allFiles = new ArrayList<>();
		revisionList.get(0).setFiles(files);
		for (int i = 0; i < revisionList.size()-1; i++) {
			Process p = QueryGenerator.processNoWait(git +folder+"\\"+projName+" --no-pager "
					+ "diff --numstat "+revisionList.get(i).getCommit()+ " "+revisionList.get(i+1).getCommit()+ " *.java");
			QueryGenerator.exception(p);
			BufferedReader stdInput = new BufferedReader(new 
	                InputStreamReader(p.getInputStream()));
	        try {
				while ((s = stdInput.readLine()) != null) {
					String[] changes = s.split("\\t");
					Metrics.locAddedAndChurn(changes, files, allFiles);
				}
			Metrics.nAuthors(revisionList.get(i).getAuthor(),files, allFiles);
			Metrics.changeSet(files, allFiles);
			List<File> supportList = new ArrayList<>();
			supportList.addAll(files);
			files.clear();
			revisionList.get(i+1).setFiles(supportList);
			} catch (IOException e) {
				log.log(Level.SEVERE, ioException , e);
			}
		}
	}
	
	/**
	 * Function to catch if a given ticket is in Github
	 */
	public static Boolean isInGit(String folder, String projName, String key) {
		String command = git +folder+"\\"+projName+" --no-pager log --grep=";
		Process pDot = processNoWait(command+"\""+key+". \"");
		Process pColon = processNoWait(command+key+":");
		exception(pDot);
		exception(pColon);
		BufferedReader stdInputDot = new BufferedReader(new 
                InputStreamReader(pDot.getInputStream()));
		BufferedReader stdInputColon = new BufferedReader(new 
                InputStreamReader(pColon.getInputStream()));
		try {
			String lineDot = stdInputDot.readLine();
			String lineColon = stdInputColon.readLine();
	        if(lineDot==null && lineColon==null) {
		        return false;
		    }
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException , e);
		}
		return true;
	}
	
	
	//return the date of the first and last commit of the project
		public static List<String> lifeProject(String folder, String projName) throws IOException {	
			//Command log commit prop.getProperty(directoryM2) "..\\..\\bookkeeper\\"
			
			String commandEndDate = git+" "+ folder+"\\"+projName+" log --pretty=format:\"%cd\" --date=iso-strict --max-count=1";
			String commandBeginDate = git+" "+ folder+"\\"+projName+" rev-list --reverse --max-parents=0 HEAD --pretty=format:\"%cd\" --date=iso-strict";
			Process pBegin = null;
			Process pEnd = null;
			List<String> date = new ArrayList<>();
			String dateBegin = null;
			String dateEnd = null;
			String line;
			try {
				//execute command
				pBegin = Runtime.getRuntime().exec(commandBeginDate);
				pEnd = Runtime.getRuntime().exec(commandEndDate);
				pBegin.waitFor();
				pEnd.waitFor();
			} catch (IOException e) {
				log.log(Level.SEVERE, ioException , e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			exception(pEnd);
			exception(pBegin);
			BufferedReader inputBegin = new BufferedReader(new InputStreamReader(pBegin.getInputStream()));
			BufferedReader inputEnd = new BufferedReader(new InputStreamReader(pEnd.getInputStream()));
			try {
				int count = 0;
				while ((line = inputBegin.readLine())!= null) {
					//extract date in form 'yyyy-mm'
					if (count==1) {
						dateBegin = line.substring(0,10);
						break;
					}
					count++;				
				}
				inputBegin.close();
			} catch (IOException e) {
				log.log(Level.SEVERE, ioException , e);
			}
			try {
				while ((line = inputEnd.readLine()) != null) {
					//extract date in form 'yyyy-mm'
					dateEnd = line.substring(0,10);				
				}
				inputEnd.close();
			} catch (IOException e) {
				log.log(Level.SEVERE, ioException , e);
			}
			date.add(dateBegin);
			date.add(dateEnd);
			return date;
		}
	
	public static Process clone(String folder, String projUrl) {
		Process p = process(git +folder+ " clone " +projUrl);
		exception(p);
		return p;
	}
	
	public static Process processNoWait (String code) {
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(code);
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException , e);
			Thread.currentThread().interrupt();
		} 
		return p;
	}
	public static Process process (String code){  
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(code);
			interrupt(p);
		} catch (IOException e) {
			log.log(Level.SEVERE, ioException , e);
			Thread.currentThread().interrupt();
		} 
		return p;
	}
	
	static void interrupt(Process p) {
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, interruptedException , e);
			Thread.currentThread().interrupt();
		}
	}
	
	static void exception(Process p) {
		if (p == null) {
			throw new IllegalStateException(nullProcess);
		}
	}

	

	

}
