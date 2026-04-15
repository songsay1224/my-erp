package com.example.sayy.Service;

import com.example.sayy.DTO.ClientImportRowDTO;
import com.example.sayy.Entity.ClientEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientBulkImportService {

    private final ClientService clientService;

    public ClientBulkImportService(ClientService clientService) {
        this.clientService = clientService;
    }

    @Transactional
    public int importAll(List<ClientImportRowDTO> rows) {
        int count = 0;
        for (ClientImportRowDTO row : rows) {
            if (row.hasError()) continue;
            if (row.getName() == null || row.getName().isBlank()) continue;

            ClientEntity c = new ClientEntity();
            c.setClientType(hasText(row.getClientType()) ? row.getClientType() : "매출");
            c.setName(row.getName().trim());
            c.setBusinessRegNo(row.getBusinessRegNo());
            c.setCorporateRegNo(row.getCorporateRegNo());
            c.setCeoName(row.getCeoName());
            c.setRegion(row.getRegion());
            c.setAddress(row.getAddress());
            c.setAddressDetail(row.getAddressDetail());
            c.setPhone(row.getPhone());
            c.setHomepage(row.getHomepage());
            c.setStatus("ACTIVE");

            clientService.createClient(c);
            count++;
        }
        return count;
    }

    private boolean hasText(String v) { return v != null && !v.isBlank(); }
}
