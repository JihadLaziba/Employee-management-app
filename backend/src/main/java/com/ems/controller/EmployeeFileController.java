package com.ems.controller;

import com.ems.dto.EmployeeResponse;
import com.ems.dto.FileDownload;
import com.ems.service.EmployeeFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * Upload / lecture de la photo et du CV d'un employé.
 * - Lire un fichier : permission READ
 * - Envoyer ou remplacer un fichier : permission CREATE ou UPDATE
 *   (CREATE pour pouvoir joindre les fichiers juste après avoir créé l'employé)
 */
@RestController
@RequestMapping("/api/employees/{id}")
@RequiredArgsConstructor
public class EmployeeFileController {

    private final EmployeeFileService fileService;

    // ------------------------------------------------------------------ photo

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('PERM_CREATE', 'PERM_UPDATE')")
    public EmployeeResponse uploadPhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return fileService.uploadPhoto(id, file);
    }

    @GetMapping("/photo")
    @PreAuthorize("hasAuthority('PERM_READ')")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        FileDownload photo = fileService.getPhoto(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .cacheControl(CacheControl.noStore())
                .body(photo.content());
    }

    // ------------------------------------------------------------------ CV

    @PostMapping(value = "/cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('PERM_CREATE', 'PERM_UPDATE')")
    public EmployeeResponse uploadCv(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return fileService.uploadCv(id, file);
    }

    /** GET /api/employees/{id}/cv            -> affichage dans le navigateur
     *  GET /api/employees/{id}/cv?download=true -> téléchargement */
    @GetMapping("/cv")
    @PreAuthorize("hasAuthority('PERM_READ')")
    public ResponseEntity<byte[]> getCv(@PathVariable Long id,
                                        @RequestParam(defaultValue = "false") boolean download) {
        FileDownload cv = fileService.getCv(id);
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(cv.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .cacheControl(CacheControl.noStore())
                .body(cv.content());
    }
}
