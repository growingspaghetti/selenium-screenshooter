package growingspaghetti.selenium_screenshooter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class App {
    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    static ExecutorService executorService
        = new ThreadPoolExecutor(40,
                                 40,
                                 0L,
                                 TimeUnit.MILLISECONDS,
                                 new LinkedBlockingQueue<Runnable>());

    private static String buildQuery(String word) {
        return String.join("",
                           "https://www.google.com/search?q=",
                           word,
                           "&source=lnms&tbm=isch&sa=X",
                           "&ei=0eZEVbj3IJG5uATalICQAQ",
                           "&ved=0CAcQ_AUoAQ&biw=939&bih=591");
    }

    private static void fetchScreenShot(String url, String filePath) throws MalformedURLException {
        WebDriver driver = new RemoteWebDriver(
            new URL("http://127.0.0.1:4444/wd/hub"),
            DesiredCapabilities.chrome());
        try {
            LOGGER.info(String.join(" -> ", url, filePath));
            driver.get(url);
            File source = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(source, new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Optional.ofNullable(driver).ifPresent(WebDriver::quit); // not close
        }
    }

    public static void main(String[] args) throws IOException {
        List<String> words = FileUtils.readLines(new File("sat_words.txt"), "UTF-8");
        for (String word : words) {
            CompletableFuture.runAsync(() -> {
                String url  = buildQuery(word);
                String path = "imgs/" + word.toLowerCase() + ".png";
                try {
                    fetchScreenShot(url, path);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }, executorService);
        }
    }
}
