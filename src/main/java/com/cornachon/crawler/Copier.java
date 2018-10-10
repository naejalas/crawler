package com.cornachon.crawler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * @author jean on 17.09.18.
 */
//@Component
public class Copier implements Runnable{

    @Value("${browser.path}")
    private String browserDir;

    @Value("${working.directory}")
    private String workingDirectory;

    public void start() throws IOException, InterruptedException {
        WatchService watcher = FileSystems.getDefault().newWatchService();

        Path path = new File(browserDir).toPath();

        path.register(watcher,
                ENTRY_CREATE,
                ENTRY_DELETE,
                ENTRY_MODIFY);

        WatchKey key;
        while ((key = watcher.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println(
                        "Event kind:" + event.kind()
                                + ". File affected: " + event.context() + ".");

                Files.move(Paths.get(browserDir +"/"+ event.context().toString()),Paths.get(workingDirectory +"/"+ cleanFileName(event.context().toString())+".csv" ), REPLACE_EXISTING);
            }
            key.reset();
        }

    }

    private String cleanFileName(String fileName){
        return fileName.contains("(")? fileName.substring(0,fileName.indexOf(" (")) : fileName;
    }

    @Override
    public void run() {
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
