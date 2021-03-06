package io.choerodon.agile.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.math.BigDecimal;

public class SprintTaskVO {
    @ApiModelProperty(value = "冲刺")
    @Encrypt
    private Long sprintId;
    @ApiModelProperty(value = "冲刺名称")
    private String sprintName;
    @ApiModelProperty(value = "主要负责人")
    @Encrypt
    private Long mainResponsibleId;
    @ApiModelProperty(value = "经办人")
    private String name;
    @ApiModelProperty(value = "经办人登陆名")
    private String loginName;
    @ApiModelProperty(value = "经办人真实名字")
    private String realName;
    @ApiModelProperty(value = "计划工时")
    private BigDecimal remainingTime;
    @ApiModelProperty(value = "完成工时")
    private BigDecimal remainingTimeComplete;
    @ApiModelProperty(value = "主要负责人工时占比")
    private BigDecimal remainingTimeRate;
    @ApiModelProperty(value = "主要负责人完成工时占比")
    private BigDecimal remainingTimeCompleteRate;
    @ApiModelProperty(value = "主要负责人完成工时比计划工时")
    private BigDecimal remainingTimePlanCompleteRate;


    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public Long getMainResponsibleId() {
        return mainResponsibleId;
    }

    public void setMainResponsibleId(Long mainResponsibleId) {
        this.mainResponsibleId = mainResponsibleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public BigDecimal getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(BigDecimal remainingTime) {
        this.remainingTime = remainingTime;
    }

    public BigDecimal getRemainingTimeComplete() {
        return remainingTimeComplete;
    }

    public void setRemainingTimeComplete(BigDecimal remainingTimeComplete) {
        this.remainingTimeComplete = remainingTimeComplete;
    }

    public BigDecimal getRemainingTimeRate() {
        return remainingTimeRate;
    }

    public void setRemainingTimeRate(BigDecimal remainingTimeRate) {
        this.remainingTimeRate = remainingTimeRate;
    }

    public BigDecimal getRemainingTimeCompleteRate() {
        return remainingTimeCompleteRate;
    }

    public void setRemainingTimeCompleteRate(BigDecimal remainingTimeCompleteRate) {
        this.remainingTimeCompleteRate = remainingTimeCompleteRate;
    }

    public BigDecimal getRemainingTimePlanCompleteRate() {
        return remainingTimePlanCompleteRate;
    }

    public void setRemainingTimePlanCompleteRate(BigDecimal remainingTimePlanCompleteRate) {
        this.remainingTimePlanCompleteRate = remainingTimePlanCompleteRate;
    }
}
