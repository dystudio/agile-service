package io.choerodon.agile.infra.utils;

import io.choerodon.agile.api.vo.*;
import io.choerodon.agile.api.vo.business.IssueVO;
import io.choerodon.agile.app.service.NoticeService;
import io.choerodon.agile.infra.dto.*;
import io.choerodon.agile.app.service.UserService;
import io.choerodon.agile.infra.dto.business.IssueDTO;
import io.choerodon.agile.infra.dto.business.IssueDetailDTO;
import io.choerodon.agile.infra.enums.SchemeApplyType;
import io.choerodon.agile.infra.feign.BaseFeignClient;
import io.choerodon.agile.infra.mapper.IssueStatusMapper;
import io.choerodon.agile.infra.mapper.ProjectInfoMapper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.message.entity.MessageSender;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * Created by HuangFuqiang@choerodon.io on 2019/4/29.
 * Email: fuqianghuang01@gmail.com
 */
@Component
public class SendMsgUtil {

    private static final String URL_TEMPLATE1 = "#/agile/work-list/issue?type=project&id=";
    private static final String URL_TEMPLATE2 = "&name=";
    private static final String URL_TEMPLATE3 = "&paramName=";
    private static final String URL_TEMPLATE4 = "&paramIssueId=";
    private static final String URL_TEMPLATE5 = "&paramOpenIssueId=";
    private static final String URL_TEMPLATE6 = "&organizationId=";
    private static final String URL_TEMPLATE7 = "&orgId=";
    private static final String ERROR_PROJECT_NOTEXIST = "error.project.notExist";
    private static final String SUB_TASK = "sub_task";
    private static final String STATUS_ID = "statusId";
    private static final String INSERT = "insert";
    private static final String IMAGE = "image";

    private static final String FEATURE_URL_TEMPLATE1 = "#/agile/feature?type=project&id=";
    private static final String FEATURE_URL_TEMPLATE2 = "&name=";
    private static final String FEATURE_URL_TEMPLATE3 = "&category=";
    private static final String FEATURE_URL_TEMPLATE4 = "&organizationId=";
    @Autowired
    private SiteMsgUtil siteMsgUtil;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @Autowired
    private IssueStatusMapper issueStatusMapper;

    @Autowired
    private BaseFeignClient baseFeignClient;

    @Autowired
    private ProjectInfoMapper projectInfoMapper;
    @Autowired
    private ModelMapper modelMapper;

    private String convertProjectName(ProjectVO projectVO) {
        String projectName = projectVO.getName();
        return projectName.replaceAll(" ", "%20");
    }

    @Async
    public void sendMsgByIssueCreate(Long projectId, IssueVO result) {
        //发送消息
        if (SchemeApplyType.AGILE.equals(result.getApplyType())) {
            List<Long> userIds = noticeService.queryUserIdsByProjectId(projectId, "ISSUECREATE", result);
            String summary = result.getIssueNum() + "-" + result.getSummary();
            String reporterName = result.getReporterName();
            ProjectVO projectVO = getProjectVO(projectId, ERROR_PROJECT_NOTEXIST);
            String url = getIssueCreateUrl(result, projectVO, result.getIssueId());
            siteMsgUtil.issueCreate(userIds, reporterName, summary, url, result.getReporterId(), projectId);
            if (result.getAssigneeId() != null) {
                List<Long> assigneeIds = new ArrayList<>();
                assigneeIds.add(result.getAssigneeId());
                siteMsgUtil.issueAssignee(assigneeIds, result.getAssigneeName(), summary, url, projectId, reporterName);
            }
        }
    }

    public String getIssueCreateUrl(IssueVO result, ProjectVO projectVO, Long paramIssueId) {
        return URL_TEMPLATE1 + projectVO.getId() 
                + URL_TEMPLATE2 + convertProjectName(projectVO) 
                + URL_TEMPLATE6 + projectVO.getOrganizationId() 
                + URL_TEMPLATE7 + projectVO.getOrganizationId() 
                + URL_TEMPLATE3 + result.getIssueNum() 
                + URL_TEMPLATE4 + paramIssueId 
                + URL_TEMPLATE5 + result.getIssueId();
    }

    public String getFeatureUrl(ProjectVO projectVO) {
        return FEATURE_URL_TEMPLATE1 + projectVO.getId()
                + FEATURE_URL_TEMPLATE2 + convertProjectName(projectVO)
                + FEATURE_URL_TEMPLATE3 + projectVO.getCategory()
                + FEATURE_URL_TEMPLATE4 + projectVO.getOrganizationId();
    }

    @Async
    public void sendMsgBySubIssueCreate(Long projectId, IssueSubVO result) {
        // 发送消息
        if (SchemeApplyType.AGILE.equals(result.getApplyType())) {
            IssueVO issueVO = new IssueVO();
            issueVO.setReporterId(result.getReporterId());
            List<Long> userIds = noticeService.queryUserIdsByProjectId(projectId, "ISSUECREATE", issueVO);
            String summary = result.getIssueNum() + "-" + result.getSummary();
            String reporterName = result.getReporterName();
            ProjectVO projectVO = getProjectVO(projectId, ERROR_PROJECT_NOTEXIST);
            String projectName = convertProjectName(projectVO);
            String url = URL_TEMPLATE1 + projectId + URL_TEMPLATE2 + projectName + URL_TEMPLATE6 + projectVO.getOrganizationId() + URL_TEMPLATE7 + projectVO.getOrganizationId() + URL_TEMPLATE3 + result.getIssueNum() + URL_TEMPLATE4 + result.getParentIssueId() + URL_TEMPLATE5 + result.getIssueId();
            siteMsgUtil.issueCreate(userIds, reporterName, summary, url, result.getReporterId(), projectId);
            Long getAssigneeId = result.getAssigneeId();
            if (!ObjectUtils.isEmpty(getAssigneeId)) {
                siteMsgUtil.issueAssignee(Arrays.asList(getAssigneeId), result.getAssigneeName(), summary, url, projectId, reporterName);
            }
        }
    }

    @Async
    public void sendMsgByIssueAssignee(Long projectId, List<String> fieldList, IssueVO result) {
        if (fieldList.contains("assigneeId") && result.getAssigneeId() != null && SchemeApplyType.AGILE.equals(result.getApplyType())) {
            List<Long> userIds = noticeService.queryUserIdsByProjectId(projectId, "ISSUEASSIGNEE", result);
            String summary = result.getIssueNum() + "-" + result.getSummary();
            String assigneeName = result.getAssigneeName();
            ProjectVO projectVO = getProjectVO(projectId, ERROR_PROJECT_NOTEXIST);
            String projectName = convertProjectName(projectVO);
            StringBuilder url = new StringBuilder();
            if (SUB_TASK.equals(result.getTypeCode())) {
                url.append(URL_TEMPLATE1 + projectId + URL_TEMPLATE2 + projectName + URL_TEMPLATE6 + projectVO.getOrganizationId() + URL_TEMPLATE7 + projectVO.getOrganizationId() + URL_TEMPLATE3 + result.getIssueNum() + URL_TEMPLATE4 + result.getParentIssueId() + URL_TEMPLATE5 + result.getIssueId());
            } else {
                url.append(URL_TEMPLATE1 + projectId + URL_TEMPLATE2 + projectName + URL_TEMPLATE6 + projectVO.getOrganizationId() + URL_TEMPLATE7 + projectVO.getOrganizationId() + URL_TEMPLATE3 + result.getIssueNum() + URL_TEMPLATE4 + result.getIssueId() + URL_TEMPLATE5 + result.getIssueId());
            }
            siteMsgUtil.issueAssignee(userIds, assigneeName, summary, url.toString(), projectId, getOperatorNameFromUserDetail());
        }
    }

    private String getOperatorNameFromUserDetail() {
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (ObjectUtils.isEmpty(userDetails)) {
            throw new CommonException("error.user.not.login");
        }
        Long userId = userDetails.getUserId();
        Map<Long, UserMessageDTO> userMap = userService.queryUsersMap(Arrays.asList(userId), true);
        UserMessageDTO user = userMap.get(userId);
        if (ObjectUtils.isEmpty(user)) {
            return null;
        } else {
            return user.getName();
        }
    }

    @Async
    public void sendMsgByIssueComplete(Long projectId, List<String> fieldList, IssueVO result) {
        Boolean completed = issueStatusMapper.selectByStatusId(projectId, result.getStatusId()).getCompleted();
        if (fieldList.contains(STATUS_ID) && completed != null && completed && result.getAssigneeId() != null && SchemeApplyType.AGILE.equals(result.getApplyType())) {
            List<Long> userIds = noticeService.queryUserIdsByProjectId(projectId, "ISSUESOLVE", result);
            ProjectVO projectVO = getProjectVO(projectId, ERROR_PROJECT_NOTEXIST);
            String projectName = convertProjectName(projectVO);
            StringBuilder url = new StringBuilder();
            if (SUB_TASK.equals(result.getTypeCode())) {
                url.append(URL_TEMPLATE1 + projectId + URL_TEMPLATE2 + projectName + URL_TEMPLATE6 + projectVO.getOrganizationId() + URL_TEMPLATE7 + projectVO.getOrganizationId() + URL_TEMPLATE3 + result.getIssueNum() + URL_TEMPLATE4 + result.getParentIssueId() + URL_TEMPLATE5 + result.getIssueId());
            } else {
                url.append(URL_TEMPLATE1 + projectId + URL_TEMPLATE2 + projectName + URL_TEMPLATE6 + projectVO.getOrganizationId() + URL_TEMPLATE7 + projectVO.getOrganizationId() + URL_TEMPLATE3 + result.getIssueNum() + URL_TEMPLATE4 + result.getIssueId() + URL_TEMPLATE5 + result.getIssueId());
            }
            Long[] ids = new Long[1];
            ids[0] = result.getAssigneeId();
            List<UserDTO> userDTOList = baseFeignClient.listUsersByIds(ids, false).getBody();
            String userName = !userDTOList.isEmpty() && userDTOList.get(0) != null ? userDTOList.get(0).getRealName() + "(" + userDTOList.get(0).getLoginName() + ")" : "";
            String summary = result.getIssueNum() + "-" + result.getSummary();
            siteMsgUtil.issueSolve(userIds, userName, summary, url.toString(), projectId, getOperatorNameFromUserDetail());
        }
    }

    @Async
    public void sendMsgByIssueMoveComplete(Long projectId, IssueMoveVO issueMoveVO, IssueDTO issueDTO) {
        // 发送消息
        Boolean completed = issueStatusMapper.selectByStatusId(projectId, issueMoveVO.getStatusId()).getCompleted();
        if (completed != null && completed && issueDTO.getAssigneeId() != null && SchemeApplyType.AGILE.equals(issueDTO.getApplyType())) {
            List<Long> userIds = noticeService.queryUserIdsByProjectId(projectId, "ISSUESOLVE", modelMapper.map(issueDTO, IssueVO.class));
            ProjectVO projectVO = getProjectVO(projectId, "error.project.notExist");
            StringBuilder url = new StringBuilder();
            String projectName = convertProjectName(projectVO);
            ProjectInfoDTO projectInfoDTO = new ProjectInfoDTO();
            projectInfoDTO.setProjectId(projectId);
            List<ProjectInfoDTO> pioList = projectInfoMapper.select(projectInfoDTO);
            ProjectInfoDTO pio = null;
            if (pioList != null && !pioList.isEmpty()) {
                pio = pioList.get(0);
            }
            String pioCode = (pio == null ? "" : pio.getProjectCode());
            if ("sub_task".equals(issueDTO.getTypeCode())) {
                url.append(URL_TEMPLATE1 + projectId + URL_TEMPLATE2 + projectName + URL_TEMPLATE6 + projectVO.getOrganizationId() + URL_TEMPLATE7 + projectVO.getOrganizationId() + URL_TEMPLATE3 + pioCode + "-" + issueDTO.getIssueNum() + URL_TEMPLATE4 + issueDTO.getParentIssueId() + URL_TEMPLATE5 + issueDTO.getIssueId());
            } else {
                url.append(URL_TEMPLATE1 + projectId + URL_TEMPLATE2 + projectName + URL_TEMPLATE6 + projectVO.getOrganizationId() + URL_TEMPLATE7 + projectVO.getOrganizationId() + URL_TEMPLATE3 + pioCode + "-" + issueDTO.getIssueNum() + URL_TEMPLATE4 + issueDTO.getIssueId() + URL_TEMPLATE5 + issueDTO.getIssueId());
            }
            String summary = pioCode + "-" + issueDTO.getIssueNum() + "-" + issueDTO.getSummary();
            Long[] ids = new Long[1];
            ids[0] = issueDTO.getAssigneeId();
            List<UserDTO> userDTOList = userService.listUsersByIds(ids);
            String userName = !userDTOList.isEmpty() && userDTOList.get(0) != null ? userDTOList.get(0).getRealName() + "(" + userDTOList.get(0).getLoginName() + ")" : "";
            siteMsgUtil.issueSolve(userIds, userName, summary, url.toString(), projectId, getOperatorNameFromUserDetail());
        }
    }

    @Async
    public void noticeIssueStatus(Long projectId, Set<Long> userSet, List<String> noticeTypeList, IssueDTO issueDTO,
                                  CustomUserDetails userDetails) {
        if (CollectionUtils.isEmpty(userSet)){
            return;
        }
        Map<String, String> templateArgsMap = new HashMap<>();
        // 设置经办人
        Long[] ids = new Long[2];
        ids[0] = issueDTO.getAssigneeId();
        ids[1] = userDetails.getUserId();
        List<UserDTO> userDTOList = userService.listUsersByIds(ids);
        String assigneeName = userDTOList.stream().filter(user -> Objects.equals(user.getId(), issueDTO.getAssigneeId()))
                .findFirst().map(UserDTO::getRealName).orElse("");
        // 设置概要
        String summary = issueDTO.getIssueNum() + "-" + issueDTO.getSummary();
        // 设置操作人
        String operatorName = userDTOList.stream().filter(user -> Objects.equals(user.getId(), userDetails.getUserId()))
                .findFirst().map(UserDTO::getRealName).orElse("");
        // 设置状态
        String status = ConvertUtil.getIssueStatusMap(projectId).get(issueDTO.getStatusId()).getName();
        templateArgsMap.put("assigneeName", assigneeName);
        templateArgsMap.put("summary", summary);
        templateArgsMap.put("operatorName", operatorName);
        templateArgsMap.put("status", status);
        siteMsgUtil.sendChangeIssueStatus(projectId, userSet, noticeTypeList, templateArgsMap);
    }


    public String queryUserName(Long userId) {
        Map<Long, UserMessageDTO> userMessageDOMap = userService.queryUsersMap(Collections.singletonList(userId), true);
        return Optional.ofNullable(userMessageDOMap.get(userId)).map(UserMessageDTO::getName).orElse("");
    }


    public MessageSender generateIssueAsigneeSender(Long projectId, Set<String> fieldList, IssueDTO issue) {
        if (!SchemeApplyType.AGILE.equals(issue.getApplyType())) {
            return null;
        }
        if (Objects.isNull(issue.getAssigneeId())) {
            return null;
        }
        if (Objects.nonNull(fieldList) && !fieldList.contains("assigneeId")){
            return null;
        }
        IssueVO result = modelMapper.map(issue, IssueVO.class);
        String summary = result.getIssueNum() + "-" + result.getSummary();
        String reporterName = queryUserName(result.getReporterId());
        String assigneeName = queryUserName(result.getAssigneeId());
        ProjectVO projectVO = getProjectVO(projectId, ERROR_PROJECT_NOTEXIST);
        String url = getIssueCreateUrl(result, projectVO, result.getIssueId());
        return siteMsgUtil.issueAssigneeSender(Collections.singletonList(result.getAssigneeId()),
                assigneeName, summary, url, projectId, reporterName);
    }

    public MessageSender generateIssueResolvSender(Long projectId, Set<String> fieldList, IssueDTO issue) {
        IssueVO result = modelMapper.map(issue, IssueVO.class);
        Boolean completed = issueStatusMapper.selectByStatusId(projectId, result.getStatusId()).getCompleted();
        if ((Objects.nonNull(fieldList) && !fieldList.contains(STATUS_ID))
                || completed == null 
                || !completed 
                || result.getAssigneeId() == null 
                || !SchemeApplyType.AGILE.equals(result.getApplyType())) {
            return null;
        }
        List<Long> userIds = noticeService.queryUserIdsByProjectId(projectId, "ISSUESOLVE", result);
        ProjectVO projectVO = getProjectVO(projectId, ERROR_PROJECT_NOTEXIST);
        StringBuilder url = new StringBuilder();
        if (SUB_TASK.equals(result.getTypeCode())) {
            url.append(getIssueCreateUrl(result, projectVO, result.getParentIssueId()));
        } else {
            url.append(getIssueCreateUrl(result, projectVO, result.getIssueId()));
        }
        String userName = queryUserName(result.getAssigneeId());
        String summary = result.getIssueNum() + "-" + result.getSummary();
        return siteMsgUtil.issueSolveSender(userIds, userName, summary, url.toString(), projectId, getOperatorNameFromUserDetail());
    }

    public MessageSender generateNoticeIssueStatusSender(Long projectId, Set<Long> userSet, List<String> noticeTypeList, 
                                                         IssueDTO issueDTO, CustomUserDetails userDetails, Set<String> fieldList) {
        if (CollectionUtils.isEmpty(userSet)){
            return null;
        }
        if (Objects.nonNull(fieldList) && !fieldList.contains(STATUS_ID)){
            return null;
        }
        Map<String, String> templateArgsMap = new HashMap<>();
        // 设置经办人
        Map<Long, UserMessageDTO> userMap = userService.queryUsersMap(Arrays.asList(issueDTO.getAssigneeId(), userDetails.getUserId()), true);
        String assigneeName = Optional.ofNullable(userMap.get(issueDTO.getAssigneeId())).map(UserMessageDTO::getName).orElse("");
        // 设置操作人
        String operatorName = Optional.ofNullable(userMap.get(userDetails.getUserId())).map(UserMessageDTO::getName).orElse("");
        // 设置概要
        String summary = issueDTO.getIssueNum() + "-" + issueDTO.getSummary();
        // 设置状态
        String status = ConvertUtil.getIssueStatusMap(projectId).get(issueDTO.getStatusId()).getName();
        templateArgsMap.put("assigneeName", assigneeName);
        templateArgsMap.put("summary", summary);
        templateArgsMap.put("operatorName", operatorName);
        templateArgsMap.put("status", status);
        return siteMsgUtil.sendChangeIssueStatusSender(projectId, userSet, noticeTypeList, templateArgsMap);
    }

    public ProjectVO getProjectVO(Long projectId, String errorProjectNotexist) {
        ProjectVO projectVO = userService.queryProject(projectId);
        if (projectVO == null) {
            throw new CommonException(errorProjectNotexist);
        }
        return projectVO;
    }

    @Async
    public void sendMsgByIssueComment(Long projectId, IssueDetailDTO issueDTO, IssueCommentVO issueCommentVO) {
        IssueVO issueVO = modelMapper.map(issueDTO, IssueVO.class);
        Map<Long, String> actionMap = new HashMap<>(3);
        String url;
        String issueType;

        ProjectVO projectVO = getProjectVO(projectId, ERROR_PROJECT_NOTEXIST);
        if ("feature".equals(issueVO.getTypeCode())) {
            issueType = "特性";
            url = getFeatureUrl(projectVO);
        } else {
            issueType = "问题";
            url = getIssueCreateUrl(issueVO, projectVO, issueVO.getIssueId());
        }
        //设置动作与发送人
        List<Long> userIds = noticeService.queryUserIdsByProjectId(projectId, "ISSUE_COMMENT", issueVO);
        setIssueCommentMessageActionAndUser(actionMap, issueCommentVO.getUserId(), issueVO, projectVO, userIds);
        String summary = String.join("-", issueVO.getIssueNum(), issueVO.getSummary());
        String comment = Optional.ofNullable(issueCommentVO.getCommentText()).map(SendMsgUtil::getText).orElse("无");

        if (CollectionUtils.isNotEmpty(actionMap.keySet())) {
            siteMsgUtil.sendIssueComment(actionMap, projectVO, summary, url, comment, issueCommentVO, issueType);
        }
    }

    private void setIssueCommentMessageActionAndUser(Map<Long, String> actionMap, Long userId, IssueVO issueVO, ProjectVO projectVO, List<Long> userIds) {
        Map<Long, String> map = new HashMap<>(2);
        map.put(issueVO.getAssigneeId(), "处理的");
        map.put(issueVO.getReporterId(), "负责的");
        userIds.forEach(sendUserId -> {
            if(sendUserId.equals(userId)){
                return;
            }
            actionMap.put(sendUserId, map.getOrDefault(userId, "管理的"));
        });
    }

    @Async
    public void sendMsgByIssueCommentReply(Long projectId, IssueDetailDTO issueDTO, IssueCommentVO issueCommentVO) {
        Map<Long, String> actionMap = new HashMap<>(1);
        actionMap.put(issueCommentVO.getReplyToUserId(), "评论的");
        IssueVO issueVO = modelMapper.map(issueDTO, IssueVO.class);
        String url;
        String issueType;

        ProjectVO projectVO = getProjectVO(projectId, ERROR_PROJECT_NOTEXIST);
        if ("feature".equals(issueVO.getTypeCode())) {
            issueType = "特性";
            url = getFeatureUrl(projectVO);
        } else {
            issueType = "问题";
            url = getIssueCreateUrl(issueVO, projectVO, issueVO.getIssueId());
        }

        String summary = String.join("-", issueVO.getIssueNum(), issueVO.getSummary());
        String comment = Optional.ofNullable(issueCommentVO.getCommentText()).map(SendMsgUtil::getText).orElse("无");
        siteMsgUtil.sendIssueComment(actionMap, projectVO, summary, url, comment, issueCommentVO, issueType);
    }

    public static String getText(String rawText) {
        if (StringUtils.isEmpty(rawText)){
            return null;
        }
        StringBuilder result = new StringBuilder();
        try {
            JSONArray root = JSON.parseArray(rawText);
            for (Object o : root) {
                JSONObject object = (JSONObject) o;
                if (!(object.get(INSERT) instanceof JSONObject)) {
                    result.append(object.getString(INSERT));
                } else if (!(((JSONObject) object.get(INSERT)).get(IMAGE) instanceof JSONObject)) {
                    result.append("<img style=\"width: auto;height: auto;max-width: 650px;\" src=\"").append(((JSONObject) object.get(INSERT)).getString(IMAGE)).append("\"></img>");
                }
            }
        } catch (Exception e) {
            return rawText;
        }
        if (result.length() == 0) {
            return null;
        }
        StringUtils.replace(rawText, "\n", "<br >");
        return result.toString();
    }
}
