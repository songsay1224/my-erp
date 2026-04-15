package com.example.sayy.Service;

import com.example.sayy.Entity.ClientContactEntity;
import com.example.sayy.Entity.ClientEntity;
import com.example.sayy.Mapper.ClientMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClientService {

    private final ClientMapper clientMapper;

    public ClientService(ClientMapper clientMapper) {
        this.clientMapper = clientMapper;
    }

    public List<ClientEntity> getClients(String status, String clientType, String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return clientMapper.selectAll(
                blank(status) ? null : status,
                blank(clientType) ? null : clientType,
                blank(keyword) ? null : keyword.trim(),
                pageSize, offset
        );
    }

    public long countClients(String status, String clientType, String keyword) {
        return clientMapper.countAll(
                blank(status) ? null : status,
                blank(clientType) ? null : clientType,
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
    public Long createClient(ClientEntity c) {
        if (blank(c.getName())) throw new IllegalArgumentException("거래처명은 필수입니다.");
        c.setName(c.getName().trim());
        c.setBusinessRegNo(normalizeBusinessRegNo(c.getBusinessRegNo()));
        c.setCorporateRegNo(normalizeCorporateRegNo(c.getCorporateRegNo()));
        if (blank(c.getClientType())) c.setClientType("매출");
        if (blank(c.getStatus())) c.setStatus("ACTIVE");
        clientMapper.insert(c);
        return c.getId();
    }

    @Transactional
    public void updateClient(Long id, ClientEntity c) {
        if (blank(c.getName())) throw new IllegalArgumentException("거래처명은 필수입니다.");
        c.setId(id);
        c.setName(c.getName().trim());
        c.setBusinessRegNo(normalizeBusinessRegNo(c.getBusinessRegNo()));
        c.setCorporateRegNo(normalizeCorporateRegNo(c.getCorporateRegNo()));
        if (blank(c.getClientType())) c.setClientType("매출");
        if (blank(c.getStatus())) c.setStatus("ACTIVE");
        clientMapper.update(c);
    }

    private static String normalizeBusinessRegNo(String v) {
        if (blank(v)) return null;
        String d = v.replaceAll("[^0-9]", "");
        if (d.length() == 10) return d.substring(0, 3) + "-" + d.substring(3, 5) + "-" + d.substring(5);
        return v.trim();
    }

    private static String normalizeCorporateRegNo(String v) {
        if (blank(v)) return null;
        String d = v.replaceAll("[^0-9]", "");
        if (d.length() == 13) return d.substring(0, 6) + "-" + d.substring(6);
        return v.trim();
    }

    @Transactional
    public void deleteClient(Long id) { clientMapper.deleteById(id); }

    @Transactional
    public void deleteClients(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        clientMapper.deleteByIds(ids);
    }

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
