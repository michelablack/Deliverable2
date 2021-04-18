package logic;

import java.io.IOException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bean.Bug;
import bean.Property;
import bean.Release;

public class GetTickets {
	
	static String folder = "C:\\Users\\miche";
	static String projName;
	static String url;
	static String releaseDate = "releaseDate";
	static int injcount = 0;
	static List<Bug> bugList = new ArrayList<>();
	static List<Bug> totalBugList = new ArrayList<>();
	static int count = 0;
	
	public static void main(String[] args) throws JSONException, IOException {
		
		Integer i = 0;
		Integer j = 0;
		Integer total = 1;
		projName = Property.getInstance().getProperty("PROJECT");
		url = Property.getInstance().getProperty("LINK_GITHUB");
		String projUrl = url+projName;
		List<Date> dates = Utils.calculateMidDate(folder, projName);
		Date beginDate = dates.get(0);
		Date midDate = dates.get(1);
		
		Process p = QueryGenerator.clone(folder, projUrl);
		QueryGenerator.exception(p);
		GetReleaseInfo.main(null);
		
		do {
			j = i + 1000;
			String url = QueryGenerator.getOrderedFixedTickets(projName, i, j);
			JSONObject json = Utils.readJsonFromUrl(url);
	        JSONArray issues = json.getJSONArray("issues");
	        total = json.getInt("total");
	        
	        
	        /*Considering all the fixed tickets which are in the github repo,
	        in this way I can find the fixed version with one of the two ways.*/ 
	        for (; i < total && i < j; i++) {
	        	JSONObject obj = issues.getJSONObject(i%1000);
	        	JSONObject objField = obj.getJSONObject("fields");
	        	String key = obj.get("key").toString();
	        	Date openVersion = Utils.dateFormatter((objField.getString("created")).substring(0,10));
	        	if ((openVersion.compareTo(midDate)>0) || (openVersion.compareTo(beginDate)<0)) {
	        		continue;
	        	}
	        	Boolean b = QueryGenerator.isInGit(folder, projName, key);	
	        	if (Boolean.TRUE.equals(b)) {
	        		Bug bug = new Bug();
	        		Release rel = new Release();
	        		bug.setRelease(rel);
	        		bug.setKey(key);
	        		bug.setOpenVersion(openVersion);
	        		GetReleaseInfo.getOpenVersionIndex(bug, openVersion);
	        		setFixedVersion(bug, objField);
	        		setInjectedVersion(bug, objField);
	        	}
	        	
	        }
	        
		} while (i < total);
		Bugginess.isBuggy(totalBugList,projName, folder);
		
	}

	/**
	 * Function to find the injected version between the affected versions
	 * of a given ticket. It considers the affected versions in the json and: 
	 * 1) if there are more versions in Jira, it is chosen as injected version the
	 * first one in chronological order;
	 * 2) if there are no versions, it gets started the proportional method, in 
	 * order to find a predicted one.
	 */
	
	public static void setInjectedVersion(Bug bug, JSONObject obj) {
		Date injVersion = null;
		JSONArray objArr = obj.getJSONArray("versions");
		//if there isn't an injected version in Jira, it is called the proportional method.
		if (objArr.isEmpty()) {
			injcount++;
			Proportional.getInjectedVersion(bug, bugList, projName);
				injVersion = bug.getInjVersion();	
				//if it is null, fv < ov, so it isn't a consistent case.
				if (injVersion != null) {
					Utils.consistence(bug, totalBugList);
				}
			
		}
		/*if there is one or more versions, it is always considered the first
		element of the Version object (the lower one).
		Then it is the added ticket in the bugList, in order to have got 
		the list needed for the proportion.*/
		else {
			if (objArr.getJSONObject(0).has(releaseDate)) {
				injVersion = Utils.dateFormatter(objArr.getJSONObject(0).getString(releaseDate));
				bug.setInjVersion(injVersion);
				GetReleaseInfo.getInjVersionIndex(bug, injVersion);
				Utils.consistence(bug, bugList);
				Utils.compareFixVersion(bugList, bugList.size());
				Utils.consistence(bug, totalBugList);
			}
			else {
				Proportional.getInjectedVersion(bug, bugList, projName);
				injVersion = bug.getInjVersion();	
				//if it is null, fv < ov, so it isn't a consistent case.
				if (injVersion != null) {
					Utils.consistence(bug, totalBugList);
				}
			
			}
		}		
	}
	
	/**
	 * Function to find the fixed version of a given ticket.
	 * It can have two possibilities:
	 * 1) if there are more versions in Jira, it is chosen as fixed version the
	 * first one in chronological order;
	 * 2) if there are no versions in Jira, it is taken from GitHub, by 
	 * considering the last commit's date for that ticket.
	 */
	public static void setFixedVersion(Bug bug, JSONObject obj) {
		Date fixVersion = null;
		JSONArray objArr = obj.getJSONArray("fixVersions");
		//if there isn't a fixVersion in Jira, it is searched in GitHub
		if (objArr.isEmpty()) {
			fixVersion = QueryGenerator.getFixedVersion(folder, bug.getKey(), projName);
			if (fixVersion!=null) {
				bug.setFixVersion(fixVersion);	
				GetReleaseInfo.getFixVersionIndex(bug, fixVersion);
			}
		}
		/*if there are more fixVersions, it is always considered the last
		element of the fixVersion object (the lower one). */
		else {
			if (objArr.length() > 1) {
				if (objArr.getJSONObject(objArr.length()-1).has(releaseDate)) {
					fixVersion = Utils.dateFormatter(objArr.getJSONObject(objArr.length()-1)
							.getString(releaseDate));
					bug.setFixVersion(fixVersion);
					GetReleaseInfo.getFixVersionIndex(bug, fixVersion);
				}
				else {
					fixVersion = QueryGenerator.getFixedVersion(folder, bug.getKey(), projName);
					bug.setFixVersion(fixVersion);
					GetReleaseInfo.getFixVersionIndex(bug, fixVersion);
				}
			}
			else {
				if (objArr.getJSONObject(0).has(releaseDate)) {
					fixVersion = Utils.dateFormatter(objArr.getJSONObject(0).getString(releaseDate));
					bug.setFixVersion(fixVersion);
					GetReleaseInfo.getFixVersionIndex(bug, fixVersion);
				}
				else {
					fixVersion = QueryGenerator.getFixedVersion(folder, bug.getKey(), projName);
					bug.setFixVersion(fixVersion);
					GetReleaseInfo.getFixVersionIndex(bug, fixVersion);
				}
			}	
		}	
	}
	
	
}
