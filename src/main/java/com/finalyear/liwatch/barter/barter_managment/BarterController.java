package com.finalyear.liwatch.barter.barter_managment;

import com.finalyear.liwatch.barter.dto.BarterCreateDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/barter")
public class BarterController {
    @Autowired
    private BarterService service;
    @PostMapping("create-barter")
    public ResponseEntity<String> createBarter(@Valid @RequestBody BarterCreateDto dto){
        return ResponseEntity.ok(service.createBarter(dto));
    }
}
