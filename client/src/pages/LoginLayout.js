
import "./Layout.css";
import { useNavigate } from 'react-router-dom';
import logo from '../Assets/shuttle_logo.png';

const Layout = ({ title, children }) => {
  const navigate = useNavigate();

  const handleBackClick = () => {
    navigate("/");
  };
 const log = () =>{
    navigate("/Register")
 }

  
  return (
    <div className="layout">
      {/* Header */}
      <header className="header">
        <div className="header-content">
        <div className="button-container">
            
      <button className="open-sidebar-btn"  onClick={log}>Register</button>
      <button className="button" onClick={handleBackClick}>Back</button>
    </div>            
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
