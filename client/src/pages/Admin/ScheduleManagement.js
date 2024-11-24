import React, { useState } from 'react';
import { auth } from '../../firebaseConfig/Firebase';
import { db } from '../../firebaseConfig/Firebase';
import { collection, addDoc } from 'firebase/firestore';
import './ScheduleManagement.css';
import Layout from '../AdminLayout';

const ScheduleManagement = () => {
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedTime, setSelectedTime] = useState('');
  const [bus, setBus] = useState('');
  const [direction, setDirection] = useState('');
  const buses = ['Bus 1', 'Bus 2', 'Bus 3'];
  const directions = ['To Campus from Station', 'To Station from Campus'];

  const handleDateChange = (event) => {
    setSelectedDate(event.target.value);
  };

  const handleTimeChange = (event) => {
    setSelectedTime(event.target.value);
  };

  const submitSchedule = async (e) => {
    e.preventDefault();

    if (!selectedDate || !selectedTime || !bus || !direction) {
      alert('Please fill all fields.');
      return;
    }

    if (!auth.currentUser) {
      alert('You must be logged in to add a schedule.');
      return;
    }
    // Format the date to d/mm/yyyy
  const dateParts = selectedDate.split('-'); // Assuming selectedDate is in yyyy-mm-dd format
  const formattedDate = `${parseInt(dateParts[2], 10)}/${parseInt(dateParts[1], 10)}/${dateParts[0]}`;

    const AdminId = auth.currentUser.uid;

    const scheduleData = {
      date: formattedDate,
      time: selectedTime,
      bus,
      direction,
      totalSeatsBooked: 0,
      adminId: AdminId,
    };

    try {
      await addDoc(collection(db, 'schedules'), scheduleData);
      console.log('Schedule submitted:', scheduleData);
      alert('Schedule submitted successfully!');
      // Reset fields after successful submission
      setSelectedDate('');
      setSelectedTime('');
      setBus('');
      setDirection('');
    } catch (error) {
      console.error('Failed to add new schedule - ', error);
      alert('Failed to submit schedule. Please try again later.');
    }
  };

  return (
    <Layout title={"Schedule Manager"}>
    <div className="container">
      <main>
        <div className="schedule-management">
          <div className="button-section">
            <h2>Add a New Schedule</h2>
            <form onSubmit={submitSchedule}>
              <label>
                Select Date:
                <input type="date" value={selectedDate} onChange={handleDateChange} required />
              </label>

              <label>
                Select Time:
                <input type="time" value={selectedTime} onChange={handleTimeChange} required />
              </label>
<br></br>
              <label>
                Select Bus:
                <select value={bus} onChange={(e) => setBus(e.target.value)} required>
                  <option value="" disabled>Select Bus</option>
                  {buses.map((bus, index) => (
                    <option key={index} value={bus}>{bus}</option>
                  ))}
                </select>
              </label>

              <label>
                Select Direction:
                <select value={direction} onChange={(e) => setDirection(e.target.value)} required>
                  <option value="" disabled>Select Direction</option>
                  {directions.map((dir, index) => (
                    <option key={index} value={dir}>{dir}</option>
                  ))}
                </select>
              </label>

              <button type="submit">Submit Schedule</button>
            </form>
          </div>
        </div>
      </main>
    </div>
    </Layout>
  );
};

export default ScheduleManagement;
