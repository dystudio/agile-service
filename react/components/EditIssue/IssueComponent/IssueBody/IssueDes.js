import React, {
  useState, useEffect, useContext,
} from 'react';
import {
  Icon, Button, Tooltip,
} from 'choerodon-ui';

import WYSIWYGViewer from '@/components/WYSIWYGViewer';
import WYSIWYGEditor from '@/components/WYSIWYGEditor';
import { text2Delta, uploadAndReplaceImg } from '@/utils/richText';
import { issueApi } from '@/api';
import FullEditor from '../../../FullEditor';
import EditIssueContext from '../../stores';
import Divider from './Divider';

const IssueDes = ({ reloadIssue, setIssueLoading }) => {
  const [editDesShow, setEditDesShow] = useState(false);
  const [fullEdit, setFullEdit] = useState(false);
  const [editDes, setEditDes] = useState('');
  const { store, disabled, descriptionEditRef } = useContext(EditIssueContext);
  const { description, descriptionTemplate, typeCode } = store.getIssue;
  useEffect(() => {
    setEditDes(description);
    setEditDesShow(false);
  }, [description]);

  const updateIssueDes = async (value) => {
    const { issueId, objectVersionNumber } = store.getIssue;

    const newValue = value || editDes;
    try {
      setIssueLoading(true);
      const text = await uploadAndReplaceImg(newValue);
      const obj = {
        issueId,
        objectVersionNumber,
        description: text,
      };
      await issueApi.update(obj);
      setEditDesShow(false);
      setFullEdit(false);
      if (reloadIssue) {
        reloadIssue(issueId);
      }
    } catch (error) {
      setIssueLoading(false);
    }
  };
  useEffect(() => {
    descriptionEditRef.current = editDesShow;
  }, [descriptionEditRef, editDesShow]);
  const renderDes = () => {
    if (editDesShow === undefined) {
      return null;
    }
    if (!description || editDesShow) {
      return (
        editDesShow && (
          <div
            className="line-start mt-10 two-to-one"
          >
            <div style={{
              width: '100%',
              position: 'absolute',
              top: 0,
              bottom: 0,
            }}
            >
              <WYSIWYGEditor
                autoFocus
                bottomBar
                value={text2Delta(editDes) || text2Delta(descriptionTemplate)}
                style={{
                  height: '100%', width: '100%',
                }}
                onChange={(value) => {
                  setEditDes(value);
                }}
                handleDelete={() => {
                  setEditDesShow(false);
                  setEditDes(description);
                }}
                handleSave={updateIssueDes}
              />
            </div>
          </div>
        )
      );
    }
    return (
      <div className="c7n-content-wrapper">
        <div
          className="mt-10 c7n-description"
          role="none"
        >
          <WYSIWYGViewer data={description} />
        </div>
      </div>
    );
  };

  const callback = (value) => {
    updateIssueDes(value);
  };

  return (
    <div id="des">
      <Divider />
      <div className="c7n-title-wrapper">
        <div className="c7n-title-left">
          {/* <Icon type="subject c7n-icon-title" /> */}
          <span>描述</span>
        </div>
        {/* <div style={{
          flex: 1, height: 1, borderTop: '1px solid rgba(0, 0, 0, 0.08)', marginLeft: '14px',
        }}
        /> */}
        {!disabled && (
          <div className="c7n-title-right" style={{ marginLeft: '14px', position: 'relative' }}>
            <Tooltip title="全屏编辑" getPopupContainer={(triggerNode) => triggerNode.parentNode}>
              <Button style={{ padding: '0 6px' }} className="leftBtn" funcType="flat" onClick={() => setFullEdit(true)}>
                <Icon type="zoom_out_map icon" style={{ marginRight: 2 }} />
              </Button>
            </Tooltip>
            <Tooltip placement="topRight" autoAdjustOverflow={false} title="编辑">
              <Button
                style={{ padding: '0 6px' }}
                className="leftBtn"
                funcType="flat"
                onClick={() => {
                  setEditDesShow(true);
                  setEditDes(description);
                }}
              >
                <Icon
                  className="c7n-des-fullEdit"
                  role="none"
                  type="mode_edit icon"
                />
              </Button>
            </Tooltip>
          </div>
        )}
      </div>
      {renderDes()}
      {
        fullEdit ? (
          <FullEditor
            autoFocus
            initValue={text2Delta(editDes)}
            visible={fullEdit}
            onCancel={() => setFullEdit(false)}
            onOk={callback}
          />
        ) : null
      }
    </div>
  );
};

export default IssueDes;
