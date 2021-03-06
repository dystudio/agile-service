import React from 'react';
import STATUS_TYPE from '@/constants/STATUS_TYPE';
import { IStatus } from '@/common/types';
import BaseTag from '../base-tag';
import styles from './index.less';

interface Props {
  mode?: 'inline' | 'tag'
  code: IStatus['valueCode']
}
const renderInlineMode = ({ color, name }: { color: string, name: string }) => (
  <div className={styles.status_type_inline_mode}>
    <div
      className={styles.status_type_block}
      style={{
        background: color || 'rgb(255, 177, 0)',
      }}
    />
    {name}
  </div>
);
const StatusTypeTag: React.FC<Props> = ({ mode = 'inline', code }) => {
  const { color, name } = STATUS_TYPE[code];
  switch (mode) {
    case 'inline': return renderInlineMode({
      color,
      name,
    });
    case 'tag': return <BaseTag color={color} text={name} />;
    default: return null;
  }
};
export default StatusTypeTag;
