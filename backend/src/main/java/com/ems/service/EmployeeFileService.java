package com.ems.service;

import com.ems.common.exception.BusinessException;
import com.ems.common.exception.ResourceNotFoundException;
import com.ems.dto.EmployeeResponse;
import com.ems.dto.FileDownload;
import com.ems.entity.Employee;
import com.ems.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeFileService {

    private static final long MAX_PHOTO_SIZE = 2 * 1024 * 1024;
    private static final long MAX_CV_SIZE = 5 * 1024 * 1024;

    private final EmployeeRepository employeeRepository;
    private final StorageService storageService;

    public EmployeeResponse uploadPhoto(Long employeeId, MultipartFile file) {
        Employee employee = findOrThrow(employeeId);
        byte[] bytes = readAndCheckSize(file, MAX_PHOTO_SIZE, "Photo must not exceed 2 MB");

        ImageType type = ImageType.detect(bytes);
        if (type == null) {
            throw BusinessException.badRequest("Photo must be a JPEG, PNG or WEBP image");
        }

        String newKey = "employees/" + employeeId + "/photo-" + UUID.randomUUID() + "." + type.extension;
        storageService.upload(newKey, bytes, type.contentType);

        String oldKey = employee.getPhotoKey();
        employee.setPhotoKey(newKey);
        employeeRepository.saveAndFlush(employee);
        storageService.deleteQuietly(oldKey);

        return EmployeeResponse.from(employee);
    }

    @Transactional(readOnly = true)
    public FileDownload getPhoto(Long employeeId) {
        Employee employee = findOrThrow(employeeId);
        if (employee.getPhotoKey() == null) {
            throw new ResourceNotFoundException("This employee has no photo");
        }
        byte[] bytes = storageService.download(employee.getPhotoKey());
        ImageType type = ImageType.detect(bytes);
        String contentType = type != null ? type.contentType : "application/octet-stream";
        return new FileDownload(bytes, contentType, "photo");
    }

    public EmployeeResponse uploadCv(Long employeeId, MultipartFile file) {
        Employee employee = findOrThrow(employeeId);
        byte[] bytes = readAndCheckSize(file, MAX_CV_SIZE, "CV must not exceed 5 MB");

        if (!isPdf(bytes)) {
            throw BusinessException.badRequest("CV must be a PDF file");
        }

        String newKey = "employees/" + employeeId + "/cv-" + UUID.randomUUID() + ".pdf";
        storageService.upload(newKey, bytes, "application/pdf");

        String oldKey = employee.getCvKey();
        employee.setCvKey(newKey);
        employee.setCvFileName(sanitizeFileName(file.getOriginalFilename()));
        employeeRepository.saveAndFlush(employee);
        storageService.deleteQuietly(oldKey);

        return EmployeeResponse.from(employee);
    }

    @Transactional(readOnly = true)
    public FileDownload getCv(Long employeeId) {
        Employee employee = findOrThrow(employeeId);
        if (employee.getCvKey() == null) {
            throw new ResourceNotFoundException("This employee has no CV");
        }
        byte[] bytes = storageService.download(employee.getCvKey());
        String name = employee.getCvFileName() != null ? employee.getCvFileName() : "cv.pdf";
        return new FileDownload(bytes, "application/pdf", name);
    }

    private Employee findOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    private byte[] readAndCheckSize(MultipartFile file, long maxSize, String tooLargeMessage) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("File is required");
        }
        if (file.getSize() > maxSize) {
            throw BusinessException.badRequest(tooLargeMessage);
        }
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw BusinessException.badRequest("Could not read the uploaded file");
        }
    }

    private static boolean isPdf(byte[] b) {
        return startsWith(b, new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D});
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        return data.length >= prefix.length
                && Arrays.equals(Arrays.copyOf(data, prefix.length), prefix);
    }

    private static String sanitizeFileName(String original) {
        if (original == null || original.isBlank()) {
            return "cv.pdf";
        }
        String name = original.replaceAll("^.*[/\\\\]", "")
                .replaceAll("[^A-Za-z0-9._ -]", "_")
                .trim();
        if (name.length() > 100) {
            name = name.substring(0, 100);
        }
        if (!name.toLowerCase().endsWith(".pdf")) {
            name = name + ".pdf";
        }
        return name;
    }

    private enum ImageType {
        JPEG("image/jpeg", "jpg"),
        PNG("image/png", "png"),
        WEBP("image/webp", "webp");

        final String contentType;
        final String extension;

        ImageType(String contentType, String extension) {
            this.contentType = contentType;
            this.extension = extension;
        }

        static ImageType detect(byte[] b) {
            if (startsWith(b, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
                return JPEG;
            }
            if (startsWith(b, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})) {
                return PNG;
            }
            if (b.length >= 12
                    && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
                return WEBP;
            }
            return null;
        }
    }
}
