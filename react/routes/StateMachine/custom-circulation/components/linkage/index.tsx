import React, { useMemo, useEffect } from 'react';
import { Form, DataSet, Select } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import { find } from 'lodash';
import { useIssueTypes } from '@/hooks';
import { IIssueType } from '@/common/types';
import styles from './index.less';

// @ts-ignore
const Linkage = ({ modal, record, selectedType }) => {
  const [issueTypes] = useIssueTypes();
  const linkageDataSet = useMemo(() => new DataSet({
    autoCreate: true,
    fields: [
      { name: 'story', label: '指定状态' },
      { name: 'task', label: '指定状态' },
      { name: 'bug', label: '指定状态' },
    ],
  }), []);

  useEffect(() => {
    const handleOk = async () => {
      const data = linkageDataSet.toData();
      // @ts-ignore
      const { story, task, bug } = data && data[0];
      console.log(data[0]);
    };
    if (modal) {
      modal.handleOk(handleOk);
    }
  }, [linkageDataSet, modal]);

  const selectedTypeCode = find(issueTypes, (
    item: IIssueType,
  ) => item.id === selectedType)?.typeCode;
  return (
    <div className={styles.linkage}>
      <div className={styles.tip}>当工作项流转到此状态后，关联的父任务状态设置。</div>
      <Form dataSet={linkageDataSet}>
        <div>
          <p className={styles.label}>父级为故事类型</p>
          <Select name="story" />
        </div>
        <div>
          <p className={styles.label}>父级为任务类型</p>
          <Select name="task" />
        </div>
        {
          selectedTypeCode === 'sub_task' && (
            <div>
              <p className={styles.label}>父级为缺陷类型</p>
              <Select name="bug" />
            </div>
          )
        }
      </Form>
    </div>
  );
};

export default observer(Linkage);
