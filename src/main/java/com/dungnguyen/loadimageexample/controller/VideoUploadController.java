package com.dungnguyen.loadimageexample.controller;



import com.dungnguyen.loadimageexample.service.VideoStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.websocket.server.PathParam;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/video")
public class VideoUploadController {

    @Autowired
    protected VideoStorageService service;

    @GetMapping("/get-video")
    public ResponseEntity<Resource> readVideoFile(@PathParam("filename") String filename){
        Resource file = service.readFileContent(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(service.readFileContent(filename));
    }

    @GetMapping("/get-all-video")
    public ResponseEntity<List<String>> getAllFilename(){
        try {
            List<String> list = service.loadAll().map(path -> {
                String urlPath = MvcUriComponentsBuilder.fromMethodName(VideoUploadController.class,
                        "readVideoFile",
                        path.getFileName().toString()).build().toUri().toString();
                return urlPath;
            }).collect(Collectors.toList());
            return ResponseEntity.ok().body(list);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(null);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideoFile(@RequestParam("file") MultipartFile file){
        return ResponseEntity.ok().body(service.storeFile(file));
    }

    @GetMapping("/stream")
    public StreamingResponseBody stream(@PathParam("filename") String filename){

        try {
            Resource resource = service.readFileContent(filename);
            File file = new File(resource.getURI());
            final InputStream videoFileStream = new FileInputStream((File) file);

            return (os) -> {
                readAndWrite(videoFileStream, os);
            };
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readAndWrite(final InputStream is, OutputStream os)
            throws IOException {
        byte[] data = new byte[2048];
        int read = 0;
        while ((read = is.read(data)) > 0) {
            os.write(data, 0, read);
        }
        os.flush();
    }
}
