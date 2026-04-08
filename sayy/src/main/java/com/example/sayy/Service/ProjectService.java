package com.example.sayy.Service;

import com.example.sayy.Entity.ContractEntity;
import com.example.sayy.Entity.ProjectEntity;
import com.example.sayy.Mapper.ProjectMapper;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final Path contractStorageRoot;

    public ProjectService(ProjectMapper projectMapper,
                          @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.projectMapper = projectMapper;
        this.contractStorageRoot = Paths.get(uploadDir, "contracts").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.contractStorageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("계약서 저장 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    public List<ProjectEntity> getProjects(String status, String keyword) {
        return projectMapper.selectAll(
                blank(status) ? null : status,
                blank(keyword) ? null : keyword.trim()
        );
    }

    public ProjectEntity getProjectOrThrow(Long id) {
        ProjectEntity p = projectMapper.selectById(id);
        if (p == null) throw new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + id);
        p.setContracts(projectMapper.selectContractsByProjectId(id));
        return p;
    }

    @Transactional
    public Long createProject(ProjectEntity p) {
        if (blank(p.getName())) throw new IllegalArgumentException("프로젝트명은 필수입니다.");
        p.setName(p.getName().trim());
        if (blank(p.getStatus())) p.setStatus("ESTIMATE");
        // 자동 계산
        calcAmounts(p);
        projectMapper.insert(p);
        return p.getId();
    }

    @Transactional
    public void updateProject(Long id, ProjectEntity p) {
        if (blank(p.getName())) throw new IllegalArgumentException("프로젝트명은 필수입니다.");
        p.setId(id);
        p.setName(p.getName().trim());
        if (blank(p.getStatus())) p.setStatus("ESTIMATE");
        calcAmounts(p);
        projectMapper.update(p);
    }

    private void calcAmounts(ProjectEntity p) {
        // 당사계약금액 = 계약금액 × 당사지분(%) / 100
        if (p.getContractAmount() != null && p.getOurShareRate() != null) {
            p.setOurAmount(p.getContractAmount() * p.getOurShareRate() / 100);
        }
        // 계약금액(부가세 제외) = 계약금액 ÷ 1.1 (소수점 버림)
        if (p.getContractAmount() != null) {
            p.setContractAmountVatEx(Math.round(p.getContractAmount() / 1.1));
        }
        // 당사 계약금액(부가세 제외) = 당사계약금액 ÷ 1.1
        if (p.getOurAmount() != null) {
            p.setOurAmountVatEx(Math.round(p.getOurAmount() / 1.1));
        }
    }

    @Transactional
    public void deleteProject(Long id) {
        // 계약서 파일 정리
        List<ContractEntity> contracts = projectMapper.selectContractsByProjectId(id);
        for (ContractEntity c : contracts) {
            deleteContractFile(c);
        }
        projectMapper.deleteById(id);
    }

    // ===== 계약서 =====

    @Transactional
    public void addContract(Long projectId, String title, String contractType,
                            LocalDate contractDate, Long amount, String status,
                            MultipartFile file, String memo) throws IOException {
        if (blank(title)) throw new IllegalArgumentException("계약서명은 필수입니다.");

        ContractEntity c = new ContractEntity();
        c.setProjectId(projectId);
        c.setTitle(title.trim());
        c.setContractType(blank(contractType) ? "MAIN" : contractType.trim());
        c.setContractDate(contractDate);
        c.setAmount(amount);
        c.setStatus(blank(status) ? "DRAFT" : status.trim());
        c.setMemo(trim(memo));

        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
            String storedName = "project-" + projectId + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
            Path dir = contractStorageRoot.resolve("project-" + projectId);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), contractStorageRoot.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
            c.setOriginalName(originalName);
            c.setStoredName(storedName);
            c.setFileSize(file.getSize());
            c.setContentType(file.getContentType());
        }
        projectMapper.insertContract(c);
    }

    @Transactional
    public void updateContractStatus(Long projectId, Long contractId, String status, String memo) {
        ContractEntity c = projectMapper.selectContractById(contractId);
        if (c == null || !c.getProjectId().equals(projectId)) throw new IllegalArgumentException("계약서를 찾을 수 없습니다.");
        c.setStatus(status);
        c.setMemo(trim(memo));
        projectMapper.updateContract(c);
    }

    public Resource downloadContract(Long projectId, Long contractId) throws MalformedURLException {
        ContractEntity c = projectMapper.selectContractById(contractId);
        if (c == null || !c.getProjectId().equals(projectId) || c.getStoredName() == null)
            throw new IllegalArgumentException("파일을 찾을 수 없습니다.");
        Path filePath = contractStorageRoot.resolve(c.getStoredName()).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) throw new IllegalStateException("파일을 읽을 수 없습니다.");
        return resource;
    }

    @Transactional
    public void deleteContract(Long projectId, Long contractId) {
        ContractEntity c = projectMapper.selectContractById(contractId);
        if (c != null && c.getProjectId().equals(projectId)) {
            deleteContractFile(c);
            projectMapper.deleteContract(contractId, projectId);
        }
    }

    public ContractEntity getContractOrThrow(Long id) {
        ContractEntity c = projectMapper.selectContractById(id);
        if (c == null) throw new IllegalArgumentException("계약서를 찾을 수 없습니다: " + id);
        return c;
    }

    private void deleteContractFile(ContractEntity c) {
        if (c.getStoredName() == null) return;
        try { Files.deleteIfExists(contractStorageRoot.resolve(c.getStoredName()).normalize()); }
        catch (IOException ignored) {}
    }

    private static boolean blank(String v) { return v == null || v.isBlank(); }
    private static String trim(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
