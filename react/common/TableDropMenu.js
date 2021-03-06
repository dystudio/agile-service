import React from 'react';
import { Dropdown, Icon } from 'choerodon-ui';
import { Permission } from '@choerodon/boot';
import './TableDropMenu.less';

/**
 * 表格中的下拉菜单
 * 4个参数
 * menu 菜单 Menu 组件构成的菜单项
 *      无传入则代表无下来菜单，仅渲染text内容
 * text 该列需渲染的文字部分
 * onClickEdit 点击文件编辑事件
 * isHasMenu 布尔类型，有menu传入时默认为true否则默认为false  true代表有下拉菜单
 *            此参数可以用于控制权限时 根据权限是否显示菜单
 * className  不传入则是默认样式
 */

const TableDropMenu = (props) => {
  const {
    menu, isHasMenu = !!menu, text, onClickEdit, className, menuPermissionProps = {},
  } = props;
  const { permission } = props;
  const {
    type, projectId, organizationId, service,
  } = permission || props;
  // 渲染文本
  const renderText = () => {
    const textA = (
      // eslint-disable-next-line jsx-a11y/interactive-supports-focus
      <a className="c7n-agile-table-cell-click" role="button" onClick={onClickEdit} onKeyDown={null}>
        {text}
      </a>
    );
    return onClickEdit ? textA : text;
  };// cursor: 'pointer'
  return (
    <div
      style={{
        display: 'flex', justifyContent: 'space-between', lineHeight: '24px', cursor: onClickEdit ? 'pointer' : 'inherit',
      }}
      className={className || 'table-drop-menu-base'}
    >
      <span style={{ display: 'flex' }}>
        {permission
          ? (
            <Permission
              type={type}
              projectId={projectId}
              organizationId={organizationId}
              service={service}
            >
              {renderText()}
            </Permission>
          )
          : renderText()}
      </span>
      <Permission {...menuPermissionProps}>
        {isHasMenu && menu
          ? (
            <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
              <Dropdown overlay={menu} trigger="click">
                <Icon shape="circle" type="more_vert" />
              </Dropdown>
            </div>
          ) : null}
      </Permission>
    </div>
  );
};

export default TableDropMenu;
