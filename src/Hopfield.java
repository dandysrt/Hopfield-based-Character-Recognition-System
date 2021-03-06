import java.util.ArrayList;

import Jama.Matrix;

public class Hopfield{
	private ArrayList<double[][]> trainingList;														//list of data
	private ArrayList<String> trainingChar;															//list of labels
	protected ArrayList<String> percentageResult;													//percentage of classification for each entry  
	private int XDIMENSION, YDIMENSION;																//dimensions
	private double[][] entryCoord;																		//input matrix	
	
	QuickSort<int[]> newSort = new QuickSort<int[]>();
	
	protected int[] matchList = null;
	protected int[] posList = null;
	
	//set values needed for calculation
	private double[][] weights;																		//matrix of weights
    private double[][] thresholds;																		//used to determine thresholds
    private int numberSamples = 0;
    private int numberPatterns;
    private double THRESHOLD_CONSTANT = 0.0;
	
	public Hopfield(int multiplier){
		XDIMENSION = 7 * multiplier;
		YDIMENSION = 9 * multiplier;
	}
    
    public Hopfield(int x, int y){
    	XDIMENSION = x;
    	YDIMENSION = y;
    }
	
	public void init(ArrayList<double[][]> matricesList, double[][] myEntry, ArrayList<String> labels){
		numberSamples = XDIMENSION*YDIMENSION;														//set the number of neurons		
		numberPatterns = matricesList.size();
		
		percentageResult = new ArrayList<String>(numberPatterns);
		weights = new double[numberSamples][numberSamples];											//create the weights matrix        
        thresholds = new double[numberSamples][1];														//create the offset Neuron weights (used to determine thresholds)
        for(int i=0; i<numberSamples; i++){
        	thresholds[i][0] = THRESHOLD_CONSTANT;
        }
        //inputs
		entryCoord = new double[numberSamples][1];	
		trainingChar = new ArrayList<String>();
		trainingList = new ArrayList<double[][]>();
		makeInput(matricesList, myEntry, labels);													//takes the inputs and transforms them into arrays.
		
		calculate();																				//start calculating
	}
	
	public void calculate(){
		double[][] inputXtransp = new double[numberSamples][numberSamples];								//input multiplied by its transposed
		for(int i=0; i<trainingList.size(); i++){
			double[][] matrixT = new double[1][numberSamples];											//transposed of a matrix
			
			matrixT = transpose(trainingList.get(i));												//create the transposed matrices
			inputXtransp = sum((multiply(trainingList.get(i), matrixT)),inputXtransp);				//sum of multiplied matrices
		}
		weights = subtract(inputXtransp,identityXpattern());
		sort();
	}
		
	
	//transpose the input matrix
	public double[][] transpose(double[][] matrix){
		Matrix A = new Matrix(matrix).transpose();
		return A.getArray();
	}
	 
	//multiply 2 matrices
	public double[][] multiply(double[][] matrix1, double[][] matrix2){	       
		Matrix C = new Matrix(matrix1).times(new Matrix(matrix2));
		return C.getArray();
	}

	//sum 2 matrices
	public double [][] sum(double[][] matrix1, double[][] matrix2) {	
		Matrix C = new Matrix(matrix1).plus(new Matrix(matrix2));
		return C.getArray();
	}
	
	//subtract 2 matrices
	public double[][] subtract(double[][] matrix1, double[][] matrix2){  
        Matrix C = new Matrix(matrix1).minus(new Matrix(matrix2));
		return C.getArray();
	}	
	
	public double[][] identityXpattern(){
		double[][] A = new double[numberSamples][numberSamples];
		for (int i=0; i<numberSamples; i++) {
            	A[i][i] = numberPatterns;
        }
		return A;
    }
	
	public double[][] sign(double[][] output){
		for (int i=0; i<numberSamples; i++) {
			if (output[i][0]>=0)
				output[i][0]=1;
		    else
		    	output[i][0]=-1;
        }
		return output;
    }
	
	//==================================================== didn't test ===================================================
	
	public double[][] activateFunction(){
	
		boolean run = true;
		int count = 0;
		double[][] output = null;
		while(run){
			output = sign(subtract(multiply(weights, entryCoord), thresholds));
				for(int i = 0; i < trainingList.size(); i++){
					for(int j = 0; j < numberSamples; j++){
						if(trainingList.get(i)[j][0] == output[j][0]){
							count++;
						}
						if(count == numberSamples){
							run = false;
							for(int k = 0; k < output.length; k++){
								System.out.print(output[k][0] + " ");
								
							}
								
						}
					}
					count = 0;
				}	
	
		}
		return makeGrid(output);
	}
		
	public void sort(){
		
		matchList = new int[trainingList.size()];
		posList = new int[trainingList.size()];
		int count = 0;
		for(int i = 0; i < trainingList.size(); i++){
			for(int j = 0; j < numberSamples; j++){
				if(trainingList.get(i)[j][0] == entryCoord[j][0]){
					count++;
				}
			}
			matchList[i] = count;
			posList[i] = i;
			count = 0;
		}
		newSort.setSortArray(posList, matchList);	
		int[] returnArray = newSort.sortArray(0, matchList.length - 1, false);
		int size = returnArray.length - 1;
		int[] countArray = newSort.getRankArray();
		
		for(int i = size; i >= 0; i--){
			String valueResult = String.format("%.2f",(double)(countArray[i])/(numberSamples)*100);
			String charResult = trainingChar.get(returnArray[i]).substring(0,1).toUpperCase();
			System.out.print(charResult + ": " + valueResult + "%\n");
			percentageResult.add(charResult + ": " + valueResult + "%\n");
		}
	}
	
	

	public double[][] makeGrid(double[][] myEntry){
		double[][] grid = new double[XDIMENSION][YDIMENSION];
		int y,x,k=0;
		for(y=0;y<YDIMENSION;y++){
			for(x=0;x<XDIMENSION;x++){
				grid[y][x]=(int)myEntry[k][0];
				k++;
			}
		}
		return grid;
	}
	
	 //takes the inputs and transforms them into arrays.
	 public void makeInput(ArrayList<double[][]> matricesList, double[][] myEntry, ArrayList<String> labels){
		 int l=0;		 
		 
		 for(int i=0;i<matricesList.size();i++){
			 double[][] aux = new double[numberSamples][1];
			 for(int y=0;y<YDIMENSION;y++){
				 for(int x=0;x<XDIMENSION;x++){				 
					 aux[l++][0] = matricesList.get(i)[y][x];
				 }
			 }			 
			 trainingChar.add(labels.get(i));	 
			 trainingList.add(aux);
			 l=0;	 		 
		 }
		 
		 for(int y=0;y<YDIMENSION;y++){
			 for(int x=0;x<XDIMENSION;x++){
				 entryCoord[l++][0] = myEntry[y][x];
			 }
		 }
	 }
	 
/*	//example of usage
	public static void main(String[] args){
			@SuppressWarnings("unused")
			Hopfield hp = new Hopfield(2, 3);
			ArrayList<double[][]> matricesList = new ArrayList<double[][]>(2);
			ArrayList<String> labels = new ArrayList<String>(2);
			double[][] matrix1 = {{1,1},{1,1},{1,1}};
			double[][] matrix2 = {{-1,-1},{-1,-1},{-1,-1}};
			double[][] matrix3 = {{1,1},{1,-1},{-1,-1}};
			
			double[][] myEntry = {{1,1},{-1,-1},{1,1}};
			
			matricesList.add(matrix1);
			labels.add("A");
			matricesList.add(matrix2);
			labels.add("B");
			matricesList.add(matrix3);
			labels.add("C");
			
			hp.init(matricesList, myEntry, labels);
		}*/
}
