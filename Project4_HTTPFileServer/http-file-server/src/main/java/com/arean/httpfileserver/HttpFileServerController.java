package com.arean.httpfileserver;


        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;
        import java.io.File;
        import java.io.IOException;
        import java.nio.file.Files;
        import java.nio.file.Path;
        import java.nio.file.Paths;
        import java.nio.file.StandardCopyOption;
        import java.util.Objects;

@RestController
public class HttpFileServerController {
    private static final String FILE_UPLOAD_DIRECTORY = "./uploads/";

    @PostMapping("/post/")
    public ResponseEntity<String> saveFile(@RequestParam MultipartFile file){

        try{
            Path fileStorageLocation = Paths.get(FILE_UPLOAD_DIRECTORY).toAbsolutePath().normalize();
            Files.createDirectories(fileStorageLocation);
            Path targetLocation = fileStorageLocation.resolve(Objects.requireNonNull("uploaded"+System.currentTimeMillis()+file.getOriginalFilename()));
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File Save Failed");
        }
        return ResponseEntity.ok().body("Successfully Saved File");
    }

    @GetMapping("/get/{fileName}")
    public ResponseEntity<File> retrieveFile(@PathVariable String fileName){
        File file = new File(FILE_UPLOAD_DIRECTORY + fileName);
        return ResponseEntity.ok().body(file);
    }
}
