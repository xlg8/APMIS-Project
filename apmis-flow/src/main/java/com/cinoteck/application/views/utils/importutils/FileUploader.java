
package com.cinoteck.application.views.utils.importutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.vaadin.flow.component.upload.Receiver;

public class FileUploader implements Receiver {
    public static String BASE_PATH = System.getProperty("user.dir");
    private File file;
    private String filename;

    public OutputStream receiveUpload(String filename,
                                      String mimeType) {
        // Create upload stream
        FileOutputStream fos = null; // Stream to write to
        try {
            // Open the file for writing.
            this.filename = FileUploader.BASE_PATH +System.getProperty("file.separator")+ filename;

            file = new File(this.filename);
            fos = new FileOutputStream(file);
        } catch (final java.io.FileNotFoundException e) {
        	
        	System.out.println("::::::::::::::: "+e.getMessage());
            return null;
        }
        return fos; // Return the output stream to write to
    }

    public String getFilename() {
        return filename;
    }
};