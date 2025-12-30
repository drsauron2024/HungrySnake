import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameRecordManager {
    private static final String RECORD_FILE = "snake_game_records.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 保存游戏记录
     */
    public static void saveRecord(int score, int length, long gameTime) {
        try (FileWriter writer = new FileWriter(RECORD_FILE, true);
             BufferedWriter bw = new BufferedWriter(writer)) {
            
            String timestamp = DATE_FORMAT.format(new Date());
            String record = String.format("%s | 分数: %d | 长度: %d | 时间: %02d:%02d",
                    timestamp, score, length, 
                    gameTime / 60, gameTime % 60);
            
            bw.write(record);
            bw.newLine();
            bw.flush();
            
            System.out.println("游戏记录已保存: " + record);
        } catch (IOException e) {
            System.err.println("保存游戏记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 读取所有游戏记录
     */
    public static List<String> loadRecords() {
        List<String> records = new ArrayList<>();
        
        File file = new File(RECORD_FILE);
        if (!file.exists()) {
            return records;
        }
        
        try (FileReader reader = new FileReader(RECORD_FILE);
             BufferedReader br = new BufferedReader(reader)) {
            
            String line;
            while ((line = br.readLine()) != null) {
                records.add(line);
            }
        } catch (IOException e) {
            System.err.println("读取游戏记录失败: " + e.getMessage());
        }
        
        return records;
    }
    
    /**
     * 获取最高分记录
     */
    public static String getHighScore() {
        List<String> records = loadRecords();
        if (records.isEmpty()) {
            return "暂无记录";
        }
        
        int maxScore = 0;
        String bestRecord = "";
        
        for (String record : records) {
            try {
                // 从记录中提取分数
                int scoreStart = record.indexOf("分数: ") + 4;
                int scoreEnd = record.indexOf(" |", scoreStart);
                String scoreStr = record.substring(scoreStart, scoreEnd).trim();
                int score = Integer.parseInt(scoreStr);
                
                if (score > maxScore) {
                    maxScore = score;
                    bestRecord = record;
                }
            } catch (Exception e) {
                // 解析失败，跳过这条记录
            }
        }
        
        return bestRecord.isEmpty() ? "暂无记录" : bestRecord;
    }
    
    /**
     * 获取最近5条记录
     */
    public static List<String> getRecentRecords(int count) {
        List<String> allRecords = loadRecords();
        List<String> recent = new ArrayList<>();
        
        int start = Math.max(0, allRecords.size() - count);
        for (int i = start; i < allRecords.size(); i++) {
            recent.add(allRecords.get(i));
        }
        
        return recent;
    }
    
    /**
     * 获取记录文件大小信息
     */
    public static String getFileInfo() {
        File file = new File(RECORD_FILE);
        if (!file.exists()) {
            return "暂无记录文件";
        }
        
        long sizeKB = file.length() / 1024;
        long recordCount = loadRecords().size();
        
        return String.format("记录文件: %s | 大小: %d KB | 记录数: %d",
                file.getName(), sizeKB, recordCount);
    }
}
