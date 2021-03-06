import React from 'react';
import { Tooltip } from 'choerodon-ui';
import Star from '@/components/tag/star';
import PriorityTag from '@/components/PriorityTag';
import StatusTag from '@/components/StatusTag';
import TypeTag from '@/components/TypeTag';
import UserHead from '@/components/UserHead';
import { ISubIssue } from '@/common/types';

function getIssueTypeName(typeCode: string) {
  switch (typeCode) {
    case 'sub_task':
      return '子任务';
    case 'sub_bug':
      return '缺陷';
    default:
      return 'error';
  }
}
interface IssueListProps {
  showAssignee?: boolean
  showPriority?: boolean
  style?: React.CSSProperties
  i: number
  issue: ISubIssue
  onClick?: (issue: ISubIssue) => void
}
const IssueItem: React.FC<IssueListProps> = ({
  issue, i, showAssignee, showPriority, onClick, style,
}) => {
  const {
    starBeacon,
    issueTypeVO,
    issueNum,
    summary,
    typeCode,
    priorityVO,
    statusVO,
    assigneeId,
    assigneeName,
    loginName,
    realName,
    imageUrl,
  } = issue;
  const issueTypeName = getIssueTypeName(typeCode as string);
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        padding: '5px 0',
        cursor: 'pointer',
        borderBottom: '1px solid rgba(0, 0, 0, 0.12)',
        borderTop: !i ? '1px solid rgba(0, 0, 0, 0.12)' : '',
        ...style,
      }}
    >
      <Tooltip mouseEnterDelay={0.5} title={`任务类型: ${issueTypeName}`}>
        <div>
          <TypeTag
            data={issueTypeVO}
          />
        </div>
      </Tooltip>
      <Tooltip title={`${issueTypeName}编号概要： ${issueNum} ${summary}`}>
        <div style={{ marginLeft: 8, flex: 1, overflow: 'hidden' }}>
          <p
            className="c7n-issueList-summary"
            style={{
              color: '#3F51B5', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', marginBottom: 0,
            }}
            role="none"
            onClick={() => {
              onClick && onClick(issue);
            }}
          >
            {`${summary}`}
          </p>
        </div>
      </Tooltip>
      <Star disabled active={starBeacon} style={{ margin: '0 8px' }} />
      {
        showPriority && (
          <div style={{ marginRight: '8px', overflow: 'hidden' }}>
            <Tooltip mouseEnterDelay={0.5} title={`优先级： ${priorityVO.name}`}>
              <div>
                <PriorityTag
                  priority={priorityVO}
                />
              </div>
            </Tooltip>
          </div>
        )
      }
      {
        showAssignee ? (
          <div style={{ marginRight: 10, display: 'flex', justifyContent: 'flex-end' }}>
            <div>
              <UserHead
                hiddenText
                user={{
                  id: assigneeId,
                  name: assigneeName,
                  loginName,
                  realName,
                  avatar: imageUrl,
                }}
              />
            </div>
          </div>
        ) : null
      }
      <div style={{
        marginRight: '8px', display: 'flex', justifyContent: 'flex-end',
      }}
      >
        <Tooltip mouseEnterDelay={0.5} title={`任务状态： ${statusVO && statusVO.name}`}>
          <div>
            <StatusTag
              data={statusVO}
            />
          </div>
        </Tooltip>
      </div>
    </div>
  );
};

export default IssueItem;
