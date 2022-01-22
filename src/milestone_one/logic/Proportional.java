package milestone_one.logic;

import java.util.List;

import milestone_one.bean.Bug;
import milestone_one.bean.Release;

public class Proportional {
	
	static double fv;
	static double iv;
	static double ov;
	static String projname;

	private Proportional() {
		super();
	}
	/**
	 * Function to calculate the injected version for a given ticket that hasn't got it.
	 */
	public static void getInjectedVersion(Bug bug, List<Bug> bugList, String projName) {
			projname = projName;
			List<Release> list = Utils.fileReader(projname);
			int avgP = (int)calculateProportion(bugList);
			fv = bug.getRelease().getFv();
			ov = bug.getRelease().getOv();
			if (fv >= ov) {
				iv = fv - (fv - ov)*avgP;
				for (int i = 0; i < list.size(); i++) {
					if (iv == list.get(i).getIndex()) {
						bug.setInjVersion(list.get(i).getDate());
						bug.getRelease().setIv((int) iv);
					}
				}	
			}
		}
	
	/**
	 * Function to calculate the incremental proportion for a given ticket.
	 * It is taken the List containing all the bugs with all the consistent 
	 * versions (fix, open and injected).
	 * It is used the Increment method to derive the proportion.
	 */
	private static double calculateProportion(List<Bug> bugList) {
		double p = 0;
		double pv = 0;
		double count = 1.0;
		int fvBefore = 1;
		double avgP = 0;
		//if it is the first bug to be evaluated, it hasn't got previous bugs to study.
		if (bugList.isEmpty()) {
			return 0;
		}
		//evaluating the average of p for the previous fixed ticket versions of the given one
		for (int i = 0; i < bugList.size(); i++) {
			GetReleaseInfo.getFixVersionIndex(bugList.get(i), bugList.get(i).getFixVersion());
			GetReleaseInfo.getOpenVersionIndex(bugList.get(i), bugList.get(i).getOpenVersion());
			GetReleaseInfo.getInjVersionIndex(bugList.get(i), bugList.get(i).getInjVersion());
			fv = bugList.get(i).getRelease().getFv();
			iv = bugList.get(i).getRelease().getIv();
			ov = bugList.get(i).getRelease().getOv();
			if (i>0) fvBefore = bugList.get(i-1).getRelease().getFv();
			/*considering only the possible indexes'values, in order to obtain
			a positive and consistent value of proportion*/
			if ((fv >= iv) && (fv > ov)) {
				/*considering only the previous fixed versions of the 
				 given ticket. If the actual ticket's fix version is grater
				 then its previous one in the list, it is calculated the 
				 average proportion for the consistent tickets until this index.*/
				if (fv > fvBefore) {
					pv = p/count;
					avgP = pv;
				}
				/*if the actual ticket is equal to its previous one in the list,
				 it means that, apart from the case in which it is considered 
				 the first version available, the average proportion is the one
				 calculated for the case above, because in this way it is 
				 considered only the case of previous fix tickets' versions. */
				if (fv == fvBefore && fv-1 != 0) {
						avgP = pv;
				}
				p = p + (int)(Math.round((fv - iv)/(fv - ov)));
				count++;
			}
		}
		return Math.round(avgP);
	}	
}
