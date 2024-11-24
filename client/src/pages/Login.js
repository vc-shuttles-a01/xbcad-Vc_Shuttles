import React, { useState } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../firebaseConfig/Firebase';
import Layout from './LoginLayout';

import { useNavigate } from 'react-router-dom';
//import { getDoc, doc } from 'firebase/firestore'; // Import Firebase Firestore methods
import  './Login.css';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const navigate = useNavigate();

    const checkAdminClaims = async () => {
        const user = auth.currentUser;
    
        if (user) {
            try {
                // Retrieve ID token with claims
                const idTokenResult = await user.getIdTokenResult(true);
                const claims = idTokenResult.claims;
                
                // Check for admin and driver claims
                const isAdmin = claims.isAdmin || false; // Default to false if not present
                const isDriver = claims.isDriver || false; // Default to false if not present
    
                // Navigate based on user role
                if (isAdmin) {
                    alert("Admin Login Successful");
                    navigate('/admin-landing');
                
                } else if(isDriver){
                    alert("drivers do not have access to to the website ");
                }
                
                else  {
                    alert("User Login Successful");
                    navigate('/StudentDashboard');
                }
            } catch (error) {
                alert("Failed to retrieve login information: " + error.message);
            }
        } else {
            alert("No user is currently signed in.");
        }
    };


    // Function to get the user role from Firestore
    /*const getUserRole = async (email) => {
        try {
            const userDocRef = doc(auth.firestore, 'users', email); // Assuming you store users in a 'users' collection
            const userDoc = await getDoc(userDocRef);
            if (userDoc.exists()) {
                return userDoc.data().role; // Return the role (e.g., 'student', 'transportAdmin')
            } else {
                throw new Error('User not found');
            }
        } catch (error) {
            console.error("Error fetching user role:", error);
            return null; // Return null if an error occurs
        }
    };*/

    const handleLogin = async (e) => {
        e.preventDefault();
    
        try {
            signInWithEmailAndPassword(auth,username, password)
                .then((userCredential) => {
                    // User successfully signed in
                    const user = userCredential.user;
    
                    // Retrieve ID token
                    user.getIdToken(true)
                        .then((idToken) => {
                            console.log("ID Token:", idToken); // Log the token
                            checkAdminClaims(); // Call checkAdminClaims to verify roles
                        })
                        .catch((error) => {
                            console.error("Error retrieving ID token:", error);
                            setErrorMessage("Failed to retrieve ID token.");
                        });
                })
                .catch((error) => {
                    setErrorMessage("Invalid username or password. Please try again.");
                });
        } catch (error) {
            setErrorMessage("An unexpected error occurred. Please try again.");
        }
    };
    
    

    return (
        <Layout title={"Login"}>
            <div className="login-container">
                <div className="welcome-message">
                    Welcome to VCShuttles! Please login below.
                </div>
                <div className="login-form">
                    <h2>Login</h2>
                    <form onSubmit={handleLogin}>
                        <div className="input-group">
                            <label htmlFor="username">Username:</label>
                            <input
                                type="email"
                                id="username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                            />
                        </div>
                        <div className="input-group">
                            <label htmlFor="password">Password:</label>
                            <input
                                type="password"
                                id="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>
                        <div className="button-group">
                            <button type="submit" className="button">Login</button>
                            
                        </div>
                    </form>
                    {errorMessage && <p className="alert">{errorMessage}</p>}
                </div>
            </div>
        </Layout>
    );
};


export default Login;
