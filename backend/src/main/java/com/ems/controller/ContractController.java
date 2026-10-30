package com.ems.controller;

import com.ems.dto.FileDownload;
import com.ems.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping("/api/employees/{id}/contract")
    @PreAuthorize("hasAuthority('PERM_READ')")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        FileDownload contract = contractService.generate(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(contract.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .cacheControl(CacheControl.noStore())
                .body(contract.content());
    }
}
