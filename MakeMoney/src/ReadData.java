import java.io.*;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: nampq
 * Date: 9/28/12
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReadData {

    private static SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    
    private static SimpleDateFormat y_M = new SimpleDateFormat("yyyyMM");

    public void readFromFile(String filePath, String startDate) {
    	try{
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(filePath);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            // Create file
            FileWriter outFile = new FileWriter("db.csv", true);
            //FileWriter dbbkFile = new FileWriter("db_bk.csv", true);
            FileWriter normalFile = new FileWriter("normal.csv", true);
            FileWriter dataFile = new FileWriter("data.csv", true);
            //FileWriter normalbkFile = new FileWriter("normal_bk.csv", true);
            FileWriter dailyFile = new FileWriter("daily.csv", true);
            FileWriter dailyFile2 = new FileWriter("daily2.csv", true);
            BufferedWriter dbOut = new BufferedWriter(outFile);
            //BufferedWriter dbbkOut = new BufferedWriter(dbbkFile);
            //BufferedWriter normalbkOut = new BufferedWriter(normalbkFile);
            BufferedWriter normalOut = new BufferedWriter(normalFile);
            BufferedWriter dataOut = new BufferedWriter(dataFile);
            BufferedWriter dailyOut = new BufferedWriter(dailyFile);
            BufferedWriter dailyOut2 = new BufferedWriter(dailyFile2);
            
            //calculate date
            Date start = df.parse(startDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            String day = df.format(start);

            String line;
            int i =0;
            //Read File Line By Line
            String dbStr = "";
            String norStr = "";
            String daily = "=================== Ngay " + day + " =================== \n";
            String daily2 = "=================== Ngay " + day + " =================== \n";
            while ((line = br.readLine()) != null)   {
                // Print the content on the console
                System.out.println (line);
                
                if (line == null) continue;
                
                if (line.indexOf("==") >= 0) {// Dac diem nhan biet start ngay moi'
                	cal.add(Calendar.DAY_OF_MONTH, 1);
                	day = df.format(cal.getTime());
                	daily += "=================== Ngay " + day + " =================== \n";
                	daily2 += "=================== Ngay " + day + " =================== \n";
                	dbStr += "\n";
                	norStr += "\n";
                	i = 0;
                	continue;
                }
                //Neu dung ham nay khi read file "daily.csv" can sua lai
                //case "Giai DB" truoc khi in ra output
                String strLine = convertInputToOutput(line);
                System.out.println("Convert the output: " + strLine);
                if(strLine != ""){
                    //For special
                    if(i == 0) {
                    	line =  "Giai DB: " + line;
                        dbStr += day + " ;" +strLine;
                        norStr += day + " ;" +strLine;
                    } else {
                    	//For normal
                    	norStr += strLine;
                    }
                }
                //Start build the "daily2.txt"
                if(line != ""){
                	daily += line + "\n";
                	
                	String[] arr = line.split(" ");
                	//Pattern pattern = Pattern.compile("^(\\d+.*|-\\d+.*)");
                	
                	//search for text
                	Pattern pattern = Pattern.compile("\\w.*");
                    Matcher matcher = null;
                	for(String num : arr) {
                		matcher = pattern.matcher(num);
                		//if not match then it's the number
                		if(!matcher.matches() || Character.isDigit(num.charAt(0))){
                			daily2 += num.trim() + " \n";
                		}
                	}
                }//end of build "daily2.txt"
                
                //go to next line
                i++;
            }//end of read data
            
            //write to file
            if(dbStr != "" && norStr != "") {
                dbOut.write(dbStr.substring(0, dbStr.length()) +" \n");
                normalOut.write(norStr.substring(0, norStr.length()) +" \n");
                dataOut.write(norStr.substring(0, norStr.length()) +" \n");
                
                //dbbkOut.write(dbStr.substring(0, dbStr.length()-1) +" \n");
                //normalbkOut.write(norStr.substring(0, norStr.length()-1) +" \n");
                
            }
            if(daily != ""){
            	dailyOut.write(daily + "\n");
            	dailyOut2.write(daily2 + "\n");
            }
            //Close the input & outputs stream
            in.close();
            normalOut.close();
            dbOut.close();
            //dbbkOut.close();
            //normalbkOut.close();
            dailyOut.close();
            dailyOut2.close();
            dataOut.close();
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public String convertInputToOutput(String input) {
        String out = "";
        try{
            if(input != null){
            	//If seperator is "-"
            	String[] arr = input.trim().split("-");
            	//If seperator is ";"
                if(arr.length <= 2) arr = input.trim().split(" ");
                //If line is text
                //Case read tu daily.csv can fix 
                if(input.startsWith("G") || input.startsWith("g")) {
                	return out;
                }
                
                int length = arr.length;
                for (int i = 0; i < length; i++){
                    String item = arr[i].trim();
                    if(item != "" && (item.length() >= 2)){
                        String num = item.substring(item.length()-2);
                        out +=  num + ";";
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return out ;
    }

    /**
     * Read data from DATA file
     * @param startDate
     * @param endDate
     * @return Map[Date -> lineData]
     */
    public Map<String, String> readData(String startDate, String endDate, String filePath) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        try{
            //Get dates
            List<String> keys = getKeys(startDate, endDate);

            // Open the file
            FileInputStream fstream = new FileInputStream(filePath);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null)   {
//                String[] arr = strLine.trim().split(";");
//                if(arr.length > 0){
//                    String date = arr[0].trim();
//
//                }
                String key = strLine.substring(0, strLine.indexOf(";")-1).trim();
                if(keys.contains(key)){
                    strLine = strLine.substring(strLine.indexOf(";")+1);
                    //System.out.println(key +" = "+strLine);
                    result.put(key, strLine);
                }
            }

        }catch (Exception e){
            System.err.println("readData: " + e.getMessage());
        }
        return result;
    }
    
    public String lookup2Number(String startDate, String endDate, String num1, String num2) {
    	String result = "";
    	try {
			Map<String, String> map = readData(startDate, endDate, "data.csv");
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()){
				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
				String key = entry.getKey();
				String value = entry.getValue();
				if(value != null && value != ""){
					if(value.contains(num1) && value.contains(num2)){
						result += key + ": " + num1 + ", " + num2 + "; \n";
					}
				}
				
			}
			
			
		} catch (Exception e) {
			System.out.println("lookup2Number has an error: "+e.getMessage());
		}
    	return result;
    }
    
    public String lookup3Number(String startDate, String endDate, String num1, String num2, String num3) {
    	String result = "";
    	try {
			Map<String, String> map = readData(startDate, endDate, "data.csv");
			Iterator it = map.entrySet().iterator();
			while (it.hasNext()){
				Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
				String key = entry.getKey();
				String value = entry.getValue();
				if(value != null && value != ""){
					if(value.contains(num1) && value.contains(num2) && value.contains(num3)){
						result += key + ": " + num1 + ", " + num2 + ", " + num3 + "; \n";
					}
				}
				
			}
			
			
		} catch (Exception e) {
			System.out.println("lookup3Number has an error: "+e.getMessage());
		}
    	return result;
    }
    
    public String compareDayInMonth(String startDate, String endDate, String day) {
    	Calendar cal = Calendar.getInstance();
    	String result = "";
        try{
            Date start = df.parse(startDate);
            Date end = df.parse(endDate);
            cal.setTime(start);
            while (cal.getTime().before(end)){
                //key is the DATE in format "yyyyMMdd"
                String key1 = y_M.format(cal.getTime()) + day;
                String month1 = ""+ (cal.get(Calendar.MONTH) + 1);
                cal.add(Calendar.MONTH,1);
                String month2 = "" + (cal.get(Calendar.MONTH) + 1);
                String key2 = y_M.format(cal.getTime()) + day;
                result += "Thang "+month1 + "-" + month2 + "\n";
                result += compare2Date(key1, key2) + "\n";
            }
            
        }catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * Read data from DATA file
     * @param startDate
     * @param endDate
     * @return
     */
    public String compare2Date(String date1, String date2) {
        String result = "";
        try{
            // Open the file
            FileInputStream fstream = new FileInputStream("compare2Date.csv");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            String[] arr1 = {};
            String[] arr2 = {};
            while ((strLine = br.readLine()) != null)   {
                String key = strLine.substring(0, strLine.indexOf(";")-1).trim();
                if(date1.equalsIgnoreCase(key)){
                    strLine = strLine.substring(strLine.indexOf(";")+1);
                    //System.out.println(key +" = "+strLine);
                    arr1 = strLine.split(";");
                }
                if(date2.equalsIgnoreCase(key)){
                	strLine = strLine.substring(strLine.indexOf(";")+1);
                    //System.out.println(key +" = "+strLine);
                    arr2 = strLine.split(";");
                }
            }
            
            for(int i=0;i < arr1.length; i++){
            	String num1 = arr1[i].trim();
            	for(int j=0;j < arr2.length; j++){
            		String num2 = arr2[j].trim();
            		if(num1.equalsIgnoreCase(num2)){
            			result += num1 + " ; ";
            		}
            	}
            }
            

        }catch (Exception e){
            System.err.println("readData: " + e.getMessage());
        }
        //System.out.println("Cac so trung nhau la: "+result);
        return result;
    }

    public List<String> getKeys(String startDate, String endDate) {
        List<String> result = new ArrayList<String>();
        Calendar cal = Calendar.getInstance();
        try{
            Date start = df.parse(startDate);
            Date end = df.parse(endDate);
            cal.setTime(start);
            while (cal.getTime().before(end)){
                //key is the DATE in format "yyyyMMdd"
                String key = df.format(cal.getTime());
                result.add(key);
                cal.add(Calendar.DATE,1);
               // System.out.println(key);
            }
            result.add(df.format(end));
        }catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        return result;
    }


    public static void main(String[] args) {
        new ReadData().readFromFile("input.csv", args[0]);
        //List<String> dates = new ReadData().getKeys("20120801","20120902");
        //System.out.println(dates.get(dates.size()-1));
        //new ReadData().readData("20120801", "20120902", "data.csv");
    	//System.out.println(new ReadData().compareDayInMonth(args[0], args[1], args[2]));
    	//System.out.println(new ReadData().lookup3Number(args[0].trim(), args[1].trim(), args[2].trim(), args[3].trim(), args[4]));
    }
}
