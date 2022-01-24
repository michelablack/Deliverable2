package milestone_two.bean_two;

public class Enumeration {
	public enum Classifier {NAIVE_BAYES, RANDOM_FOREST, IBK}
	public enum Feature {NO_FEATURES_SELECTION, BEST_FIRST}
	public enum Sampling {NO_SAMPLING, OVER_SAMPLING, UNDER_SAMPLING, SMOTE}
	public enum Sensitivity {NO_COST_SENSITIVE, SENSITIVE_THRESHOLD, SENSITIVE_LEARNING}
	
	public Enumeration() {
		//Enumeration of all the possible alternatives
	}

}

