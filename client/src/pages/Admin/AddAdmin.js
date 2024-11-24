import React, { useState } from "react";
import axios from "axios";
import { auth } from '../../firebaseConfig/Firebase';
import './AddAdmins.css';
import Layout from "../AdminLayout";
const AddAdmin = () => {
    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    

    const handleAddAdmin = async () => {
        if (!email) {
            setMessage("Please enter a valid email.");
            return;
        }

        try {
            const current_user = auth.currentUser;
         
                 


            
            const idToken = await current_user.getIdToken(true);


            if (idToken != null){
              sendAdminRequest(idToken, email)     
            }
            
        } catch (error) {
            console.error("Error granting admin privileges:", error);
            setMessage("Failed to grant admin privileges. Please try again.");
        }
    };

    const sendAdminRequest = async (idToken, email) => {
       // const serverUrl = "http://localhost:3012/setAdmin"; // Replace with your server URL
       const serverUrl ="https://us-central1-shuttleapp-75184.cloudfunctions.net/api/setAdmin";
       const payload = { email };
    
        try {
            const response = await axios.post(serverUrl, payload, {
                headers: {
                    Authorization: `Bearer ${idToken}`,
                },
            });
            alert(response.data.message);
        } catch (error) {
            console.error("Error setting admin role:", error);
            alert("Failed to grant admin privileges");
        }
    };

    return (
        <Layout title={"Add an Admin"}>
        <div className="container">
      <main>
                
                <div classname="admin-landing">
                  
                    <div className="buttons-section">
                 
                    <h2>Create an admin</h2>
                        <input
                    type="email"
                    placeholder="Enter email to make admin"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                                                    />
                <button  onClick={handleAddAdmin}>Make Admin</button>
                                                                {message && <p>{message}</p>}
            </div>
            </div>
            </main>
        </div>
        </Layout>
    );
};

export default AddAdmin;
