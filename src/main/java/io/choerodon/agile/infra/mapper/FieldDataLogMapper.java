package io.choerodon.agile.infra.mapper;

import io.choerodon.agile.api.vo.FieldDataLogCreateVO;
import io.choerodon.agile.infra.dto.FieldDataLogDTO;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author shinan.chen
 * @since 2019/6/19
 */
public interface FieldDataLogMapper extends BaseMapper<FieldDataLogDTO> {
    List<FieldDataLogDTO> queryByInstanceId(@Param("projectId") Long projectId, @Param("schemeCode") String schemeCode, @Param("instanceId") Long instanceId);

    void batchInsert(@Param("projectId") Long projectId, @Param("schemeCode") String schemeCode, @Param("list") List<FieldDataLogCreateVO> list, @Param("userId") Long userId);

    void deleteByInstanceIdAndFieldIds(@Param("projectId") Long projectId, @Param("instanceId") Long instanceId, @Param("schemeCode") String schemeCode, @Param("fieldIds") Set<Long> fieldIds);

    void updateProjectId(@Param("projectId") Long projectId, @Param("targetProjectId") Long targetProjectId, @Param("instanceId") Long instanceId, @Param("schemeCode") String schemeCode);
}
