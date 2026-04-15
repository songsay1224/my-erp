package com.example.sayy.Mapper;

import com.example.sayy.Entity.ClientContactEntity;
import com.example.sayy.Entity.ClientEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClientMapper {
    List<ClientEntity> selectAll(@Param("status") String status,
                                 @Param("clientType") String clientType,
                                 @Param("keyword") String keyword,
                                 @Param("limit") Integer limit,
                                 @Param("offset") Integer offset);
    long countAll(@Param("status") String status,
                  @Param("clientType") String clientType,
                  @Param("keyword") String keyword);
    ClientEntity selectById(@Param("id") Long id);
    int insert(ClientEntity client);
    int update(ClientEntity client);
    int deleteById(@Param("id") Long id);
    int deleteByIds(@Param("ids") List<Long> ids);

    List<ClientContactEntity> selectContactsByClientId(@Param("clientId") Long clientId);
    int insertContact(ClientContactEntity contact);
    int updateContact(ClientContactEntity contact);
    int deleteContact(@Param("id") Long id, @Param("clientId") Long clientId);
    int deleteAllContacts(@Param("clientId") Long clientId);
}
