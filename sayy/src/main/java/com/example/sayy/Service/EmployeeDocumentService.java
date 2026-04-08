package com.example.sayy.Service;

import com.example.sayy.Entity.EmployeeDocumentEntity;
import com.example.sayy.Mapper.EmployeeDocumentMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeDocumentService {

    private final EmployeeDocumentMapper documentMapper;
    private final Path docStorageRoot;

    public EmployeeDocumentService(EmployeeDocumentMapper documentMapper,
                                   @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.documentMapper = documentMapper;
        this.docStorageRoot = Paths.get(uploadDir, "employee-docs").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.docStorageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("문서 저장 디렉토리를 생성할 수 없습니다: " + this.docStorageRoot, e);
        }
    }

    public List<EmployeeDocumentEntity> getDocuments(String empNo) {
        return documentMapper.selectByEmpNo(empNo);
    }

    @Transactional
    public void upload(String empNo, MultipartFile file, String category, String uploadedBy) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("파일을 선택해 주세요.");
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) originalName = "unknown";

        // 사번별 폴더로 분리 저장
        Path empDir = docStorageRoot.resolve(empNo);
        Files.createDirectories(empDir);

        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) ext = originalName.substring(dot);
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path target = empDir.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        EmployeeDocumentEntity doc = new EmployeeDocumentEntity();
        doc.setEmpNo(empNo);
        doc.setOriginalName(originalName);
        doc.setStoredName(empNo + "/" + storedName);
        doc.setFileSize(file.getSize());
        doc.setContentType(file.getContentType());
        doc.setCategory(category != null && !category.isBlank() ? category.trim() : "기타");
        doc.setUploadedBy(uploadedBy);
        documentMapper.insert(doc);
    }

    public Resource download(String empNo, Long id) throws MalformedURLException {
        EmployeeDocumentEntity doc = documentMapper.selectById(id);
        if (doc == null || !doc.getEmpNo().equals(empNo)) {
            throw new IllegalArgumentException("파일을 찾을 수 없습니다.");
        }
        Path filePath = docStorageRoot.resolve(doc.getStoredName()).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("파일을 읽을 수 없습니다.");
        }
        return resource;
    }

    @Transactional
    public void delete(String empNo, Long id) {
        EmployeeDocumentEntity doc = documentMapper.selectById(id);
        if (doc == null || !doc.getEmpNo().equals(empNo)) return;
        // 실제 파일 삭제
        try {
            Path filePath = docStorageRoot.resolve(doc.getStoredName()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {}
        documentMapper.deleteById(id, empNo);
    }

    public EmployeeDocumentEntity findById(Long id) {
        return documentMapper.selectById(id);
    }
}
