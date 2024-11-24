import React, { useEffect, useState, useCallback } from 'react';
import { getAuth, onAuthStateChanged } from 'firebase/auth';
import { getFirestore, collection, query, where, getDocs } from 'firebase/firestore';
import { useNavigate } from 'react-router-dom';
import Layout from '../Layout';

const MyTrips = () => {
 // const [trips, setTrips] = useState([]);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [sortedTrips, setSortedTrips] = useState([]);
 

  const navigate = useNavigate();

  // Handle the click event to navigate to the Student Dashboard
  
  // Memoize the loadTrips function to avoid unnecessary re-renders
  const loadTrips = useCallback((userId, db) => {
    const schedulesRef = collection(db, 'schedules');

    // Fetch all schedules
    getDocs(schedulesRef)
      .then((schedulesSnapshot) => {
        if (!schedulesSnapshot.empty) {
          const allTrips = [];

          schedulesSnapshot.forEach((scheduleDoc) => {
            const scheduleId = scheduleDoc.id;
            console.log(`Fetching bookings for schedule ID: ${scheduleId}`);

            // Fetch bookings related to the current schedule
            const bookingsRef = collection(db, 'schedules', scheduleId, 'bookings');
            const q = query(bookingsRef, where('userId', '==', userId));

            getDocs(q)
              .then((bookingsSnapshot) => {
                bookingsSnapshot.forEach((bookingDoc) => {
                  const seats = bookingDoc.data().seats || 'N/A';
                  const trip = {
                    id: bookingDoc.id,
                    scheduleId: scheduleId,
                    date: scheduleDoc.data().date || 'Unknown date',
                    time: scheduleDoc.data().time || 'Unknown time',
                    seats: seats,
                    direction: scheduleDoc.data().direction || 'No direction',
                    busNumber: scheduleDoc.data().bus || 'No bus number'
                  };

                  allTrips.push(trip);
                });

                // After fetching all bookings for this schedule, sort trips
                setSortedTrips(sortTrips(allTrips));
              })
              .catch((error) => {
                console.error(`Error fetching bookings for schedule ${scheduleId}`, error);
              });
          });
        } else {
          console.log('No schedules found');
        }
      })
      .catch((error) => {
        console.error('Error fetching schedules', error);
      });
  }, []);

  useEffect(() => {
    const auth = getAuth();
    const db = getFirestore();

    onAuthStateChanged(auth, (user) => {
      if (user) {
        setIsAuthenticated(true);
        loadTrips(user.uid, db);
      } else {
        // Redirect to login if user is not authenticated
        window.location.href = "/login.html";
      }
    });
  }, [loadTrips]);

  // Sort trips by departure date
  const sortTrips = (trips) => {
    if (!trips) return [];
    return trips
      .sort((a, b) => new Date(a.date) - new Date(b.date));
  };



  const formatTime = (time) => {
  const parsedTime = new Date(`1970-01-01T${time}Z`);
  return isNaN(parsedTime.getTime()) ? 'Invalid time' : parsedTime.toLocaleTimeString();
  };






  // Render trips in the ListView
  const renderTrips = () => {
    return sortedTrips.map((trip, index) => {
     // Determine if the trip is upcoming or completed
    const status = new Date(trip.date) >= new Date() ? 'Upcoming' : 'Completed';

    // Assign a class based on the status for different card designs
    
    const cardClass = trip.status === "Upcoming"
    ? "card-perfil-s"
    : trip.status === "Warning"
    ? "card-perfil-w"
    : "card-perfil-d";

    return (
      <div
        key={index}
        className={`card-perfil ${cardClass}`}
        style={{
          backgroundColor: '#E7D9A7', // Solid background color
          padding: '20px', // Adds padding around the content
          margin: '10px 0', // Adds spacing between cards
          borderRadius: '8px', // Optional, for rounded corners
          boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)', // Optional, adds a subtle shadow for depth
        }}
      >
        {/* Trip Details */}
        <section>
          <div className={`card-text-box`}>
            <p><h1>Trip #{trip.id}</h1></p>
            <p><strong>Date:</strong> {trip.date}</p>
            <p><strong>Route:</strong> {trip.direction}</p>
            <p><strong>Time:</strong> {formatTime(trip.time)}</p>
            <p><strong>Status:</strong> {status}</p>
            <p><strong>Seats:</strong> {trip.seats}</p>
            <p><strong>Bus Number:</strong> {trip.busNumber}</p>
          </div>
        </section>
      </div>
    );
    
    
    
  });
  };
  

  return (

    <Layout title={"My Trips"}>
    <div id="card-prefil card-prefil-w">
    {isAuthenticated && sortedTrips.length > 0 ? (
       <div className={`card-perfil `}>
        {renderTrips()}
      </div>
      ) : (
        <p id="no-trips-message">No trips found.</p>
      )}
    </div></Layout>
  );
};

export default MyTrips;
