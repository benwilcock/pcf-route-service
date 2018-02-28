package io.pivotalservices.tokenservice;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwkFileConroller {


    @Value("${jwk.filename}")
    private String fileName;

    @GetMapping("/jwk")
    @ResponseBody
    public FileSystemResource test() {
        return new FileSystemResource(JwkFileConroller.class.getClassLoader().getResource(fileName).getFile());
    }
}
