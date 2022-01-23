package com.dungnguyen.loadimageexample.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class VideoStorageService implements IStorageService{
    private final Path storageFolder = Paths.get("uploads/videos");

    public VideoStorageService(){
        try {
            Files.createDirectories(storageFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()){
                throw new RuntimeException("Failed to empty file");
            }
            if (!isVideoFile(file)){
                throw new RuntimeException("You can only upload image file");
            }

            float fileSizeInMegabytes = file.getSize()/1_000_000.0f;
            if(fileSizeInMegabytes > 20.0f){
                throw new RuntimeException("File must be  <= 20Mb");
            }

            // change filename when upload to server
            String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
            String generatedFilename = UUID.randomUUID().toString().replace("-","");
            generatedFilename = generatedFilename + "." + fileExtension;

            Files.copy(file.getInputStream(), this.storageFolder.resolve(generatedFilename));
            return generatedFilename;
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public boolean isVideoFile(MultipartFile file){
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        return Arrays.asList(new String[] {"mp4", "mov", "wmv", "avi"})
                .contains(fileExtension.trim().toLowerCase());
    }


    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.storageFolder, 1)
                    .filter(path -> !path.equals(this.storageFolder))
                    .map(this.storageFolder::relativize);
        } catch (Exception e){
            throw new RuntimeException("Failed to load stored files", e);
        }
    }

    @Override
    public Resource readFileContent(String filename) {
        try {
            Path file = storageFolder.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteAllFiles() {

    }
}
