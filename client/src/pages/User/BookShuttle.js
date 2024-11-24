import React, { useState, useEffect } from 'react';
import { auth } from "../../firebaseConfig/Firebase";
import { db } from '../../firebaseConfig/Firebase';
import { collection, addDoc, updateDoc, doc, getDocs } from 'firebase/firestore';
import Layout from '../Layout';
import './BookShuttle.css';

const BookShuttle = () => {
  const [schedulesList, setSchedulesList] = useState([]);
  const [selectedScheduleId, setSelectedScheduleId] = useState(null);
  const [seatNumber, setSeatNumber] = useState(1);
  const [currentSeatCount, setCurrentSeatCount] = useState(0);
  
  useEffect(() => {
    fetchSchedules();
  }, []);

  const fetchSchedules = async () => {
    try {
      const schedulesSnapshot = await getDocs(collection(db, 'schedules'));
      const schedules = schedulesSnapshot.docs.map(docSnapshot => ({
        id: docSnapshot.id,
        ...docSnapshot.data(),
      }));
      setSchedulesList(schedules);
    } catch (error) {
      console.error('Error fetching schedules', error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!selectedScheduleId) {
      alert("Please select a schedule first.");
      return;
    }

    const numberOfSeats = parseInt(seatNumber);
    if (isNaN(numberOfSeats) || numberOfSeats <= 0) {
      alert("Please enter a valid number of seats.");
      return;
    }

    const totalSeatsAfterBooking = currentSeatCount + numberOfSeats;
    if (currentSeatCount >= 16) {
      alert("Shuttle is already fully booked.");
      return;
    } else if (totalSeatsAfterBooking > 16) {
      alert(`Only ${16 - currentSeatCount} seats are available.`);
      return;
    }

    const userId = auth.currentUser?.uid;
    if (!userId) {
      alert("User not authenticated.");
      return;
    }

    const bookingData = {
      userId: userId,
      seats: numberOfSeats,
      scheduleId: selectedScheduleId
    };

    try {
      await addDoc(collection(db, 'schedules', selectedScheduleId, 'bookings'), bookingData);
      await updateTotalSeats(numberOfSeats);
      alert("Booking successful!");
    } catch (error) {
      console.error("Booking failed:", error);
    }
  };

  const updateTotalSeats = async (seatsToAdd) => {
    try {
      await updateDoc(doc(db, 'schedules', selectedScheduleId), {
        totalSeatsBooked: currentSeatCount + seatsToAdd
      });
      setCurrentSeatCount(prevCount => prevCount + seatsToAdd);
    } catch (error) {
      console.error("Failed to update total seats booked", error);
    }
  };

  return (
    <Layout title={"Book a Shuttle"}>
              <h1 className='h'>Fill in the form below to book your shuttle ride.</h1>

      <div className="form-1">

        <form onSubmit={handleSubmit}>
          {/* Single Form Group with Column Layout */}
          <div className="form-group1">
            {/* Schedule Selection */}
            <div className="form-field">
              <label>Select Shuttle Schedule</label>
              <select onChange={(e) => setSelectedScheduleId(e.target.value)}>
                <option value="">Select a Schedule</option>
                {schedulesList.map(schedule => (
                  <option key={schedule.id} value={schedule.id}>
                    {`${schedule.date}, ${schedule.time} - ${schedule.bus} (${schedule.direction})`}
                  </option>
                ))}
              </select>
            </div>

            {/* Seat Number */}
            <div className="form-field">
              <label>Number of Seats</label>
              <input 
                type="number" 
                value={seatNumber} 
                onChange={(e) => setSeatNumber(e.target.value)} 
                placeholder="Number of seats" 
                min="1"
              />
            </div>

            {/* Submit Button */}
            <div className="form-field">
              <button type="submit">Submit Booking</button>
            </div>
          </div>
        </form>
      </div>
    </Layout>
  );
};

export default BookShuttle;
