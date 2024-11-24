import React, { useEffect } from "react";
import "./Layout.css";
import { useNavigate } from 'react-router-dom';
import logo from '../Assets/shuttle_logo.png';

const Layout = ({ title, children }) => {
  const navigate = useNavigate();

  const handleBackClick = () => {
    navigate("/admin-landing");
  };
  const handleNavigation = (path) => {
    navigate(path);
  };
  // Sidebar toggle functionality
  useEffect(() => {
    const openSidebarBtn = document.querySelector(".open-sidebar-btn");
    const closeSidebarBtn = document.querySelector(".close-sidebar-btn");
    const sidebar = document.querySelector(".sidebar");
    const layout = document.querySelector(".layout");

    const openSidebar = () => {
      sidebar.classList.add("open");
      layout.style.transform = "translateX(250px)"; // Shift the layout to the right
    };

    const closeSidebar = () => {
      sidebar.classList.remove("open");
      layout.style.transform = "translateX(0)"; // Reset the layout position
    };

    openSidebarBtn.addEventListener("click", openSidebar);
    closeSidebarBtn.addEventListener("click", closeSidebar);

    // Cleanup event listeners on component unmount
    return () => {
      openSidebarBtn.removeEventListener("click", openSidebar);
      closeSidebarBtn.removeEventListener("click", closeSidebar);
    };
  }, []); // Empty dependency array ensures this runs once on mount

  return (
    <div className="layout">
      {/* Header */}
      <header className="header">
        <div className="header-content">
        <div className="button-container">
      <button className="open-sidebar-btn">☰</button>
      <button className="button" onClick={handleBackClick}>Back</button>
    </div>          
          <h1>{title}</h1>
          <img src={logo} alt="Shuttle Logo" className="logo" />
        </div>
      </header>

      {/* Sidebar */}
      <div className="sidebar">
        <button className="close-sidebar-btn">✖ </button>
        <nav>
          <ul>
          <li onClick={() => handleNavigation("/admin-landing")}>Home</li>
        <li onClick={() => handleNavigation("/add-admin")}>Create a Admin</li>
        <li onClick={() => handleNavigation("/Manage-Users")}>Manage Users</li>
        <li onClick={() => handleNavigation("/Manage-Drivers")}>Manage Drivers</li>
        <li onClick={() => handleNavigation("/Schedule-manager")}>Manage Schedules</li>
        <li onClick={() => handleNavigation("/Admin-report")}>Reports</li>
          </ul>
        </nav>
      </div>

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
