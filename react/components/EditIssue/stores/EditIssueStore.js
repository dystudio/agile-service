import {
  observable, action, computed, toJS,
} from 'mobx';
import { find } from 'lodash';
import { uiApi } from '@/api';

const hiddenFields = ['issueType', 'summary', 'description', 'remainingTime', 'storyPoints'];
class EditIssueStore {
  // issue
  @observable issue = {};

  @action setIssue(data) {
    this.issue = data;
  }

  @computed get getIssue() {
    return this.issue;
  }

  // fields
  @observable fields = [];

  @action setIssueFields(issue, fields) {
    this.fields = fields;
    this.issue = issue;
  }

  getFieldByCode(code) {
    return find(this.fields, { fieldCode: code });
  }

  @computed get customFields() {
    return this.fields.filter((field) => !hiddenFields.includes(field.fieldCode));
  }

  @observable doc = {};

  @observable workLogs = [];

  @observable dataLogs = [];

  @observable linkIssues = [];

  @observable branch = {};

  @action setDoc(data) {
    this.doc = data;
  }

  @computed get getDoc() {
    return this.doc;
  }

  @action setWorkLogs(data) {
    this.workLogs = data;
  }

  @computed get getWorkLogs() {
    return this.workLogs.slice();
  }

  @action setDataLogs(data) {
    this.dataLogs = data;
  }

  @computed get getDataLogs() {
    return this.dataLogs;
  }

  @action setLinkIssues(data) {
    this.linkIssues = data;
  }

  @computed get getLinkIssues() {
    return this.linkIssues;
  }

  @action setBranches(data) {
    this.branches = data;
  }

  @computed get getBranch() {
    return this.branch;
  }

  setBranch(branch) {
    this.branch = branch || {};
  }

  @action initIssueAttribute(doc, workLogs, dataLogs, linkIssues) {
    this.doc = doc;
    this.workLogs = workLogs || [];
    this.dataLogs = dataLogs || [];
    this.linkIssues = linkIssues || [];
  }

  @observable createBranchShow = false;

  @observable linkBranchShow = false;

  @observable commitShow = false;

  @observable mergeRequestShow = false;

  @observable workLogShow = false;

  @observable createSubTaskShow = false;

  @observable createSubBugShow = false;

  @observable copyIssueShow = false;

  @observable transformSubIssueShow = false;

  @observable transformFromSubIssueShow = false;

  @observable relateStoryShow = false;

  @observable assigneeShow = false;

  @observable changeParentShow = false;

  @observable detailShow = false;

  @observable wsjfDTOShow = true;

  @observable tab = 'detail';

  @action
  setTab(tab) {
    if (tab) {
      this.tab = tab;
    } else {
      this.tab = 'detail';
    }
  }

  @action setCreateBranchShow(data) {
    this.createBranchShow = data;
  }

  @action setLinkBranchShow(data) {
    this.linkBranchShow = data;
  }

  @computed get getCreateBranchShow() {
    return this.createBranchShow;
  }

  @action setCommitShow(data) {
    this.commitShow = data;
  }

  @computed get getCommitShow() {
    return this.commitShow;
  }

  @action setMergeRequestShow(data) {
    this.mergeRequestShow = data;
  }

  @computed get getMergeRequestShow() {
    return this.mergeRequestShow;
  }

  @action setWorkLogShow(data) {
    this.workLogShow = data;
  }

  @computed get getWorkLogShow() {
    return this.workLogShow;
  }

  @action setCreateSubTaskShow(data) {
    this.createSubTaskShow = data;
  }

  @computed get getCreateSubTaskShow() {
    return this.createSubTaskShow;
  }

  @action setCreateSubBugShow(data) {
    this.createSubBugShow = data;
  }

  @computed get getCreateSubBugShow() {
    return this.createSubBugShow;
  }

  @action setCopyIssueShow(data) {
    this.copyIssueShow = data;
  }

  @computed get getCopyIssueShow() {
    return this.copyIssueShow;
  }

  @action setTransformSubIssueShow(data) {
    this.transformSubIssueShow = data;
  }

  @computed get getTransformSubIssueShow() {
    return this.transformSubIssueShow;
  }

  @action setTransformFromSubIssueShow(data) {
    this.transformFromSubIssueShow = data;
  }

  @computed get getTransformFromSubIssueShow() {
    return this.transformFromSubIssueShow;
  }

  @action setRelateStoryShow(data) {
    this.relateStoryShow = data;
  }

  @computed get getRelateStoryShow() {
    return this.relateStoryShow;
  }

  @action setAssigneeShow(data) {
    this.assigneeShow = data;
  }

  @computed get getAssigneeShow() {
    return this.assigneeShow;
  }

  @action setChangeParentShow(data) {
    this.changeParentShow = data;
  }

  @computed get getChangeParentShow() {
    return this.changeParentShow;
  }

  @action setDetailShow(detailShow) {
    this.detailShow = detailShow;
  }

  @computed get getDetailShow() {
    return this.detailShow;
  }

  @action setWSJFDTOShow(wsjfDTOShow) {
    this.wsjfDTOShow = wsjfDTOShow;
  }

  @computed get getWSJFDTOShow() {
    return this.wsjfDTOShow;
  }

  @observable backlogLinks = [];

  @action setBacklogLinks = (data) => {
    this.backlogLinks = data;
  }

  commentExpandMap = observable.map();

  commentReplysMap = observable.map();

  @observable linkedUI = [];

  @action setLinkedUI = (data) => {
    this.linkedUI = data;
  }

  @observable outside = false;

  @observable organizationId;

  @observable projectId;

  /**
   * api初始化， 外部与内部调用的接口在此进行判断
   * @param source
   */
  initApi(outside, organizationId, projectId) {
    this.outside = outside;
    if (this.outside) {
      this.organizationId = organizationId;
    }
    this.projectId = projectId;
  }

  @action
  destroy() {
    this.outside = false;
    this.projectId = undefined;
    this.organizationId = undefined;
  }

  getLinkedUI = () => {
    if (this.issue.issueId) {
      uiApi.project(this.projectId).org(this.organizationId).outside(this.outside).getLinkedUI(this.issue.issueId)
        .then((res) => {
          this.setLinkedUI(res || []);
        });
    }
  }

  refreshBranch=() => {}

  setRefreshBranch(refreshBranch) {
    this.refreshBranch = refreshBranch;
  }
}
export default EditIssueStore;
