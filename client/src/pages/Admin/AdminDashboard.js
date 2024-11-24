import React from "react";
import { useNavigate } from "react-router-dom";
import "./AdminDashboard.css";
import Layout from "../Dashlayout";
const AdminLanding = () => {
  const navigate = useNavigate();

  const handleNavigation = (path) => {
    navigate(path);
  };

  const handleLogout = () => {
    // Clear user data logic
    alert("Logged out successfully");
    navigate("/"); // Redirect to login page
  };

  return (
    <Layout title={"Welcome Admin"}>

<div className="container">
<main>
    <div className="admin-landing">
     <h2>Admin Options</h2>
      <div className="buttons-section">
        <button onClick={() => handleNavigation("/add-admin")}>Make Admin</button>
        <button onClick={() => handleNavigation("/schedule-manager")}>
          Manage Bus Schedules
        </button>
        <button onClick={() => handleNavigation("/Manage-Drivers")}>
          Manage Drivers
        </button>
        <button onClick={() => handleNavigation("/Manage-Users")}>
          Manage Users
        </button>
       
        <button onClick={() => handleNavigation("/Admin-Report")}>
          View Reports
        </button>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </div>
</main>
</div>
</Layout>
  );
};

export default AdminLanding;
