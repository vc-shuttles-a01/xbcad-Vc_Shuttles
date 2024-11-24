import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Register.css';
import { db } from '../firebaseConfig/Firebase'; // Firestore configuration
import { collection, addDoc, serverTimestamp } from 'firebase/firestore';
import Layout from './RegLayout';

function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const navigate = useNavigate();

 

  const validateInput = () => {
    if (!name.trim()) {
      alert('Please enter your name.');
      return false;
    }
    if (!email || !/\S+@\S+\.\S+/.test(email)) {
      alert('Enter a valid email address.');
      return false;
    }
    return true;
  };

  const handleSignUpRequest = async () => {
    if (validateInput()) {
      const signupRequest = {
        name,
        email,
        timestamp: serverTimestamp(), // For sorting by request time
      };

      try {
        await addDoc(collection(db, 'signupRequests'), signupRequest);
        alert('Signup request sent successfully.');
        navigate('/'); // Redirect to a "Thank You" page (optional)
      } catch (error) {
        console.error('Error sending signup request:', error);
        alert('Failed to send signup request. Please try again later.');
      }
    }
  };

  return (
    <Layout title={"Sign Up"}>
    <div className="signup-container">
    
      <label>Name</label>
      <input
        type="text"
        placeholder="Your name"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />
      <label>Email</label>
      <input
        type="email"
        placeholder="example@gmail.com"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />
      <button className="signup-button" onClick={handleSignUpRequest}>
        Submit Request
      </button>
    </div></Layout>
  );
}

export default Register;
