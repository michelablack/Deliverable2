package milestone_two.logic_two;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import milestone_one.bean.Property;
import milestone_one.bean.Release;
import milestone_two.bean_two.Dataset;
import milestone_two.bean_two.Enumeration;
import milestone_two.bean_two.Enumeration.Classifier;
import milestone_two.bean_two.Enumeration.Feature;
import milestone_two.bean_two.Enumeration.Sampling;
import milestone_two.bean_two.Enumeration.Sensitivity;
import weka.core.Instance;
import weka.core.Instances;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.lazy.IBk;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.Filter;
import weka.filters.supervised.instance.*;

public class MachineLearning {
	
	static Instances trainingSet = null;
	static Instances testingSet = null;
	static String projName = Property.getInstance().getProperty("PROJECT");
	private static final Logger log = Logger.getLogger(MachineLearning.class.getName());
	static Enumeration.Classifier classifier = null;
	static Enumeration.Feature feature = null;
	static Enumeration.Sampling sampling = null;
	static Enumeration.Sensitivity sensitivity = null;
	static String exception= "Exception";
	static List<Dataset> datasets = new ArrayList<>();
	static int trainingRelease;
	
	public static void main(String[] args){
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		List<Release> listRelease = UtilsML.datasetReader(projName);
		UtilsML.csvToArff(projName);
		walkforward(listRelease);
		UtilsML.datasetWriter(datasets,projName);
	}
	
	/**
	 * Implementation of Walk-Forward as evaluation technique.
	 * The data-set is divided into parts in a chronological way, so that, 
	 * to predict following releases, the classifiers are trained on previous ones.
	 * @param listRelease, list of all the projects' releases.
	 */
	private static void walkforward(List<Release> listRelease) {
		DataSource source = null;
		Instances dataset = null;
	
		try {
			source = new DataSource(projName+"Dataset.arff");
			dataset = source.getDataSet();
		} catch (Exception e) {
			log.info(e.getMessage());
		}
		
		for (int i=0; i<listRelease.size()-1; i++) {
			trainingRelease = i+1;
			int size = listRelease.get(i).getList().size();
			int size2 = listRelease.get(i+1).getList().size();
			//considering as training set the previous data-set instances
			trainingSet = new Instances(dataset, 0, (int)(listRelease.get(i).getList().get(size-1).getNum()));
			//considering as testing set the following data-set instances
			testingSet = new Instances(dataset, (int)listRelease.get(i).getList().get(size-1).getNum(), 
				(int)(listRelease.get(i+1).getList().get(size2-1).getNum())-(int)(listRelease.get(i).getList().get(size-1).getNum()));
			classifierSelection();
		}
	}
	
	/**
	 * Classifier selection, based on enumeration between the alternatives
	 */
	private static void classifierSelection(){
		for (Classifier c : Enumeration.Classifier.values()) {
			classifier = c;
			featureSelection();
		}
	}
	
	/**
	 * Implementation of the Feature Selection techniques,
	 * considering the no-feature selection case and the 
	 * best-first one.
	 */
	private static void featureSelection() {
		for (Feature f : Enumeration.Feature.values()) {
			feature = f;
			Instances training = new Instances(trainingSet);
			Instances testing = new Instances(testingSet);
			
			switch (feature) {
			case NO_FEATURES_SELECTION:
				int numAttrFilteredNoFS = training.numAttributes();
				training.setClassIndex(numAttrFilteredNoFS-1);
				testing.setClassIndex(numAttrFilteredNoFS-1);
				try {
					samplingSelection(training, testing) ;
				} catch (Exception e1) {
					log.info(e1.getMessage());
				}
				break;
				
			case BEST_FIRST:
				//create AttributeSelection object
				AttributeSelection filter = new AttributeSelection();
				//create evaluator and search algorithm objects
				CfsSubsetEval eval = new CfsSubsetEval();
				GreedyStepwise search = new GreedyStepwise();
				//set the algorithm to search backward
				search.setSearchBackwards(true);
				//set the filter to use the evaluator and search algorithm
				filter.setEvaluator(eval);
				filter.setSearch(search);
				//specify the dataset
				Instances filteredTraining = null;
				Instances filteredTesting = null;
				try {
					filter.setInputFormat(training);
					//apply
					filteredTraining = Filter.useFilter(training, filter);
					
					//evaluation with filtered
					int numAttrFiltered = filteredTraining.numAttributes();
					filteredTraining.setClassIndex(numAttrFiltered - 1);
					filteredTesting = Filter.useFilter(testing, filter);
					filteredTesting.setClassIndex(numAttrFiltered - 1);
				} catch (Exception e) {
					log.info(e.getMessage());
				}
				samplingSelection(filteredTraining, filteredTesting);
				break;

			default:
				break;
			}
		}
	}
	
	/**
	 * Implementation of the Sampling Selection techniques, 
	 * by considering all the given alternatives and passing to 
	 * each one the needed parameters.
	 * @param training, folds selected as training;
	 * @param testing, folds selected as testing.
	 * @throws Exception 
	 */
	private static void samplingSelection(Instances training, Instances testing){
		for (Sampling s : Enumeration.Sampling.values()) {
			sampling = s;
			Instances filteredTraining = training;
			Instances filteredTesting = testing;
			switch (sampling) {
			case NO_SAMPLING:
				sensitivitySelection(filteredTraining, testing);
				break;
			case OVER_SAMPLING:
				String sampleSizePercent = String.valueOf(2.0*labelPercentage(filteredTraining, false));
				Resample  resample = new Resample();
				String[] optsOver = new String[]{"-B", "1.0", "-Z", sampleSizePercent, "-no-replacement"};
				try {
					resample.setOptions(optsOver);
					resample.setInputFormat(training);
					filteredTraining = Filter.useFilter(training, resample);
				} catch (Exception e) {
					log.info(e.getMessage());
				}	
				sensitivitySelection(filteredTraining, filteredTesting);
				break;
			case UNDER_SAMPLING:
				SpreadSubsample  spreadSubsample = new SpreadSubsample();
				String[] optsUnder = new String[]{ "-M", "1.0"};
				try {
					spreadSubsample.setOptions(optsUnder);
					spreadSubsample.setInputFormat(training);
					filteredTraining = Filter.useFilter(training, spreadSubsample);
				} catch (Exception e) {
					log.info(e.getMessage());
				}
				sensitivitySelection(filteredTraining, filteredTesting);
				break;
			case SMOTE:
				SMOTE smote = new SMOTE();
				try {
					smote.setInputFormat(filteredTraining);
					filteredTraining = Filter.useFilter(training, smote);
				} catch (Exception e) {
					log.info(e.getMessage());
				}
				sensitivitySelection(filteredTraining, filteredTesting);
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * Crating a cost matrix, based on weights.
	 * @param cfn, weight false negative;
	 * @param cfp, weight false positive.
	 * @return 2*2 cost matrix used for sensitive evaluation.
	 */
	private static CostMatrix costMatrix(double cfn, double cfp) {
		CostMatrix costMatrix = new CostMatrix(2);
		costMatrix.setCell(0, 0, 0.0);
		costMatrix.setCell(0, 1, cfn);
		costMatrix.setCell(1, 0, cfp);
		costMatrix.setCell(1, 1, 0.0);
		return costMatrix;
		
	}
	
	/**
	 * Selection of the classifier type for evaluation.
	 * We can have no cost sensitive classifiers or cost sensitive
	 * ones, which are further divided in those with sensitive
	 * threshold and those with sensitive learning.
	 * @param training, folds selected as training;
	 * @param testing, folds selected as testing.
	 */
	private static void sensitivitySelection(Instances training, Instances testing) {
		for (Sensitivity s : Enumeration.Sensitivity.values()) {
			sensitivity = s;
			switch (sensitivity) {
			case NO_COST_SENSITIVE:
				evaluate(training, testing, null);
				break;
			case SENSITIVE_THRESHOLD:
				evaluate(training, testing, true);
				break;
			case SENSITIVE_LEARNING:
				evaluate(training, testing, false);
				break;	
			}
		}
		
	}
	
	/**
	 * Evaluation of the classifiers accuracy, in terms of
	 * Precision, Recall, AUC and Kappa.
	 * @param training, folds selected as training;
	 * @param testing, folds selected as testing;
	 * @param sensType, representing the classifier type.
	 * @throws Exception 
	 */
	private static void evaluate(Instances training, Instances testing, Boolean sensType) {
		weka.classifiers.Classifier cl = null;
		
		switch (classifier) {
		case NAIVE_BAYES:
			cl = new NaiveBayes();
			break;
		case RANDOM_FOREST:
			cl = new RandomForest();
			break;
		case IBK:
			cl = new IBk();
			break;
		default:
			break;
		}
		Evaluation evaluation = buildModel(sensType,cl,training, testing);
		

		if (Objects.isNull(evaluation)) {
			return;
		}
		double sizeDataset =  trainingSet.numInstances() + (double)testingSet.numInstances();
		double trainingPercentage = trainingSet.numInstances()/ sizeDataset;
		
		double defectiveInTrainPerc = labelPercentage(training, true);
		double defectiveInTestPerc = labelPercentage(testing, true);
		
		Dataset dataset = new Dataset(); 
		dataset.setDataset(projName);
		dataset.setTrainingRelease(trainingRelease);
		dataset.setDefectiveInTrainPerc(defectiveInTrainPerc);
		dataset.setDefectiveInTestPerc(defectiveInTestPerc);
		dataset.setClassifier(classifier);
		dataset.setFeature(feature);
		if (sensType==null) dataset.setSensitivity("NO_COST_SENSITIVE");
		else if (Boolean.TRUE.equals(sensType)) dataset.setSensitivity("SENSITIVE_THRESHOLD");
		else dataset.setSensitivity("SENSITIVE_LEARNING");
		dataset.setTrainingPercentage(trainingPercentage);
		dataset.setSampling(sampling); 
		dataset.setTruePositive(evaluation.numTruePositives(0));
		dataset.setFalsePositive(evaluation.numFalsePositives(0)); 
		dataset.setTrueNegative(evaluation.numTruePositives(1));
		dataset.setFalseNegative(evaluation.numFalsePositives(1)); 
		dataset.setPrecision(evaluation.precision(1)); 
		dataset.setRecall(evaluation.recall(1));
		dataset.setRocArea(evaluation.areaUnderROC(1)); 
		dataset.setKappa(evaluation.kappa());
		
		datasets.add(dataset);
	}
		
	/**
	 * Building classifiers differently based on the sensitivity.
	 * @param sensType, classifier sensitivity type;
	 * @param cl, classifier;
	 * @param training, fold of data-set used as training;
	 * @param testing, fold of data-set used as testing.
	 * @return
	 */
	private static Evaluation buildModel(Boolean sensType, weka.classifiers.Classifier cl,
			Instances training, Instances testing) {
		Evaluation eval = null;
		if (sensType==null) {
			if (cl!=null) {
				try {
					cl.buildClassifier(training);
					eval = new Evaluation(testing);
					eval.evaluateModel(cl, testing);
				} catch (Exception e) {
					log.info(e.getMessage());
				}
			}
		}
		
		else {
			CostSensitiveClassifier csc = new CostSensitiveClassifier();
			CostMatrix costMatrix = costMatrix(10.0, 1.0);
			csc.setClassifier(cl);
			csc.setCostMatrix(costMatrix);
			csc.setMinimizeExpectedCost(sensType);
			if (cl!=null) {
				try {
					csc.buildClassifier(training);
					eval = new Evaluation(testing, csc.getCostMatrix());
					eval.evaluateModel(csc, testing);
				} catch (Exception e) {
					log.info(e.getMessage());
				}
			}
		}
		return eval;
	}

	/**
	 * Calculating the buggy label percentage in the selected data-set fold.
	 * This method is used both for the defecting percentage in test-set and 
	 * train-set, and for the for the over-sampling technique's sample size 
	 * percentage.
	 * @param dataset, given fold of data;
	 * @param evaluate, specifying request origin.
	 * @return prevalent label percentage.
	 */
	private static double labelPercentage(Instances dataset, boolean evaluate) {
		int numInstances = dataset.numInstances();
		double yes=0;
		double no=0;
		int numAttr = dataset.numAttributes();
		for (int i = 0; i < numInstances; i++) {
			Instance inst = dataset.instance(i);
			String buggyString = inst.stringValue(numAttr-1);
			if (buggyString.equals("YES")) {
				yes++;
			}else {
				no++;
			}
		}
		double numInstancesDouble = (double)dataset.numInstances();
		
		if (evaluate) {
			return yes/numInstancesDouble;
		}
		
		if (yes>no)
			return yes/numInstancesDouble;
		
		return no/numInstancesDouble;
	}
}
