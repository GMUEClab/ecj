package ec.drm.app.tutorial8;

import java.io.*;

import ec.*;
import ec.drm.ProblemData;
import ec.util.*;
import java.util.*;

public class MyProblemData extends ProblemData
    {
	private static final long serialVersionUID = 1L;
	
    public static final String P_TRAIN_IN = "train-in";
    public static final String P_TRAIN_OUT = "train-out";
    public static final String P_TEST_IN = "test-in";
    public static final String P_TEST_OUT = "test-out";
    
    public double[][] train_in;
    public double[] train_out;
    public double[][] test_in;
    public double[] test_out;
    
    public double[][] load_data_in(String filename){
    	ArrayList tmp_data_in = new ArrayList();
    	double[][] data_in;
    	
		System.out.print("Loading data from " + filename + "...");
    	try{
    		BufferedReader bufferedReader = 
    			new BufferedReader(new FileReader(filename));
            String line = bufferedReader.readLine();
            
        	while(line != null){
        		tmp_data_in.add(line);
        		line = bufferedReader.readLine();
        	}
    	}
    	catch(FileNotFoundException e){
    		Output.initialError("Data file " + filename + " not found");}
    	catch(IOException e){
    		Output.initialError("IOException when reading " + filename + ": " + e);}

    	data_in = new double[tmp_data_in.size()][];
    	String[] splittedLine;
    	for(int i=0; i<tmp_data_in.size(); i++){
    		splittedLine = ((String)tmp_data_in.get(i)).split(" ");
    		data_in[i] = new double[2];
    		data_in[i][0] = Double.parseDouble(splittedLine[0]);
    		data_in[i][1] = Double.parseDouble(splittedLine[1]);
    	}
    	System.out.println("OK");
    	return data_in;
    }
    
    public double[] load_data_out(String filename){
    	ArrayList tmp_data_out = new ArrayList();
    	double[] data_out;
    	
		System.out.print("Loading data from " + filename + "...");
    	try{
    		BufferedReader bufferedReader = 
    			new BufferedReader(new FileReader(filename));
            String line = bufferedReader.readLine();
            
        	while(line != null){
        		tmp_data_out.add(line);
        		line = bufferedReader.readLine();
        	}
    	}
    	catch(FileNotFoundException e){
    		Output.initialError("Data file " + filename + " not found");}
    	catch(IOException e){
    		Output.initialError("IOException when reading " + filename + ": " + e);}

    	data_out = new double[tmp_data_out.size()];
    	for(int i=0; i<tmp_data_out.size(); i++){
    		data_out[i] = Double.parseDouble((String)tmp_data_out.get(i));
    	}
    	System.out.println("OK");
    	return data_out;
    }
    
    public void setup(final EvolutionState state, final Parameter base){
        train_in = load_data_in(state.parameters.getString(base.push(P_TRAIN_IN), null));
        train_out = load_data_out(state.parameters.getString(base.push(P_TRAIN_OUT), null));
        test_in = load_data_in(state.parameters.getString(base.push(P_TEST_IN), null));
        test_out = load_data_out(state.parameters.getString(base.push(P_TEST_OUT), null));
        }
    }
