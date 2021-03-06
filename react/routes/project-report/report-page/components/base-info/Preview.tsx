import React from 'react';
import { observer } from 'mobx-react-lite';
import { useProjectReportContext } from '../../context';
import styles from './Preview.less';

const BaseInfoPreview: React.FC = () => {
  const { store } = useProjectReportContext();
  const { baseInfo } = store;

  return (
    <div className={styles.container}>
      <div className={styles.title}>
        {baseInfo?.title}
      </div>
      <section>
        <div className={styles.label}>
          报告说明:
        </div>
        <div>
          {baseInfo?.description || '-'}
        </div>
      </section>
      <section>
        <div className={styles.label}>
          收件人:
        </div>
        <div>
          {(baseInfo?.receiverList || []).map((user) => user.realName).join(',')}
        </div>
      </section>
      <section>
        <div className={styles.label}>
          抄送人:
        </div>
        <div>
          {(baseInfo?.ccList || []).map((user) => user.realName).join(',')}
        </div>
      </section>
    </div>
  );
};
export default observer(BaseInfoPreview);
