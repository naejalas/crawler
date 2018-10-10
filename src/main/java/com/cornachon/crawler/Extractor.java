package com.cornachon.crawler;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author jean on 06.09.18.
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "extractor")
public class Extractor {

    public static class Conf{




        public String periode;
        public String name;
        public boolean journal;
        public boolean csv;
        public String fileName;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public boolean isJournal() {
            return journal;
        }

        public void setJournal(boolean journal) {
            this.journal = journal;
        }

        public boolean isCsv() {
            return csv;
        }

        public void setCsv(boolean csv) {
            this.csv = csv;
        }

        public String getPeriode() {
            return periode;
        }

        public void setPeriode(String periode) {
            this.periode = periode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private String clicSleepTime;

    private String springDir = "${config.path}";

    private final String SPEAKER = "tdNameAffichage / Speaker|";

    private final String EXPORTS = "tdNameEXPORTS|";

    private List<Conf> section = new ArrayList<>();


    private Map<String, String> exports;

    private String url;

    private String fileDir;

    private String browserDir;

    private String workingDir;

    private String macDriver = "chromedriver";

    private String winDriver = "chromedriver.exe";

    private static String OS = System.getProperty("os.name").toLowerCase();

    public Extractor() throws IOException {

    }

    private static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    private static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    private static boolean isUnix() {
        return (OS.indexOf("nux") >= 0);
    }

    public void start() {

        this.log.info("spring dir " + System.getProperty("config.path") + "chromedriver");

        if (isWindows()) {
            System.setProperty("webdriver.chrome.driver", System.getProperty("config.path") + "chromedriver.exe");
        } else {
            System.setProperty("webdriver.chrome.driver", System.getProperty("config.path") + "chromedriver");
        }


        section.forEach(conf -> new Thread(new Periodic(Integer.parseInt(conf.periode), conf.name, conf.fileName, conf.journal, conf.csv)).start());


        //new Thread(new Live()).start();

        //new Thread(new Periodic()).start();


        /*for(Map.Entry<String,String> entry: exports.entrySet()){
            new Thread(new Periodic(Integer.parseInt(entry.getValue()),entry.getKey())).start();
        }*/


    }

    private class Live implements Runnable{



        @Override
        public void run() {

            WebDriver driver = new ChromeDriver();

            driver.get("http://192.168.0.81/_103513/center/?lang=en-fr&pw=1D9C9A366FDF6E8992C16876D0FAC34A");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {

            }
            WebElement body = driver.findElement(By.id("catlive"));
            body.click();


            driver.switchTo().frame(driver.findElement(By.id("_live")));
            WebElement tbLists = driver.findElement(By.id("tbLists"));
            List<WebElement> live_s   = tbLists.findElements(By.xpath(" //td[contains(text(), \"LIVE S\")]"));
            live_s.get(0).click();

        }
    }


    private class Periodic implements Runnable {

        int periodic = 1;
        String sectionName;
        String fileName;
        boolean journal = false;
        boolean csv = false;

        public Periodic(int periodic, String sectionName, String fileName, boolean journal, boolean csv) {
            this.periodic = periodic;
            this.sectionName = sectionName;
            this.fileName = fileName;
            this.journal = journal;
            this.csv = csv;
        }



        @Override
        public void run() {
            Extractor.log.info("period " + periodic);
            Extractor.log.info("sectionName " + sectionName);

            //Instantiating driver object
            WebDriver driver = new ChromeDriver();

            driver.get(url);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {

            }
            //driver.manage().window().setPosition(new Point(-2000, 0));

            WebElement body = driver.findElement(By.id("catout"));
            body.click();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            driver.switchTo().frame(driver.findElement(By.id("_out")));
            while (true) {

                Extractor.log.info("time stamp " + new Date().toGMTString());
                driver.switchTo().frame(driver.findElement(By.name("iFrameSide")));
                try {
                    Thread.sleep(Integer.parseInt(clicSleepTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                WebElement element = driver.findElement(By.id(sectionName));//EXPORTS + sectionName));
                element.click();
                try {
                    Thread.sleep(Integer.parseInt(clicSleepTime));
                } catch (InterruptedException e) {

                }
                driver.switchTo().parentFrame();
                driver.switchTo().frame(driver.findElement(By.name("myiFrame")));
                try {
                    Thread.sleep(Integer.parseInt(clicSleepTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                driver.findElements(By.className("tile")).get(6).findElement(By.tagName("div")).click();

                try {
                    Thread.sleep(Integer.parseInt(clicSleepTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //WebElement csv = driver.findElement(By.xpath("//div[contains(string(),'HTML')]"));

                // driver.findElements(By.)

                //csv.click();
                try {
                    Thread.sleep(Integer.parseInt(clicSleepTime));
                } catch (InterruptedException e) {

                }
                WebElement pre = driver.findElement(By.tagName("pre"));
                String data = pre.getText();

                if(this.journal){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                FileWriter fileWriter = new FileWriter(fileDir + fileName +"_flat.txt", false);
                                fileWriter.write(data.substring( data.indexOf("\n", 0)+1,data.length()).replace(","," ").replaceAll("\"", "").replaceAll("\\n", " "));
                                fileWriter.flush();
                                fileWriter.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
                }

                if(this.csv){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                FileWriter fileWriter = new FileWriter(fileDir + fileName +".csv", false);
                                fileWriter.write(data);
                                fileWriter.flush();
                                fileWriter.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }).start();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            FileWriter fileWriter = new FileWriter(fileDir + fileName +".txt", false);
                            fileWriter.write(data.replace(",",";"));
                            fileWriter.flush();
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();


                driver.switchTo().parentFrame();

                try {
                    Thread.sleep(periodic);
                } catch (InterruptedException e) {

                }


                /*List<WebElement> enregistrer = driver.findElements(By.linkText("Enregistrer"));

                enregistrer.get(3).click();

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Extractor.log.info("downloadfile " + periodic+"  "+new Date().toGMTString());
                try {
                    Files.move(Paths.get(browserDir + "/" + convertName(sectionName) + ".csv"), Paths.get(workingDir + "/" + convertName(sectionName) + ".csv"), REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(periodic);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/


            }

            /*speakers.forEach(item->{

                driver.switchTo().frame(driver.findElement(By.name("iFrameSide")));
                WebElement element = driver.findElement(By.id(SPEAKER + item));
                element.click();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {

                }
                driver.switchTo().parentFrame();
                driver.switchTo().frame(driver.findElement(By.name("myiFrame")));
                List<WebElement> enregistrer = driver.findElements(By.linkText("Enregistrer"));
                Extractor.log.info("downloadfile "+ new Date().toGMTString());
                enregistrer.get(3).click();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                driver.switchTo().parentFrame();
                try {
                    Files.move(Paths.get(browserDir +"/"+ convertName(item)+".csv"),Paths.get(workingDir +"/"+ convertName(item)+".csv" ), REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });*/
        }

        private String convertName(String name) {
            return name.replace(" ", "_").replace("/", "");
        }
    }

    public String getSpringDir() {
        return springDir;
    }

    public void setSpringDir(String springDir) {
        this.springDir = springDir;
    }

    public String getSPEAKER() {
        return SPEAKER;
    }

    public String getEXPORTS() {
        return EXPORTS;
    }

    public Map<String, String> getExports() {
        return exports;
    }

    public void setExports(Map<String, String> exports) {
        this.exports = exports;
    }

    public String getBrowserDir() {
        return browserDir;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public String getMacDriver() {
        return macDriver;
    }

    public void setMacDriver(String macDriver) {
        this.macDriver = macDriver;
    }

    public String getWinDriver() {
        return winDriver;
    }

    public void setWinDriver(String winDriver) {
        this.winDriver = winDriver;
    }

    public static String getOS() {
        return OS;
    }

    public static void setOS(String OS) {
        Extractor.OS = OS;
    }

    public static Logger getLog() {
        return log;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBrowserDir(String browserDir) {
        this.browserDir = browserDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public List<Conf> getSection() {
        return section;
    }

    public void setSection(List<Conf> section) {
        this.section = section;
    }

    public String getClicSleepTime() {
        return clicSleepTime;
    }

    public void setClicSleepTime(String clicSleepTime) {
        this.clicSleepTime = clicSleepTime;
    }
}
