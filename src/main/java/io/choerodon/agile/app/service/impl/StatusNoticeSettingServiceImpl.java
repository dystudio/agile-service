package io.choerodon.agile.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.choerodon.agile.api.vo.StatusNoticeSettingVO;
import io.choerodon.agile.api.vo.UserVO;
import io.choerodon.agile.app.service.StatusNoticeSettingService;
import io.choerodon.agile.infra.dto.IssueDTO;
import io.choerodon.agile.infra.dto.StatusDTO;
import io.choerodon.agile.infra.dto.StatusNoticeSettingDTO;
import io.choerodon.agile.infra.enums.StatusNoticeUserType;
import io.choerodon.agile.infra.feign.BaseFeignClient;
import io.choerodon.agile.infra.mapper.*;
import io.choerodon.agile.infra.utils.ConvertUtil;
import io.choerodon.agile.infra.utils.SendMsgUtil;
import io.choerodon.core.exception.CommonException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 邮件通知应用服务默认实现
 *
 * @author choerodon@choerodon.cn 2020-08-12 11:41:01
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class StatusNoticeSettingServiceImpl implements StatusNoticeSettingService {

    @Autowired
    private StatusNoticeSettingMapper statusNoticeSettingMapper;
    @Autowired
    private StatusMapper statusMapper;
    @Autowired
    private IssueMapper issueMapper;
    @Autowired
    private IssueTypeMapper issueTypeMapper;
    @Autowired
    private SendMsgUtil sendMsgUtil;
    @Autowired
    private BaseFeignClient baseFeignClient;
    @Autowired
    private FieldValueMapper fieldValueMapper;

    @Override
    public StatusNoticeSettingVO detail(Long projectId, Long issueTypeId, Long statusId) {
        StatusNoticeSettingVO statusNoticeSettingVO = new StatusNoticeSettingVO(projectId, issueTypeId, statusId);
        StatusNoticeSettingDTO notice = new StatusNoticeSettingDTO(projectId, issueTypeId, statusId);
        List<StatusNoticeSettingDTO> list = statusNoticeSettingMapper.select(notice);
        if (CollectionUtils.isEmpty(list)){
            return statusNoticeSettingVO;
        }
        list.forEach(item -> statusNoticeSettingVO.addUserWithNotice(item.getUserType(), item.getUserId()));
        statusNoticeSettingVO.setNoticeTypeList(Stream.of(StringUtils.split(list.stream().map(StatusNoticeSettingDTO::getNoticeType)
                .findFirst().orElse(""), BaseConstants.Symbol.COMMA)).collect(Collectors.toList()));
        return statusNoticeSettingVO;
    }

    @Override
    public void save(Long projectId, StatusNoticeSettingVO statusNoticeSettingVO) {
        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setId(statusNoticeSettingVO.getStatusId());
        statusDTO.setOrganizationId(ConvertUtil.getOrganizationId(projectId));
        statusDTO.setObjectVersionNumber(statusNoticeSettingVO.getObjectVersionNumber());
        if (Objects.isNull(statusMapper.selectOne(statusDTO))){
            throw new CommonException(BaseConstants.ErrorCode.DATA_INVALID);
        }
        StatusNoticeSettingDTO noticeDTO = new StatusNoticeSettingDTO(projectId, statusNoticeSettingVO.getIssueTypeId(),
                statusNoticeSettingVO.getStatusId());
        // 删除
        List<StatusNoticeSettingDTO> deleteList = statusNoticeSettingMapper.select(noticeDTO);
        if (CollectionUtils.isNotEmpty(deleteList)){
            deleteList.forEach(item -> statusNoticeSettingMapper.delete(item));
        }
        // 插入
        List<StatusNoticeSettingDTO> saveList = statusNoticeSettingVO.getUserTypeList()
                .stream()
                .map(useType -> new StatusNoticeSettingDTO(statusNoticeSettingVO, useType))
                .collect(Collectors.toList());
        saveList.addAll(statusNoticeSettingVO.getUserIdList()
                .stream()
                .map(userId -> new StatusNoticeSettingDTO(statusNoticeSettingVO, userId))
                .collect(Collectors.toList()));
        saveList.forEach(statusNoticeSettingMapper::insertSelective);
        int i = statusMapper.updateOptional(statusDTO);
        if (i != 1){
            throw new CommonException(BaseConstants.ErrorCode.OPTIMISTIC_LOCK);
        }
    }

    @Override
    public void noticeByChangeStatus(Long projectId, Long issueId) {
        // 根据issueId找到对应的issueType和status
        IssueDTO issue = issueMapper.selectByPrimaryKey(issueId);
        Assert.notNull(issue, BaseConstants.ErrorCode.DATA_NOT_EXISTS);
        // 找到通知内容
        List<StatusNoticeSettingDTO> noticeList = statusNoticeSettingMapper.select(new StatusNoticeSettingDTO(projectId,
                issue.getIssueTypeId(), issue.getStatusId()));
        // 根据类型找到接收人
        Set<Long> userSet = new HashSet<>();
        noticeList.forEach(noticeDTO -> this.receiverType2User(projectId, noticeDTO, issue, userSet));
        // 分析发送渠道发消息，一条一条发
        sendMsgUtil.noticeIssueStatus(userSet,
                Arrays.asList(StringUtils.split(noticeList.stream()
                        .map(StatusNoticeSettingDTO::getNoticeType).findFirst().orElse(""), BaseConstants.Symbol.COMMA)));
    }

    private void receiverType2User(Long projectId, StatusNoticeSettingDTO noticeDTO, IssueDTO issue, Set<Long> userSet) {
        switch (noticeDTO.getUserType()){
            case StatusNoticeUserType.PROJECT_OWNER:
                userSet.addAll(baseFeignClient.listProjectOwnerById(projectId).getBody().stream().map(UserVO::getId).collect(Collectors.toSet()));
                break;
            case StatusNoticeUserType.ASSIGNEE:
                userSet.add(issue.getAssigneeId());
                break;
            case StatusNoticeUserType.REPORTER:
                userSet.add(issue.getReporterId());
                break;
            case StatusNoticeUserType.SPECIFIER:
                userSet.add(noticeDTO.getUserId());
                break;
            default:
                // 不在默认配置里，则检索自定义字段，有则加入，没有则忽略
                userSet.add(fieldValueMapper.selectUserIdByField(projectId, noticeDTO.getUserType(), issue.getIssueId()));
                break;
        }
    }
}
