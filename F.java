import java.io.*;
import java.util.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PdfProcessor {

    public static String readPdf(String path) {
        try (PDDocument document = PDDocument.load(new File(path))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> tokenizeText(String text) {
        String[] tokens = text.split("\\s+"); // Split by whitespace
        List<String> keywords = new ArrayList<>();

        for (String token : tokens) {
            if (!token.matches("[(),;:\\[\\],\\s]+")) { // Exclude specified punctuations
                keywords.add(token);
            }
        }

        return keywords;
    }

    public static Map<String, Integer> createKeywordRankMap(List<String> keywords) {
        Map<String, Integer> keywordMap = new HashMap<>();
        
        for (int i = 0; i < keywords.size(); i += 2) {
            String keyword = keywords.get(i);
            int rank = Integer.parseInt(keywords.get(i + 1));
            keywordMap.put(keyword, rank);
        }

        return new LinkedHashMap<>(sortByValue(keywordMap, false));
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean ascending) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        if (!ascending) {
            Collections.reverse(list);
        }

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static void writeToJson(String filename, Map<String, Integer> data) {
        try (Writer writer = new FileWriter(filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data, writer);
            System.out.println("Written to file " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToPickle(String filename, Map<String, Integer> data) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(data);
            System.out.println("Written to file " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String pdfPath = "/p";
        String jsonOutputPath = "/pajson";
        String pickleOutputPath = "./dat";

        String pdfText = readPdf(pdfPath);

        if (pdfText != null) {
            List<String> tokenizedText = tokenizeText(pdfText);
            Map<String, Integer> keywordMap = createKeywordRankMap(tokenizedText);

            
            writeToJson(jsonOutputPath, keywordMap);
           
            writeToPickle(pickleOutputPath, keywordMap);
        }
    }
}
