package ec.drm.app.tutorial7;

import java.io.*;

import ec.*;
import ec.drm.ProblemData;
import ec.util.*;
import java.util.*;

public class MyData extends ProblemData
    {
	private static final long serialVersionUID = 1L;
	
    public static final String P_FILE_DATA_IN = "file-in";
    public static final String P_FILE_DATA_OUT = "file-out";
	
    public double[][] data_in;
    public double[] data_out;
    
    public void setup(final EvolutionState state, final Parameter base){
    	String file_in = (String) state.parameters.getString(base.push(P_FILE_DATA_IN), null);

    	ArrayList tmp_data_in = new ArrayList();
		System.out.print("Loading data from " + file_in + "...");
    	try{
    		BufferedReader bufferedReader = 
    			new BufferedReader(new FileReader(file_in));
            String line = bufferedReader.readLine();
            
        	while(line != null){
        		tmp_data_in.add(line);
        		line = bufferedReader.readLine();
        	}
    	}
    	catch(FileNotFoundException e){
    		Output.initialError("Data file " + file_in + " not found");}
    	catch(IOException e){
    		Output.initialError("IOException when reading " + file_in + ": " + e);}

    	data_in = new double[tmp_data_in.size()][];
    	String[] splittedLine;
    	for(int i=0; i<tmp_data_in.size(); i++){
    		splittedLine = ((String)tmp_data_in.get(i)).split(" ");
    		data_in[i] = new double[2];
    		data_in[i][0] = Double.parseDouble(splittedLine[0]);
    		data_in[i][1] = Double.parseDouble(splittedLine[1]);
    	}
    	System.out.println("OK");

    	String file_out = (String) state.parameters.getString(base.push(P_FILE_DATA_OUT), null);
    	
    	ArrayList tmp_data_out = new ArrayList();
		System.out.print("Loading data from " + file_out + "...");
    	try{
    		BufferedReader bufferedReader = 
    			new BufferedReader(new FileReader(file_out));
            String line = bufferedReader.readLine();
            
        	while(line != null){
        		tmp_data_out.add(line);
        		line = bufferedReader.readLine();
        	}
    	}
    	catch(FileNotFoundException e){
    		Output.initialError("Data file " + file_in + " not found");}
    	catch(IOException e){
    		Output.initialError("IOException when reading " + file_in + ": " + e);}

    	data_out = new double[tmp_data_out.size()];
    	for(int i=0; i<tmp_data_out.size(); i++){
    		data_out[i] = Double.parseDouble((String)tmp_data_out.get(i));
    	}
    	System.out.println("OK");
        }
    }
