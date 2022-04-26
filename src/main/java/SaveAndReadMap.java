import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class SaveAndReadMap {
    public static void saveMapToTxt(Map<String,String> map,String filePath){
        OutputStreamWriter outFile=null;
        FileOutputStream fileName;
        try{
            fileName=new FileOutputStream(filePath);
            outFile=new OutputStreamWriter(fileName);
            String str="";
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                assert outFile != null;
                outFile.flush();
                outFile.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Map<String,String> map=new HashMap<String, String>();
        map.put("BFILE","VARCHAR(255)");
        map.put("BINARY_FLOAT","FLOAT");
        map.put("BINARY_DOUBLE","DOUBLE");
//        map.put("","");
        saveMapToTxt(map,"src/datatype.txt");
    }
}
