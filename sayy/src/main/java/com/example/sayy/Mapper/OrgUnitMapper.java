package com.example.sayy.Mapper;

import com.example.sayy.Entity.OrgUnitEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrgUnitMapper {
    List<OrgUnitEntity> selectAll();

    List<OrgUnitEntity> selectChildren(@Param("parentId") Long parentId);

    OrgUnitEntity selectById(@Param("id") long id);

    int countChildren(@Param("id") long id);

    Integer selectMaxSortOrder(@Param("parentId") Long parentId);

    int insert(OrgUnitEntity entity);

    int updateName(@Param("id") long id, @Param("name") String name);

    int updateSortOrder(@Param("id") long id, @Param("sortOrder") int sortOrder);

    int updateParentAndSort(@Param("id") long id,
                            @Param("parentId") Long parentId,
                            @Param("sortOrder") int sortOrder);

    int delete(@Param("id") long id);
}

