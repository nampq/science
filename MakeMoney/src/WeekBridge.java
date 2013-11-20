import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;


public class WeekBridge {
	
	private static LinkedList<String> DB = new LinkedList<String>();
	
	private static LinkedList<String> WEEK_DB = new LinkedList<String>();
	
	final String DB_GIAI = "G.DB";
	
	@SuppressWarnings({ "unused", "unchecked" })
	public static void main(String[] args) throws Exception {
		//String file = args[0];
		for(String db : args){
			System.out.println("Add "+db);
			WEEK_DB.add(db);
		}
		
		String file = "WeekBridge.csv";
		WeekBridge bridge = new WeekBridge();
		
		Map<String, Object> result = bridge.readDaily(file);
		//look bridge
		Map<String, Object> bridgesData = bridge.lookWeekBridge(result);
		//
		Map<String, Integer> counter = new HashMap<String, Integer>();
		//
		Map<String, String> giai_of_ngay = new HashMap<String, String>();
		
		//Map : Bridge_with_counter_bigger_than_3 -> Ngay
		Map<String, String> giai_of_ngay_bigger_than_3 = new HashMap<String, String>();
		
		Map<String, String> giai_of_ngay_bigger_than_x = new HashMap<String, String>();
		//print out
		for(Entry<String, Object> _bridge : bridgesData.entrySet()) {
			String ngay = _bridge.getKey();
			Map<String, String> giais = (Map<String, String>) _bridge.getValue();
			for(String giai_and_index : giais.keySet()) {
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
				if(days.split(",").length >= 2){
					giai_of_ngay_bigger_than_3.put(giai_and_index, days);
				}
			}
		}
		giai_of_ngay = new HashMap<String, String>();
		for (Entry<String, Object> _bridge : bridgesData.entrySet()) {
			String ngay = _bridge.getKey();
			Map<String, String> giais = (Map<String, String>) _bridge.getValue();
			//
			for(String giai_without_index : giais.values()){
				//
				String days = giai_of_ngay.get(giai_without_index);
				if(days == null){
					days = ngay;
				}else {
					if(days.indexOf(ngay) < 0) {
						days = days + "," + ngay;
					}
				}
				giai_of_ngay.put(giai_without_index, days);
				
				//
				if(days.split(",").length >= 2){
					giai_of_ngay_bigger_than_x.put(giai_without_index, days);
				}
			}
		}
		//for not same position
		System.out.println("===================NOT SAME VT======================");
		System.out.println("====================================================");
		for(Entry<String, String> entry : giai_of_ngay_bigger_than_x.entrySet()){
			System.out.println("Bridge [Khong cung vi tri] xuat hien voi tan suat >=2: "+ entry.getKey());
			System.out.println("Vao cac ngay: "+ entry.getValue());
		}
		//for same position
		System.out.println("======================SAME VT=======================");
		System.out.println("====================================================");
		for(Entry<String, String> entry : giai_of_ngay_bigger_than_3.entrySet()){
			System.out.println("Bridge [Cung vi tri] xuat hien voi tan suat >=2: "+ entry.getKey());
			System.out.println("Vao cac ngay: "+ entry.getValue());
		}
		
	}
	
	/**
	 * 
	 * @param maps
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> lookWeekBridge(Map<String, Object> maps) {
		Map<String, Object> ngay_bridges = new LinkedHashMap<String, Object>();
		for(String db : WEEK_DB) {
			Map<String, String> founds = new LinkedHashMap<String, String>();
			String inputDB = db.substring(db.length()-2);
			System.out.println("Look bridge for =================== "+inputDB);
			Map<String, Object> tmp = maps;
			for(Entry<String, Object> ngay_giai : tmp.entrySet()) {
				String ngay = ngay_giai.getKey();
				System.out.println("Ngay hom truoc =================== "+ngay);
				Map<String, String> giai = (Map<String, String>) ngay_giai.getValue();
				for(Entry<String, String> g : giai.entrySet()) {
					String _giai = g.getValue();
					//if found the number of GOLD
					if(_giai.indexOf(inputDB.charAt(0)) >= 0){
						int index1 = _giai.indexOf(inputDB.charAt(0))+1;
						String brd = g.getKey()+"_index_"+index1;
						System.out.println("Founds !!!!  '"+brd +"<"+_giai+">");
						founds.put(brd, g.getKey());
						int index2 = _giai.lastIndexOf(inputDB.charAt(0))+1;
						if(index2 > index1) {
							brd = g.getKey()+"_index_"+index2;
							System.out.println("Founds !!!!  '"+brd +"<"+_giai+">");
							founds.put(brd, g.getKey());
						}
					}
					if(_giai.indexOf(inputDB.charAt(1)) >= 0) {
						int index1 = _giai.indexOf(inputDB.charAt(1))+1;
						String brd = g.getKey()+"_index_"+index1;
						System.out.println("Founds !!!!  '"+brd +"<"+_giai+">");
						founds.put(brd, g.getKey());
						int index2 = _giai.lastIndexOf(inputDB.charAt(1))+1;
						if(index2 > index1){
							brd = g.getKey()+"_index_"+index2;
							System.out.println("Founds !!!!  '"+brd +"<"+_giai+">");
							founds.put(brd, g.getKey());
						}
					}
				}
				
				//
				ngay_bridges.put(ngay, founds);
				tmp.remove(ngay);
				break;
			}
		}
		return ngay_bridges;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> readDaily(String file) {
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
            String oldDB = "";
            while ((strLine = br.readLine().trim()) != null) {
            	//
            	if(strLine.trim().isEmpty()) continue;
            	Map<String, String> cac_giai_cua_ngay = new LinkedHashMap<String, String>();
            	//Keep the date
            	if(strLine.startsWith("===")){
            		System.out.println("Start of day !!!!!!!!!!!!!!!!!!!!!");
            		//clean "="
            		strLine = strLine.replaceAll("=", "");
            		//clean "Ngay"
            		strLine = strLine.replaceAll("Ngay", "");
            		sDate = strLine.trim();
            		result.put(sDate, cac_giai_cua_ngay);
            		soNgay++;
            		soGiai=1;
            		soGiaiPhu=1;
            		n=1;
            		
            		continue;
            	} else {
            		cac_giai_cua_ngay = (Map<String, String>) result.get(sDate);
            	}
            	
            	//restric so ngay
            	if(soNgay > 30){
            		break;
            	}
            	
            	String ten_giai = getTenGiai(soGiai, soGiaiPhu).trim();
            	//System.out.println("ten_giai========= '"+ten_giai);
            	String giai = strLine.trim();
            	//System.out.println("giai=========xxxx '"+giai);
            	cac_giai_cua_ngay.put(ten_giai, giai);
            	result.put(sDate, cac_giai_cua_ngay);
            	
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
            		System.out.println("so giai is "+n);
            	}
            	
            	//
            	n++;
            	soGiaiPhu++;
            }
        }catch (Exception e){
            System.err.println("readData: " + e.getMessage());
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
