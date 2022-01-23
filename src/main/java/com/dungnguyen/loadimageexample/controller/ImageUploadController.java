package com.dungnguyen.loadimageexample.controller;


import com.dungnguyen.loadimageexample.service.IStorageService;
import com.dungnguyen.loadimageexample.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/image")
public class ImageUploadController {
    @Autowired
    private ImageStorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImageFile(@RequestParam("file")MultipartFile file){
        try{
            String generatedFilename = storageService.storeFile(file);
            return ResponseEntity.ok().body(generatedFilename);
        }catch (Exception e){
            System.out.println("Exception : " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(null);
    }

    @GetMapping("/get-image")
    public ResponseEntity<Resource> readImageFile(@PathParam("filename") String filename){
        try {
            Resource resource = storageService.readFileContent(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } catch (Exception e){
            return ResponseEntity.noContent().build();
        }
    }

    // load all upload files
    @GetMapping("/get-all-filenames")
    public ResponseEntity<List<String>> getAllFilename(){
        try {
            List<String> list = storageService.loadAll().map(path -> {
                String urlPath = MvcUriComponentsBuilder.fromMethodName(ImageUploadController.class,
                        "readImageFile",
                                    path.getFileName().toString()).build().toUri().toString();
                return urlPath;
            }).collect(Collectors.toList());
            return ResponseEntity.ok().body(list);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(null);
        }
    }
}
