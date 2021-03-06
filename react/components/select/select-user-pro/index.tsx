import React, { forwardRef, useState } from 'react';
import { Select } from 'choerodon-ui/pro';
import useSelect from '@/hooks/useSelectPro';
import { SelectProps } from 'choerodon-ui/pro/lib/select/Select';
import FlatSelect from '@/components/flat-select';
import useProjectUsers from '@/hooks/data/useProjectUsers';

export interface SelectUserProps extends Partial<SelectProps> {
  extraOptions?: {
    id: string,
    realName: string,
  }[],
  flat?: boolean
  projectId?: string
}

const SelectUser: React.FC<SelectUserProps> = forwardRef(({
  extraOptions, flat, projectId, ...otherProps
}, ref: React.Ref<Select>) => {
  const [text, setText] = useState('');
  const {
    fetchNextPage, data, hasNextPage,
  } = useProjectUsers({ param: text });
  const [props] = useSelect({
    data,
    textField: 'realName',
    valueField: 'id',
    hasNextPage,
    fetchNextPage,
    onSearch: setText,
    paging: true,
  });
  const Component = flat ? FlatSelect : Select;
  return (
    <Component
      ref={ref}
      clearButton={false}
      {...props}
      {...otherProps}
    />
  );
});
SelectUser.displayName = 'SelectUser';
export default SelectUser;
