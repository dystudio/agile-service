import React, { useState, useEffect } from 'react';
import { pageConfigApi, PageConfigIssueType } from '@/api';
import { Modal } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import useQuery from '@/hooks/useQuery';
import { usePageIssueTypeStore } from '../../stores';
import Switch from './Switch';

interface IssueOption {
  value: string,
  text: string,
  type: 'organization' | 'common',
}

function PageSwitch() {
  const [switchOptions, setSwitchOption] = useState<Array<IssueOption>>();
  const params = useQuery();
  const { pageIssueTypeStore } = usePageIssueTypeStore();
  const handleSelectBox = (val: any) => {
    if (pageIssueTypeStore.getDirty) {
      Modal.confirm({
        title: '是否放弃更改',
        className: 'c7n-agile-page-config-page-issue-type-switch-modal',
        children: (
          <div>
            页面有未保存的内容,是否放弃更改？
          </div>
        ),
        onOk: () => pageIssueTypeStore.setCurrentIssueType(val as PageConfigIssueType),
      });
      return false;
    }
    pageIssueTypeStore.setCurrentIssueType(val as PageConfigIssueType);
    return true;
  };
  useEffect(() => {
    if (pageIssueTypeStore.currentIssueType !== '') {
      pageIssueTypeStore.loadData();
    }
  }, [pageIssueTypeStore.currentIssueType]);
  // 加载全部字段 用于增添已有字段
  useEffect(() => {
    pageIssueTypeStore.setLoading(true);
    pageConfigApi.loadAvailableIssueType().then((res) => {
      const issueTypeId = params.get('issueTypeId');
      let currentType;
      if (issueTypeId) {
        currentType = res.find((t) => String(t.id) === issueTypeId);
      }
      pageIssueTypeStore.init((currentType ?? res[0]).typeCode as PageConfigIssueType);
      setSwitchOption(res.map((type) => ({
        value: type.typeCode,
        text: type.name,
        type: 'common',
      })));
    });
  }, []);
  return (
    <Switch
      value={pageIssueTypeStore.currentIssueType}
      options={switchOptions || []}
      onChange={handleSelectBox}
    />
  );
}

export default observer(PageSwitch);
