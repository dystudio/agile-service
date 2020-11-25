import React from 'react';
import classNames from 'classnames';
import styles from './RowToggler.less';

interface RowTogglerProps {
  onClick: () => void
  collapsed: boolean
  level: number
}
const RowToggler: React.FC<RowTogglerProps> = ({ onClick, collapsed, level }) => (
  <div
    role="none"
    onClick={onClick}
    className={styles['row-toggler']}
  >
    <div className={classNames(styles['row-toggler'], {
      [styles.collapsed]: collapsed,
    })}
    >
      <i data-level={level}>
        {level <= 0 ? (
          <svg viewBox="0 0 1024 1024">
            <path
              d="M266.24 420.0448a30.8736 30.8736 0 0 1 52.736-21.8624L512 591.0016l192.9728-192.8192a30.8736 30.8736 0 1 1 43.7248 43.7248l-214.8352 214.6304a31.0272 31.0272 0 0 1-43.776 0L275.3024 441.9072a30.8736 30.8736 0 0 1-9.0624-21.8624z"
            />
          </svg>
        )
          : (
            <svg viewBox="0 0 1024 1024">
              <path
                d="M296.704 409.6a14.9504 14.9504 0 0 0-10.752 4.608 15.5648 15.5648 0 0 0 0.1536 21.7088l210.8416 212.0704a24.832 24.832 0 0 0 35.584-0.256l205.5168-211.968a15.5136 15.5136 0 0 0 4.352-10.752c0-8.4992-6.7584-15.4112-15.104-15.4112h-430.592z"
              />
            </svg>
          )}
      </i>
    </div>
  </div>
);
export default RowToggler;
