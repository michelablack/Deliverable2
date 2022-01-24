package milestone_two.bean_two;
import milestone_two.bean_two.Enumeration.Classifier;
import milestone_two.bean_two.Enumeration.Feature;
import milestone_two.bean_two.Enumeration.Sampling;

public class Dataset {
	

	private String data;
	private int trainingRelease;
	private double trainingPercentage;
	private double defectiveInTrainPerc;
	private double defectiveInTestPerc;
	private Classifier classifier;
	private Sampling sampling;
	private Feature feature;
	private double truePositive;
	private double falsePositive;
	private double trueNegative;
	private double falseNegative;
	private double precision;
	private double recall;
	private double rocArea;
	private double kappa;
	private String sensType;
	
	public String getDataset() {
		return data;
	}
	public void setDataset(String dataset) {
		this.data = dataset;
	}
	public int getTrainingRelease() {
		return trainingRelease;
	}
	public void setTrainingRelease(int trainingRelease) {
		this.trainingRelease = trainingRelease;
	}
	public double getTrainingPercentage() {
		return trainingPercentage;
	}
	public void setTrainingPercentage(double trainingPercentage) {
		this.trainingPercentage = trainingPercentage;
	}
	public double getDefectiveInTrainPerc() {
		return defectiveInTrainPerc;
	}
	public void setDefectiveInTrainPerc(double defectiveInTrainPerc) {
		this.defectiveInTrainPerc = defectiveInTrainPerc;
	}
	public double getDefectiveInTestPerc() {
		return defectiveInTestPerc;
	}
	public void setDefectiveInTestPerc(double defectiveInTestPerc) {
		this.defectiveInTestPerc = defectiveInTestPerc;
	}
	public Classifier getClassifier() {
		return classifier;
	}
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	public Sampling getSampling() {
		return sampling;
	}
	public void setSampling(Sampling sampling) {
		this.sampling = sampling;
	}
	public Feature getFeature() {
		return feature;
	}
	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	public double getTruePositive() {
		return truePositive;
	}
	public void setTruePositive(double truePositive) {
		this.truePositive = truePositive;
	}
	public double getFalsePositive() {
		return falsePositive;
	}
	public void setFalsePositive(double falsePositive) {
		this.falsePositive = falsePositive;
	}
	public double getTrueNegative() {
		return trueNegative;
	}
	public void setTrueNegative(double trueNegative) {
		this.trueNegative = trueNegative;
	}
	public double getFalseNegative() {
		return falseNegative;
	}
	public void setFalseNegative(double falseNegative) {
		this.falseNegative = falseNegative;
	}
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getRecall() {
		return recall;
	}
	public void setRecall(double recall) {
		this.recall = recall;
	}
	public double getRocArea() {
		return rocArea;
	}
	public void setRocArea(double rocArea) {
		this.rocArea = rocArea;
	}
	public double getKappa() {
		return kappa;
	}
	public void setKappa(double kappa) {
		this.kappa = kappa;
	}
	
	public void setSensitivity(String sensType) {
		this.sensType = sensType;
	}
	
	public String toStringForDataset() {
		return data + ", " + trainingRelease + ", "+ trainingPercentage + ", "+defectiveInTrainPerc+", " + defectiveInTestPerc + 
				", "+ classifier + ", " + feature + ", " + sensType + ", " + sampling + ", " + truePositive+ ", " + falsePositive + 
				", " + trueNegative + ", "+ falseNegative + ", " + precision + ", " + recall + ", " + rocArea+ ", " 
				+ kappa + "\n";
	}
	
}

