package main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
// import java.io.BufferedWriter; // 如果不再需要寫入檔案，可以移除
// import java.io.FileWriter;     // 如果不再需要寫入檔案，可以移除


public class SimpleWebScraper {

    // 移除 static weatherPhrase; 我們將通過方法返回數據

    // 創建一個方法來執行爬蟲並返回結果
    public String getWeatherPhrase(String url) throws IOException {

        // 從這裡開始是原 main 方法中的爬蟲邏輯
        System.out.println("開爬");
        Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36") // 模擬瀏覽器訪問
                            .timeout(10000) // 設置超時時間 10 秒
                            .get();

        // 如果不再需要寫入檔案，移除以下程式碼
        // String html = doc.html();
        // try (BufferedWriter writer = new BufferedWriter(new FileWriter("out.txt"))) {
        //     writer.write(html);
        // }
        // System.out.println("HTML已寫入out檔案。");

        Elements phraseElements = doc.select("span.phrase");
        if (!phraseElements.isEmpty()) {
            Element phraseElement = phraseElements.first();
            // 返回提取到的文本
            System.out.println("爬到了: " + phraseElement.text());
            return phraseElement.text();

        } else {
            // 如果沒有找到元素，返回一個默認值或拋出異常
            System.out.println("沒東西");
            return "天氣資訊未找到"; // 返回一個表示未找到的字符串
            // 或者拋出異常： throw new IOException("Weather phrase element not found.");
        }
        // 到這裡結束原 main 方法的爬蟲邏輯
    }

    // 您可以選擇保留 main 方法用於單獨測試這個 Scraper
    public static void main(String[] args) {
        String testUrl = "https://www.accuweather.com/zh/tw/tainan-city/314999/weather-forecast/314999"; // 測試 URL
        SimpleWebScraper scraper = new SimpleWebScraper();
        String weather;
        try {
            weather = scraper.getWeatherPhrase(testUrl);
            System.out.println("天氣資訊: " + weather);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
