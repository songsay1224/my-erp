package com.example.sayy.Service;

import com.example.sayy.Entity.CompanyInfoEntity;
import com.example.sayy.Mapper.CompanyInfoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class CompanyService {
    private final CompanyInfoMapper companyInfoMapper;
    private final Path uploadRoot;

    public CompanyService(CompanyInfoMapper companyInfoMapper,
                          @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.companyInfoMapper = companyInfoMapper;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public CompanyInfoEntity getCompanyInfoOrEmpty() {
        CompanyInfoEntity e = companyInfoMapper.selectOne();
        if (e == null) {
            e = new CompanyInfoEntity();
            e.setId(1);
        }
        return e;
    }

    @Transactional
    public void saveCompanyInfo(String companyName,
                                String ceoName,
                                String phone,
                                LocalDate foundedDate,
                                String zipCode,
                                String address,
                                String addressDetail,
                                String businessRegNo,
                                String corporateRegNo) {
        if (!StringUtils.hasText(companyName)) {
            throw new IllegalArgumentException("회사명은 필수입니다.");
        }

        CompanyInfoEntity current = companyInfoMapper.selectOne();
        String sealPath = (current == null) ? null : current.getSealPath();
        String logoPath = (current == null) ? null : current.getLogoPath();

        CompanyInfoEntity e = new CompanyInfoEntity();
        e.setId(1);
        e.setCompanyName(companyName.trim());
        e.setCeoName(trimToNull(ceoName));
        e.setPhone(normalizePhone(phone));
        e.setFoundedDate(foundedDate);
        e.setZipCode(normalizeZipCode(zipCode));
        e.setAddress(trimToNull(address));
        e.setAddressDetail(trimToNull(addressDetail));
        e.setBusinessRegNo(normalizeDigitsOrNull(businessRegNo, 10, "사업자등록번호"));
        e.setCorporateRegNo(normalizeDigitsOrNull(corporateRegNo, 13, "법인등록번호"));
        e.setLogoPath(logoPath);
        e.setSealPath(sealPath);

        companyInfoMapper.upsert(e);
    }

    @Transactional
    public void saveCompanyLogo(MultipartFile logoFile, boolean removeLogo) {
        CompanyInfoEntity current = companyInfoMapper.selectOne();
        if (current == null || !StringUtils.hasText(current.getCompanyName())) {
            throw new IllegalArgumentException("회사 정보를 먼저 저장해 주세요.");
        }

        String logoPath = current.getLogoPath();

        if (removeLogo) {
            if (StringUtils.hasText(logoPath)) {
                deleteIfExists(logoPath);
            }
            logoPath = null;
            companyInfoMapper.updateLogoPath(null);
            return;
        }

        if (logoFile == null || logoFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 로고 이미지를 선택해 주세요.");
        }

        String newPath = storeImage(logoFile, "company/logo");
        if (StringUtils.hasText(logoPath)) {
            deleteIfExists(logoPath);
        }
        companyInfoMapper.updateLogoPath(newPath);
    }

    @Transactional
    public void saveCompanySeal(MultipartFile sealFile, boolean removeSeal) {
        CompanyInfoEntity current = companyInfoMapper.selectOne();
        if (current == null || !StringUtils.hasText(current.getCompanyName())) {
            throw new IllegalArgumentException("회사 정보를 먼저 저장해 주세요.");
        }

        String sealPath = current.getSealPath();

        if (removeSeal) {
            if (StringUtils.hasText(sealPath)) {
                deleteIfExists(sealPath);
            }
            sealPath = null;
            companyInfoMapper.updateSealPath(null);
            return;
        }

        if (sealFile == null || sealFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 직인 이미지를 선택해 주세요.");
        }

        String newPath = storeImage(sealFile, "company/seal");
        if (StringUtils.hasText(sealPath)) {
            deleteIfExists(sealPath);
        }
        companyInfoMapper.updateSealPath(newPath);

    }

    private String storeImage(MultipartFile file, String subDir) {
        String contentType = (file.getContentType() == null) ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        String original = (file.getOriginalFilename() == null) ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
        if (!(ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("webp") || ext.equals("gif"))) {
            // content-type이 image라도 확장자가 이상한 경우 방어(선택)
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (png/jpg/jpeg/webp/gif)");
        }

        try {
            Path targetDir = uploadRoot.resolve(subDir).normalize();
            Files.createDirectories(targetDir);

            String filename = UUID.randomUUID() + "." + ext;
            Path target = targetDir.resolve(filename).normalize();
            if (!target.startsWith(targetDir)) {
                throw new IllegalArgumentException("잘못된 파일 경로입니다.");
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String publicSubDir = subDir.replace('\\', '/');
            return "/uploads/" + publicSubDir + "/" + filename;
        } catch (IOException e) {
            throw new IllegalStateException("로고 파일 저장에 실패했습니다.");
        }
    }

    private void deleteIfExists(String publicPath) {
        // publicPath: /uploads/company/xxx.png
        String prefix = "/uploads/";
        if (!publicPath.startsWith(prefix)) return;
        String relative = publicPath.substring(prefix.length()); // company/xxx.png
        Path filePath = uploadRoot.resolve(relative).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) return null;
        return s.trim();
    }

    private static String normalizePhone(String s) {
        String v = trimToNull(s);
        if (v == null) return null;
        // 숫자/공백/하이픈만 허용(너무 타이트하게는 막지 않음)
        if (!v.matches("^[0-9\\-\\s]+$")) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
        }
        return v;
    }

    private static String normalizeDigitsOrNull(String s, int digits, String fieldName) {
        String v = trimToNull(s);
        if (v == null) return null;
        String only = v.replaceAll("[^0-9]", "");
        if (only.isEmpty()) return null;
        if (!only.matches("^[0-9]+$")) {
            throw new IllegalArgumentException(fieldName + "는 숫자만 입력 가능합니다.");
        }
        if (only.length() != digits) {
            throw new IllegalArgumentException(fieldName + "는 " + digits + "자리 숫자여야 합니다.");
        }
        return only;
    }

    private static String normalizeZipCode(String s) {
        String v = trimToNull(s);
        if (v == null) return null;
        String only = v.replaceAll("[^0-9]", "");
        if (only.isEmpty()) return null;
        // 대한민국 우편번호 5자리
        if (only.length() != 5) {
            throw new IllegalArgumentException("우편번호는 5자리 숫자여야 합니다.");
        }
        return only;
    }
}

