package com.example.sayy.Service;

import com.example.sayy.DTO.OrgUnitRowDTO;
import com.example.sayy.Entity.OrgUnitEntity;
import com.example.sayy.Mapper.OrgUnitMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class OrgUnitService {
    private final OrgUnitMapper orgUnitMapper;

    public OrgUnitService(OrgUnitMapper orgUnitMapper) {
        this.orgUnitMapper = orgUnitMapper;
    }

    public List<OrgUnitEntity> getAll() {
        return orgUnitMapper.selectAll();
    }

    public List<OrgUnitRowDTO> listRows() {
        List<OrgUnitEntity> all = orgUnitMapper.selectAll();
        if (all.isEmpty()) return List.of();

        Map<Long, List<OrgUnitEntity>> childrenByParent = new HashMap<>();
        Map<Long, Integer> childCountById = new HashMap<>();

        for (OrgUnitEntity u : all) {
            if (u.getParentId() != null) {
                childCountById.put(u.getParentId(), childCountById.getOrDefault(u.getParentId(), 0) + 1);
            }
            childrenByParent.computeIfAbsent(u.getParentId(), k -> new ArrayList<>()).add(u);
        }

        for (List<OrgUnitEntity> list : childrenByParent.values()) {
            list.sort(Comparator
                    .comparing((OrgUnitEntity e) -> Optional.ofNullable(e.getSortOrder()).orElse(0))
                    .thenComparing(e -> Optional.ofNullable(e.getId()).orElse(0L)));
        }

        List<OrgUnitRowDTO> out = new ArrayList<>();
        Deque<OrgUnitEntity> stack = new ArrayDeque<>();
        Deque<Integer> depthStack = new ArrayDeque<>();

        List<OrgUnitEntity> roots = childrenByParent.getOrDefault(null, List.of());
        for (int i = roots.size() - 1; i >= 0; i--) {
            stack.push(roots.get(i));
            depthStack.push(0);
        }

        while (!stack.isEmpty()) {
            OrgUnitEntity cur = stack.pop();
            int depth = depthStack.pop();

            boolean hasChildren = childCountById.getOrDefault(cur.getId(), 0) > 0;
            out.add(new OrgUnitRowDTO(
                    cur.getId(),
                    cur.getName(),
                    cur.getParentId(),
                    depth,
                    hasChildren,
                    Optional.ofNullable(cur.getSortOrder()).orElse(0)
            ));

            List<OrgUnitEntity> kids = childrenByParent.get(cur.getId());
            if (kids == null || kids.isEmpty()) continue;
            for (int i = kids.size() - 1; i >= 0; i--) {
                stack.push(kids.get(i));
                depthStack.push(depth + 1);
            }
        }

        return out;
    }

    @Transactional
    public long create(String name, Long parentId) {
        String n = normalizeName(name);
        if (parentId != null) {
            OrgUnitEntity parent = orgUnitMapper.selectById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("상위 조직이 존재하지 않습니다.");
            }
        }

        Integer max = orgUnitMapper.selectMaxSortOrder(parentId);
        int nextSort = (max == null) ? 0 : (max + 1);

        OrgUnitEntity e = new OrgUnitEntity();
        e.setName(n);
        e.setParentId(parentId);
        e.setSortOrder(nextSort);
        orgUnitMapper.insert(e);
        return (e.getId() == null) ? 0L : e.getId();
    }

    @Transactional
    public void rename(long id, String name) {
        String n = normalizeName(name);
        OrgUnitEntity cur = orgUnitMapper.selectById(id);
        if (cur == null) {
            throw new IllegalArgumentException("조직이 존재하지 않습니다.");
        }
        orgUnitMapper.updateName(id, n);
    }

    @Transactional
    public void delete(long id) {
        OrgUnitEntity cur = orgUnitMapper.selectById(id);
        if (cur == null) {
            throw new IllegalArgumentException("조직이 존재하지 않습니다.");
        }
        int children = orgUnitMapper.countChildren(id);
        if (children > 0) {
            throw new IllegalArgumentException("하위 조직이 있는 항목은 삭제할 수 없습니다.");
        }
        orgUnitMapper.delete(id);
    }

    /**
     * Move node to another parent and/or reorder among siblings.
     * - target parent can be null(root)
     * - specify one of beforeId/afterId (both null -> append to end)
     */
    @Transactional
    public void move(long id, Long newParentId, Long beforeId, Long afterId) {
        OrgUnitEntity moving = orgUnitMapper.selectById(id);
        if (moving == null) {
            throw new IllegalArgumentException("이동할 조직이 존재하지 않습니다.");
        }
        if (newParentId != null) {
            if (newParentId == id) {
                throw new IllegalArgumentException("자기 자신 아래로 이동할 수 없습니다.");
            }
            OrgUnitEntity newParent = orgUnitMapper.selectById(newParentId);
            if (newParent == null) {
                throw new IllegalArgumentException("대상 상위 조직이 존재하지 않습니다.");
            }
            if (isDescendant(newParentId, id)) {
                throw new IllegalArgumentException("하위 조직 아래로 이동할 수 없습니다.");
            }
        }
        if (beforeId != null && afterId != null) {
            throw new IllegalArgumentException("정렬 기준이 올바르지 않습니다.");
        }

        List<OrgUnitEntity> siblings = new ArrayList<>(orgUnitMapper.selectChildren(newParentId));

        // remove itself if already in the list (moving within same parent)
        siblings.removeIf(s -> s.getId() != null && s.getId() == id);

        int insertIdx = siblings.size();
        if (beforeId != null) {
            insertIdx = indexOfId(siblings, beforeId);
        } else if (afterId != null) {
            insertIdx = indexOfId(siblings, afterId) + 1;
        }

        if (insertIdx < 0 || insertIdx > siblings.size()) {
            throw new IllegalArgumentException("정렬 대상이 올바르지 않습니다.");
        }

        // build new order list of ids
        List<Long> order = new ArrayList<>(siblings.size() + 1);
        for (int i = 0; i < insertIdx; i++) order.add(siblings.get(i).getId());
        order.add(id);
        for (int i = insertIdx; i < siblings.size(); i++) order.add(siblings.get(i).getId());

        // apply: set moving parent + sort, then reindex siblings
        for (int i = 0; i < order.size(); i++) {
            long curId = order.get(i);
            if (curId == id) {
                orgUnitMapper.updateParentAndSort(id, newParentId, i);
            } else {
                orgUnitMapper.updateSortOrder(curId, i);
            }
        }
    }

    private static String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("조직 이름을 입력해 주세요.");
        }
        String n = name.trim();
        if (n.length() > 100) {
            throw new IllegalArgumentException("조직 이름은 100자 이하여야 합니다.");
        }
        return n;
    }

    private static int indexOfId(List<OrgUnitEntity> list, long id) {
        for (int i = 0; i < list.size(); i++) {
            OrgUnitEntity e = list.get(i);
            if (e.getId() != null && e.getId() == id) return i;
        }
        throw new IllegalArgumentException("정렬 대상이 올바르지 않습니다.");
    }

    private boolean isDescendant(long nodeId, long potentialAncestorId) {
        // true if nodeId is inside subtree of potentialAncestorId
        List<OrgUnitEntity> all = orgUnitMapper.selectAll();
        Map<Long, Long> parentById = new HashMap<>();
        for (OrgUnitEntity e : all) {
            if (e.getId() == null) continue;
            parentById.put(e.getId(), e.getParentId());
        }
        Long cur = nodeId;
        int guard = 0;
        while (cur != null && guard++ < 10_000) {
            if (cur == potentialAncestorId) return true;
            cur = parentById.get(cur);
        }
        return false;
    }
}

