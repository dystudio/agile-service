import React, { useCallback, useEffect, useState } from 'react';
import { Icon, Popconfirm } from 'choerodon-ui';
import { text2Delta } from '@/utils/richText';
import WYSIWYGEditor from '@/components/WYSIWYGEditor';
import WYSIWYGViewer from '@/components/WYSIWYGViewer';
import DatetimeAgo from '@/components/CommonComponent/DatetimeAgo';
import UserHead from '@/components/UserHead';
// @ts-ignore
import Delta from 'quill-delta';
import './Comment.less';
import { IComment } from '@/common/types';
import { useDetailContext } from '../../context';

interface Props {
  hasPermission: boolean
  comment: IComment
  onDelete: Function
  onUpdate: (delta: Delta) => Promise<any>
}
const Comment: React.FC<Props> = ({
  hasPermission, comment, onDelete, onUpdate,
}) => {
  const { outside } = useDetailContext();
  const [editing, setEditing] = useState(false);
  const [value, setValue] = useState<Delta>();
  useEffect(() => {
    const delta = text2Delta(comment.commentText);
    setValue(delta);
  }, [comment.commentText]);
  const handleUpdate = useCallback(async (delta: Delta) => {
    await onUpdate(delta);
    setEditing(false);
  }, [onUpdate]);

  const canEditOrDelete = hasPermission;

  const handleChange = useCallback((delta: Delta) => {
    setValue(delta);
  }, []);
  return (
    <div
      className="c7n-comment"
    >
      <div className="line-justify">
        <div className="c7n-title-commit" style={{ flex: 1 }}>
          <UserHead
            // @ts-ignore
            user={{
              id: comment.userId,
              name: comment.userName,
              realName: comment.userRealName,
              loginName: comment.userLoginName,
              avatar: comment.userImageUrl,
            }}
            color="#3f51b5"
          />
          <div style={{ color: 'rgba(0, 0, 0, 0.65)', marginLeft: 15 }}>
            <DatetimeAgo
              date={comment.lastUpdateDate}
            />
          </div>
        </div>
        <div className="c7n-action">
          {
            hasPermission && (
              <Icon
                type="mode_edit mlr-3 pointer"
                onClick={() => {
                  if (canEditOrDelete) {
                    setEditing(true);
                  }
                }}
              />
            )
          }

          {
            hasPermission && (
              <Popconfirm
                title="确认要删除该评论吗?"
                placement="left"
                onConfirm={() => onDelete()}
                okText="删除"
                cancelText="取消"
                okType="danger"
              >
                <Icon
                  type="delete_forever mlr-3 pointer"
                />
              </Popconfirm>
            )
          }
        </div>
      </div>
      {
        editing ? (
          <div className="c7n-conent-commit" style={{ marginTop: 10 }}>
            <WYSIWYGEditor
              autoFocus
              bottomBar
              value={value}
              onChange={handleChange}
              style={{ height: 200, width: '100%' }}
              handleDelete={() => {
                setEditing(false);
              }}
              handleSave={handleUpdate}
            />
          </div>
        ) : (
          <div style={{ marginTop: 10 }}>
            <WYSIWYGViewer data={comment.commentText} />
          </div>
        )
      }
    </div>
  );
};

export default Comment;
