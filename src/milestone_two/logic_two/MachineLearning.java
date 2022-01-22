package milestone_two.logic_two;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import milestone_one.bean.Property;
import milestone_one.bean.Release;
import milestone_two.bean_two.Dataset;
import milestone_two.bean_two.Enumeration;
import milestone_two.bean_two.Enumeration.Classifier;
import milestone_two.bean_two.Enumeration.Feature;
import milestone_two.bean_two.Enumeration.Sampling;
import weka.core.Instance;
import weka.core.Instances;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
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
	private static final Logger log = LoggerFactory.getLogger(MachineLearning.class.getName());
	static Enumeration.Classifier classifier = null;
	static Enumeration.Feature feature = null;
	static Enumeration.Sampling sampling = null;
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
	
	private static void walkforward(List<Release> listRelease) {
		DataSource source = null;
		Instances dataset = null;
	
		try {
			source = new DataSource(projName+"Dataset.arff");
			dataset = source.getDataSet();
		} catch (Exception e) {
			log.debug(exception);
		}
		
		for (int i=0; i<listRelease.size()-1; i++) {
			trainingRelease = i+1;
			int size = listRelease.get(i).getList().size();
			int size2 = listRelease.get(i+1).getList().size();
			trainingSet = new Instances(dataset, 0, (int)(listRelease.get(i).getList().get(size-1).getNum()));
			testingSet = new Instances(dataset, (int)listRelease.get(i).getList().get(size-1).getNum(), 
				(int)(listRelease.get(i+1).getList().get(size2-1).getNum())-(int)(listRelease.get(i).getList().get(size-1).getNum()));
		classifierSelection();
		}
	}
	
	private static void classifierSelection(){
		for (Classifier c : Enumeration.Classifier.values()) {
			classifier = c;
			featureSelection();
		}
	}
	
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
				samplingSelection(training, testing) ;
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
					log.debug(exception);
				}
				
				samplingSelection(filteredTraining, filteredTesting);
				break;

			default:
				break;
			}
		}
	}
	
	private static void samplingSelection(Instances training, Instances testing) {
		for (Sampling s : Enumeration.Sampling.values()) {
			sampling = s;
			FilteredClassifier fc = new FilteredClassifier();
			
			Instances filteredTraining = new Instances(training);
			Instances filteredTesting = new Instances(testing);
			switch (sampling) {
			case NO_SAMPLING:
				evaluate(filteredTraining, filteredTesting, null);
				break;
			case OVER_SAMPLING:
				String sampleSizePercent = String.valueOf(2.0*majorityClass(filteredTraining, false));
				Resample  resample = new Resample();
				String[] optsOver = new String[]{"-B", "1.0", "-Z", sampleSizePercent, "-no-replacement"};
				try {
					resample.setOptions(optsOver);
					resample.setInputFormat(training);
				} catch (Exception e) {
					log.debug(exception);
				}
								
				fc.setFilter(resample);
				
				evaluate(filteredTraining, filteredTesting, fc);
				break;
			case UNDER_SAMPLING:
				SpreadSubsample  spreadSubsample = new SpreadSubsample();
				String[] optsUnder = new String[]{ "-M", "1.0"};
				try {
					spreadSubsample.setOptions(optsUnder);
				} catch (Exception e) {
					log.debug(exception);
				}
				fc.setFilter(spreadSubsample);
				evaluate(filteredTraining, filteredTesting, fc);
				break;
			case SMOTE:
				SMOTE smote = new SMOTE();
				try {
					smote.setInputFormat(filteredTraining);
				} catch (Exception e) {
					log.debug(exception);
				}
				fc.setFilter(smote);
				evaluate(filteredTraining, filteredTesting, fc);
				break;
			default:
				break;
			}
		}
	}
	
	private static void evaluate(Instances training, Instances testing, FilteredClassifier fc) {
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
		Evaluation evaluation = null;
		if (fc != null) {
			
			fc.setClassifier(cl);
			
			try {
				fc.buildClassifier(training);
				evaluation = new Evaluation(testing);
				evaluation.evaluateModel(fc, testing);
			} catch (Exception e) {
				log.debug(exception);
				return;
			}
		}
		else {
			try {
				if (Objects.isNull(cl)) {
					return;
				}
				cl.buildClassifier(training);
				evaluation = new Evaluation(testing);
				evaluation.evaluateModel(cl, testing);
			} catch (Exception e) {
				log.debug(exception);
			}
		}
		if (Objects.isNull(evaluation)) {
			return;
		}
		log.info("Classifier: {}, Feature: {}, Sampling: {}, number of training release: {}", 
				classifier, feature, sampling, trainingRelease);
		log.info("Kappa: {}, Recall: {}", evaluation.kappa(), evaluation.recall(1));
	
		String confusion = evaluation.confusionMatrix()[0][0]+", "+evaluation.confusionMatrix()[0][1]+", "+evaluation.confusionMatrix()[1][0]+
				", "+evaluation.confusionMatrix()[1][1]+", "+evaluation.numTruePositives(0)+", "+ evaluation.numFalsePositives(0)+", "+ 
				evaluation.trueNegativeRate(1)+", "+evaluation.falseNegativeRate(1);
		log.info("{}", confusion); 
		String correct = String.valueOf(evaluation.correct());
		log.info("{}", correct);
		String incorrect = String.valueOf(evaluation.incorrect());
		log.info("{}", incorrect);
		String precision = String.valueOf(evaluation.precision(1));
		log.info("{}", precision);
		
		
		log.info("num of instances: {}, correct: {}, incorrect: {}, percentageCorrect: {}, percentageIncorrect:{}", 
				evaluation.numInstances(), evaluation.correct(), evaluation.incorrect(), evaluation.pctCorrect(), evaluation.pctIncorrect());
		String numTruePositives = String.valueOf(evaluation.numTruePositives(0));
		log.info("{}", numTruePositives);
		String numFalsePositives = String.valueOf(evaluation.numTruePositives(0));
		log.info("{}", numFalsePositives);
		
		double sizeDataset =  trainingSet.numInstances() + (double)testingSet.numInstances();
		double trainingPercentage = trainingSet.numInstances()/ sizeDataset;
		
		double defectiveInTrainPerc = majorityClass(training, true);
		double defectiveInTestPerc = majorityClass(testing, true);
		
		Dataset dataset = new Dataset(); 
		dataset.setDataset(projName);
		dataset.setTrainingRelease(trainingRelease);
		dataset.setDefectiveInTrainPerc(defectiveInTrainPerc);
		dataset.setDefectiveInTestPerc(defectiveInTestPerc);
		dataset.setClassifier(classifier);
		dataset.setFeature(feature);
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
	
	private static double majorityClass(Instances dataset, boolean choice) {
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
		
		if (choice) {
			return yes/numInstancesDouble;
		}
		
		if (yes>no)
			return yes/numInstancesDouble;
		return no/numInstancesDouble;
	}

}
