import React, { useState, useEffect } from "react";
import { auth } from "../../firebaseConfig/Firebase";
import "./ManageDrivers.css";
import Layout from "../AdminLayout";
import axios  from "axios";

const ManageDrivers = () => {
  const [email, setEmail] = useState("");
  const [driverEmails, setDriverEmails] = useState([]);
  const [loading, setLoading] = useState(false);
  const serverUrl = "https://us-central1-shuttleapp-75184.cloudfunctions.net/api/admin/setDriver";
  const fetchDriversUrl = "https://us-central1-shuttleapp-75184.cloudfunctions.net/api/getDrivers";
  const deleteDriverUrl =  "https://api-qeqahdzppa-uc.a.run.app/admin/deleteUser";
  const createDriverUrl ="https://us-central1-shuttleapp-75184.cloudfunctions.net/api/admin/setDriver";

  // Fetch driver emails on component load
  useEffect(() => {
    fetchDriverEmails();
  }, []);

  const fetchDriverEmails = async () => {
    setLoading(true);
    const user = auth.currentUser;
    try {
      if (!user) throw new Error("No user is signed in.");

      const token = await user.getIdToken();
      const response = await fetch(fetchDriversUrl, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.ok) {
        const data = await response.json();
        setDriverEmails(data.map((driver) => driver.email));
      } else {
        console.error("Failed to fetch drivers");
      }
    } catch (error) {
      console.error("Error fetching drivers:", error);
    } finally {
      setLoading(false);
    }
  };

  const assignDriverRole = async () => {
    console.log("Assigning driver role for email:", email); // Debugging line
    const currentUser = auth.currentUser;
  
    try {
      if (!currentUser) throw new Error("No user is logged in");
  
      const idToken = await currentUser.getIdToken(true);
      if (idToken) {
        await sendDriverRequest(idToken, email);
      } else {
        alert("Failed to get token");
      }
    } catch (error) {
      console.error("Error fetching token:", error);
      alert(`Error fetching token: ${error.message}`);
    }
  };
  
  
  const sendDriverRequest = async (idToken, email) => {
    const data = { email };
  
    try {
      const response = await axios.post(serverUrl,data, {
       
        headers: {
          
          Authorization: `Bearer ${idToken}`,
        },
        
      });
      alert(response.data.message);
  
      
        fetchDriverEmails(); // Refresh the list of drivers
      
    } catch (error) {
      console.error("Error setting driver role:", error);
      alert("Failed to grant driver privileges");
    }
  };
  
  const deleteDriver = async (emailToDelete) => {
    const confirmDelete = window.confirm(`Are you sure you want to delete driver: ${emailToDelete}?`);
    if (!confirmDelete) return;
  
    try {
      const user = auth.currentUser;
      if (!user) throw new Error("No user is signed in.");
  
      const token = await user.getIdToken(true);
  
      const response = await axios.post(deleteDriverUrl, { email: emailToDelete }, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });
  
      if (response.status === 200) {
        alert(response.data.message);
        setDriverEmails((prev) => prev.filter((email) => email !== emailToDelete));
      } else {
        alert("Failed to delete driver");
      }
    } catch (error) {
      console.error("Error deleting driver:", error?.response?.data || error.message);
      alert(error?.response?.data?.message || "An error occurred while deleting the driver.");
    }
  };
  
  

  const getIdToken = async () => {
    const user = auth.currentUser;
    if (!user) throw new Error("User is not logged in");
    return await user.getIdToken();
  };

  const validateEmail = (email) => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  };

  return (

<Layout title={"Manage Drivers"}>

    <div className="manage-drivers">
    
       {/*second div goes here*/}

<h2>create a driver</h2>
<div className="manage-drivers-2">
      <input
        type="email"
        placeholder="Enter Driver's Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        className="email-input"
      />
      <button
  onClick={() => assignDriverRole()}
  className="assign-button"
  disabled={loading || !validateEmail(email)}
>
  {loading ? "Loading..." : "Create Driver"}
</button>

</div>
      {/*third div goes here*/}
<h2 className="help-text">View and delete drivers</h2>
      <div className="manage-drivers-3">
      {loading && <p>Loading drivers...</p>}
      
      <ul className="drivers-list">
        {driverEmails.length > 0 ? (
          driverEmails.map((driverEmail, index) => (
            <li
              key={index}
              onDoubleClick={() => deleteDriver(driverEmail)}
              className="driver-item"
            >
              {driverEmail}
              <button  key={index} onClick={() => deleteDriver(driverEmail)}>delete</button>
            </li>
          ))
        ) : (
          <p>No drivers found</p>
        )}
      </ul>
</div>



    </div></Layout>
  );
};

export default ManageDrivers;
