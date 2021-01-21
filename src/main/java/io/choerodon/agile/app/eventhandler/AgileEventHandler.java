package io.choerodon.agile.app.eventhandler;

import com.alibaba.fastjson.JSON;
import io.choerodon.agile.api.vo.ProjectInfoVO;
import io.choerodon.agile.api.vo.event.OrganizationCreateEventPayload;
import io.choerodon.agile.api.vo.event.ProjectEvent;
import io.choerodon.agile.api.vo.event.ProjectEventCategory;
import io.choerodon.agile.app.service.*;
import io.choerodon.agile.infra.dto.ProjectInfoDTO;
import io.choerodon.agile.infra.enums.InitStatus;
import io.choerodon.agile.infra.enums.ProjectCategory;
import io.choerodon.agile.infra.feign.operator.TestServiceClientOperator;
import io.choerodon.agile.infra.mapper.ProjectInfoMapper;
import io.choerodon.agile.infra.utils.SpringBeanUtil;
import io.choerodon.asgard.saga.annotation.SagaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.choerodon.agile.infra.utils.SagaTopic.Organization.ORG_CREATE;
import static io.choerodon.agile.infra.utils.SagaTopic.Organization.TASK_ORG_CREATE;
import static io.choerodon.agile.infra.utils.SagaTopic.Project.*;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/5/22.
 * Email: fuqianghuang01@gmail.com
 */
@Component
public class AgileEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgileEventHandler.class);

    @Autowired
    private ProjectInfoService projectInfoService;
    @Autowired
    private ProjectInfoMapper projectInfoMapper;
    @Autowired
    private IssueLinkTypeService issueLinkTypeService;
    @Autowired
    private IssueTypeService issueTypeService;
    @Autowired
    private StateMachineSchemeService stateMachineSchemeService;
    @Autowired
    private IssueTypeSchemeService issueTypeSchemeService;
    @Autowired
    private PriorityService priorityService;
    @Autowired
    private InitService initService;
    @Autowired
    private ObjectSchemeFieldService objectSchemeFieldService;

    @Autowired
    private TestServiceClientOperator testServiceClientOperator;

    @SagaTask(code = TASK_ORG_CREATE,
            description = "创建组织事件",
            sagaCode = ORG_CREATE,
            seq = 1)
    public String handleOrgaizationCreateByConsumeSagaTask(String data) {
        LOGGER.info("消费创建组织消息{}", data);
        OrganizationCreateEventPayload organizationEventPayload = JSON.parseObject(data, OrganizationCreateEventPayload.class);
        Long organizationId = organizationEventPayload.getId();
        //注册组织初始化问题类型
        issueTypeService.initIssueTypeByConsumeCreateOrganization(organizationId);
        //注册组织初始化优先级
        priorityService.initProrityByOrganization(Arrays.asList(organizationId));
        //初始化状态
        initService.initStatus(organizationId, InitStatus.listInitStatus());
        //初始化默认状态机
        initService.initDefaultStateMachine(organizationId);
        //初始化页面配置数据
        objectSchemeFieldService.createSystemFieldIfNotExisted(organizationId);
        return data;
    }

    /**
     * 创建项目事件
     *
     * @param message message
     */
    @SagaTask(code = TASK_PROJECT_CREATE,
            description = "agile消费创建项目事件初始化项目数据",
            sagaCode = PROJECT_CREATE,
            seq = 2)
    public String handleProjectInitByConsumeSagaTask(String message) {
        ProjectEvent projectEvent = JSON.parseObject(message, ProjectEvent.class);
        LOGGER.info("接受创建项目消息{}", message);
        List<ProjectEventCategory> projectEventCategories = projectEvent.getProjectCategoryVOS();
        if (!ObjectUtils.isEmpty(projectEventCategories)) {
            initIfAgileProject(projectEvent, projectEventCategories);
        }
        return message;
    }

    private void initIfAgileProject(ProjectEvent projectEvent, List<ProjectEventCategory> projectEventCategories) {
        Set<String> codes =
                projectEventCategories
                        .stream()
                        .map(ProjectEventCategory::getCode)
                        .collect(Collectors.toSet());
        if (ProjectCategory.consumeProjectCreatEvent(codes)) {
            LOGGER.info("初始化项目{}, code: {}", projectEvent.getProjectId(), projectEvent.getProjectCode());
            //创建projectInfo
            projectInfoService.initializationProjectInfo(projectEvent);
            //创建项目初始化issueLinkType
            issueLinkTypeService.initIssueLinkType(projectEvent.getProjectId());
            if (codes.contains(ProjectCategory.MODULE_PROGRAM)) {
                AgilePluginService pluginService = SpringBeanUtil.getExpandBean(AgilePluginService.class);
                if (pluginService != null) {
                    pluginService.initProjectIssueTypeSchemeAndArt(projectEvent);
                }
            } else {
                //创建项目时创建默认状态机方案
                stateMachineSchemeService.initByConsumeCreateProject(projectEvent);
                //创建项目时创建默认问题类型方案
                issueTypeSchemeService.initByConsumeCreateProject(projectEvent.getProjectId(), projectEvent.getProjectCode());
            }
        }
    }

    /**
     * 更新项目事件
     *
     * @param message message
     */
    @SagaTask(code = TASK_PROJECT_UPDATE, sagaCode = PROJECT_UPDATE,seq = 2,
            description = "agile消费更新项目事件初始化项目数据")
    public String handleProjectUpdateByConsumeSagaTask(String message) {
        ProjectEvent projectEvent = JSON.parseObject(message, ProjectEvent.class);
        LOGGER.info("接受更新项目消息{}", message);
        Long projectId = projectEvent.getProjectId();
        ProjectInfoDTO dto = new ProjectInfoDTO();
        dto.setProjectId(projectId);
        if (projectInfoMapper.select(dto).isEmpty()) {
            List<ProjectEventCategory> projectEventCategories = projectEvent.getProjectCategoryVOS();
            if (!ObjectUtils.isEmpty(projectEventCategories)) {
                syncProjectCode(projectId, projectEvent, projectEventCategories);
                initIfAgileProject(projectEvent, projectEventCategories);
            }
        } else {
            LOGGER.info("项目{}已初始化，跳过项目初始化", projectEvent.getProjectCode());
        }
        return message;
    }

    private void syncProjectCode(Long projectId, ProjectEvent projectEvent, List<ProjectEventCategory> projectEventCategories) {
        List<String> codes = projectEventCategories.stream().map(ProjectEventCategory::getCode).collect(Collectors.toList());
        if (codes.contains("N_TEST")) {
            ProjectInfoVO projectInfoVO = testServiceClientOperator.queryProjectInfo(projectId);
            if (!ObjectUtils.isEmpty(projectInfoVO)) {
                projectEvent.setProjectCode(projectInfoVO.getProjectCode());
            }
        }
    }
}
