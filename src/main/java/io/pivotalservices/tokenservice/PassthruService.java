package io.pivotalservices.tokenservice;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

@Service
public class PassthruService {

    private final RestOperations restOperations;

    @Autowired
    public PassthruService(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    public ResponseEntity<byte[]> exchange(RequestEntity<?> outgoing) {
        return this.restOperations.exchange(outgoing, byte[].class);
    }
}
