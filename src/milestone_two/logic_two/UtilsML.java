package milestone_two.logic_two;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import milestone_one.bean.Release;
import milestone_two.bean_two.Dataset;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class UtilsML {
	
	
	private UtilsML() {
		super();
	}

	public static void csvToArff(String projName) {
		// load CSV
		Instances data = null;
	    CSVLoader loader = new CSVLoader();
	    try {
			loader.setSource(new File(projName+"Dataset.csv"));
			data = loader.getDataSet();//get instances object
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    // save ARFF
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(data);//set the dataset we want to convert
	    //and save as ARFF
	    try {
	    	saver.setFile(new File(projName+"Dataset.arff"));
			saver.writeBatch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<Release> datasetReader(String projName) {
		int column = 0;
		String row = null;
		FileReader fileReader = null;
		List<Release> listRelease = new ArrayList<>();
		List<milestone_one.bean.File> listFile = null;
		try {
			fileReader = new FileReader(projName + "Dataset.csv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader csvReader = new BufferedReader(fileReader);
		try {
			while ((row = csvReader.readLine()) != null) {
				String[] data = row.split(",");
				Release release = new Release();
				milestone_one.bean.File file = new milestone_one.bean.File();
				if (column > 0) {
					int index = Integer.parseInt(data[0]);
					release.setIndex(index);
					file.setNum(column);
					file.setName(data[1]);
					file.setNr(Float.parseFloat(data[2]));
					file.setnAuthors(Float.parseFloat(data[3]));
					file.setLocAdded(Float.parseFloat(data[4]));
					file.setAvgLocAdded(Float.parseFloat(data[5]));
					file.setMaxLocAdded(Float.parseFloat(data[6]));
					file.setChurn(Float.parseFloat(data[7]));
					file.setAvgChurn(Float.parseFloat(data[8]));
					file.setMaxChurn(Float.parseFloat(data[9]));
					file.setChgSetSize(Float.parseFloat(data[10]));
					file.setAvgChgSet(Float.parseFloat(data[11]));
					file.setMaxChgSet(Float.parseFloat(data[12]));
					file.setIsBuggy(data[13]);
					int lenght = listRelease.size();
					listFile = new ArrayList<>();
					if (lenght!= 0) {
						if (listRelease.get(lenght-1).getIndex()!= release.getIndex()) {
							listFile = new ArrayList<>();
							listRelease.add(release);
						}
					}
					else {
						listRelease.add(release);
					}
					listFile.add(file);
					release.setList(listFile);
					
					
				}
			    column += 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listRelease;
	}
	
	public static void datasetWriter(List<Dataset> datasets, String projName) {
		String outname = projName + "MachineLearning.csv";
		try (FileWriter fileWriter = new FileWriter(outname)){
	            fileWriter.append("Dataset, #TrainingRelease, %training, %Defective in training, %Defective in testing, Classifier, "  
	            						+"Feature Selection, Balancing, TP, FP, TN, FN, Precision, Recall, ROC Area, Kappa");
	            fileWriter.append("\n");
	            for (Dataset dataset : datasets) {
	    			fileWriter.append(dataset.toStringForDataset());
	    		}
		}catch (Exception e) {
            e.printStackTrace();
		}
	}
}
