package io.choerodon.agile.infra.dto;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author chihao.ran@hand-china.com
 * 2021/01/13 16:07
 */
@Table(name = "agile_static_file_header")
@ModifyAudit
@VersionAudit
public class StaticFileHeaderDTO extends AuditDomain {
    @Id
    @GeneratedValue
    private Long id;
    private Long projectId;
    private Long organizationId;
    private Long issueId;
    private String url;
    private String fileName;

    public StaticFileHeaderDTO() {
    }

    public StaticFileHeaderDTO(Long projectId, Long organizationId, Long issueId, String url, String fileName) {
        this.projectId = projectId;
        this.organizationId = organizationId;
        this.issueId = issueId;
        this.url = url;
        this.fileName = fileName;
    }

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

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
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
}
