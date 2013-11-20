import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;


public class LotoBridge {
	
	private static LinkedList<String> DB = new LinkedList<String>();
	
	final String DB_GIAI = "G.DB";
	
	private static String lastDate = "";
	
	static SimpleDateFormat  df = new SimpleDateFormat("yyyyMMdd");
	static SimpleDateFormat  df_reverse = new SimpleDateFormat("ddMM");
	
	@SuppressWarnings({ "unused", "unchecked" })
	public static void main(String[] args) throws Exception {
		//String file = args[0];
		String file = "temp.csv";
		LotoBridge bridge = new LotoBridge();
		
		//Map (yyyyMMdd -> Map (Giai -> Number_of_G)
		Map<String, Object> final_MetaData = bridge.readDaily(file, "");
		
		System.out.println("Last Date = "+ lastDate);
		Map<String, String> lastData = (Map<String, String>) final_MetaData.get(lastDate);
		
		//look bridge
		Map<String, Object> final_ngay_bridges = bridge.lookBridge(final_MetaData);
		//
		//Map<String, Integer> counter = new HashMap<String, Integer>();
		
		//Map : Bridge_with_counter_bigger_than_3 -> Ngay
		//TODO: Should use LinkedHashMap
		Map<String, String> final_giai_of_ngay_bigger_than_3 = new HashMap<String, String>();
		//
		Map<String, String> giai_of_ngay = new HashMap<String, String>();
		for(Entry<String, Object> _bridge : final_ngay_bridges.entrySet()) {
			String ngay = _bridge.getKey();
			Map<String, String> bridges = (Map<String, String>) _bridge.getValue();
			for(String giai_and_index : bridges.keySet()) {
				//
				String days = giai_of_ngay.get(giai_and_index);
				if(days == null){
					days = ngay;
				}else {
					if(days.indexOf(ngay) < 0) {
						days = days + "," + ngay;
					}
				}
				giai_of_ngay.put(giai_and_index, days);
				
				//
				if(days.split(",").length > 4){
					final_giai_of_ngay_bigger_than_3.put(giai_and_index, days);
				}
			}
		}

		//store final data
		String curDate = args[0];
		int loopCount = 5;
		try {
			loopCount = Integer.parseInt(args[4]);
		} catch (Exception e) {
			loopCount = 5;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(df.parse(curDate));
		for (int i =1 ; i <= loopCount; i++) {
			String tempDate = df.format(cal.getTime());
			System.out.println("Date "+ tempDate);
			
			Map<String, Object> curMetaData = bridge.readDaily(file, tempDate.trim());
			Map<String, Object> cur_ngay_bridges = bridge.lookBridge(curMetaData);
			Map<String, String> curData = (Map<String, String>) curMetaData.get(tempDate);
			Map<String, String> cur_giai_of_ngay_bigger_than_3 = new HashMap<String, String>();
			
			//Get current DATA
			LotoBridge.getCurrentData(curMetaData, cur_ngay_bridges, cur_giai_of_ngay_bigger_than_3);
			
			//Data_Current
			Map<String, Object> outputOfCurDate = new HashMap<String, Object>();
			
			//get output for current date
			getFinalData(outputOfCurDate, cur_giai_of_ngay_bigger_than_3, curData, args, tempDate);
			
			//Data_Next
	        Map<String, Object> finalData = new HashMap<String, Object>();
			getFinalData(finalData, final_giai_of_ngay_bigger_than_3, lastData, args, tempDate);
			
			LotoBridge.writeMultipleDays(outputOfCurDate, finalData, final_giai_of_ngay_bigger_than_3, tempDate);
			
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
	
	}
	
	public static void writeMultipleDays(Map<String, Object> outputOfCurDate, Map<String, Object> finalData, Map<String, String> finalBridgeMap, String curDate) throws Exception {
		// Create file
		String curFileName = df_reverse.format(df.parse(curDate));
		//String nextFileName = df_reverse.format(df.parse(curDate)) + "_next";
		FileWriter curFile = new FileWriter(curFileName, false);
		BufferedWriter curOutput = new BufferedWriter(curFile);
		
		curOutput.write("======== Current: "+ curDate + " ========= \n");


		List<String> sortedKeys = new ArrayList<String>(outputOfCurDate.keySet());
		Collections.sort(sortedKeys);
		Map<String, Object> sortedResult = new LinkedHashMap<String, Object>();
		for (String key : sortedKeys)
		{
			sortedResult.put( key , outputOfCurDate.get(key));
		}
		int count = 1;
		for (Entry<String, Object> entry : sortedResult.entrySet()) {
			String keyData = entry.getKey();

			//Map ( G.3.1_index_5_VS_G.5.6_index_4 -> string_of_days )
			Map<String, String> data = (Map<String, String>) entry.getValue();

			if (data.size() == 0) continue;

			//System.out.println(String.format("Number is %s", keyData));
			curOutput.write(String.format("Number is %s", keyData) +" \n");
			for (Entry<String, String> item : data.entrySet()) {
				//System.out.println(item.getKey()+ " [" + count + "] ");
				//System.out.println(item.getValue());
				String key = item.getKey();
				
				//TODO:
				//THIS IS BIG CHANGE OF IDEA:
				// GET "DAYS" in LAST_DATA instead of CURRENT DATA
				String days = finalBridgeMap.get(key);
				
				if (days == null) {
					//reverse key
					String[] arr = key.split("_VS_");
					String reverseKey = arr[1] + "_VS_" + arr[0];
					
					System.out.println("KEY NOT EXIST == "+ key);
					System.out.println("REVERSE KEY == "+ reverseKey);
					
					days = finalBridgeMap.get(reverseKey);
				}

				if (days.indexOf(curDate) >= 0) {
					//An tiep vao hom sau
					curOutput.write(item.getKey()+ " [" + count + "] PLUS 1 \n");
				} else {
					//NO ELSE
					curOutput.write(item.getKey()+ " [" + count + "] \n");
				}
				curOutput.write(days + " \n");
				count ++;
			}
		}
		
		//File {curDate}_next
		/*FileWriter nextFile = new FileWriter(nextFileName, false);
		BufferedWriter nextOutput = new BufferedWriter(nextFile);
		//
		nextOutput.write("======== Last: "+ lastDate + " ========= \n");
		
		sortedKeys = new ArrayList<String>(finalData.keySet());
		Collections.sort(sortedKeys);
		sortedResult = new LinkedHashMap<String, Object>();
		for (String key : sortedKeys)
		{
			sortedResult.put( key , finalData.get(key));
		}

		count = 1;
		for (Entry<String, Object> entry : sortedResult.entrySet()) {
			String keyData = entry.getKey();

			//Map ( G.3.1_index_5_VS_G.5.6_index_4 -> string_of_days )
			Map<String, String> data = (Map<String, String>) entry.getValue();

			if (data.size() == 0) continue;

			//System.out.println(String.format("Number is %s", keyData));
			nextOutput.write(String.format("Number is %s", keyData) +" \n");
			for (Entry<String, String> item : data.entrySet()) {
				//System.out.println(item.getKey()+ " [" + count + "] ");
				//System.out.println(item.getValue());
				String days = item.getValue();
				nextOutput.write(item.getKey()+ " [" + count + "] \n");
				nextOutput.write(days + " \n");
				count ++;
			}
		}*/
		
		//close Buffer
		curOutput.close();
		//nextOutput.close();
	}
	
	private static void getCurrentData(Map<String, Object> curMetaData, Map<String, Object> cur_ngay_bridges, Map<String, String> cur_giai_of_ngay_bigger_than_3) {
		Map<String, String> giai_of_ngay = new HashMap<String, String>();
		for(Entry<String, Object> _bridge : cur_ngay_bridges.entrySet()) {
			String ngay = _bridge.getKey();
			Map<String, String> bridges = (Map<String, String>) _bridge.getValue();
			for(String giai_and_index : bridges.keySet()) {
				//
				String days = giai_of_ngay.get(giai_and_index);
				if(days == null){
					days = ngay;
				}else {
					if(days.indexOf(ngay) < 0) {
						days = days + "," + ngay;
					}
				}
				giai_of_ngay.put(giai_and_index, days);
				
				//
				if(days.split(",").length > 4){
					cur_giai_of_ngay_bigger_than_3.put(giai_and_index, days);
				}
			}
		}
	}
	
	
	public static <K, V extends Comparable<? super V>> Map<K, V> 
	sortByKey( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
			@SuppressWarnings("unchecked")
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				//return (o1.getValue()).compareTo( o2.getValue() );
				return ((Comparable) ((Map.Entry) (o1)).getKey())
                        .compareTo(((Map.Entry) (o2)).getKey());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static void getFinalData(Map<String, Object> finalData, Map<String, String> giai_of_ngay_bigger_than_3, Map<String, String> lastData, String[] args, String curDate) throws ParseException {
		//
		//String agrStartDate = curDate;
		Date argDate = df.parse(curDate);
		int pre = Integer.parseInt(args[1]);
		int count = Integer.parseInt(args[2]);
		int post = Integer.parseInt(args[3]) - 1;
		
		String except = "";
		Calendar cal = Calendar.getInstance();
		cal.setTime(argDate);
		cal.add(Calendar.DAY_OF_MONTH, -count);
		for (int i = 0; i < pre; i++) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
			if (except != "") {
				except += ",";
			}
			except += df.format(cal.getTime());
		}
		
		//
		if (except != "") {
			except += ",";
		}
		
		//post
		cal = Calendar.getInstance();
		cal.setTime(argDate);
		
		if (post > 0){
			except += curDate;
		}
		
		//cal.add(Calendar.DAY_OF_MONTH, count);
		for (int i = 0; i < post; i++) {
			if (except != "") {
				except += ",";
			}
			cal.add(Calendar.DAY_OF_MONTH, 1);
			except += df.format(cal.getTime());
		}
		
		System.out.println("Except date ======= "+ except);
		
		//
		String required = "";
		cal = Calendar.getInstance();
		cal.setTime(argDate);
		cal.add(Calendar.DAY_OF_MONTH, -count);
		required += df.format(cal.getTime());
		for (int i = 1; i < count; i++) {
			if (required != "") {
				required += ",";
			}
			cal.add(Calendar.DAY_OF_MONTH, 1);
			required += df.format(cal.getTime());
		}
		
		System.out.println("required date ======= "+ required);
		
		String[] notRequired = new String[0];
		
		if (except.trim() != ""){
			notRequired = except.split(",");
		}
		Map<String, String> processed = new HashMap<String, String>();
		for(Entry<String, String> entry : giai_of_ngay_bigger_than_3.entrySet()){
			
			// e.g: key = G.3.3_index_4_VS_G.3.6_index_2
			String key = entry.getKey();
			
			if (processed.containsKey(key)) continue;
			
			String firstKey = key.substring(0,key.indexOf("_VS_"));
			String lastKey = key.substring(key.indexOf("_VS_")+4);
			
			String reverseKey = lastKey + "_VS_" + firstKey;
			
			processed.put(key, key);
			processed.put(reverseKey, reverseKey);
			
			//Get real key and index
			String realKey_1 = firstKey.substring(0, firstKey.indexOf("_index"));
			String realKey_2 = lastKey.substring(0, lastKey.indexOf("_index"));
			
			int _index_1 = Integer.parseInt(firstKey.substring(firstKey.length()-1));
			int _index_2 = Integer.parseInt(lastKey.substring(lastKey.length()-1));
			
			String firstNum = String.valueOf(lastData.get(realKey_1).charAt(_index_1 - 1));
			String lastNum = String.valueOf(lastData.get(realKey_2).charAt(_index_2 - 1));
			
			String keyData = firstNum + lastNum;
			//String reverseKeyData = lastNum + firstNum;
			
			Map<String, String> data = (Map<String, String>) finalData.get(keyData);
			//Map<String, String> reverseData = (Map<String, String>) finalData.get(reverseKeyData);
			
			if (data == null) {
				data = new HashMap<String, String>();
			}
			
			//if (reverseData != null) {
				//data.putAll(reverseData);
				//finalData.remove(reverseKeyData);
			//}
			
			//consistent KEY
			//int num1 = Integer.parseInt(keyData);
			//int num2 = Integer.parseInt(reverseKeyData);
			//if (num1 > num2) {
				//keyData = reverseKeyData;
			//}
			
			//{start_date} {pre=2} {count=6} {post=4}
			if(args.length > 0) {
				//
				if(entry.getValue().contains(required)){
					
					boolean isExit = false;
					for (String not : notRequired) {
						if (entry.getValue().contains(not)) isExit = true;
					}
					//
					if (isExit) continue;
					
					data.put(entry.getKey(), String.format("Ngay: %s", entry.getValue()));
					//
//					System.out.println(String.format("Bridge xuat hien voi dieu kien(required): %s ==> %s", 
//														entry.getKey(), firstNum + lastNum));
//					System.out.println("Vao cac ngay: "+ entry.getValue());
				}
			} else {
				//
				data.put(entry.getKey(), String.format("Ngay: %s", entry.getValue()));
				//System.out.println(String.format("Bridge [Cung vi tri] xuat hien voi tan suat >=3:  %s ==> %s", entry.getKey(), firstNum + lastNum));
				//System.out.println("Vao cac ngay: "+ entry.getValue());
			}
			
			//
			finalData.put(keyData, data);
			
		}
	}
	
	/**
	 * 
	 * @param maps
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> lookBridge(Map<String, Object> maps) throws Exception {
		//
		Map<String, Object> nextDays =  new LinkedHashMap<String, Object>();
		//copy to nextDays map
		int i =0;
		//System.out.println("Copy data ...");
		for(Entry<String, Object> data : maps.entrySet()){
			//skip the first day
			if(i == 0){
				i++;
				continue;
			}
			nextDays.put(data.getKey(), data.getValue());
			i++;
		}
		
		//===============Do look for GOLD====================//
		
		//TOTAL RESULT
		Map<String, Object> TOTAL_BRIDGES = new LinkedHashMap<String, Object>();
		
		//Loop for 1 DAY
		for(Entry<String, Object> data : nextDays.entrySet()) {
			String sDate = data.getKey();
			Date date = df.parse(sDate);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DAY_OF_MONTH, -1);
			String prevDate = df.format(cal.getTime());
			//System.out.println(String.format("Cur Date vs Prev Data: %s vs %s", sDate, prevDate));
			
			// Data of date
			Map<String, String> _data_of_date = (Map<String, String>) data.getValue();
			
			//bridge on 1 day
			Map<String, String> bridge = new HashMap<String, String>();
			
			//loop for each "giai"
			for(Entry<String, String> _data_of_giai : _data_of_date.entrySet()){
				String _g_Key = _data_of_giai.getKey();
				System.out.println("Process: "+_g_Key);
				String _g_value = _data_of_giai.getValue();
				//
				String loto = "";
				if(_g_value.length() > 3){
					loto = _g_value.substring(_g_value.length()-2);
				}else if(_g_value.length() == 3){
					//6th
					loto = _g_value.substring(1);
				}else if(_g_value.length() == 2){
					//7th
					loto = _g_value;
				}
				//start of look
				
				String firstNum = String.valueOf(loto.charAt(0));
				String lastNum = String.valueOf(loto.charAt(1));
				
				//the data of prev date
				Map<String, String> _data_of_prev_date = (Map<String, String>) maps.get(prevDate);
				
				//temp
				Map<String, String> _dau = new HashMap<String, String>(); 
				Map<String, String> _duoi = new HashMap<String, String>(); 
				for(Entry<String, String> _pre_g : _data_of_prev_date.entrySet()) {
					//loop for 1 _giai of prev date
					String _giai = _pre_g.getKey();
					String number = _pre_g.getValue();
					if(number.indexOf(firstNum) >=0){
						int index = number.indexOf(firstNum)+1;
						//
						_dau.put(_giai+"_index_"+index, firstNum);
						while(number.substring(index).indexOf(firstNum) >=0){
							//System.out.println("Loop for look next firstNum");
							//look for next "first" in "number"
							index += number.substring(index).indexOf(firstNum) + 1;
							_dau.put(_giai+"_index_"+index, firstNum);
						}
					}
					//for lastNumber
					if(number.indexOf(lastNum) >= 0){
						int index = number.indexOf(lastNum)+1;
						//
						_duoi.put(_giai+"_index_"+index, lastNum);
						while(number.substring(index).indexOf(lastNum) >=0){
							//System.out.println("Loop for look next lastNum");
							//look for next "last" in "number"
							index += number.substring(index).indexOf(lastNum) + 1;
							_duoi.put(_giai+"_index_"+index, lastNum);
						}
					}
				}
				//we have result of "dau" & "_duoi"
				//ghep bridge
				for(Entry<String, String> entry : _dau.entrySet()){
					//Get giai of firstNumber
					String first = entry.getKey();
					for(Entry<String, String> e : _duoi.entrySet()){
						String last = e.getKey();
						//(DAU -> DUOI) & (DUOI -> DAU)
						bridge.put(first + "_VS_"+last, loto);
						bridge.put(last + "_VS_"+first, loto);
					}
				}//end of ghep bridge on 1 Number / 1 G
				
			}//end "FOR" on 1 giai of prev date
			
			//put bridge to TOTAL_BRIDGES
			TOTAL_BRIDGES.put(prevDate, bridge);
		}//end "FOR" on 1 DAY
		
		
		//===============END look for GOLD====================//
		
		return TOTAL_BRIDGES;
	}
	
	/**
	 * Read data cua 1 ngay va dua vao Map (yyyyMMdd -> Map (Giai -> Number_Of_Giai))
	 * @param file path to data file
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> readDaily(String file, String curDate) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
        try{
            // Open the file
            FileInputStream fstream = new FileInputStream(file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String sDate = "";
            String strLine;
            int soNgay = 0;
            int soGiai = 0;
            int soGiaiPhu = 0;
            int n = 0;
            boolean isFinish = false;
            while ((strLine = br.readLine().trim()) != null) {
            	//
            	if(strLine.trim().isEmpty()) continue;
            	
            	Map<String, String> cac_giai_cua_ngay = new LinkedHashMap<String, String>();
            	//Keep the date
            	if(strLine.startsWith("===")){
            		
            		if (isFinish) {
            			break;
            		}
            		
            		//System.out.println("Start of day !!!!!!!!!!!!!!!!!!!!!");
            		//clean "="
            		strLine = strLine.replace("=", "");
            		//clean "Ngay"
            		strLine = strLine.replace("Ngay", "");
            		sDate = strLine.trim();
            		result.put(sDate, cac_giai_cua_ngay);
            		
            		//
            		if (sDate.trim().equals(curDate.trim())) {
            			isFinish = true;
            		}
            		
            		soNgay++;
            		soGiai=1;
            		soGiaiPhu=1;
            		n=1;
            		
            		continue;
            	} else {
            		cac_giai_cua_ngay = (Map<String, String>) result.get(sDate);
            	}
            	
            	//restric so ngay
            	if(soNgay > 70){
            		break;
            	}
            	
            	//e.g: ten_giai = G.3.1
            	String ten_giai = getTenGiai(soGiai, soGiaiPhu).trim();
            	//e.g: giai = 23875 (5 numbers)
            	String giai = strLine.trim();
            	
            	//Map ( G.3.1 -> 23875 )
            	cac_giai_cua_ngay.put(ten_giai, giai);
            	result.put(sDate, cac_giai_cua_ngay);
            	
            	if (curDate.equals("")) {
            		lastDate = sDate;
            	}
            	//
            	if(ten_giai.equalsIgnoreCase(DB_GIAI)) {
            		//
            		if(soNgay > 1){
            			DB.add(giai);
            		}
            	}
            	
            	//
            	if(isChangeGiai(n)){
            		soGiai++;
            		soGiaiPhu=0;
            	}
            	
            	if(n >= 27) {
            		//reset vars
            		//System.out.println("so giai is "+n);
            	}
            	
            	//
            	n++;
            	soGiaiPhu++;
            }
        } catch (NullPointerException npe) {
        	System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
	}
	
	public String getTenGiai(int soGiai, int soGiaiPhu) {
		String ten_giai = "";
		switch (soGiai) {
		case 1:
			ten_giai = DB_GIAI;
			break;
		case 2:
			ten_giai = "G.1." + soGiaiPhu;
			break;
		case 3:
			ten_giai = "G.2." + soGiaiPhu;
			break;
		case 4:
			ten_giai = "G.3." + soGiaiPhu;
			break;
		case 5:
			ten_giai = "G.4." + soGiaiPhu;
			break;
		case 6:
			ten_giai = "G.5." + soGiaiPhu;
			break;
		case 7:
			ten_giai = "G.6." + soGiaiPhu;
			break;
		case 8:
			ten_giai = "G.7." + soGiaiPhu;
			break;
		default:
			break;
		}
		return ten_giai;
	}
	
	public boolean isChangeGiai(int giaiThu) {
		switch (giaiThu) {
		case 1:
			return true;
		case 2:
			return true;
		case 4:
			return true;
		case 10:
			return true;
		case 14:
			return true;
		case 20:
			return true;
		case 23:
			return true;
		default:
			return false;
		}
	}

}
