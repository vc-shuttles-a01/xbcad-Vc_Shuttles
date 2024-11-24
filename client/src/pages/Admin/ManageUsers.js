import React, { useState, useEffect } from "react";
import { auth, db } from "../../firebaseConfig/Firebase";
import axios from "axios";
import "./ManageUsers.css";
import Layout from "../AdminLayout";
import { collection, getDocs, doc, deleteDoc } from "firebase/firestore";

const ManageUsers = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [userEmails, setUserEmails] = useState([]);
  const [loading, setLoading] = useState(false);
  const [signupRequests, setSignupRequests] = useState([]);

  const createUserUrl = "https://us-central1-shuttleapp-75184.cloudfunctions.net/api/admin/createUser"; // Endpoint for creating users
  const fetchUsersUrl = "https://us-central1-shuttleapp-75184.cloudfunctions.net/api/getUsers"; // Endpoint to fetch users
  const deleteUserUrl = "https://us-central1-shuttleapp-75184.cloudfunctions.net/api/admin/deleteUser"; // Endpoint to delete a user

    useEffect(() => {
    fetchUserEmails();
    fetchSignupRequests();
  }, []);

  const fetchUserEmails = async () => {
    setLoading(true);

    const user = auth.currentUser;
    try {
      
      if (!user) throw new Error("No user is signed in.");

      const token = await user.getIdToken();

      const response = await axios.get(fetchUsersUrl, {
        headers: { Authorization: `Bearer ${token}` },
      });

      setUserEmails(response.data.map((user) => user.email));
    } catch (error) {
      console.error("Error fetching users:", error);
      alert("Failed to retrieve user list");
    } finally {
      setLoading(false);
    }
  };


  const fetchSignupRequests = async () => {
    try {
      const signupRequestsSnapshot = await getDocs(collection(db, "signupRequests"));
      const requests = signupRequestsSnapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      }));
      setSignupRequests(requests);
    } catch (error) {
      console.error("Error fetching signup requests:", error);
      alert("Failed to retrieve signup requests");
    }
  };
  const handleCreateUserFromRequest = (requestEmail) => {
    setEmail(requestEmail); // Pre-fill email for the Create User section
  };
  const handleDeclineRequest = async (requestId) => {
    try {
      await deleteDoc(doc(db, "signupRequests", requestId));
      alert("Signup request declined.");
      fetchSignupRequests(); // Refresh the list of signup requests
    } catch (error) {
      console.error("Error declining signup request:", error);
      alert("Failed to decline signup request");
    }
  };

  const createUser = async () => {
    if (!email || !password) {
      alert("Please enter a valid email and password");
      return;
    }

    try {
      const user = auth.currentUser;
      if (!user) throw new Error("No user is signed in.");

      const token = await user.getIdToken();

      const response = await axios.post(createUserUrl, { email, password }, {
        headers: { Authorization: `Bearer ${token}` },
      });

      alert(response.data.message);
      setEmail("");
      setPassword("");
      fetchUserEmails();
    } catch (error) {
      console.error("Error creating user:", error);
      alert("Failed to create user");
    }
  };

  const deleteUser = async (emailToDelete) => {
    try {
      const user = auth.currentUser;
      if (!user) throw new Error("No user is signed in.");

      const token = await user.getIdToken();

      const response = await axios.post(deleteUserUrl, { email: emailToDelete }, {
        headers: { Authorization: `Bearer ${token}` },
      });

      alert(response.data.message);
      fetchUserEmails();
    } catch (error) {
      console.error("Error deleting user:", error);
      alert("Failed to delete user");
    }
  };

  return (
    <Layout title={"Manage Users"}>
    <div className="manage-users">

<div className="manage-users-section">
  <h1>Create a New User</h1>
  <input
    type="text"
    value={email}
    onChange={(e) => setEmail(e.target.value)}
    placeholder="Enter User's Email"
    className="input-email"
  />
  <input
    type="password"
    value={password}
    onChange={(e) => setPassword(e.target.value)}
    placeholder="Enter User's Password"
    className="input-password"
  />
  <button onClick={createUser} className="add-user-button">
    Add User
  </button>
</div>

{/* Signup Requests Section */}
<div className="manage-users-section">
          <h1>Signup Requests</h1>
          {signupRequests.length === 0 ? (
            <p>No signup requests</p>
          ) : (
            <ul className="user-list">
              {signupRequests.map((request) => (
                <li key={request.id} className="user-item">
                 
                  <button
                    className="delete-button"
                    onClick={() => handleCreateUserFromRequest(request.email)}
                  >
                    Create User
                  </button>
                   <span>{request.email}</span>
                  <button
                    className="decline-button"
                    onClick={() => handleDeclineRequest(request.id)}
                  >
                    Decline Request
                  </button>
                  
                </li>
              ))}
            </ul>
          )}
        </div>

<div className="manage-users-section">
  <h1>View and Delete Users</h1>
  {loading ? (
    <p>Loading...</p>
  ) : (
    <ul className="user-list">
      {userEmails.map((userEmail, index) => (
        <li key={index} className="user-item">
          <span>{userEmail}</span>
          <button
            className="delete-button"
            onClick={() => deleteUser(userEmail)}
          >
            Delete
          </button>
        </li>
      ))}
    </ul>
  )}
</div>
</div>

    </Layout>
  );
};

export default ManageUsers;
