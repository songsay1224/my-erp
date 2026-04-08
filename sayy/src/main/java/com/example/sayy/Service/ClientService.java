package com.example.sayy.Service;

import com.example.sayy.Entity.ClientContactEntity;
import com.example.sayy.Entity.ClientEntity;
import com.example.sayy.Mapper.ClientMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientMapper clientMapper;

    public ClientService(ClientMapper clientMapper) {
        this.clientMapper = clientMapper;
    }

    public List<ClientEntity> getClients(String status, String keyword) {
        return clientMapper.selectAll(
                blank(status) ? null : status,
                blank(keyword) ? null : keyword.trim()
        );
    }

    public ClientEntity getClientOrThrow(Long id) {
        ClientEntity c = clientMapper.selectById(id);
        if (c == null) throw new IllegalArgumentException("거래처를 찾을 수 없습니다: " + id);
        c.setContacts(clientMapper.selectContactsByClientId(id));
        return c;
    }

    @Transactional
    public Long createClient(String name, String businessRegNo, String corporateRegNo,
                             String ceoName, String industry, String phone, String email,
                             String zipCode, String address, String addressDetail,
                             String status, String memo) {
        if (blank(name)) throw new IllegalArgumentException("거래처명은 필수입니다.");
        ClientEntity c = new ClientEntity();
        c.setName(name.trim());
        c.setBusinessRegNo(normalizeBusinessRegNo(businessRegNo));
        c.setCorporateRegNo(normalizeCorporateRegNo(corporateRegNo));
        c.setCeoName(trim(ceoName));
        c.setIndustry(trim(industry));
        c.setPhone(trim(phone));
        c.setEmail(trim(email));
        c.setZipCode(trim(zipCode));
        c.setAddress(trim(address));
        c.setAddressDetail(trim(addressDetail));
        c.setStatus(blank(status) ? "ACTIVE" : status.trim());
        c.setMemo(trim(memo));
        clientMapper.insert(c);
        return c.getId();
    }

    @Transactional
    public void updateClient(Long id, String name, String businessRegNo, String corporateRegNo,
                             String ceoName, String industry, String phone, String email,
                             String zipCode, String address, String addressDetail,
                             String status, String memo) {
        if (blank(name)) throw new IllegalArgumentException("거래처명은 필수입니다.");
        ClientEntity c = new ClientEntity();
        c.setId(id);
        c.setName(name.trim());
        c.setBusinessRegNo(normalizeBusinessRegNo(businessRegNo));
        c.setCorporateRegNo(normalizeCorporateRegNo(corporateRegNo));
        c.setCeoName(trim(ceoName));
        c.setIndustry(trim(industry));
        c.setPhone(trim(phone));
        c.setEmail(trim(email));
        c.setZipCode(trim(zipCode));
        c.setAddress(trim(address));
        c.setAddressDetail(trim(addressDetail));
        c.setStatus(blank(status) ? "ACTIVE" : status.trim());
        c.setMemo(trim(memo));
        clientMapper.update(c);
    }

    /** 사업자등록번호: 숫자 10자리 → XXX-XX-XXXXX */
    private static String normalizeBusinessRegNo(String v) {
        if (blank(v)) return null;
        String d = v.replaceAll("[^0-9]", "");
        if (d.length() == 10) return d.substring(0, 3) + "-" + d.substring(3, 5) + "-" + d.substring(5);
        return v.trim();
    }

    /** 법인등록번호: 숫자 13자리 → XXXXXX-XXXXXXX */
    private static String normalizeCorporateRegNo(String v) {
        if (blank(v)) return null;
        String d = v.replaceAll("[^0-9]", "");
        if (d.length() == 13) return d.substring(0, 6) + "-" + d.substring(6);
        return v.trim();
    }

    @Transactional
    public void deleteClient(Long id) {
        clientMapper.deleteById(id);
    }

    // ===== 담당자 =====

    @Transactional
    public void addContact(Long clientId, String name, String department, String position,
                           String phone, String email, boolean isPrimary) {
        if (blank(name)) throw new IllegalArgumentException("담당자명은 필수입니다.");
        ClientContactEntity c = new ClientContactEntity();
        c.setClientId(clientId);
        c.setName(name.trim());
        c.setDepartment(trim(department));
        c.setPosition(trim(position));
        c.setPhone(trim(phone));
        c.setEmail(trim(email));
        c.setIsPrimary(isPrimary);
        c.setSortOrder(0);
        clientMapper.insertContact(c);
    }

    @Transactional
    public void deleteContact(Long clientId, Long contactId) {
        clientMapper.deleteContact(contactId, clientId);
    }

    private static boolean blank(String v) { return v == null || v.isBlank(); }
    private static String trim(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
