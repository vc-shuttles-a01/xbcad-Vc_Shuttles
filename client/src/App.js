import React, { useState } from "react";
import { GoogleMap, LoadScript, DirectionsRenderer } from "@react-google-maps/api";
import { collection, addDoc } from "firebase/firestore";
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { db } from './firebaseConfig/Firebase'; // Firebase config file
import Login from './pages/Login';
import StudentDashboard from './pages/User/StudentDashboard';
import BookShuttle from './pages/User/BookShuttle';
import './App.css';
import Register from "./pages/Register";
import ViewShuttleSchedules from "./pages/User/ViewShuttleShedules";
import MyTrips from "./pages/User/MyTrips";
import TrackShuttle from "./pages/User/TrackShuttle";


import AdminLanding from "./pages/Admin/AdminDashboard";
import AddAdmin from "./pages/Admin/AddAdmin";
import ScheduleManagement from "./pages/Admin/ScheduleManagement";
import AdminReports from "./pages/Admin/AdminReport";
import ManageUsers from "./pages/Admin/ManageUsers";
import ManageDrivers from "./pages/Admin/ManageDrivers";
import OpeningPage from "./pages/OpeningPage";



function App() {
  const [directionsResponse, setDirectionsResponse] = useState(null);
  const [origin, setOrigin] = useState("");
  const [destination, setDestination] = useState("");

  // Function to calculate and render the route
  const handleRoute = () => {
    const directionsService = new window.google.maps.DirectionsService();
    directionsService.route(
      {
        origin,
        destination,
        travelMode: window.google.maps.TravelMode.DRIVING,
      },
      (result, status) => {
        if (status === window.google.maps.DirectionsStatus.OK) {
          setDirectionsResponse(result);
          handleSaveRoute(origin, destination, result);
        } else {
          console.error(`Error fetching directions: ${status}`);
        }
      }
    );
  };

  // Function to save route to Firebase Firestore
  const handleSaveRoute = async (origin, destination, directionsResponse) => {
    try {
      await addDoc(collection(db, "routes"), {
        origin,
        destination,
        routeData: directionsResponse.routes[0].legs[0],
        timestamp: new Date(),
      });
      console.log("Route saved successfully!");
    } catch (e) {
      console.error("Error saving route to Firebase: ", e);
    }
  };

  return (
    <Router>
      <Routes>

<Route path="/" element={<OpeningPage/>}/>
        <Route path="/Login" element={<Login />} />
        <Route path= "/Register" element={<Register/>}/>t add
        <Route path="/StudentDashboard" element={<StudentDashboard />} />
      
        <Route path="/book-shuttle" element={<BookShuttle />} />
        <Route path="/shuttle-schedule" element={<ViewShuttleSchedules />} />
        <Route path="/my-trips" element={<MyTrips />} />
        <Route path="/track-shuttle" element={<TrackShuttle />} />
        <Route path="/admin-landing" element={<AdminLanding />}/>
        <Route path="/add-admin" element={<AddAdmin />}/>
        <Route path="/Schedule-manager" element={<ScheduleManagement />}/>
        <Route path="/Admin-Report" element={<AdminReports/>}/>
        <Route path="/Manage-Users" element={<ManageUsers />}/>
        <Route path="/Manage-Drivers" element={<ManageDrivers />}/>
        
        <Route
          path="/route-finder"
          element={
            <div className="App">
              <header className="App-header">
                <h1>Google Maps Route Finder</h1>
                <input
                  type="text"
                  placeholder="Enter origin"
                  value={origin}
                  onChange={(e) => setOrigin(e.target.value)}
                />
                <input
                  type="text"
                  placeholder="Enter destination"
                  value={destination}
                  onChange={(e) => setDestination(e.target.value)}
                />
                <button onClick={handleRoute}>Find Route</button>

                {/* Load Google Map */}
                <LoadScript googleMapsApiKey={process.env.REACT_APP_GOOGLE_MAPS_API_KEY}>
                  <GoogleMap
                    center={directionsResponse ? directionsResponse.routes[0].legs[0].start_location : { lat: 0, lng: 0 }}
                    zoom={10}
                    mapContainerStyle={{ height: "60vh", width: "80%" }}
                  >
                    {directionsResponse && <DirectionsRenderer directions={directionsResponse} />}
                  </GoogleMap>
                </LoadScript>
              </header>
            </div>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
