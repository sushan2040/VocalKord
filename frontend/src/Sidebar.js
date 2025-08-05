import React from 'react';
import { slide as Menu } from 'react-burger-menu';
import './Sidebar.css';

export default props => {
  return (
    <Menu>
      <a className="menu-item" href="/">
        Home
      </a>
      <a className="menu-item" href="/translate-documents">
        Translate Documents
      </a>
      <a className="menu-item" href="/speech-to-text">
       Voice to text 
      </a>
    </Menu>
  );
};