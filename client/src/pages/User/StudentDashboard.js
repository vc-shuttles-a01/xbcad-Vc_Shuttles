import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { db } from "../../firebaseConfig/Firebase"; // Firebase config file
import { doc, getDoc } from "firebase/firestore";
import './StudentDashboard.css'; 
import { auth } from '../../firebaseConfig/Firebase';
import Layout from "../Dashlayout";


function StudentDashboard() {
  const [studentName, setStudentName] = useState("");
  const navigate = useNavigate();
  
  // Retrieve student information from Firebase
  useEffect(() => {
    const studentId = auth.currentUser; // Store in sessionStorage for React

    if (!studentId) {
      navigate("/"); // Redirect to login if not logged in
    } else {
      loadStudentName(studentId);
    }
  }, [navigate]);

  const loadStudentName = async (studentId) => {
    try {
      const studentRef = doc(db, "students", studentId); // Assumes Firestore document path
      const studentSnap = await getDoc(studentRef);

      if (studentSnap.exists()) {
        const studentData = studentSnap.data();
        setStudentName(`${studentData.firstName} ${studentData.lastName}`);
      } else {
        console.log("No such student!");
      }
    } catch (error) {
      console.error("Error getting student data: ", error);
    }
  };

  const handleLogout = () => {
    sessionStorage.removeItem("StudentID");
    navigate("/");
  };

  return (
   
      
      <Layout title={"Welcome"}> <div className="container">
      <main>
        <h2>Welcome back {studentName}!</h2>
        <p>What would you like to do today?</p>

        {/* Action Buttons */}
        <div className="card">
          <div className="card-header">
            <h5>Choose an Option</h5>
          </div>
          <div className="card-body">
            
              <button
                className="btn btn-primary btn-block mb-2"
                onClick={() => navigate("/book-shuttle")}
              >
                Book a Shuttle
              </button>
              <button
                className="btn btn-secondary btn-block mb-2"
                onClick={() => navigate("/shuttle-schedule")}
              >
                View Shuttle Schedules
              </button>
              <button
                className="btn btn-info btn-block mb-2"
                onClick={() => navigate("/my-trips")}
              >
                See My Trips
              </button>
              
              <button
                className="btn btn-danger btn-block mb-2"
                onClick={handleLogout}
              >
                Logout
              </button>
            </div>
          </div>
        
      </main> 
      </div>
      </Layout>
   
  );
}

export default StudentDashboard;
