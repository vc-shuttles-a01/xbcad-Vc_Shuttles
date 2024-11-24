
import "./Layout.css";

import logo from '../Assets/shuttle_logo.png';

const Layout = ({ title, children }) => {
 

  

  
  return (
    <div className="layout">
      {/* Header */}
      <header className="header">
        <div className="header-content">
                 
          <h1>{title}</h1>
          <img src={logo} alt="Shuttle Logo" className="logo" />
        </div>
      </header>

     

      {/* Main Content */}
      <main className="content">{children}</main>

      {/* Footer */}
      <footer className="footer">
        <p>© 2024 - Varsity College</p>
      </footer>
    </div>
  );
};

export default Layout;
