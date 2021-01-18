package io.choerodon.agile.api.vo;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import java.util.Date;

/**
 * @author chihao.ran@hand-china.com
 * 2021/01/14 16:34
 */
public class StaticFileHeaderVO {
    @ApiModelProperty(value = "静态文件头id")
    @Encrypt
    private Long id;
    @ApiModelProperty(value = "项目id")
    private Long projectId;
    @ApiModelProperty(value = "问题id")
    @Encrypt
    private Long issueId;
    @ApiModelProperty(value = "ui/ux图原压缩包url")
    private String url;
    @ApiModelProperty(value = "文件名")
    private String fileName;
    @ApiModelProperty(value = "创建时间")
    private Date creationDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}