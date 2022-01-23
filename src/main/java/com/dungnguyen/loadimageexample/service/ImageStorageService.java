package com.dungnguyen.loadimageexample.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ImageStorageService implements IStorageService{
    private final Path storageFolder = Paths.get("uploads/images");

    public ImageStorageService() {
        try{
            Files.createDirectories(storageFolder);
        } catch (IOException e){
            throw new RuntimeException("Cant initialize storage", e);
        }
    }

    private boolean isImageFile(MultipartFile file){
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        return Arrays.asList(new String[] {"png", "jpg", "jpeg", "bmp"})
                .contains(fileExtension.trim().toLowerCase());
    }

    @Override
    public String storeFile(MultipartFile file) {
        try{
            if (file.isEmpty()){
                throw new RuntimeException("Failed to empty file");
            }
            if (!isImageFile(file)){
                throw new RuntimeException("You can only upload image file");
            }

            float fileSizeInMegabytes = file.getSize()/1_000_000.0f;
            if(fileSizeInMegabytes > 5.0f){
                throw new RuntimeException("File must be  <= 5Mb");
            }

            // change filename when upload to server
            String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
            String generatedFilename = UUID.randomUUID().toString().replace("-","");
            generatedFilename = generatedFilename + "." + fileExtension;

            Path destinationFilePath = this.storageFolder.resolve(
                    Paths.get(generatedFilename)
            ).normalize().toAbsolutePath();
            if (!destinationFilePath.getParent().equals(this.storageFolder.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory");
            }
            try (InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return generatedFilename;
        } catch (IOException e){
            throw new RuntimeException("Fail to store file", e);
        }
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
